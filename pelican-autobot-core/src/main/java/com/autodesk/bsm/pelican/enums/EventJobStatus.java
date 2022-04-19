package com.autodesk.bsm.pelican.enums;

/**
 *
 * @author Muhammad
 *
 */
public enum EventJobStatus {
    COMPLETED("COMPLETED"),
    STARTING("STARTING"),
    STARTED("STARTED"),
    STOPPING("STOPPING"),
    STOPPED("STOPPED"),
    FAILED("FAILED"),
    ABANDONED("ABANDONED"),
    UNKNOWN("UNKNOWN");

    private final String status;

    EventJobStatus(final String status) {
        this.status = status;
    }

    public String getName() {
        return status;
    }
}
