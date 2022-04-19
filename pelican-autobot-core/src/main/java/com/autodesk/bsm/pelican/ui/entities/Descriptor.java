package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;

public class Descriptor {
    private String appFamily;
    private DescriptorEntityTypes entity;
    private String groupName;
    private String fieldName;
    private String apiName;
    private String localized;
    private String maxLength;
    private String otherGroupName;
    private String Id;

    /**
     * @return the id
     */
    public String getId() {
        return Id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        Id = id;
    }

    public String getAppFamily() {
        return appFamily;
    }

    public void setAppFamily(final String appFamily) {
        this.appFamily = appFamily;
    }

    public DescriptorEntityTypes getEntity() {
        return entity;
    }

    public void setEntity(final DescriptorEntityTypes entity) {
        this.entity = entity;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(final String apiName) {
        this.apiName = apiName;
    }

    public String getLocalized() {
        return localized;
    }

    public void setLocalized(final String localized) {
        this.localized = localized;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final String maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return the otherGroupName
     */
    public String getOtherGroupName() {
        return otherGroupName;
    }

    /**
     * @param otherGroupName the otherGroupName to set
     */
    public void setOtherGroupName(final String otherGroupName) {
        this.otherGroupName = otherGroupName;
    }

}
