package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.module.transaction.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 50)
    private String transactionCode;

    @Column(name = "sender_unit_id", nullable = false)
    private Long senderUnitId;

    @Column(name = "receiver_unit_id", nullable = false)
    private Long receiverUnitId;

    @Column(name = "document_code", nullable = false, length = 100)
    private String documentCode;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "file_reference", nullable = false, length = 1000)
    private String fileReference;

    @Column(name = "note", length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
