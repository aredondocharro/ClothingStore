package com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.PasswordResetTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.RefreshSessionInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AdminUserController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthPasswordController;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.RefreshController;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackageClasses = {
        AuthController.class,
        AuthPasswordController.class,
        RefreshController.class,
        AdminUserController.class
})
public class IdentityGlobalErrorHandler {

    // -------------------------
    // DOMAIN (Identity)
    // -------------------------

    @ExceptionHandler(EmailAlreadyExistException.class)
    ResponseEntity<ErrorResponse> emailAlreadyExists(EmailAlreadyExistException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "identity.email_already_exists", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    ResponseEntity<ErrorResponse> emailNotVerified(EmailNotVerifiedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.email_not_verified", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(EmailRequiredException.class)
    ResponseEntity<ErrorResponse> emailRequired(EmailRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.email_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        var entity = build(HttpStatus.UNAUTHORIZED, "identity.invalid_credentials", ex.getMessage(), req, null, ex, false);
        return ResponseEntity.status(entity.getStatusCode())
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(entity.getBody());
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    ResponseEntity<ErrorResponse> invalidEmailFormat(InvalidEmailFormatException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.invalid_email_format", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    ResponseEntity<ErrorResponse> invalidPassword(InvalidPasswordException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.invalid_password", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    ResponseEntity<ErrorResponse> passwordMismatch(PasswordMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_mismatch", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordConfirmationMismatchException.class)
    ResponseEntity<ErrorResponse> passwordConfirmationMismatch(PasswordConfirmationMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_confirmation_mismatch", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordNotBCryptedException.class)
    ResponseEntity<ErrorResponse> passwordNotBCrypted(PasswordNotBCryptedException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_not_bcrypted", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordRequiredException.class)
    ResponseEntity<ErrorResponse> passwordRequired(PasswordRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ErrorResponse> userNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "identity.user_not_found", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(NewPasswordSameAsOldException.class)
    ResponseEntity<ErrorResponse> newPasswordSameAsOld(NewPasswordSameAsOldException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.new_password_same_as_old", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(RoleRequiredException.class)
    ResponseEntity<ErrorResponse> roleRequired(RoleRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.role_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(CannotRemoveLastAdminException.class)
    ResponseEntity<ErrorResponse> cannotRemoveLastAdmin(CannotRemoveLastAdminException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "identity.cannot_remove_last_admin", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(SelfDemotionForbiddenException.class)
    ResponseEntity<ErrorResponse> selfDemotionForbidden(SelfDemotionForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.self_demotion_forbidden", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // TOKENS de aplicación (verify-email / reset / refresh)
    // -------------------------

    @ExceptionHandler(VerificationTokenInvalidException.class)
    ResponseEntity<ErrorResponse> verifyTokenInvalid(VerificationTokenInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.verification_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    ResponseEntity<ErrorResponse> resetTokenInvalid(PasswordResetTokenInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_reset_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(RefreshSessionInvalidException.class)
    ResponseEntity<ErrorResponse> refreshInvalid(RefreshSessionInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "identity.refresh_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(MissingRefreshCookieException.class)
    ResponseEntity<ErrorResponse> refreshCookieMissing(MissingRefreshCookieException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "identity.refresh_cookie_missing", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // SPRING / VALIDATION / HTTP
    // -------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "identity.validation_error", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var fields = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldError(
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : null,
                        v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "identity.constraint_violation", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        var cause = ex.getMostSpecificCause();
        String msg = (cause != null ? cause.getMessage() : ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", msg, req, null, ex, false);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parameter '" + ex.getName() + "' has invalid value";
        return build(HttpStatus.BAD_REQUEST, "identity.type_mismatch", msg, req, null, ex, false);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Missing request parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, "identity.missing_parameter", msg, req, null, ex, false);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "identity.method_not_allowed", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> illegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // FALLBACK
    // -------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> any(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "identity.internal_error", "Unexpected error", req, null, ex, true);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest req,
            List<ErrorResponse.FieldError> fieldErrors,
            Exception ex,
            boolean logAsError
    ) {
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
