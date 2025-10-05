package com.aredondocharro.ClothingStore.identity.domain.port.out;

public interface PasswordPolicyPort {
    void validate(String newPassword);
}