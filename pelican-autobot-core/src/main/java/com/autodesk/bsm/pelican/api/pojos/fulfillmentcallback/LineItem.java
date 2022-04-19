package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * This is a pojo class for <pfx3:LineItem> in FulfillmentCallBack API request body
 *
 * @author kishor
 */
public class LineItem {
    private String ExternalRefNumber;
    private ListOfCurrentAsset listOfCurrentAsset;

    /**
     * Method to return the externalRefNumber in the FulfillmentCallback API request
     *
     * @return String
     */
    public String getExternalRefNumber() {
        return ExternalRefNumber;
    }

    /**
     * Method to set the externalRefNumber in the FulfillmentCallback API request
     */
    @XmlElement(name = "ExternalRefNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setExternalRefNumber(final String externalRefNumber) {
        ExternalRefNumber = externalRefNumber;
    }

    /**
     * @return the listOfCurrentAsset
     */
    public ListOfCurrentAsset getListOfCurrentAsset() {
        return listOfCurrentAsset;
    }

    /**
     * @param listOfCurrentAsset the listOfCurrentAsset to set
     */
    @XmlElement(name = "ListOfCurrentAsset", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setListOfCurrentAsset(final ListOfCurrentAsset listOfCurrentAsset) {
        this.listOfCurrentAsset = listOfCurrentAsset;
    }
}
