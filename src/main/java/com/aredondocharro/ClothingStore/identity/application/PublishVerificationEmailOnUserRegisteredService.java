package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenRotationPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class PublishVerificationEmailOnUserRegisteredService {

    private final LoadUserPort loadUsers;
    private final VerificationTokenRotationPort tokenRotationPort;
    private final EventBusPort eventBus;
    private final Clock clock;
    private final String verifyBaseUrl;

    public void on(UserRegistered e) {
        log.debug("Handling UserRegistered event (userId={})", e.userId());

        User user = loadUsers.findById(UserId.of(e.userId()))
                .orElseThrow(() -> {
                    log.warn("User not found while handling UserRegistered (userId={})", e.userId());
                    return new IllegalStateException("User not found: " + e.userId());
                });

        // Rota/crea el token de verificación y lo guarda en la tabla verification_tokens
        VerificationTokenRotationPort.RotatedVerificationToken rotated =
                tokenRotationPort.rotateForUser(user);

        String token = rotated.token();
        String url = verifyBaseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String email = user.email().getValue();
        eventBus.publish(new VerificationEmailRequested(email, url, Instant.now(clock)));

        log.info("Published VerificationEmailRequested (userId={}, email={})",
                e.userId(), LogSanitizer.maskEmail(email));
        // Do NOT log token or URL
    }
}
