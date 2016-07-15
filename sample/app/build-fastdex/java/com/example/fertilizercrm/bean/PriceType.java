package com.example.fertilizercrm.bean;

/**
 * Created by tong on 16/1/4.
 * 价格条目
 */
public enum PriceType {
    /**
     * 自提
     */
    PICKUP("自提"),
    /**
     * 送到
     */
    DELIVERY("送到");

    private String description;

    PriceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public PriceType otherType() {
        if (this == PICKUP) {
            return DELIVERY;
        }
        else if (this == DELIVERY) {
            return PICKUP;
        }

        return null;
    }

    public static PriceType defaultType() {
        return DELIVERY;
    }
}
