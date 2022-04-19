package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of the request to get reduce seats by subscription id
 *
 * @author Muhammad
 */
public class ReduceSeatsBySubscriptionId extends PelicanPojo {
    private Data data;
    private List<Errors> errors;

    /**
     * This class represents JSON object of reduce seats by subscription id.
     */
    public static class Data {
        private String id;
        private String type;
        private String quantity;
        private String qtyToReduce;
        private String renewalQuantity;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(final String quantity) {
            this.quantity = quantity;
        }

        public String getQtyToReduce() {
            return qtyToReduce;
        }

        public void setQtyToReduce(final String qtyToReduce) {
            this.qtyToReduce = qtyToReduce;
        }

        public String getRenewalQuantity() {
            return renewalQuantity;
        }

        public void setRenewalQuantity(final String renewalQuantity) {
            this.renewalQuantity = renewalQuantity;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(final Data data) {
        this.data = data;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }
}
