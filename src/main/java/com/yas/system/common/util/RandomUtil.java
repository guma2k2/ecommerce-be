package com.yas.system.common.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class RandomUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generatesOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }

    public static String generateRandomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateUuidToken() {
        return UUID.randomUUID().toString();
    }
}
