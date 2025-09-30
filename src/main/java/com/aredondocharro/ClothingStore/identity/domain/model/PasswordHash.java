package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.HashedPasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordNotBCryptedException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public final class PasswordHash {
    // Expresión regular para validar el formato BCrypt
    private static final Pattern BCRYPT =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash ofHashed(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new HashedPasswordRequiredException();
        }
        if (!BCRYPT.matcher(hashed).matches()) {
            throw new PasswordNotBCryptedException(); // formato no BCrypt válido
        }
        return new PasswordHash(hashed);
    }


    @Override
    public String toString() {
        return "******";
    } // evita fugas en logs
}
