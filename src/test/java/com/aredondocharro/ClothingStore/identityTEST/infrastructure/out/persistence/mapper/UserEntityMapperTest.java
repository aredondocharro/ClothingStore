package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordNotBCryptedException;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserEntityMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityMapperTest {

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    void toDomain_mapsAllFields_andRoles() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity e = UserEntity.builder()
                .id(id)
                .email("user@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(true)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .createdAt(now)
                .build();

        User u = UserEntityMapper.toDomain(e);

        assertEquals(UserId.of(id), u.id());
        assertEquals("user@example.com", u.email().getValue());
        assertEquals(BCRYPT, u.passwordHash().getValue());
        assertTrue(u.emailVerified());
        assertEquals(Set.of(Role.USER, Role.ADMIN), u.roles());
        assertEquals(now, u.createdAt());
    }

    @Test
    void toDomain_whenRolesEmpty_defaultsToUSER() {
        UserEntity e = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(false)
                .roles(Set.of()) // vacío
                .createdAt(Instant.now())
                .build();

        User u = UserEntityMapper.toDomain(e);
        assertEquals(Set.of(Role.USER), u.roles());
    }

    @Test
    void toCredentialsView_invalidHash_throws() {
        UserEntity e = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("u@example.com")
                .passwordHash("not-bcrypt") // inválido a propósito
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now())
                .build();

        assertThrows(PasswordNotBCryptedException.class,
                () -> UserEntityMapper.toCredentialsView(e));
    }

    @Test
    void roundTrip_domainToEntityToDomain_preservesData() {
        Instant now = Instant.now();
        User domain = User.rehydrate(
                UserId.newId(),
                IdentityEmail.of("rt@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                true,
                Set.of(Role.USER),
                now
        );

        UserEntity entity = UserEntityMapper.toEntity(domain);
        User again = UserEntityMapper.toDomain(entity);

        assertEquals(domain.id(), again.id());
        assertEquals(domain.email().getValue(), again.email().getValue());
        assertEquals(domain.passwordHash().getValue(), again.passwordHash().getValue());
        assertEquals(domain.emailVerified(), again.emailVerified());
        assertEquals(domain.roles(), again.roles());
        assertEquals(domain.createdAt(), again.createdAt());
    }
}
