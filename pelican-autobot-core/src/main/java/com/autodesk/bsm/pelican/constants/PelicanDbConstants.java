package com.autodesk.bsm.pelican.constants;

/**
 * These constants are common for all pelican entities.
 *
 * @author yerragv
 */
public class PelicanDbConstants {

    public static final String WHERE_ID_CONDITION = " where id = ";
    public static final String APP_FAMILY_ID_CONDITION_IN_QUERY = " where app_family_id = ";

    // Collection of DB FIELDS
    public static final String AMOUNT_DB_FIELD = "amount";
    public static final String NEXT_BILLING_DATE_DB_FIELD = "next_billing_date";
    public static final String BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS =
        "BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS";
    public static final String SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_IN_HOURS =
        "SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_IN_HOURS";
    public static final String SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_CREATED_IN_DAYS =
        "SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_CREATED_IN_DAYS";
    public static final String VALUE = "V";
    public static final String SELECT_VALUE_FROM_MIDAS_HIVE = "select " + VALUE + " from midas_hive where k like '%s'";

    // Collection of DB select queries
    // Select Section:
    public static final String SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE =
        "select amount from subscription_price" + WHERE_ID_CONDITION;
    public static final String SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION =
        "select next_billing_date from subscription" + WHERE_ID_CONDITION;
    public static final String SELECT_PURCHASE_ORDER_ID_FROM_SUBSCRIPTION =
        "select purchase_order_id from subscription" + WHERE_ID_CONDITION;
    public static final String SELECT_FIELD_FROM_SUBSCRIPTION = "select %s from subscription where id = %s";

    public static final String SELECT_SQL_FOR_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL =
        "Select ID from subscription where sales_channel = 0 and status = 0 and QTY_TO_REDUCE is null and "
            + "APP_FAMILY_ID = ";
    public static final String SELECT_SUBSCRIPTION_WITH_QUANTITY_IS_ONE =
        "select id from subscription where quantity =1 " + "and APP_FAMILY_ID = '%s' limit 5";
    public static final String SQL_QUERY_ID_FROM_LICENSING_MODEL =
        "select ID from LICENSING_MODEL where EXTERNAL_KEY = '";
    public static final String SELECT_ID_SUBSCRIPTION_ENTITLEMENT =
        "Select ID from subscription_entitlement where RELATED_ID='%s' and ITEM_ID='%s' order by ID desc";
    public static final String SELECT_SINGLE_ID_SUBSCRIPTION_ENTITLEMENT =
        "Select ID from subscription_entitlement where RELATED_ID='%s' order by ID desc";
    public static final String SELECT_ID_CURRENCY_ENTITLEMENT =
        "Select ID from subscription_entitlement where RELATED_ID='%s' and GRANT_TYPE = 0 order by ID desc";
    public static final String UPDATE_EOS_SUBSCRIPTION_ENTITLEMENT =
        "update subscription_entitlement set EOS_DATE = '%s' where id='%s'";
    public static final String UPDATE_EOL_IMME_SUBSCRIPTION_ENTITLEMENT =
        "update subscription_entitlement set EOL_IMMEDIATE_DATE = '%s' where id='%s'";
    public static final String UPDATE_EOL_RENEWAL_SUBSCRIPTION_ENTITLEMENT =
        "update subscription_entitlement set EOL_RENEWAL_DATE = '%s' where id='%s'";
    public static final String UPDATE_REMOVE_FEATURE_DATES_SUBSCRIPTION_ENTITLEMENT =
        "update subscription_entitlement set EOS_DATE = '%s', EOL_IMMEDIATE_DATE = '%s', EOL_RENEWAL_DATE = '%s'"
            + "  where id='%s'";
    public static final String SELECT_ID_OFFERING =
        "select id from offering where EXTERNAL_KEY='%s' and APP_FAMILY_ID='%s'";
    public static final String SELECT_ID_OFFER =
        "select id from subscription_offer where EXTERNAL_KEY='%s' and APP_FAMILY_ID='%s'";
    public static final String SELECT_PRICE =
        "select id from subscription_price where OFFER_ID = '%s' and currency_id = %s";
    public static final String SELECT_SUBSCRIPTION_OFFER_ID = "select ID from SUBSCRIPTION_OFFER where PLAN_ID = '%s'";

    // Updates Section:
    public static final String UPDATE_SUBSCRIPTION_WITH_STATUS_PENDING_PAYMENT_FIELDS =
        "update subscription set status=%s, pending_payment = %s where id = %s";
    public static final String UPDATE_SUBSCRIPTION_STATUS = "update subscription set status=%s where id = %s";

    // Collection of DB Update queries
    public static final String UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION = "update subscription set next_billing_date =";
    public static final String UPDATE_SUBSCRIPTION_TABLE = "update subscription ";
    public static final String UPDATE_BIC_RELEASE_TABLE = "update bic_release set IS_ACTIVE = 0 where id in (";
    public static final String UPDATE_BIC_RELEASE_TABLE_STATUS = "update bic_release set IS_ACTIVE = 0";
    public static final String UPDATE_TABLE_NAME = "update subscription set next_billing_date = ";
    public static final String UPDATE_CONDITION = " where id = ";
    public static final String DB_FIELD_NEXT_BILLING_DATE = "next_billing_date";
    public static final String UPDATE_CREATED_DATE_IN_SUBSCRIPTION = " set created = ";
    public static final String UPDATE_STATUS_IN_SUBSCRIPTION = " status =";

    public static final String UPDATE_SQL_TO_SET_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL =
        "Update subscription set QTY_TO_REDUCE = null where id =";
    public static final String SQL_QUERY_FOR_UPDATE_NEXT_BILLING_DATE =
        "Update subscription set NEXT_BILLING_DATE='%s' where id = '%s'";
    public static final String SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE =
        "Update subscription_price set START_DATE= '%s' where offering_id = '%s' ";
    public static final String SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE =
        "Update subscription_price set END_DATE= '%s' where offering_id = '%s' ";
    public static final String SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE_WHERE_PRICE_ID =
        "Update subscription_price set END_DATE= '%s' where id = '%s' ";
    public static final String SQL_QUERY_TO_UPDATE_START_AND_END_DATE =
        "update subscription_price set START_DATE='%s', END_DATE='%s' where id = '%s'";
    public static final String SQL_QUERY_TO_UPDATE_OFFER_EXTERNAL_KEY =
        "update subscription_offer set EXTERNAL_KEY = '%S' where PLAN_ID = '%s'";
    public static final String SQL_QUERY_TO_UPDATE_PRICE_AMOUNT =
        "update subscription_price set amount = %s where id = %s";

    // Update Query for Finance Report
    public static final String SQL_QUERY_TO_UPDATE_FINANCE_REPORT_TABLE =
        "update finance_report set order_date='%s' where PURCHASE_ORDER_ID = '%s'";

    public static final String SQL_QUERY_TO_SELECT_UNIQUE_USER_EXTERNAL_KEY =
        "select distinct user_external_key from " + "finance_report order by order_date desc limit %s";

    // Select Query for Purchase Order.
    public static final String SELECT_SFDC_CASE_FOR_PO =
        "select SFDC_CASE_NUMBER from pending_purchase_order_sfdc_case where PURCHASE_ORDER_ID = %s";
    public static final String SQL_QUERY_TO_UPDATE_PURCHASE_ORDER_CREATE_DATE =
        "update purchase_order set CREATED = '%s' where ID= '%s'";
    public static final String SELECT_COUNT_OF_POS = "select count(*) from subscription where purchase_order_id = ";

    // Select Query for Credentials.
    public static final String SELECT_CRED_FOR_ACTOR = "select count(*) from cred where NP_ID =%s";

    // Select Query for Actor
    public static final String SELET_ACTOR = "select count(*) from NAMED_PARTY where id =%s";

    // Update GDPR delete flag on named_party table
    public static final String UPDATE_GDPR_DELETE_FLAG = "update named_party set GDPR_DELETED = %s where ID = %s";

    // Select Name Party Table
    public static final String SELECT_GDPR_DELETE_FROM_NAMED_PARTY =
        "select GDPR_DELETED from named_party where XKEY = ";

    public static final String SELECT_ITEM_TYPE_ID = "Select id from item_type where xkey = %s and app_id in (%s)";
    public static final String SELECT_ITEM_TYPE_XKEY = "Select xkey from item_type where id = (%s)";

    // Select query from work_in_progress
    public static final String SELECT_WORK_IN_PROGRESS_WITH_JOB_STATUS_ID =
        "select * from work_in_progress where job_status_id = '%s'";
    public static final String UPDATE_WORK_IN_PROGRESS_WITH_OBJECT_ID =
        "update work_in_progress set state = %s where job_status_id = '%s' and object_id = '%s'";
    public static final String DELETE_WORK_IN_PROGRESS_WITH_JOB_ID =
        "delete from work_in_progress where job_status_id = '%s'";
    public static final String DELETE_WORK_IN_PROGRESS_WITH_OBJECT_ID =
        "delete from work_in_progress where object_id = '%s'";
    public static final String DELETE_WORK_IN_PROGRESS_WITH_START_DATE =
        "delete from work_in_progress where object_type=%s and created like \"%s\"";

    // Select query from job_statuses
    public static final String SELECT_JOB_STATUSES_WITH_GUID = "select id from job_statuses where guid = '%s'";
    public static final String UPDATE_JOB_STATUSES_WITH_ID = "update job_statuses set state = '%s' where id = '%s'";
    public static final String DELETE_JOB_STATUSES_WITH_START_DATE =
        "delete from job_statuses where category = %s and created like \"%s\"";
}
