package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final TokenVerifierPort tokenVerifier;
    private final RefreshTokenStorePort store;
    private final LoadUserPort loadUserPort;
    private final TokenGeneratorPort tokens;

    @Override
    public AuthResult refresh(String refreshTokenRaw, String ip, String userAgent) {
        TokenVerifierPort.DecodedToken decoded = tokenVerifier.verify(refreshTokenRaw, "refresh"); // firma, iss, type, exp
        Instant now = Instant.now();

        Optional<RefreshSession> sessionOpt = store.findByJti(decoded.jti());
        if (sessionOpt.isEmpty()) {
            log.warn("Refresh failed: unknown jti={}", decoded.jti());
            throw new SecurityException("Invalid refresh token");
        }

        RefreshSession session = sessionOpt.get();

        if (session.isRevoked() || session.isActive(now)) {
            // Detección de reuso
            log.warn("Refresh reuse detected for userId={} jti={}", session.userId(), session.jti());
            store.revokeAllForUser(session.userId(), "reuse-detected", now);
            throw new SecurityException("Refresh token reuse detected");
        }

        if (session.isExpired(now)) {
            log.warn("Refresh expired for userId={} jti={}", session.userId(), session.jti());
            throw new SecurityException("Refresh token expired");
        }

        // Cargar usuario
        User user = loadUserPort.findById(session.userId())
                .orElseThrow(() -> new SecurityException("User not found"));

        // Generar nuevos tokens
        String newAccess = tokens.generateAccessToken(user);
        String newRefresh = tokens.generateRefreshToken(user);

        // Decodificar el nuevo refresh para extraer jti/exp
        TokenVerifierPort.DecodedToken newDecoded = tokenVerifier.verify(newRefresh, "refresh");

        // Guardar nueva sesión rotada y marcar la anterior como reemplazada
        RefreshSession newSession = new RefreshSession(
                newDecoded.jti(),
                user.id(),
                newDecoded.expiresAt(),
                now,
                null,
                null,
                ip,
                userAgent
        );
        store.saveNew(newSession, newRefresh);
        store.markReplaced(session.jti(), newDecoded.jti(), now);

        log.info("Refresh rotated for userId={} oldJti={} newJti={}", user.id(), session.jti(), newDecoded.jti());
        return new AuthResult(newAccess, newRefresh);
    }
}
