package com.docbridge.docbridge.module.unit.dto;

import lombok.Builder;
import lombok.Getter;

// ── UC2.5 — kết quả phê duyệt, hiển thị 1 lần ─────────────────────────────
@Getter
@Builder
public class ApproveUnitResult {

    private String interopCode;

    // Mật khẩu tạm thời — hiển thị đúng 1 lần, không lưu plaintext
    private String tempPassword;
}
