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

    // 400 - DTO inválido / JSON malformado
    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "validation.error", "Validation failed", req, fields, ex, false);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // 401 - credenciales inválidas / no autenticado
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(error(HttpStatus.UNAUTHORIZED, "identity.unauthorized", ex.getMessage(), req, null));
    }

    // 403 - autenticado pero sin verificar email o sin permisos
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccess(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.forbidden", ex.getMessage(), req, null, ex, false);
    }

    // 400 - token verificación inválido/expirado (JWT)
    @ExceptionHandler(JWTVerificationException.class)
    ResponseEntity<ErrorResponse> handleJwt(JWTVerificationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.verification_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    // 400 - email con formato inválido (desde VO Email.of(...))
    @ExceptionHandler(InvalidEmailFormatException.class)
    ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailFormatException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.invalid_email", ex.getMessage(), req, null, ex, false);
    }

    // 401 - credenciales inválidas (cuando migres LoginService)
    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(error(HttpStatus.UNAUTHORIZED, "identity.invalid_credentials", ex.getMessage(), req, null));
    }

    // 403 - autenticado pero email no verificado
    @ExceptionHandler(EmailNotVerifiedException.class)
    ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "identity.email_not_verified", ex.getMessage(), req, null, ex, false);
    }

    // 409 - conflicto: usuario ya existe
    @ExceptionHandler(EmailAlreadyExistException.class)
    ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "identity.user_already_exists", ex.getMessage(), req, null, ex, false);
    }

    // 400 - token de verificación inválido/expirado (dominio)
    @ExceptionHandler(VerificationTokenInvalidException.class)
    ResponseEntity<ErrorResponse> handleVerificationTokenInvalid(VerificationTokenInvalidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.verification_token_invalid", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Violaciones de validación en @PathVariable/@RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getConstraintViolations().stream().map(v -> new ErrorResponse.FieldError(v.getPropertyPath().toString(), v.getMessage())).toList();
        return build(HttpStatus.BAD_REQUEST, "validation.error", "Validation failed", req, fields, ex, false);
    }

    // 400 - Password no hasheada (cuando migres RegisterUseCase)
    @ExceptionHandler(HashedPasswordRequiredException.class)
    ResponseEntity<ErrorResponse> handleNotHashedPassword(HashedPasswordRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_not_hashed", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Password y confirmación no coinciden (cuando migres RegisterUseCase)
    @ExceptionHandler(PasswordConfirmationMismatchException.class)
    ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordConfirmationMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_mismatch", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Password no en formato BCrypt (cuando migres RegisterUseCase)
    @ExceptionHandler(PasswordNotBCryptedException.class)
    ResponseEntity<ErrorResponse> handlePasswordNotBCrypted(PasswordNotBCryptedException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_not_bcrypt", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Falta un parámetro obligatorio
    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.bad_request", ex.getMessage(), req, null, ex, false);
    }

    // 405 - Método no soportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "identity.method_not_allowed", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Password faltante en el request (cuando migres RegisterUseCase)
    @ExceptionHandler(PasswordRequiredException.class)
    ResponseEntity<ErrorResponse> handlePasswordRequired(PasswordRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.password_required", ex.getMessage(), req, null, ex, false);
    }

    // 400 - Falta la cookie HttpOnly de refresh (cuando migres RefreshController)
    @ExceptionHandler(MissingRefreshCookieException.class)
    ResponseEntity<ErrorResponse> handleMissingRefreshCookie(MissingRefreshCookieException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = List.of(new ErrorResponse.FieldError(MissingRefreshCookieException.COOKIE_NAME, "Required HttpOnly cookie was not provided"));

        return build(HttpStatus.BAD_REQUEST, "identity.missing_refresh_cookie", ex.getMessage(), req, fields, ex, false);
    }


    // 400 - Falta el email en el request (cuando migres RegisterUseCase)
    @ExceptionHandler(EmailRequiredException.class)
    ResponseEntity<ErrorResponse> handleEmailRequired(EmailRequiredException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "identity.email_required", ex.getMessage(), req, null, ex, false);
    }

    // 500 (fallback)
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "identity.internal_error", "Unexpected error", req, null, ex, true);
    }


    /* ===================== HELPERS ===================== */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req, List<ErrorResponse.FieldError> fields, Exception ex, boolean logAsError) {
        if (logAsError) log.error("{}: {}", code, ex.getMessage(), ex);
        else log.warn("{}: {}", code, ex.getMessage());
        return ResponseEntity.status(status).body(error(status, code, message, req, fields));
    }

    private ErrorResponse error(HttpStatus status, String code, String message, HttpServletRequest req, List<ErrorResponse.FieldError> fields) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), code, message, req.getRequestURI(), fields);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage());
    }
}
