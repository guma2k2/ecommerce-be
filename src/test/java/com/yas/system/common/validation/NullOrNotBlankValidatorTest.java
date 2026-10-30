package com.yas.system.common.validation;

import com.yas.system.common.validation.annotation.NullOrNotBlank;
import com.yas.system.common.validation.validator.NullOrNotBlankValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullOrNotBlankValidatorTest {

    private NullOrNotBlankValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NullOrNotBlankValidator();
    }

    @Test
    @DisplayName("null string should be valid")
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("non-empty string should be valid")
    void isValid_nonEmpty_returnsTrue() {
        assertThat(validator.isValid("valid string", null)).isTrue();
    }

    @Test
    @DisplayName("empty string should be invalid")
    void isValid_empty_returnsFalse() {
        assertThat(validator.isValid("", null)).isFalse();
    }

    @Test
    @DisplayName("whitespace-only string should be invalid")
    void isValid_whitespace_returnsFalse() {
        assertThat(validator.isValid("   ", null)).isFalse();
    }
}

