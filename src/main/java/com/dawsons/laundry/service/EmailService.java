package com.dawsons.laundry.service;

import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Runs on the "mailTaskExecutor" background pool (see AsyncConfig) so the
    // caller (bill creation) doesn't wait on the SMTP round trip.
    @Async("mailTaskExecutor")
    public void sendOrderConfirmation(Bill bill, String customerEmail) {
        logger.info("=== Sending Order Confirmation Email ===");
        logger.info("Email enabled: {}", emailEnabled);
        logger.info("Customer email: {}", customerEmail);
        
        if (!emailEnabled) {
            logger.warn("Email is disabled!");
            return;
        }
        
        if (customerEmail == null || customerEmail.isEmpty()) {
            logger.warn("Customer email is null or empty!");
            return;
        }

        try {
            String subject = "🧺 Order Confirmation #" + String.format("%03d", bill.getReceiptNo());
            String body = buildOrderConfirmationEmail(bill);
            
            logger.info("Sending order confirmation to: {}", customerEmail);
            sendEmail(customerEmail, subject, body);
            logger.info("✅ Order confirmation email sent successfully to: {}", customerEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send order confirmation email: {}", e.getMessage(), e);
        }
    }

    @Async("mailTaskExecutor")
    public void sendReadyNotification(Bill bill, String customerEmail) {
        logger.info("=== Sending Ready Notification Email ===");
        logger.info("Customer email: {}", customerEmail);
        
        if (!emailEnabled || customerEmail == null || customerEmail.isEmpty()) {
            logger.warn("Cannot send ready notification - email disabled or missing");
            return;
        }

        try {
            String subject = "✅ Your Order is Ready - #" + String.format("%03d", bill.getReceiptNo());
            String body = buildReadyEmail(bill);
            sendEmail(customerEmail, subject, body);
            logger.info("✅ Ready notification email sent successfully to: {}", customerEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send ready notification email: {}", e.getMessage(), e);
        }
    }

    @Async("mailTaskExecutor")
    public void sendPaymentConfirmation(Bill bill, String customerEmail) {
        logger.info("=== Sending Payment Confirmation Email ===");
        logger.info("Email enabled: {}", emailEnabled);
        logger.info("Customer email: {}", customerEmail);
        logger.info("Receipt #: {}", bill.getReceiptNo());
        
        if (!emailEnabled) {
            logger.warn("Email is disabled!");
            return;
        }
        
        if (customerEmail == null || customerEmail.isEmpty()) {
            logger.warn("Customer email is null or empty!");
            return;
        }

        try {
            String subject = "💳 Payment Confirmation - #" + String.format("%03d", bill.getReceiptNo());
            String body = buildPaymentConfirmationEmail(bill);
            
            logger.info("Sending payment confirmation to: {}", customerEmail);
            logger.info("Email body length: {} characters", body.length());
            
            sendEmail(customerEmail, subject, body);
            logger.info("✅ Payment confirmation email sent successfully to: {}", customerEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send payment confirmation email: {}", e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            logger.info("Preparing email to: {}", to);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            logger.info("Sending email...");
            mailSender.send(message);
            logger.info("✅ Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // ORDER CONFIRMATION EMAIL
    // ============================================================
    private String buildOrderConfirmationEmail(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }");
        sb.append(".header { text-align: center; border-bottom: 3px solid #103B49; padding-bottom: 20px; }");
        sb.append(".header h1 { color: #103B49; margin: 0; }");
        sb.append(".content { padding: 20px 0; }");
        sb.append(".order-details { background: #f5f5f5; padding: 15px; border-radius: 8px; margin: 15px 0; }");
        sb.append(".order-details table { width: 100%; border-collapse: collapse; }");
        sb.append(".order-details td { padding: 8px; border-bottom: 1px solid #ddd; }");
        sb.append(".order-details .total { font-weight: bold; font-size: 18px; color: #103B49; }");
        sb.append(".footer { text-align: center; border-top: 1px solid #ddd; padding-top: 20px; margin-top: 20px; color: #888; font-size: 12px; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        
        sb.append("<div class='header'>");
        sb.append("<h1>🧺 Dawson's Laundry</h1>");
        sb.append("<p>Quality Laundry Services</p>");
        sb.append("</div>");
        
        sb.append("<div class='content'>");
        sb.append("<h2>Order Confirmation</h2>");
        sb.append("<p>Dear <strong>").append(escapeHtml(bill.getCustomerName())).append("</strong>,</p>");
        sb.append("<p>Thank you for choosing Dawson's Laundry! Your order has been received and is being processed.</p>");
        
        sb.append("<div class='order-details'>");
        sb.append("<h3 style='margin-top:0;'>Order Details</h3>");
        sb.append("<table>");
        sb.append("<tr><td><strong>Receipt #</strong></td><td>").append(String.format("%03d", bill.getReceiptNo())).append("</td></tr>");
        sb.append("<tr><td><strong>Date</strong></td><td>").append(bill.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
        sb.append("<tr><td><strong>Delivery Date</strong></td><td>").append(bill.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
        sb.append("<tr><td><strong>Items</strong></td><td></td></tr>");
        
        for (BillItem item : bill.getItems()) {
            sb.append("<tr><td style='padding-left:20px;'>").append(escapeHtml(item.getName()));
            if (item.isCustom()) {
                sb.append(" <span style='color:#2E9C8F;font-size:11px;'>(Custom)</span>");
            }
            sb.append("</td><td>").append(item.getQuantity()).append(" x Rs. ").append(item.getPrice()).append(" = Rs. ").append(item.getSubtotal()).append("</td></tr>");
        }
        
        sb.append("<tr><td><strong>Total</strong></td><td class='total'>Rs. ").append(bill.getTotalAmount()).append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");
        
        sb.append("<p><strong>Status:</strong> <span style='color:#F59E0B;'>Pending</span></p>");
        sb.append("<p>We will notify you when your order is ready for collection.</p>");
        sb.append("</div>");
        
        sb.append("<div class='footer'>");
        sb.append("<p>Dawson's Laundry<br>Phone: +92 300 1234567</p>");
        sb.append("<p>This is an automated message, please do not reply.</p>");
        sb.append("</div>");
        
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }

    // ============================================================
    // READY FOR COLLECTION EMAIL
    // ============================================================
    private String buildReadyEmail(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }");
        sb.append(".header { text-align: center; border-bottom: 3px solid #103B49; padding-bottom: 20px; }");
        sb.append(".header h1 { color: #103B49; margin: 0; }");
        sb.append(".content { padding: 20px 0; }");
        sb.append(".ready-details { background: #f0f8f5; padding: 15px; border-radius: 8px; margin: 15px 0; }");
        sb.append(".footer { text-align: center; border-top: 1px solid #ddd; padding-top: 20px; margin-top: 20px; color: #888; font-size: 12px; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        
        sb.append("<div class='header'>");
        sb.append("<h1>🧺 Dawson's Laundry</h1>");
        sb.append("</div>");
        
        sb.append("<div class='content'>");
        sb.append("<h2>✅ Your Order is Ready!</h2>");
        sb.append("<p>Dear <strong>").append(escapeHtml(bill.getCustomerName())).append("</strong>,</p>");
        sb.append("<p>Good news! Your order is now ready for collection.</p>");
        
        sb.append("<div class='ready-details'>");
        sb.append("<p><strong>Receipt #:</strong> ").append(String.format("%03d", bill.getReceiptNo())).append("</p>");
        sb.append("<p><strong>Total:</strong> Rs. ").append(bill.getTotalAmount()).append("</p>");
        sb.append("<p><strong>Please bring your receipt/ticket when collecting.</strong></p>");
        sb.append("</div>");
        
        sb.append("<p>📍 <strong>Dawson's Laundry</strong><br>123 Main Street, City</p>");
        sb.append("<p>We look forward to serving you again!</p>");
        sb.append("</div>");
        
        sb.append("<div class='footer'>");
        sb.append("<p>Dawson's Laundry<br>Phone: +92 300 1234567</p>");
        sb.append("</div>");
        
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }

    // ============================================================
    // PAYMENT CONFIRMATION EMAIL - SIMPLIFIED
    // ============================================================
    private String buildPaymentConfirmationEmail(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }");
        sb.append(".header { text-align: center; border-bottom: 3px solid #103B49; padding-bottom: 20px; }");
        sb.append(".header h1 { color: #103B49; margin: 0; }");
        sb.append(".content { padding: 20px 0; }");
        sb.append(".payment-details { background: #f0f8f5; padding: 15px; border-radius: 8px; margin: 15px 0; }");
        sb.append(".footer { text-align: center; border-top: 1px solid #ddd; padding-top: 20px; margin-top: 20px; color: #888; font-size: 12px; }");
        sb.append(".status-paid { color: #2E8B57; font-weight: bold; font-size: 16px; }");
        sb.append(".receipt-items { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        sb.append(".receipt-items td { padding: 6px; border-bottom: 1px solid #eee; }");
        sb.append(".receipt-items .total { font-weight: bold; font-size: 16px; color: #103B49; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        
        sb.append("<div class='header'>");
        sb.append("<h1>🧺 Dawson's Laundry</h1>");
        sb.append("<p>Quality Laundry Services</p>");
        sb.append("</div>");
        
        sb.append("<div class='content'>");
        sb.append("<h2>💳 Payment Confirmation</h2>");
        sb.append("<p>Dear <strong>").append(escapeHtml(bill.getCustomerName())).append("</strong>,</p>");
        sb.append("<p>Thank you for your payment! Your order has been successfully paid and completed.</p>");
        
        sb.append("<div class='payment-details'>");
        sb.append("<h3 style='margin-top:0;'>Payment Summary</h3>");
        sb.append("<p><strong>Receipt #:</strong> ").append(String.format("%03d", bill.getReceiptNo())).append("</p>");
        sb.append("<p><strong>Date:</strong> ").append(bill.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
        sb.append("<p><strong>Status:</strong> <span class='status-paid'>✅ PAID</span></p>");
        sb.append("<p><strong>Total Amount Paid:</strong> <span class='status-paid'>Rs. ").append(bill.getTotalAmount()).append("</span></p>");
        sb.append("</div>");
        
        sb.append("<div style='margin: 15px 0;'>");
        sb.append("<h3 style='margin-bottom:10px;'>Items</h3>");
        sb.append("<table class='receipt-items'>");
        sb.append("<tr style='background:#f5f5f5;'>");
        sb.append("<td><strong>Item</strong></td>");
        sb.append("<td style='text-align:center;'><strong>Qty</strong></td>");
        sb.append("<td style='text-align:right;'><strong>Price</strong></td>");
        sb.append("<td style='text-align:right;'><strong>Total</strong></td>");
        sb.append("</tr>");
        
        for (BillItem item : bill.getItems()) {
            sb.append("<tr>");
            sb.append("<td>").append(escapeHtml(item.getName()));
            if (item.isCustom()) {
                sb.append(" <span style='color:#2E9C8F;font-size:11px;'>(Custom)</span>");
            }
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>");
            sb.append("<td style='text-align:right;'>Rs. ").append(item.getPrice()).append("</td>");
            sb.append("<td style='text-align:right;'>Rs. ").append(item.getSubtotal()).append("</td>");
            sb.append("</tr>");
        }
        
        sb.append("<tr style='border-top:2px solid #103B49;'>");
        sb.append("<td colspan='3' style='text-align:right;font-weight:bold;'>Total</td>");
        sb.append("<td style='text-align:right;font-weight:bold;font-size:16px;color:#103B49;'>Rs. ").append(bill.getTotalAmount()).append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");
        
        sb.append("<p style='margin-top:20px;'>Thank you for choosing Dawson's Laundry! We look forward to serving you again.</p>");
        sb.append("<p style='color:#666;font-size:13px;'>📌 Please keep this receipt for your records.</p>");
        sb.append("</div>");
        
        sb.append("<div class='footer'>");
        sb.append("<p>Dawson's Laundry<br>Phone: +92 300 1234567</p>");
        sb.append("<p>This is an automated message, please do not reply.</p>");
        sb.append("</div>");
        
        sb.append("</body>");
        sb.append("</html>");
        
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}