package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidEmailFormatException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class IdentityEmail {
    private static final Pattern P = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    private IdentityEmail(String value) {
        this.value = value;
    }

    public static IdentityEmail of(String raw) {
        if (raw == null || raw.isBlank()) throw new EmailRequiredException();
        String v = raw.trim().toLowerCase();
        if (!P.matcher(v).matches()) throw new InvalidEmailFormatException(raw + " is not a valid email");
        return new IdentityEmail(v);
    }

    public String getValue() {
        return value;
    }

    public String localPart() {
        int at = value.indexOf('@');
        return at > 0 ? value.substring(0, at) : value;
    }

    public String domain() {
        int at = value.indexOf('@');
        return at > 0 ? value.substring(at + 1) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityEmail that = (IdentityEmail) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "IdentityEmail{" +
                "value='" + value + '\'' +
                '}';
    }
}