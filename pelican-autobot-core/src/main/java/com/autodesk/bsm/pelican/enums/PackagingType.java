package com.autodesk.bsm.pelican.enums;

public enum PackagingType {

    INDUSTRY_COLLECTION(0, "Industry Collection"),
    NONE(1, "None"),
    IC(2, "IC"),
    NULL(3, ""),
    VG(3, "VG"),
    VERTICAL_GROUPING(3, "Vertical Grouping");

    private int value;
    private String displayName;

    PackagingType(final int value, final String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getNumber() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
