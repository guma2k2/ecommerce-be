package com.yas.system.common.response;

public interface ParamError {

    String FIELD_NAME = "{fieldName} cannot be ${validatedValue == null ? 'null' : 'empty'}";

    String INVALID_EMAIL = "{fieldName} is invalid email format";

    String MAX_LENGTH = "Maximum length of {fieldName} is {max} characters";

    String MIN_LENGTH = "Min length of {fieldName} is {min} characters";

    String MIN = "{fieldName} min is {value}";

    String MAX = "{fieldName} max is {value}";

}
