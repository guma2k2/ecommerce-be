package com.yas.system.auth.internal.mfa;

public interface MfaService {

    String generateMfaSecret(String email);

    boolean verifyTotpCode(String secret, String code);

    String generateQrCodeUri(String email, String secret, String issuer);
}
