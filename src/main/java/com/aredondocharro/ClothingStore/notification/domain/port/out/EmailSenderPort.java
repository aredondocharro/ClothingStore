package com.aredondocharro.ClothingStore.notification.domain.port.out;


import com.aredondocharro.ClothingStore.notification.domain.model.Email;

public interface EmailSenderPort {
    void send(Email email);
}