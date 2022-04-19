package com.autodesk.bsm.pelican.cse;

public class ChangeNotificationData {

    private String id;
    private String type;
    private ChangeNotificationAttributes attributes;
    private ChangeNotificationRelationships relationships;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public ChangeNotificationAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final ChangeNotificationAttributes attributes) {
        this.attributes = attributes;
    }

    public ChangeNotificationRelationships getRelationships() {
        return relationships;
    }

    public void setRelationships(final ChangeNotificationRelationships relationships) {
        this.relationships = relationships;
    }

}
