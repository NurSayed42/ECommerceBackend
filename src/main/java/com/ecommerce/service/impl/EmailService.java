package com.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Async("taskExecutor")
    public void sendOtpEmail(String to, String otp) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;background:#f9f9f9">
              <div style="background:#fff;border-radius:10px;padding:30px;text-align:center">
                <h2 style="color:#e74c3c">Email Verification</h2>
                <p>Your OTP code is:</p>
                <div style="font-size:36px;font-weight:bold;color:#e74c3c;letter-spacing:8px;margin:20px 0">%s</div>
                <p style="color:#888">Valid for 5 minutes. Do not share this OTP.</p>
              </div>
            </div>""".formatted(otp);
        sendHtml(to, "Email Verification - OTP", html);
    }

    @Async("taskExecutor")
    public void sendPasswordResetEmail(String to, String otp) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2>Password Reset Request</h2>
              <p>Your OTP for password reset:</p>
              <div style="font-size:32px;font-weight:bold;color:#3498db;letter-spacing:6px">%s</div>
              <p>Valid for 5 minutes.</p>
            </div>""".formatted(otp);
        sendHtml(to, "Password Reset OTP", html);
    }

    @Async("taskExecutor")
    public void sendOrderConfirmationEmail(String to, String orderNumber, String customerName) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;background:#f9f9f9">
              <div style="background:#fff;border-radius:10px;padding:30px">
                <h2 style="color:#2ecc71">Order Confirmed! 🎉</h2>
                <p>Dear %s,</p>
                <p>Your order <strong>#%s</strong> has been placed successfully.</p>
                <p>You will receive updates about your order status via email.</p>
                <a href="#" style="background:#e74c3c;color:#fff;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;margin-top:15px">Track Order</a>
              </div>
            </div>""".formatted(customerName, orderNumber);
        sendHtml(to, "Order Confirmed - #" + orderNumber, html);
    }

    @Async("taskExecutor")
    public void sendOrderStatusUpdateEmail(String to, String orderNumber, String status) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2>Order Update</h2>
              <p>Your order <strong>#%s</strong> status has been updated to: <strong>%s</strong></p>
            </div>""".formatted(orderNumber, status);
        sendHtml(to, "Order Update - #" + orderNumber, html);
    }

    private void sendHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
