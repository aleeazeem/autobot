package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;

public class FulfillmentResponse {

    private String date;
    private String fulfillmentGroupId;
    private String type;
    private String result;

    public enum FulfillmentResponseType {
        TRIGGERS_ASYNC_REQUEST("TRIGGERS_ASYNC_REQUEST"),
        LEGACY_ASYNC_REQUEST("LEGACY_ASYNC_REQUEST"),
        LEGACY_ASYNC_NOTIFICATION("LEGACY_ASYNC_NOTIFICATION");

        private String type;

        FulfillmentResponseType(final String type) {
            this.type = type;
        }

        public String getResponseType() {
            return type;
        }
    }

    public enum FulfillmentResponseResult {
        SUCCEEDED("SUCCEEDED"),
        PENDING("PENDING");

        private String result;

        FulfillmentResponseResult(final String result) {
            this.result = result;
        }

        public String getResponseResult() {
            return result;
        }
    }

    public String getDate() {
        return date;
    }

    @XmlAttribute
    public void setDate(final String date) {
        this.date = date;
    }

    public String getFulfillmentGroupId() {
        return fulfillmentGroupId;
    }

    @XmlAttribute
    public void setFulfillmentGroupId(final String fulfillmentGroupId) {
        this.fulfillmentGroupId = fulfillmentGroupId;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(final String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    @XmlAttribute
    public void setResult(final String result) {
        this.result = result;
    }

}
