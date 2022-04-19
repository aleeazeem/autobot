package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class is a pojo for Asset entity in FulfillmentCallBack API request
 *
 * @author kishor
 */
public class Asset {

    private String SerialNumber;

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return SerialNumber;
    }

    /**
     * @param serialNumber the serialNumber to set
     */
    @XmlElement(name = "SerialNumber", namespace = "http://www.autodesk.com/schemas/Business/AssetV1.0")
    public void setSerialNumber(final String serialNumber) {
        SerialNumber = serialNumber;
    }
}
