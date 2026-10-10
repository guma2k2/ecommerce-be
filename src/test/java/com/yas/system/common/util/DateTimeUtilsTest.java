package com.yas.system.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeUtilsTest {

    private static final ZonedDateTime SAMPLE_DATE_TIME = ZonedDateTime.of(
            2026, 10, 10, 21, 45, 30, 0, ZoneId.of("+07:00")
    );

    @Test
    @DisplayName("Private constructor should throw UnsupportedOperationException")
    void privateConstructor_throwsException() throws NoSuchMethodException {
        Constructor<DateTimeUtils> constructor = DateTimeUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("format(ZonedDateTime) should return ISO-8601 offset formatted string")
    void format_withZonedDateTime_returnsIsoString() {
        String result = DateTimeUtils.format(SAMPLE_DATE_TIME);
        assertThat(result).isEqualTo("2026-10-10T21:45:30+07:00");
    }

    @Test
    @DisplayName("format(ZonedDateTime) should return null when input is null")
    void format_withNull_returnsNull() {
        assertThat(DateTimeUtils.format(null)).isNull();
    }

    @Test
    @DisplayName("format(ZonedDateTime, formatter) should format correctly with custom formatter")
    void format_withCustomFormatter_returnsFormattedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String result = DateTimeUtils.format(SAMPLE_DATE_TIME, formatter);
        assertThat(result).isEqualTo("10/10/2026 21:45");
    }

    @Test
    @DisplayName("format(ZonedDateTime, formatter) should return null when either input is null")
    void format_withCustomFormatter_nullHandling() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertThat(DateTimeUtils.format(null, formatter)).isNull();
        assertThat(DateTimeUtils.format(SAMPLE_DATE_TIME, (DateTimeFormatter) null)).isNull();
    }

    @Test
    @DisplayName("format(ZonedDateTime, pattern) should format with pattern string")
    void format_withPattern_returnsFormattedString() {
        String result = DateTimeUtils.format(SAMPLE_DATE_TIME, "yyyy/MM/dd");
        assertThat(result).isEqualTo("2026/10/10");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("format(ZonedDateTime, pattern) returns null when pattern is blank")
    void format_withBlankPattern_returnsNull(String pattern) {
        assertThat(DateTimeUtils.format(SAMPLE_DATE_TIME, pattern)).isNull();
        assertThat(DateTimeUtils.format(null, pattern)).isNull();
    }

    @Test
    @DisplayName("formatDateTime should format as 'yyyy-MM-dd HH:mm:ss'")
    void formatDateTime_returnsExpectedFormat() {
        String result = DateTimeUtils.formatDateTime(SAMPLE_DATE_TIME);
        assertThat(result).isEqualTo("2026-10-10 21:45:30");
        assertThat(DateTimeUtils.formatDateTime(null)).isNull();
    }

    @Test
    @DisplayName("formatDate should format as 'yyyy-MM-dd'")
    void formatDate_returnsExpectedFormat() {
        String result = DateTimeUtils.formatDate(SAMPLE_DATE_TIME);
        assertThat(result).isEqualTo("2026-10-10");
        assertThat(DateTimeUtils.formatDate(null)).isNull();
    }

    @Test
    @DisplayName("formatTime should format as 'HH:mm:ss'")
    void formatTime_returnsExpectedFormat() {
        String result = DateTimeUtils.formatTime(SAMPLE_DATE_TIME);
        assertThat(result).isEqualTo("21:45:30");
        assertThat(DateTimeUtils.formatTime(null)).isNull();
    }

    @Test
    @DisplayName("formatInstant should format Instant at UTC")
    void formatInstant_returnsIsoStringInUtc() {
        Instant instant = SAMPLE_DATE_TIME.toInstant();
        String result = DateTimeUtils.formatInstant(instant);
        assertThat(result).isEqualTo("2026-10-10T14:45:30Z");
        assertThat(DateTimeUtils.formatInstant(null)).isNull();
    }

    @Test
    @DisplayName("parse(String) should parse ISO-8601 offset string correctly")
    void parse_isoString_returnsZonedDateTime() {
        String text = "2026-10-10T21:45:30+07:00";
        ZonedDateTime result = DateTimeUtils.parse(text);
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonthValue()).isEqualTo(10);
        assertThat(result.getDayOfMonth()).isEqualTo(10);
        assertThat(result.getHour()).isEqualTo(21);
        assertThat(result.getMinute()).isEqualTo(45);
        assertThat(result.getSecond()).isEqualTo(30);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("parse(String) returns null for null or blank input")
    void parse_nullOrBlank_returnsNull(String input) {
        assertThat(DateTimeUtils.parse(input)).isNull();
        assertThat(DateTimeUtils.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)).isNull();
        assertThat(DateTimeUtils.parse(input, "yyyy-MM-dd", ZoneOffset.UTC)).isNull();
    }

    @Test
    @DisplayName("toUtc should convert ZonedDateTime to UTC offset")
    void toUtc_convertsToUtcZone() {
        ZonedDateTime utcDateTime = DateTimeUtils.toUtc(SAMPLE_DATE_TIME);
        assertThat(utcDateTime).isNotNull();
        assertThat(utcDateTime.getZone()).isEqualTo(ZoneOffset.UTC);
        assertThat(utcDateTime.getHour()).isEqualTo(14);
        assertThat(DateTimeUtils.toUtc(null)).isNull();
    }

    @Test
    @DisplayName("fromInstant should convert Instant to ZonedDateTime in target zone")
    void fromInstant_convertsCorrectly() {
        Instant instant = SAMPLE_DATE_TIME.toInstant();
        ZonedDateTime result = DateTimeUtils.fromInstant(instant, ZoneId.of("+07:00"));
        assertThat(result).isNotNull();
        assertThat(result.getHour()).isEqualTo(21);
        assertThat(DateTimeUtils.fromInstant(null, ZoneId.of("+07:00"))).isNull();
    }
}
