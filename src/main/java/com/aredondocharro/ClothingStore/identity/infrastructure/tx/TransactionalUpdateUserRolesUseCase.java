package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
public class TransactionalUpdateUserRolesUseCase implements UpdateUserRolesUseCase {
    private final UpdateUserRolesUseCase delegate;

    @Override
    @Transactional
    public void setRoles(UserId userId, Set<Role> roles) {
        delegate.setRoles(userId, roles);
    }
}
