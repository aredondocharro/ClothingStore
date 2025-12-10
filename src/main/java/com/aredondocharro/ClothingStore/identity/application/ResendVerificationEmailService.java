package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResendVerificationEmailUseCase;
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
public class ResendVerificationEmailService implements ResendVerificationEmailUseCase {

    private final LoadUserPort loadUserPort;
    private final VerificationTokenRotationPort tokenRotationPort;
    private final EventBusPort eventBus;
    private final Clock clock;
    private final String verifyBaseUrl;

    @Override
    public void resend(IdentityEmail email) {
        // 1) Intentar cargar usuario
        var maybeUser = loadUserPort.findByEmail(email);

        if (maybeUser.isEmpty()) {
            log.info("Resend verification requested for non-existing email={}", LogSanitizer.maskEmail(email.getValue()));
            // No-op para no revelar si existe o no
            return;
        }

        User user = maybeUser.get();

        // 2) Si ya está verificado: no hacemos nada
        if (user.emailVerified()) {
            log.info("Resend verification requested for already verified user id={} email={}",
                    user.id().value(), LogSanitizer.maskEmail(user.email().getValue()));
            return;
        }

        // 3) Rotar token de verificación (revocar anterior + crear nuevo)
        var rotated = tokenRotationPort.rotateForUser(user);

        String token = rotated.token();
        String url = verifyBaseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        // 4) Publicar el mismo evento que en el registro inicial
        eventBus.publish(new VerificationEmailRequested(
                user.email().getValue(),
                url,
                Instant.now(clock)
        ));

        log.info("Resent verification email for user id={} email={}",
                user.id().value(), LogSanitizer.maskEmail(user.email().getValue()));
    }
}

