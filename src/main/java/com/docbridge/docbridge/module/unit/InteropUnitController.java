package com.docbridge.docbridge.module.unit;

import com.docbridge.docbridge.module.unit.dto.*;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Tag(name = "Interop Unit", description = "Quản lý đơn vị liên thông")
public class InteropUnitController {

    private final InteropUnitService unitService;

    // ── UC2.1 — Tạo đơn vị ──────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('UNIT_CREATE')")
    @Operation(summary = "Tạo đơn vị liên thông",
               description = "Operator tạo đơn vị mới, status = PENDING chờ Admin phê duyệt")
    public ResponseEntity<ApiResponse<UnitResponse>> create(
            @Valid @RequestBody CreateUnitRequest request) {

        UnitResponse result = unitService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn vị thành công", result));
    }

    // ── UC2.2 / UC2.8 — Danh sách + tìm kiếm ───────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('UNIT_VIEW')")
    @Operation(summary = "Danh sách đơn vị liên thông",
               description = "Lọc theo hệ thống, trạng thái; tìm theo tên hoặc mã liên thông")
    public ResponseEntity<ApiResponse<ApiResponse.PageData<UnitResponse>>> list(
            @ModelAttribute UnitFilterRequest request) {

        return ResponseEntity.ok(ApiResponse.success(unitService.list(request)));
    }

    // ── UC2.3 — Chi tiết ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_VIEW')")
    @Operation(summary = "Chi tiết đơn vị liên thông",
               description = "Bao gồm thông tin tài khoản Unit đại diện nếu đã phê duyệt")
    public ResponseEntity<ApiResponse<UnitDetailResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(unitService.detail(id)));
    }

    // ── UC2.4 — Cập nhật ────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_UPDATE')")
    @Operation(summary = "Cập nhật đơn vị liên thông",
               description = "Chỉ cho phép sửa: name, description, representativeName, representativePhone. " +
                             "Không thể sửa email (nếu account đã ACTIVE), systemId, interopCode")
    public ResponseEntity<ApiResponse<UnitResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUnitRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật đơn vị thành công", unitService.update(id, request)));
    }

    // ── UC2.5 — Phê duyệt ───────────────────────────────────────────────────
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UNIT_APPROVE')")
    @Operation(summary = "Phê duyệt đơn vị liên thông",
               description = "Admin phê duyệt: sinh mã liên thông, tạo tài khoản Unit, sinh mật khẩu tạm thời. " +
                             "Kết quả trả về 1 lần duy nhất — Admin cần ghi lại mật khẩu tạm thời")
    public ResponseEntity<ApiResponse<ApproveUnitResult>> approve(@PathVariable Long id) {
        ApproveUnitResult result = unitService.approve(id);
        return ResponseEntity.ok(
                ApiResponse.success("Phê duyệt đơn vị thành công. Vui lòng lưu mật khẩu tạm thời", result));
    }

    // ── UC2.5 — Từ chối ─────────────────────────────────────────────────────
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UNIT_APPROVE')")
    @Operation(summary = "Từ chối đơn vị liên thông",
               description = "Admin từ chối kèm lý do. Record giữ nguyên để audit. " +
                             "Operator cần tạo đơn vị mới nếu muốn submit lại")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectUnitRequest request) {

        unitService.reject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối đơn vị"));
    }

    // ── UC2.6 — Khoá / mở khoá ──────────────────────────────────────────────
    @PostMapping("/{id}/toggle-lock")
    @PreAuthorize("hasAuthority('UNIT_LOCK')")
    @Operation(summary = "Khoá / mở khoá đơn vị liên thông",
               description = "ACTIVE → LOCKED: tài khoản Unit vẫn đăng nhập được nhưng không tạo giao dịch. " +
                             "LOCKED → ACTIVE: khôi phục hoạt động bình thường")
    public ResponseEntity<ApiResponse<Void>> toggleLock(@PathVariable Long id) {
        unitService.toggleLock(id);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn vị thành công"));
    }

    // ── UC2.7 — Xoá ─────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_DELETE')")
    @Operation(summary = "Xoá đơn vị liên thông",
               description = "Chỉ xoá được khi đơn vị chưa có giao dịch nào. " +
                             "Xoá cả tài khoản Unit đi kèm (nếu đã tạo)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá đơn vị thành công"));
    }
}
