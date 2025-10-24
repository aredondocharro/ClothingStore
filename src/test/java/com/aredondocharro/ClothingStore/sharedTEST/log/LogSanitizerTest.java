package com.aredondocharro.ClothingStore.sharedTEST.log;

import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    @Test
    void maskEmail_regularEmail() {
        assertEquals("j***@example.com", LogSanitizer.maskEmail("john@example.com"));
    }

    @Test
    void maskEmail_shortLocalPart() {
        assertEquals("***", LogSanitizer.maskEmail("a@b.com"));
    }

    @Test
    void maskEmail_nullReturnsNull() {
        assertNull(LogSanitizer.maskEmail(null));
    }

    @Test
    void maskUuid_masksAsTemplate() {
        String masked = LogSanitizer.maskUuid("12345678-aaaa-bbbb-cccc-1234567890ab");
        assertTrue(masked.startsWith("12345678-****-****-****-"));
    }

    @Test
    void maskUuid_tooShort() {
        assertEquals("***", LogSanitizer.maskUuid("1234567"));
    }

    @Test
    void maskToken_keepsPrefixAndLength() {
        String masked = LogSanitizer.maskToken("abcdef0123456789");
        assertTrue(masked.startsWith("abcdef*** (len=16)"));
    }

    @Test
    void maskToken_nullIsLiteralNull() {
        assertEquals("null", LogSanitizer.maskToken(null));
    }
}
