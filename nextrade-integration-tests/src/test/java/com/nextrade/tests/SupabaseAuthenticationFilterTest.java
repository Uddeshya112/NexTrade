package com.nextrade.tests;

import com.nextrade.api.security.SupabaseAuthenticationFilter;
import com.nextrade.api.security.SupabaseTokenVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupabaseAuthenticationFilterTest {
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void onlyConfirmedSupabaseIdentityBecomesBackendPrincipal() throws Exception {
        SupabaseTokenVerifier verifier = mock(SupabaseTokenVerifier.class);
        when(verifier.verify("confirmed-token")).thenReturn(new SupabaseTokenVerifier.VerifiedUser(
                UUID.randomUUID(), "345f710d-8aad-4c07-bbba-7650ce371789", "TRADER", "verified@example.com"));
        SupabaseAuthenticationFilter filter = new SupabaseAuthenticationFilter(verifier);
        MockHttpServletRequest request = request("confirmed-token");
        var response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        filter.doFilter(request, response, (req, res) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertEquals("345f710d-8aad-4c07-bbba-7650ce371789", authentication.getPrincipal());
            assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADER")));
        });

        verify(verifier).verify("confirmed-token");
    }

    @Test
    void invalidOrExpiredTokenDoesNotAuthenticateProtectedRequest() throws Exception {
        SupabaseTokenVerifier verifier = mock(SupabaseTokenVerifier.class);
        when(verifier.verify("expired-token")).thenThrow(new org.springframework.web.client.HttpClientErrorException(
                org.springframework.http.HttpStatus.UNAUTHORIZED));
        SupabaseAuthenticationFilter filter = new SupabaseAuthenticationFilter(verifier);
        var response = new MockHttpServletResponse();

        filter.doFilter(request("expired-token"), response, (req, res) ->
                assertNull(SecurityContextHolder.getContext().getAuthentication()));

        verify(verifier).verify("expired-token");
    }

    private static MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
