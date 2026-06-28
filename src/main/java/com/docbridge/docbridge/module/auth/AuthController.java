package com.docbridge.docbridge.module.auth;

import com.docbridge.docbridge.module.auth.dto.ChangePasswordRequest;
import com.docbridge.docbridge.module.auth.dto.FirstLoginChangePasswordRequest;
import com.docbridge.docbridge.module.auth.dto.LoginRequest;
import com.docbridge.docbridge.module.auth.dto.LoginResponse;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng nhập và quản lý mật khẩu")
public class AuthController {

    private final AuthService authService;

    // ----------------------------------------------------------------
    // UC4.1 — Đăng nhập
    // POST /api/auth/login  → public
    // ----------------------------------------------------------------
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Trả về JWT token. Nếu mustChangePassword=true, redirect đến /auth/first-change-password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        LoginResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    // UC4.2 — Đăng xuất
    // POST /api/auth/logout  → cần token
    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    // ----------------------------------------------------------------
    // UC4.3 — Đổi mật khẩu lần đầu (is_temp_password = true)
    // POST /api/auth/first-change-password  → cần token, chưa cần đổi mật khẩu
    // ----------------------------------------------------------------
    @PostMapping("/first-change-password")
    @Operation(summary = "Đổi mật khẩu lần đầu",
            description = "Bắt buộc khi mustChangePassword=true. Sau khi thành công, token hiện tại hết hiệu lực — client phải đăng nhập lại")
    public ResponseEntity<ApiResponse<Void>> firstChangePassword(
            @Valid @RequestBody FirstLoginChangePasswordRequest request) {

        authService.firstLoginChangePassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại"));
    }

    // ----------------------------------------------------------------
    // UC4.4 — Đổi mật khẩu thông thường
    // POST /api/auth/change-password  → cần token
    // ----------------------------------------------------------------
    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }
}
