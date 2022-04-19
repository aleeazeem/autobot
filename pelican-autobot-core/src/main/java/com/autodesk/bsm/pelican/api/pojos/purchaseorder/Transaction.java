package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Transaction {

    private String id;
    private TransactionType type;
    private GatewayResponse gatewayResponse;
    private FulfillmentResponse fulfillmentResponse;

    public enum TransactionType {
        SALE,
        AUTHORIZATION,
        FUNDS_AUTHORIZATION,
        FULFILLMENT,
        REFUND
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setTransactionType(final TransactionType type) {
        this.type = type;
    }

    public GatewayResponse getGatewayResponse() {
        return gatewayResponse;
    }

    @XmlElement(name = "gatewayResponse")
    public void setGatewayResponse(final GatewayResponse response) {
        this.gatewayResponse = response;
    }

    public FulfillmentResponse getFulfillmentResponse() {
        return fulfillmentResponse;
    }

    @XmlElement(name = "fulfillmentResponse")
    public void setFulfillmentResponse(final FulfillmentResponse fulfillmentResponse) {
        this.fulfillmentResponse = fulfillmentResponse;
    }
}
