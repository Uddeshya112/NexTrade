package com.nextrade.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.UUID;

/** Validates access tokens against Supabase Auth, including projects using legacy HS256 signing keys. */
@Component
public class SupabaseTokenVerifier {
    private final RestClient restClient;
    private final String projectUrl;
    private final String publishableKey;
    private final SupabaseUserProvisioner users;

    public SupabaseTokenVerifier(RestClient.Builder builder,
            @Value("${nextrade.supabase.url:}") String projectUrl,
            @Value("${nextrade.supabase.publishable-key:}") String publishableKey,
            SupabaseUserProvisioner users) {
        this.restClient = builder.build();
        this.projectUrl = projectUrl == null ? "" : projectUrl.replaceAll("/+$", "");
        this.publishableKey = publishableKey == null ? "" : publishableKey;
        this.users = users;
    }

    public VerifiedUser verify(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || projectUrl.isBlank() || publishableKey.isBlank()) return null;
        JsonNode user = restClient.get()
                .uri(URI.create(projectUrl + "/auth/v1/user"))
                .header("apikey", publishableKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(JsonNode.class);
        if (user == null) return null;
        boolean emailConfirmed = !user.path("email_confirmed_at").asText("").isBlank()
                || !user.path("confirmed_at").asText("").isBlank();
        if (!emailConfirmed) return null;
        UUID supabaseUserId;
        String email = user.path("email").asText("");
        try { supabaseUserId = UUID.fromString(user.path("id").asText()); }
        catch (RuntimeException invalidId) { return null; }
        if (email.isBlank()) return null;
        SupabaseUserProvisioner.LinkedAccount account = users.getNexTradeAccount(supabaseUserId, email);
        return new VerifiedUser(supabaseUserId, account.nextradeUserId(), account.role(), email);
    }

    public record VerifiedUser(UUID supabaseUserId, String nextradeUserId, String role, String email) { }
}
