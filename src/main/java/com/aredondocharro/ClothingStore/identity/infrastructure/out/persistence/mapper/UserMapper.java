package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    // --- entity -> domain ---
    default User toDomain(UserEntity e) {
        if (e == null) return null;
        Set<Role> roles = rolesOrDefault(e.getRoles());
        return User.rehydrate(
                UserId.of(e.getId()),
                IdentityEmail.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified(),
                roles,
                e.getCreatedAt()
        );
    }

    // --- domain -> entity (genera impl) ---
    @Mapping(target = "id",           expression = "java( u.id().value() )")
    @Mapping(target = "email",        expression = "java( u.email().getValue() )")
    @Mapping(target = "passwordHash", expression = "java( u.passwordHash().getValue() )")
    @Mapping(target = "emailVerified", source     = "emailVerified")
    @Mapping(target = "createdAt",    source     = "createdAt")
    @Mapping(target = "roles",        expression = "java( rolesOrDefault(u.roles()) )")
    UserEntity toEntity(User u);

    // --- entity -> CredentialsView (usa 'id' en lugar de 'userId') ---
    @Mapping(target = "id",           expression = "java( UserId.of(e.getId()) )")
    @Mapping(target = "email",        expression = "java( IdentityEmail.of(e.getEmail()) )")
    @Mapping(target = "passwordHash", expression = "java( PasswordHash.ofHashed(e.getPasswordHash()) )")
    @Mapping(target = "emailVerified", source     = "emailVerified")
    CredentialsView toCredentialsView(UserEntity e);

    // helper
    default Set<Role> rolesOrDefault(Set<Role> roles) {
        return (roles == null || roles.isEmpty()) ? Set.of(Role.USER) : Set.copyOf(roles);
    }
}
