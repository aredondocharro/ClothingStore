package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;

import java.util.Set;

public final class UserEntityMapper {

    private UserEntityMapper() {}

    public static User toDomain(UserEntity e) {
        if (e == null) return null;
        Set<Role> roles = (e.getRoles() == null || e.getRoles().isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(e.getRoles());

        return User.rehydrate(
                UserId.of(e.getId()),
                IdentityEmail.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified(),
                roles,
                e.getCreatedAt()
        );
    }

    public static UserEntity toEntity(User u) {
        if (u == null) return null;
        Set<Role> roles = (u.roles() == null || u.roles().isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(u.roles());

        return UserEntity.builder()
                .id(u.id().value())
                .email(u.email().getValue())
                .passwordHash(u.passwordHash().getValue())
                .emailVerified(u.emailVerified())
                .roles(roles)
                .createdAt(u.createdAt())
                .build();
    }

    public static CredentialsView toCredentialsView(UserEntity e) {
        if (e == null) return null;
        return new CredentialsView(
                UserId.of(e.getId()),
                IdentityEmail.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified()
        );
    }
}
