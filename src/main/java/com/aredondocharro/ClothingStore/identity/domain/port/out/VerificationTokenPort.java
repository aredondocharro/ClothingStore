package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.util.UUID;

public interface VerificationTokenPort {

    record VerificationTokenData(UUID userId, UUID jti) {}

    VerificationTokenData validate(String token);

    default UUID validateAndExtractUserId(String token) {
        return validate(token).userId();
    }
}
