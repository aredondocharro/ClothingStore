package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenRotationPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenStorePort;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
public class JwtVerificationTokenRotationAdapter implements VerificationTokenRotationPort {

    private final TokenGeneratorPort tokenGenerator;
    private final VerificationTokenStorePort store;
    private final Clock clock;

    public JwtVerificationTokenRotationAdapter(
            TokenGeneratorPort tokenGenerator,
            VerificationTokenStorePort store,
            Clock clock
    ) {
        this.tokenGenerator = tokenGenerator;
        this.store = store;
        this.clock = clock;
    }

    @Override
    public RotatedVerificationToken rotateForUser(User user) {
        Instant now = Instant.now(clock);

        // 1) Revocar tokens anteriores activos del usuario
        store.revokeActiveTokensForUser(user.id(), now);

        // 2) Generar nuevo JWT de verificación (String)
        String token = tokenGenerator.generateVerificationToken(user);

        // 3) Decodificar para extraer JTI y fecha de expiración
        DecodedJWT decoded = JWT.decode(token);

        String jtiString = decoded.getId();
        if (jtiString == null || jtiString.isBlank()) {
            throw new IllegalStateException(
                    "Verification token has no JTI. " +
                            "Ensure JwtTokenGeneratorAdapter uses .withJWTId(...) when creating it."
            );
        }

        UUID jti = UUID.fromString(jtiString);

        if (decoded.getExpiresAt() == null) {
            throw new IllegalStateException(
                    "Verification token has no expiresAt. " +
                            "Ensure JwtTokenGeneratorAdapter uses .withExpiresAt(...) when creating it."
            );
        }

        Instant expiresAt = decoded.getExpiresAt().toInstant();

        // 4) Registrar el nuevo token en el store como “actual”
        store.saveNewToken(user.id(), jti, expiresAt);

        log.info("Verification token rotated for userId={} jti={}", user.id().value(), jti);

        // 5) Devolverlo a Identity
        return new RotatedVerificationToken(token, expiresAt);
    }
}
