package com.docbridge.docbridge.module.account;

import com.docbridge.docbridge.module.account.dto.*;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "UC3.1–3.5: Quản lý tài khoản")
public class AccountController {

    private final AccountService accountService;

    // -------------------------------------------------------------------------
    // UC3.1 — Tạo tài khoản Operator
    // POST /api/v1/accounts/operators
    // -------------------------------------------------------------------------

    @PostMapping("/operators")
    @PreAuthorize("hasAuthority('ACCOUNT_CREATE')")
    @Operation(summary = "UC3.1 - Tạo tài khoản Operator")
    public ResponseEntity<ApiResponse<CreateAccountResult>> createOperator(
            @Valid @RequestBody CreateOperatorRequest request) {
        CreateAccountResult result = accountService.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    // -------------------------------------------------------------------------
    // UC3.2 — Khoá tài khoản
    // PUT /api/v1/accounts/{id}/lock
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('ACCOUNT_LOCK')")
    @Operation(summary = "UC3.2 - Khoá tài khoản")
    public ResponseEntity<ApiResponse<AccountResponse>> lockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.lockAccount(id)));
    }

    // -------------------------------------------------------------------------
    // UC3.2 — Mở khoá tài khoản
    // PUT /api/v1/accounts/{id}/unlock
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('ACCOUNT_LOCK')")
    @Operation(summary = "UC3.2 - Mở khoá tài khoản")
    public ResponseEntity<ApiResponse<AccountResponse>> unlockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.unlockAccount(id)));
    }

    // -------------------------------------------------------------------------
    // UC3.3 — Reset mật khẩu
    // PUT /api/v1/accounts/{id}/reset-password
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ACCOUNT_RESET_PASSWORD')")
    @Operation(summary = "UC3.3 - Reset mật khẩu tài khoản")
    public ResponseEntity<ApiResponse<CreateAccountResult>> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.resetPassword(id)));
    }

    // -------------------------------------------------------------------------
    // UC3.4 — Xem danh sách tài khoản
    // GET /api/v1/accounts?role=OPERATOR&status=ACTIVE&page=0&size=20
    // -------------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "UC3.4 - Xem danh sách tài khoản")
    public ResponseEntity<ApiResponse<ApiResponse.PageData<AccountResponse>>> getAccounts(
            @ModelAttribute AccountFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.PageData.of(accountService.getAccounts(filter))));
    }

    // -------------------------------------------------------------------------
    // UC3.5 — Xem chi tiết tài khoản
    // GET /api/v1/accounts/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "UC3.5 - Xem chi tiết tài khoản")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountDetail(id)));
    }
}
