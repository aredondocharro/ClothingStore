package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;
import java.util.List;

public interface TokenVerifierPort {

    enum TokenKind { ACCESS, REFRESH, VERIFY }

    record DecodedToken(
            UserId userId,
            String jti,
            Instant createdAt,
            Instant expiresAt,
            List<String> authorities // NUEVO: roles/scopes ya normalizados
    ) {}

    // Firma original (se mantiene)
    DecodedToken verify(String token, String expectedType);

    // Nuevos atajos tipados (no rompen implementaciones existentes)
    default DecodedToken verify(String token, TokenKind expectedKind) {
        return verify(token, expectedKind.name().toLowerCase());
    }
    default DecodedToken verifyAccess(String token) { return verify(token, TokenKind.ACCESS); }
    default DecodedToken verifyRefresh(String token) { return verify(token, TokenKind.REFRESH); }
    default DecodedToken verifyVerify(String token) { return verify(token, TokenKind.VERIFY); }
}
