package com.docbridge.docbridge.module.transaction.transaction;

/**
 * Projection — map bảng interop_unit, dùng nội bộ trong module transaction.
 * Tránh coupling với InteropUnitEntity của module unit.
 */
public interface UnitTransactionSummary {
    Long getId();
    String getInteropCode();
    String getName();
    String getStatus();       // PENDING / ACTIVE / LOCKED / REJECTED
    Long getSystemId();
}
