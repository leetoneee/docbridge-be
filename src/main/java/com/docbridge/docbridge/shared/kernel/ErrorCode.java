package com.docbridge.docbridge.shared.kernel;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Tập trung toàn bộ error code của DocBridge.
 * Mỗi entry gồm: HTTP status + code string + message mặc định.
 * <p>
 * Dùng trong AppException:
 * throw new AppException(ErrorCode.UNIT_NOT_FOUND);
 * throw new AppException(ErrorCode.UNIT_NOT_FOUND, "Đơn vị EOFFICE-001 không tồn tại");
 */
@Getter
public enum ErrorCode {

    // ----------------------------------------------------------------
    // Generic
    // ----------------------------------------------------------------
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Lỗi hệ thống, vui lòng thử lại sau"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu đầu vào không hợp lệ"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa xác thực, vui lòng đăng nhập"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên"),

    // ----------------------------------------------------------------
    // Auth
    // ----------------------------------------------------------------
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED", "Tài khoản đã bị khoá"),
    ACCOUNT_PENDING(HttpStatus.FORBIDDEN, "ACCOUNT_PENDING", "Tài khoản chờ phê duyệt"),
    MUST_CHANGE_PASSWORD(HttpStatus.FORBIDDEN, "MUST_CHANGE_PASSWORD", "Bạn phải đổi mật khẩu trước khi tiếp tục"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Token không hợp lệ hoặc đã hết hạn"),
    WRONG_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_OLD_PASSWORD", "Mật khẩu cũ không đúng"),

    // ----------------------------------------------------------------
    // Interop System
    // ----------------------------------------------------------------
    SYSTEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SYSTEM_NOT_FOUND", "Không tìm thấy hệ thống liên thông"),
    SYSTEM_CODE_DUPLICATED(HttpStatus.CONFLICT, "SYSTEM_CODE_DUPLICATED", "Mã hệ thống đã tồn tại"),
    SYSTEM_CODE_IMMUTABLE(HttpStatus.BAD_REQUEST, "SYSTEM_CODE_IMMUTABLE", "Không thể thay đổi mã hệ thống khi đã có đơn vị"),
    SYSTEM_LOCKED(HttpStatus.FORBIDDEN, "SYSTEM_LOCKED", "Hệ thống liên thông đang bị khoá"),
    SYSTEM_HAS_UNITS(HttpStatus.CONFLICT, "SYSTEM_HAS_UNITS", "Không thể xoá hệ thống đang có đơn vị"),

    // ----------------------------------------------------------------
    // Interop Unit
    // ----------------------------------------------------------------
    UNIT_NOT_FOUND(HttpStatus.NOT_FOUND, "UNIT_NOT_FOUND", "Không tìm thấy đơn vị liên thông"),
    UNIT_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "UNIT_EMAIL_DUPLICATED", "Email đơn vị đã được sử dụng"),
    UNIT_LOCKED(HttpStatus.FORBIDDEN, "UNIT_LOCKED", "Đơn vị đang bị khoá, không thể thực hiện giao dịch"),
    UNIT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "UNIT_NOT_ACTIVE", "Đơn vị chưa được kích hoạt"),
    UNIT_ALREADY_APPROVED(HttpStatus.CONFLICT, "UNIT_ALREADY_APPROVED", "Đơn vị đã được phê duyệt trước đó"),
    UNIT_ALREADY_REJECTED(HttpStatus.CONFLICT, "UNIT_ALREADY_REJECTED", "Đơn vị đã bị từ chối trước đó"),
    UNIT_NOT_PENDING(HttpStatus.BAD_REQUEST, "UNIT_NOT_PENDING", "Đơn vị không ở trạng thái chờ phê duyệt"),
    UNIT_CANNOT_LOCK(HttpStatus.BAD_REQUEST, "UNIT_CANNOT_LOCK", "Chỉ có thể khoá/mở khoá đơn vị đang ACTIVE hoặc LOCKED"),
    UNIT_HAS_TRANSACTIONS(HttpStatus.CONFLICT, "UNIT_HAS_TRANSACTIONS", "Không thể xoá đơn vị đã có giao dịch"),
    UNIT_CANNOT_UPDATE_REJECTED(HttpStatus.BAD_REQUEST, "UNIT_CANNOT_UPDATE_REJECTED", "Không thể cập nhật đơn vị đã bị từ chối"),
    UNIT_CANNOT_UPDATE_EMAIL(HttpStatus.BAD_REQUEST, "UNIT_CANNOT_UPDATE_EMAIL", "Chỉ có thể đổi email đơn vị đang ACTIVE hoặc LOCKED"),

    // ----------------------------------------------------------------
    // Account
    // ----------------------------------------------------------------
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND,             "ACCOUNT_NOT_FOUND","Không tìm thấy tài khoản"),

    ACCOUNT_EMAIL_DUPLICATED(HttpStatus.CONFLICT,              "ACCOUNT_EMAIL_DUPLICATED","Email tài khoản đã tồn tại"),

    UNIT_ACCOUNT_EXISTS(HttpStatus.CONFLICT,              "UNIT_ACCOUNT_EXISTS","Đơn vị đã có tài khoản đại diện"),

    ACCOUNT_ALREADY_LOCKED(HttpStatus.BAD_REQUEST,           "ACCOUNT_ALREADY_LOCKED","Tài khoản đã bị khoá"),

    ACCOUNT_NOT_LOCKED(HttpStatus.BAD_REQUEST,           "ACCOUNT_NOT_LOCKED","Tài khoản không trong trạng thái khoá"),

    CANNOT_MODIFY_ADMIN_ACCOUNT(HttpStatus.FORBIDDEN,             "CANNOT_MODIFY_ADMIN_ACCOUNT","Không thể thực hiện thao tác này với tài khoản Admin"),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,              "EMAIL_ALREADY_EXISTS","Email đã được sử dụng"),

    // ----------------------------------------------------------------
    // Transaction
    // ----------------------------------------------------------------
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND,             "TRANSACTION_NOT_FOUND","Không tìm thấy giao dịch"),

    TRANSACTION_NOT_EDITABLE(HttpStatus.CONFLICT,              "TRANSACTION_NOT_EDITABLE","Giao dịch đã ở trạng thái cuối, không thể thay đổi"),

    TRANSACTION_OPTIMISTIC_LOCK(HttpStatus.CONFLICT,           "TRANSACTION_OPTIMISTIC_LOCK","Giao dịch vừa được cập nhật bởi người khác, vui lòng thử lại"),

    TRANSACTION_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN,         "TRANSACTION_CANCEL_FORBIDDEN","Chỉ đơn vị gửi mới có thể thu hồi giao dịch"),

    TRANSACTION_ACCEPT_FORBIDDEN(HttpStatus.FORBIDDEN,         "TRANSACTION_ACCEPT_FORBIDDEN","Chỉ đơn vị nhận mới có thể chấp nhận giao dịch"),

    TRANSACTION_REJECT_FORBIDDEN(HttpStatus.FORBIDDEN,         "TRANSACTION_REJECT_FORBIDDEN","Chỉ đơn vị nhận mới có thể từ chối giao dịch"),

    CANNOT_SEND_TO_SELF(HttpStatus.BAD_REQUEST,           "CANNOT_SEND_TO_SELF","Không thể gửi văn bản cho chính đơn vị mình"),

    RECEIVER_SYSTEM_LOCKED(HttpStatus.BAD_REQUEST,      "RECEIVER_SYSTEM_LOCKED",      "Hệ thống của đơn vị nhận đang bị khoá"),

    RECEIVER_UNIT_LOCKED(HttpStatus.BAD_REQUEST,        "RECEIVER_UNIT_LOCKED",        "Đơn vị nhận đang bị khoá"),

    // ----------------------------------------------------------------
    // Permission / Role
    // ----------------------------------------------------------------
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND,             "ROLE_NOT_FOUND","Không tìm thấy role"),

    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND,             "PERMISSION_NOT_FOUND","Không tìm thấy permission"),

    PERMISSION_ALREADY_ASSIGNED(HttpStatus.CONFLICT,           "PERMISSION_ALREADY_ASSIGNED","Permission đã được gán cho role này"),

    PERMISSION_NOT_ASSIGNED(HttpStatus.CONFLICT,           "PERMISSION_NOT_ASSIGNED","Permission chưa được gán cho role này"),

    PERMISSION_PROTECTED(HttpStatus.FORBIDDEN,           "PERMISSION_PROTECTED","Permission này không được phép thay đổi");

    // ----------------------------------------------------------------

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    }
