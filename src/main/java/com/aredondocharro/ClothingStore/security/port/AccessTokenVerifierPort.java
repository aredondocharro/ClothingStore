package com.aredondocharro.ClothingStore.security.port;

public interface AccessTokenVerifierPort {

    AuthPrincipal verify(String rawAccessToken)
            throws InvalidTokenException,
            ExpiredTokenException,
            UnsupportedTokenTypeException,
            MissingRequiredClaimException;


    final class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) { super(message); }
        public InvalidTokenException(String message, Throwable cause) { super(message, cause); }
    }

    final class ExpiredTokenException extends RuntimeException {
        public ExpiredTokenException(String message) { super(message); }
    }

    final class UnsupportedTokenTypeException extends RuntimeException {
        public UnsupportedTokenTypeException(String message) { super(message); }
    }

    final class MissingRequiredClaimException extends RuntimeException {
        public MissingRequiredClaimException(String message) { super(message); }
    }
}
