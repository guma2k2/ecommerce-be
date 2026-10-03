package com.yas.system.common.util;

import java.util.Objects;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }

    /**
     * Converts an HTML string into clean, normalized plain text.
     * Removes HTML tags, replaces breaks with spaces, decodes common HTML entities,
     * and normalizes whitespace.
     */
    public static String cleanHtmlToPlainText(String html) {
        return cleanHtmlToPlainText(html, 0);
    }

    /**
     * Converts an HTML string into clean, normalized plain text and optionally truncates it.
     */
    public static String cleanHtmlToPlainText(String html, int maxLength) {
        if (isBlank(html)) {
            return "";
        }

        String text = html
                .replaceAll("(?i)<br\\s*/?>", " ")
                .replaceAll("(?i)</p>", " ")
                .replaceAll("(?i)</li>", " ")
                .replaceAll("(?i)</div>", " ")
                .replaceAll("(?i)<h[1-6][^>]*>", " ")
                .replaceAll("(?i)</h[1-6]>", " ")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();

        if (maxLength > 0 && text.length() > maxLength) {
            return text.substring(0, maxLength);
        }
        return text;
    }
}
