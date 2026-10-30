package com.yas.system.common.validation.validator;

import com.yas.system.common.validation.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

@Slf4j
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startFieldName;
    private String endFieldName;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startFieldName = constraintAnnotation.startField();
        this.endFieldName = constraintAnnotation.endField();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }

        try {
            Object startVal = getFieldValue(object, startFieldName);
            Object endVal = getFieldValue(object, endFieldName);

            if (startVal == null || endVal == null) {
                return true;
            }

            if (startVal instanceof Comparable startComp && endVal instanceof Comparable endComp) {
                return startComp.compareTo(endComp) < 0;
            }
        } catch (Exception e) {
            log.warn("Failed to validate date range for object: {}", object.getClass().getName(), e);
            return false;
        }

        return true;
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Class<?> clazz = object.getClass();

        // 1. Try record component accessor if it's a Java record
        if (clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                if (rc.getName().equals(fieldName)) {
                    Method accessor = rc.getAccessor();
                    accessor.setAccessible(true);
                    return accessor.invoke(object);
                }
            }
        }

        // 2. Try getter method (e.g., getStartDate() or startDate())
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            Method method = clazz.getDeclaredMethod(getterName);
            method.setAccessible(true);
            return method.invoke(object);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method method = clazz.getDeclaredMethod(fieldName);
            method.setAccessible(true);
            return method.invoke(object);
        } catch (NoSuchMethodException ignored) {
        }

        // 3. Try direct field
        var field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
