package com.docbridge.docbridge.shared.security;

import com.docbridge.docbridge.module.auth.AccountPrincipal;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Tạo và xác thực JWT token.
 *
 * Payload claims:
 *   sub          — email (standard claim)
 *   accountId    — Long
 *   role         — String: ADMIN / OPERATOR / UNIT
 *   unitId       — Long, chỉ có nếu role = UNIT
 *   permissions  — List<String>
 *   tempPwd      — boolean, true nếu chưa đổi mật khẩu lần đầu
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_ACCOUNT_ID  = "accountId";
    private static final String CLAIM_ROLE        = "role";
    private static final String CLAIM_UNIT_ID     = "unitId";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TEMP_PWD    = "tempPwd";

    private final SecretKey signingKey;
    private final long      expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationMs) {

        // HMAC-SHA256 key, secret phải >= 256 bit (32 chars UTF-8)
        this.signingKey   = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ----------------------------------------------------------------
    // Generate
    // ----------------------------------------------------------------

    public String generate(AccountPrincipal principal) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(principal.getEmail())
                .claim(CLAIM_ACCOUNT_ID,  principal.getAccountId())
                .claim(CLAIM_ROLE,        principal.getRoleCode())
                .claim(CLAIM_PERMISSIONS, principal.getPermissions())
                .claim(CLAIM_TEMP_PWD,    principal.isTempPassword())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey);

        // unitId chỉ embed nếu là UNIT account
        if (principal.getUnitId() != null) {
            builder.claim(CLAIM_UNIT_ID, principal.getUnitId());
        }

        return builder.compact();
    }

    // ----------------------------------------------------------------
    // Parse
    // ----------------------------------------------------------------

    public Claims parseAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Token đã hết hạn");
        } catch (SignatureException ex) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Chữ ký token không hợp lệ");
        } catch (MalformedJwtException ex) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Token không đúng định dạng");
        } catch (JwtException ex) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Token không hợp lệ");
        }
    }

    public String extractEmail(String token) {
        return parseAllClaims(token).getSubject();
    }

    public Long extractAccountId(String token) {
        return parseAllClaims(token).get(CLAIM_ACCOUNT_ID, Long.class);
    }

    public boolean extractTempPwd(String token) {
        return Boolean.TRUE.equals(parseAllClaims(token).get(CLAIM_TEMP_PWD, Boolean.class));
    }

    // ----------------------------------------------------------------
    // Validate (dùng trong JwtAuthFilter — không throw, chỉ return boolean)
    // ----------------------------------------------------------------

    public boolean isValid(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (AppException ex) {
            log.debug("JWT invalid: {}", ex.getMessage());
            return false;
        }
    }
}
