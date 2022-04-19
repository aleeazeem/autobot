package com.autodesk.bsm.pelican.enums;

public enum AdminPages {

    // Top level links
    CATALOG("catalog"),
    FEATURE("feature"),
    FEATURE_TYPE("featureType"),
    CURRENCY("currency"),

    // AT pages
    LOGIN("loginForm"),

    STORE_TYPE("storeType"),
    STORE("store"),
    PRICE_LIST("priceList"),
    SHIPPING_METHODS("shippingMethod"),
    DEFAULT_STORE("defaultStore"),

    PRODUCT_LINE("productLine"),
    BIC_RELEASE("bicRelease"),
    OFFERING_DETAIL("offeringDetail"),

    // Purchase Order
    PURCHASE_ORDER("purchaseOrder"),

    FINANCE_REPORT("financeFindForm"),
    BASIC_OFFERING_REPORT("basicOfferingsFindForm"),
    // SUBSCRIPTION
    SUBSCRIPTION("subscription"),
    SUBSCRIPTIONS("subscriptions"),
    EDIT_CREDIT_DAYS("showAddCreditDaysForm"),
    UPLOAD_SUBSCRIPTION_MIGRATION("importMigrationJobForm"),
    SUBSCRIPTION_MIGRATION_JOB_STATUS("viewMigrationJobStatusForm"),
    SUBSCRIPTION_MIGRATION_JOB_DETAIL("viewMigrationJob"),

    // SUBSCRIPTION PLAN
    SUBSCRIPTION_PLAN("subscriptionPlan"),

    // Application Family
    APPLICATION_FAMILY("applicationFamily"),

    // forms
    ADD_FORM("addForm"),
    EDIT_FORM("editForm"),
    FIND_FORM("findForm"),
    DOWNLOAD_FORM("downloadForm"),
    BASIC_OFFERINGS("offering"),
    PROMOTION("promotion"),

    // show the page
    SHOW("show"),

    // Descriptors
    DESCRIPTOR_DEFINITION("descriptorDefinition"),
    FIND("find"),
    REPORTS("reports"),

    // upload
    IMPORT_FORM("importForm"),

    // Reports
    SUBSCRIPTION_OFFERS_REPORT("subscriptionOffersFindForm"),
    FULFILLMENT_REPORT("fulfillmentReport"),
    DECLINED_ORDER_REPORT("declinedOrdersReportForm"),
    ORDER_PROMOTION_USE_REPORT("orderPromotionUseReportForm"),
    PROMOTION_USE_REPORT("promotionReportForm"),
    OFFERINGS_PROMOTION_REPORT("offeringsPromotionReportForm"),
    PENDING_MIGRATION_SUBSCRIPTIONS_REPORT("pendingMigrationSubscriptions"),
    JOB_STATUSES_REPORT("jobStatus"),
    WORK_IN_PROGRESS_REPORT("workInProgress"),
    ORDERS_IN_EXPORT_CONTROL_HOLD_REPORT("ordersInExportControlHoldForm"),
    CANCELLED_SUBSCRIPTION_REPORT("cancelledSubscriptionsFindForm"),
    SUBSCRIPTION_PLANS_AND_FEATURES_REPORT("subscriptionPlansAndFeaturesFindForm"),
    EXPORT_CONTROL_STATISTICS_FORM("exportControlStatisticsForm"),
    USER_ACCESS_REPORT("userAccessReportForm"),
    AUDIT_LOG_REPORT("auditLogReportForm"),
    AUDIT_LOG("auditLogReport"),
    DR_MIGRATION_REPORT("digitalRiverSubscriptionMigrationReportFindForm"),

    // User
    USER("user"),
    CREDENTIAL("credential"),
    ROLE_ASSIGNMENT("roleAssignment"),

    // Actor
    ACTOR("actor"),
    NAMED_PARTY("namedParty"),

    // Role
    ROLE("role"),

    // View Upload Status
    VIEW_UPLOAD_STATUS("viewUploadStatus"),

    // Catalog Sub-Menus
    ITEM("feature"),

    // Temporary upload Subscription status page
    IMPORT("import"),

    // BootStrap Change Notifications page
    ADMIN("admin"),
    EVENT("event"),
    BOOTSTRAP_FORM("bootstrapForm"),

    // Licensing Model
    LICENSING_MODEL("licensingModel"),

    // coreProduct
    COREPRODUCT("coreProduct");

    private String page;

    AdminPages(final String page) {
        this.page = page;
    }

    public String getForm() {
        return page;
    }
}
