package com.aredondocharro.ClothingStore.notification.domain.port.out;


import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;

public interface EmailSenderPort {
    void send(EmailMessage emailMessage);
}