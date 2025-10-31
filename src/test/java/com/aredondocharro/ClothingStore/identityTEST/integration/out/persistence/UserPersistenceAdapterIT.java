package com.aredondocharro.ClothingStore.identityTEST.integration.out.persistence;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserMapper;
import com.aredondocharro.ClothingStore.testconfig.TestcontainersConfiguration;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserPersistenceAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserPersistenceAdapter.class})
class UserPersistenceAdapterIT {

    @TestConfiguration
    static class MapstructTestConfig {
        @Bean
        UserMapper userMapper() {
            // Usa la clase generada por MapStruct (UserMapperImpl)
            return Mappers.getMapper(UserMapper.class);
        }
    }
    @Autowired
    private SpringDataUserRepository repo;

    @Autowired
    private UserPersistenceAdapter adapter;

    private static final String HASH =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    void save_and_findById_roundtrip_rolesAndFields() {
        // ⬇️ id NO nulo: lo genera el dominio
        User domain = new User(
                UserId.newId(),
                IdentityEmail.of("ituser@example.com"),
                PasswordHash.ofHashed(HASH),
                false,
                Set.of(Role.USER, Role.ADMIN),
                Instant.now()
        );

        User persisted = adapter.save(domain);
        assertNotNull(persisted.id());

        Optional<User> loaded = adapter.findById(persisted.id());
        assertTrue(loaded.isPresent());

        User u = loaded.get();
        assertEquals("ituser@example.com", u.email().getValue());
        assertEquals(HASH, u.passwordHash().getValue());
        assertEquals(Set.of(Role.USER, Role.ADMIN), u.roles());
        assertFalse(u.emailVerified());
    }

    @Test
    void findByEmail_roundtrip() {
        User domain = new User(
                UserId.newId(),
                IdentityEmail.of("itmail@example.com"),
                PasswordHash.ofHashed(HASH),
                true,
                Set.of(Role.USER),
                Instant.now()
        );

        adapter.save(domain);

        Optional<User> loaded = adapter.findByEmail(IdentityEmail.of("ITMAIL@example.com"));
        assertTrue(loaded.isPresent());
        assertEquals("itmail@example.com", loaded.get().email().getValue());
        assertTrue(loaded.get().emailVerified());
    }
}
