package com.nextrade.tests;

import com.nextrade.api.security.SupabaseTokenVerifier;
import com.nextrade.api.security.SupabaseUserProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpStatus;

class SupabaseTokenVerifierTest {
    private static final UUID SUPABASE_ID = UUID.fromString("61e4b5a4-2e95-4bc1-9c5e-2c0f95d54601");

    @Test
    void asksSupabaseToValidateTokenAndRequiresConfirmedEmail() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseUserProvisioner provisioner = mock(SupabaseUserProvisioner.class);
        when(provisioner.getNexTradeAccount(SUPABASE_ID, "verified@example.com"))
                .thenReturn(new SupabaseUserProvisioner.LinkedAccount("345f710d-8aad-4c07-bbba-7650ce371789", "TRADER"));
        SupabaseTokenVerifier verifier = new SupabaseTokenVerifier(builder, "https://example.supabase.co", "sb_publishable_test", provisioner);
        server.expect(requestTo("https://example.supabase.co/auth/v1/user"))
                .andExpect(header("apikey", "sb_publishable_test"))
                .andExpect(header("Authorization", "Bearer token-from-client"))
                .andRespond(withSuccess("""
                        {"id":"61e4b5a4-2e95-4bc1-9c5e-2c0f95d54601","email":"verified@example.com","email_confirmed_at":"2026-01-01T00:00:00Z"}
                        """, MediaType.APPLICATION_JSON));

        SupabaseTokenVerifier.VerifiedUser user = verifier.verify("token-from-client");

        assertNotNull(user);
        assertEquals(SUPABASE_ID, user.supabaseUserId());
        assertEquals("345f710d-8aad-4c07-bbba-7650ce371789", user.nextradeUserId());
        assertEquals("TRADER", user.role());
        verify(provisioner).getNexTradeAccount(SUPABASE_ID, "verified@example.com");
        server.verify();
    }

    @Test
    void rejectsUnconfirmedSupabaseUserWithoutProvisioningAccount() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseUserProvisioner provisioner = mock(SupabaseUserProvisioner.class);
        SupabaseTokenVerifier verifier = new SupabaseTokenVerifier(builder, "https://example.supabase.co", "sb_publishable_test", provisioner);
        server.expect(requestTo("https://example.supabase.co/auth/v1/user"))
                .andRespond(withSuccess("""
                        {"id":"61e4b5a4-2e95-4bc1-9c5e-2c0f95d54601","email":"unverified@example.com","email_confirmed_at":null,"confirmed_at":null}
                        """, MediaType.APPLICATION_JSON));

        assertNull(verifier.verify("unconfirmed-token"));
        verifyNoInteractions(provisioner);
        server.verify();
    }

    @Test
    void invalidOrExpiredTokenIsRejectedBySupabase() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseTokenVerifier verifier = new SupabaseTokenVerifier(builder, "https://example.supabase.co", "sb_publishable_test", mock(SupabaseUserProvisioner.class));
        server.expect(requestTo("https://example.supabase.co/auth/v1/user"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(org.springframework.web.client.RestClientResponseException.class,
                () -> verifier.verify("expired-or-invalid-token"));
        server.verify();
    }
}
