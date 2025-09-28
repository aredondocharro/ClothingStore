package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface VerifyEmailUseCase {
    AuthResult verify(String verificationToken);
}