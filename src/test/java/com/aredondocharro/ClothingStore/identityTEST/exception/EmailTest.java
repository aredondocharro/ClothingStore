package com.aredondocharro.ClothingStore.identityTEST.exception;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidEmailFormatException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void shouldThrowEmailRequired_whenNullOrBlank() {
        assertThrows(EmailRequiredException.class, () -> Email.of(null));
        assertThrows(EmailRequiredException.class, () -> Email.of(""));
        assertThrows(EmailRequiredException.class, () -> Email.of("   "));
    }

    @Test
    void shouldThrowInvalidEmailFormat_whenPatternDoesNotMatch() {
        assertThrows(InvalidEmailFormatException.class, () -> Email.of("not-a-valid-email"));
        assertThrows(InvalidEmailFormatException.class, () -> Email.of("missing-at-symbol.com"));
        assertThrows(InvalidEmailFormatException.class, () -> Email.of("missing-domain@"));
        assertThrows(InvalidEmailFormatException.class, () -> Email.of("missing-dot@domaincom"));
    }
}
