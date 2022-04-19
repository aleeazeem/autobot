package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.Status;

public class BicReleaseAdvSearch extends BaseEntity {

    private String application;
    private String downloadRelease;
    private String subsPlanProductLine;
    private String downloadProductLine;
    private Status status;
    private String legacySku;
    private String fcsStartDate;
    private String fcsEndDate;

    public String getApplication() {
        return application;
    }

    public void setApplication(final String value) {
        this.application = value;
    }

    public String getDownloadRelease() {
        return downloadRelease;
    }

    public void setDownloadRelease(final String value) {
        this.downloadRelease = value;
    }

    public String getSubPlanProductLine() {
        return subsPlanProductLine;
    }

    public void setSubPlanProductLine(final String value) {
        this.subsPlanProductLine = value;

    }

    public String getDownloadProductLine() {
        return downloadProductLine;
    }

    public void setDownloadProductLine(final String value) {
        this.downloadProductLine = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status value) {
        this.status = value;
    }

    public String getLegacySku() {
        return legacySku;
    }

    public void setLegacySku(final String value) {
        this.legacySku = value;
    }

    public String getFcsStartDate() {
        return fcsStartDate;
    }

    public void setFcsStartDate(final String value) {
        this.fcsStartDate = value;
    }

    public String getFcsEndDate() {
        return fcsEndDate;
    }

    public void setFcsEndDate(final String value) {
        this.fcsEndDate = value;
    }

}
