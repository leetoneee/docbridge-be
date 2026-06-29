package com.docbridge.docbridge.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi mật khẩu tạm thời cho tài khoản mới tạo hoặc vừa reset.
     * Chạy async để không block request.
     */
    @Async
    public void sendTempPassword(String toEmail, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("docbridge2026@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[DocBridge] Thông tin đăng nhập");
            message.setText("""
                    Chào bạn,
                    
                    Tài khoản DocBridge của bạn đã được tạo / đặt lại mật khẩu.
                    
                    Email đăng nhập : %s
                    Mật khẩu tạm thời: %s
                    
                    Vui lòng đăng nhập và đổi mật khẩu ngay lần đầu tiên.
                    
                    Trân trọng,
                    Hệ thống DocBridge
                    """.formatted(toEmail, tempPassword));

            mailSender.send(message);
            log.info("Sent temp-password email to {}", toEmail);
        } catch (Exception e) {
            // Không throw — lỗi email không nên rollback transaction tạo tài khoản
            log.error("Failed to send temp-password email to {}", toEmail, e);
        }
    }
}
