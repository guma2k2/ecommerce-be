package com.yas.system.common.util;

import java.security.SecureRandom;

public class RandomUtil {

    public static String generatesOtp() {
        SecureRandom secureRandom = new SecureRandom();
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
