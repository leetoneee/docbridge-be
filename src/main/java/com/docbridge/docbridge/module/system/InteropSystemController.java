package com.docbridge.docbridge.module.system;

import com.docbridge.docbridge.module.system.dto.CreateSystemRequest;
import com.docbridge.docbridge.module.system.dto.SystemFilterRequest;
import com.docbridge.docbridge.module.system.dto.SystemResponse;
import com.docbridge.docbridge.module.system.dto.UpdateSystemRequest;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interop-systems")
@RequiredArgsConstructor
@Tag(name = "Interop System", description = "Quản lý hệ thống liên thông")
public class InteropSystemController {

    private final InteropSystemService systemService;

    // ----------------------------------------------------------------
    // UC1.1 — Thêm mới hệ thống liên thông
    // POST /api/interop-systems
    // ----------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasAuthority('SYSTEM_CREATE')")
    @Operation(summary = "Thêm mới hệ thống liên thông",
               description = "Mã hệ thống: viết hoa, không dấu, không trùng, không thay đổi sau khi đã có đơn vị")
    public ResponseEntity<ApiResponse<SystemResponse>> create(
            @Valid @RequestBody CreateSystemRequest request) {

        Long actorId = SecurityUtils.getCurrentAccountId();
        SystemResponse response = systemService.create(request, actorId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới hệ thống liên thông thành công", response));
    }

    // ----------------------------------------------------------------
    // UC1.2 / UC1.7 — Danh sách + tìm kiếm hệ thống liên thông
    // GET /api/interop-systems?name=&status=&page=0&size=20
    // ----------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_VIEW')")
    @Operation(summary = "Danh sách hệ thống liên thông",
               description = "Hỗ trợ filter theo tên và trạng thái, phân trang")
    public ResponseEntity<ApiResponse<ApiResponse.PageData<SystemResponse>>> findAll(
            SystemFilterRequest filter) {

        Page<SystemResponse> page = systemService.findAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hệ thống liên thông thành công",
                ApiResponse.PageData.of(page)));
    }

    // ----------------------------------------------------------------
    // UC1.3 — Xem chi tiết hệ thống liên thông
    // GET /api/interop-systems/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_VIEW')")
    @Operation(summary = "Chi tiết hệ thống liên thông")
    public ResponseEntity<ApiResponse<SystemResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Lấy chi tiết hệ thống liên thông thành công",
                        systemService.findById(id)));
    }

    // ----------------------------------------------------------------
    // UC1.4 — Cập nhật thông tin hệ thống liên thông
    // PUT /api/interop-systems/{id}
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_UPDATE')")
    @Operation(summary = "Cập nhật hệ thống liên thông",
               description = "Không cho phép thay đổi mã hệ thống")
    public ResponseEntity<ApiResponse<SystemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSystemRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật hệ thống liên thông thành công",
                        systemService.update(id, request)));
    }

    // ----------------------------------------------------------------
    // UC1.5 — Khoá / mở khoá hệ thống liên thông
    // PATCH /api/interop-systems/{id}/lock
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('SYSTEM_LOCK')")
    @Operation(summary = "Khoá / mở khoá hệ thống liên thông",
               description = "Toggle: ACTIVE → LOCKED → ACTIVE. Khi LOCKED, toàn bộ đơn vị bị chặn giao dịch ở runtime")
    public ResponseEntity<ApiResponse<SystemResponse>> toggleLock(@PathVariable Long id) {
        SystemResponse response = systemService.toggleLock(id);
        String message = response.getStatus() == InteropSystemStatus.LOCKED
                ? "Khoá hệ thống liên thông thành công"
                : "Mở khoá hệ thống liên thông thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ----------------------------------------------------------------
    // UC1.6 — Xoá hệ thống liên thông (soft delete)
    // DELETE /api/interop-systems/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_DELETE')")
    @Operation(summary = "Xoá hệ thống liên thông",
               description = "Chỉ xoá được khi chưa có đơn vị nào thuộc hệ thống")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        systemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá hệ thống liên thông thành công"));
    }
}
