package com.aredondocharro.ClothingStore.identity.domain.exception;

public class MissingRefreshCookieException extends RuntimeException {
    public static final String COOKIE_NAME = "refresh_token";
    public MissingRefreshCookieException() {
        super("Missing " + COOKIE_NAME + " cookie");
    }
}
