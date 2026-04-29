package com.yas.system.common.constant;

public class AppConstant {

    public static final String BEARER  = "Bearer ";

    enum ConsoleType {
        ADMIN,
        CUSTOMER
    }

    enum Status{
        PENDING("PENDING", 2),
        ACTIVE("ACTIVE", 1),
        INACTIVE("INACTIVE", 0);

        private String name;
        private int value;
        Status(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
