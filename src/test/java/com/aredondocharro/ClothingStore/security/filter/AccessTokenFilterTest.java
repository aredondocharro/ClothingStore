package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessTokenFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("With a valid token, the filter sets Authentication in the SecurityContext")
    void valid_token_sets_authentication() throws ServletException, IOException {
        AccessTokenVerifierPort verifier = mock(AccessTokenVerifierPort.class);
        AccessTokenFilter filter = new AccessTokenFilter(verifier);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        req.addHeader("Authorization", "Bearer valid.jwt.token");

        String userId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now().minusSeconds(5);
        Instant expiresAt = issuedAt.plusSeconds(3600);
        List<String> authorities = List.of("ADMIN", "USER");
        AuthPrincipal principal = new AuthPrincipal(userId, authorities, issuedAt, expiresAt);

        when(verifier.verify("valid.jwt.token")).thenReturn(principal);

        filter.doFilterInternal(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set");
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());

        assertEquals(userId, SecurityContextHolder.getContext().getAuthentication().getName());

        assertInstanceOf(AuthPrincipal.class, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        AuthPrincipal saved = (AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(userId, saved.userId());

        assertFalse(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Without Authorization header or with an invalid token, the chain continues and no authentication is set")
    void invalid_or_absent_token_leaves_context_empty() throws ServletException, IOException {
        AccessTokenVerifierPort verifier = mock(AccessTokenVerifierPort.class);
        AccessTokenFilter filter = new AccessTokenFilter(verifier);

        // ---- Case 1: no Authorization header ----
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        FilterChain chain1 = new MockFilterChain();

        filter.doFilterInternal(req1, res1, chain1);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // ---- Case 2: invalid token ----
        SecurityContextHolder.clearContext();

        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        FilterChain chain2 = new MockFilterChain();

        when(verifier.verify("bad.token")).thenReturn(null); // or thenThrow(...)
        filter.doFilterInternal(req2, res2, chain2);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
