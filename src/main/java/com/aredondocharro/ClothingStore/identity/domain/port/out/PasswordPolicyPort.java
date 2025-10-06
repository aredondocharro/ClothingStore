package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordMismatchException;

public interface PasswordPolicyPort {
    void validate(String password);

    default void validatePair(String password, String confirm) {
        validate(password);
        if (confirm == null || !confirm.equals(password)) {
            throw new PasswordMismatchException(); // excepción de dominio
        }
    }
}