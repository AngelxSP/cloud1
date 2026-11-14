package com.ferreteria.v1.models.dto;

public class SetupMfaResponse {
    private String secret;
    private String otpAuthUrl;

    public SetupMfaResponse(String secret, String otpAuthUrl) {
        this.secret = secret;
        this.otpAuthUrl = otpAuthUrl;
    }

    public String getSecret() { return secret; }
    public String getOtpAuthUrl() { return otpAuthUrl; }
}
