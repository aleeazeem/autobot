package com.autodesk.bsm.pelican.enums;

/**
 * This enum is for setting purchase order for Subscription Extension and Subscription Quantity Created by Shweta Hegde
 * on 3/28/17.
 */
public enum LineItemParams {

    SUBSCRIPTION_ID("subscriptionId"),
    CURRENCY_ID("currencyId"),
    CURRENCY_NAME("currencyName"),
    PROMOTION_REFERENCE("promotionReference"),
    PRICE_ID("priceId"),
    QUANTITY("quantity"),
    SUBSCRIPTION_RENEWAL_DATE("subscriptionRenewalDate"),
    TARGET_RENEWAL_DATE("targetRenewalDate"),
    OFFERING_EXTERNAL_KEY("offeringExternalKey"),
    OFFERING_ID("offeringId"),
    OFFER_EXTERNAL_KEY("offerExternalKey"),
    OFFER_ID("offerId"),
    PROMOTION_REFERENCE_ID("id");

    private String value;

    LineItemParams(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
