package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.shared.util.CodeGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh mã giao dịch dạng TX-{yyyyMMdd}-{6 chữ số}.
 * <p>
 * Chiến lược:
 * - Lưu counter trong bảng system_params với key = "TX_COUNTER_{yyyyMMdd}"
 * - Dùng SELECT ... FOR UPDATE để tránh race condition
 * - Chạy trong transaction REQUIRES_NEW để lock độc lập với transaction gọi bên ngoài
 * - Counter reset tự nhiên vì key mang theo ngày
 * - Format chuỗi ủy quyền cho CodeGenerator.generateTransactionCode()
 */
@Component
@RequiredArgsConstructor
public class TransactionCodeGenerator {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String KEY_PREFIX = "TX_COUNTER_";

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next() {
        String today = LocalDate.now().format(DATE_FMT);
        String key = KEY_PREFIX + today;

        Object existing = em.createNativeQuery(
                "SELECT param_value FROM system_params WHERE param_key = ? FOR UPDATE"
        ).setParameter(1, key).getResultStream().findFirst().orElse(null);

        long sequence;
        if (existing == null) {
            sequence = 1L;
            em.createNativeQuery(
                    "INSERT INTO system_params (param_key, param_value, description, updated_at) " +
                            "VALUES (?, '1', 'Auto-generated transaction counter', NOW())"
            ).setParameter(1, key).executeUpdate();
        } else {
            sequence = Long.parseLong(existing.toString()) + 1;
            em.createNativeQuery(
                            "UPDATE system_params SET param_value = ?, updated_at = NOW() WHERE param_key = ?"
                    ).setParameter(1, String.valueOf(sequence))
                    .setParameter(2, key)
                    .executeUpdate();
        }

        // Ủy quyền format cho CodeGenerator — nhất quán với toàn hệ thống
        return CodeGenerator.generateTransactionCode(LocalDate.now(), sequence, 6);
    }
}
