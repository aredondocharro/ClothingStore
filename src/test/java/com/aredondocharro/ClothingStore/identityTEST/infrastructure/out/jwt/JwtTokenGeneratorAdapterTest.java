package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.jwt;


import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenGeneratorAdapterTest {

    private static final String SECRET = "test-secret-123";
    private static final String ISSUER = "clothingstore-test";
    private static final long ACCESS_SEC = 3600;
    private static final long REFRESH_SEC = 7200;
    private static final long VERIFY_SEC = 600;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    private User makeUser(Set<Role> roles) {
        return new User(
                UUID.randomUUID(),
                IdentityEmail.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                true,
                roles,
                Instant.now()
        );
    }

    @Test
    void accessToken_containsIssuerSubjectTypeEmailAndRoles() {
        var adapter = new JwtTokenGeneratorAdapter(SECRET, ISSUER, ACCESS_SEC, REFRESH_SEC, VERIFY_SEC);
        var user = makeUser(Set.of(Role.USER, Role.ADMIN));

        var token = adapter.generateAccessToken(user);

        var verifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).build();
        var decoded = verifier.verify(token);

        assertEquals(user.id().toString(), decoded.getSubject());
        assertEquals("access", decoded.getClaim("type").asString());
        assertEquals("user@example.com", decoded.getClaim("email").asString());

        List<String> roles = decoded.getClaim("roles").asList(String.class);
        assertNotNull(roles);
        assertTrue(roles.containsAll(List.of("USER", "ADMIN"))); // según tu mapeo Role::name

        // exp / iat razonables
        var now = Instant.now();
        assertTrue(decoded.getIssuedAt().toInstant().isBefore(now.plusSeconds(5)));
        assertTrue(decoded.getExpiresAt().toInstant().isAfter(now.plusSeconds(ACCESS_SEC - 5)));
    }

    @Test
    void refreshToken_containsIssuerSubjectAndTypeRefresh() {
        var adapter = new JwtTokenGeneratorAdapter(SECRET, ISSUER, ACCESS_SEC, REFRESH_SEC, VERIFY_SEC);
        var user = makeUser(Set.of(Role.USER));

        var token = adapter.generateRefreshToken(user);

        var verifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).build();
        var decoded = verifier.verify(token);

        assertEquals(user.id().toString(), decoded.getSubject());
        assertEquals("refresh", decoded.getClaim("type").asString());

        // En refresh NO incluimos email ni roles -> deben ser null al convertir
        assertNull(decoded.getClaim("email").asString(), "refresh must not carry email");
        assertNull(decoded.getClaim("roles").asList(String.class), "refresh must not carry roles");

        // (opcional) también puedes comprobar que el mapa de claims no los contiene
        assertFalse(decoded.getClaims().containsKey("email"));
        assertFalse(decoded.getClaims().containsKey("roles"));
    }

    @Test
    void verificationToken_containsIssuerSubjectTypeVerifyAndEmail() {
        var adapter = new JwtTokenGeneratorAdapter(SECRET, ISSUER, ACCESS_SEC, REFRESH_SEC, VERIFY_SEC);
        var user = makeUser(Set.of(Role.USER));

        var token = adapter.generateVerificationToken(user);

        var verifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).build();
        var decoded = verifier.verify(token);

        assertEquals(user.id().toString(), decoded.getSubject());
        assertEquals("verify", decoded.getClaim("type").asString());
        assertEquals("user@example.com", decoded.getClaim("email").asString());
    }

    @Test
    void refreshToken_containsIssuerSubjectTypeAndJti() {
        var adapter = new JwtTokenGeneratorAdapter(SECRET, ISSUER, ACCESS_SEC, REFRESH_SEC, VERIFY_SEC);
        var user = makeUser(Set.of(Role.USER));

        var token = adapter.generateRefreshToken(user);

        var verifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).withClaim("type", "refresh").build();
        var decoded = verifier.verify(token);

        assertEquals(user.id().toString(), decoded.getSubject());
        assertEquals("refresh", decoded.getClaim("type").asString());

        // NEW: ensure JTI is present
        assertNotNull(decoded.getId());
        assertFalse(decoded.getId().isBlank());
    }

}
