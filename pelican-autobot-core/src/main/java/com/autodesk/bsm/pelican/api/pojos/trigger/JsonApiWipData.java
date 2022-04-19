package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for Wip
 *
 * @author yin
 */
public class JsonApiWipData {

    @SerializedName("data")
    private List<JsonApiWip> wips = new ArrayList<>();

    @SerializedName("links")
    private JsonApiDataLinks links;

    @SerializedName("errors")
    private List<Errors> errors;

    @SerializedName("totals")
    private Totals totals;

    public List<JsonApiWip> getWips() {
        return wips;
    }

    public void setWips(final List<JsonApiWip> wips) {
        this.wips = wips;
    }

    public JsonApiDataLinks getLinks() {
        if (links == null) {
            links = new JsonApiDataLinks();
        }
        return links;
    }

    public void setLinks(final JsonApiDataLinks links) {
        this.links = links;
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
