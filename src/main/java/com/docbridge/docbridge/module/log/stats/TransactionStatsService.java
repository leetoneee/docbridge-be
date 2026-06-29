package com.docbridge.docbridge.module.log.stats;

import com.docbridge.docbridge.module.log.stats.dto.TransactionStatsRequest;
import com.docbridge.docbridge.module.log.stats.dto.TransactionStatsResponse;
import com.docbridge.docbridge.module.transaction.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionStatsService {
    private final TransactionRepository transactionRepository;

    public TransactionStatsResponse getStats(TransactionStatsRequest req) {
        LocalDate from = req.getDateFrom();
        LocalDate to = req.getDateTo();
        Long systemId = req.getSystemId();

        // Tổng quan
        List<Object[]> overview =
                transactionRepository.countByStatus(from, to, systemId);

        // KPI: đơn vị
        long totalActiveUnits = transactionRepository.countActiveUnits(systemId);
        long pendingUnits     = transactionRepository.countPendingUnits();

        // Theo hệ thống
        List<Object[]> bySystem =
                transactionRepository.countBySystem(from, to);

        // Top đơn vị
        List<Object[]> byUnit =
                transactionRepository.countByUnit(from, to, systemId,
                        req.getTopN() != null ? req.getTopN() : 10);

        // Theo thời gian
        String groupBy = req.getGroupBy() != null ? req.getGroupBy() : "day";
        List<Object[]> timeline = switch (groupBy) {
            case "month" -> transactionRepository.countByMonth(from, to, systemId);
            default -> transactionRepository.countByDay(from, to, systemId);
        };

        // Giao dịch gần nhất
        List<Object[]> recent = transactionRepository.findRecentTransactions(
                from, to, systemId, 10);

        return TransactionStatsResponse.builder()
                .overview(mapOverview(overview, totalActiveUnits, pendingUnits))
                .bySystem(mapBySystem(bySystem))
                .byUnit(mapByUnit(byUnit))
                .timeline(mapTimeline(timeline))
                .recentTransactions(mapRecentTransactions(recent))
                .build();
    }

    private TransactionStatsResponse.Overview mapOverview(List<Object[]> rows, long totalActiveUnits, long pendingUnits) {
        long total = 0, sent = 0, accepted = 0, rejected = 0, cancelled = 0;
        for (Object[] r : rows) {
            String status = (String) r[0];
            long count = ((Number) r[1]).longValue();
            total += count;
            switch (status) {
                case "SENT"      -> sent      = count;
                case "ACCEPTED"  -> accepted  = count;
                case "REJECTED"  -> rejected  = count;
                case "CANCELLED" -> cancelled = count;
            }
        }
        return new TransactionStatsResponse.Overview(
                total, sent, accepted, rejected, cancelled, totalActiveUnits, pendingUnits);
    }

    private List<TransactionStatsResponse.SystemStat> mapBySystem(List<Object[]> rows) {
        return rows.stream().map(r -> new TransactionStatsResponse.SystemStat(
                ((Number) r[0]).longValue(),   // systemId
                r[1].toString(),                 // systemCode
                r[2].toString(),                 // systemName
                ((Number) r[3]).longValue()    // count
        )).toList();
    }

    private List<TransactionStatsResponse.UnitStat> mapByUnit(List<Object[]> rows) {
        return rows.stream().map(r -> new TransactionStatsResponse.UnitStat(
                ((Number) r[0]).longValue(),   // unitId
                r[1].toString(),                 // interopCode
                r[2].toString(),                 // unitName
                ((Number) r[3]).longValue()    // count
        )).toList();
    }

    private List<TransactionStatsResponse.TimelineStat> mapTimeline(List<Object[]> rows) {
        return rows.stream().map(r -> new TransactionStatsResponse.TimelineStat(
                r[0].toString(),                 // period label
                ((Number) r[1]).longValue()    // count
        )).toList();
    }

    private List<TransactionStatsResponse.RecentTransaction> mapRecentTransactions(
            List<Object[]> rows) {
        return rows.stream().map(r -> new TransactionStatsResponse.RecentTransaction(
                r[0].toString(),                          // transaction_code
                r[1].toString(),                          // from_unit (interop_code)
                r[2].toString(),                          // to_unit   (interop_code)
                r[3].toString(),                          // status
                ((java.sql.Timestamp) r[4]).toLocalDateTime() // created_at
        )).toList();
    }
}
