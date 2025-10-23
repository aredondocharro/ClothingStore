package com.aredondocharro.ClothingStore.security.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtAccessTokenVerifierAdapter implements AccessTokenVerifierPort {

    private final JWTVerifier verifier;

    public JwtAccessTokenVerifierAdapter(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        this.verifier = JWT
                .require(Algorithm.HMAC256(Objects.requireNonNull(secret)))
                .withIssuer(Objects.requireNonNull(issuer))
                .withClaim("type", "access")
                .acceptLeeway(2)
                .build();
    }

    @Override
    public AuthPrincipal verify(String rawAccessToken)
            throws InvalidTokenException, ExpiredTokenException,
            UnsupportedTokenTypeException, MissingRequiredClaimException {
        try {
            DecodedJWT jwt = verifier.verify(Objects.requireNonNull(rawAccessToken));
            String sub = jwt.getSubject();
            if (sub == null || sub.isBlank()) {
                throw new MissingRequiredClaimException("Missing sub");
            }

            // roles[] o scope → authorities de Spring (ROLE_*)
            List<String> roles;
            try {
                String[] arr = jwt.getClaim("roles").asArray(String.class);
                roles = arr != null ? Arrays.asList(arr) : List.of();
            } catch (Exception ignored) {
                roles = List.of();
            }

            if (roles.isEmpty()) {
                String scope = jwt.getClaim("scope").asString();
                if (scope != null && !scope.isBlank()) {
                    roles = Arrays.stream(scope.split("\\s+")).toList();
                }
            }

            List<String> authorities = roles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toList());

            Instant iat = jwt.getIssuedAt() != null ? jwt.getIssuedAt().toInstant() : null;
            Instant exp = jwt.getExpiresAt() != null ? jwt.getExpiresAt().toInstant() : null;

            return new AuthPrincipal(sub, authorities, iat, exp);

        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new ExpiredTokenException("Access token expired");
        } catch (com.auth0.jwt.exceptions.InvalidClaimException e) {
            throw new UnsupportedTokenTypeException("Unsupported token type");
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            throw new InvalidTokenException("Invalid access token: " + e.getMessage(), e);
        }
    }
}
