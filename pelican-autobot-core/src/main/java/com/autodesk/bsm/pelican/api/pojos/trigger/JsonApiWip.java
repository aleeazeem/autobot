package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.autodesk.bsm.pelican.enums.Status;

public class JsonApiWip {

    private String type;
    private String id;
    private String created;
    private String lastModified;
    private String objectType;
    private String objectId;
    private Status state;
    private String notes;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(final String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }

    public Status getWipState() {
        return state;
    }

    public void setWipState(final Status state) {
        this.state = state;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
}
