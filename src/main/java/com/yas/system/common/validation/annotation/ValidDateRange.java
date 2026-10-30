package com.yas.system.common.validation.annotation;

import com.yas.system.common.validation.validator.ValidDateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that startDate is strictly before endDate on a container object or record.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "Start date must be strictly before end date";

    String startField() default "startDate";

    String endField() default "endDate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

