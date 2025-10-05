package com.aredondocharro.ClothingStore.identity.infrastructure.security;

public final class PasswordPolicyConstants {

    private PasswordPolicyConstants() {}

    /** Longitud permitida (alineada con BCrypt/Argon2 límites habituales) */
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;

    /** Reglas: al menos 1 minúscula, 1 mayúscula y 1 dígito */
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";

    /** Mensaje estándar para validación */
    public static final String MESSAGE = "password must contain upper, lower and digit";
}
