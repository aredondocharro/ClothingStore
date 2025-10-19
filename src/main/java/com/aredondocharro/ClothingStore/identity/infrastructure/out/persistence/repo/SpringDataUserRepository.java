package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    @Query("""
           select (count(u) > 0)
           from UserEntity u join u.roles r
           where u.id = :id and r = :role
           """)
    boolean userHasRole(@Param("id") UUID id, @Param("role") Role role);

    @Query("""
           select count(distinct u)
           from UserEntity u join u.roles r
           where r = :role
           """)
    int countUsersWithRole(@Param("role") Role role);
}
