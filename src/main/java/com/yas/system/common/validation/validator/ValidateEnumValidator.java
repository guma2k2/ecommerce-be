package com.yas.system.common.validation.validator;

import com.yas.system.common.validation.annotation.ValidateEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class ValidateEnumValidator implements ConstraintValidator<ValidateEnum, String> {

    private List<String> acceptedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidateEnum annotation) {
        ignoreCase = annotation.ignoreCase();
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return acceptedValues.stream().anyMatch(enumValue ->
                ignoreCase ? enumValue.equalsIgnoreCase(value) : enumValue.equals(value)
        );
    }
}
