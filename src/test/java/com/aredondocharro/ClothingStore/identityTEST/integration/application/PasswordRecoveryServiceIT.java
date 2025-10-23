package com.aredondocharro.ClothingStore.identityTEST.integration.application;

import com.aredondocharro.ClothingStore.testconfig.TestcontainersConfiguration;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ TestcontainersConfiguration.class, PasswordResetTokenRepositoryAdapter.class })
class PasswordResetTokenRepositoryAdapterIT {

    @org.springframework.beans.factory.annotation.Autowired
    private SpringPasswordResetTokenJpaRepository jpa;

    @org.springframework.beans.factory.annotation.Autowired
    private PasswordResetTokenRepositoryAdapter adapter;

    @org.springframework.beans.factory.annotation.Autowired
    private SpringDataUserRepository userRepo;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @Test
    void save_findValid_markUsed_deleteAll() throws Exception {
        // 1) Crea primero el usuario en BD (requisito del FK fk_prt_user)
        UserId userId = UserId.newId();
        seedUser(userId, "reset@example.com");

        // 2) Construye el token con el MISMO userId
        String rawToken  = "raw-token-ABC";
        String tokenHash = sha256Base64(rawToken);
        Instant created  = Instant.now();
        Instant expires  = created.plus(30, ChronoUnit.MINUTES);

        PasswordResetTokenRepositoryPort.Token token =
                new PasswordResetTokenRepositoryPort.Token(
                        PasswordResetTokenId.newId(),
                        userId,              // <-- mismo userId que acabas de sembrar
                        tokenHash,
                        expires,
                        null,
                        created
                );

        // save
        adapter.save(token);

        // find valid
        Optional<PasswordResetTokenRepositoryPort.Token> found =
                adapter.findValidByHash(tokenHash, Instant.now());
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().userId());

        // mark used
        adapter.markUsed(token.id(), Instant.now());
        Optional<PasswordResetTokenRepositoryPort.Token> afterUse =
                adapter.findValidByHash(tokenHash, Instant.now());
        assertTrue(afterUse.isEmpty(), "Once used, token must not be returned as valid");

        // deleteAllForUser
        adapter.deleteAllForUser(userId);
        assertTrue(
                jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, Instant.now()).isEmpty()
        );
    }

    // ---------- helpers ----------
    private void seedUser(UserId userId, String email) {
        userRepo.save(
                UserEntity.builder()
                        .id(userId.value())
                        .email(email)
                        .passwordHash(BCRYPT)
                        .emailVerified(true)
                        .roles(Set.of(Role.USER))
                        .createdAt(Instant.now())
                        .build()
        );
    }

    private static String sha256Base64(String raw) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }
}
