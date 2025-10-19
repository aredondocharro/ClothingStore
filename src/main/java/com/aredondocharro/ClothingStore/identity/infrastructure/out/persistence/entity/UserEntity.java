package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@AllArgsConstructor // Utilizamos AllArgsConstructor para el builder en JPA en combinación con @Builder y @NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @ElementCollection(fetch = FetchType.LAZY) // mejor LAZY; usa EAGER si realmente lo necesitas
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false)
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private Set<Role> roles = new HashSet<>(); // ← evita nulls cuando usas builder()

    @Column(name = "created_at", nullable = false)
    private Instant createdAt; // ← lo trae la capa de aplicación
}
