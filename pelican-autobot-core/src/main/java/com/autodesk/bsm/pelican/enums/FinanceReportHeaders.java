package com.autodesk.bsm.pelican.enums;

public enum FinanceReportHeaders {

    ORDER_NUMBER("Order No"),
    TRANSACTION_ID("Transaction ID"),
    ORDER_DATE("Order Date"),
    CUSTOMER_NAME("Customer Name"),
    O2_ID("O2 ID"),
    PRODUCTLINE_CODE("Product Line Code"),
    PRODUCT_ID("Product ID"),
    PRODUCT_NAME("Product Name"),
    BILLING_TERM("Billing Term"),
    SUBSCRIPTION_ID("Subscription ID"),
    QUANTITY("Quantity"),
    LIST_PRICE("List Price"),
    TOTAL_PRICE("Total Price"),
    CREDIT_DAYS_DISCOUNT("Credit Days Discount"),
    UNIT_DISCOUNT("Unit Discount"),
    TAX("Tax"),
    TOTAL_ORDER_PRICE("Total Order Price"),
    BILL_TO_PROVINCE("Bill To Province"),
    BILL_TO_POSTAL_CODE("Bill To Postal Code"),
    BILL_TO_COUNTRY("Bill To Country"),
    SALE_TYPE("Sale Type"),
    ORDER_STATUS("Order Status"),
    CURRENCY("Currency"),
    ORDER_ORIGIN("Order Origin"),
    OFFERING_TYPE("Offering Type"),
    OFFERING_DETAILS_NAME("Offering Details Name"),
    OFFERING_DETAILS_EXTERNAL_KEY("Offering Details External Key"),
    SUPPORT_LEVEL("Support Level"),
    MEDIA_TYPE("Media Type"),
    NEXT_BILLING_DATE("Next Billing Date"),
    EXTENDED_LIST_PRICE("Extended List Price"),
    EXTENDED_TOTAL_DISCOUNT("Extended Total Discount"),
    PROMO_NAME("Promo Name"),
    PROMO_TYPE("Promo Type"),
    PROMO_SUB_TYPE("Promo Sub Type"),
    SHIP_TO_PROVINCE("Ship To Province"),
    SHIP_TO_POSTAL_CODE("Ship To Postal Code"),
    SHIP_TO_COUNTRY("Ship To Country"),
    PAYMENT_TYPE("Payment Type"),
    PSP("PSP"),
    STORE("Store"),
    SUBSCRIPTION_START_DATE("Subscription Period Start Date"),
    SUBSCRIPTION_END_DATE("Subscription Period End Date"),
    SAP_CONTRACT_START_DATE("SAP Contract Start Date"),
    SAP_CONTRACT_END_DATE("SAP Contract End Date"),
    FULFILLMENT_DATE("Fulfillment Date"),
    VAT_REGISTRATION_ID("Vat Registration Id"),
    VAT_RATE_APPLIED("Vat Rate Applied"),
    TAX_CODE("Tax Code"),
    INVOICE_NUMBER("Invoice Number"),
    IP_COUNTRY("IP Country"),
    LAST_MODIFIED_DATE("Last Modified Date"),
    PROMO_SETUP_AMOUNT("Promo Setup Amount"),
    PROMO_SETUP_UNIT("Promo Setup Unit"),
    USAGE_TYPE("Usage Type"),
    SUBSCRIPTION_PLAN_EXTERNAL_KEY("Subscription Plan External Key"),
    SUBSCRIPTION_PLAN_NAME("Subscription Plan Name"),
    CONTRACT_MODIFICATION("Contract Modification"),
    SUBSCRIPTION_ADDED_TO("Subscription Added To");
    private String columnHeader;

    FinanceReportHeaders(final String columnHeader) {

        this.columnHeader = columnHeader;
    }

    public String getHeader() {
        return columnHeader;
    }
}
