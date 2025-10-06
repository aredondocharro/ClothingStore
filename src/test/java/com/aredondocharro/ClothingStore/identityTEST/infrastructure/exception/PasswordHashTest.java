package com.aredondocharro.ClothingStore.identityTEST.infrastructure.exception;
import com.aredondocharro.ClothingStore.identity.domain.exception.HashedPasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordNotBCryptedException;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {

    @Test
    void ofHashed_nullOrBlank_throwsHashedPasswordRequiredException() {
        assertAll(
                () -> assertThrows(HashedPasswordRequiredException.class, () -> PasswordHash.ofHashed(null)),
                () -> assertThrows(HashedPasswordRequiredException.class, () -> PasswordHash.ofHashed("")),
                () -> assertThrows(HashedPasswordRequiredException.class, () -> PasswordHash.ofHashed("   "))
        );
    }

    @Test
    void ofHashed_invalidFormat_throwsPasswordNotBCryptedException() {
        assertAll(
                () -> assertThrows(PasswordNotBCryptedException.class, () -> PasswordHash.ofHashed("$2b$X$not53chars")),
                () -> assertThrows(PasswordNotBCryptedException.class, () -> PasswordHash.ofHashed("$argon2id$v=19$...")),
                () -> assertThrows(PasswordNotBCryptedException.class, () -> PasswordHash.ofHashed("$2c$10$abcdef...")) // 2c no existe
        );
    }

    @Test
    void ofHashed_validBcrypt_passes() {
        // 60 chars totales; este es un ejemplo de estructura válida
        var valid = "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";
        var h = PasswordHash.ofHashed(valid);
        assertEquals(valid, h.getValue());
    }

}


