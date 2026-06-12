package com.docbridge.docbridge.shared.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh mã định danh theo business rules của DocBridge.
 *
 * - Mã liên thông : [MÃ_HT]-[STT 3 chữ số]  →  EOFFICE-001
 * - Mã giao dịch  : TX-[YYYYMMDD]-[SEQ 6 chữ số]  →  TX-20240115-000001
 *
 * STT / SEQ do caller truyền vào (lấy từ DB count hoặc sequence).
 * Class này chỉ chịu trách nhiệm format chuỗi.
 */
public final class CodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private CodeGenerator() {}

    /**
     * Sinh mã liên thông.
     *
     * @param systemCode  Mã hệ thống, VD: "EOFFICE"
     * @param sequence    STT tự tăng riêng theo hệ thống, bắt đầu từ 1
     * @param seqLength   Độ dài phần số, thường = 3
     * @return "EOFFICE-001"
     */
    public static String generateInteropCode(String systemCode, long sequence, int seqLength) {
        String seq = String.format("%0" + seqLength + "d", sequence);
        return systemCode.toUpperCase() + "-" + seq;
    }

    /** Overload với seqLength mặc định = 3 */
    public static String generateInteropCode(String systemCode, long sequence) {
        return generateInteropCode(systemCode, sequence, 3);
    }

    /**
     * Sinh mã giao dịch.
     *
     * @param date      Ngày tạo giao dịch
     * @param sequence  Số thứ tự trong ngày, bắt đầu từ 1
     * @param seqLength Độ dài phần số, thường = 6
     * @return "TX-20240115-000001"
     */
    public static String generateTransactionCode(LocalDate date, long sequence, int seqLength) {
        String dateStr = date.format(DATE_FMT);
        String seq     = String.format("%0" + seqLength + "d", sequence);
        return "TX-" + dateStr + "-" + seq;
    }

    /** Overload dùng ngày hiện tại, seqLength mặc định = 6 */
    public static String generateTransactionCode(long sequence) {
        return generateTransactionCode(LocalDate.now(), sequence, 6);
    }
}
