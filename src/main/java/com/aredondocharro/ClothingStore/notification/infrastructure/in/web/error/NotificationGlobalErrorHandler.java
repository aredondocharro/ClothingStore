package com.aredondocharro.ClothingStore.notification.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.notification.domain.exception.*;
import com.aredondocharro.ClothingStore.notification.infrastructure.in.web.EmailController;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.thymeleaf.exceptions.TemplateInputException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackageClasses = {EmailController.class})
public class NotificationGlobalErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors()
                .stream().map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "notification.validation_error", "Validation failed", req, fields, ex, false);
    }

    // 400 - falta asunto
    @ExceptionHandler(SubjectRequiredException.class)
    ResponseEntity<ErrorResponse> handleSubjectRequired(SubjectRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.subject_required", ex.getMessage(), req, null, ex, false);
    }

    // 400 - falta cuerpo del mensaje
    @ExceptionHandler(BodyRequiredException.class)
    ResponseEntity<ErrorResponse> handleBodyRequired(BodyRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.body_required", ex.getMessage(), req, null, ex, false);
    }


    @ExceptionHandler({ HttpMessageNotReadableException.class, IllegalArgumentException.class })
    ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // Plantilla inexistente / inaccesible
    @ExceptionHandler(TemplateInputException.class)
    ResponseEntity<ErrorResponse> handleTemplateNotFound(TemplateInputException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.template_not_found",
                "Email template not found or not readable", req, null, ex, false);
    }

    // Asunto i18n faltante
    @ExceptionHandler(NoSuchMessageException.class)
    ResponseEntity<ErrorResponse> handleNoMessage(NoSuchMessageException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.subject_key_missing", ex.getMessage(), req, null, ex, false);
    }

    // Error al enviar correo
    @ExceptionHandler(MessagingException.class)
    ResponseEntity<ErrorResponse> handleMessaging(MessagingException ex, HttpServletRequest req) {
        // Integración externa → 502 (o 503 si prefieres)
        return build(HttpStatus.BAD_GATEWAY, "notification.email_send_failed",
                "Failed to send email", req, null, ex, true);
    }
    // 400 - faltan destinatarios
    @ExceptionHandler(RecipientsRequiredException.class)
    ResponseEntity<ErrorResponse> handleRecipientsRequired(RecipientsRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.recipients_required",
                ex.getMessage(), req, null, ex, false);
    }

    // 400 - falta templateId
    @ExceptionHandler(TemplateIdRequiredException.class)
    ResponseEntity<ErrorResponse> handleTemplateIdRequired(TemplateIdRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.template_id_required",
                ex.getMessage(), req, null, ex, false);
    }

    // 400 - email inválido (desde VO EmailAddress)
    @ExceptionHandler(InvalidEmailAddressException.class)
    ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailAddressException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.invalid_email",
                ex.getMessage(), req, null, ex, false);
    }

    // 502 - fallo del proveedor de correo (dominio)
    @ExceptionHandler(EmailSendFailedException.class)
    ResponseEntity<ErrorResponse> handleEmailSendFailed(EmailSendFailedException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "notification.email_send_failed",
                ex.getMessage(), req, null, ex, true);
    }


    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "notification.internal_error", "Unexpected error", req, null, ex, true);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest req, List<ErrorResponse.FieldError> fields,
                                                Exception ex, boolean logAsError) {
        if (logAsError) log.error("{}: {}", code, ex.getMessage(), ex);
        else log.warn("{}: {}", code, ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), code, message, req.getRequestURI(), fields
        ));
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage());
    }
}
