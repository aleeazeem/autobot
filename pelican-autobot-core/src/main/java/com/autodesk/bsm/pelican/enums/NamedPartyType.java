package com.autodesk.bsm.pelican.enums;

/**
 * Enum to store all namedPartyType of named party
 */
public enum NamedPartyType {

    ANY("ANY (*)"),
    USER("User"),
    ACTOR("Actor"),
    FULFILLMENT_CENTER("Fulfillment Center"),
    GOVERNMENT_ENTITY("Government Entity");

    private String namedPartyType;

    NamedPartyType(final String namedPartyType) {
        this.namedPartyType = namedPartyType;
    }

    /**
     * Method to get the value
     *
     * @return
     */
    public String getValue() {
        return namedPartyType;
    }

    /**
     * Method to return the default value
     *
     * @return
     */
    public static NamedPartyType getDefault() {
        return USER;
    }

}
