package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserJpaAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserJpaAdapterTest {

    @Mock
    SpringDataUserRepository repo;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    void save_mapsDomainRolesToStrings_andReturnsDomainWithSameRoles() {
        var adapter = new UserJpaAdapter(repo);

        var domain = new User(
                null, // id nulo: el adapter lo rellenará
                Email.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                false,
                Set.of(Role.USER, Role.ADMIN),
                Instant.now()
        );

        // repo.save devuelve lo que recibe (simulamos persistencia)
        when(repo.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = adapter.save(domain);

        // Verifica que el adapter pasó strings "USER","ADMIN" al entity
        ArgumentCaptor<UserEntity> cap = ArgumentCaptor.forClass(UserEntity.class);
        verify(repo).save(cap.capture());
        var entity = cap.getValue();
        assertNotNull(entity.getId(), "adapter should set id if null");
        assertTrue(entity.getRoles().containsAll(Set.of("USER", "ADMIN")));

        // Devuelve dominio con los mismos roles
        assertEquals(Set.of(Role.USER, Role.ADMIN), saved.roles());
        assertEquals("user@example.com", saved.email().getValue());
    }

    @Test
    void findById_nullRoles_defaultsToUser() {
        var adapter = new UserJpaAdapter(repo);
        var id = UUID.randomUUID();

        var entity = UserEntity.builder()
                .id(id)
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(true)
                .roles(null) // viene null de BD
                .createdAt(Instant.now())
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(entity));

        var user = adapter.findById(id).orElseThrow();
        assertEquals(java.util.Set.of(Role.USER), user.roles()); // ← default
    }

    @Test
    void findByEmail_mapsEntityToDomain() {
        UserJpaAdapter adapter = new UserJpaAdapter(repo);
        UUID id = UUID.randomUUID();

        UserEntity entity = UserEntity.builder()
                .id(id)
                .email("u@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(false)
                .roles(new HashSet<>(List.of("USER")))
                .createdAt(Instant.now())
                .build();

        when(repo.findByEmail("u@example.com")).thenReturn(Optional.of(entity));

        Optional<User> opt = adapter.findByEmail(Email.of("u@example.com"));
        assertTrue(opt.isPresent());
        User u = opt.get();
        assertEquals(id, u.id());
        assertEquals("u@example.com", u.email().getValue());
        assertFalse(u.emailVerified());
        assertEquals(Set.of(Role.USER), u.roles());
    }
}
