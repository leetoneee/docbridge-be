package com.docbridge.docbridge.module.system;

import com.docbridge.docbridge.module.log.audit.AuditAction;
import com.docbridge.docbridge.module.log.audit.AuditLogDocument;
import com.docbridge.docbridge.module.log.audit.AuditLogService;
import com.docbridge.docbridge.module.log.audit.AuditTargetType;
import com.docbridge.docbridge.module.system.dto.*;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteropSystemService {

    private final InteropSystemRepository systemRepository;
    private final AuditLogService auditLogService;

    // ----------------------------------------------------------------
    // UC1.1 — Thêm mới hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse create(CreateSystemRequest request, Long actorId) {
        if (systemRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SYSTEM_CODE_DUPLICATED);
        }

        InteropSystemEntity entity = InteropSystemEntity.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .status(InteropSystemStatus.ACTIVE)
                .createdBy(actorId)
                .build();

        entity = systemRepository.save(entity);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(actorId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.CREATE.name())
                .targetType(AuditTargetType.INTEROP_SYSTEM.name())
                .targetId(String.valueOf(entity.getId()))
                .description("Tạo hệ thống liên thông '" + entity.getName() + "'")
                .result("SUCCESS")
                .build());

        return SystemResponse.from(entity);
    }

    // ----------------------------------------------------------------
    // UC1.2 / UC1.7 — Danh sách + tìm kiếm hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<SystemResponse> findAll(SystemFilterRequest filter) {
        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return systemRepository
                .findAllWithFilter(filter.getName(), filter.getStatus(), pageable)
                .map(e -> SystemResponse.from(e, systemRepository.countUnitBySystemId(e.getId())));
    }

    // ----------------------------------------------------------------
    // UC1.3 — Xem chi tiết hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public SystemResponse findById(Long id) {
        InteropSystemEntity entity = getOrThrow(id);
        return SystemResponse.from(entity, systemRepository.countUnitBySystemId(id));
    }

    // ----------------------------------------------------------------
    // UC1.4 — Cập nhật thông tin hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse update(Long id, UpdateSystemRequest request) {
        InteropSystemEntity entity = getOrThrow(id);

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

//        entity = systemRepository.save(entity);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.UPDATE.name())
                .targetType(AuditTargetType.INTEROP_SYSTEM.name())
                .targetId(String.valueOf(id))
                .description("Cập nhật thông tin hệ thống '" + entity.getName() + "'")
                .result("SUCCESS")
                .build());

        return SystemResponse.from(entity);
    }

    // ----------------------------------------------------------------
    // UC1.5 — Khoá / mở khoá hệ thống liên thông
    // Cascade khoá đơn vị xử lý ở tầng runtime giao dịch (không UPDATE interop_unit)
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse toggleLock(Long id) {
        InteropSystemEntity entity = getOrThrow(id);

        boolean locking = entity.getStatus() == InteropSystemStatus.ACTIVE;

        entity.setStatus(
                locking
                        ? InteropSystemStatus.LOCKED :
                        InteropSystemStatus.ACTIVE);
        SystemResponse response = SystemResponse.from(systemRepository.save(entity));

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(locking ? AuditAction.LOCK.name() : AuditAction.UNLOCK.name())
                .targetType(AuditTargetType.INTEROP_SYSTEM.name())
                .targetId(String.valueOf(id))
                .description((locking ? "Khoá" : "Mở khoá")
                        + " hệ thống '" + entity.getName() + "'")
                .result("SUCCESS")
                .build());

        return response;
    }

    // ----------------------------------------------------------------
    // UC1.6 — Xoá hệ thống liên thông (soft delete)
    // Chỉ xoá khi chưa có đơn vị nào
    // ----------------------------------------------------------------
    @Transactional
    public void delete(Long id) {
        InteropSystemEntity entity = getOrThrow(id);
        String systemName = entity.getName();

        if (systemRepository.existsUnitBySystemId(id)) {
            throw new AppException(ErrorCode.SYSTEM_HAS_UNITS);
        }

        systemRepository.deleteById(id);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.DELETE.name())
                .targetType(AuditTargetType.INTEROP_SYSTEM.name())
                .targetId(String.valueOf(id))
                .description("Xoá hệ thống liên thông '" + systemName + "'")
                .result("SUCCESS")
                .build());
    }

    @Transactional(readOnly = true)
    public List<SystemSummaryResponse> findAllForDropdown() {
        return systemRepository.findAllByOrderByNameAsc()
                .stream()
                .map(SystemSummaryResponse::from)
                .toList();
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------
    private InteropSystemEntity getOrThrow(Long id) {
        return systemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_NOT_FOUND));
    }
}
