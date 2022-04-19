package com.autodesk.bsm.pelican.api.pojos.json;

public class ProductLineData {

    private String type;
    private String name;
    private String externalKey;
    private String id;
    private Boolean isActive;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
    }
}
