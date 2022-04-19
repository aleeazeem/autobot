package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus.Links;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JsonApiJobStatusesData {

    @SerializedName("data")
    private List<JsonApiJobStatus> jobStatuses;

    @SerializedName("errors")
    private List<Errors> errors;

    @SerializedName("links")
    private Links links;

    @SerializedName("totals")
    private Totals totals;

    public List<JsonApiJobStatus> getData() {
        return jobStatuses;
    }

    public void setData(final List<JsonApiJobStatus> jobStatus) {
        this.jobStatuses = jobStatus;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    static class Totals {
        private String pages;
        private String results;

        public String getPages() {
            return pages;
        }

        public void setPages(final String pages) {
            this.pages = pages;
        }

        public String getResults() {
            return results;
        }

        public void setResults(final String results) {
            this.results = results;
        }

    }
}
