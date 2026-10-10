package com.yas.system.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Utility class for formatting and parsing {@link ZonedDateTime} instances.
 * <p>
 * Ensures null-safe conversions between {@link ZonedDateTime} and formatted {@link String}
 * representations, preventing boilerplate null-checks in DTOs and API responses.
 */
public final class DateTimeUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

    public static final ZoneId UTC_ZONE = ZoneOffset.UTC;

    private DateTimeUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Formats a {@link ZonedDateTime} into an ISO-8601 offset string (e.g., "2026-10-10T21:49:42+07:00").
     *
     * @param zonedDateTime the date-time to format, may be null
     * @return the formatted ISO string, or {@code null} if input is null
     */
    public static String format(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, ISO_OFFSET_FORMATTER);
    }

    /**
     * Formats a {@link ZonedDateTime} using a custom {@link DateTimeFormatter}.
     *
     * @param zonedDateTime the date-time to format, may be null
     * @param formatter     the formatter to use, may be null
     * @return the formatted string, or {@code null} if zonedDateTime or formatter is null
     */
    public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter formatter) {
        if (Objects.isNull(zonedDateTime) || Objects.isNull(formatter)) {
            return null;
        }
        return zonedDateTime.format(formatter);
    }

    /**
     * Formats a {@link ZonedDateTime} using a custom pattern string.
     *
     * @param zonedDateTime the date-time to format, may be null
     * @param pattern       the format pattern (e.g., "yyyy-MM-dd HH:mm:ss"), may be null or blank
     * @return the formatted string, or {@code null} if zonedDateTime is null or pattern is blank
     */
    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        if (Objects.isNull(zonedDateTime) || Objects.isNull(pattern) || pattern.isBlank()) {
            return null;
        }
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a {@link ZonedDateTime} into standard date-time string ("yyyy-MM-dd HH:mm:ss").
     *
     * @param zonedDateTime the date-time to format, may be null
     * @return formatted date-time string, or {@code null} if input is null
     */
    public static String formatDateTime(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, DATE_TIME_FORMATTER);
    }

    /**
     * Formats a {@link ZonedDateTime} into standard date-only string ("yyyy-MM-dd").
     *
     * @param zonedDateTime the date-time to format, may be null
     * @return formatted date string, or {@code null} if input is null
     */
    public static String formatDate(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, DATE_FORMATTER);
    }

    /**
     * Formats a {@link ZonedDateTime} into standard time-only string ("HH:mm:ss").
     *
     * @param zonedDateTime the date-time to format, may be null
     * @return formatted time string, or {@code null} if input is null
     */
    public static String formatTime(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, TIME_FORMATTER);
    }

    /**
     * Formats an {@link Instant} into an ISO-8601 offset string at UTC.
     *
     * @param instant the instant to format, may be null
     * @return formatted ISO string, or {@code null} if input is null
     */
    public static String formatInstant(Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }
        return format(instant.atZone(UTC_ZONE));
    }

    /**
     * Parses an ISO-8601 offset date-time string into a {@link ZonedDateTime}.
     *
     * @param text the ISO string to parse, may be null or blank
     * @return parsed {@link ZonedDateTime}, or {@code null} if text is null or blank
     */
    public static ZonedDateTime parse(String text) {
        return parse(text, ISO_OFFSET_FORMATTER);
    }

    /**
     * Parses a date-time string using a custom {@link DateTimeFormatter}.
     *
     * @param text      the text to parse, may be null or blank
     * @param formatter the formatter to use, may be null
     * @return parsed {@link ZonedDateTime}, or {@code null} if text or formatter is null
     */
    public static ZonedDateTime parse(String text, DateTimeFormatter formatter) {
        if (Objects.isNull(text) || text.isBlank() || Objects.isNull(formatter)) {
            return null;
        }
        return ZonedDateTime.parse(text, formatter);
    }

    /**
     * Parses a date-time string using a custom pattern string and an optional zone.
     *
     * @param text    the text to parse, may be null or blank
     * @param pattern the format pattern, may be null or blank
     * @param zoneId  the timezone to assign (defaults to UTC if null)
     * @return parsed {@link ZonedDateTime}, or {@code null} if text or pattern is null/blank
     */
    public static ZonedDateTime parse(String text, String pattern, ZoneId zoneId) {
        if (Objects.isNull(text) || text.isBlank() || Objects.isNull(pattern) || pattern.isBlank()) {
            return null;
        }
        ZoneId zone = Objects.nonNull(zoneId) ? zoneId : UTC_ZONE;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zone);
        return ZonedDateTime.parse(text, formatter);
    }

    /**
     * Adjusts a {@link ZonedDateTime} to the UTC timezone while preserving the same instant.
     *
     * @param zonedDateTime the date-time to convert, may be null
     * @return {@link ZonedDateTime} in UTC, or {@code null} if input is null
     */
    public static ZonedDateTime toUtc(ZonedDateTime zonedDateTime) {
        if (Objects.isNull(zonedDateTime)) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(UTC_ZONE);
    }

    /**
     * Converts an {@link Instant} to {@link ZonedDateTime} using the specified timezone.
     *
     * @param instant the instant to convert, may be null
     * @param zoneId  the target zone, defaults to UTC if null
     * @return {@link ZonedDateTime}, or {@code null} if instant is null
     */
    public static ZonedDateTime fromInstant(Instant instant, ZoneId zoneId) {
        if (Objects.isNull(instant)) {
            return null;
        }
        return instant.atZone(Objects.nonNull(zoneId) ? zoneId : UTC_ZONE);
    }
}
