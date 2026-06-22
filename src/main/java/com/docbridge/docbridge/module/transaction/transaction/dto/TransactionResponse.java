package com.docbridge.docbridge.module.transaction.transaction.dto;

import com.docbridge.docbridge.module.transaction.transaction.TransactionStatus;
import com.docbridge.docbridge.module.transaction.transaction_history.dto.TransactionHistoryResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long              id;
    private String            transactionCode;
    private UnitBriefResponse sender;
    private UnitBriefResponse receiver;
    private String            documentCode;
    private String            title;
    private String            fileReference;
    private String            note;
    private TransactionStatus status;
    private Integer           version;
    private LocalDateTime     createdAt;
    private LocalDateTime     updatedAt;

    /** Chỉ có trong detail response (UC5.3, UC6.2). Null trong list. */
    private List<TransactionHistoryResponse> history;
}
