package com.docbridge.docbridge.module.permission.permission;

import com.docbridge.docbridge.module.log.audit.AuditAction;
import com.docbridge.docbridge.module.log.audit.AuditLogDocument;
import com.docbridge.docbridge.module.log.audit.AuditLogService;
import com.docbridge.docbridge.module.log.audit.AuditTargetType;
import com.docbridge.docbridge.module.permission.permission.dto.PermissionResponse;
import com.docbridge.docbridge.module.permission.role.RoleEntity;
import com.docbridge.docbridge.module.permission.role.RoleRepository;
import com.docbridge.docbridge.module.permission.role.dto.RoleDetailResponse;
import com.docbridge.docbridge.module.permission.role.dto.RoleResponse;
import com.docbridge.docbridge.module.permission.rolepermission.RolePermissionEntity;
import com.docbridge.docbridge.module.permission.rolepermission.RolePermissionRepository;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuditLogService auditLogService;
    // -------------------------------------------------------------------------
    // UC8.1 — Xem danh sách role
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        // findAll() không fetch rolePermissions → N+1 nếu dùng size()
        // Dùng findByIdWithPermissions cho detail, còn list dùng query count riêng
        return roleRepository.findAll().stream()
                .map(role -> {
                    // rolePermissions là LAZY — trigger load để lấy size
                    // OK vì số role rất nhỏ (3 role), không dùng batch fetch cũng được
                    RoleEntity loaded = roleRepository.findByIdWithPermissions(role.getId())
                            .orElse(role);
                    return RoleResponse.from(loaded);
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // UC8.2 — Xem chi tiết role và permission
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleDetail(Long roleId) {
        RoleEntity role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Set<Long> assignedIds = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        Map<String, List<PermissionResponse>> grouped = permissionRepository
                .findAllByOrderByGroupNameAscCodeAsc().stream()
                .map(p -> PermissionResponse.from(p, assignedIds.contains(p.getId())))
                .collect(Collectors.groupingBy(PermissionResponse::getGroupName));


        return RoleDetailResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .permissionsByGroup(grouped)
                .build();
    }

    // -------------------------------------------------------------------------
    // Xem toàn bộ permissions có trong hệ thống (để Admin chọn khi gán)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAllByOrderByGroupNameAscCodeAsc().stream()
                .map(PermissionResponse::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // UC8.3 — Gán permission vào role
    // -------------------------------------------------------------------------

    @Transactional
    public RoleDetailResponse assignPermission(Long roleId, Long permissionId) {
        try {
            RoleEntity role = roleRepository.findByIdWithPermissions(roleId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            PermissionEntity permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

            if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                throw new AppException(ErrorCode.PERMISSION_ALREADY_ASSIGNED);
            }

            RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                    .role(role)
                    .permission(permission)
                    .createdAt(LocalDateTime.now())
                    .build();

            rolePermissionRepository.save(rolePermission);

            // Reload để trả về state mới nhất
            RoleEntity updated = roleRepository.findByIdWithPermissions(roleId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.GRANT_PERMISSION.name())
                    .targetType(AuditTargetType.ROLE.name())
                    .targetId(String.valueOf(roleId))
                    .description("Gán quyền '" + permission.getCode()
                            + "' cho role " + role.getCode())
                    .result("SUCCESS")
                    .build());

            return RoleDetailResponse.from(updated);

        } catch (AppException ex) {
            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.GRANT_PERMISSION.name())
                    .targetType(AuditTargetType.ROLE.name())
                    .targetId(String.valueOf(roleId))
                    .description("Gán quyền thất bại")
                    .result("FAILURE")
                    .failureReason(ex.getErrorCode().toFailureReason())
                    .build());
            throw ex;
        }
    }

    // -------------------------------------------------------------------------
    // UC8.4 — Bỏ permission khỏi role
    // -------------------------------------------------------------------------

    @Transactional
    public RoleDetailResponse removePermission(Long roleId, Long permissionId) {
        try {
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            PermissionEntity permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

            if ("ROLE_MANAGE".equals(permission.getCode()) && "ADMIN".equals(role.getCode())) {
                throw new AppException(ErrorCode.PERMISSION_PROTECTED);
            }

            int deleted = rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);

            if (deleted == 0) {
                throw new AppException(ErrorCode.PERMISSION_NOT_ASSIGNED);
            }

            RoleEntity updated = roleRepository.findByIdWithPermissions(roleId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.REVOKE_PERMISSION.name())
                    .targetType(AuditTargetType.ROLE.name())
                    .targetId(String.valueOf(roleId))
                    .description("Bỏ quyền '" + permission.getCode()
                            + "' khỏi role " + role.getCode())
                    .result("SUCCESS")
                    .build());
            
            return RoleDetailResponse.from(updated);

        } catch (AppException ex) {
            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.REVOKE_PERMISSION.name())
                    .targetType(AuditTargetType.ROLE.name())
                    .targetId(String.valueOf(roleId))
                    .description("Bỏ quyền thất bại")
                    .result("FAILURE")
                    .failureReason(ex.getErrorCode().toFailureReason())
                    .build());
            throw ex;
        }
    }
}
