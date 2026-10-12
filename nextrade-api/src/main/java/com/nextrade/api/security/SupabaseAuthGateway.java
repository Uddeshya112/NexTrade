package com.nextrade.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/** Supabase Auth REST calls used by the Java desktop client; never uses a service-role key. */
@Component
public class SupabaseAuthGateway {
    private final RestClient client;
    private final String projectUrl;
    private final String publishableKey;
    private final String webUrl;

    public SupabaseAuthGateway(RestClient.Builder builder,
            @Value("${nextrade.supabase.url:}") String projectUrl,
            @Value("${nextrade.supabase.publishable-key:}") String publishableKey,
            @Value("${nextrade.supabase.web-url}") String webUrl) {
        this.client = builder.build();
        this.projectUrl = projectUrl == null ? "" : projectUrl.replaceAll("/+$", "");
        this.publishableKey = publishableKey == null ? "" : publishableKey;
        this.webUrl = webUrl.replaceAll("/+$", "");
    }

    public String webUrl() { return webUrl; }

    public JsonNode signup(String email, String password, String username) {
        return send("/auth/v1/signup", null, Map.of("email", email, "password", password,
                "data", Map.of("username", username == null ? "" : username)), webUrl + "/auth/callback");
    }

    public JsonNode login(String email, String password) {
        return send("/auth/v1/token?grant_type=password", null, Map.of("email", email, "password", password), null);
    }

    public JsonNode refresh(String refreshToken) {
        return send("/auth/v1/token?grant_type=refresh_token", null, Map.of("refresh_token", refreshToken), null);
    }

    public void logout(String token, boolean global) {
        String scope = global ? "global" : "local";
        send("/auth/v1/logout?scope=" + scope, token, null, null);
    }

    public void recover(String email) {
        send("/auth/v1/recover", null, Map.of("email", email), webUrl + "/auth/reset-password");
    }

    public void resendSignup(String email) {
        send("/auth/v1/resend", null, Map.of("type", "signup", "email", email), webUrl + "/auth/callback");
    }

    public JsonNode updatePassword(String token, String password) {
        return send("/auth/v1/user", token, Map.of("password", password), null, true);
    }

    private JsonNode send(String path, String bearer, Object body, String redirectTo) {
        return send(path, bearer, body, redirectTo, false);
    }

    private JsonNode send(String path, String bearer, Object body, String redirectTo, boolean put) {
        if (projectUrl.isBlank() || publishableKey.isBlank()) throw new IllegalStateException("Supabase URL and publishable key are not configured on the API service");
        var request = put ? client.put() : client.post();
        var spec = request.uri(buildUri(path, redirectTo))
                .header("apikey", publishableKey)
                .contentType(MediaType.APPLICATION_JSON);
        if (bearer != null && !bearer.isBlank()) spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        if (body == null) return spec.retrieve().body(JsonNode.class);
        return spec.body(body).retrieve().body(JsonNode.class);
    }

    private URI buildUri(String path, String redirectTo) {
        var builder = UriComponentsBuilder.fromUriString(projectUrl + path);
        if (redirectTo != null) builder.queryParam("redirect_to", redirectTo);
        return builder.build().encode().toUri();
    }
}
