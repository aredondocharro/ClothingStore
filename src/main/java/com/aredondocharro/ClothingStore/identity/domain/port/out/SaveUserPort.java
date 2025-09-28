package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.User;

public interface SaveUserPort {
    User save(User user);
}
