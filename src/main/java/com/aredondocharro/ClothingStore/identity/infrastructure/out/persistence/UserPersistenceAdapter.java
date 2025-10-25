package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserEntityMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(IdentityEmail email) {
        log.debug("Finding user by email={}", maskEmail(email.getValue()));
        return repo.findByEmailIgnoreCase(email.getValue()).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        log.debug("Finding user by id={}", id.value());
        return repo.findById(id.value()).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.id() == null) {
            throw new IllegalArgumentException("User.id must be provided by application layer");
        }
        if (user.createdAt() == null) {
            throw new IllegalArgumentException("User.createdAt must be provided by application layer");
        }

        log.debug("Saving user email={}", user.email().getValue());
        UserEntity saved = repo.save(UserEntityMapper.toEntity(user));
        log.info("User persisted id={}", saved.getId());
        log.debug("User persisted id={} email={}", saved.getId(), maskEmail(saved.getEmail()));
        return UserEntityMapper.toDomain(saved);
    }
}
