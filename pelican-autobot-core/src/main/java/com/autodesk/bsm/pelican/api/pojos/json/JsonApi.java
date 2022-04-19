package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.ArrayList;
import java.util.List;

public class JsonApi {

    private EntityType type;
    private String id;

    public static class LinkageArray {
        private List<JsonApi> linkage;

        public List<JsonApi> getLinkage() {
            if (linkage == null) {
                linkage = new ArrayList<>();
            }
            return linkage;
        }

        public void setLinkage(final List<JsonApi> linkage) {
            this.linkage = linkage;
        }
    }

    public static class Linkage {
        private JsonApi linkage;

        public JsonApi getLinkage() {
            return linkage;
        }

        public void setLinkage(final JsonApi linkage) {
            this.linkage = linkage;
        }
    }

    public EntityType getType() {
        return type;
    }

    public void setType(final EntityType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
