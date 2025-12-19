package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.InventorySearchQuery;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.Page;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.PageRequest;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.view.InventoryItemSummaryView;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity.InventoryItemEntity;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.mapper.InventoryPersistenceMapper;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.repository.InventoryItemJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryItemRepositoryAdapter implements InventoryItemRepositoryPort {

    private final InventoryItemJpaRepository jpaRepo;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<InventoryItem> findById(InventoryItemId id) {
        return jpaRepo.findById(id.getValue()).map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<InventoryItem> findBySku(Sku sku) {
        return jpaRepo.findBySku(sku.getValue()).map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void save(InventoryItem item) {
        InventoryItemEntity entity = InventoryPersistenceMapper.toEntity(item);
        jpaRepo.save(entity);
    }

    /**
     * Search realista para portfolio:
     * - Filtros opcionales
     * - Text search en sku o name
     * - Offset/limit (no "page number") para encajar con vuestro PageRequest
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<InventoryItemSummaryView> search(InventorySearchQuery query, PageRequest pageRequest) {
        if (pageRequest == null) throw new IllegalArgumentException("pageRequest is required");
        return searchAsTuple(query, pageRequest);
    }

    private Page<InventoryItemSummaryView> searchAsTuple(InventorySearchQuery query, PageRequest pageRequest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ---------- Items query ----------
        CriteriaQuery<Tuple> itemsCq = cb.createTupleQuery();
        Root<InventoryItemEntity> root = itemsCq.from(InventoryItemEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, query);

        itemsCq.multiselect(
                root.get("id").alias("id"),
                root.get("sku").alias("sku"),
                root.get("name").alias("name"),
                root.get("priceAmount").alias("priceAmount"),
                root.get("priceCurrency").alias("priceCurrency"),
                root.get("stockOnHand").alias("stockOnHand"),
                root.get("stockReserved").alias("stockReserved"),
                root.get("status").alias("status")
        );

        if (!predicates.isEmpty()) {
            itemsCq.where(cb.and(predicates.toArray(Predicate[]::new)));
        }

        itemsCq.orderBy(
                cb.desc(root.get("updatedAt")),
                cb.asc(root.get("id"))
        );

        TypedQuery<Tuple> itemsQuery = em.createQuery(itemsCq);
        itemsQuery.setFirstResult(pageRequest.offset());
        itemsQuery.setMaxResults(pageRequest.limit());

        List<Tuple> tuples = itemsQuery.getResultList();
        List<InventoryItemSummaryView> items = tuples.stream()
                .map(this::toSummaryView)
                .toList();

        // ---------- Count query ----------
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<InventoryItemEntity> countRoot = countCq.from(InventoryItemEntity.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, query);

        countCq.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countCq.where(cb.and(countPredicates.toArray(Predicate[]::new)));
        }

        long total = em.createQuery(countCq).getSingleResult();

        return new Page<>(items, total, pageRequest);
    }

    private InventoryItemSummaryView toSummaryView(Tuple t) {
        InventoryItemId id = InventoryItemId.of(t.get("id", java.util.UUID.class));
        Sku sku = Sku.of(t.get("sku", String.class));
        String name = t.get("name", String.class);

        BigDecimal amount = t.get("priceAmount", BigDecimal.class);
        String currencyCode = t.get("priceCurrency", String.class);
        Money price = new Money(amount, Currency.getInstance(currencyCode));

        int onHand = t.get("stockOnHand", Integer.class);
        int reserved = t.get("stockReserved", Integer.class);
        ItemStatus status = t.get("status", ItemStatus.class);

        return new InventoryItemSummaryView(id, sku, name, price, onHand, reserved, status);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<InventoryItemEntity> root, InventorySearchQuery query) {
        List<Predicate> predicates = new ArrayList<>();
        if (query == null) return predicates;

        if (query.text() != null && !query.text().isBlank()) {
            String like = "%" + query.text().trim().toLowerCase() + "%";
            Predicate skuLike = cb.like(cb.lower(root.get("sku")), like);
            Predicate nameLike = cb.like(cb.lower(root.get("name")), like);
            predicates.add(cb.or(skuLike, nameLike));
        }

        if (query.category() != null) predicates.add(cb.equal(root.get("category"), query.category()));
        if (query.gender() != null) predicates.add(cb.equal(root.get("gender"), query.gender()));
        if (query.size() != null) predicates.add(cb.equal(root.get("size"), query.size()));
        if (query.fabric() != null) predicates.add(cb.equal(root.get("fabric"), query.fabric()));
        if (query.status() != null) predicates.add(cb.equal(root.get("status"), query.status()));

        return predicates;
    }
}
