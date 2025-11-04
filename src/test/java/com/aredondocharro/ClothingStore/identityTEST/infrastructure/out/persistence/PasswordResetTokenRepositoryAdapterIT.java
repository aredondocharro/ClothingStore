package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import com.aredondocharro.ClothingStore.testconfig.TestcontainersConfiguration;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        PasswordResetTokenRepositoryAdapter.class,
        PasswordResetTokenRepositoryAdapterIT.MapperTestConfig.class // 👈 IMPORTANTE
})
class PasswordResetTokenRepositoryAdapterIT {

    @TestConfiguration
    static class MapperTestConfig {
        @Bean
        PasswordResetTokenMapper passwordResetTokenMapper() {
            return Mappers.getMapper(PasswordResetTokenMapper.class);
        }
    }

    @Autowired private SpringPasswordResetTokenJpaRepository jpa;
    @Autowired private PasswordResetTokenRepositoryAdapter adapter; // puedes autowire por interfaz si prefieres
    @Autowired private SpringDataUserRepository userRepo;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    void save_findValid_markUsed_deleteAll() throws Exception {
        // -- 1) Precondición: el usuario debe existir (FK)
        UUID userUuid = UUID.randomUUID();
        var userId = UserId.of(userUuid);

        userRepo.save(UserEntity.builder()
                .id(userUuid)
                .email("reset-it@example.com")
                .passwordHash(BCRYPT)
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now())
                .build());

        // -- 2) Preparamos token (DOMINIO)
        String rawToken = "raw-token-ABC";
        String tokenHash = sha256Base64(rawToken);
        Instant created = Instant.now();
        Instant expires = created.plus(30, ChronoUnit.MINUTES);

        PasswordResetToken token = new PasswordResetToken(
                PasswordResetTokenId.newId(),
                userId,
                tokenHash,
                expires,
                null,
                created
        );

        // -- 3) save
        adapter.save(token);

        // -- 4) find valid
        Optional<PasswordResetToken> found =
                adapter.findValidByHash(tokenHash, Instant.now());
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().userId());               // VO vs VO

        // -- 5) mark used
        adapter.markUsed(token.id(), Instant.now());
        Optional<PasswordResetToken> afterUse =
                adapter.findValidByHash(tokenHash, Instant.now());
        assertTrue(afterUse.isEmpty(), "Once used, token must not be returned as valid");

        // -- 6) deleteAllForUser
        adapter.deleteAllForUser(userId);
        Optional<PasswordResetTokenEntity> leftover =
                jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, Instant.now());
        assertTrue(leftover.isEmpty());
    }

    private static String sha256Base64(String raw) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }
}
