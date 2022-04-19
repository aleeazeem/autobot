package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.util.Util;

import java.util.List;

/**
 * Price Quotes entity for Json API
 *
 * @author t_mohag
 */
public class PriceQuotes {

    private PriceQuoteData data;
    private List<Errors> errors;

    public static class PriceQuoteData {
        private EntityType type;
        private String buyerId;
        private List<JLineItem> lineItems;
        private Totals totals;
        private Shipping shipping;
        private List<JPromotionReference> promotionReferences;
        private List<Errors> errors;
        private Boolean estimateVat;
        private boolean cartUpdated;

        public EntityType getType() {
            return type;
        }

        public void setType(final EntityType type) {
            this.type = type;
        }

        public String getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(final String buyerId) {
            this.buyerId = buyerId;
        }

        public Shipping getShipping() {
            return shipping;
        }

        public void setShipping(final Shipping shipping) {
            this.shipping = shipping;
        }

        public List<JLineItem> getLineItems() {
            return lineItems;
        }

        public void setLineItems(final List<JLineItem> lineItems) {
            this.lineItems = lineItems;
        }

        public List<JPromotionReference> getPromotionReferences() {
            return promotionReferences;
        }

        public void setPromotionReferences(final List<JPromotionReference> promotionReferences) {
            this.promotionReferences = promotionReferences;
        }

        public List<Errors> getErrors() {
            return errors;
        }

        public void setErrors(final List<Errors> errors) {
            this.errors = errors;
        }

        public Totals getTotals() {
            return totals;
        }

        public void setEstimateVat(final Boolean estimateVat) {
            this.estimateVat = estimateVat;
        }

        public boolean isEstimateVat() {
            return estimateVat;
        }

        public Boolean getCartUpdated() {
            return cartUpdated;
        }

        public void setCartUpdated(final Boolean cartUpdated) {
            this.cartUpdated = cartUpdated;
        }
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public PriceQuoteData getData() {
        return data;
    }

    public void setData(final PriceQuoteData data) {
        this.data = data;
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }
}
