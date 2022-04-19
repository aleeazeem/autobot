package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class LineItems {

    private List<LineItem> lineItems;

    public List<LineItem> getLineItems() {

        return lineItems;
    }

    @XmlElement(name = "lineItem")
    public void setLineItems(final List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }
}
