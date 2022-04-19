package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.JobState;

public class JsonApiJobStatus {

    private String type;
    private String id;
    private String created;
    private String lastModified;
    private JobCategory category;
    private JobState state;
    private Links links;

    public static class Links {

        private Wips wips;

        public static class Wips {

            private String related;

            public String getRelated() {
                return related;
            }

            public void setRelated(final String value) {
                this.related = value;
            }
        }

        public Wips getWips() {
            return wips;
        }

        public void setWips(final Wips wips) {
            this.wips = wips;
        }
    }

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

    public JobCategory getJobCategory() {
        return category;
    }

    public void setJobCategory(final JobCategory category) {
        this.category = category;
    }

    public JobState getJobState() {
        return state;
    }

    public void setJobState(final JobState state) {
        this.state = state;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }
}
