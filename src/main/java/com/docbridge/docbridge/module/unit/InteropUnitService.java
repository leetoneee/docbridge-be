package com.docbridge.docbridge.module.unit;

import com.docbridge.docbridge.module.account.AccountEntity;
import com.docbridge.docbridge.module.account.AccountRepository;
import com.docbridge.docbridge.module.account.AccountStatus;
import com.docbridge.docbridge.module.account.RoleSummaryRepository;
import com.docbridge.docbridge.module.log.audit.AuditAction;
import com.docbridge.docbridge.module.log.audit.AuditLogDocument;
import com.docbridge.docbridge.module.log.audit.AuditLogService;
import com.docbridge.docbridge.module.log.audit.AuditTargetType;
import com.docbridge.docbridge.module.permission.role.RoleRepository;
import com.docbridge.docbridge.module.system.InteropSystemEntity;
import com.docbridge.docbridge.module.system.InteropSystemRepository;
import com.docbridge.docbridge.module.system.InteropSystemStatus;
import com.docbridge.docbridge.module.unit.dto.*;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import com.docbridge.docbridge.shared.util.CodeGenerator;
import com.docbridge.docbridge.shared.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteropUnitService {

    private final InteropUnitRepository unitRepository;
    private final InteropSystemRepository systemRepository;
    private final AccountRepository accountRepository;
    private final RoleSummaryRepository roleSummaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // ── UC2.1 — Tạo đơn vị liên thông ──────────────────────────────────────
    @Transactional
    public UnitResponse create(CreateUnitRequest req) {
        Long actorId = SecurityUtils.getCurrentAccountId();

        // Kiểm tra hệ thống tồn tại và đang ACTIVE
        InteropSystemEntity system = systemRepository.findById(req.getSystemId())
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_NOT_FOUND));

        if (system.getStatus() == InteropSystemStatus.LOCKED) {
            throw new AppException(ErrorCode.SYSTEM_LOCKED);
        }

        // Email phải duy nhất toàn bảng interop_unit
        if (unitRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.UNIT_EMAIL_DUPLICATED);
        }

        InteropUnitEntity unit = InteropUnitEntity.builder()
                .system(system)
                .interopCode(null)               // sinh khi phê duyệt
                .name(req.getName())
                .description(req.getDescription())
                .representativeName(req.getRepresentativeName())
                .representativePhone(req.getRepresentativePhone())
                .email(req.getEmail())
                .status(InteropUnitStatus.PENDING)
                .createdBy(actorId)
                .build();

        unitRepository.save(unit);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(actorId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.CREATE.name())
                .targetType(AuditTargetType.INTEROP_UNIT.name())
                .targetId(String.valueOf(unit.getId()))
                .description("Tạo đơn vị liên thông '" + unit.getName() + "'")
                .result("SUCCESS")
                .build());

        return UnitResponse.from(unit);
    }

    // ── UC2.2 / UC2.8 — Danh sách + tìm kiếm ───────────────────────────────
    @Transactional(readOnly = true)
    public Page<UnitResponse> list(UnitFilterRequest req) {
        PageRequest pageable = PageRequest.of(
                req.getPage(), req.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        String kw = (req.getKeyword() != null && req.getKeyword().isBlank())
                ? null : req.getKeyword();

        return unitRepository
                .findByFilter(req.getSystemId(), req.getStatus(), kw, pageable)
                .map(UnitResponse::from);
    }

    // ── UC2.3 — Chi tiết đơn vị ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UnitDetailResponse detail(Long id) {
        InteropUnitEntity unit = findOrThrow(id);

        // Tìm tài khoản Unit nếu đã phê duyệt
        UnitDetailResponse.UnitAccountSummary accountSummary = null;
        if (unit.getStatus() == InteropUnitStatus.ACTIVE
                || unit.getStatus() == InteropUnitStatus.LOCKED) {
            accountSummary = accountRepository.findByEmail(unit.getEmail())
                    .map(a -> UnitDetailResponse.UnitAccountSummary.builder()
                            .accountId(a.getId())
                            .email(a.getEmail())
                            .status(a.getStatus().name())
                            .lastLoginAt(a.getLastLoginAt())
                            .build())
                    .orElse(null);
        }

        // Lấy email admin phê duyệt
        String approvedByEmail = null;
        if (unit.getApprovedBy() != null) {
            approvedByEmail = accountRepository.findById(unit.getApprovedBy())
                    .map(AccountEntity::getEmail)
                    .orElse(null);
        }

        return UnitDetailResponse.builder()
                .id(unit.getId())
                .interopCode(unit.getInteropCode())
                .name(unit.getName())
                .description(unit.getDescription())
                .email(unit.getEmail())
                .representativeName(unit.getRepresentativeName())
                .representativePhone(unit.getRepresentativePhone())
                .status(unit.getStatus())
                .rejectedReason(unit.getRejectedReason())
                .system(UnitResponse.SystemSummary.builder()
                        .id(unit.getSystem().getId())
                        .code(unit.getSystem().getCode())
                        .name(unit.getSystem().getName())
                        .build())
                .unitAccount(accountSummary)
                .approvedBy(approvedByEmail)
                .approvedAt(unit.getApprovedAt())
                .createdAt(unit.getCreatedAt())
                .build();
    }

    // ── UC2.4 — Cập nhật thông tin ──────────────────────────────────────────
    @Transactional
    public UnitResponse update(Long id, UpdateUnitRequest req) {
        InteropUnitEntity unit = findOrThrow(id);

        // Chỉ cho sửa khi PENDING hoặc ACTIVE/LOCKED
        // (REJECTED vẫn để nguyên cho audit — Operator tạo đơn vị mới)
        if (unit.getStatus() == InteropUnitStatus.REJECTED) {
            throw new AppException(ErrorCode.UNIT_CANNOT_UPDATE_REJECTED);
        }

        unit.setName(req.getName());
        unit.setDescription(req.getDescription());
        unit.setRepresentativeName(req.getRepresentativeName());
        unit.setRepresentativePhone(req.getRepresentativePhone());
        // email, system_id, interop_code: KHÔNG cập nhật

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.UPDATE.name())
                .targetType(AuditTargetType.INTEROP_UNIT.name())
                .targetId(String.valueOf(id))
                .description("Cập nhật thông tin đơn vị '" + unit.getName() + "'")
                .result("SUCCESS")
                .build());

        return UnitResponse.from(unit);
    }

    // ── UC2.4 (Admin only) — Đổi email đơn vị ──────────────────────────────
    @Transactional
    public UnitResponse updateEmail(Long id, UpdateUnitEmailRequest req) {
        InteropUnitEntity unit = findOrThrow(id);

        if (unit.getStatus() == InteropUnitStatus.PENDING
                || unit.getStatus() == InteropUnitStatus.REJECTED) {
            throw new AppException(ErrorCode.UNIT_CANNOT_UPDATE_EMAIL);
        }

        String newEmail = req.getEmail();

        // Email mới không được trùng với bất kỳ đơn vị nào khác
        if (unitRepository.existsByEmailAndIdNot(newEmail, id)) {
            throw new AppException(ErrorCode.UNIT_EMAIL_DUPLICATED);
        }

        // Cập nhật interop_unit.email
        unit.setEmail(newEmail);

        // Cập nhật account.email + reset is_temp_password
        String tempPassword = CodeGenerator.generateTempPassword();
        AccountEntity account = accountRepository.findByUnitId(unit.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        account.setEmail(newEmail);
        account.setTempPassword(true);
        account.setPassword(passwordEncoder.encode(tempPassword));

        emailService.sendTempPassword(newEmail, tempPassword);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.UPDATE.name())
                .targetType(AuditTargetType.INTEROP_UNIT.name())
                .targetId(String.valueOf(id))
                .description("Cập nhật email đơn vị '" + unit.getName()
                        + "' thành " + req.getEmail())
                .result("SUCCESS")
                .build());

        return UnitResponse.from(unit);
    }

    // ── UC2.5 — Phê duyệt ───────────────────────────────────────────────────
    @Transactional
    public ApproveUnitResult approve(Long id) {
        Long actorId = SecurityUtils.getCurrentAccountId();

        try {
            InteropUnitEntity unit = findOrThrow(id);

            if (unit.getStatus() != InteropUnitStatus.PENDING) {
                throw new AppException(ErrorCode.UNIT_NOT_PENDING);
            }

            long seq = unitRepository.countActiveBySystemId(unit.getSystem().getId()) + 1;
            String interopCode = CodeGenerator.generateInteropCode(unit.getSystem().getCode(), seq);

            String tempPassword = CodeGenerator.generateTempPassword();

            // Lấy roleId của UNIT từ RoleSummaryRepository (đã có sẵn trong module account)
            Long unitRoleId = roleSummaryRepository.findByCode("UNIT")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND))
                    .getId();

            emailService.sendTempPassword(unit.getEmail(), tempPassword);

            AccountEntity account = AccountEntity.builder()
                    .roleId(unitRoleId)
                    .unitId(unit.getId())
                    .email(unit.getEmail())
                    .password(passwordEncoder.encode(tempPassword))
                    .isTempPassword(true)
                    .status(AccountStatus.ACTIVE)
                    .createdBy(actorId)
                    .build();

            accountRepository.save(account);

            unit.setInteropCode(interopCode);
            unit.setStatus(InteropUnitStatus.ACTIVE);
            unit.setApprovedBy(actorId);
            unit.setApprovedAt(LocalDateTime.now());

            auditLogService.log(AuditLogDocument.builder()
                    .actorId(actorId)
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.APPROVE.name())
                    .targetType(AuditTargetType.INTEROP_UNIT.name())
                    .targetId(String.valueOf(id))
                    .description("Phê duyệt đơn vị '" + unit.getName()
                            + "', mã liên thông " + interopCode)
                    .result("SUCCESS")
                    .build());

            return ApproveUnitResult.builder()
                    .interopCode(interopCode)
                    .tempPassword(tempPassword)
                    .build();

        } catch (AppException ex) {
            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.APPROVE.name())
                    .targetType(AuditTargetType.INTEROP_UNIT.name())
                    .targetId(String.valueOf(id))
                    .description("Phê duyệt đơn vị thất bại")
                    .result("FAILURE")
                    .failureReason(ex.getErrorCode().toFailureReason())
                    .build());
            throw ex;
        }
    }

    // ── UC2.5 — Từ chối ─────────────────────────────────────────────────────
    @Transactional
    public void reject(Long id, RejectUnitRequest req) {
        Long actorId = SecurityUtils.getCurrentAccountId();

        try {
            InteropUnitEntity unit = findOrThrow(id);

            if (unit.getStatus() != InteropUnitStatus.PENDING) {
                throw new AppException(ErrorCode.UNIT_NOT_PENDING);
            }

            unit.setStatus(InteropUnitStatus.REJECTED);
            unit.setRejectedReason(req.getReason());
            unit.setApprovedBy(actorId);       // người thực hiện hành động
            unit.setApprovedAt(LocalDateTime.now());

            auditLogService.log(AuditLogDocument.builder()
                    .actorId(actorId)
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.REJECT.name())
                    .targetType(AuditTargetType.INTEROP_UNIT.name())
                    .targetId(String.valueOf(id))
                    .description("Từ chối đơn vị '" + unit.getName()
                            + "', lý do: " + req.getReason())
                    .result("SUCCESS")
                    .build());

        } catch (AppException ex) {
            auditLogService.log(AuditLogDocument.builder()
                    .actorId(SecurityUtils.getCurrentAccountId())
                    .actorEmail(SecurityUtils.getCurrentEmail())
                    .actorRole(SecurityUtils.getCurrentRole())
                    .action(AuditAction.REJECT.name())
                    .targetType(AuditTargetType.INTEROP_UNIT.name())
                    .targetId(String.valueOf(id))
                    .description("Từ chối đơn vị thất bại")
                    .result("FAILURE")
                    .failureReason(ex.getErrorCode().toFailureReason())
                    .build());
            throw ex;
        }
    }

    // ── UC2.6 — Khoá / mở khoá ──────────────────────────────────────────────
    @Transactional
    public void toggleLock(Long id) {
        InteropUnitEntity unit = findOrThrow(id);

        if (unit.getStatus() == InteropUnitStatus.PENDING
                || unit.getStatus() == InteropUnitStatus.REJECTED) {
            throw new AppException(ErrorCode.UNIT_CANNOT_LOCK);
        }

        boolean locking = unit.getStatus() == InteropUnitStatus.ACTIVE;

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(locking ? AuditAction.LOCK.name() : AuditAction.UNLOCK.name())
                .targetType(AuditTargetType.INTEROP_UNIT.name())
                .targetId(String.valueOf(id))
                .description((locking ? "Khoá" : "Mở khoá")
                        + " đơn vị '" + unit.getName() + "'")
                .result("SUCCESS")
                .build());

        // ACTIVE → LOCKED, LOCKED → ACTIVE
        unit.setStatus(unit.getStatus() == InteropUnitStatus.ACTIVE
                ? InteropUnitStatus.LOCKED
                : InteropUnitStatus.ACTIVE);
    }

    // ── UC2.7 — Xoá ─────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        InteropUnitEntity unit = findOrThrow(id);

        if (unitRepository.hasAnyTransaction(id)) {
            throw new AppException(ErrorCode.UNIT_HAS_TRANSACTIONS);
        }

        // Nếu đã có account Unit → xoá account trước
        accountRepository.findByEmail(unit.getEmail())
                .ifPresent(accountRepository::delete);

        String unitName = unit.getName();

        auditLogService.log(AuditLogDocument.builder()
                .actorId(SecurityUtils.getCurrentAccountId())
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.DELETE.name())
                .targetType(AuditTargetType.INTEROP_UNIT.name())
                .targetId(String.valueOf(id))
                .description("Xoá đơn vị liên thông '" + unitName + "'")
                .result("SUCCESS")
                .build());

        unitRepository.delete(unit);
    }

    // ── Dropdown — danh sách rút gọn, không phân trang ──────────────────────
    @Transactional(readOnly = true)
    public List<UnitOptionResponse> listAll(String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return unitRepository.findAllForDropdown(kw)
                .stream()
                .map(UnitOptionResponse::from)
                .toList();
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private InteropUnitEntity findOrThrow(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNIT_NOT_FOUND));
    }
}
