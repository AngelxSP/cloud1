package com.ferreteria.v1.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class MfaService {

    private static final Logger logger = LoggerFactory.getLogger(MfaService.class);

    public String generateSecret() {
        try {
            GoogleAuthenticator g = new GoogleAuthenticator();
            Object keyObj = g.createCredentials();
            if (keyObj instanceof GoogleAuthenticatorKey) {
                return ((GoogleAuthenticatorKey) keyObj).getKey();
            }
            return keyObj != null ? keyObj.toString() : null;
        } catch (Throwable t) {
            // fallback: generate a simple random secret
            return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    public String getOtpAuthUrl(String user, String secret) {
        String issuer = "Ferreteria";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30", issuer, user, secret, issuer);
    }

    public boolean verifyCode(String secret, int code) {
        // Compute expected codes for window -1,0,+1 and log them for debugging
        try {
            List<Integer> expected = getExpectedCodes(secret);
            logger.warn("[TOTP DEBUG] secret={} provided={} expected={}", secret, code, expected);
        } catch (Exception e) {
            logger.warn("[TOTP DEBUG] failed to compute expected codes: {}", e.getMessage());
        }

        GoogleAuthenticator g = new GoogleAuthenticator();
        return g.authorize(secret, code);
    }

    // Compute TOTP codes for the current time window and neighbors
    private List<Integer> getExpectedCodes(String base32Secret) throws Exception {
        byte[] key = base32Decode(base32Secret);
        long epochSeconds = Instant.now().getEpochSecond();
        long timeStep = epochSeconds / 30;
        List<Integer> codes = new ArrayList<>();
        for (long t = timeStep - 1; t <= timeStep + 1; t++) {
            codes.add(generateTotp(key, t));
        }
        return codes;
    }

    // Public wrapper for debugging/test purposes: returns the expected TOTP codes
    public List<Integer> getCurrentTotpCodes(String base32Secret) {
        try {
            return getExpectedCodes(base32Secret);
        } catch (Exception e) {
            logger.warn("[TOTP DEBUG] failed to compute expected codes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private int generateTotp(byte[] key, long timestep) throws Exception {
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (timestep & 0xFF);
            timestep >>= 8;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec spec = new SecretKeySpec(key, "HmacSHA1");
        mac.init(spec);
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        return binary % 1000000;
    }

    // Minimal Base32 decoder (RFC 4648) for uppercase alphabet with optional padding
    private byte[] base32Decode(String input) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String s = input.trim().replace("=", "").toUpperCase();
        int bits = 0;
        int value = 0;
        List<Byte> out = new ArrayList<>();
        for (char c : s.toCharArray()) {
            int idx = alphabet.indexOf(c);
            if (idx < 0) continue;
            value = (value << 5) | idx;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                int b = (value >> bits) & 0xFF;
                out.add((byte) b);
            }
        }
        byte[] res = new byte[out.size()];
        for (int i = 0; i < out.size(); i++) res[i] = out.get(i);
        return res;
    }
}
