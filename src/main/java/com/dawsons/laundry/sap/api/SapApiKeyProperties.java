package com.dawsons.laundry.sap.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration for SAP API Keys.
 * 
 * Keys are stored in application.properties:
 * sap.api.enabled=true
 * sap.api.keys=key1,key2,key3
 * 
 * You can set any value you want for the API keys.
 * There are no rules - you can use letters, numbers, special characters.
 * Common formats: UUID, random string, or simple words.
 */
@Component
@ConfigurationProperties(prefix = "sap.api")
public class SapApiKeyProperties {
    
    private boolean enabled = false;
    private List<String> keys;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}