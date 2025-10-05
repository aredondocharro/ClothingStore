package com.aredondocharro.ClothingStore.identity.infrastructure.security;

import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import java.util.regex.Pattern;

import static com.aredondocharro.ClothingStore.identity.infrastructure.security.PasswordPolicyConstants.*;

public class SimplePasswordPolicy implements PasswordPolicyPort {

    private static final Pattern COMPILED = Pattern.compile(REGEX);

    @Override
    public void validate(String newPassword) {
        if (newPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        int len = newPassword.length();
        if (len < MIN_LENGTH || len > MAX_LENGTH) {
            throw new IllegalArgumentException("Password length must be between " + MIN_LENGTH + " and " + MAX_LENGTH);
        }
        if (!COMPILED.matcher(newPassword).matches()) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }
}