package com.docbridge.docbridge.shared.security;

import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tiện ích lấy thông tin tài khoản đang đăng nhập từ SecurityContext.
 *
 * Principal trong DocBridge là AccountPrincipal (sẽ implement ở module auth).
 * Dùng:
 *   Long actorId    = SecurityUtils.getCurrentAccountId();
 *   String email    = SecurityUtils.getCurrentEmail();
 *   boolean isAdmin = SecurityUtils.hasRole("ADMIN");
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Lấy account id của người đang đăng nhập.
     * Ném UNAUTHORIZED nếu chưa xác thực.
     */
    public static Long getCurrentAccountId() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // AccountPrincipal implement ở module auth, có method getId()
        // Cast về AccountPrincipal để lấy id
        if (auth.getPrincipal() instanceof AccountPrincipalHolder holder) {
            return holder.getAccountId();
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public static String getCurrentEmail() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return auth.getName(); // UserDetails.getUsername() = email
    }

    public static boolean hasRole(String roleCode) {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleCode));
    }

    /**
     * Interface marker để module auth implement.
     * Tránh circular dependency giữa shared/kernel và module/auth.
     */
    public interface AccountPrincipalHolder {
        Long getAccountId();
    }
}
