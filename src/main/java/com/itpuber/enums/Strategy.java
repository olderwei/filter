package com.itpuber.enums;

/**
 * Created by yoyo on 17/5/20.
 */
public enum Strategy {

    GUAVA_UNIT_LIMIT("com.itpuber.enums.Strategy.GUAVA_UNIT_LIMIT"),

    GUAVA_RATE_LIMIT("com.itpuber.enums.Strategy.GUAVA_RATE_LIMIT"),

    PERCENT_LIMIT("com.itpuber.enums.Strategy.PERCENT_LIMIT"),

    REDIS("com.itpuber.enums.Strategy.REDIS");

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private Strategy(String value) {
        this.value = value;
    }
}
