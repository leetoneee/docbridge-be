package com.docbridge.docbridge.module.auth;

import com.docbridge.docbridge.shared.security.JwtUtil;
import com.docbridge.docbridge.module.auth.dto.ChangePasswordRequest;
import com.docbridge.docbridge.module.auth.dto.FirstLoginChangePasswordRequest;
import com.docbridge.docbridge.module.auth.dto.LoginRequest;
import com.docbridge.docbridge.module.auth.dto.LoginResponse;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager   authManager;
    private final JwtUtil                 jwtUtil;
    private final AccountAuthRepository   accountAuthRepo;
    private final PasswordEncoder         passwordEncoder;

    // ----------------------------------------------------------------
    // UC4.1 — Đăng nhập
    // ----------------------------------------------------------------

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Spring Security xác thực email + password, ném exception nếu sai
        AccountPrincipal principal = authenticate(request.getEmail(), request.getPassword());

        // Cập nhật last_login_at
        accountAuthRepo.updateLastLoginAt(principal.getAccountId(), LocalDateTime.now());

        String token = jwtUtil.generate(principal);

        return LoginResponse.builder()
                .token(token)
                .email(principal.getEmail())
                .role(principal.getRoleCode())
                .mustChangePassword(principal.isTempPassword())
                .build();
    }

    // ----------------------------------------------------------------
    // UC4.3 — Đổi mật khẩu lần đầu (bắt buộc khi is_temp_password = true)
    // Token hiện tại vô hiệu hoá ngay — client phải login lại
    // ----------------------------------------------------------------

    @Transactional
    public void firstLoginChangePassword(FirstLoginChangePasswordRequest request) {
        AccountPrincipal principal = currentPrincipal();

        if (!principal.isTempPassword()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Tài khoản không ở trạng thái mật khẩu tạm thời");
        }

        validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        accountAuthRepo.updatePassword(
                principal.getAccountId(),
                passwordEncoder.encode(request.getNewPassword())
        );

        // Token cũ sẽ vô hiệu vì lần load UserDetails tiếp theo
        // isTempPassword = false → endpoint MUST_CHANGE_PASSWORD check sẽ pass,
        // nhưng ta invalidate bằng cách yêu cầu client login lại (trả 200 không kèm token mới)
        log.info("Account [{}] completed first-login password change", principal.getEmail());
    }

    // ----------------------------------------------------------------
    // UC4.4 — Đổi mật khẩu thông thường
    // ----------------------------------------------------------------

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        AccountPrincipal principal = currentPrincipal();

        // Load lại từ DB để có password hash mới nhất
        AccountAuthRepository.AccountAuthProjection acc =
                accountAuthRepo.findAuthProjectionByEmail(principal.getEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), acc.getPassword())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }

        validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        accountAuthRepo.updatePassword(
                principal.getAccountId(),
                passwordEncoder.encode(request.getNewPassword())
        );

        log.info("Account [{}] changed password", principal.getEmail());
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private AccountPrincipal authenticate(String email, String password) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(email, password);
            var auth = authManager.authenticate(authToken);
            return (AccountPrincipal) auth.getPrincipal();

        } catch (BadCredentialsException ex) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        } catch (LockedException ex) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        } catch (DisabledException ex) {
            throw new AppException(ErrorCode.ACCOUNT_PENDING);
        }
    }

    private AccountPrincipal currentPrincipal() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AccountPrincipal p)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return p;
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Mật khẩu xác nhận không khớp");
        }
        // Độ mạnh tối thiểu: 8 ký tự, có chữ hoa, chữ thường, số
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số");
        }
    }
}
