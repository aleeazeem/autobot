package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Shipping {

    private ShippingMethod shippingMethod;
    private ShipTo shipTo;

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    @XmlElement(name = "shippingMethod")
    public void setShippingMethod(final ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public ShipTo getShipTo() {
        return shipTo;
    }

    @XmlElement(name = "shipTo")
    public void setShipTo(final ShipTo shipTo) {
        this.shipTo = shipTo;
    }
}
