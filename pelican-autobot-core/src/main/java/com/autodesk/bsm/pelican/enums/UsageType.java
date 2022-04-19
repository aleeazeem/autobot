package com.autodesk.bsm.pelican.enums;

public enum UsageType {
    COM("Commercial", "COM", 0),
    EDU("Education", "EDU", 1),
    NCM("Non Commercial", "NCM", 2),
    TRL("Trial", "TRL", 3),
    GOV("Government", "GOV", 4);

    private String displayName;
    private String uploadName;
    private int value;

    UsageType(final String displayName, final String uploadName, final int value) {
        this.displayName = displayName;
        this.uploadName = uploadName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUploadName() {
        return uploadName;
    }

    public int getNumber() {
        return value;
    }

    public String toString() {
        return displayName;
    }
}
