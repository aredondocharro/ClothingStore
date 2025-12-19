package com.aredondocharro.ClothingStore.inventoryTEST.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
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

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        StockReservationRepositoryAdapter.class
})
class StockReservationRepositoryAdapterIT {

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
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    StockReservationRepositoryAdapter adapter;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_withoutTransaction_throwsBecausePropagationMandatory() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-123");

        StockReservation reservation = StockReservation.createNew(null, itemId, ref, 2, Instant.parse("2025-01-01T00:00:00Z"));
        assertThrows(IllegalTransactionStateException.class, () -> adapter.save(reservation));
    }

    @Test
    void findActiveByItemAndReference_returnsActive() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-123");
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        StockReservation created = StockReservation.createNew(null, itemId, ref, 2, now);
        adapter.save(created);

        Optional<StockReservation> found = adapter.findActiveByItemAndReference(itemId, ref);
        assertTrue(found.isPresent());
        assertEquals(ReservationStatus.ACTIVE, found.get().status());
        assertEquals(2, found.get().quantity());
    }

    @Test
    void findByItemAndReferenceAndStatus_supportsConsumed() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-999");
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        StockReservation created = StockReservation.createNew(null, itemId, ref, 3, now);
        adapter.save(created);

        StockReservation consumed = created.consume(now.plusSeconds(30));
        adapter.save(consumed);

        assertTrue(adapter.findByItemAndReferenceAndStatus(itemId, ref, ReservationStatus.ACTIVE).isEmpty());
        assertTrue(adapter.findByItemAndReferenceAndStatus(itemId, ref, ReservationStatus.CONSUMED).isPresent());
    }
}
