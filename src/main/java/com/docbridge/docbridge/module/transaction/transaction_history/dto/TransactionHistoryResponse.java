package com.docbridge.docbridge.module.transaction.transaction_history.dto;

import com.docbridge.docbridge.module.transaction.transaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TransactionHistoryResponse {
    private TransactionStatus fromStatus;
    private TransactionStatus toStatus;
    private String            reason;
    private Long              actedBy;
    private LocalDateTime     actedAt;
}
