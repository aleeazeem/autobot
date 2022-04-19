package com.autodesk.bsm.pelican.enums;

public enum ApplicationFamily {

    AUTO("AUTO_FAMILY"),
    DEMO("Demo"),
    AUTODESK("AUTODESK"),
    TWOFISH("TWOFISH"),
    OTHER("Other");

    private String appFamilyName;

    ApplicationFamily(final String appFamily) {
        this.appFamilyName = appFamily;
    }

    public String getName() {
        return appFamilyName;
    }

}
