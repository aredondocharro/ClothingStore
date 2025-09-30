package com.aredondocharro.ClothingStore.notification.infrastructure.in.web;

import com.aredondocharro.ClothingStore.notification.infrastructure.in.dto.SendEmailRequest;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Tag(name = "email", description = "Email sending operations")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final SendEmailUseCase sendEmail;

    @Operation (summary = "Send an email", description = "Sends an email to the specified recipients.")
    @PostMapping
    public ResponseEntity<Void> send(@Valid @RequestBody SendEmailRequest req) {
        Locale locale = req.locale() != null ? req.locale() : Locale.getDefault();
        sendEmail.send(req.from(), req.to(), req.templateId(), req.model(), locale);
        log.debug("Email sent request processed");
        return ResponseEntity.accepted().build();
    }
}
