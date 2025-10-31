package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserPersistenceAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJpaAdapterTest {

    @Mock SpringDataUserRepository repo;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    private UserPersistenceAdapter newAdapter() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        return new UserPersistenceAdapter(repo, mapper);
    }

    @Test
    @DisplayName("save maps domain roles and returns domain with same roles")
    void save_mapsDomainRolesToStrings_andReturnsDomainWithSameRoles() {
        UserPersistenceAdapter adapter = newAdapter();

        UserId userId = UserId.newId();
        Instant now = Instant.now();

        // Dominio DEBE venir con id y createdAt (los genera aplicación)
        User domain = User.create(
                userId,
                IdentityEmail.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                Set.of(Role.USER, Role.ADMIN),
                now
        );

        // simulamos persistencia: repo.save devuelve lo que recibe
        when(repo.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = adapter.save(domain);

        // Verifica que el adapter mapeó correctamente a la entidad
        ArgumentCaptor<UserEntity> cap = ArgumentCaptor.forClass(UserEntity.class);
        verify(repo).save(cap.capture());
        UserEntity entity = cap.getValue();

        assertEquals(userId.value(), entity.getId());
        assertEquals("user@example.com", entity.getEmail());
        assertEquals(Set.of(Role.USER, Role.ADMIN), entity.getRoles());

        // Y que vuelve al dominio con los mismos datos/roles
        assertEquals(userId, saved.id());
        assertEquals("user@example.com", saved.email().getValue());
        assertEquals(Set.of(Role.USER, Role.ADMIN), saved.roles());
    }

    @Test
    @DisplayName("findById applies USER fallback when roles are null")
    void findById_nullRoles_defaultsToUser() {
        UserPersistenceAdapter adapter = newAdapter();
        UUID id = UUID.randomUUID();

        UserEntity entity = UserEntity.builder()
                .id(id)
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(true)
                .roles(null)                 // viene null de BD
                .createdAt(Instant.now())
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(entity));

        User user = adapter.findById(UserId.of(id)).orElseThrow();
        assertEquals(UserId.of(id), user.id());           // VO
        assertEquals(Set.of(Role.USER), user.roles());    // default a USER
    }

    @Test
    @DisplayName("findByEmail maps entity to domain correctly")
    void findByEmail_mapsEntityToDomain() {
        UserPersistenceAdapter adapter = newAdapter();
        UUID id = UUID.randomUUID();

        UserEntity entity = UserEntity.builder()
                .id(id)
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(false)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now())
                .build();

        when(repo.findByEmailIgnoreCase("u@example.com")).thenReturn(Optional.of(entity));

        Optional<User> opt = adapter.findByEmail(IdentityEmail.of("u@example.com"));
        assertTrue(opt.isPresent());
        User u = opt.get();

        assertEquals(UserId.of(id), u.id());             // VO en dominio
        assertEquals("u@example.com", u.email().getValue());
        assertFalse(u.emailVerified());
        assertEquals(Set.of(Role.USER), u.roles());
    }
}
