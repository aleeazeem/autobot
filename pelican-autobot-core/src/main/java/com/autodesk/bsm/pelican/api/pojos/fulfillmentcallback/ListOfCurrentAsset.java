package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * This is a pojo class for <pfx3:ListOfCurrentAsset> in FulfillmentCallBack API request body
 *
 * @author kishor
 */
public class ListOfCurrentAsset {

    private Asset asset;

    /**
     * @return the asset
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * @param asset the asset to set
     */
    @XmlElement(name = "Asset", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setAsset(final Asset asset) {
        this.asset = asset;
    }
}
