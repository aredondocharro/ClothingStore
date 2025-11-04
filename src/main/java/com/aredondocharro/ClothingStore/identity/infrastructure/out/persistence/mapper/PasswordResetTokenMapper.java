package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PasswordResetTokenMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",        expression = "java(PasswordResetTokenId.of(e.getId()))")
    @Mapping(target = "userId",    expression = "java(UserId.of(e.getUserId()))")
    @Mapping(target = "tokenHash", source = "tokenHash")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "usedAt",    source = "usedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    PasswordResetToken toDomain(PasswordResetTokenEntity e);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",        expression = "java(t.id().value())")
    @Mapping(target = "userId",    expression = "java(t.userId().value())")
    @Mapping(target = "tokenHash", source = "tokenHash")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "usedAt",    source = "usedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    PasswordResetTokenEntity toEntity(PasswordResetToken t);
}
