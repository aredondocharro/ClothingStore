package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;


public interface ChangePasswordUseCase {
    void change(UserId userId, String currentPassword, String newPassword);
}
