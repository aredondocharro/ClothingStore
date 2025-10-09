package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean emailVerified;

    @ElementCollection(fetch = FetchType.EAGER) // o LAZY si prefieres
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false)
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Set<Role> roles = new HashSet<>();     // ✅ Cambiado: Set<String> → Set<Role>

    @Column(nullable = false)
    private Instant createdAt;
}
