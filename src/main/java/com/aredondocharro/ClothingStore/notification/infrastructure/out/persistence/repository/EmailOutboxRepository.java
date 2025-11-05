package com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository;


import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.Instant;
import java.util.List;


public interface EmailOutboxRepository extends JpaRepository<EmailOutboxEntity, Long> {
    List<EmailOutboxEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            EmailOutboxEntity.Status status,
            Instant now,
            Pageable pageable
    );
}