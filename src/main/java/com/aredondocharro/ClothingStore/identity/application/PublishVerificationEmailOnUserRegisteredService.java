package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;


@Slf4j
@Component
@RequiredArgsConstructor
public class PublishVerificationEmailOnUserRegisteredService {

    private final TokenGeneratorPort tokens;
    private final LoadUserPort loadUsers;
    private final EventBusPort eventBus;
    private final Clock clock;

    @Value("${app.verify.baseUrl}")
    private String verifyBaseUrl;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(UserRegistered e) {
        // Si tu TokenGeneratorPort necesita el agregado completo:
        User user = loadUsers.findById(e.userId()).orElseThrow();

        String token = tokens.generateVerificationToken(user);
        String url   = verifyBaseUrl + "?token=" +
                    URLEncoder.encode(token, StandardCharsets.UTF_8);

        eventBus.publish(new VerificationEmailRequested(e.email(), url, Instant.now(clock)));
        log.info("Published VerificationEmailRequested for userId={} email={}", e.userId(), LogSanitizer.maskEmail(e.email().getValue()));
    }
}
