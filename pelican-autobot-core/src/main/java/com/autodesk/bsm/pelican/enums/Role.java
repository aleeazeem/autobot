package com.autodesk.bsm.pelican.enums;

/**
 * Enum for Roles
 *
 * @author jains
 */

public enum Role {

    ADMIN("Admin"),
    APPLICATION_MANAGER("Application Manager"),
    ATC_ADMIN("ATC Admin"),
    AUTHENTICATOR("Authenticator"),
    BANKING_ADMIN("Banking Admin"),
    BIC_RELEASES_ADMIN("BIC Releases Admin"),
    COMMUNITY_ADMIN("Community Admin"),
    CSAT_PROMOTION_REPRESENTATIVE("CSAT Promotion Representative"),
    EBSO("EBSO Role"),
    OFFERING_MANAGER("Offering Manager"),
    PAYMENT_GATEWAY_AGENT("Payment Gateway Agent"),
    PAYMENT_GATEWAY_MANAGER("Payment Gateway Manager"),
    PAYMENT_SETTLEMENT_AGENT("Payment Settlement Agent"),
    PROMOTION_MANAGER("Promotion Manager"),
    QA_ONLY("QA ROLE"),
    READ_ONLY("Read Only"),
    ROLE_GRANTER("Role Granter"),
    SECURITY_MANAGER("Security Manager"),
    STORE_MANAGER("Store Manager"),
    SUB_RENEWAL_ROLE("sub-renewal-role"),
    SUBSCRIPTIONMANAGER("SubscriptionManager"),
    USER_ADMIN("User Admin"),
    GCSO("GCSO"),
    GDPR_ROLE("GDPR Role"),
    GCSO_EDIT_SUBSCRIPTION("GCSO Edit Subscription");

    private String role;

    Role(final String role) {
        this.role = role;
    }

    public String getValue() {
        return role;
    }
}
