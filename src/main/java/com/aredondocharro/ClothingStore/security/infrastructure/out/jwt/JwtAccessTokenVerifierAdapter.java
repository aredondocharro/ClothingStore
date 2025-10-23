package com.aredondocharro.ClothingStore.security.infrastructure.out.jwt;

import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenExpiredException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenMissingClaimException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.TokenUnsupportedTypeException;
import com.aredondocharro.ClothingStore.security.port.AccessTokenVerifierPort;
import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JwtAccessTokenVerifierAdapter implements AccessTokenVerifierPort {

    private final TokenVerifierPort tokenVerifier;

    public JwtAccessTokenVerifierAdapter(TokenVerifierPort tokenVerifier) {
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier is required");
    }

    @Override
    public AuthPrincipal verify(String rawAccessToken)
            throws InvalidTokenException, ExpiredTokenException,
            UnsupportedTokenTypeException, MissingRequiredClaimException {
        try {
            var d = tokenVerifier.verify(rawAccessToken, "access");
            return new AuthPrincipal(
                    d.userId().value().toString(),
                    d.authorities(),
                    d.createdAt(),
                    d.expiresAt()
            );
        } catch (TokenExpiredException e) {
            throw new ExpiredTokenException("Token expired");
        } catch (TokenUnsupportedTypeException e) {
            throw new UnsupportedTokenTypeException("Unsupported token type");
        } catch (TokenMissingClaimException e) {
            throw new MissingRequiredClaimException("Missing required claim");
        } catch (TokenInvalidException e) {
            throw new InvalidTokenException("Invalid access token", e);
        }
    }
}
