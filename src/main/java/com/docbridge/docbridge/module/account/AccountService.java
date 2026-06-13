package com.docbridge.docbridge.module.account;

import com.docbridge.docbridge.module.account.dto.*;
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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleSummaryRepository roleSummaryRepository;
    private final InteropUnitSummaryRepository interopUnitSummaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // -------------------------------------------------------------------------
    // UC3.1 — Tạo tài khoản Operator
    // -------------------------------------------------------------------------

    @Transactional
    public CreateAccountResult createOperator(CreateOperatorRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        RoleSummary operatorRole = roleSummaryRepository.findByCode("OPERATOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        String tempPassword = CodeGenerator.generateTempPassword();

        AccountEntity account = AccountEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .roleId(operatorRole.getId())
                .isTempPassword(true)
                .status(AccountStatus.ACTIVE)
                .createdBy(SecurityUtils.getCurrentAccountId())
                .build();

        accountRepository.save(account);

        // Gửi email async — lỗi email không rollback transaction
        emailService.sendTempPassword(account.getEmail(), tempPassword);

        return new CreateAccountResult(account.getId(), account.getEmail(), tempPassword);
    }

    // -------------------------------------------------------------------------
    // UC3.2 — Khoá tài khoản (Operator hoặc Unit)
    // -------------------------------------------------------------------------

    @Transactional
    public AccountResponse lockAccount(Long id) {
        AccountEntity account = findAccountOrThrow(id);
        guardNotAdmin(account);

        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_LOCKED);
        }

        account.setStatus(AccountStatus.LOCKED);
        return toResponse(account);
    }

    // -------------------------------------------------------------------------
    // UC3.2 — Mở khoá tài khoản
    // -------------------------------------------------------------------------

    @Transactional
    public AccountResponse unlockAccount(Long id) {
        AccountEntity account = findAccountOrThrow(id);
        guardNotAdmin(account);

        if (account.getStatus() != AccountStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        account.setStatus(AccountStatus.ACTIVE);
        return toResponse(account);
    }

    // -------------------------------------------------------------------------
    // UC3.3 — Reset mật khẩu
    // -------------------------------------------------------------------------

    @Transactional
    public CreateAccountResult resetPassword(Long id) {
        AccountEntity account = findAccountOrThrow(id);
        guardNotAdmin(account);

        String tempPassword = CodeGenerator.generateTempPassword();
        account.setPassword(passwordEncoder.encode(tempPassword));
        account.setTempPassword(true);

        emailService.sendTempPassword(account.getEmail(), tempPassword);

        return new CreateAccountResult(account.getId(), account.getEmail(), tempPassword);
    }

    // -------------------------------------------------------------------------
    // UC3.4 — Xem danh sách tài khoản
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(AccountFilterRequest filter) {
        String roleCode = (filter.getRole() == null || filter.getRole().isBlank()) ? null : filter.getRole().toUpperCase();

        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return accountRepository
                .findByFilter(roleCode, filter.getStatus(), pageable)
                .map(this::toResponse);
    }

    // -------------------------------------------------------------------------
    // UC3.5 — Xem chi tiết tài khoản
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AccountResponse getAccountDetail(Long id) {
        return toResponse(findAccountOrThrow(id));
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private AccountEntity findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    /**
     * Admin không được khoá/reset chính mình hoặc tài khoản ADMIN khác.
     */
    private void guardNotAdmin(AccountEntity account) {
        RoleSummary role = roleSummaryRepository.findById(account.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if ("ADMIN".equals(role.getCode())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_ADMIN_ACCOUNT);
        }
    }


    private AccountResponse toResponse(AccountEntity account) {
        RoleSummary role = roleSummaryRepository.findById(account.getRoleId())
                .orElse(null);

        AccountResponse.UnitInfo unitInfo = null;
        if (account.getUnitId() != null) {
            unitInfo = interopUnitSummaryRepository.findById(account.getUnitId())
                    .map(u -> AccountResponse.UnitInfo.builder()
                            .unitId(u.getId())
                            .interopCode(u.getInteropCode())
                            .unitName(u.getName())
                            .build())
                    .orElse(null);
        }

        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .roleCode(role != null ? role.getCode() : null)
                .roleName(role != null ? role.getName() : null)
                .status(account.getStatus())
                .isTempPassword(account.isTempPassword())
                .lastLoginAt(account.getLastLoginAt())
                .createdAt(account.getCreatedAt())
                .unitInfo(unitInfo)
                .build();
    }
}
