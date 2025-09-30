package com.aredondocharro.ClothingStore.notificationsTEST.domain;

import com.aredondocharro.ClothingStore.notification.domain.exception.InvalidEmailAddressException;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void valid_email_is_accepted() {
        assertDoesNotThrow(() -> new EmailAddress("user@example.com"));
        assertDoesNotThrow(() -> new EmailAddress("u.s-e+r_1@sub.domain.io"));
    }

    @Test
    void null_or_malformed_email_is_rejected() {
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress(null));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress(""));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress("no-at-symbol"));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress("a@b"));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress("a@b."));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress("@domain.com"));
        assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress("name@ domain.com"));
    }
}