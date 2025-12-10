package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;

public interface ResendVerificationEmailUseCase {
    void resend(IdentityEmail email);
}
