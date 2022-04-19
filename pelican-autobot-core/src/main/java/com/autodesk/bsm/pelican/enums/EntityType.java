package com.autodesk.bsm.pelican.enums;

import com.google.gson.annotations.SerializedName;

/**
 * Entity Type for the Json API
 *
 * @author yin
 */
public enum EntityType {

    @SerializedName("offering")
    OFFERING,

    @SerializedName("offeringDetail")
    OFFERING_DETAIL,

    @SerializedName("price")
    PRICE,

    @SerializedName("billingPlan")
    BILLINGPLAN,

    // Added by Kishor for getStore API Automation
    // Starts here
    @SerializedName("store")
    STORE,

    @SerializedName("shippingMethod")
    SHIPPING_METHOD,

    @SerializedName("priceList")
    PRICE_LIST,
    // Ends here!

    // Added by t_mohag for getPromotions API Automation
    @SerializedName("promotion")
    PROMOTION,

    @SerializedName("Basic Offering")
    BASIC_OFFERING,

    @SerializedName("cart")
    CART,

    @SerializedName("subscription")
    SUBSCRIPTION,

    @SerializedName("subscriptions")
    SUBSCRIPTIONS,

    @SerializedName("productLine")
    PRODUCT_LINE,

    @SerializedName("subscription_entitlement")
    SUBSCRIPTION_ENTITLEMENT,

    @SerializedName("paymentMethod")
    PAYMENT_METHOD,
    UNKNOWN,
    ITEM
}
