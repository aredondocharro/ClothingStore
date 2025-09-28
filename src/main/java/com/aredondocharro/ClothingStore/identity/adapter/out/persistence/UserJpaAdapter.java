package com.aredondocharro.ClothingStore.identity.adapter.out.persistence;

import com.aredondocharro.ClothingStore.identity.adapter.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.adapter.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class UserJpaAdapter implements LoadUserPort, SaveUserPort {

    private final SpringDataUserRepository repo;

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email={}", email);
        return repo.findByEmail(email.toLowerCase()).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        log.debug("Saving user email={}", user.getEmail());
        UserEntity saved = repo.save(toEntity(user));
        log.info("User persisted id={} email={}", saved.getId(), saved.getEmail());
        return toDomain(saved);
    }
    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by id={}", id);
        return repo.findById(id).map(this::toDomain);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.isEmailVerified(), e.getRoles(), e.getCreatedAt());
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId() == null ? UUID.randomUUID() : u.getId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .emailVerified(u.isEmailVerified())
                .roles(u.getRoles())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
