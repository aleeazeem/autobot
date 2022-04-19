package com.autodesk.bsm.pelican.api.pojos;

public class DRWIPData {
    private Integer objectId;
    private Integer state;
    private Integer jobStatusId;
    private String notes;

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(final Integer objectId) {
        this.objectId = objectId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(final Integer state) {
        this.state = state;
    }

    public Integer getJobStatusId() {
        return jobStatusId;
    }

    public void setJobStatusId(final Integer jobStatusId) {
        this.jobStatusId = jobStatusId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
}
