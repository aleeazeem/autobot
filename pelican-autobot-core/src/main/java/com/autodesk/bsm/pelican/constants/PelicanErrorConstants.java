package com.autodesk.bsm.pelican.constants;

/**
 * This class contains all error messages
 *
 * @author Shweta Hegde, Vineel Yerragudi.
 */
public class PelicanErrorConstants {

    // Date related errors
    public static final String INVALID_LAST_MODIFIED_DATE_RANGE =
        "Invalid Last Modified Dates. The date range is limited to ";
    public static final String INVALID_LAST_MODIFIED_END_DATE =
        "Invalid Last Modified End Date. Must be same as or older than the Last Modified Start Date.";
    public static final String EMPTY_LAST_MODIFIED_END_DATE = "Invalid Last Modified End Date. Cannot be empty.";
    public static final String DATE_RANGE_NINTYDAYS_ERROR_MEESAGE = "Date range cannot exceed 90 days.";
    public static final String EMPTY_DATE_RANGE = "Must provide at least one date range.";
    public static final String END_DATE_BEFORE_START_DATE_ERROR_MEESAGE =
        "Start date must be the same as or earlier than the end date";
    public static final String DATE_RANGE_ERROR = "Date range cannot exceed 92 days.";
    public static final String DEFAULT_DATE_SELECTION_ERROR_MESSAGE = "Must select at least one date range.";

    // Entity not found errors/invalid errors
    public static final String INVALID_COUNTRY_CODE = "Invalid country code. (abcd)";
    public static final String STORE_NOT_FOUND = "Store not found. (abcd)";
    public static final String STORE_TYPE_NOT_FOUND = "Store type not found. (abcd)";
    public static final String STORE_TYPE_ERROR_MESSAGE = "Store type not found.";
    public static final String INVALID_COUNTRY_CODE_EXCEPTION_ERROR_CODE = "420001";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_ERROR_CODE = "990002";
    public static final String ITEM_NOT_FOUND_EXCEPTION_ERROR_CODE = "400002";
    public static final String TFE_ENITY_NOT_FOUND_EXCEPTION = "990013";
    public static final String COUNTRY_CODE_ERROR_MESSAGE = "Invalid country code.";
    public static final String EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE =
        "invalid quantity entered. (min: 1, max: 999)";
    public static final String INVALID_PRICE_ERROR_MESSAGE = "Cannot find price with ID: ";
    public static final String INVALID_OFFER_EXTERNAL_KEY_ERROR_MESSAGE = "Cannot find offer with external key";
    public static final String NUMBER_ERROR_MESSAGE = "Must be a number";
    public static final String DUPLICATE_CURRENCY_ERROR_MESSAGE = "Currency already exists in this application family";
    public static final String ACTOR_NOT_FOUND = "No actor found with id: ";
    public static final String EXPECTED_BILLING_PERIOD_OR_BILLING_COUNT_MISSING_MESSAGE =
        "Either both billingPeriodCount and billingPeriod must be provided or both must not be provided.";

    // Block Size and start index related errors
    public static final String ERROR_MESSAGE_FOR_BLOCK_SIZE_LESS_THAN_0 =
        "Invalid pagination parameter. Block size" + " must be 1 or greater";
    public static final String ERROR_MESSAGE_FOR_BLOCK_SIZE_GREATER_THAN_1000 =
        "Invalid pagination parameter. Block " + "size cannot be greater than 1000";
    public static final String ERROR_MESSAGE_FOR_NEGATIVE_START_INDEX =
        "Invalid pagination parameter. Start index " + "cannot be a negative number";

    // Republish errors
    public static final String REPUB_MAX_LIMIT_ERROR =
        "Cannot republish. There are more than the maximum of 20 entities for the given criteria."
            + " Please select different criteria to narrow the the number of results.";

    // Duplicate entity errors
    public static final String DUPLICATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_ERROR_MESSAGE =
        "The external key is already in use by another subscription plan.";
    public static final String DUPLICATE_VALUE_ERROR_MESSAGE = "This value is already in use by another offering.";

    // Price Quote Promotion Errors
    public static final String INVALID_PROMOTION_CODE = "invalid-promotion-code";
    public static final String NOT_APPLICABLE_PROMOTION = "not-applicable-promotion";
    public static final String INACTIVE_PROMOTION = "inactive-promotion";
    public static final String INVALID_PROMOTION_ERROR_MESSAGE = "Invalid promotion code: ";
    public static final String MORE_THAN_ONE_PROMOS_PER_PRICE_FIRST_PROMO_SELECTED =
        "more-than-1-promos-per-price-first-promo-selected";
    public static final String MULTIPLE_VALID_PROMO_CODES_FOR_LINE_ITEM_APPLIED_FIRST_ONE =
        "Multiple valid promo codes for line item, applied the first one";
    public static final String MORE_THAN_ONE_PROMOS_PER_PRICE_PROMO_CODE_SELECTED =
        "more-than-1-promos-per-price-promo-code-selected";
    public static final String MULTIPLE_VALID_PROMOTIONS_FOR_LINE_ITEMS_APPLIED_PROMO_CODE_PROMO =
        "Multiple valid promotions for line item, applied promo code promo";
    public static final String EXCEEDS_MAX_USE_COUNT = "exceeds-max-use-count";
    public static final String EXCEEDS_MAX_USE_COUNT_PER_USER = "exceeds-max-use-count-per-user";
    // bundle promotion related errors in Price Quote
    public static final String INSUFFICIENT_QUANTITY_FOR_BUNDLED_PROMOTION = "insufficient-qty-for-bundled-promo";
    public static final String MISSING_ITEM_FOR_BUNDLED_PROMO = "missing-item-for-bundled-promo";
    public static final String ERROR_MESSAGE_FOR_DIFFERENT_CURRENCY =
        "All provided price IDs must contain the same currency.";
    public static final String ERROR_MESSAGE_FOR_DIFFERENT_STORES =
        "All provided price IDs must belong to the same store.";
    public static final String ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID =
        "A lineItem with purchaseType as subscriptionQuantity or subscriptionExtension or subscriptionRenewal must "
            + "contain subscriptionId.";
    public static final String ERROR_MESSAGE_FOR_MISSING_PRICE_ID = "Price ID missing";
    public static final String ERROR_MESSAGE_FOR_MISSING_TARGET_RENEWAL_DATE =
        "A lineItem with purchaseType as subscriptionExtension or subscriptionRenewal must have a target renewal date";
    public static final String ERROR_MESSAGE_FOR_META_SUBSCRIPTION_ID =
        "A lineItem with purchaseType as subscriptionQuantity can only contain prices associated to Bic Subscriptions.";
    public static final String ERROR_MESSAGE_FOR_SUPPLIMENT_PROMO = "not-applicable-promotion";
    public static final String ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION = "The subscription with id: ";
    public static final String ERROR_MESSAGE_FOR_EXPIRED_SUBSCRIPTION =
        "Renewal not allowed for the subscription with id: %s because it is expired.";
    public static final String ERROR_MESSAGE_FOR_SUBSCRIPTION_RENEWAL_WITH_PENDING_PAYMENT =
        "Renewal not allowed for the subscription with id: %s because it has the pending payment.";
    public static final String ERROR_MESSAGE_FOR_SUBSCRIPTION_RENEWAL_DATE_IN_FUTURE =
        "Renewal not allowed for the subscription with id: %s because it has renewal date in future.";
    public static final String ERROR_MESSAGE_FOR_POST_TEXT_PAST_TARGET_RENEWAL_DATE =
        " has its Next Billing Date after the target renewal date.";
    public static final String ERROR_MESSAGE_FOR_POST_TEXT_INACTIVE_SUBSCRIPTION = " is not active.";
    public static final String ERROR_MESSAGE_FOR_POST_TEXT_EXPIRED_SUBSCRIPTION = " is expired.";
    public static final String ERROR_MESSAGE_FOR_POST_TEXT_PAST_NEXT_NILLLING_DATE =
        " has its Next Billing Date null or in the past.";
    public static final String ERROR_MESSAGE_FOR_NO_PROMOTION_APPLICABLE =
        "No promotion is valid for the purchase type.";
    public static final String ERROR_MESSAGE_FOR_INVALID_SUBSCRIPTION = "Invalid subscription IDs: [";
    public static final String ERROR_MESSAGE_FOR_MIXED_PURCHASETYPES =
        "All line items must contain the same purchaseType.";
    public static final String ERROR_MESSAGE_FOR_QUANTITY_ZERO = "Quantity should be greater than 0.";
    public static final String ERROR_MESSAGE_FOR_INVALID_PURCHASETYPE = "Invalid Purchase Type.";

    // Price Quote Errors for subscription renewal.
    public static final String ERROR_MESSAGE_FOR_NON_ACTIVE_PRICE_ID =
        "No active store found for the price Ids informed.";

    // Activate Promotion Error
    public static final String OFFERINGS_OFFERS_REQUIRED = "Offering(s)/Offer(s) required";
    public static final String EFFECTIVE_DATE_REQUIRED = "Effective Date Required";
    public static final String SELECT_AN_APPLICABLE_STORE = "select an applicable store";
    public static final String STORE_WIDE_PROMO_ALREADY_EXISTS = "Unable to activate promotion. "
        + "A storewide promotion already exists in the store for the same offer(s)/offering(s).";
    public static final String EFFECTIVE_DATES_IN_PAST =
        "To activate promotion, Expiration Date must be later than current date";
    // Bundle Promotion activation errors/warning
    public static final String NOT_ALL_OFFERS_HAVE_SAME_TERMS =
        "Not all offers have the same billing frequency. Do you want to continue?";
    public static final String APPLY_DISCOUNT_FLAG_NOT_SET =
        "at least one of the offerings/offers should have the apply discount flag set";
    public static final String EXPECTED_MORE_OFFERING_ERROR_MESSAGE =
        "Bundle count exceeded. Only 5 products (offers/basic offerings combined)" + " are allowed within a bundle";

    // Report error
    public static final String AT_LEAST_ONE_STATUS_ERROR_MESSAGE = "At least one status must be selected.";
    public static final String AT_LEAST_ONE_USAGE_TYPE_ERROR_MESSAGE = "At least one usage type must be selected.";
    public static final String VIEW_ERRORS = "View Errors";

    // Errors
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String ERROR_HEADER = "There was an error";
    public static final String PERMISSION_DENIED = "Permission denied";
    public static final String DEFAULT_ERRORS_MESSAGE = "Please correct the errors listed below:";
    public static final String DEFAULT_ERROR_MESSAGE = "Please correct the error listed below:";
    public static final String REQUIRED_ERROR_MESSAGE = "Required";
    public static final String MAIN_ERROR_MESSAGE_2 =
        "Please provide 'price id' OR 'offer external key' OR " + "select productLine/Plan/Offer/Price";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String PLEASE_TRY_AGAIN = "Did not match. Please try again";
    public static final String PASSWORD_NOT_MET_CRITERIA =
        "Password should be at least 8 characters in length and should contain at least "
            + "one upper-case, one lower-case, and one numeric character";

    // signs constants
    public static final String WARNING = "⚠";
    public static final String Error = "❗";

    // Upload file related errors
    public static final String ERRORS_UPLOAD = "Errors";
    public static final String WARNINGS_UPLOAD = "Warnings";
    public static final String DEAFULT_UPLOAD_FILE_ERROR_MESSAGE =
        "Please upload a valid XLSX file that is not password-protected.";

    // price mapping warning and error messages
    public static final String WARNING_MESSAGE_FOR_TARGET_AMOUNT = "Target amount : ";
    public static final String WARNING_MESSAGE_FOR_SOURCE_AMOUNT = " USD does not match the source amount : ";
    public static final String ERROR_MESSAGE_FOR_PRICELIST_1 =
        "Target Price Id is missing for " + "Subscription Offer XKEY : ";
    public static final String ERROR_MESSAGE_FOR_PRICELIST_2 = " and Source Price ID : ";
    public static final String ERROR_MESSAGE_FOR_INVALID_SOURCE_PLAN = "Source Subscription Plan XKEY: ";
    public static final String ERROR_MESSAGE_FOR_INVALID_TARGET_PLAN = "Target Subscription Plan XKEY: ";
    public static final String ERROR_MESSAGE_FOR_INVALID_SOURCE_OFFER = "Source Subscription Offer XKEY: ";
    public static final String ERROR_MESSAGE_FOR_INVALID_TARGET_OFFER = "Target Subscription Offer XKEY: ";
    public static final String ERROR_MESSAGE_INVALID = " is not valid";
    public static final String PROMOTION_WARNING_MESSAGE =
        "subscription(s) currently set to renew with a " + "promotion applied to the renewal";
    public static final String LIST_OF_SUBSCRIPTION_WARNING_MESSAGE = "Click here for the list of subscription(s).";
    public static final String WARNING_MESSAGE_FOR_DIFFERENT_OFFERING_TYPE = "Migrating to a different offering type.";

    // Subscription Migration
    public static final String ANOTHER_MIGRATION_JOB_RUNNING =
        "Another migration job is currently running. Please try later.";
    public static final String CANCEL_MIGRATION_JOB_ERROR_MESSAGE =
        "Cannot cancel. You can only cancel job that has been running for 10 minutes or more.";
    public static final String MIGRATE_SUBSCRIPTION_NOT_FIND_PRICE_MESSAGE =
        "Could not migrate subscription: Could not find Price";

    // Subscription Migration
    public static final String ROLL_BACK_ERROR_MESSAGE =
        "Rollback not allowed after 48 hours of the initial migration run time ";
    public static final String MUST_SELECT_AT_LEAST_ONE_STATUS = "Must select at least one status.";

    // Submit Purchase Order
    public static final String SUBSCRIPTION_INACTIVE_ERROR_MESSAGE = "Subscription with id: %s is not active.";
    public static final String SUBSCRIPTION_PRICE_ID_NOT_MATCHING_ERROR_MESSAGE =
        "Either the pricelist or offer or offering of the price: '%s' is not matching with the subscription: '%s'";
    public static final String SUBSCRIPTION_ZERO_OR_NEGATIVE_QUANTITY_ERROR_MESSAGE =
        "Input quantity must be greater than zero.";
    public static final String NON_BIC_SUBSCRIPTION_ERROR_MESSAGE =
        "Subscription quantity request is not allowed for Meta Subscription";
    public static final String ADD_SEATS_BUNDLED_PROMO_ERROR_MESSAGE =
        "Bundled promotions are not valid for the purchase type associated to the price.";
    public static final String ADD_SEATS_SUPPLEMENT_PROMOTION_ERROR_MESSAGE =
        "Supplement promotions are not valid for the purchase type associated to the price.";
    public static final String SUBSCRIPTION_NEXT_BILLING_DATE_IN_PAST =
        "Subscription next billing date is in the past.";
    public static final String PRICE_ID_REQUIRED_ERROR =
        "The priceId must be provided for: subscriptionExtensionRequest";
    public static final String SUBSCRIPTION_ID_REQUIRED_ERROR =
        "The subscriptionId must be provided for: subscriptionExtensionRequest";
    public static final String SUBSCRIPTION_ID_NOT_FOUND_ERROR = "Subscription not found with the id: ";
    public static final String TARGET_RENEWAL_DATE_REQUIRED_ERROR =
        "Valid target renewal date must be provided for: subscriptionExtensionRequest.";
    public static final String TARGET_RENEWAL_DATE_SHOULD_BE_GREATER_THAN_NBD_ERROR =
        "Target renewal date must be greater than subscription next billing date.";
    public static final String NOT_ACTIVE_ERROR = " is not active.";
    public static final String PRICE_NOT_MATCHING_SUBSCRIPTION_ERROR =
        "Either the pricelist or offer or offering of the price: '%s' is not matching with the subscription: '%s'";
    public static final String SUBSCRIPTION_NOT_FOUND_ERROR = "Could not find subscription with id=";
    public static final String SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR =
        "Subscription with id: %s already has a pending order. "
            + "Cannot process request before pending order is charged.";
    public static final String PRICE_ID_NOT_ACTIVE_ERROR = "Price with id: %s is not active.";
    public static final String PURCHASE_ORDER_ID_SHOULD_BE_NUMERIC = "Purchase Order Id must be a numeric.";
    public static final String PURCHASE_ORDER_NOT_FOUND = "Purchase Order %s not found";
    public static final String PROCESS_PO_ERROR = "The specified command (%s) is not allowed with this purchase order.";

    // Subscription Renewal Error
    public static final String DUPLICATE_SUBSCRIPTION_IDS_ERROR = "Duplicate subscription ids found: ";
    public static final String SUBSCRIPTION_IDS_BELONG_TO_DIFFERENT_PRICE_LIST_ERROR =
        "All subscriptions should belong to the same price list";
    public static final String SUBSCRIPTIONS_NOT_FOUND_ERROR = "Subscriptions not found for ids:";
    public static final String SUBSCRIPTION_ID_REQUIRED_FOR_RENEWALS_ERROR = "subscriptionId must be provided.";
    public static final String CURRENCY_ID_OR_NAME_REQUIRED_ERROR =
        "Mandatory attribute Currency Id or Currency Name must be provided.";
    public static final String CURRENCY_ID_NAME_DO_NOT_MATCH =
        "Currency Name = %s does not match with Currency Id = %s in Purchase Order.";
    public static final String INVALID_CURRENCY_NAME_ERROR = "Invalid Currency Name = %s in Purchase Order.";
    public static final String SUBSCRIPTIONS_SHOULD_BELONG_TO_SAME_STORE_ERROR =
        "When storeExternalKey is provided, all line items must belong to that store.";

    // Refund Errors
    public static final String ADD_SEATS_REFUND_AFTER_RENEWAL =
        "The seats in this order have already been renewed for the next period (or are in the process of), "
            + "so no action will be taken in the subscription. Refunding this order will just reverse the "
            + "payment to the customer. Do you want to proceed?";
    public static final String ADD_SEATS_REFUND_BEFORE_RENEWAL =
        "You cannot perform a refund when the Quantity to Reduce value on the subscription is higher or equal "
            + "to number of active seats on the subscription after the refund. You need to update the value in "
            + "the Quantity to Reduce field.";
    public static final String ZERO_AMOUNT_ORDER_REFUND = "Refunds are not allowed for orders where amount is 0";
    public static final String REFUND_MESSAGE = "Are you sure you want to refund this purchase order?";

    // License Model
    public static final String INVALID_LICENSING_MODEL_MESSAGE = "Invalid Licensing Model";

    // GDPR Error Message
    public static final String GDPR_ERROR_NO_USER = "Nothing to be deleted, user not found";
    public static final String GDPR_ERROR_BAD_REQUEST = "Bad request received. Required user information is missing.";
    public static final String GDPR_ERROR_PRE_CONDITION_FAILED =
        "Precondition failed. User has an unexpired subscription";

    // CSR - subscription plan
    public static final String CSR_ERROR = "For Offerings that only have CSR entitlements, at least one entitlement "
        + "needs to have the Assignable attribute set to False.";

    // Payment Profile Error.
    public static final String UNSUPPORTED_PAYMENT_METHOD = "Unsupported payment type directDebitPayment encountered.";
    public static final String VALIDATION_ERROR_MSG = "Invalid Address";
    public static final String SUB_PLAN_UPLOAD_ASSINABLE_ERROR = "For Offerings that only have CSR entitlements, at "
        + "least one entitlement needs to have the Assignable attribute set to False.";

    // Entitlement Dates Error
    public static final String SUB_PLAN_UPLOAD_PAST_EOS_DATE_ERROR = "EOS Date must be in future";
    public static final String SUB_PLAN_UPLOAD_DATES_OUT_OF_ORDER =
        "When setting multiple EOS and EOL dates, follow this logic: EOS Date < EOL Renewal Date < EOL Immediate Date";
    public static final String SEPA_MANDATE_FIELD_ERROR_MSG =
        "Both mandateId and mandateDate are needed for SEPA payments.";
    public static final String PAST_DATE_CHANGED_TO_FUTURE_ERROR = "EOS Date in the past is not editable";

}
