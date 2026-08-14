package com.dawsons.laundry.sap;

import com.dawsons.laundry.config.SapB1Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the SAP Business One Service Layer REST API.
 *
 * Responsibilities (only this - no business/mapping logic lives here):
 *  - log in with CompanyDB/username/password and remember the session
 *  - attach the session cookie to every call
 *  - transparently re-login and retry once if the session expired
 *
 * Business-specific calls (e.g. "push this Customer as a Business Partner")
 * belong in a separate SapB1SyncService that USES this client - keeping the
 * plumbing (this file) and the mapping (that file) apart.
 *
 * Does nothing until sap.b1.enabled=true is set in application.properties.
 */
@Component
public class SapB1Client {

    private static final Logger logger = LoggerFactory.getLogger(SapB1Client.class);

    private final SapB1Properties properties;
    private final RestTemplate restTemplate;

    private String sessionCookie; // e.g. "B1SESSION=xxxxxxxx"

    public SapB1Client(SapB1Properties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /** Simple connectivity check - call this manually first to prove the config works. */
    public boolean testConnection() {
        try {
            login();
            return true;
        } catch (Exception e) {
            logger.error("SAP B1 connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> get(String resourcePath) {
        return execute(() -> restTemplate.exchange(
                properties.getBaseUrl() + resourcePath,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class));
    }

    public Map<String, Object> post(String resourcePath, Object body) {
        return execute(() -> restTemplate.exchange(
                properties.getBaseUrl() + resourcePath,
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                Map.class));
    }

    public void patch(String resourcePath, Object body) {
        execute(() -> restTemplate.exchange(
                properties.getBaseUrl() + resourcePath,
                HttpMethod.PATCH,
                new HttpEntity<>(body, authHeaders()),
                Void.class));
    }

    // ------------------------------------------------------------------

    private synchronized void login() {
        String url = properties.getBaseUrl() + "/Login";
        Map<String, String> body = Map.of(
                "CompanyDB", properties.getCompanyDb(),
                "UserName", properties.getUsername(),
                "Password", properties.getPassword()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        this.sessionCookie = extractSessionCookie(setCookies);

        if (this.sessionCookie == null) {
            throw new IllegalStateException("SAP B1 login succeeded but no B1SESSION cookie was returned");
        }
        logger.info("Logged into SAP B1 Service Layer (CompanyDB: {})", properties.getCompanyDb());
    }

    private String extractSessionCookie(List<String> setCookieHeaders) {
        if (setCookieHeaders == null) return null;
        for (String header : setCookieHeaders) {
            if (header.startsWith("B1SESSION=")) {
                return header.split(";")[0];
            }
        }
        return null;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return headers;
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        ResponseEntity<T> run();
    }

    private <T> T execute(ApiCall<T> call) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("SAP B1 integration is disabled (set sap.b1.enabled=true to use it)");
        }
        if (sessionCookie == null) {
            login();
        }
        try {
            return call.run().getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            // Session expired (B1 sessions last ~30 min) - log in again and retry exactly once.
            logger.info("SAP B1 session expired, re-authenticating");
            login();
            return call.run().getBody();
        }
    }
}