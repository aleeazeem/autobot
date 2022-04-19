package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * This is a pojo for Updating subscription using Subscription Service end point.
 */
public class UpdateSubscription {

    private Meta meta;
    private Data data;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }

    public static class Meta {
        private String context;

        public String getContext() {
            return context;
        }

        public void setContext(final String context) {
            this.context = context;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(final Data data) {
        this.data = data;
    }

    /**
     * This Data is needed, in Subscription service, many of the field names as different. Ex: "creditDays" changed to
     * "daysCredited"
     */
    public static class Data {

        private String status;
        private String nextBillingDate;
        private int quantity;
        private int daysCredited;
        private String priceId;

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public String getNextBillingDate() {
            return nextBillingDate;
        }

        public void setNextBillingDate(final String nextBillingDate) {
            this.nextBillingDate = nextBillingDate;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(final int quantity) {
            this.quantity = quantity;
        }

        public int getDaysCredited() {
            return daysCredited;
        }

        public void setDaysCredited(final int daysCredited) {
            this.daysCredited = daysCredited;
        }

        public String getPriceId() {
            return priceId;
        }

        public void setPriceId(final String priceId) {
            this.priceId = priceId;
        }
    }
}
