package com.autodesk.bsm.pelican.api.pojos.trigger;

import java.util.List;

public class JsonApiJobStatuses {

    private List<JsonApiJobStatus> jobStatuses;

    public List<JsonApiJobStatus> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(final List<JsonApiJobStatus> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }
}
