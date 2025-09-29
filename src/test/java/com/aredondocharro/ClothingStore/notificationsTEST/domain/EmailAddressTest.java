package com.aredondocharro.ClothingStore.notificationsTEST.domain;

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
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("no-at-symbol"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("a@b"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("a@b."));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("name@ domain.com"));
    }
}