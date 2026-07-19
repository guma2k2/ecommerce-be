package com.yas.system.common.util;

import java.util.Objects;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
