package com.docbridge.docbridge.module.log.stats;

import com.docbridge.docbridge.module.log.stats.dto.TransactionStatsRequest;
import com.docbridge.docbridge.module.log.stats.dto.TransactionStatsResponse;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Thống kê giao dịch")
public class TransactionStatsController {

    private final TransactionStatsService statsService;

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('LOG_STATS')")
    @Operation(summary = "Thống kê giao dịch theo thời gian / hệ thống / đơn vị")
    public ResponseEntity<ApiResponse<TransactionStatsResponse>> getStats(
            TransactionStatsRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(statsService.getStats(req)));
    }
}
