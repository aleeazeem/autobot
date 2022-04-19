package com.autodesk.bsm.pelican.constants;

import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.httpclient.HttpStatus;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * These constants are common for all Pelican Entities
 *
 * @author Shweta Hegde.
 */
public class PelicanConstants {

    public static final String STATUS = "status";
    public static final String VAT_PERCENT = "vatPercent";
    public static final String SOLD_TO_CSN = "soldToCSN";
    public static final String CANCEL = "CANCEL";
    public static final String STATUS_FIELD = "Status";
    public static final String ERRORS_FIELD = "Errors";
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String DELINQUENT_RESOLVE_BY_DATE = "delinquentResolveByDate";
    public static final String LAST_MODIFIED = "lastModified";
    public static final String MARK_AS_REFUND = "Mark As Refund";
    public static final int RENEWAL_GRACE_PERIOD_IN_DAYS = 7;
    public static final int RENEWAL_DAYS_FOR_ADD_SEATS = 5;
    public static final String NAME = "name";
    public static final String EXTERNAL_KEY = "externalKey";
    public static final String ACTIVE_STATUS = "active";
    public static final String ACTIVE_STATUS_FIELD = "Active";
    public static final String OFFERING_TYPE = "offeringType";
    public static final String CANCELLATION_POLICY = "cancellationPolicy";
    public static final String USAGE_TYPE = "usageType";
    public static final String OFFERING_DETAIL_ID = "offeringDetailId";
    public static final String PRODUCT_LINE = "productLine";
    public static final String SUPPORT_LEVEL = "supportLevel";
    public static final String PRE_SUB_PLAN_BLOCK_MESSAGE =
        "The subscription plan will be blocked from adding additional features for ";
    public static final String PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE =
        "Active subscription plan(s) will be blocked from adding additional features for ";
    public static final String POST_SUB_PLAN_BLOCK_MESSAGE = " hours.\n";
    public static final String POST_SUB_PLAN_BLOCK_MESSAGE_HOURS = " hours.";
    public static final String ADD_FEATURE_POPUP_EXPECTED_MESSAGE =
        "Are you sure you want to add the following features to the offering?:\n" + "● ";
    public static final String PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE =
        "Are you sure you want to add this feature to ";
    public static final String POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE = " subscription plan(s)?";
    public static final String BULK_REMOVE_MESSAGE1 = "There are ";
    public static final String BULK_REMOVE_MESSAGE2 = " subscription plan(s) with feature ";
    public static final String BULK_REMOVE_MESSAGE_FOR_ZERO_PLANS = "There are 0 subscription plan(s) with feature ";
    public static final String SPLITTER = "\nConfirm";
    public static final String OFFERING_DETAIL = "OfferingDetails1 (DC020500)";
    public static final String ID = "id";
    public static final String ID_FIELD = "ID";
    public static final String USER_ID = "userId";
    public static final String PAYMENT_PROCESSOR = "paymentProcessor";
    public static final String RELATED_ID = "relatedId";
    public static final String AMOUNT_CURRENCY_ID = "amount.currencyId";
    public static final String AMOUNT_AMOUNT = "amount.amount";
    public static final String GRANT_TYPE = "grantType";
    public static final String MEDIA_TYPE = "mediaType";
    public static final String LANGUAGE_CODE = "languageCode";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currencyId";
    public static final String CURRENCY_ENTITLEMENT = "CURRENCY";
    public static final String ITEM = "ITEM";
    public static final String CURRENCY_NAME = "currencyName";
    public static final String PROPERTIES = "properties.props";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String OFFERING_ID = "offeringId";
    public static final String PRICE_LIST_ID = "priceListId";
    public static final String STORE_ID = "storeId";
    public static final String SUBTYPE = "subType";
    public static final String DESCRIPTION = "description";
    public static final String EFFECTIVE_DATE = "effectiveDate";
    public static final String APP_FAMILY_ID = "appFamilyId";
    public static final String APP_ID = "appId";
    public static final String TYPE_ID = "typeId";
    public static final String PRICE_ID = "priceId";
    public static final String PLAN_ID = "planId";
    public static final String QUANTITY = "quantity";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String CREATE_AND_UPDATE = "createandupdate";
    public static final String LAST_BILLING_CYCLE_DAYS = "lastBillingCycleDays";
    public static final String PURCHASE_ORDER_ID_DB = "purchase_order_id";
    public static final String DAYS_CREDITED = "daysCredited";
    public static final String OWNER_ID = "ownerId";
    public static final String OFFER = "offer";
    public static final String CREATED_FIELD = "Created";
    public static final String CREATED_BY_FIELD = "Created By";
    public static final String LAST_MODIFIED_FIELD = "Last Modified";
    public static final String LAST_MODIFIED_BY_FIELD = "Last Modified By";
    public static final String NOT_RECORDED = "Not recorded";
    public static final String PERMISSION_DENIED = "Permission denied";
    public static final String PERMISSIONS = "permissions";
    public static final String NOT_APPLICABLE = "N/A";
    public static final String ONCE = "Once";
    public static final String EVERY_MONTH = "Every month";
    public static final String EVERY_2_MONTHS = "Every 2 months";
    public static final String EVERY_3_MONTHS = "Every 3 months";
    public static final String EVERY_6_MONTHS = "Every 6 months";
    public static final String EVERY_YEAR = "Every year";
    public static final String EVERY_2_YEARS = "Every 2 years";
    public static final String EVERY_3_YEARS = "Every 3 years";
    public static final String EVERY_4_YEARS = "Every 4 years";
    public static final String AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD = "billingOption.billingPeriod";
    public static final String AUDIT_LOG_ID = "AuditLogId";
    public static final String DATE = "Date";
    public static final String AUDIT_LOG_TIMESTAMP = "AuditTimeStamp";
    public static final String NAMED_PARTY_ID = "namedPartyId";
    public static final String ROLE_ID = "roleId";
    public static final String OFFERING_DETAILS1 = "OfferingDetails1";
    public static final String TIMESTAMP_CREATED = "timestamp.created";
    public static final String TIMESTAMP_LAST_MODIFIED = "timestamp.lastModified";
    public static final String AUDIT_LOG_PROPERTIES = "properties.props";
    public static final String FEATURE_TYPE = "featuretype";
    public static final String ACTIVE_FIELD = "Active";
    public static final String ACTIVE_FIELD_NAME = "active";
    public static final String FEATURE = "feature";
    public static final String APPLICATION_FAMILY_NAME = "Automated Tests (AUTO)";
    public static final String ROLE = "Role";
    public static final String ROLE_ASSIGNMENT = "RoleAssignment";
    public static final String ACTOR = "Actor";
    public static final String API_SECRET_CREDENTIAL = "APISecretCredential";
    public static final String PASSWORD_CREDENTIAL = "PasswordCredential";
    public static final String UUID = "uuid";
    public static final String PROPERTIES_VALUE = "v3";
    public static final String APPLICATION_FAMILY = "Application Family";
    public static final String STATE = "State";
    public static final String ROLES = "Roles";
    public static final String USER = "User";
    public static final String DISCOUNT_CODE = "discountCode";
    public static final String CLOUD_CREDITS = "CLOUD_CREDITS";
    public static final String LEGACY = "LEGACY";
    public static final String XLSX_FORMAT = "xlsx";
    public static final String CHECKBOX_CHECK = "check";
    public static final String UNLIMITED = "UNLIMITED";

    // Simple Date Format fields
    public static final SimpleDateFormat DATE_TIME_FORMAT_WITH_TIME_ZONE =
        new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
    public static final SimpleDateFormat DATE_TIME_FORMAT_NEXT_BILLING_DATE =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    public static final SimpleDateFormat DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE_IN_SECONDS =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // api main fields
    public static final String CONTENT_TYPE = "application/vnd.api+json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_XML_UTF8 = "application/xml; charset=UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String ACCEPT = "Accept";
    public static final String DATA = "data";
    public static final String ERRORS = "errors";
    public static final String INCLUDE_PRICES_OFFERS_PARAMS = "prices,offers";
    public static final String INCLUDE_PRICES_PARAMS = "prices";
    public static final String INCLUDE_OFFERS_PARAMS = "offers";
    public static final String INCLUDE_ALL_PARAMS_FOR_OFFERING = "prices,offers,entitlements";
    public static final String INCLUDED = "included";
    public static final String NEXT_CURSOR = "page[next-cursor]";

    // Response Fields
    public static final String TYPE = "type";
    public static final String STORE_TYPE = "storeType";
    public static final String COUNTRY_CODE = "countryCode";
    public static final String STORE = "store";
    public static final String OFFER_FIELD = "Offer";
    public static final String QTY_TO_REDUCE = "qtyToReduce";
    public static final String RENEWAL_QTY = "renewalQty";
    public static final String CUREENCY_CODE = "currency";

    public static final String INVOICE_NUMBER = "Invoice Number";
    public static final String DEFAULT_STORE_TYPE = "defaultStore";
    public static final String PURCHASE_ORDER = "PurchaseOrder";

    // DR upload constants
    public static final int SUB_ID_COLUMN = 11;
    public static final int SKU_CODE_COLUMN = 22;
    public static final int QUANTITY_COLUMN = 31;
    public static final int OXYGEN_ID_COLUMN = 84;
    public static final String DR_SUBS_VALID_DATA_FILE_NAME = "DRSubscriptions_ValidData.xlsx";

    // Banking conf Properties
    public static final String CORE = "CORE";
    public static final String REPUBLISH_MAX_LIMIT = "CHANGE_NOTIFICATION_REPUBLISH_MAX_RESULTS";
    public static final String REPUBLISH_CHANGE_NOTIFICATION_ENABLE = "REPUBLISH_CHANGE_NOTIFICATIONS";

    // Find BIC Releases Column names
    public static final String SUB_PLAN_PROD_LINE = "SubPlan Product Line";
    public static final String DOWNLOAD_PROD_LINE = "Download Product Line";
    public static final String DOWNLOAD_RELEASE = "Download Release";

    // BIC RELEASE Download Release values
    public static final String DOWNLOAD_RELEASE1 = "1.0";
    public static final String DOWNLOAD_RELEASE2 = "2.0";

    // Feature flag related constants
    public static final String FEATURE_FLAG = "FEATURE";
    public static final String TRUE = "True";
    public static final String FALSE = "False";
    public static final String SHOW_ES_LOGGING_DELAY_ON_REPORT = "SHOW_ES_LOGGING_DELAY_ON_REPORT";
    public static final String VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY = "VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY";
    public static final String SUBSCRIPTION_MIGRATION_FEATURE_FLAG = "SUBSCRIPTION_MIGRATION";
    public static final String REDUCE_SEATS_FEATURE_FLAG = "REDUCE_SEATS";
    public static final String CSE_HEADER_FEATURE_FLAG = "PO_CHANGE_NOTIFICATION_HEADER";
    public static final String STOP_LEGACY_FULFILLMENT_CALL_FLAG = "STOP_LEGACY_FULFILLMENT_CALL";
    public static final String ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG = "ASSIGNABLE_ENTITLEMENTS";
    public static final String REMOVE_FEATURES_FEATURE_FLAG = "REMOVE_FEATURES";
    public static final String DIRECT_DEBIT_ENABLED_FEATURE_FLAG = "DIRECT_DEBIT_ENABLED";
    public static final String IDEMPOTENT_RENEWALS = "IDEMPOTENT_RENEWALS";

    public static final String TOOLS_PATH =
        Util.getTestRootDir() + File.separator + "src/test/resources/tools" + File.separator;
    public static final String INVALID_INPUT = "INVALID INPUT";
    public static final String INVALID_NUMBER = "1234567890";

    // Find Upload Status Constants
    public static final String JOB_STATUS_NOT_STARTED_DROPDOWN = "Not Started";
    public static final String JOB_STATUS_IN_PROGRESS_DROPDOWN = "In Progress";
    public static final String JOB_STATUS_NOT_STARTED = "NOT_STARTED";
    public static final String JOB_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String JOB_STATUS_COMPLETED = "Completed";
    public static final String JOB_STATUS_FAILED = "Failed";
    public static final String ENTITY_ITEMS = "Items";
    public static final String ENTITY_BASIC_OFFERINGS = "Basic Offerings";
    public static final String ENTITY_SUBSCRIPTION_PLANS = "Subscription Plans";

    // Date & time format constants
    public static final String DATE_FORMAT_FOR_AUDIT_LOG_START_DATE = "yyyy-MM-dd 00:00:00";
    public static final String DATE_FORMAT_FOR_AUDIT_LOG_END_DATE = "yyyy-MM-dd 23:59:59";
    public static final String AUDIT_LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String UTC_TIME_ZONE = "UTC";
    public static final String DATE_FORMAT_WITH_SLASH = "MM/dd/yyyy";
    public static final String EMAIL_DATE_FORMAT = "MMMM d, yyyy";
    public static final String EMAIL_DATE_FORMAT_EXTENSION_ORDER = "M/d/yy";
    public static final String EMAIL_DATE_FORMAT_ADD_SEATS_ORDER = "M/d/yy";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_FORMAT = "M/d/yyyy";
    public static final String DATE_FORMAT_NO_SEPARATOR = "yyyyMMdd";
    public static final String DB_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String REPUB_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSSSSS";
    public static final String RENEWAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String DATE_TIME_WITH_ZONE = "MM/dd/yyyy HH:mm:ss z";
    public static final String DATE_FORMAT_RECEIVED_MAILBOX = "EEE MMM d HH:mm:ss zzz yyyy";
    public static final String DATE_FORMAT_WITH_HYPHEN = "MM-dd-yyyy";
    public static final String DATE_FORMAT_FROM_WORKERS_TABLE = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_WITH_TIME_ZONE = "MM/dd/yyyy HH:mm:ss zzz";
    public static final String DATE_FORMAT_WITH_MONTH_NAME = "MMMM d, yyyy";
    public static final String FEATURE_DISABLE_DATE_FORMAT = "MMM d, h:mma z";
    public static final String DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE = "MM/dd/yyyy";
    public static final String DATE_FORMAT_IN_TEXT = "MMM dd, yyyy";

    public static final String AUTO_FAMILY_SELECT_VALUE = "Automated Tests (AUTO)";
    public static final String IPP = "ipp";
    public static final String ESTORE = "estore";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String ANY = "-- ANY --";
    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_EN = "en";
    public static final String DESCRIPTOR = "Descriptor";
    public static final String DEFINITION_ID = "definitionId";
    public static final String DEFINITION = "Definition";
    public static final String VALUE = "value";
    public static final String VALUE_FIELD = "Value";
    public static final String COUNTRY = "country";
    public static final String EMPTY_STRING = "";
    public static final String AUDIT_DATA_ENTITY_ID = "entityId";
    public static final String NONE_FOUND = "None found";
    public static final String NONE = "None";
    public static final String SUBSCRIPTION_PLAN_WITH_NO_PRICE_ERROR = "You must add at least one price for offer : ";

    // Page titles
    public static final String PELICAN = "Pelican";
    public static final String USER_DETAIL_TITLE = "User Detail";
    public static final String SUBSCRIPTION_MIGRATION_TITLE = "Subscription Migration";
    public static final String SUBSCRIPTION_DETAIL_TITLE = "Subscription Detail";
    public static final String SUBSCRIPTION_MIGRATION_JOB_DETAIL_TITLE = "Migration Job Detail";
    public static final String UPLOAD_STATUS_TITLE = "Upload Status";
    public static final String PRODUCT_LINE_DETAIL_TITLE = "Product Line Detail";
    public static final String DOWNLOAD_PRODUCT_LINES_TITLE = "Download Product Lines";
    public static final String DOWNLOAD_PRODUCT_LINE = "ProductLine";
    public static final String CANCELLED_SUBSCRIPTION_REPORT = "Cancelled Subscriptions Report";
    public static final String SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE = "Subscription Search Results";

    // Page Headers
    public static final String LICENSING_MODEL_HEADER = "Licensing Model Detail";

    // External key for different user roles. Commonly used are ebso and gcso
    public static final String EBSO_USER_EXTERNAL_KEY = "Automation_test_ebso_user";
    public static final String GCSO_USER_EXTERNAL_KEY = "Automation_test_gcso_user";
    public static final String NON_EBSO_USER_EXTERNAL_KEY = "Automation_test_non_ebso_user";
    public static final String NON_GCSO_USER_EXTERNAL_KEY = "Automation_test_non_gcso_user";
    public static final String GCSO_EDIT_SUBSCRIPTION_USER_EXTERNAL_KEY = "Automation_test_gcso_edit_subscription_user";

    // External key for different user roles. Commonly used are for offering manager
    public static final String NON_OFFERING_MANAGER_USER_EXTERNAL_KEY = "Automation_test_non_offering_manager_user";
    public static final String READ_ONLY_USER = "Automation_test_read_only_user";
    public static final String ATC_ADMIN_ONLY_USER = "Automation_test_atc_admin_only_user";
    public static final String EBSO_ONLY_USER = "Automation_test_ebso_only_user";
    public static final String GCSO_ONLY_USER = "Automation_test_gcso_only_user";
    public static final String GDPR_ONLY_USER = "Automation_test_gdpr_only_user";
    public static final String OFFERING_MANAGER_ONLY_USER = "Automation_test_offering_manager_only_user";
    public static final String QA_ROLE_ONLY_USER = "Automation_test_qa_role_only_user";

    // prefix before random String
    public static final String PRODUCT_LINE_PREFIX = "SQA_Product_Line_";
    public static final String OFFER_EXT_KEY_PREFIX = "TestOfferKey_";
    public static final String OFFER_NAME_PREFIX = "TestOfferName_";
    public static final String SUB_PLAN_EXT_KEY_PREFIX = "SubPlan_ExtKey_";
    public static final String SUB_PLAN_NAME_PREFIX = "SubPlan_Name_";

    // promotion related variables
    public static final int PROMOTION_CODE_LENGTH = 13;
    public static final String PROMOTION_TYPE_DISCOUNT = "Discount";
    public static final String PROMOTION_DISCOUNT_TYPE_CASH = "Cash Discount";
    public static final String PROMOTION_DISCOUNT_TYPE_PERCENTAGE = "Percentage Discount";
    public static final String BUNDLED_COLUMN = "Bundled";
    public static final String STOREWIDE_COLUMN = "Store Wide";

    // Purchase Order Type
    public static final String ORDER_TYPE = "Order Type";
    public static final String ADD_SEATS_ORDER = "Add Seats";
    public static final String ADD_SEATS_DECLINED = "ADD_SEATS_DECLINED";
    public static final String SUBSCRIPTION_EXTENSION_ORDER = "Subscription Extension";

    // Sale Types
    public static final String NEW_ACQUISITION = "New Acquisition";
    public static final String AUTO_RENEWS = "Auto-Renews";

    // pagination
    public static final String META = "meta";
    public static final String PAGINATION = "pagination";
    public static final String BLOCK_SIZE = "blockSize";
    public static final String SKIP_COUNT = "skipCount";
    public static final String COUNT = "count";
    public static final String START_INDEX = "startIndex";

    // Report
    public static final String VIEW = "View";
    public static final String DOWNLOAD = "Download";
    public static final String USER_ID_FIELD = "User ID";
    public static final String USER_NAME_FIELD = "User Name";
    public static final String USER_EXTERNAL_KEY_FIELD = "User External Key";
    public static final String APPLICATION_FAMILY_ID_FIELD = "Application Family ID";
    public static final String APPLICATION_FAMILY_NAME_FIELD = "Application Family Name";
    public static final String EXTERNAL_KEY_FIELD = "External Key";
    public static final String NAME_FIELD = "Name";
    public static final String LAST_MODIFIED_DATE_FIELD = "Last Modified Date";
    public static final String JOB_ID = "Job ID";
    public static final String RUN_DATE = "Run Date";
    public static final String RUN_BY = "Run By";
    public static final String PLAN_ID_FIELD = "Plan ID";
    public static final String PLAN_EXTERNAL_KEY_FIELD = "Plan Ext Key";
    public static final String PLAN_NAME_FIELD = "Plan Name";
    public static final String PLAN_STATUS_FIELD = "Plan Status";
    public static final String PLAN_USAGE_TYPE_FIELD = "Plan Usage Type";
    public static final String PRODUCT_LINE_CODE_FIELD = "Product Line Code";
    public static final String PRODUCT_LINE_NAME_FIELD = "Product Line Name";
    public static final String FEATURE_EXTERNAL_KEY_FIELD = "Feature Ext Key";
    public static final String FEATURE_NAME_FIELD = "Feature Name";
    public static final String FEATURE_TYPE_EXTERNAL_KEY_FIELD = "Feature Type Ext Key";
    public static final String FEATURE_TYPE_NAME_FIELD = "Feature Type Name";
    public static final String LICENSING_MODEL_FIELD = "Licensing Model";
    public static final String PARENT_FEATURE_FIELD = "Parent Feature";
    public static final String CURRENCY_NAME_REPORT = "Currency Name";
    public static final String SKU_REPORT = "SKU";
    public static final String AMOUNT_REPORT = "Amount";
    public static final String OFFERING_DETAIL_EXTERNAL_KEY_FIELD = "Offering Detail Ext Key";
    public static final String STORE_FIELD = "Store";
    public static final String SEATS_FIELD = "Seats";
    public static final String NEXT_BILLING_DATE_FIELD = "Next Billing Date";
    public static final String NEXT_BILLING_AMOUNT_FIELD = "Next Billing Amount";
    public static final String PLAN_FIELD = "Plan";
    public static final String SUBSCRIPTION_ID_FIELD = "Subscription ID";
    public static final String SUBSCRIPTION_OWNER_FIELD = "Subscription Owner";
    public static final String AUTO_RENEW_FIELD = "Auto-Renew";
    public static final String SUBSCRIPTION_STATUS_FIELD = "Subscription Status";
    public static final String CANCELLED_DATE_FIELD = "Cancelled Date";
    public static final String EXPIRATION_DATE_FIELD = "Expiration Date";
    public static final String REQUESTOR_FIELD = "Requestor";
    public static final String PAYMENT_METHOD = "Payment Method";
    public static final String CREDIT_CARD_TYPE = "Credit Card Type";
    public static final String SUBSCRIPTION_OFFER_FIELD = "Subscription Offer";
    public static final String BILLING_PERIOD = "Billing Period";
    public static final String CURRENT_QUANTITY = "Current Quantity";
    public static final String RENEWAL_QUANTITY = "Renewal Quantity";
    public static final String NEXT_BILLING_PRICE = "Next Billing Price";
    public static final String USAGE_FIELD = "Usage";
    public static final String PAYMENT_PROFILE_FIELD = "Payment Profile";
    public static final String SUBSCRIPTION_PLAN_FIELD = "Subscription Plan";
    public static final String EC_STATUS_FIELD = "EC Status";
    public static final String SALES_CHANNEL = "Sales Channel";

    // Audit Log Report
    public static final String PARENT_OBJECT_ID_FIELD = "Parent Object Id";
    public static final String PARENT_OBJECT_FIELD = "Parent Object";
    public static final String ENTITY_TYPE_FIELD = "Entity Type";
    public static final String OBJECT_FIELD = "Object";
    public static final String OBJECT_ID_FIELD = "Object Id";
    public static final String CHANGE_DATE_FIELD = "Change Date";
    public static final String USER_FIELD = "User";
    public static final String ACTION_FIELD = "Action";
    public static final String DESCRIPTION_FIELD = "Description";
    public static final String ES_LOGGING_DELAY_FIELD = "ES logging delay (sec)";
    public static final String ENTITY_SUBSCRIPTION_PLAN = "SubscriptionPlan";
    public static final String ENTITY_SUBSCRIPTION_OFFER = "SubscriptionOffer";
    public static final String ENTITY_SUBSCRIPTION_PRICE = "SubscriptionPrice";
    public static final String ENTITY_SUBSCRIPTION_ENTITLEMENT = "SubscriptionEntitlement";
    public static final String ENTITY_BASIC_OFFERING = "BasicOffering";
    public static final String ENTITY_FEATURES = "Features";
    public static final String ENTITY_FEATURE = "Item";
    public static final String ENTITY_DESCRIPTOR = "Descriptor";
    public static final String IS_ACTIVE = "isActive";
    public static final String AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME = "AuditLogReport.xlsx";
    public static final String FINANCE_REPORT_FILE_NAME = "FinanceReport.xlsx";
    public static final int DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT = 10;
    public static final String PENDING_PAYMENT = "pendingPayment";
    public static final String EXPIRATION_REMINDER_EMAILS_ENABLED = "sendExpirationReminderEmails";

    // Subscription Plan Audit Log Report
    public static final String OFFERING_TYPE_FIELD = "Offering Type";
    public static final String CANCELLATION_POLICY_FIELD = "Cancellation Policy";
    public static final String USAGE_TYPE_FIELD = "Usage Type";
    public static final String OFFERING_DETAIL_FIELD = "Offering Detail";
    public static final String PRODUCT_LINE_FIELD = "Product Line";
    public static final String SUPPORT_LEVEL_FIELD = "Support Level";
    public static final String PACKAGING_TYPE_FIELD = "Packaging Type";
    // Subscription Offer Audit Log Report
    public static final String BILLING_FREQUENCY_FIELD = "Billing Frequency";
    // Subscription Price Audit Log Report
    public static final String CURRENCY_FIELD = "Currency";
    public static final String AMOUNT_FIELD = "Amount";
    public static final String PRICE_LIST_FIELD = "Price List";
    public static final String EFFECTIVE_START_DATE_FIELD = "Effective Start Date";
    public static final String EFFECTIVE_END_DATE_FIELD = "Effective End Date";
    // Entitlement in Audit Log Report
    public static final String GRANT_TYPE_FIELD = "Grant Type";
    public static final String ENTITLEMENT_TYPE_FIELD = "Entitlement Type";
    public static final String ONE_TIME_ENTITLEMENTS = "One-Time Entitlements";
    // Feature Entitlement in Audit Log Report
    public static final String FEATURE_FIELD = "Feature";
    public static final String FEATURE_FIELD_SUBSCRIPTION_PLAN = "FEATURE";
    public static final String CURRENCY_FIELD_SUBSCRIPTION_PLAN = "CURRENCY";
    public static final String DISABLE_FEATURE_MESSAGE = "Feature (Disabled until ";
    public static final String CLOSING_PARANTHESIS = ")";
    public static final String CORE_PRODUCTS_FIELD = "Core Products";
    // Feature in Audit Log Report
    public static final String FEATURE_TYPE_FIELD = "Feature Type";
    // Basic Offering in Audit Log Report
    public static final String MEDIA_TYPE_FIELD = "Media Type";
    public static final String RECURRING = "Recurring";
    public static final String LIMITED_OFFER_TYPE = "Specify:";

    // Description value in Audit Log Report
    public static final String DESCRIPTION_CHANGES = "No changes on this object - possible changes on child object(s).";
    public static final String DESCRIPTION_CHANGES_FOR_UPLOAD_FEATURES =
        DESCRIPTION_CHANGES + "\nUploadFeatures.xlsx was uploaded.";
    public static final String DESCRIPTION_CHANGES_FOR_UPLOAD_PLAN =
        DESCRIPTION_CHANGES + "\nUploadSubscriptionPlan.xlsx was uploaded.";
    public static final String DESCRIPTION_CHANGES_FOR_UPLOAD_BASIC_OFFERING =
        DESCRIPTION_CHANGES + "\nUploadBasicOfferings.xlsx was uploaded.";

    // Environment variables
    public static final String DEFAULT_ENVIRONMENT = "wildflydev";

    public static final String REMOTE_WEBDRIVER = "remoteWebDriverurl";
    public static final String PLATFORM = "platform";
    public static final String ENVIRONMENT = "environmentType";
    public static final String TRIGGERS = "triggers";
    public static final String API_DOC_PARAMS = "/tfel2rs/doc/v2/";

    // HTTP statuses
    public static final int HttpStatusCode_OK = HttpStatus.SC_OK;
    public static final int HttpStatusCode_BAD_REQ = HttpStatus.SC_BAD_REQUEST;

    // Republish Change Notification
    public static final String CREATED_DATE = "Created Date";
    public static final String LAST_MODIFIED_DB = "LAST_MODIFIED";
    public static final String REPUB_PAGE_HEADER = "Republish Change Notifications";
    public static final String ZERO_DECIMALS = ".000";
    public static final String NINER_DECIMALS = ".999";
    public static final String CATEGORY = "Category";
    public static final String BATCH_SIZE = "Batch Size";
    public static final String STEP_COUNT = "Step Count";
    public static final String ROLL_BACK_RECORDS_COUNT = "Rollback Records Count";
    public static final String SKIPPED_RECORDS_COUNT = "Skipped Records Count";
    public static final String PROCESSED_RECORDS_COUNT = "Processed Records Count";

    // Auto generate offer and subscription plan external key
    public static final String AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX = "O-";
    public static final String AUTO_GENERATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_PREFIX = "SO-";

    // Boolean variables for true and false
    public static final Boolean TRUE_VALUE = true;
    public static final Boolean FALSE_VALUE = false;

    // Variables required for promotions
    public static final String DISCOUNT_PROMOTION_TYPE = "Discount";
    public static final String CASH_AMOUNT_TYPE = "Cash Amount";
    public static final String PERCENTAGE_DISCOUNT_TYPE = "Percentage";
    public static final String BASIC_OFFERING_TYPE = "Basic Offerings";
    public static final String SUBSCRIPTION_OFFERS_TYPE = "Subscription Offers";
    public static final String EXPECTED_CASH_TYPE = "Cash Discount";
    public static final String START_HOUR = "03";
    public static final String START_MINUTE = "08";
    public static final String START_SECOND = "56";
    public static final String END_HOUR = "09";
    public static final String END_MINUTE = "28";
    public static final String END_SECOND = "26";

    // Variable for empty values
    public static final String HIPHEN = "-";

    // cse
    public static final String PURCHASE_ORDERS = "purchaseOrders";
    public static final String CHANGE_NOTIFICATION_PRIORITY_HEADER = "low";
    public static final String CHANGE_NOTIFICATION_CONTEXT_HEADER = "subscription-migration";
    public static final String CHANGE_NOTIFICATION_CATEGORY_HEADER = "pelican-change.notifications";
    public static final String OFFERING_CHANGE_NOTIFICATION_CATEGORY_HEADER =
        "pelican-change.notifications-offering-updated";
    public static final String SUBSCRIPTION_CHANGE_NOTIFICATION_CATEGORY_HEADER = "subscription-change.notifications";
    public static final String SUBSCRIPTION_CHANGE_NOTIFICATION = "subscriptionChangeNotification";
    public static final String CHANGE_NOTIFICATIONS = "changeNotifications";
    public static final String CSE_PURCHASE_ORDERS = "purchaseOrders";
    public static final String CSE_PURCHASE_ORDER = "purchaseOrder";
    public static final String SUBSCRIPTION = "subscription";
    public static final String PURCHASEORDER = "purchaseOrder";
    public static final String ENTITLEMENT = "entitlement";
    public static final String SUBSCRIPTION_OFFERING = "subscriptionOffering";
    public static final String OFFERINGS = "offerings";
    public static final String SUBSCRIPTION_OFFERINGS = "subscriptionOfferings";
    public static final String BASIC_OFFERING = "basicOffering";
    public static final String CSE_PELICAN_CONTEXT_ADD_FEATURE = "add-feature";
    public static final String CSE_ADD_FEATURE_CHANGE_TYPE = "added";
    public static final String REQUESTER = "PELICAN";
    public static final String CHANNEL = "pelican-events-batch-bootstrap";

    // File upload
    public static final String INVALID_UPLOAD_FILE_NAME = "UploadInvalidJpgFile.jpg";

    // Sherpa upload constants
    public static final String SUBSCRIPTION_ID = "Subscription Id";
    public static final String SOURCE_PLAN_ID = "Source Plan Id";
    public static final String SOURCE_PLAN_NAME = "Source Plan Name";
    public static final String SOURCE_PLAN_EXTERNAL_KEY = "Source Plan External Key";
    public static final String SOURCE_OFFER_NAME = "Source Offer Name";
    public static final String SOURCE_OFFER_EXTERNAL_KEY = "Source Offer External Key";
    public static final String SOURCE_PRICE_ID = "Source Price ID";
    public static final String SOURCE_AMOUNT = "Source Amount";
    public static final String SOURCE_PRICE_END_DATE = "Source Price End Date";
    public static final String TARGET_PLAN_ID = "Target Plan Id";
    public static final String TARGET_PLAN_EXTERNAL_KEY = "Target Plan External Key";
    public static final String TARGET_PLAN_NAME = "Target Plan Name";
    public static final String TARGET_OFFER_EXTERNAL_KEY = "Target Offer External Key";
    public static final String TARGET_OFFER_NAME = "Target Offer Name";
    public static final String TARGET_PRICE_ID = "Target Price ID";
    public static final String TARGET_AMOUNT = "Target Amount";
    public static final String TARGET_PRICE_END_DATE = "Target Price End Date";
    public static final String STORE_UPLOAD = "Store";
    public static final String SUBSCRIPTION_COUNT = "Subscription Count";
    public static final String MAPPING_RESULTS = "Mapping Results";
    public static final String EMPTY_VALUE = "";
    public static final String HASH_VALUE = "#";
    public static final String PARENT_JOB = "Parent Job";
    public static final String ROLLBACK_JOB = "Rollback Job";
    public static final String Rollback_JOB_STATUS = "Rollback Job Status";
    public static final String CLDCR = "CLDCR";
    public static final String CLOUD = "CLOUD";
    public static final String CLOUD_CURRENCY_SELECT = "Cloud Credits (CLOUD)";

    // DR Subscription Migration Report
    public static final String SUCCESSFULLY_MIGRATED = "Successfully Migrated";
    public static final String MIGRATED_BUT_FAILED_TO_UPDATE_IN_DR_OR_SIEBEL =
        "Migrated, but failed to update status in DR/Siebel";

    // BootStrap Constants
    public static final String SUBSCRIPTION_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB =
        "subscriptionChangeNotificationBootstrapJob";
    public static final String PURCHASE_ORDER_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB =
        "purchaseOrderChangeNotificationBootstrapJob";
    public static final String ENTITLEMENT_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB =
        "entitlementChangeNotificationBootstrapJob";
    public static final String SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB =
        "subscriptionOfferingChangeNotificationBootstrapJob";
    public static final String BASIC_OFFERING_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB =
        "basicOfferingChangeNotificationBootstrapJob";
    public static final String ID_RANGE = "Id Range";
    public static final String DATE_RANGE = "Date Range";

    // signs constants
    public static final String OK = "✔";

    // Username
    public static final String AUTO_USER_NAME = "svc_p_pelican";

    // SOAP Wsdl Namespaces
    public static final String CONVERGENT_CHARGING_NAMESPACE =
        "http://www.autodesk.com/schemas/Business/ConvergentChargingV1.0";

    public static final String DAYS_LOWER_CASE = "days";
    public static final String DAYS_CAMEL_CASE = "Days";

    // Licensing model constants
    // Note: Trail is the Name for
    public static final String RETAIL_LICENSING_MODEL_NAME = "Trial";
    public static final String RETAIL_LICENSING_MODEL_EXTERNAL_KEY = "TRL#2";

    // Core Product Constants
    public static final String CORE_PRODUCT_AUTO_1 = "AUTO_CORE1";
    public static final String CORE_PRODUCT_AUTO_2 = "AUTO_CORE2";

    // Search Results Constants
    public static final String ZERO_RESULTS = "0 results";

    // ConvergentCharging - QuerySubscriptionBalanceResponse
    public static final String CC_SERVICE_PRIVILEGE_DESCRIPTION = "Cloud Credits";
    public static final String CC_UNIT_OF_MEASURE_UNIT_AVAILABLE = "available";

    // Cloud credit subscription activity
    public static final String CLOUD_CREDITS_TRLCR_GRANTED_IN_CONVERGENT_CHARGING =
        "Cloud Credits (%s TRLCR). Granted in Convergent Charging.";
    public static final String CLOUD_CREDITS_TRLCR_MESSAGE_SENT_TO_QUEUE =
        "Cloud Credits (%s TRLCR). Message sent to Queue.";

    // GDPR MESSAGE
    public static final String GDPR_MESSAGE =
        "User's data found and not deleted. retained for business reasons like SOX";

    // subscription activity
    public static final String CREDIT = "CREDIT";
    public static final String ADD_SEATS = "ADD_SEATS";
    public static final String REDUCE_SEATS = "REDUCE_SEATS";
    public static final String SUBSCRIPTION_ACTIVITY_FOR_PENDING_PAYMENT_FLAG_UPDATE = "PENDING_PAYMENT_FLAG_UPDATE";
    public static final String EXPIRATION_REMINDER = "EXPIRATION_REMINDER_SENT";
    public static final String EDIT = "EDIT";

    public static final String CHARGE = "CHARGE";
    public static final String EXTENSION_DECLINED = "EXTENSION_DECLINED";
    public static final String EXTENSION = "EXTENSION";
    public static final String PAYMENT_PROFILE_UPDATE = "PAYMENT_PROFILE_UPDATE";

    // License Model
    public static final String LICENSE_MODEL_PREFIX = "SQA_License_Model_";
    public static final String APPEND_EDIT_TEXT = "_edit";

    public static final String SUBSCRIPTION_WITH_ID = "Subscription with id: ";

    // Job Reports
    public static final String JOB_GUID = "Job GUID";
    public static final String WIP_GUID = "Wip GUID";
    public static final String OBJECT_TYPE = "Object Type";
    public static final String OBJECT_ID = "Object Id";

    public static final String NEXT_BILLING_DATE_AUDIT_LOG = "nextBillingDate";

    public static final String MARK_AS_REFUND_NOTES = "Automation mark as refunded";

    public static final String INCLUDE_ENTITLEMENTS = "entitlements";
    public static final String INCLUDE_DESCRIPTORS = "descriptors";

    public static final String LOCAL_DESCRIPTOR_API = "AUTO_TEST_LOCAL_DESCRIPTOR_API";
    public static final String DESCRIPTOR_API = "AUTO_TEST_DESCRIPTOR_API";

    public static final int RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY = 30;
    public static final int RENEWAL_REMINDER_EMAIL_DAYS_MONTHLY = 7;

    public static final String PROPERTIES_MESSAGE = "providerErrorMessage";
    public static final String PROPERTIES_ERROR = "providerErrorCode";

    public static final String CSR_FEATURE_TYPE_EXTERNAL_KEY = "'CSR'";
    public static final String CSR_EXTERNAL_KEY = "CSR";

    public static final String ITEM_TYPE = "Type:";
    public static final String ITEM_TYPE_VALUE = "ITEM";
    public static final String ASSIGNABLE_COLUMN_NAME = "Assignable";
    public static final String AUDIT_ASSIGNABLE_COLUMN_NAME = "assignable";
    public static final String ASSIGNABLE_COLUMN_VALUE = "true";
    public static final String EOS_DATE_COLUMN_NAME = "EOS Date";
    public static final String AUDIT_EOS_DATE_COLUMN_NAME = "eosDate";
    public static final String EOL_RENEWAL_DATE_COLUMN_NAME = "EOL Renewal Date";
    public static final String AUDIT_EOL_RENEWAL_DATE_COLUMN_NAME = "eolRenewalDate";
    public static final String EOL_IMMEDIATE_DATE_COLUMN_NAME = "EOL Immediate Date";
    public static final String AUDIT_EOL_IMMEDIATE_DATE_COLUMN_NAME = "eolImmediateDate";
    public static final String ENTITLEMENTS_END_DATE_REACHED_REPORT = "ENTITLEMENTS_END_DATE_REACHED";
    public static final String CSE_HEADER_REMOVE_ENTITLEMENTS_CATEGORY =
        "pelican-change.notifications-offering-entitlements-end-date-reached";
    public static final String CSE_HEADER_REMOVE_ENTITLEMENTS_CONTEXT = "offering-entitlements-end-date-reached";
    public static final String CSE_APP_FAMILY = "applicationFamilies";
    public static final String CSE_REMOVE_ENTITLEMENT_CHANGE_TYPE = "endDateReached";
    public static final String ENTITLEMENTS_END_DATE_JOB_CATEGORY = "32";
    public static final String ENTITLEMENTS_END_DATE_WORK_IN_PROGRESS_OBJECT_TYPE = "8";
}
