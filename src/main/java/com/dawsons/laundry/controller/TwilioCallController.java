package com.dawsons.laundry.controller;

import com.dawsons.laundry.service.VoiceAssistantService;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/twilio")
public class TwilioCallController {

    private static final Logger logger = LoggerFactory.getLogger(TwilioCallController.class);
    private final VoiceAssistantService voiceAssistantService;

    public TwilioCallController(VoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    // ================================================================
    // ENTRY POINT - When customer calls
    // ================================================================
    @PostMapping(value = "/welcome", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> welcome(@RequestParam Map<String, String> request) {
        String fromNumber = request.get("From");
        logger.info("📞 Incoming call from: {}", fromNumber);

        // Build the response using TwiML
        String twiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Response>"
                + "<Gather input=\"speech\" action=\"/api/twilio/process-speech\" method=\"POST\" language=\"en-US\" speechTimeout=\"auto\">"
                + "<Say voice=\"Polly.Amy\">Welcome to Dawson's Laundry. Please tell me your receipt number, or say 'help' for assistance.</Say>"
                + "</Gather>"
                + "</Response>";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(twiml);
    }

    // ================================================================
    // PROCESS SPEECH - Handle customer's spoken response
    // ================================================================
    @PostMapping(value = "/process-speech", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> processSpeech(@RequestParam Map<String, String> request) {
        String speechResult = request.get("SpeechResult");
        String fromNumber = request.get("From");
        String confidence = request.get("Confidence");

        logger.info("🗣️ Spoken: '{}' (Confidence: {}) from: {}", speechResult, confidence, fromNumber);

        // Process through voice assistant service
        String responseText = voiceAssistantService.processVoiceInput(speechResult, fromNumber);

        // Check if we need to hang up
        if (responseText.contains("GOODBYE")) {
            String twiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Response>"
                    + "<Say voice=\"Polly.Amy\">Thank you for calling Dawson's Laundry. Goodbye.</Say>"
                    + "</Response>";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(twiml);
        }

        // Continue the conversation
        String twiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Response>"
                + "<Gather input=\"speech\" action=\"/api/twilio/process-speech\" method=\"POST\" language=\"en-US\" speechTimeout=\"auto\">"
                + "<Say voice=\"Polly.Amy\">" + escapeXml(responseText) + "</Say>"
                + "</Gather>"
                + "</Response>";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(twiml);
    }

    // ================================================================
    // Helper method to escape XML special characters
    // ================================================================
    private String escapeXml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}