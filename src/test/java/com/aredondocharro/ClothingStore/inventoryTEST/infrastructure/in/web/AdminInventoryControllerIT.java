package com.aredondocharro.ClothingStore.inventoryTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.repository.InventoryItemJpaRepository;
import com.aredondocharro.ClothingStore.testconfig.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        // --- TEST DB lifecycle ---
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.task.scheduling.enabled=false",

        // --- placeholders que tu application.yml espera (evitamos fallos por ${...} sin default) ---
        "DB_USER=test",
        "DB_PASSWORD=test",

        // Mail (si algo del contexto lo requiere)
        "MAIL_HOST=localhost",
        "MAIL_PORT=2525",
        "MAIL_USERNAME=test",
        "MAIL_PASSWORD=test",
        "MAIL_FROM=no-reply@test.local",
        "MAIL_REPLY_TO=support@test.local",

        // JWT (tu JwtAccessTokenVerifierAdapter exige secret + issuer)
        "JWT_SECRET=test-secret",
        "JWT_ISSUER=ClothingStore",

        // Placeholders Flyway (aunque Flyway esté off, no molestan)
        "APP_ADMIN_ID=00000000-0000-0000-0000-000000000001",
        "APP_ADMIN_EMAIL=admin@test.local",
        "APP_ADMIN_PASSWORD_HASH=$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminInventoryControllerIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    InventoryItemJpaRepository inventoryJpa;

    @Test
    void adminEndpoints_requireAuthentication() throws Exception {
        // Sin auth -> 401
        mvc.perform(post("/admin/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createItemJson("TSHIRT-401")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_requireAdminRole() throws Exception {
        // Auth pero sin ROLE_ADMIN -> 403
        mvc.perform(post("/admin/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createItemJson("TSHIRT-403")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reserve_isIdempotent_and_release_isIdempotent_and_updatesItemReserved() throws Exception {
        // 1) Create item
        var createRes = mvc.perform(post("/admin/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createItemJson("TSHIRT-BASIC-BLK-M")))
                .andExpect(status().isCreated())
                .andReturn();

        UUID itemId = UUID.fromString(JsonPath.read(createRes.getResponse().getContentAsString(), "$.id"));

        // Sanity check: stock inicial
        var createdEntity = inventoryJpa.findById(itemId).orElseThrow();
        assertThat(createdEntity.getStockOnHand()).isEqualTo(10);
        assertThat(createdEntity.getStockReserved()).isEqualTo(0);

        // 2) Reserve stock (qty 3)
        var reserveBody = """
                {"reference":"ORDER-1001","quantity":3}
                """;

        var reserveRes1 = mvc.perform(post("/admin/inventory/items/{id}/reserve", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserveBody))
                .andExpect(status().isOk()) // controller devuelve 200
                .andReturn();

        UUID reservationId1 = UUID.fromString(
                JsonPath.read(reserveRes1.getResponse().getContentAsString(), "$.reservationId")
        );

        // 3) Reserve again same (ref, qty) -> idempotente (misma reserva / no duplica stock)
        var reserveRes2 = mvc.perform(post("/admin/inventory/items/{id}/reserve", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserveBody))
                .andExpect(status().isOk()) // controller devuelve 200 también
                .andReturn();

        UUID reservationId2 = UUID.fromString(
                JsonPath.read(reserveRes2.getResponse().getContentAsString(), "$.reservationId")
        );

        assertThat(reservationId2).isEqualTo(reservationId1);

        var afterReserve = inventoryJpa.findById(itemId).orElseThrow();
        assertThat(afterReserve.getStockOnHand()).isEqualTo(10);
        assertThat(afterReserve.getStockReserved()).isEqualTo(3);

        // 4) Release
        var releaseBody = """
                {"reference":"ORDER-1001"}
                """;

        mvc.perform(post("/admin/inventory/items/{id}/release", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(releaseBody))
                .andExpect(status().isNoContent()); // controller devuelve 204

        var afterRelease = inventoryJpa.findById(itemId).orElseThrow();
        assertThat(afterRelease.getStockReserved()).isEqualTo(0);

        // 5) Release again -> idempotente (no debe fallar, y reserved sigue a 0)
        mvc.perform(post("/admin/inventory/items/{id}/release", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(releaseBody))
                .andExpect(status().isNoContent()); // sigue devolviendo 204

        var afterRelease2 = inventoryJpa.findById(itemId).orElseThrow();
        assertThat(afterRelease2.getStockReserved()).isEqualTo(0);
    }

    private static String createItemJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Basic T-Shirt",
                  "description": "100%% cotton",
                  "category": "TOP",
                  "accessoryType": "NONE",
                  "gender": "UNISEX",
                  "size": "M",
                  "fabric": "COTTON",
                  "color": "BLACK",
                  "priceAmount": 19.99,
                  "currencyCode": "EUR",
                  "initialStock": 10
                }
                """.formatted(sku);
    }
}