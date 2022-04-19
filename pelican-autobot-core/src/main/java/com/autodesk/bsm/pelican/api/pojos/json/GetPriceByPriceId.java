package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of price id.
 *
 * @author Muhammad
 */
public class GetPriceByPriceId extends PelicanPojo {
    private List<Included> included;
    private List<Errors> errors;
    private Price data;

    public static class Included {
        private String type;
        private String id;
        private String priceListId;
        private String amount;
        private String currency;
        private String storeId;
        private String storeExternalKey;
        private String priceListExternalKey;
        private String status;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(final String currency) {
            this.currency = currency;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(final String amount) {
            this.amount = amount;
        }

        public String getPriceListId() {
            return priceListId;
        }

        public void setPriceListId(final String priceListId) {
            this.priceListId = priceListId;
        }

        public String getStoreId() {
            return storeId;
        }

        public void setStoreId(final String storeId) {
            this.storeId = storeId;
        }

        public String getStoreExternalKey() {
            return storeExternalKey;
        }

        public void setStoreExternalKey(final String storeExternalKey) {
            this.storeExternalKey = storeExternalKey;
        }

        public String getPriceListExternalKey() {
            return priceListExternalKey;
        }

        public void setPriceListExternalKey(final String priceListExternalKey) {
            this.priceListExternalKey = priceListExternalKey;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }
    }

    public List<Included> getIncluded() {
        return included;
    }

    public void setIncluded(final List<Included> included) {
        this.included = included;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public Price getData() {
        return data;
    }

    public void setData(final Price data) {
        this.data = data;
    }

}
