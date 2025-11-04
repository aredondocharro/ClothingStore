package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RefreshSessionEntityMapper {

    // entity -> domain
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "jti",          source = "jti")
    @Mapping(target = "userId",       expression = "java(UserId.of(e.getUserId()))")
    @Mapping(target = "expiresAt",    source = "expiresAt")
    @Mapping(target = "createdAt",    source = "createdAt")
    @Mapping(target = "revokedAt",    source = "revokedAt")
    @Mapping(target = "replacedByJti",source = "replacedByJti")
    @Mapping(target = "ip",           source = "ip")
    @Mapping(target = "userAgent",    source = "userAgent")
    RefreshSession toDomain(RefreshSessionEntity e);

    // domain -> entity (tokenHash NO existe en domain → ignorarlo aquí)
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "jti",          source = "jti")
    @Mapping(target = "userId",       expression = "java(s.userId().value())")
    @Mapping(target = "expiresAt",    source = "expiresAt")
    @Mapping(target = "createdAt",    source = "createdAt")
    @Mapping(target = "revokedAt",    source = "revokedAt")
    @Mapping(target = "replacedByJti",source = "replacedByJti")
    @Mapping(target = "ip",           source = "ip")
    @Mapping(target = "userAgent",    source = "userAgent")
    @Mapping(target = "tokenHash",    ignore = true)
    RefreshSessionEntity toEntity(RefreshSession s);
}
