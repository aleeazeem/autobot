package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * This is the pojo representation of shipmentData for fulfilmentCallback API
 *
 * @author kishor
 */
public class ShipmentData {

    private String trackingNumber;

    /**
     * @return the trackingNumber
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * @param trackingNumber the trackingNumber to set
     */
    @XmlElement(name = "TrackingNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setTrackingNumber(final String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
