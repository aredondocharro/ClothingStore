package com.aredondocharro.ClothingStore.identity.domain.exception;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;


public class UserNotFoundException extends RuntimeException {


    public UserNotFoundException(UserId userId) {
        super("User not found: " + userId);
    }
}
