package com.aredondocharro.ClothingStore.identity.domain.port.out;

public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String hash);
}

