package com.yas.system.common.exception;

import com.yas.system.common.response.ParamError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Resolves FIELD_NAME with null rejected value")
    void resolveErrorMessage_fieldNameNull() {
        FieldError error = new FieldError("user", "email", null, false, null, null, ParamError.FIELD_NAME);
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("email cannot be null");
    }

    @Test
    @DisplayName("Resolves FIELD_NAME with empty rejected value")
    void resolveErrorMessage_fieldNameEmpty() {
        FieldError error = new FieldError("user", "name", "", false, null, null, ParamError.FIELD_NAME);
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("name cannot be empty");
    }

    @Test
    @DisplayName("Resolves FIELD_NAME when EL already evaluated by Bean Validation")
    void resolveErrorMessage_fieldNameAlreadyEvaluated() {
        FieldError error = new FieldError("user", "name", "", false, null, null, "{fieldName} cannot be empty");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("name cannot be empty");
    }

    @Test
    @DisplayName("Resolves INVALID_EMAIL with field name")
    void resolveErrorMessage_invalidEmail() {
        FieldError error = new FieldError("user", "email", "invalid", false, null, null, ParamError.INVALID_EMAIL);
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("email is invalid email format");
    }

    @Test
    @DisplayName("Resolves MAX_LENGTH with field name and interpolated max")
    void resolveErrorMessage_maxLength() {
        FieldError error = new FieldError("product", "name", "toolong", false, null, null, "Maximum length of {fieldName} is 100 characters");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("Maximum length of name is 100 characters");
    }

    @Test
    @DisplayName("Resolves MIN_LENGTH with field name")
    void resolveErrorMessage_minLength() {
        FieldError error = new FieldError("user", "password", "123", false, null, null, "Min length of {fieldName} is 8 characters");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("Min length of password is 8 characters");
    }

    @Test
    @DisplayName("Resolves MIN with field name")
    void resolveErrorMessage_minValue() {
        FieldError error = new FieldError("item", "quantity", 0, false, null, null, "{fieldName} min is 1");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("quantity min is 1");
    }

    @Test
    @DisplayName("Resolves MAX with field name")
    void resolveErrorMessage_maxValue() {
        FieldError error = new FieldError("item", "quantity", 200, false, null, null, "{fieldName} max is 100");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("quantity max is 100");
    }

    @Test
    @DisplayName("Falls back to field name is invalid when message is null or blank")
    void resolveErrorMessage_blankMessage() {
        FieldError error = new FieldError("item", "title", null, false, null, null, "");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("title is invalid");
    }

    @Test
    @DisplayName("Handles ObjectError when not a FieldError")
    void resolveErrorMessage_objectError() {
        ObjectError error = new ObjectError("order", "Order validation failed");
        String resolved = handler.resolveErrorMessage(error);
        assertThat(resolved).isEqualTo("Order validation failed");
    }
}
