package com.docbridge.docbridge.module.log.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TransactionStatsResponse {
    private Overview overview;
    private List<SystemStat> bySystem;
    private List<UnitStat> byUnit;
    private List<TimelineStat> timeline;
    private List<RecentTransaction> recentTransactions;

    @Getter
    @AllArgsConstructor
    public static class Overview {
        private long total;
        private long sent;
        private long accepted;
        private long rejected;
        private long cancelled;
        private long totalActiveUnits;
        private long pendingUnits;
    }

    @Getter
    @AllArgsConstructor
    public static class SystemStat {
        private long systemId;
        private String systemCode;
        private String systemName;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class UnitStat {
        private long unitId;
        private String interopCode;
        private String unitName;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class TimelineStat {
        private String period;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class RecentTransaction {
        private String        transactionCode;
        private String        fromUnit;   // interop_code của sender
        private String        toUnit;     // interop_code của receiver
        private String        status;
        private LocalDateTime createdAt;
    }
}
