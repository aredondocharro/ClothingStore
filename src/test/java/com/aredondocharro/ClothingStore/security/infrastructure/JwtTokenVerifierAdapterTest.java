package com.aredondocharro.ClothingStore.security.infrastructure;

import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort.DecodedToken;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenExpiredException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenMissingClaimException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenUnsupportedTypeException;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenVerifierAdapter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenVerifierAdapterTest {

    private static final String ISSUER = "test-issuer";
    private static final String SECRET = "test-secret-123";
    private JwtTokenVerifierAdapter verifier;

    @BeforeEach
    void setUp() {
        verifier = new JwtTokenVerifierAdapter(SECRET, ISSUER);
    }

    @Test
    void valid_access_token_with_roles_array() {
        String userId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("type", "access")
                .withArrayClaim("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"})
                .withIssuedAt(java.util.Date.from(Instant.now()))
                .withExpiresAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(SECRET));

        DecodedToken d = verifier.verify(token, "access");

        assertEquals(userId, d.userId().value().toString());
        assertNotNull(d.createdAt());
        assertNotNull(d.expiresAt());
        assertTrue(d.authorities().containsAll(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    void valid_access_token_with_scope_string() {
        String userId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("type", "access")
                .withClaim("scope", "payments:charge cart:read")
                .withIssuedAt(java.util.Date.from(Instant.now()))
                .withExpiresAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(SECRET));

        DecodedToken d = verifier.verify(token, "access");

        assertEquals(userId, d.userId().value().toString());
        assertTrue(d.authorities().containsAll(List.of("payments:charge", "cart:read")));
    }

    @Test
    void expired_access_token_throws_TokenExpiredException() {
        String userId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("type", "access")
                .withIssuedAt(java.util.Date.from(Instant.now().minusSeconds(3600)))
                .withExpiresAt(java.util.Date.from(Instant.now().minusSeconds(10)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(SECRET));

        assertThrows(TokenExpiredException.class, () -> verifier.verify(token, "access"));
    }

    @Test
    void wrong_type_throws_TokenUnsupportedTypeException() {
        String userId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("type", "refresh")
                .withIssuedAt(java.util.Date.from(Instant.now()))
                .withExpiresAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(SECRET));

        assertThrows(TokenUnsupportedTypeException.class, () -> verifier.verify(token, "access"));
    }

    @Test
    void missing_sub_throws_TokenMissingClaimException() {
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withClaim("type", "access")
                .withIssuedAt(java.util.Date.from(Instant.now()))
                .withExpiresAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(SECRET));

        assertThrows(TokenMissingClaimException.class, () -> verifier.verify(token, "access"));
    }

    @Test
    void invalid_signature_throws_TokenInvalidException() {
        String userId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("type", "access")
                .withIssuedAt(java.util.Date.from(Instant.now()))
                .withExpiresAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256("other-secret"));

        assertThrows(TokenInvalidException.class, () -> verifier.verify(token, "access"));
    }
}
