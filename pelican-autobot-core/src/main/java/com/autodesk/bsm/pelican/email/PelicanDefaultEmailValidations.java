package com.autodesk.bsm.pelican.email;

import com.autodesk.bsm.pelican.api.clients.CheckPelicanEmailClient;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.constants.EmailConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PelicanDefaultEmailValidations {

    private static final String PRODUCT_LINE_QUERY = "select pl.name from subscription_price sp join offering o on "
        + "sp.offering_id = o.id join product_line pl on o.product_line_id = pl.id where sp.id = ";
    private static final String PRODUCT_LINE_NAME = "pl.name";
    private static final String NEW_ACQUISITION_DECLINED_REASON_IN_EMAIL =
        "The export review process determined your order does not " + "comply with United States export regulations.";
    private static final String RENEWAL_DECLINED_REASON_IN_EMAIL =
        "The export review process determined your order no longer "
            + "complies with United States export regulations.";
    private static final String MAIL_SUBJECT_FOR_NEW_ACQUISTION_1 = "Your ";
    private static final String MAIL_SUBJECT_FOR_NEW_ACQUISTION_2 = " order was declined";
    private static final String MAIL_SUBJECT_FOR_RENEWAL_1 = "Your auto-renewal order for ";
    private static final String MAIL_SUBJECT_FOR_RENEWAL_2 = " was declined";
    private static final String MAIL_SUBJECT_FOR_PAYMENT_ERROR = "[Action Required] Renewal payment error";

    /**
     * Helper class for credit note memo
     *
     * @return true or false, based on mail found/not found
     */
    public static Boolean creditNoteMemo(final String purchaseOrder, final EnvironmentVariables environmentVariables) {

        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        // get InvoiceNumber for the purchase order from purchase_order table
        final String sqlQueryInvoiceNumber =
            "select invoice_number from purchase_order where id=\"" + purchaseOrder + "\"";

        // get Credit Note number for the purchase order from purchase_order
        // table
        final String sqlQueryCreditNoteNumber =
            "select credit_note_number from purchase_order where id=\"" + purchaseOrder + "\"";

        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get Subscription Id for the purchase order from finance_report table
        final String subscriptionId =
            "select subscription_id from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get Next Billing Date for the purchase order from finance_report table
        final String nextBillingDate =
            "select next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get billing currency for the purchase order from finance_report table
        final String sqlQueryBillingCurrency =
            "select billing_currency from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // query which returns the subscription id as list
        final List<String> subscriptionIds =
            DbUtils.selectQuery(subscriptionId, "subscription_id", environmentVariables);

        // query which returns the next billing date as list
        final List<String> nextBillingDates =
            DbUtils.selectQuery(nextBillingDate, "next_billing_date", environmentVariables);

        // query which returns the invoicenumber as list(this is limitation of
        // the method)
        final List<String> invoiceNumberList =
            DbUtils.selectQuery(sqlQueryInvoiceNumber, "invoice_number", environmentVariables);

        // query which returns the creditnotenumber as list(this is limitation
        // of the method)
        final List<String> creditNoteNumberList =
            DbUtils.selectQuery(sqlQueryCreditNoteNumber, "credit_note_number", environmentVariables);

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList =
            DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);

        // query which returns the billingcurrency as list(this is limitation of
        // the method)
        final List<String> billingCurrencyList =
            DbUtils.selectQuery(sqlQueryBillingCurrency, "billing_currency", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);
        validationChecklist.add(subscriptionIds.get(0));
        validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
            PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));

        validationChecklist.add(invoiceNumberList.get(0));
        validationChecklist.add(creditNoteNumberList.get(0));
        validationChecklist.add(paymentTypeList.get(0));
        validationChecklist.add(billingCurrencyList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);
        // check for credit note email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.creditNoteMemo(purchaseOrder, billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * Helper class for order complete
     *
     * @param isBic
     * @param buyerEmailIds
     * @return true or false, based on mail found/not found
     */
    public static boolean orderComplete(final String purchaseOrder, final EnvironmentVariables environmentVariables,
        final boolean isBic, final List<String> buyerEmailIds) {
        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get Next Billing Date for the purchase order from finance_report table
        final String nextBillingDate =
            "select next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList =
            DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);

        // query which returns the next billing date as list
        final List<String> nextBillingDates =
            DbUtils.selectQuery(nextBillingDate, "next_billing_date", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);
        if (isBic) {
            // get Subscription Id for the purchase order from finance_report table
            final String subscriptionId =
                "select subscription_id from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

            // query which returns the subscription id as list
            final List<String> subscriptionIds =
                DbUtils.selectQuery(subscriptionId, "subscription_id", environmentVariables);

            validationChecklist.add(subscriptionIds.get(0));

            final List<String> quantity =
                DbUtils.selectQuery("select QUANTITY from subscription where ID = " + subscriptionIds.get(0),
                    "quantity", environmentVariables);

            validationChecklist.add(quantity.get(0));
            validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
                PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));
        }

        validationChecklist.add(paymentTypeList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Order confirmation email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        for (final String emailId : buyerEmailIds) {
            CheckPelicanEmailClient.orderComplete(purchaseOrder, emailId, validationChecklist);
        }

        return true;
    }

    /**
     * Helper class for order complete
     *
     * @param isBic
     * @param buyerEmailIds
     * @return true or false, based on mail found/not found
     */
    public static boolean orderCompleteWithPromotion(final String purchaseOrder,
        final EnvironmentVariables environmentVariables, final boolean isBic, final List<String> buyerEmailIds,
        final String promotionDiscount, final String totalPriceAfterPromotion) {
        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type,next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrder
                + "\"";

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList =
            DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);

        // query which returns the next billing date as list
        final List<String> nextBillingDates =
            DbUtils.selectQuery(sqlQueryPaymentType, "next_billing_date", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);
        if (isBic) {
            // get Subscription Id for the purchase order from finance_report table
            final String subscriptionId =
                "select subscription_id from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

            // query which returns the subscription id as list
            final List<String> subscriptionIds =
                DbUtils.selectQuery(subscriptionId, "subscription_id", environmentVariables);

            validationChecklist.add(subscriptionIds.get(0));
            validationChecklist.add("Total Savings");
            validationChecklist.add(promotionDiscount);
            validationChecklist.add("Total Order");
            validationChecklist.add(totalPriceAfterPromotion);

            final List<String> quantity =
                DbUtils.selectQuery("select QUANTITY from subscription where ID = " + subscriptionIds.get(0),
                    "quantity", environmentVariables);

            validationChecklist.add(quantity.get(0));
            validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
                PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));
        }

        validationChecklist.add(paymentTypeList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Order confirmation email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        for (final String emailId : buyerEmailIds) {
            CheckPelicanEmailClient.orderComplete(purchaseOrder, emailId, validationChecklist);
        }

        return true;
    }

    /**
     * Helper method for scf email
     *
     * @param environmentVariables
     * @param offerAndFeatureNamesList
     * @return
     */
    public static boolean checkScfEmail(final EnvironmentVariables environmentVariables,
        final List<String> offerAndFeatureNamesList, final boolean shouldWait) {

        final String billingEmail = environmentVariables.getUserEmail();

        if (shouldWait) {
            Util.waitInSeconds(TimeConstants.FIVE_MINS);
        }
        CheckPelicanEmailClient.scfEmail(billingEmail, offerAndFeatureNamesList);

        return true;
    }

    /**
     * Helper class for Order Fulfillment
     *
     * @param isPerpetual TODO
     * @return true or false, based on mail found/not found
     */
    public static boolean orderFulfillment(final String purchaseOrder, final EnvironmentVariables environmentVariables,
        final Boolean isPerpetual) {
        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList =
            DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);

        if (!isPerpetual) {
            // get Subscription Id for the purchase order from finance_report table
            final String subscriptionId =
                "select subscription_id from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

            // get Next Billing Date for the purchase order from finance_report table
            final String nextBillingDate =
                "select next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

            // query which returns the subscription id as list
            final List<String> subscriptionIds =
                DbUtils.selectQuery(subscriptionId, "subscription_id", environmentVariables);

            // query which returns the next billing date as list
            final List<String> nextBillingDates =
                DbUtils.selectQuery(nextBillingDate, "next_billing_date", environmentVariables);
            validationChecklist.add(subscriptionIds.get(0));
            validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
                PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));
        }

        validationChecklist.add(paymentTypeList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Order confirmation email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.orderFulfillment(purchaseOrder, billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * Helper class for auto email
     *
     * @return true or false, based on mail found/not found
     */
    public static boolean autoRenewal(final String purchaseOrderId, final String subscriptionId,
        final EnvironmentVariables environmentVariables) {
        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type, next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrderId
                + "\"";

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList =
            DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);

        // query which returns the next billing date as list
        final List<String> nextBillingDates =
            DbUtils.selectQuery(sqlQueryPaymentType, "next_billing_date", environmentVariables);

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrderId + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        validationChecklist.add(subscriptionId);
        validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
            PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));
        validationChecklist.add(paymentTypeList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Order confirmation email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.autoRenewal(purchaseOrderId, billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * Helper class for refund email
     *
     * @return true or false, based on mail found/not found
     */
    public static boolean refundEmail(final String purchaseOrder, final EnvironmentVariables environmentVariables) {

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);

        // check for Invoice/send tax email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.refundConfirmation(purchaseOrder, billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * method to find whether or not emails are being sent for new orders in mailBox if it is declined beacuse of export
     * control
     *
     * @param priceId
     * @param recipientMail
     * @param environmentVariables
     * @param purchaseOrderId TODO
     * @return true or false, based on mail found/not found
     */

    public static void declinedOrderEmailForNewAcquisition(final String priceId, final String recipientMail,
        final EnvironmentVariables environmentVariables, final String purchaseOrderId) {

        // query which returns the name of the product line which is required to find subject of an email
        final String productLine =
            DbUtils.selectQuery((PRODUCT_LINE_QUERY + priceId), PRODUCT_LINE_NAME, environmentVariables).get(0);

        final ArrayList<String> validationChecklist = new ArrayList<>();

        validationChecklist.add(NEW_ACQUISITION_DECLINED_REASON_IN_EMAIL);
        final String mailSubject = MAIL_SUBJECT_FOR_NEW_ACQUISTION_1 + productLine + MAIL_SUBJECT_FOR_NEW_ACQUISTION_2;
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        CheckPelicanEmailClient.declinedOrder(mailSubject, recipientMail, validationChecklist);
    }

    /***
     * Verify Payment Error (Delinquent) mail has been sent out with payment error message and contains Subscription Id.
     *
     * @param subscriptionId
     * @param recipientMail
     * @param environmentVariables
     */
    public static void paymentErrorEmailOnRenewal(final String subscriptionId, final String recipientMail,
        final EnvironmentVariables environmentVariables) {
        final ArrayList<String> validationCheckList = new ArrayList<>();
        validationCheckList.add(subscriptionId);
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        CheckPelicanEmailClient.delinquentOrder(MAIL_SUBJECT_FOR_PAYMENT_ERROR, recipientMail, validationCheckList,
            subscriptionId);
    }

    /**
     * method to find whether or not emails are being sent for renewal orders in mailBox if it is declined beacuse of
     * export control
     *
     * @param priceId
     * @param recipientMail
     * @param environmentVariables
     * @param subscriptionIdList TODO
     * @return true or false, based on mail found/not found
     */

    public static void declinedOrderEmailForRenewal(final String priceId, final String recipientMail,
        final EnvironmentVariables environmentVariables, final List<String> subscriptionIdList) {

        // query which returns the name of the product line which is required to find subject of an email
        final String productLine =
            DbUtils.selectQuery(PRODUCT_LINE_QUERY + priceId, PRODUCT_LINE_NAME, environmentVariables).get(0);

        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(RENEWAL_DECLINED_REASON_IN_EMAIL);

        final String mailSubject = MAIL_SUBJECT_FOR_RENEWAL_1 + productLine + MAIL_SUBJECT_FOR_RENEWAL_2;
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        CheckPelicanEmailClient.declinedOrder(mailSubject, recipientMail, validationChecklist);
    }

    /**
     * Helper class for Invoice email
     *
     * @param isStoredPaymentProfileMissing
     *
     * @return true or false, based on mail found/not found
     */
    public static boolean invoice(final String purchaseOrder, final EnvironmentVariables environmentVariables,
        final boolean isStoredPaymentProfileMissing) {
        // get InvoiceNumber for the purchase order from purchase_order table
        final String sqlQueryInvoiceNumber =
            "select invoice_number from purchase_order where id=\"" + purchaseOrder + "\"";

        // get payment type for the purchase order from finance_report table
        final String sqlQueryPaymentType =
            "select payment_type from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get Subscription Id for the purchase order from finance_report table
        final String subscriptionId =
            "select subscription_id from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get Next Billing Date for the purchase order from finance_report table
        final String nextBillingDate =
            "select next_billing_date from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get billing currency for the purchase order from finance_report table
        final String sqlQueryBillingCurrency =
            "select billing_currency from finance_report where purchase_order_id=\"" + purchaseOrder + "\"";

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the invoicenumber as list(this is limitation of
        // the method)
        final List<String> invoiceNumberList =
            DbUtils.selectQuery(sqlQueryInvoiceNumber, "invoice_number", environmentVariables);

        // query which returns the subscription id as list
        final List<String> subscriptionIds =
            DbUtils.selectQuery(subscriptionId, "subscription_id", environmentVariables);

        // query which returns the next billing date as list
        final List<String> nextBillingDates =
            DbUtils.selectQuery(nextBillingDate, "next_billing_date", environmentVariables);

        // query which returns the payment type as list(this is limitation of
        // the method)
        final List<String> paymentTypeList;
        if (!isStoredPaymentProfileMissing) {
            paymentTypeList = DbUtils.selectQuery(sqlQueryPaymentType, "payment_type", environmentVariables);
        } else {
            paymentTypeList = Arrays.asList(PaymentType.PAYPAL.getValue());
        }
        // query which returns the billingcurrency as list(this is limitation of
        // the method)
        final List<String> billingCurrencyList =
            DbUtils.selectQuery(sqlQueryBillingCurrency, "billing_currency", environmentVariables);

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrder);
        validationChecklist.add(subscriptionIds.get(0));
        validationChecklist.add(invoiceNumberList.get(0));
        validationChecklist.add(paymentTypeList.get(0));
        validationChecklist.add(billingCurrencyList.get(0));
        validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDates.get(0),
            PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Invoice/send tax email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.invoice(purchaseOrder, billingEmails.get(0), validationChecklist, true);

        return true;
    }

    /**
     * Helper class for paymentMethodChange email
     *
     * @param subscriptionId
     * @param paymentOption
     *
     * @return true or false, based on mail found/not found
     */
    public static boolean paymentMethodChange(final String purchaseOrder, final String subscriptionId,
        final EnvironmentVariables environmentVariables, final PaymentOption paymentOption) {

        // get Product Name for the Subscription ID from subscription ,Product
        // Line and offering table
        final String sqlQueryProductName =
            "select name from product_line where id = " + "(select PRODUCT_LINE_ID from offering where id = "
                + "(select plan_id from subscription where id = \"" + subscriptionId + "\"))";

        final List<String> productNameList = DbUtils.selectQuery(sqlQueryProductName, "name", environmentVariables);

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(paymentOption.getValue());
        validationChecklist.add(productNameList.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for Invoice/send tax email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.paymentMethodChanged(productNameList.get(0), billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * Helper class for Renewal Reminder email
     *
     * @param subscription
     * @param nextBilling
     * @param environmentVariables
     * @param billingfrequency
     * @param buyerUserEmail
     * @param currencySymbol
     * @return true or false, based on mail found/not found
     */
    public static boolean renewalReminder(final JSubscription subscription,
        final EnvironmentVariables environmentVariables, final BillingFrequency billingfrequency,
        final String buyerUserEmail, final String currencySymbol) {

        final String subscriptionPlanName = subscription.getIncluded().getOffering().getName();
        final double priceOfLineItemInDouble = Double.valueOf(subscription.getData().getNextBillingPriceAmount());
        final DecimalFormat df = new DecimalFormat("##,##0.00");
        final String priceOfLineItem = currencySymbol + df.format(priceOfLineItemInDouble);

        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(subscriptionPlanName);
        validationChecklist.add(String.valueOf(subscription.getData().getQuantity()));
        validationChecklist.add(priceOfLineItem);

        final String nextBillingDate = subscription.getData().getNextBillingDate().replace(" UTC", "");
        validationChecklist.add(" Subscription #" + subscription.getData().getId());
        validationChecklist.add(DateTimeUtils.changeDateFormat(nextBillingDate, PelicanConstants.DB_DATE_FORMAT,
            PelicanConstants.EMAIL_DATE_FORMAT));

        String subjectOfEmail;
        if (BillingFrequency.MONTH != billingfrequency) {
            subjectOfEmail = EmailConstants.AUTO_RENEWAL_REMINDER_FOR_YEARLY;
        } else {
            subjectOfEmail = EmailConstants.AUTO_RENEWAL_REMINDER_FOR_MONTHLY;
        }

        // wait required to receive an email in inbox.
        Util.waitInSeconds(TimeConstants.LONG_WAIT);
        // check for Renewal Reminder email with validations as part of validationChecklist this call will has assertion
        // if not found
        CheckPelicanEmailClient.renewalReminder(subscriptionPlanName, buyerUserEmail, validationChecklist,
            subjectOfEmail);

        return true;
    }

    /**
     * Helper class for Renewal Restart email
     *
     * @param subscriptionId
     * @return true or false, based on mail found/not found
     */
    public static boolean renewalRestart(final String purchaseOrder, final String subscriptionId,
        final EnvironmentVariables environmentVariables) {
        String productName;

        // get Product Name for the Subscription ID from subscription ,Product
        // Line and offering table
        final String sqlQueryProductName =
            "select name from product_line where id = " + "(select PRODUCT_LINE_ID from offering where id = "
                + "(select plan_id from subscription where id = \"" + subscriptionId + "\"))";

        final List<String> productNameList = DbUtils.selectQuery(sqlQueryProductName, "name", environmentVariables);

        productName = productNameList.get(0);

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrder + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add PNext billing date and payment
        // type to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(subscriptionId);

        Util.waitInSeconds(TimeConstants.LONG_WAIT);

        // check for Re activate subscription email with validations as part of
        // validationChecklist this call will has assertion if not found
        CheckPelicanEmailClient.restartAutoEmail(billingEmails.get(0), productName, validationChecklist,
            subscriptionId);

        return true;
    }

    /**
     * Helper class for Extension Order Complete Email.
     *
     * @return true or false, based on mail found/not found.
     */
    public static Boolean extensionOrderComplete(final String purchaseOrderId,
        final Map<String, String> subscriptionIdNextBillingDateMap, final String targetRenewalDate,
        final String paymentOption, final EnvironmentVariables environmentVariables) {

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrderId + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        for (final String subscriptionId : subscriptionIdNextBillingDateMap.keySet()) {
            validationChecklist.add(subscriptionId);
            validationChecklist
                .add((DateTimeUtils.changeDateFormat(subscriptionIdNextBillingDateMap.get(subscriptionId),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER)));
            validationChecklist.add(DateTimeUtils.changeDateFormat(targetRenewalDate,
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER));
        }
        validationChecklist.add(paymentOption);
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for order complete email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.extensionOrderComplete(purchaseOrderId, billingEmails.get(0), validationChecklist);

        return true;

    }

    /**
     * Helper class for Extension Invoice Email.
     *
     * @return true or false, based on mail found/not found.
     */
    public static Boolean extensionInvoice(final String purchaseOrderId,
        final Map<String, String> subscriptionIdNextBillingDateMap, final String targetRenewalDate,
        final String invoiceNumber, final String paymentOption, final EnvironmentVariables environmentVariables) {

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrderId + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        for (final String subscriptionId : subscriptionIdNextBillingDateMap.keySet()) {
            validationChecklist.add(subscriptionId);
            validationChecklist
                .add((DateTimeUtils.changeDateFormat(subscriptionIdNextBillingDateMap.get(subscriptionId),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER)));
            validationChecklist.add(DateTimeUtils.changeDateFormat(targetRenewalDate,
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER));
        }

        validationChecklist.add(invoiceNumber);
        validationChecklist.add(paymentOption);

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for order complete email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.invoice(purchaseOrderId, billingEmails.get(0), validationChecklist, false);

        return true;

    }

    /**
     * Helper class for credit note memo for Extension Order.
     *
     * @return true or false, based on mail found/not found
     */
    public static Boolean extensionCreditNoteMemo(final String purchaseOrderId,
        final Map<String, String> subscriptionIdNextBillingDateMap, final String targetRenewalDate,
        final String invoiceNumber, final String creditNoteNumber, final String paymentOption,
        final EnvironmentVariables environmentVariables) {

        // get billing currency for the purchase order from finance_report table
        final String billingEmail = "select billing_email from purchase_order where id=\"" + purchaseOrderId + "\"";

        // query which returns the billing_email as list(this is limitation of
        // the method)
        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // add Purchase Order ID, Invoice number, credit note number, payment
        // type and billing currency to the list
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        for (final String subscriptionId : subscriptionIdNextBillingDateMap.keySet()) {
            validationChecklist.add(subscriptionId);
            validationChecklist.add(DateTimeUtils.changeDateFormat(subscriptionIdNextBillingDateMap.get(subscriptionId),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER));
            validationChecklist.add(DateTimeUtils.changeDateFormat(targetRenewalDate,
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_EXTENSION_ORDER));
        }

        validationChecklist.add(invoiceNumber);
        validationChecklist.add(creditNoteNumber);
        validationChecklist.add(paymentOption);

        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // check for credit note email with validations as part of
        // validationChecklist
        // this call will has assertion if not found
        CheckPelicanEmailClient.creditNoteMemo(purchaseOrderId, billingEmails.get(0), validationChecklist);

        return true;
    }

    /**
     * This method to validate the email body of Add Seats Order Complete
     *
     * @param purchaseOrderId
     * @param subscriptionId
     * @param addedSeats
     * @param totalSeats
     * @param prorateStartDate
     * @param environmentVariables
     * @param buyerEmailIds
     */
    public static void orderCompleteForAddSeats(final String purchaseOrderId, final String subscriptionId,
        final int addedSeats, final int totalSeats, final String prorateStartDate,
        final EnvironmentVariables environmentVariables, final List<String> buyerEmailIds) {

        // get Next Billing Date for the subscription from subscription table
        final String nextBillingDateQuery = "select NEXT_BILLING_DATE from subscription where id = " + subscriptionId;

        // query which returns the next billing date
        final List<String> nextBillingDate =
            DbUtils.selectQuery(nextBillingDateQuery, "NEXT_BILLING_DATE", environmentVariables);

        // get proration start and end date
        final String prorationStartDate = DateTimeUtils.changeDateFormat(prorateStartDate.split(" ")[0],
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);
        final String prorationEndDate = DateTimeUtils.changeDateFormat(nextBillingDate.get(0).split(" ")[0],
            PelicanConstants.DATE_TIME_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);

        // add Purchase Order ID, proration start & end date, subscription id, quantity
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        validationChecklist.add(prorationStartDate);
        validationChecklist.add(prorationEndDate);
        validationChecklist.add("Subscription #" + subscriptionId);
        validationChecklist.add(String.valueOf(addedSeats));
        validationChecklist.add(totalSeats + " seat(s)");
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        for (final String buyerEmailId : buyerEmailIds) {
            CheckPelicanEmailClient.addSeatsOrderComplete(purchaseOrderId, buyerEmailId, validationChecklist);
        }
    }

    /**
     * This method to validate the email body of Add Seats Invoice Email
     *
     * @param purchaseOrderId
     * @param subscriptionId
     * @param addedSeats
     * @param prorateStartDate
     * @param environmentVariables
     * @param subscriptionQuantity TODO
     */
    public static void addSeatsInvoice(final String purchaseOrderId, final String subscriptionId, final int addedSeats,
        final String prorateStartDate, final EnvironmentVariables environmentVariables) {

        // get billing email for the purchase order from purchase_order table
        final String billingEmail = "select billing_email from purchase_order where id=" + purchaseOrderId;

        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // get invoice number for the purchase order from purchase_order table
        final String invoiceNumber = "select INVOICE_NUMBER from purchase_order where id=" + purchaseOrderId;

        final List<String> invoiceNumbers = DbUtils.selectQuery(invoiceNumber, "INVOICE_NUMBER", environmentVariables);

        // get Next Billing Date for the subscription from subscription table
        final String nextBillingDateQuery = "select NEXT_BILLING_DATE from subscription where id = " + subscriptionId;

        // query which returns the next billing date
        final List<String> nextBillingDate =
            DbUtils.selectQuery(nextBillingDateQuery, "NEXT_BILLING_DATE", environmentVariables);

        // get proration start and end date
        final String prorationStartDate = DateTimeUtils.changeDateFormat(prorateStartDate.split(" ")[0],
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);
        final String prorationEndDate = DateTimeUtils.changeDateFormat(nextBillingDate.get(0).split(" ")[0],
            PelicanConstants.DATE_TIME_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);

        // add Purchase Order ID, proration start & end date, subscription id, quantity
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        validationChecklist.add(prorationStartDate);
        validationChecklist.add(prorationEndDate);
        // validationChecklist.add("Subscription #" + subscriptionId);
        // validationChecklist.add(String.valueOf(addedSeats));
        validationChecklist.add(invoiceNumbers.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);
        CheckPelicanEmailClient.addSeatsInvoice(purchaseOrderId, billingEmails.get(0), validationChecklist);
    }

    /**
     * This method to validate the email body of Add Seats Invoice Email
     *
     * @param purchaseOrderId
     * @param subscriptionId
     * @param addedSeats
     * @param prorateStartDate
     * @param environmentVariables
     */
    public static void addSeatsCreditNote(final String purchaseOrderId, final String subscriptionId,
        final int addedSeats, final String prorateStartDate, final EnvironmentVariables environmentVariables) {

        // get billing email for the purchase order from purchase_order table
        final String billingEmail = "select billing_email from purchase_order where id=" + purchaseOrderId;

        final List<String> billingEmails = DbUtils.selectQuery(billingEmail, "billing_email", environmentVariables);

        // get invoice number for the purchase order from purchase_order table
        final String creditNoteNumber = "select CREDIT_NOTE_NUMBER from purchase_order where id=" + purchaseOrderId;

        final List<String> creditNoteNumbers =
            DbUtils.selectQuery(creditNoteNumber, "CREDIT_NOTE_NUMBER", environmentVariables);

        // get Next Billing Date for the subscription from subscription table
        final String nextBillingDateQuery = "select NEXT_BILLING_DATE from subscription where id = " + subscriptionId;

        // query which returns the next billing date
        final List<String> nextBillingDate =
            DbUtils.selectQuery(nextBillingDateQuery, "NEXT_BILLING_DATE", environmentVariables);

        // get proration start and end date
        final String prorationStartDate = DateTimeUtils.changeDateFormat(prorateStartDate.split(" ")[0],
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);
        final String prorationEndDate = DateTimeUtils.changeDateFormat(nextBillingDate.get(0).split(" ")[0],
            PelicanConstants.DATE_TIME_FORMAT, PelicanConstants.EMAIL_DATE_FORMAT_ADD_SEATS_ORDER);

        // add Purchase Order ID, proration start & end date, subscription id, quantity
        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(purchaseOrderId);
        validationChecklist.add(prorationStartDate);
        validationChecklist.add(prorationEndDate);
        // validationChecklist.add("Subscription #" + subscriptionId);
        // validationChecklist.add(String.valueOf(addedSeats));
        validationChecklist.add(creditNoteNumbers.get(0));

        Util.waitInSeconds(TimeConstants.TWO_MINS);
        CheckPelicanEmailClient.addSeatsCreditNote(purchaseOrderId, billingEmails.get(0), validationChecklist);
    }

    /**
     * Method to validate body of expiration reminder email.
     *
     * @param expirationDate
     * @param environmentVariables
     * @param buyerUserEmail
     * @param productLineName
     */
    public static void expirationReminder(final String expirationDate, final EnvironmentVariables environmentVariables,
        final String buyerUserEmail, final String productLineName) {
        // Product line name is blank right now, we will add product line name when BIC-7098 story is completed in next
        // sprint.
        final String expirationDateInEmailFormat = DateTimeUtils.changeDateFormat(expirationDate,
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.EMAIL_DATE_FORMAT);

        final ArrayList<String> validationChecklist = new ArrayList<>();
        validationChecklist.add(productLineName);
        // wait is required to receive an email in inbox
        Util.waitInSeconds(TimeConstants.LONG_WAIT);
        CheckPelicanEmailClient.expirationReminder(expirationDateInEmailFormat, buyerUserEmail, validationChecklist);
    }

}
