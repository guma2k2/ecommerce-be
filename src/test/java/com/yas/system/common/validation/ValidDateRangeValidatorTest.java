package com.yas.system.common.validation;

import com.yas.system.common.validation.annotation.ValidDateRange;
import com.yas.system.common.validation.validator.ValidDateRangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ValidDateRangeValidatorTest {

    private ValidDateRangeValidator validator;

    @ValidDateRange
    public record TestRecord(ZonedDateTime startDate, ZonedDateTime endDate) {}

    @BeforeEach
    void setUp() {
        validator = new ValidDateRangeValidator();
        ValidDateRange annotation = TestRecord.class.getAnnotation(ValidDateRange.class);
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("null object returns true")
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("startDate before endDate returns true")
    void isValid_startBeforeEnd_returnsTrue() {
        ZonedDateTime now = ZonedDateTime.now();
        TestRecord record = new TestRecord(now, now.plusDays(1));
        assertThat(validator.isValid(record, null)).isTrue();
    }

    @Test
    @DisplayName("startDate after endDate returns false")
    void isValid_startAfterEnd_returnsFalse() {
        ZonedDateTime now = ZonedDateTime.now();
        TestRecord record = new TestRecord(now.plusDays(1), now);
        assertThat(validator.isValid(record, null)).isFalse();
    }

    @Test
    @DisplayName("startDate equal to endDate returns false")
    void isValid_startEqualsEnd_returnsFalse() {
        ZonedDateTime now = ZonedDateTime.now();
        TestRecord record = new TestRecord(now, now);
        assertThat(validator.isValid(record, null)).isFalse();
    }

    @Test
    @DisplayName("either date null returns true (handled by @NotNull)")
    void isValid_oneNull_returnsTrue() {
        TestRecord record = new TestRecord(ZonedDateTime.now(), null);
        assertThat(validator.isValid(record, null)).isTrue();
    }
}
