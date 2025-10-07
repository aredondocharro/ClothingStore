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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.thymeleaf.exceptions.TemplateInputException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackageClasses = { EmailController.class })
public class NotificationGlobalErrorHandler {

    // -------------------------
    // DOMAIN EXCEPTIONS (NOTIFICATION)
    // -------------------------

    @ExceptionHandler(RecipientsRequiredException.class)
    ResponseEntity<ErrorResponse> handleRecipientsRequired(RecipientsRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.recipients_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(SubjectRequiredException.class)
    ResponseEntity<ErrorResponse> handleSubjectRequired(SubjectRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.subject_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(BodyRequiredException.class)
    ResponseEntity<ErrorResponse> handleBodyRequired(BodyRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.body_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(TemplateIdRequiredException.class)
    ResponseEntity<ErrorResponse> handleTemplateIdRequired(TemplateIdRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.template_id_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(InvalidEmailAddressException.class)
    ResponseEntity<ErrorResponse> handleInvalidEmailAddress(InvalidEmailAddressException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.invalid_email_address", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(EmailSendFailedException.class)
    ResponseEntity<ErrorResponse> handleEmailSendFailed(EmailSendFailedException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "notification.email_send_failed", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // TEMPLATE / I18N / SMTP
    // -------------------------

    @ExceptionHandler(TemplateInputException.class)
    ResponseEntity<ErrorResponse> handleTemplateInput(TemplateInputException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.template_error", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(NoSuchMessageException.class)
    ResponseEntity<ErrorResponse> handleNoSuchMessage(NoSuchMessageException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.i18n_missing_message", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(MessagingException.class)
    ResponseEntity<ErrorResponse> handleMessaging(MessagingException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "notification.smtp_error", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // SPRING / VALIDATION / HTTP
    // -------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors()
                .stream().map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "notification.validation_error", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.bad_request", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Missing request parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, "notification.missing_parameter", msg, req, null, ex, false);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "notification.method_not_allowed", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "notification.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // FALLBACK
    // -------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "notification.internal_error", "Unexpected error", req, null, ex, true);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String code,
                                                String message,
                                                HttpServletRequest req,
                                                List<ErrorResponse.FieldError> fieldErrors,
                                                Exception ex,
                                                boolean logAsError) {
        if (logAsError) {
            log.error("[{}] {} - {}", code, status.value(), ex.getMessage(), ex);
        } else {
            log.warn("[{}] {} - {}", code, status.value(), ex.getMessage());
        }
        var body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req != null ? req.getRequestURI() : null,
                (fieldErrors == null || fieldErrors.isEmpty()) ? null : List.copyOf(fieldErrors)
        );
        return ResponseEntity.status(status).body(body);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage());
    }
}
