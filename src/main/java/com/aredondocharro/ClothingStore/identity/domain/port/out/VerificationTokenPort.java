package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.util.UUID;

public interface VerificationTokenPort {
    UUID validateAndExtractUserId(String verificationToken);
}
