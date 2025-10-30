package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalDeleteUserUseCase implements DeleteUserUseCase {
    private final DeleteUserUseCase delegate;

    @Override
    @Transactional
    public void delete(UserId userId) {
        delegate.delete(userId);
    }
}
