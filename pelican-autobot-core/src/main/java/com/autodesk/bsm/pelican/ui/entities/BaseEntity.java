package com.autodesk.bsm.pelican.ui.entities;

public class BaseEntity {

    private String applFamily;
    private String id;

    public String getApplicationFamily() {
        return applFamily;
    }

    public void setApplicationFamily(final String applFamily) {
        this.applFamily = applFamily;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
