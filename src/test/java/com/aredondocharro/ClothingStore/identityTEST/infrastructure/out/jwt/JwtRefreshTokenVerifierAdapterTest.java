package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenUnsupportedTypeException;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtRefreshTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtRefreshTokenVerifierAdapterTest {

    static final String SECRET = "secret-for-tests-123";
    static final String ISSUER = "ClothingStore";

    private static User makeUser() {
        var id = UserId.newId();
        var email = IdentityEmail.of("user@example.com");
        var hash  = PasswordHash.ofHashed("$2a$10$" + "a".repeat(53)); // 60 chars
        var now   = Instant.now();
        return User.create(id, email, hash, Set.of(Role.USER), now).verified();
    }

    @Test
    void verify_refresh_ok() {
        var gen = new JwtTokenGeneratorAdapter(SECRET, ISSUER, 600, 3600, 900);
        var ver = new JwtRefreshTokenVerifierAdapter(SECRET, ISSUER);

        var user = makeUser();
        var refresh = gen.generateRefreshToken(user); // incluye jti + type=refresh

        RefreshTokenVerifierPort.DecodedRefresh dt = ver.verify(refresh);

        assertEquals(user.id(), dt.userId());         // UserId VO
        assertNotNull(dt.jti());
        assertNotNull(dt.issuedAt());
        assertNotNull(dt.expiresAt());
        assertTrue(dt.expiresAt().isAfter(dt.issuedAt()));
        assertTrue(dt.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void verify_wrong_type_throws() {
        var gen = new JwtTokenGeneratorAdapter(SECRET, ISSUER, 600, 3600, 900);
        var ver = new JwtRefreshTokenVerifierAdapter(SECRET, ISSUER);

        var user = makeUser();
        var access = gen.generateAccessToken(user);   // type=access

        assertThrows(TokenUnsupportedTypeException.class, () -> ver.verify(access));
    }
}
