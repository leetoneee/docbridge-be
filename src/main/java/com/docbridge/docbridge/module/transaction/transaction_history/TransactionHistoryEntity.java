package com.docbridge.docbridge.module.transaction.transaction_history;

import com.docbridge.docbridge.module.transaction.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private TransactionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private TransactionStatus toStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "acted_by", nullable = false)
    private Long actedBy;

    @Column(name = "acted_at", nullable = false)
    private LocalDateTime actedAt;
}
