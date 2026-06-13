package com.yas.system.auth.internal.mfa.impl;

import com.yas.system.auth.internal.mfa.MfaService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class MfaServiceImpl implements MfaService {

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public MfaServiceImpl() {
        this.secretGenerator = new DefaultSecretGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    @Override
    public String generateMfaSecret(String email) {
        // Returns a base32 encoded 32-character secret
        return secretGenerator.generate();
    }

    @Override
    public boolean verifyTotpCode(String secret, String code) {
        // Validates the code using the configured system time provider
        return codeVerifier.isValidCode(secret, code);
    }

    @Override
    public String generateQrCodeUri(String email, String secret, String issuer) {
        // Builds the otpauth:// URL that can be shown as a QR code or provided as a link
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
                
        return data.getUri();
    }
}
