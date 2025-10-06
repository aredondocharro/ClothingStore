package com.aredondocharro.ClothingStore.identity.infrastructure.out.security;

import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidPasswordException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;

import java.util.regex.Pattern;

public class SimplePasswordPolicy implements PasswordPolicyPort {
    private static final int MIN = 8, MAX = 72;
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{" + MIN + "," + MAX + "}$");

    @Override
    public void validate(String p) {
        if (p == null) throw new InvalidPasswordException("Password cannot be null");
        if (!PATTERN.matcher(p).matches()) {
            throw new InvalidPasswordException("Password must contain upper, lower and digit (8–72)");
        }
    }
}