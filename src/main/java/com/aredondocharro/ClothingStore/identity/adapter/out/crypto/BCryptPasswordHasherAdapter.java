package com.aredondocharro.ClothingStore.identity.adapter.out.crypto;

import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String raw) {
        String h = encoder.encode(raw);
        log.debug("Password hashed (bcrypt)"); // nunca loguees el hash ni la password
        return h;
    }

    @Override
    public boolean matches(String raw, String hash) {
        boolean match = encoder.matches(raw, hash);
        log.debug("Password match: {}", match);
        return match;
    }
}
