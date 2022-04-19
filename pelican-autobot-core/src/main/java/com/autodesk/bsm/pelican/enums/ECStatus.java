package com.autodesk.bsm.pelican.enums;

public enum ECStatus {

    UNVERIFIED("UNVERIFIED", "Unverified"),
    REVIEW("REVIEW", "Review"),
    BLOCK("BLOCK", "Block"),
    REOPEN("REOPEN", "Reopen"),
    ACCEPT("ACCEPT", "Accept"),
    HARDBLOCK("HARDBLOCK", "Hard Block");

    private String displayName;
    private String name;

    ECStatus(final String displayName, final String name) {
        this.displayName = displayName;
        this.name = name;
    }

    /**
     * This method returns Admin Tool value
     *
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * This method returns Api value
     *
     * @return String
     */
    public String getName() {
        return name;
    }

}
