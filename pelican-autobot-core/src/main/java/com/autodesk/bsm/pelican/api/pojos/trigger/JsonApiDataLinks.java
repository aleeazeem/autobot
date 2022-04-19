package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.google.gson.annotations.SerializedName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger's Data Links
 *
 * @author yin
 */
public class JsonApiDataLinks {

    @SerializedName("first")
    private String firstLink;

    @SerializedName("last")
    private String lastLink;

    @SerializedName("next")
    private String nextLink;
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonApiDataLinks.class.getSimpleName());

    public String getFirst() {
        return firstLink;
    }

    public void setFirstLink(final String value) {
        this.firstLink = value;
    }

    public String getLastLink() {
        return lastLink;
    }

    public void setLastLink(final String value) {
        this.lastLink = value;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(final String value) {
        this.nextLink = value;
    }

    public int getTotalPages() {
        int lastPage = 0;
        if (lastLink != null) {
            final int pos = lastLink.indexOf("page[number]");
            if (pos > 0) {
                final String pageNumber = lastLink.split("=")[1];
                lastPage = Integer.parseInt(pageNumber);
            }
        }
        LOGGER.info("Last page is " + lastPage);
        return lastPage;
    }
}
