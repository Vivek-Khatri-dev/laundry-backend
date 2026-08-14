package com.dawsons.laundry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the sap.b1.* keys from application.properties.
 *
 * Kept disabled by default (sap.b1.enabled=false) so the app boots and runs
 * normally even before SAP credentials exist - SapB1Client refuses to make
 * any call while this is false, instead of failing on missing config.
 */
@Component
@ConfigurationProperties(prefix = "sap.b1")
public class SapB1Properties {

    private boolean enabled = false;

    /** Service Layer root, e.g. https://192.168.1.50:50000/b1s/v1 (no trailing slash) */
    private String baseUrl;

    /** The Company DB name on SQL Server/HANA, e.g. SBO_DAWSONS */
    private String companyDb;

    /** The dedicated integration user's B1 username */
    private String username;

    /** The dedicated integration user's B1 password */
    private String password;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getCompanyDb() { return companyDb; }
    public void setCompanyDb(String companyDb) { this.companyDb = companyDb; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}