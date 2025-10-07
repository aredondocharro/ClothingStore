package com.aredondocharro.ClothingStore.identity.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.identity.domain.exception.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.web.AuthController;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackageClasses = {AuthController.class})
public class IdentityGlobalErrorHandler {

    // -------------------------
    // DOMAIN EXCEPTIONS (IDENTITY)
    // -------------------------

    @ExceptionHandler(EmailAlreadyExistException.class)
    ResponseEntity<ErrorResponse> handleEmailAlreadyExist(EmailAlreadyExistException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "identity.email_already_exists", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.email_not_verified", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(EmailRequiredException.class)
    ResponseEntity<ErrorResponse> handleEmailRequired(EmailRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.email_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(HashedPasswordRequiredException.class)
    ResponseEntity<ErrorResponse> handleHashedPasswordRequired(HashedPasswordRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.hashed_password_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        // Incluimos cabecera WWW-Authenticate por si algún cliente lo necesita
        var entity = build(HttpStatus.UNAUTHORIZED, "identity.invalid_credentials", ex.getMessage(), req, null, ex, false);
        return ResponseEntity.status(entity.getStatusCode()).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(entity.getBody());
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    ResponseEntity<ErrorResponse> handleInvalidEmailFormat(InvalidEmailFormatException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.invalid_email_format", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.invalid_password", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_mismatch", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(MissingRefreshCookieException.class)
    ResponseEntity<ErrorResponse> handleMissingRefreshCookie(MissingRefreshCookieException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.missing_refresh_cookie", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordConfirmationMismatchException.class)
    ResponseEntity<ErrorResponse> handlePasswordConfirmationMismatch(PasswordConfirmationMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_confirmation_mismatch", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordNotBCryptedException.class)
    ResponseEntity<ErrorResponse> handlePasswordNotBCrypted(PasswordNotBCryptedException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_not_bcrypted", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordRequiredException.class)
    ResponseEntity<ErrorResponse> handlePasswordRequired(PasswordRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_required", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(VerificationTokenInvalidException.class)
    ResponseEntity<ErrorResponse> handleVerificationTokenInvalid(VerificationTokenInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.verification_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {

        return build(HttpStatus.NOT_FOUND, "identity.user_not_found", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(NewPasswordSameAsOldException.class)
    ResponseEntity<ErrorResponse> handleNewPasswordSameAsOld(NewPasswordSameAsOldException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.new_password_same_as_old", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenInvalid(PasswordResetTokenInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_reset_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(RefreshSessionInvalidException.class)
    public ResponseEntity<ErrorResponse> handleRefreshSessionInvalid(RefreshSessionInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.refresh_session_invalid", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(RoleRequiredException.class)
    public ResponseEntity<ErrorResponse> handleRoleRequired(RoleRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.role_required", ex.getMessage(), req, null, ex, false);
    }
    // -------------------------
    // SECURITY / JWT
    // -------------------------

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        var entity = build(HttpStatus.UNAUTHORIZED, "identity.unauthorized", ex.getMessage(), req, null, ex, false);
        return ResponseEntity.status(entity.getStatusCode()).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(entity.getBody());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.access_denied", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(JWTVerificationException.class)
    ResponseEntity<ErrorResponse> handleJwtVerification(JWTVerificationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.jwt_invalid", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // SPRING / VALIDATION / HTTP
    // -------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "identity.validation_error", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var fields = ex.getConstraintViolations().stream().map(v -> new ErrorResponse.FieldError(v.getPropertyPath() != null ? v.getPropertyPath().toString() : null, v.getMessage())).toList();
        return build(HttpStatus.BAD_REQUEST, "identity.constraint_violation", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ex.getMostSpecificCause();
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", ex.getMostSpecificCause().getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parameter '" + ex.getName() + "' has invalid value";
        return build(HttpStatus.BAD_REQUEST, "identity.type_mismatch", msg, req, null, ex, false);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Missing request parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, "identity.missing_parameter", msg, req, null, ex, false);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "identity.method_not_allowed", ex.getMessage(), req, null, ex, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // -------------------------
    // FALLBACK
    // -------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "identity.internal_error", "Unexpected error", req, null, ex, true);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req, List<ErrorResponse.FieldError> fieldErrors, Exception ex, boolean logAsError) {
        if (logAsError) {
            log.error("[{}] {} - {}", code, status.value(), ex.getMessage(), ex);
        } else {
            log.warn("[{}] {} - {}", code, status.value(), ex.getMessage());
        }
        var body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), code, message, req != null ? req.getRequestURI() : null, (fieldErrors == null || fieldErrors.isEmpty()) ? null : List.copyOf(fieldErrors));
        return ResponseEntity.status(status).body(body);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage());
    }
}
