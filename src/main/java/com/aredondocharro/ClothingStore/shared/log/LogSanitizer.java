package com.aredondocharro.ClothingStore.shared.log;

import java.util.regex.Pattern;

public final class LogSanitizer {
    private LogSanitizer() {}

    // Emails en texto libre (para sanitizar mensajes de excepciones)
    private static final Pattern EMAIL_IN_TEXT =
            Pattern.compile("([A-Za-z0-9._%+-])[A-Za-z0-9._%+-]*(@[^\\s\"'<>]+)");

    public static String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***@" + (at + 1 < email.length() ? email.substring(at + 1) : "***");
    }

    public static String sanitizeText(String msg) {
        if (msg == null) return null;
        String s = EMAIL_IN_TEXT.matcher(msg).replaceAll("$1***$2");              // emails
        s = s.replaceAll("(?i)(Bearer\\s+)[A-Za-z0-9._-]+", "$1***");             // Authorization
        s = s.replaceAll("\\b(\\d{6})\\d{3,9}(\\d{4})\\b", "$1******$2");         // PAN (tarjeta 13–19)
        s = s.replaceAll("\\b(\\w{4})\\w{4,}(\\w{2})\\b", "$1***$2");             // tokens genéricos
        s = s.replaceAll("\\b(\\d{1,3}\\.){3}\\d{1,3}\\b", "***.***.***.***");    // IPv4
        return s;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "***" + phone.substring(phone.length() - 4);
    }

    public static String maskIban(String iban) {
        if (iban == null || iban.length() < 6) return "***";
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }

    public static String maskUuid(String uuid) {
        if (uuid == null || uuid.length() < 8) return "***";
        return uuid.substring(0, 8) + "-****-****-****-************";
    }
}
