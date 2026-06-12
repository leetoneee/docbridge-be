package com.docbridge.docbridge.shared.kernel;

import lombok.Getter;

/**
 * Exception nghiệp vụ duy nhất của DocBridge.
 * Luôn throw AppException thay vì RuntimeException thông thường.
 *
 * Dùng:
 *   throw new AppException(ErrorCode.UNIT_NOT_FOUND);
 *   throw new AppException(ErrorCode.UNIT_NOT_FOUND, "Đơn vị EOFFICE-001 không tồn tại");
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /** Ghi đè message mặc định, dùng khi cần đính kèm thông tin cụ thể */
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** Dùng khi wrap exception từ tầng dưới (VD: OptimisticLockException) */
    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
