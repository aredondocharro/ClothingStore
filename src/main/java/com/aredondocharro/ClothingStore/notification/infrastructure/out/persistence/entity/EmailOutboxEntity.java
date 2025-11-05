package com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;


@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
@Table(name = "email_outbox", indexes = { @Index(name = "idx_email_outbox_due", columnList = "status,next_attempt_at") })
public class EmailOutboxEntity {

    public enum Status { PENDING, PROCESSING, SENT, FAILED }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "to_addresses", nullable = false, columnDefinition = "TEXT")
    private String toAddresses; // comma-separated


    @Column(name = "subject", nullable = false, columnDefinition = "TEXT")
    private String subject;


    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;


    @Column(name = "is_html", nullable = false)
    private boolean html = true;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;


    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;


    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;


    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_attempt_at", nullable = true)
    private Instant nextAttemptAt;

    @Column(name = "sent_at")
    private Instant sentAt;


    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (nextAttemptAt == null) nextAttemptAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public static EmailOutboxEntity pending(String toCsv, String subject, String body, boolean html) {
        return EmailOutboxEntity.builder()
                .toAddresses(toCsv)
                .subject(subject)
                .body(body)
                .html(html)
                .status(Status.PENDING)
                .attemptCount(0)
                .build();
    }

}
