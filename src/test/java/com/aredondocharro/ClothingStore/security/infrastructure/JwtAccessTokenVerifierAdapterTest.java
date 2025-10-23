package com.aredondocharro.ClothingStore.security.infrastructure;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort.DecodedToken;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenExpiredException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenMissingClaimException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenUnsupportedTypeException;
import com.aredondocharro.ClothingStore.security.infrastructure.out.jwt.JwtAccessTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAccessTokenVerifierAdapterTest {

    TokenVerifierPort tokenVerifier = mock(TokenVerifierPort.class);
    JwtAccessTokenVerifierAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JwtAccessTokenVerifierAdapter(tokenVerifier);
    }

    @Test
    void maps_success_to_AuthPrincipal() {
        var uid = UUID.randomUUID();
        var decoded = new DecodedToken(
                UserId.of(uid),
                "jti-123",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                List.of("ROLE_USER", "payments:charge")
        );
        when(tokenVerifier.verify("good", "access")).thenReturn(decoded);

        AuthPrincipal p = adapter.verify("good");

        assertEquals(uid.toString(), p.userId());
        assertEquals(List.of("ROLE_USER", "payments:charge"), p.authorities());
        assertNotNull(p.issuedAt());
        assertNotNull(p.expiresAt());
    }

    @Test
    void maps_expired() {
        when(tokenVerifier.verify("expired", "access")).thenThrow(new TokenExpiredException("x"));
        assertThrows(AccessTokenVerifierPort.ExpiredTokenException.class, () -> adapter.verify("expired"));
    }

    @Test
    void maps_unsupported_type() {
        when(tokenVerifier.verify("wrongtype", "access")).thenThrow(new TokenUnsupportedTypeException("x"));
        assertThrows(AccessTokenVerifierPort.UnsupportedTokenTypeException.class, () -> adapter.verify("wrongtype"));
    }

    @Test
    void maps_missing_claim() {
        when(tokenVerifier.verify("missing", "access")).thenThrow(new TokenMissingClaimException("x"));
        assertThrows(AccessTokenVerifierPort.MissingRequiredClaimException.class, () -> adapter.verify("missing"));
    }

    @Test
    void maps_invalid() {
        when(tokenVerifier.verify("bad", "access")).thenThrow(new TokenInvalidException("x"));
        assertThrows(AccessTokenVerifierPort.InvalidTokenException.class, () -> adapter.verify("bad"));
    }
}
