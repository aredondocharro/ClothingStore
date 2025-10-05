package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserView> findByEmail(String email) {
        log.debug("Finding user by email={}", maskEmail(email));

        Optional<UserEntity> opt = repo.findByEmailIgnoreCase(email);
        if (opt.isEmpty()) {
            log.debug("User not found by email={}", maskEmail(email));
            return Optional.empty();
        }

        UserEntity u = opt.get();
        log.debug("User found id={} email={}", u.getId(), maskEmail(u.getEmail()));
        return Optional.of(toView(u));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserView> findById(UUID id) {
        log.debug("Finding user by id={}", id);

        Optional<UserEntity> opt = repo.findById(id);
        if (opt.isEmpty()) {
            log.debug("User not found id={}", id);
            return Optional.empty();
        }

        UserEntity u = opt.get();
        log.debug("User found id={} email={}", u.getId(), maskEmail(u.getEmail()));
        return Optional.of(toView(u));
    }

    @Override
    @Transactional
    public void updatePasswordHash(UUID id, String newHash) {
        log.debug("Updating password hash for user id={}", id);

        UserEntity u = repo.findById(id).orElseThrow(() -> {
            log.warn("Attempt to update password for non-existing user id={}", id);
            return new IllegalArgumentException("User not found");
        });

        u.setPasswordHash(newHash);
        repo.save(u);

        log.info("Password hash updated for user id={}", id);
    }

    private static UserView toView(UserEntity u) {
        return new UserView(u.getId(), u.getEmail(), u.getPasswordHash(), u.isEmailVerified());
    }
}
