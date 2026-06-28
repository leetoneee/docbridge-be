package com.docbridge.docbridge.module.log.audit;

import com.docbridge.docbridge.module.log.audit.dto.AuditLogFilterRequest;
import com.docbridge.docbridge.module.log.audit.dto.AuditLogResponse;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Nhật ký hoạt động hệ thống")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // UC7.1 + UC7.2
    @GetMapping
    @PreAuthorize("hasAuthority('LOG_VIEW')")
    @Operation(summary = "Danh sách log (search_after pagination)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            AuditLogFilterRequest filter) {
        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.search(filter)));
    }

    // UC7.3
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOG_VIEW')")
    @Operation(summary = "Chi tiết log")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.getById(id)));
    }
}
