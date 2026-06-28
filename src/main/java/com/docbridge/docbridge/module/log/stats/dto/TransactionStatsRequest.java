package com.docbridge.docbridge.module.log.stats.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionStatsRequest {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long systemId;
    private String groupBy;   // day | month
    private Integer topN;
}
