package com.autodesk.bsm.pelican.cse;

import java.util.ArrayList;

public class Data {
    private String type;
    private String id;
    private Attributes attributes;
    private ArrayList<Features> features;

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final Attributes attributes) {
        this.attributes = attributes;
    }

    public ArrayList<Features> getFeatures() {
        return features;
    }

    public void setFeatures(final ArrayList<Features> features) {
        this.features = features;
    }
}
