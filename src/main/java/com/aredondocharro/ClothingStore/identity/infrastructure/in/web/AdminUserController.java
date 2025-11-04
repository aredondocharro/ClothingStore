package com.aredondocharro.ClothingStore.identity.infrastructure.in.web;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.AdminSetRolesRequest;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.dto.MessageResponse;
import com.aredondocharro.ClothingStore.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Delete a user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Deleted successfully",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "auth.unauthorized", "message": "Unauthorized" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (ADMIN required)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "auth.forbidden", "message": "Forbidden (ADMIN required)" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "identity.user_not_found", "message": "User not found" }
                    """))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("ADMIN | delete request | userId={}", id);
        deleteUC.delete(UserId.of(id));
        log.info("ADMIN | delete success | userId={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign roles to a user (full replace)",
            description = "Replaces the user's existing roles with the provided set.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminSetRolesRequest.class),
                            examples = @ExampleObject(
                                    name = "set-roles",
                                    value = """
                        { "roles": ["ADMIN", "USER"] }
                        """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Roles updated",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role value / validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "identity.invalid_role", "message": "Invalid role value" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "auth.unauthorized", "message": "Unauthorized" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Policy violation (e.g., self demotion forbidden)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "identity.self_demotion_forbidden", "message": "Self demotion is forbidden" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "identity.user_not_found", "message": "User not found" }
                    """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Policy conflict (e.g., cannot remove the last ADMIN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                    { "code": "identity.cannot_remove_last_admin", "message": "Cannot remove the last ADMIN" }
                    """))
            )
    })
    @PutMapping(path = "/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> setRoles(@PathVariable UUID id,
                                         @Valid @org.springframework.web.bind.annotation.RequestBody AdminSetRolesRequest body) {
        Set<Role> roles;
        try {
            roles = body.roles().stream()
                    .map(s -> Role.valueOf(s.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role value");
        }
        log.info("ADMIN | Change Role request | userId={} | Role ={}", id, roles);
        rolesUC.setRoles(UserId.of(id), roles);
        log.info("ADMIN | Change Role success | userId={} | Role ={}", id, roles);
        return ResponseEntity.accepted()
                .body(new MessageResponse("User " +id +" has role: "+roles));
    }
}
