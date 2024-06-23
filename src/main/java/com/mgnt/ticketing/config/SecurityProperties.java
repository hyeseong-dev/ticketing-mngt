package com.mgnt.ticketing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private List<String> allowedUris;

    // Getters and Setters
    public List<String> getAllowedUris() {
        return allowedUris;
    }

    public void setAllowedUris(List<String> allowedUris) {
        this.allowedUris = allowedUris;
    }
}