package com.yas.system.common.constant;

public class AppConstant {

    public static final String BEARER  = "Bearer ";
    public static final String PRODUCT_VARIANT_DEFAULT_TITLE = "Default Title";
    public static final String DEFAULT_LANGUAGE = "en";

    public enum ConsoleType {
        BACKOFFICE,
        STOREFRONT
    }

    enum Status{
        PENDING,
        ACTIVE,
        INACTIVE
    }
}
