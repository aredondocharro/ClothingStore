package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.User;

import java.time.Instant;

public interface VerificationTokenRotationPort {

    /**
     * Rota el token de verificación del usuario:
     * - Revoca/invalida todos los tokens anteriores activos asociados al usuario.
     * - Crea un nuevo token de verificación.
     * - Registra este nuevo token como el actual.
     */
    RotatedVerificationToken rotateForUser(User user);

    record RotatedVerificationToken(
            String token,        // JWT listo para poner en la URL
            Instant expiresAt
    ) {}
}
