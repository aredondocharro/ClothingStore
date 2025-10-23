package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenVerifierPort refreshVerifier;
    private final RefreshTokenStorePort store;

    @Override
    public void logout(String refreshTokenRaw, String ip) {
        RefreshTokenVerifierPort.DecodedRefresh decoded = refreshVerifier.verify(refreshTokenRaw);
        Instant now = Instant.now();
        store.revoke(decoded.jti(), "logout", now);
        log.info("Logout: refresh revoked jti={} ip={}", decoded.jti(), ip);
    }
}
