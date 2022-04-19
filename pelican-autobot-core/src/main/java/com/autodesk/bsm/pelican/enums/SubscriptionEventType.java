package com.autodesk.bsm.pelican.enums;

public enum SubscriptionEventType {

    CHARGE,
    START_PHASE,
    CANCEL,
    REFUND,
    CHARGE_FAILED,
    EXPIRED,
    GRANT_ENTITLEMENTS,
    ENTITLEMENTS_DELIVERY_FAILED,
    CREDIT,
    RENEWAL_REMINDER,
    CREDIT_CARD_EXPIRY_REMINDER,
    UPGRADE,
    RESTART,

    // Currently, used by mail notifications only
    CHARGE_PURCHASE,
    CHARGE_RENEWAL,

    // new EventType for migration activity
    MIGRATED,
    UPDATE_LEGACY_FAILED,
    CANCEL_DR_SUB_FAILED,
    MIGRATION_START,
    GET_DR_SUB_INFO_FAILED,

    EC_CHANGE,
    EC_CHANGE_IGNORED,
    MIG_RETRIGGER_UPDATE,
    MIG_RETRIGGER_CANCEL,

    // Subscription Migration
    SUB_MIGRATED,
    SUB_MIGRATION_REVERTED,

    // CREDITS
    PROVISION_CREDITS,

    // Seats
    REDUCE_SEATS,

    // Edit Pending Payment Flag
    PENDING_PAYMENT_FLAG_UPDATE,

    // Subscription Quantity
    ADD_SEATS,
    ADD_SEATS_DECLINED,

    // Purchase Order Refund
    REFUNDED,

    // Monitoring events
    MONITORING_ALERT,

    EDIT,

    // Subscription Extension
    EXTENSION,
    EXTENSION_DECLINED,

    // Purchase Order Chargedback
    CHARGED_BACK,

    // Payment Profile Change
    PAYMENT_PROFILE_UPDATE,

    // emails
    EXPIRATION_REMINDER_SENT,

    // gdpr cancel
    GDPR_CANCEL
}
