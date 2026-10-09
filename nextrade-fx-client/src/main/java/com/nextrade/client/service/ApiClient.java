package com.nextrade.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrade.contracts.auth.AuthResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Small async HTTP client for the JavaFX authentication flow and generic API calls. */
public final class ApiClient {
    private final HttpClient client;
    private final URI base;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiClient(String baseUrl) {
        this(URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/"));
    }

    public ApiClient(URI base) {
        String value = base.toString();
        this.base = URI.create(value.endsWith("/") ? value : value + "/");
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public CompletableFuture<HttpResponse<String>> request(String method, String path, String json, String bearer) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(base.resolve(path.startsWith("/") ? path.substring(1) : path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Request-Id", java.util.UUID.randomUUID().toString());
        if (bearer != null && !bearer.isBlank()) builder.header("Authorization", "Bearer " + bearer);
        HttpRequest.BodyPublisher body = json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json);
        switch (method.toUpperCase(java.util.Locale.ROOT)) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(body);
            case "PUT" -> builder.PUT(body);
            case "DELETE" -> builder.method("DELETE", body);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<AuthResponse> login(String username, String password, boolean rememberMe) {
        return postJson("/api/v1/auth/login", Map.of("username", username, "password", password))
                .thenApply(this::readAuthResponse);
    }

    public CompletableFuture<HttpResponse<String>> forgotPassword(String email) {
        return postJson("/api/v1/auth/forgot-password", Map.of("email", email));
    }

    public CompletableFuture<AuthResponse.TokenVerificationResponse> verifyResetToken(String token) {
        return postJson("/api/v1/auth/verify-reset-token", Map.of("token", token))
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        return new AuthResponse.TokenVerificationResponse.Invalid(false, "Invalid or expired token");
                    }
                    try {
                        JsonNode root = mapper.readTree(response.body());
                        boolean valid = root.path("valid").asBoolean(root.has("message") && !root.has("error"));
                        return valid ? new AuthResponse.TokenVerificationResponse.Valid(true, root.path("message").asText("Token valid"))
                                : new AuthResponse.TokenVerificationResponse.Invalid(false, root.path("message").asText("Invalid or expired token"));
                    } catch (Exception e) {
                        return new AuthResponse.TokenVerificationResponse.Invalid(false, "Could not read token verification response");
                    }
                });
    }

    public CompletableFuture<HttpResponse<String>> resetPassword(String token, String newPassword) {
        return postJson("/api/v1/auth/reset-password", Map.of("token", token, "newPassword", newPassword));
    }

    private CompletableFuture<HttpResponse<String>> postJson(String path, Object body) {
        try {
            return request("POST", path, mapper.writeValueAsString(body), null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private AuthResponse readAuthResponse(HttpResponse<String> response) {
        try {
            JsonNode root = mapper.readTree(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && root.hasNonNull("accessToken")) {
                return new AuthResponse.Success(root.path("accessToken").asText(), root.path("refreshToken").asText(""),
                        root.path("userId").asText(""), root.path("role").asText("TRADER"), root.path("sessionId").asText(""));
            }
            if (response.statusCode() >= 200 && response.statusCode() < 300 && root.hasNonNull("userId")) {
                return new AuthResponse.PendingVerification(root.path("userId").asText(), root.path("message").asText("Verification required"));
            }
            return new AuthResponse.Failure(root.path("code").asText("HTTP_" + response.statusCode()),
                    root.path("message").asText("Authentication request failed"));
        } catch (Exception e) {
            return new AuthResponse.Failure("INVALID_RESPONSE", "Could not read server response (HTTP " + response.statusCode() + ")");
        }
    }
}
