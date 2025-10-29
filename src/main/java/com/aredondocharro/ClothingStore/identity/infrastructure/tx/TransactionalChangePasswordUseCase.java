// TransactionalChangePasswordUseCase.java
package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalChangePasswordUseCase implements ChangePasswordUseCase {

    private final ChangePasswordUseCase delegate;

    @Override
    @Transactional
    public void change(UserId userId, String currentPassword, String newPassword) {
        delegate.change(userId, currentPassword, newPassword);
    }
}
