package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.Status;

import java.util.ArrayList;
import java.util.List;

public class BicRelease extends BaseEntity implements Cloneable {

    private String application;
    private String downloadRelease;
    private String subsPlanProductLine;
    private String downloadProductLine;
    private Status status;
    private boolean clicEnabled;
    private String legacySku;
    private String fcsDate;
    private boolean ignoreEmailNotification;

    // System created properties
    private String createdBy;
    private String createdOn;
    private String updatedBy;
    private String updatedOn;

    public String getApplication() {
        return application;
    }

    public BicRelease setApplication(final String value) {
        this.application = value;
        return this;
    }

    public String getDownloadRelease() {
        return downloadRelease;
    }

    public BicRelease setDownloadRelease(final String value) {
        this.downloadRelease = value;
        return this;
    }

    public String getSubsPlanProductLine() {
        return subsPlanProductLine;
    }

    public BicRelease setSubsPlanProductLine(final String value) {
        this.subsPlanProductLine = value;
        return this;

    }

    public String getDownloadProductLine() {
        return downloadProductLine;
    }

    public BicRelease setDownloadProductLine(final String value) {
        this.downloadProductLine = value;
        return this;

    }

    public Status getStatus() {
        return status;
    }

    public BicRelease setStatus(final Status value) {
        this.status = value;
        return this;

    }

    public boolean isClicEnabled() {
        return clicEnabled;
    }

    public BicRelease setClic(final boolean enabled) {
        this.clicEnabled = enabled;
        return this;
    }

    public String getLegacySku() {
        return legacySku;
    }

    public BicRelease setLegacySku(final String value) {
        this.legacySku = value;
        return this;
    }

    public String getFcsDate() {
        return fcsDate;
    }

    public BicRelease setFcsDate(final String value) {
        this.fcsDate = value;
        return this;
    }

    public boolean getIgnoreEmailNotification() {
        return ignoreEmailNotification;
    }

    public BicRelease setIgnoredEmailNotification(final boolean enabled) {
        this.ignoreEmailNotification = enabled;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public BicRelease setCreatedBy(String value) {
        // Remove user's id if present
        final int index = value.indexOf("(");
        if (index > 0) {
            value = value.substring(0, index - 1).trim();
        }
        this.createdBy = value;
        return this;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public BicRelease setCreatedOn(final String value) {
        this.createdOn = value;
        return this;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public BicRelease setUpdatedBy(String value) {

        if (value != null) {
            // Remove user's id if present
            final int index = value.indexOf("(");
            if (index > 0) {
                value = value.substring(0, index - 1).trim();
            }
            this.updatedBy = value;
        }
        return this;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public BicRelease setUpdatedOn(final String value) {
        this.updatedOn = value;
        return this;
    }

    public List<String> getProperties() {
        final List<String> properties = new ArrayList<>();

        properties.add("application");
        properties.add("downloadRelease");
        properties.add("subsPlanProductLine");
        properties.add("downloadProductLine");
        properties.add("status");
        properties.add("clicEnabled");
        properties.add("legacySku");
        properties.add("fcsDate");
        properties.add("ignoreEmailNotification");
        properties.add("createdBy");
        properties.add("updatedBy");
        return properties;
    }

    public List<String> getDateProperties() {
        final List<String> properties = new ArrayList<>();
        properties.add("createdOn");
        properties.add("updatedOn");

        return properties;
    }

    public Object clone() {
        Object cloned = null;
        try {
            cloned = super.clone();
        } catch (final CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cloned;
    }

}
