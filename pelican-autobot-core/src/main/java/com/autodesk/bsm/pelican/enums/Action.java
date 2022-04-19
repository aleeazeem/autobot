package com.autodesk.bsm.pelican.enums;

/**
 * These enums are intended to be used by Audit log test classes and CSE test classes
 *
 * @author Shweta Hegde
 */
public enum Action {

    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private String displayName;

    Action(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
