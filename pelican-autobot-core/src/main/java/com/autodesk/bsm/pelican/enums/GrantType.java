package com.autodesk.bsm.pelican.enums;

/**
 * Grant Type for the JSON API
 *
 * @author jains
 */
public enum GrantType {
    FEATURE("FEATURE");

    private String grantType;

    GrantType(final String grantType) {
        this.grantType = grantType;
    }

    public String getValue() {
        return grantType;
    }

}
