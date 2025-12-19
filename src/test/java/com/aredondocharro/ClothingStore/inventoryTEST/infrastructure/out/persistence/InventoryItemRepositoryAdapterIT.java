package com.aredondocharro.ClothingStore.inventoryTEST.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.InventorySearchQuery;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.PageRequest;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.InventoryItemRepositoryAdapter;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.StockReservationRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        InventoryItemRepositoryAdapter.class,
        StockReservationRepositoryAdapter.class
})
class InventoryItemRepositoryAdapterIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventory_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // Para IT de repos: dejamos que Hibernate cree tablas
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Evitar que Flyway intente correr migraciones si aún no las tienes
        r.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    InventoryItemRepositoryAdapter adapter;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_withoutTransaction_throwsBecausePropagationMandatory() {
        InventoryItem item = newItem("TSHIRT-001", "T-Shirt", Instant.parse("2025-01-01T00:00:00Z"));
        assertThrows(IllegalTransactionStateException.class, () -> adapter.save(item));
    }

    @Test
    void save_and_findById_and_findBySku_workInsideTx() {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        InventoryItem item = newItem("HOODIE-001", "Hoodie", now);

        adapter.save(item);

        var byId = adapter.findById(item.id());
        assertTrue(byId.isPresent());
        assertEquals("HOODIE-001", byId.get().sku().getValue());

        var bySku = adapter.findBySku(Sku.of("HOODIE-001"));
        assertTrue(bySku.isPresent());
        assertEquals(item.id(), bySku.get().id());
    }

    @Test
    void search_filtersByText_and_paginates() {
        adapter.save(newItem("TSHIRT-RED-M", "Red T-Shirt", Instant.parse("2025-01-01T00:00:00Z")));
        adapter.save(newItem("TSHIRT-BLUE-M", "Blue T-Shirt", Instant.parse("2025-01-01T00:00:10Z")));
        adapter.save(newItem("HOODIE-BLK-L", "Black Hoodie", Instant.parse("2025-01-01T00:00:20Z")));

        // Busca "tshirt" (case-insensitive, contains) y pagina 1 item
        var query = new InventorySearchQuery("tshirt", null, null, null, null, null);
        var page1 = adapter.search(query, PageRequest.of(0, 1));

        assertEquals(1, page1.items().size());
        assertEquals(2, page1.total());

        // Siguiente página
        var page2 = adapter.search(query, PageRequest.of(1, 1));
        assertEquals(1, page2.items().size());
        assertEquals(2, page2.total());

        // Busca "hoodie"
        var hoodie = adapter.search(new InventorySearchQuery("hoodie", null, null, null, null, null), PageRequest.of(0, 10));
        assertEquals(1, hoodie.items().size());
        assertEquals("HOODIE-BLK-L", hoodie.items().get(0).sku().getValue());
    }

    private InventoryItem newItem(String sku, String name, Instant now) {
        return InventoryItem.createNew(
                null,
                Sku.of(sku),
                ItemName.of(name),
                null,
                InventoryCategory.TOP,
                AccessoryType.NONE,
                Gender.UNISEX,
                Size.M,
                Fabric.COTTON,
                Color.of("Black"),
                Money.of(new BigDecimal("19.99"), "EUR"),
                10,
                now
        );
    }
}
