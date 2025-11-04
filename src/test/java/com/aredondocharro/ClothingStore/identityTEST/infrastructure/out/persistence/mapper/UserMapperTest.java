package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordNotBCryptedException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    // MapStruct instance without Spring context
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    @DisplayName("entity -> domain maps all fields and roles")
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

        User u = mapper.toDomain(e);

        assertEquals(UserId.of(id), u.id());
        assertEquals("user@example.com", u.email().getValue());
        assertEquals(BCRYPT, u.passwordHash().getValue());
        assertTrue(u.emailVerified());
        assertEquals(Set.of(Role.USER, Role.ADMIN), u.roles());
        assertEquals(now, u.createdAt());
    }

    @Test
    @DisplayName("entity -> domain applies USER fallback when roles are null/empty")
    void toDomain_whenRolesEmpty_defaultsToUSER() {
        UserEntity e = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(false)
                .roles(Set.of()) // empty -> fallback USER
                .createdAt(Instant.now())
                .build();

        User u = mapper.toDomain(e);
        assertEquals(Set.of(Role.USER), u.roles());
    }

    @Test
    @DisplayName("toCredentialsView throws when password hash is not a valid bcrypt")
    void toCredentialsView_invalidHash_throws() {
        UserEntity e = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("u@example.com")
                .passwordHash("not-bcrypt") // invalid on purpose
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now())
                .build();

        assertThrows(PasswordNotBCryptedException.class,
                () -> mapper.toCredentialsView(e));
    }

    @Test
    @DisplayName("Round-trip entity → domain → entity preserves key data")
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

        UserEntity entity = mapper.toEntity(domain);
        User again = mapper.toDomain(entity);

        assertEquals(domain.id(), again.id());
        assertEquals(domain.email().getValue(), again.email().getValue());
        assertEquals(domain.passwordHash().getValue(), again.passwordHash().getValue());
        assertEquals(domain.emailVerified(), again.emailVerified());
        assertEquals(domain.roles(), again.roles());
        assertEquals(domain.createdAt(), again.createdAt());
    }
}
