package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.AdminSetRolesRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.AdminUserResponse;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin Users", description = "Administrative operations on users (requires ADMIN role)")
@RequiredArgsConstructor
public class AdminUserController {

    private final DeleteUserUseCase deleteUC;
    private final UpdateUserRolesUseCase rolesUC;

    @Operation(summary = "Delete a user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("ADMIN | delete request | userId={}", id);
        deleteUC.delete(UserId.of(id));
        log.info("ADMIN | delete success | userId={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign roles to a user (full replace)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Roles updated")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Policy conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping(path = "/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setRoles(@PathVariable UUID id,
                                         @Valid @RequestBody AdminSetRolesRequest body) {

        Set<Role> roles;
        try {
            roles = body.roles().stream()
                    .map(s -> Role.valueOf(s.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role value");
        }

        rolesUC.setRoles(UserId.of(id), roles);
        return ResponseEntity.noContent().build();
    }

}
