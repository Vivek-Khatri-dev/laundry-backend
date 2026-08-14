package com.dawsons.laundry.service;

import com.dawsons.laundry.entity.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class VoiceAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(VoiceAssistantService.class);

    private final BillService billService;
    private final CustomerService customerService;
    private final WebClient webClient;

    // Store conversation state per caller
    private final Map<String, CallSession> sessions = new HashMap<>();

    public VoiceAssistantService(BillService billService, CustomerService customerService) {
        this.billService = billService;
        this.customerService = customerService;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    // ================================================================
    // MAIN PROCESSING METHOD
    // ================================================================

    public String processVoiceInput(String speechResult, String callerId) {
        if (speechResult == null || speechResult.trim().isEmpty()) {
            return "I didn't catch that. Please repeat.";
        }

        String text = speechResult.trim().toLowerCase();
        logger.info("Processing: '{}' for caller: {}", text, callerId);

        // Get or create session for this caller
        CallSession session = sessions.getOrDefault(callerId, new CallSession());
        sessions.put(callerId, session);

        // ============================================================
        // STEP 1: Handle Help/Commands
        // ============================================================
        if (containsAny(text, "help", "menu", "what can you do", "options")) {
            return "You can say your 3-digit receipt number to check order status, or ask general questions like 'what is your address'.";
        }

        if (containsAny(text, "goodbye", "bye", "thank you", "thanks")) {
            session.reset();
            return "GOODBYE";
        }

        // ============================================================
        // STEP 2: Handle General Questions (AI)
        // ============================================================
        if (isGeneralQuestion(text)) {
            return getAIResponse(text);
        }

        // ============================================================
        // STEP 3: Handle Receipt Number Detection
        // ============================================================
        String receiptNo = extractReceiptNumber(text);
        if (receiptNo != null && session.getReceiptNo() == null) {
            return handleReceiptNumber(receiptNo, session);
        }

        // ============================================================
        // STEP 4: Handle Name Verification
        // ============================================================
        if (session.getReceiptNo() != null && session.getCustomerName() == null) {
            return handleNameVerification(text, session);
        }

        // ============================================================
        // STEP 5: Handle Amount Verification
        // ============================================================
        if (session.getReceiptNo() != null && session.getCustomerName() != null && !session.isVerified()) {
            Double amount = extractAmount(text);
            if (amount != null) {
                return handleAmountVerification(amount, session);
            }
        }

        // ============================================================
        // STEP 6: Handle Status Check
        // ============================================================
        if (session.isVerified() && containsAny(text, "status", "order", "check", "where", "ready", "when")) {
            return getOrderStatus(session);
        }

        // ============================================================
        // STEP 7: Fallback - Ask AI
        // ============================================================
        return getAIResponse(text);
    }

    // ================================================================
    // HANDLE RECEIPT NUMBER
    // ================================================================

    private String handleReceiptNumber(String receiptNo, CallSession session) {
        try {
            Integer receiptNumber = Integer.parseInt(receiptNo);
            Bill bill = billService.getByReceiptNoOrThrow(receiptNumber);

            session.setReceiptNo(receiptNo);
            session.setCustomerName(bill.getCustomerName());
            session.setTotalAmount(bill.getFinalAmount() != null ? 
                    bill.getFinalAmount().doubleValue() : 
                    bill.getTotalAmount().doubleValue());

            return String.format(
                "Found order for %s. The total amount is Rs. %.0f. Please say your name to verify.",
                session.getCustomerName(), session.getTotalAmount()
            );

        } catch (Exception e) {
            return "I couldn't find a bill with that receipt number. Please try again or say 'help'.";
        }
    }

    // ================================================================
    // HANDLE NAME VERIFICATION
    // ================================================================

    private String handleNameVerification(String text, CallSession session) {
        // Clean text to extract name
        String name = text.replaceAll("(?i)(my name is|i am|this is|name is|its|it's)", "").trim();

        if (name.length() < 2) {
            return "I didn't catch your name. Please say it clearly.";
        }

        if (name.equalsIgnoreCase(session.getCustomerName())) {
            return String.format(
                "Verification successful. Your order total is Rs. %.0f. Say 'status' to check your order.",
                session.getTotalAmount()
            );
        } else {
            return String.format(
                "The name '%s' doesn't match our records. Please try again or say 'help'.",
                name
            );
        }
    }

    // ================================================================
    // HANDLE AMOUNT VERIFICATION
    // ================================================================

    private String handleAmountVerification(Double enteredAmount, CallSession session) {
        if (Math.abs(enteredAmount - session.getTotalAmount()) < 0.01) {
            session.setVerified(true);
            return "Verification successful. Say 'status' to check your order.";
        } else {
            return String.format(
                "The amount Rs. %.0f doesn't match our records (Rs. %.0f). Please try again.",
                enteredAmount, session.getTotalAmount()
            );
        }
    }

    // ================================================================
    // GET ORDER STATUS
    // ================================================================

    private String getOrderStatus(CallSession session) {
        try {
            Integer receiptNo = Integer.parseInt(session.getReceiptNo());
            Bill bill = billService.getByReceiptNoOrThrow(receiptNo);
            String status = bill.getStatus().name();

            String statusMessage = getStatusMessage(status, bill);

            // Reset session so next customer can use
            String response = statusMessage;
            session.reset();
            return response;

        } catch (Exception e) {
            return "I'm having trouble checking your order. Please try again later.";
        }
    }

    private String getStatusMessage(String status, Bill bill) {
        switch (status) {
            case "PENDING":
                return "Your order is pending and will be processed soon. Expected delivery: " + bill.getDeliveryDate();
            case "PROCESSING":
                return "Your order is being processed. It will be ready by " + bill.getDeliveryDate();
            case "READY":
                return "Good news! Your order is ready for pickup.";
            case "PAID":
                return "Your order has been paid and delivered. Thank you for choosing Dawson's Laundry!";
            case "VOIDED":
                return "This order has been cancelled. Please contact us for more information.";
            default:
                return "Your order is currently being processed.";
        }
    }

    // ================================================================
    // AI FOR GENERAL QUESTIONS
    // ================================================================

    private boolean isGeneralQuestion(String text) {
        String[] generalKeywords = {
            "address", "location", "timing", "hours", "open", "close",
            "price", "cost", "charge", "service", "what", "how", "where",
            "who", "when", "which", "why", "tell", "explain", "information"
        };
        return containsAny(text, generalKeywords);
    }

    private String getAIResponse(String question) {
        try {
            String systemPrompt = """
                You are a friendly laundry assistant for Dawson's Laundry in Pakistan.
                You help customers with order status and general questions.
                Answer questions about:
                - Business hours: 9 AM to 9 PM, Monday to Saturday
                - Address: Provide your actual address
                - Services: Wash & Fold, Dry Cleaning, Ironing, Special Treatments
                - Pricing: Depends on item type and quantity
                If you don't know something, say "I'll connect you to a representative."
                Keep responses short and conversational.
                """;

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3.2:3b");
            request.put("prompt", systemPrompt + "\n\nQuestion: " + question + "\n\nAnswer:");
            request.put("stream", false);

            // Call Ollama
            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse JSON response
            if (response != null && response.contains("\"response\"")) {
                int start = response.indexOf("\"response\":\"") + 12;
                int end = response.indexOf("\"", start);
                if (end > start) {
                    return response.substring(start, end).replace("\\n", " ");
                }
            }

            return "I'll connect you to a representative for that question.";

        } catch (Exception e) {
            logger.error("AI error: {}", e.getMessage());
            return "I'm having trouble answering that. Please call again later.";
        }
    }

    // ================================================================
    // UTILITY METHODS
    // ================================================================

    private String extractReceiptNumber(String text) {
        Pattern pattern = Pattern.compile("\\b(\\d{3})\\b");
        var matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Double extractAmount(String text) {
        Pattern pattern = Pattern.compile("\\b(\\d+(\\.\\d+)?)\\b");
        var matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    // ================================================================
    // INNER CLASS - Call Session
    // ================================================================

    private static class CallSession {
        private String receiptNo;
        private String customerName;
        private Double totalAmount;
        private boolean verified;

        public String getReceiptNo() { return receiptNo; }
        public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        public void reset() {
            receiptNo = null;
            customerName = null;
            totalAmount = null;
            verified = false;
        }
    }
}