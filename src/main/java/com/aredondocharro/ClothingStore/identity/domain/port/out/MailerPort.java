package com.aredondocharro.ClothingStore.identity.domain.port.out;


public interface MailerPort {
    void sendVerificationEmail(String to, String verificationUrl);
    void sendPasswordResetLink(String to, String resetLink);
}
