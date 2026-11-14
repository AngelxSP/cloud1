package com.ferreteria.v1.models.dto;

public class MfaVerifyRequest {
    private String email;
    private String method; // totp or email
    private String code;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
