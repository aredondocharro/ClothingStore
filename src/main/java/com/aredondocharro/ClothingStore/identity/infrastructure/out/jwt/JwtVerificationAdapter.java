package com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class JwtVerificationAdapter implements VerificationTokenPort {

    private final JWTVerifier verifier;

    public JwtVerificationAdapter(String secret, String issuer) {
        this.verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .withClaim("type", "verify")
                .build();
    }

    @Override
    public VerificationTokenData validate(String verificationToken) {
        DecodedJWT decoded = verifier.verify(verificationToken); // lanza si es inválido/expirado

        UUID userId = UUID.fromString(decoded.getSubject());

        String jtiStr = decoded.getId();
        if (jtiStr == null || jtiStr.isBlank()) {
            throw new IllegalStateException("Verification token has no JTI");
        }
        UUID jti = UUID.fromString(jtiStr);

        log.debug("Verification token valid for userId={} jti={}", userId, jti);

        return new VerificationTokenData(userId, jti);
    }
}
