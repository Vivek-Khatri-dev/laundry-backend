package com.dawsons.laundry.security;

import com.dawsons.laundry.sap.api.SapApiKeyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;  // ✅ Use @Component, NOT @RestController
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component  // ✅ CORRECT - This is a Spring bean/filter, NOT a controller
public class SapApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SapApiKeyFilter.class);

    private final SapApiKeyProperties sapApiKeyProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SapApiKeyFilter(SapApiKeyProperties sapApiKeyProperties) {
        this.sapApiKeyProperties = sapApiKeyProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only validate API key for /api/sap endpoints
        if (!path.startsWith("/api/sap")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if SAP API is enabled
        if (!sapApiKeyProperties.isEnabled()) {
            logger.warn("SAP API is disabled in configuration");
            sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                    "SAP API is disabled");
            return;
        }

        // Get API key from header
        String apiKey = request.getHeader("X-API-Key");

        // Validate API key exists
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Missing API key for request: {}", path);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "API Key required. Please provide X-API-Key header.");
            return;
        }

        // Validate API key is in allowed list
        List<String> validKeys = sapApiKeyProperties.getKeys();
        if (validKeys == null || validKeys.isEmpty()) {
            logger.error("No valid API keys configured");
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "No API keys configured");
            return;
        }

        if (!validKeys.contains(apiKey.trim())) {
            logger.warn("Invalid API key used: {}", apiKey.substring(0, Math.min(apiKey.length(), 8)) + "...");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Invalid API Key");
            return;
        }

        // Valid API key - proceed with request
        logger.info("Valid API key used for request: {}", path);
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}