package com.autodesk.bsm.pelican.enums;

import com.google.gson.annotations.SerializedName;

/**
 * Job category for trigger jobs
 *
 * @author yin
 */
public enum JobCategory {

    @SerializedName("scf_emails")
    SCF_EMAILS,

    @SerializedName("subscription_expirations")
    SUBSCRIPTION_EXPIRATIONS,

    @SerializedName("subscription_renewals")
    SUBSCRIPTION_RENEWALS,

    @SerializedName("renewal_reminders")
    RENEWAL_REMINDERS,

    @SerializedName("file_processing")
    FILE_PROCESSING,

    @SerializedName("change_notification_subscription_bootstrap_job")
    CHANGE_NOTIFICATION_SUBSCRIPTION_BOOTSTRAP_JOB,

    @SerializedName("change_notification_purchase_order_bootstrap_job")
    CHANGE_NOTIFICATION_PURCHASE_ORDER_BOOTSTRAP_JOB,

    @SerializedName("change_notiifcation_entitlement_bootstrap_job")
    CHANGE_NOTIFICATION_ENTITLEMENT_BOOTSTRAP_JOB,

    @SerializedName("subscription_file_processing")
    SUBSCRIPTION_FILE_PROCESSING,

    @SerializedName("basicOfferingChangeNotificationRepublishJob")
    BASIC_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB("basicOfferingChangeNotificationRepublishJob"),

    @SerializedName("purchaseOrderChangeNotificationRepublishJob")
    PURCHASE_ORDER_CHANGENOTIFICATION_REPUBLISH_JOB("purchaseOrderChangeNotificationRepublishJob"),

    @SerializedName("subscriptionChangeNotificationRepublishJob")
    SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB("subscriptionChangeNotificationRepublishJob"),

    @SerializedName("subscriptionOfferingChangeNotificationRepublishJob")
    SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB("subscriptionOfferingChangeNotificationRepublishJob"),

    @SerializedName("storeChangeNotificationRepublishJob")
    STORE_CHANGENOTIFICATION_REPUBLISH_JOB("storeChangeNotificationRepublishJob"),

    @SerializedName("entitlementChangeNotificationRepublishJob")
    ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB("entitlementChangeNotificationRepublishJob"),

    @SerializedName("subscriptionChangeNotificationBootstrapJob")
    SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("subscriptionChangeNotificationBootstrapJob"),

    @SerializedName("purchaseOrderChangeNotificationBootstrapJob")
    PURCHASE_ORDER_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("purchaseOrderChangeNotificationBootstrapJob"),

    @SerializedName("entitlementChangeNotificationBootstrapJob")
    ENITITLEMENT_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("entitlementChangeNotificationBootstrapJob"),

    @SerializedName("subscriptionOfferingChangeNotificationBootstrapJob")
    SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("subscriptionOfferingChangeNotificationBootstrapJob"),

    @SerializedName("basicOfferingChangeNotificationBootstrapJob")
    BASIC_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("basicOfferingChangeNotificationBootstrapJob"),

    @SerializedName("comSubscriptionChangeNotificationBootstrapJob")
    COM_SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB("comSubscriptionChangeNotificationBootstrapJob"),

    @SerializedName("aumSubscriptionChangeNotificationJob")
    AUM_SUBSCRIPTION_CHANGE_NOTIFICATION_REPUBLISH_JOB("aumSubscriptionChangeNotificationRepublishJob"),

    @SerializedName("changeNotificationRecoveryJob")
    CHANGE_NOTIFICATION_RECOVERY_JOB("changeNotificationRecoveryJob"),

    @SerializedName("monitoring-job")
    MONITORING_JOB("monitoring-job"),

    @SerializedName("expiration_reminders")
    EXPIRATION_REMINDERS("expiration_reminders"),

    @SerializedName("monitor_pending_purchase_order_job")
    MONITORING_PENDING_PURCHASE_ORDER_JOB("monitor_pending_purchase_order_job"),

    @SerializedName("ENTITLEMENTS_END_DATE_REACHED")
    ENTITLEMENTS_END_DATE_REACHED("ENTITLEMENTS_END_DATE_REACHED");

    private String jobCategory;

    JobCategory() {

    }

    JobCategory(final String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getJobCategory() {
        return jobCategory;
    }
}
