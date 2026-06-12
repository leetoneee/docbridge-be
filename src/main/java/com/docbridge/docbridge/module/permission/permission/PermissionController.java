package com.docbridge.docbridge.module.permission.permission;


import com.docbridge.docbridge.module.permission.permission.dto.PermissionResponse;
import com.docbridge.docbridge.module.permission.role.dto.RoleDetailResponse;
import com.docbridge.docbridge.module.permission.role.dto.RoleResponse;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "UC8.1–8.4: Quản lý phân quyền role")
public class PermissionController {

    private final PermissionService permissionService;

    // -------------------------------------------------------------------------
    // UC8.1 — Xem danh sách role
    // GET /api/v1/roles
    // -------------------------------------------------------------------------

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "UC8.1 - Xem danh sách role")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = permissionService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    // -------------------------------------------------------------------------
    // UC8.2 — Xem chi tiết role và permission
    // GET /api/v1/roles/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "UC8.2 - Xem chi tiết role và danh sách permission đã gán")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleDetail(@PathVariable Long id) {
        RoleDetailResponse detail = permissionService.getRoleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // -------------------------------------------------------------------------
    // Xem toàn bộ permissions có trong hệ thống (hỗ trợ UC8.3 — Admin cần
    // danh sách để chọn permission gán vào role)
    // GET /api/v1/permissions
    // -------------------------------------------------------------------------

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "Xem toàn bộ permissions hệ thống (dùng để chọn khi gán)")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    // -------------------------------------------------------------------------
    // UC8.3 — Gán permission vào role
    // POST /api/v1/roles/{id}/permissions/{permissionId}
    // -------------------------------------------------------------------------

    @PostMapping("/roles/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "UC8.3 - Gán permission vào role")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> assignPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        RoleDetailResponse result = permissionService.assignPermission(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // -------------------------------------------------------------------------
    // UC8.4 — Bỏ permission khỏi role
    // DELETE /api/v1/roles/{id}/permissions/{permissionId}
    // -------------------------------------------------------------------------

    @DeleteMapping("/roles/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "UC8.4 - Bỏ permission khỏi role")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> removePermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        RoleDetailResponse result = permissionService.removePermission(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
