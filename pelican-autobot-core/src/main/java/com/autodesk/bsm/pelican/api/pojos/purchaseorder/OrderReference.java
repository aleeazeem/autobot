package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class is the pojo representation of OrderReference entity in FulfillmentCallback API response
 *
 * @author kishor
 */
public class OrderReference {

    private String orderNumber;
    private String poNumber;

    /**
     * @return the pONumber
     */
    public String getPONumber() {
        return poNumber.split("-")[0];
    }

    /**
     * @param pONumber the pONumber to set
     */
    @XmlElement(name = "PONumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setPONumber(final String poNumber) {
        this.poNumber = poNumber;
    }

    /**
     * @return the orderNumber
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * @param orderNumber the orderNumber to set
     */
    @XmlElement(name = "OrderNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
