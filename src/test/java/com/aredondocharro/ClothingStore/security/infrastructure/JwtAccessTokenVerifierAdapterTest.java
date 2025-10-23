package com.aredondocharro.ClothingStore.security.infrastructure;

import com.aredondocharro.ClothingStore.security.infrastructure.out.jwt.JwtAccessTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtAccessTokenVerifierAdapterTest {

    private static final String SECRET  = "test-secret-123";
    private static final String ISSUER  = "AUTH0JWT-BACKEND";

    private JwtAccessTokenVerifierAdapter adapter;
    private Algorithm algo;

    @BeforeEach
    void setUp() {
        adapter = new JwtAccessTokenVerifierAdapter(SECRET, ISSUER);
        algo = Algorithm.HMAC256(SECRET);
    }

    @Test
    void maps_success_to_AuthPrincipal_with_roles_array() {
        UUID uid = UUID.randomUUID();
        Instant now = Instant.now();

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(uid.toString())
                .withIssuedAt(Date.from(now.minusSeconds(5)))
                .withExpiresAt(Date.from(now.plusSeconds(600)))
                .withClaim("type", "access")
                .withArrayClaim("roles", new String[]{"USER", "ADMIN"})
                .sign(algo);

        AuthPrincipal p = adapter.verify(token);

        assertEquals(uid.toString(), p.userId());
        // El adaptador añade el prefijo ROLE_ a los roles del claim
        assertTrue(p.authorities().containsAll(List.of("ROLE_USER", "ROLE_ADMIN")));
        assertNotNull(p.issuedAt());
        assertNotNull(p.expiresAt());
    }

    @Test
    void expired_token_throws_ExpiredTokenException() {
        Instant now = Instant.now();

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(UUID.randomUUID().toString())
                .withIssuedAt(Date.from(now.minusSeconds(120)))
                .withExpiresAt(Date.from(now.minusSeconds(60))) // ya expirado
                .withClaim("type", "access")
                .withArrayClaim("roles", new String[]{"USER"})
                .sign(algo);

        assertThrows(AccessTokenVerifierPort.ExpiredTokenException.class, () -> adapter.verify(token));
    }

    @Test
    void wrong_type_claim_throws_UnsupportedTokenTypeException() {
        Instant now = Instant.now();

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(UUID.randomUUID().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(600)))
                .withClaim("type", "refresh") // no es access
                .withArrayClaim("roles", new String[]{"USER"})
                .sign(algo);

        assertThrows(AccessTokenVerifierPort.UnsupportedTokenTypeException.class, () -> adapter.verify(token));
    }

    @Test
    void missing_sub_claim_throws_MissingRequiredClaimException() {
        Instant now = Instant.now();

        String token = JWT.create()
                .withIssuer(ISSUER)
                // sin subject
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(600)))
                .withClaim("type", "access")
                .withArrayClaim("roles", new String[]{"USER"})
                .sign(algo);

        assertThrows(AccessTokenVerifierPort.MissingRequiredClaimException.class, () -> adapter.verify(token));
    }

    @Test
    void invalid_signature_throws_InvalidTokenException() {
        Instant now = Instant.now();
        Algorithm other = Algorithm.HMAC256("other-secret"); // firma distinta

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(UUID.randomUUID().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(600)))
                .withClaim("type", "access")
                .withArrayClaim("roles", new String[]{"USER"})
                .sign(other);

        assertThrows(AccessTokenVerifierPort.InvalidTokenException.class, () -> adapter.verify(token));
    }
}
