package com.yas.system.common.validation.annotation;

import com.yas.system.common.validation.validator.NullOrNotBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a CharSequence is either null or non-blank.
 * Useful for partial updates (PATCH / optional PUT fields) where null means "do not change",
 * but providing an empty string or whitespace-only is invalid.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {

    String message() default "Field must be either null or non-blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

