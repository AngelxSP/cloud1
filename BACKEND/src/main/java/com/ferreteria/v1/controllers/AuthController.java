package com.ferreteria.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.models.dto.LoginRequest;
import com.ferreteria.v1.models.dto.LoginResponse;
import com.ferreteria.v1.models.dto.MfaChallengeResponse;
import com.ferreteria.v1.models.dto.MfaVerifyRequest;
import com.ferreteria.v1.models.dto.SetupMfaResponse;
import com.ferreteria.v1.models.dto.ChangePasswordRequest;

import com.ferreteria.v1.security.JwtUtil;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.UsuarioRepository;
import com.ferreteria.v1.services.MfaService;
import com.ferreteria.v1.services.EmailService;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        if (usuario.getMfaEnabled() != null && usuario.getMfaEnabled()) {
            // generate and send email OTP for email-based MFA
            int code = (int) (Math.random() * 900000) + 100000;
            String codeStr = String.valueOf(code);
            usuario.setEmailOtp(codeStr);
            usuario.setEmailOtpExpiry(LocalDateTime.now().plusMinutes(10));
            usuarioRepository.save(usuario);

            String subject = "Tu código de verificación";
            String body = "Tu código de verificación es: " + codeStr + " (válido 10 minutos)";
            try {
                emailService.sendSimpleMessage(usuario.getEmail(), subject, body);
            } catch (Exception ex) {
                // log but continue; email configuration may be placeholder
                System.err.println("Error enviando OTP por email: " + ex.getMessage());
            }

            MfaChallengeResponse challenge = new MfaChallengeResponse(true, List.of("totp", "email"));
            return ResponseEntity.ok(challenge);
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().getNombre(), usuario.getId());
        LoginResponse response = new LoginResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().getNombre());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail()).orElseThrow();

        boolean ok = false;
        if ("totp".equalsIgnoreCase(req.getMethod())) {
            try {
                int code = Integer.parseInt(req.getCode());
                ok = mfaService.verifyCode(usuario.getMfaSecret(), code);
            } catch (NumberFormatException ex) {
                ok = false;
            }
        } else if ("email".equalsIgnoreCase(req.getMethod())) {
            if (usuario.getEmailOtp() != null && usuario.getEmailOtp().equals(req.getCode())
                    && usuario.getEmailOtpExpiry() != null && usuario.getEmailOtpExpiry().isAfter(LocalDateTime.now())) {
                ok = true;
                usuario.setEmailOtp(null);
                usuario.setEmailOtpExpiry(null);
                usuarioRepository.save(usuario);
            }
        }

        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código MFA inválido");
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().getNombre(), usuario.getId());
        LoginResponse response = new LoginResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().getNombre());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-mfa")
    public ResponseEntity<?> setupMfa(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        String secret = usuario.getMfaSecret();
        if (secret == null || secret.isBlank()) {
            secret = mfaService.generateSecret();
            usuario.setMfaSecret(secret);
            usuarioRepository.save(usuario);
        }

        String otpAuth = mfaService.getOtpAuthUrl(usuario.getEmail(), secret);
        SetupMfaResponse resp = new SetupMfaResponse(secret, otpAuth);
        return ResponseEntity.ok(resp);
    }

    // --- Debug endpoints (temporary) ---
    @PostMapping("/debug-totp")
    public ResponseEntity<?> debugTotp(@RequestBody Map<String, String> body) {
        String secret = body.get("secret");
        if (secret == null || secret.isBlank()) {
            return ResponseEntity.badRequest().body("Falta el campo 'secret' en el cuerpo");
        }
        return ResponseEntity.ok(mfaService.getCurrentTotpCodes(secret));
    }

    @PostMapping("/debug-get-secret")
    public ResponseEntity<?> debugGetSecret(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Falta el campo 'email' en el cuerpo");
        }
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(Map.of("secret", usuario.getMfaSecret(), "mfaEnabled", usuario.getMfaEnabled()));
    }

    @PostMapping("/send-email-otp")
    public ResponseEntity<?> sendEmailOtp(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        int code = (int) (Math.random() * 900000) + 100000;
        String codeStr = String.valueOf(code);
        usuario.setEmailOtp(codeStr);
        usuario.setEmailOtpExpiry(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);

        String subject = "Tu código de verificación";
        String body = "Tu código de verificación es: " + codeStr + " (válido 10 minutos)";
        emailService.sendSimpleMessage(usuario.getEmail(), subject, body);
        return ResponseEntity.ok("Código enviado");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Falta 'email'");
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        int code = (int) (Math.random() * 900000) + 100000;
        String codeStr = String.valueOf(code);
        usuario.setEmailOtp(codeStr);
        usuario.setEmailOtpExpiry(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);
        String subject = "Código para recuperar contraseña";
        String bodyMsg = "Usa este código para recuperar tu contraseña: " + codeStr + " (válido 15 minutos).";
        try { emailService.sendSimpleMessage(usuario.getEmail(), subject, bodyMsg); } catch (Exception ex) { System.err.println("Error enviando forgot-password email: " + ex.getMessage()); }
        return ResponseEntity.ok("Código enviado");
    }

    @PostMapping("/verify-forgot-password")
    public ResponseEntity<?> verifyForgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        if (email == null || code == null || newPassword == null) return ResponseEntity.badRequest().body("Faltan campos");
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        if (usuario.getEmailOtp() == null || !usuario.getEmailOtp().equals(code) || usuario.getEmailOtpExpiry() == null || usuario.getEmailOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido o caducado");
        }
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuario.setEmailOtp(null);
        usuario.setEmailOtpExpiry(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada");
    }

    @PostMapping("/enable-mfa")
    public ResponseEntity<?> enableMfa(@RequestBody MfaVerifyRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail()).orElseThrow();

        if (usuario.getMfaSecret() == null) {
            return ResponseEntity.badRequest().body("No hay secreto configurado. Llama a /setup-mfa primero.");
        }

        if (!"totp".equalsIgnoreCase(req.getMethod())) {
            return ResponseEntity.badRequest().body("Sólo se admite 'totp' para habilitar MFA en este endpoint.");
        }

        try {
            int code = Integer.parseInt(req.getCode());
            boolean ok = mfaService.verifyCode(usuario.getMfaSecret(), code);
            if (!ok) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código TOTP inválido");
            usuario.setMfaEnabled(true);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("MFA habilitado");
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body("Código inválido");
        }
    }

    @PostMapping("/disable-mfa")
    public ResponseEntity<?> disableMfa(@RequestBody com.ferreteria.v1.models.dto.DisableMfaRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail()).orElseThrow();
        if (!passwordEncoder.matches(req.getPassword(), usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña incorrecta");
        }
        usuario.setMfaEnabled(false);
        usuario.setMfaSecret(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("MFA deshabilitado");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail()).orElseThrow();
        if (!passwordEncoder.matches(req.getOldPassword(), usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña actual incorrecta");
        }
        usuario.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada");
    }
}
