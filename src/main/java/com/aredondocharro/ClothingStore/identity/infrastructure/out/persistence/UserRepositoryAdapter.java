package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<CredentialsView> findByEmail(IdentityEmail email) {
        log.debug("Finding user by email={}", maskEmail(email.getValue()));

        Optional<UserEntity> opt = repo.findByEmailIgnoreCase(email.getValue());
        if (opt.isEmpty()) {
            log.debug("User not found by email={}", maskEmail(email.getValue()));
            return Optional.empty();
        }

        UserEntity u = opt.get();
        log.debug("User found id={} email={}", u.getId(), maskEmail(u.getEmail()));
        return Optional.of(toView(u));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CredentialsView> findById(UserId id) {
        log.debug("Finding user by id={}", id);

        Optional<UserEntity> opt = repo.findById(id.value());
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
    public void updatePasswordHash(UserId id, String newHash) {
        log.debug("Updating password hash for user id={}", id);

        UserEntity u = repo.findById(id.value()).orElseThrow(() -> {
            log.warn("Attempt to update password for non-existing user id={}", id);
            return new UserNotFoundException(id);
        });

        u.setPasswordHash(newHash);
        repo.save(u);

        log.info("Password hash updated for user id={}", id);
    }

    @Override
    @Transactional
    public void delete(UserId id) {
        log.warn("Trying to delete the user account id={}", id);

        UserEntity u = repo.findById(id.value()).orElseThrow(() -> {
            log.warn("Attempt to delete non-existing user id={}", id);
            return new UserNotFoundException(id);
        });

        repo.delete(u);
        log.info("User deleted id={}", id);
    }

    private static CredentialsView toView(UserEntity u) {
        return new CredentialsView(
                UserId.of(u.getId()),                      // String → UserId
                IdentityEmail.of(u.getEmail()),          // String → IdentityEmail
                PasswordHash.ofHashed(u.getPasswordHash()), // String → PasswordHash
                u.isEmailVerified()
        );
    }
}
