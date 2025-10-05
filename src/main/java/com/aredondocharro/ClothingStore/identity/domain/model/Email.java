package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidEmailFormatException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
@ToString
public final class Email {
    private static final Pattern P = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String raw) {
        if (raw == null || raw.isBlank()) throw new EmailRequiredException();
        String v = raw.trim().toLowerCase();
        if (!P.matcher(v).matches()) throw new InvalidEmailFormatException(raw + " is not a valid email");
        return new Email(v);
    }

    public String localPart() {
        int at = value.indexOf('@');
        return at > 0 ? value.substring(0, at) : value;
    }

    public String domain() {
        int at = value.indexOf('@');
        return at > 0 ? value.substring(at + 1) : "";
    }
}
