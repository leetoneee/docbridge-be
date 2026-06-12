package com.docbridge.docbridge.module.auth.filter;

import com.docbridge.docbridge.module.auth.AccountPrincipal;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Chặn mọi request (trừ /auth/**) nếu tài khoản đang dùng mật khẩu tạm thời.
 * Phải chạy sau JwtAuthFilter.
 *
 * Response khi bị chặn:
 * HTTP 403  { "code": "MUST_CHANGE_PASSWORD", "message": "..." }
 */
@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    // Các path được phép gọi kể cả khi chưa đổi mật khẩu
    private static final String[] ALLOWED_PATHS = {
            "/api/auth/",
            "/swagger-ui",
            "/v3/api-docs"
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (isAllowedPath(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof AccountPrincipal principal
                && principal.isTempPassword()) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            var body = Map.of(
                    "code",    ErrorCode.MUST_CHANGE_PASSWORD.getCode(),
                    "message", ErrorCode.MUST_CHANGE_PASSWORD.getDefaultMessage()
            );
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowedPath(String uri) {
        for (String path : ALLOWED_PATHS) {
            if (uri.startsWith(path)) return true;
        }
        return false;
    }
}
