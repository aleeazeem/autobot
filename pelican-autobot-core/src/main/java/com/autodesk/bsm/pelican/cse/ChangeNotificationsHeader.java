package com.autodesk.bsm.pelican.cse;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import com.google.gson.annotations.SerializedName;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for CSE message Header
 *
 * @author mandas.
 */
public class ChangeNotificationsHeader extends PelicanPojo {
    private String republishRequester;
    private String category;
    private String plug;
    @SerializedName("rcv at")
    private String rcvAt;
    private String republishFlag;
    private String bufferedAt;
    @SerializedName("pelican-priority")
    private String pelicanPriority;
    @SerializedName("pelican-context")
    private String pelicanContext;

    public String getRepublishRequester() {
        return republishRequester;
    }

    public void setRepublishRequester(final String republishRequester) {
        this.republishRequester = republishRequester;
    }

    public String getcategory() {
        return category;
    }

    public void setcategory(final String category) {
        this.category = category;
    }

    public String getPlug() {
        return plug;
    }

    public void setPlug(final String plug) {
        this.plug = plug;
    }

    public String getRcvAt() {
        return rcvAt;
    }

    public void setRcvAt(final String rcvAt) {
        this.rcvAt = rcvAt;
    }

    public String getRepublishFlag() {
        return republishFlag;
    }

    public void setRepublishFlag(final String republishFlag) {
        this.republishFlag = republishFlag;
    }

    public String getBufferedAt() {
        return bufferedAt;
    }

    public void setBufferedAt(final String bufferedAt) {
        this.bufferedAt = bufferedAt;
    }

    public String getPelicanPriority() {
        return pelicanPriority;
    }

    public void setPelicanPriority(final String pelicanPriority) {
        this.pelicanPriority = pelicanPriority;
    }

    @JsonProperty("pelican-context")
    public String getPelicanContext() {
        return pelicanContext;
    }

    @JsonProperty("pelican-context")
    public void setPelicanContext(final String pelicanContext) {
        this.pelicanContext = pelicanContext;
    }

}
