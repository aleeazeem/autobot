package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * This is a pojo class for <pfx3:ListOfLineItem> in FulfillmentCallBack API request body
 *
 * @author kishor
 */
public class ListOfLineItem {

    private List<LineItem> listOfLineItems;

    /**
     * @return the listOfLineItems
     */
    public List<LineItem> getListOfLineItems() {
        return listOfLineItems;
    }

    /**
     * @param listOfLineItems the listOfLineItems to set
     */
    @XmlElement(name = "LineItem", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setListOfLineItems(final List<LineItem> listOfLineItems) {
        this.listOfLineItems = listOfLineItems;
    }
}
