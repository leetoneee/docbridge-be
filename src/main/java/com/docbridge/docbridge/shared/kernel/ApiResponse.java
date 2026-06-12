package com.docbridge.docbridge.shared.kernel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper chung cho mọi HTTP response của DocBridge.
 *
 * Thành công:  { "code": "SUCCESS",       "message": "...", "data": {...} }
 * Lỗi:        { "code": "UNIT_NOT_FOUND", "message": "...", "data": null  }
 * Phân trang: { "code": "SUCCESS",        "message": "...", "data": { "content": [...], "page": {...} } }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code    = code;
        this.message = message;
        this.data    = data;
    }

    /** Package-private: dùng cho GlobalExceptionHandler khi cần trả data có kiểu cụ thể */
    static <T> ApiResponse<T> of(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    // ----------------------------------------------------------------
    // Success factory methods
    // ----------------------------------------------------------------

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "Thành công", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /** Trả về success không kèm data (VD: delete, lock) */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    /**
     * Wrap Spring Page thành PageData để frontend dễ parse.
     * Dùng cho tất cả các endpoint có phân trang.
     */
    public static <T> ApiResponse<PageData<T>> success(Page<T> page) {
        return new ApiResponse<>("SUCCESS", "Thành công", PageData.of(page));
    }

    public static <T> ApiResponse<PageData<T>> success(String message, Page<T> page) {
        return new ApiResponse<>("SUCCESS", message, PageData.of(page));
    }

    // ----------------------------------------------------------------
    // Error factory methods
    // ----------------------------------------------------------------

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // ----------------------------------------------------------------
    // Inner class: PageData
    // Giữ nguyên thông tin Spring Page nhưng đặt vào field "data"
    // để structure nhất quán với non-paged response.
    // ----------------------------------------------------------------

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageData<T> {

        private final List<T> content;
        private final PageMeta page;

        private PageData(List<T> content, PageMeta page) {
            this.content = content;
            this.page    = page;
        }

        public static <T> PageData<T> of(Page<T> springPage) {
            PageMeta meta = new PageMeta(
                    springPage.getNumber(),
                    springPage.getSize(),
                    springPage.getTotalElements(),
                    springPage.getTotalPages()
            );
            return new PageData<>(springPage.getContent(), meta);
        }
    }

    @Getter
    public static class PageMeta {
        private final int    page;          // 0-based, giữ nguyên theo Spring
        private final int    size;
        private final long   totalElements;
        private final int    totalPages;

        public PageMeta(int page, int size, long totalElements, int totalPages) {
            this.page          = page;
            this.size          = size;
            this.totalElements = totalElements;
            this.totalPages    = totalPages;
        }
    }
}
