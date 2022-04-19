package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.google.gson.annotations.SerializedName;

public class JsonApiJobStatusData {

    @SerializedName("data")
    private JsonApiJobStatus jobStatus;

    public JsonApiJobStatus getData() {
        return jobStatus;
    }

    public void setData(final JsonApiJobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }
}
