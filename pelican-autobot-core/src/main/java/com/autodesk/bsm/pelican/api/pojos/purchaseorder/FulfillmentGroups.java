package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class FulfillmentGroups {

    private List<FulfillmentGroup> fulfillmentGroups;

    public List<FulfillmentGroup> getFulfillmentGroups() {
        if (fulfillmentGroups == null) {
            fulfillmentGroups = new ArrayList<>();
        }
        return fulfillmentGroups;
    }

    @XmlElement(name = "fulfillmentGroup")
    public void setFulfillmentGroups(final List<FulfillmentGroup> groups) {
        this.fulfillmentGroups = groups;
    }
}
