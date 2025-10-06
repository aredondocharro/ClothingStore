package com.aredondocharro.ClothingStore.notificationsTEST.domain;

import com.aredondocharro.ClothingStore.notification.domain.exception.BodyRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.exception.RecipientsRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.exception.SubjectRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailMessageRecordTest {

    @Test
    void requires_non_empty_to_subject_and_body() {
        List<EmailAddress> to = List.of(new EmailAddress("a@b.com"));
        assertThrows(RecipientsRequiredException.class, () -> new EmailMessage(null, null, "s", "b", false));
        assertThrows(RecipientsRequiredException.class, () -> new EmailMessage(null, List.of(), "s", "b", false));
        assertThrows(SubjectRequiredException.class, () -> new EmailMessage(null, to, " ", "b", false));
        assertThrows(BodyRequiredException.class, () -> new EmailMessage(null, to, "s", " ", false));
        assertDoesNotThrow(() -> new EmailMessage(null, to, "Hello", "World", false));
    }

    @Test
    void recipients_list_is_defensive_copy_and_unmodifiable() {
        List<EmailAddress> original = new ArrayList<>();
        original.add(new EmailAddress("a@b.com"));
        EmailMessage emailMessage = new EmailMessage(null, original, "s", "b", false);

        // mutate original - email.to() should not change
        original.add(new EmailAddress("x@y.com"));
        assertEquals(1, emailMessage.to().size());

        // email.to() should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> emailMessage.to().add(new EmailAddress("c@d.com")));
    }
}