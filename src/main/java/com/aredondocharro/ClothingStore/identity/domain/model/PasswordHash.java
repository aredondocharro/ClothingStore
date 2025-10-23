package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordNotBCryptedException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class PasswordHash {
    private static final Pattern BCRYPT =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash ofHashed(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new PasswordNotBCryptedException();
        }
        if (!BCRYPT.matcher(hashed).matches()) {
            throw new PasswordNotBCryptedException();
        }
        return new PasswordHash(hashed);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHash that = (PasswordHash) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "******";
    }
}