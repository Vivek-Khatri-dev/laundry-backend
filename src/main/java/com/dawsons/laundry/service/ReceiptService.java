package com.dawsons.laundry.service;

import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class ReceiptService {

    @Value("${app.company.name:Dawson's Laundry}")
    private String companyName;

    @Value("${app.company.address:123 Main Street, City}")
    private String companyAddress;

    @Value("${app.company.phone:+92 300 1234567}")
    private String companyPhone;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // ============================================================
    // HTML Receipt for Print
    // ============================================================
    public String buildReceiptHtml(Bill bill) {
        String qrCodeBase64 = generateQRCode(bill);
        return buildReceipt(bill, qrCodeBase64, false);
    }

    // ============================================================
    // PDF Receipt
    // ============================================================
    public byte[] buildReceiptPdf(Bill bill) throws Exception {
        String qrCodeBase64 = generateQRCode(bill);
        String xhtml = buildReceipt(bill, qrCodeBase64, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(xhtml, null);
        builder.toStream(out);
        builder.run();
        return out.toByteArray();
    }

    // ============================================================
    // QR Code Generator
    // ============================================================
    private String generateQRCode(Bill bill) {
        try {
            String trackUrl = baseUrl + "/track.html?receipt=" + bill.getReceiptNo();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(trackUrl, BarcodeFormat.QR_CODE, 150, 150);
            
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // ============================================================
    // Shared markup builder
    // ============================================================
    private String buildReceipt(Bill bill, String qrCodeBase64, boolean forPdf) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
        html.append("<head>");
        html.append(forPdf ? "<meta charset='UTF-8' />" : "<meta charset='UTF-8'>");
        html.append("<title>Receipt #").append(String.format("%03d", bill.getReceiptNo())).append("</title>");
        html.append("<style>");
        html.append("@page { size: 80mm auto; margin: 0; }");
        html.append("body { font-family: 'Courier New', monospace; margin: 0; padding: 4px 6px; background: white; font-size: 11px; line-height: 1.3; width: 80mm; }");
        html.append(".receipt { max-width: 100%; margin: 0 auto; padding: 4px 0; }");
        html.append(".divider-dash { border: none; border-top: 1px dashed #999; margin: 6px 0; }");
        html.append(".divider-solid { border: none; border-top: 2px solid #333; margin: 6px 0; }");
        html.append(".header { text-align: center; padding-bottom: 8px; margin-bottom: 6px; }");
        html.append(".company-name { font-size: 16px; font-weight: 700; color: #103B49; margin: 0; letter-spacing: 1px; }");
        html.append(".company-tag { font-size: 9px; color: #666; margin: 2px 0 4px 0; letter-spacing: 2px; text-transform: uppercase; }");
        html.append(".company-details { font-size: 9px; color: #888; line-height: 1.3; }");
        html.append(".receipt-title { text-align: center; margin: 4px 0; }");
        html.append(".receipt-title h2 { font-size: 14px; color: #103B49; margin: 0; letter-spacing: 2px; text-transform: uppercase; }");
        html.append(".receipt-title .sub { font-size: 10px; color: #888; margin-top: 2px; }");
        
        // Customer Info
        if (forPdf) {
            html.append(".info { width: 100%; border-collapse: collapse; background: #f5f5f5; margin: 6px 0; font-size: 10px; }");
            html.append(".info td { padding: 1px 8px; }");
            html.append(".info .label { color: #888; }");
            html.append(".info .value { font-weight: 600; color: #333; text-align: right; }");
        } else {
            html.append(".info { display: grid; grid-template-columns: 1fr 1.5fr; gap: 1px 6px; background: #f5f5f5; padding: 5px 8px; border-radius: 3px; margin: 6px 0; font-size: 10px; }");
            html.append(".info .label { color: #888; }");
            html.append(".info .value { font-weight: 600; color: #333; text-align: right; }");
        }
        
        html.append("table.items { width: 100%; border-collapse: collapse; margin: 4px 0; font-size: 10px; }");
        html.append("table.items thead th { text-align: left; padding: 3px 0; border-bottom: 1px solid #ccc; font-size: 9px; text-transform: uppercase; color: #888; letter-spacing: 0.5px; }");
        html.append("table.items tbody td { padding: 3px 0; border-bottom: 1px dotted #eee; vertical-align: top; }");
        html.append(".item-name { font-weight: 600; }");
        html.append(".custom-tag { font-size: 7px; color: #2E9C8F; font-weight: 600; margin-left: 3px; }");
        html.append(".item-qty { text-align: center; }");
        html.append(".item-price { text-align: right; }");
        html.append(".item-total { text-align: right; font-weight: 600; }");
        html.append(".discount-row { font-size: 9px; color: #666; text-align: right; }");
        
        if (forPdf) {
            html.append("table.total-row { width: 100%; border-top: 2px solid #103B49; margin-top: 2px; padding: 6px 0; }");
            html.append("table.total-row td { font-size: 14px; font-weight: 700; color: #103B49; padding: 6px 0; }");
        } else {
            html.append(".total-row { display: flex; justify-content: space-between; padding: 6px 0; border-top: 2px solid #103B49; margin-top: 2px; font-size: 14px; font-weight: 700; color: #103B49; }");
        }
        
        html.append(".footer { text-align: center; padding-top: 8px; margin-top: 8px; border-top: 1px dashed #ccc; font-size: 9px; color: #888; }");
        html.append(".footer .thankyou { font-size: 13px; font-weight: 700; color: #103B49; margin-bottom: 2px; }");
        html.append(".footer .timestamp { font-size: 8px; color: #aaa; margin-top: 4px; }");
        html.append(".status-badge { display: inline-block; padding: 1px 8px; border-radius: 6px; font-size: 9px; font-weight: 700; text-transform: uppercase; }");
        html.append(".status-badge.pending { background: #FEF3C7; color: #92400E; }");
        html.append(".status-badge.paid { background: #D1FAE5; color: #065F46; }");
        html.append(".status-badge.voided { background: #FEE2E2; color: #991B1B; }");
        html.append(".status-badge.returned { background: #FEF3C7; color: #92400E; }");
        html.append(".status-badge.processing { background: #DBEAFE; color: #1E40AF; }");
        html.append(".status-badge.ready { background: #D1FAE5; color: #065F46; }");
        html.append(".qr-code { text-align: center; margin: 8px 0; }");
        html.append(".qr-code img { width: 80px; height: 80px; }");
        html.append(".qr-code .track-text { font-size: 7px; color: #888; margin-top: 2px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='receipt'>");

        // Header
        html.append("<div class='header'>");
        html.append("<div class='company-name'>").append(forPdf ? "" : "\uD83E\uDDFA ").append(escapeHtml(companyName)).append("</div>");
        html.append("<div class='company-tag'>Quality Laundry Services</div>");
        html.append("<div class='company-details'>");
        html.append(escapeHtml(companyAddress)).append(forPdf ? "<br />" : "<br>");
        html.append("Tel: ").append(escapeHtml(companyPhone));
        html.append("</div>");
        html.append("</div>");

        html.append(forPdf ? "<hr class='divider-dash' />" : "<hr class='divider-dash'>");

        // Receipt Title
        html.append("<div class='receipt-title'>");
        html.append("<h2>RECEIPT</h2>");
        html.append("<div class='sub'>#").append(String.format("%03d", bill.getReceiptNo())).append("</div>");
        html.append("</div>");

        html.append(forPdf ? "<hr class='divider-dash' />" : "<hr class='divider-dash'>");

        // Customer Info
        if (forPdf) {
            html.append("<table class='info'>");
            html.append("<tr><td class='label'>Customer</td><td class='value'>").append(escapeHtml(bill.getCustomerName())).append("</td></tr>");
            if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isEmpty()) {
                html.append("<tr><td class='label'>Phone</td><td class='value'>").append(escapeHtml(bill.getCustomerPhone())).append("</td></tr>");
            }
            html.append("<tr><td class='label'>Date</td><td class='value'>").append(bill.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
            html.append("<tr><td class='label'>Delivery</td><td class='value'>").append(bill.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
            String statusClassPdf = bill.getStatus().name().toLowerCase();
            html.append("<tr><td class='label'>Status</td><td class='value'><span class='status-badge ").append(statusClassPdf).append("'>").append(bill.getStatus().name()).append("</span></td></tr>");
            html.append("</table>");
        } else {
            html.append("<div class='info'>");
            html.append("<span class='label'>Customer</span>");
            html.append("<span class='value'>").append(escapeHtml(bill.getCustomerName())).append("</span>");
            if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isEmpty()) {
                html.append("<span class='label'>Phone</span>");
                html.append("<span class='value'>").append(escapeHtml(bill.getCustomerPhone())).append("</span>");
            }
            html.append("<span class='label'>Date</span>");
            html.append("<span class='value'>").append(bill.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</span>");
            html.append("<span class='label'>Delivery</span>");
            html.append("<span class='value'>").append(bill.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</span>");
            html.append("<span class='label'>Status</span>");
            String statusClass = bill.getStatus().name().toLowerCase();
            html.append("<span class='value'><span class='status-badge ").append(statusClass).append("'>").append(bill.getStatus().name()).append("</span></span>");
            html.append("</div>");
        }

        html.append(forPdf ? "<hr class='divider-dash' />" : "<hr class='divider-dash'>");

        // Items Table
        html.append("<table class='items'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th style='width:45%;'>Item</th>");
        html.append("<th style='width:10%;text-align:center;'>Qty</th>");
        html.append("<th style='width:15%;text-align:right;'>Price</th>");
        html.append("<th style='width:15%;text-align:right;'>Disc</th>");
        html.append("<th style='width:15%;text-align:right;'>Total</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (BillItem item : bill.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal discountAmt = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal finalPrice = item.getFinalPrice() != null ? item.getFinalPrice() : itemTotal;
            totalDiscount = totalDiscount.add(discountAmt);
            
            String discountDisplay = "";
            if (discountAmt.compareTo(BigDecimal.ZERO) > 0) {
                String type = item.getDiscountType();
                if ("PERCENTAGE".equals(type)) {
                    discountDisplay = item.getDiscountValue() + "%";
                } else if ("FIXED".equals(type)) {
                    discountDisplay = "Rs." + item.getDiscountValue();
                }
            }
            
            html.append("<tr>");
            html.append("<td class='item-name'>");
            html.append(escapeHtml(item.getName()));
            if (item.isCustom()) {
                html.append("<span class='custom-tag'>Custom</span>");
            }
            html.append("</td>");
            html.append("<td class='item-qty'>").append(item.getQuantity()).append("</td>");
            html.append("<td class='item-price'>Rs ").append(item.getPrice()).append("</td>");
            html.append("<td class='item-price'>").append(discountDisplay).append("</td>");
            html.append("<td class='item-total'>Rs ").append(finalPrice).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        html.append(forPdf ? "<hr class='divider-solid' />" : "<hr class='divider-solid'>");

        // Total with Discount
        BigDecimal finalAmount = bill.getFinalAmount() != null ? bill.getFinalAmount() : bill.getTotalAmount();
        
        if (forPdf) {
            html.append("<table class='total-row'>");
            html.append("<tr><td style='text-align:left;'>Subtotal</td><td style='text-align:right;'>Rs. ").append(bill.getTotalAmount()).append("</td></tr>");
            if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                html.append("<tr><td style='text-align:left;'>Discount</td><td style='text-align:right;'>- Rs. ").append(totalDiscount).append("</td></tr>");
            }
            html.append("<tr><td style='text-align:left;font-size:16px;font-weight:700;'>TOTAL</td><td style='text-align:right;font-size:16px;font-weight:700;'>Rs. ").append(finalAmount).append("</td></tr>");
            html.append("</table>");
        } else {
            html.append("<div style='padding:4px 0;font-size:11px;color:#666;text-align:right;'>");
            html.append("Subtotal: Rs. ").append(bill.getTotalAmount());
            if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                html.append("<br>Discount: - Rs. ").append(totalDiscount);
            }
            html.append("</div>");
            html.append("<div class='total-row'>");
            html.append("<span>TOTAL</span>");
            html.append("<span>Rs. ").append(finalAmount).append("</span>");
            html.append("</div>");
        }

        html.append(forPdf ? "<hr class='divider-dash' />" : "<hr class='divider-dash'>");

        // QR Code
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            html.append("<div class='qr-code'>");
            html.append("<img src='").append(qrCodeBase64).append("' alt='QR Code' />");
            html.append("<div class='track-text'>Scan to track order</div>");
            html.append("</div>");
        }

        // Footer
        html.append("<div class='footer'>");
        html.append("<div class='thankyou'>Thank You!</div>");
        html.append("<p>Please bring receipt when collecting.").append(forPdf ? "<br />" : "<br>");
        html.append("Items not collected within 30 days").append(forPdf ? "<br />" : "<br>").append("will be donated to charity.</p>");

        LocalDate today = LocalDate.now();
        String dateStr = String.format("%02d/%02d/%04d",
            today.getDayOfMonth(),
            today.getMonthValue(),
            today.getYear()
        );
        html.append("<div class='timestamp'>Printed: ").append(dateStr).append("</div>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    // ============================================================
    // Helper Methods
    // ============================================================
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}