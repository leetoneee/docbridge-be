package com.docbridge.docbridge.shared.kernel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class cho tất cả JPA Entity của DocBridge.
 *
 * Tự động set created_at / updated_at qua Spring Data JPA Auditing.
 * Bật auditing bằng cách thêm @EnableJpaAuditing vào DocbridgeApplication.
 *
 * Entity kế thừa:
 *   public class InteropSystemEntity extends BaseEntity { ... }
 *
 * Các entity có thêm created_by (FK) tự khai báo field đó trực tiếp
 * vì kiểu dữ liệu là Long (account id), không phải String.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
