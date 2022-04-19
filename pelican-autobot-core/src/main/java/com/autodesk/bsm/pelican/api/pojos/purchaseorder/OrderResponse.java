package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the pojo representation for the OrderResponse entity in FulfillmentCallback API Response
 *
 * @author kishor
 */
@XmlRootElement(name = "OrderResponse", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
public class OrderResponse extends PelicanPojo {

    private String orderCreationStatus;
    private OrderReference orderReference;

    /**
     * Method to return the OrderCreationStatus from the response
     *
     * @return order status String
     */
    public String getOrderCreationStatus() {
        return orderCreationStatus;
    }

    /**
     * Method to set the orderCreationStatus in orderResponse
     */
    @XmlElement(name = "OrderCreationStatus", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setOrderCreationStatus(final String orderCreationStatus) {
        this.orderCreationStatus = orderCreationStatus;
    }

    /**
     * Method to return the OrderReference entity from OrderResponse in Fulfillment Callback API
     */
    public OrderReference getOrderReference() {
        return orderReference;
    }

    /**
     * Method to set the OrderReference entity to OrderResponse in Fulfillment Callback API marshalling
     */
    @XmlElement(name = "OrderReference", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setOrderReference(final OrderReference orderReference) {
        this.orderReference = orderReference;
    }
}
