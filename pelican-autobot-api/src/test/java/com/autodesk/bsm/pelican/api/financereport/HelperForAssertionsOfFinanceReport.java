package com.autodesk.bsm.pelican.api.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.FinanceReportHeaders;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.FinanceReportUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * This is a helper class to validate and assert finance report entries for - Single Line items, multiline items - New
 * Acquisition, Auto Renewal - Credit Card, Paypal - Bic, Meta and Basic Offerings
 *
 * @author Shweta Hegde
 */
public class HelperForAssertionsOfFinanceReport {

    private static String unitPrice1 = null;
    private static String totalAmount1 = null;
    private static String extendedListPrice1 = null;
    private static String unitDiscount1 = null;
    private static String unitPrice2 = null;
    private static String totalAmount2 = null;
    private static String extendedListPrice2 = null;
    private static String unitDiscount2 = null;

    /**
     * This method helps to find the record depending on PO id and status
     *
     * @param actualRestHeader
     * @param actualReport
     * @param orderCommand
     * @param purchaseOrderId
     * @param offerings
     * @param quantity
     * @param invoiceNumber
     * @param creditNoteNumber
     * @param subscriptionId
     * @param secondSubscriptionId
     * @param nextBillingDate
     * @param isNewAcquisition
     * @param isAddSeatsOrder
     * @param resource
     * @param tax
     * @param assertionErrorList
     */
    public static void assertionsForLineItems(final String actualRestHeader, final List<String> actualReport,
        final OrderCommand orderCommand, final String purchaseOrderId, final Offerings offerings, final int quantity,
        final String invoiceNumber, final String creditNoteNumber, final String subscriptionId,
        final String secondSubscriptionId, final String nextBillingDate, final boolean isNewAcquisition,
        final boolean isAddSeatsOrder, final PelicanPlatform resource, final Double tax,
        final List<AssertionError> assertionErrorList) {

        boolean recordFound = false;

        for (final String reportRow : actualReport) {
            if (FinanceReportUtils
                .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.ORDER_NUMBER.getHeader())
                .equalsIgnoreCase(purchaseOrderId)
                && FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.ORDER_STATUS.getHeader())
                    .equalsIgnoreCase(orderCommand.getValue())
                && FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.PRODUCTLINE_CODE.getHeader())
                    .equalsIgnoreCase(offerings.getOfferings().get(0).getProductLine())
                && (FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.QUANTITY.getHeader())
                    .equals(String.valueOf(-1 * quantity))
                    || FinanceReportUtils
                        .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.QUANTITY.getHeader())
                        .equals(String.valueOf(quantity)))) {

                // Assertions are related to offering details, payment details etc
                assertionsForFinanceReport(actualRestHeader, reportRow, purchaseOrderId, offerings, subscriptionId,
                    secondSubscriptionId, nextBillingDate, isNewAcquisition, assertionErrorList);

                // Assertions are related to calculation, invoice number, credit note number etc
                assertionsForCalculationPartInFinanceReport(actualRestHeader, reportRow, purchaseOrderId, quantity,
                    orderCommand, invoiceNumber, creditNoteNumber, resource, isNewAcquisition, isAddSeatsOrder, tax,
                    assertionErrorList);

                // Assertions are related to Subscription management fields such as "Added to subscription" etc
                assertionsForSubscriptionManagementFields(actualRestHeader, reportRow, purchaseOrderId, subscriptionId,
                    secondSubscriptionId, isAddSeatsOrder, assertionErrorList);

                recordFound = true;
                break;
            }
        }
        AssertCollector.assertTrue("FAILURE!! " + orderCommand.toString() + " RECORD IS NOT FOUND FOR PO : "
            + purchaseOrderId + " and Subscription Id : " + subscriptionId, recordFound, assertionErrorList);
    }

    /**
     * This method does all assertions related to static fields for finance report tests for New Acquisition and Auto
     * Renewals and Add Seats
     *
     * @param actualRestHeader
     * @param reportRow
     * @param purchaseOrderId
     * @param offerings
     * @param subscriptionId
     * @param secondSubscriptionId
     * @param nextBillingDate
     * @param isNewAcquisition
     * @param assertionErrorList
     */
    private static void assertionsForFinanceReport(final String actualRestHeader, final String reportRow,
        final String purchaseOrderId, final Offerings offerings, final String subscriptionId,
        final String secondSubscriptionId, final String nextBillingDate, final boolean isNewAcquisition,
        final List<AssertionError> assertionErrorList) {

        // Offering Type
        AssertCollector.assertThat("Incorrect offeringType for PO : " + purchaseOrderId,
            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                FinanceReportHeaders.OFFERING_TYPE.getHeader()),
            equalTo(offerings.getOfferings().get(0).getOfferingType().getDisplayName()), assertionErrorList);

        // Subscription Plan Name, External Key, Subscription Id, Support Level, Next Billing Date
        if (offerings.getOfferings().get(0).getOfferingType() == OfferingType.BIC_SUBSCRIPTION
            || offerings.getOfferings().get(0).getOfferingType() == OfferingType.META_SUBSCRIPTION) {

            AssertCollector.assertThat("Incorrect subscription plan name for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_PLAN_NAME.getHeader()),
                equalTo(offerings.getOfferings().get(0).getName()), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription plan external key for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_PLAN_EXTERNAL_KEY.getHeader()),
                equalTo(offerings.getOfferings().get(0).getExternalKey()), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()),
                isOneOf(subscriptionId, secondSubscriptionId), assertionErrorList);
            AssertCollector.assertThat("Incorrect Support Level for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUPPORT_LEVEL.getHeader()),
                equalTo(offerings.getOfferings().get(0).getSupportLevel().getDisplayName()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Next billing Date for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.NEXT_BILLING_DATE.getHeader()).split("\\s+")[0],
                equalTo(nextBillingDate.split("\\s+")[0]), assertionErrorList);

            // Subscription Start Date
            if (isNewAcquisition) {
                AssertCollector.assertThat("Incorrect subscription start date for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.SUBSCRIPTION_START_DATE.getHeader()).split("\\s+")[0],
                    equalTo(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
            } else {

                // This calculation happens for renewal orders only
                String subscriptionStartDate;

                subscriptionStartDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
                AssertCollector.assertThat("Incorrect subscription start date for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.SUBSCRIPTION_START_DATE.getHeader()).split("\\s+")[0],
                    equalTo(subscriptionStartDate), assertionErrorList);
            }

            // Subscription End Date
            AssertCollector.assertThat("Incorrect subscription end date for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_END_DATE.getHeader()).split("\\s+")[0],
                equalTo(DateTimeUtils.addDaysToDate(nextBillingDate.split("\\s+")[0],
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, -1)),
                assertionErrorList);

        } else {
            AssertCollector.assertThat(
                "Incorrect subscription plan name", FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.SUBSCRIPTION_PLAN_NAME.getHeader()),
                equalTo(""), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription plan external key", FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.SUBSCRIPTION_PLAN_EXTERNAL_KEY.getHeader()),
                equalTo(""), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id", FinanceReportUtils.getColumnValueFromList(reportRow,
                actualRestHeader, FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()), equalTo(""), assertionErrorList);
            AssertCollector.assertThat("No Next billing Date", FinanceReportUtils.getColumnValueFromList(reportRow,
                actualRestHeader, FinanceReportHeaders.NEXT_BILLING_DATE.getHeader()), equalTo(""), assertionErrorList);
        }

        // Usage Type
        AssertCollector.assertThat("Incorrect usage type for PO : " + purchaseOrderId,
            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                FinanceReportHeaders.USAGE_TYPE.getHeader()),
            equalTo(offerings.getOfferings().get(0).getUsageType().getDisplayName()), assertionErrorList);

        // Sale Type, Add Seats test classes are sent with value "true" for isNewAcquisition
        if (isNewAcquisition) {
            AssertCollector.assertThat("Incorrect sale type for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SALE_TYPE.getHeader()),
                equalTo(PelicanConstants.NEW_ACQUISITION), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect sale type for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SALE_TYPE.getHeader()),
                equalTo(PelicanConstants.AUTO_RENEWS), assertionErrorList);
        }

        // Last Modified Date
        AssertCollector.assertThat("Incorrect last modified date in the finance report for PO : " + purchaseOrderId,
            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                FinanceReportHeaders.LAST_MODIFIED_DATE.getHeader()),
            notNullValue(), assertionErrorList);
        // PSP
        AssertCollector.assertThat("Incorrect PSP for PO : " + purchaseOrderId,
            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                FinanceReportHeaders.PSP.getHeader()),
            isOneOf(PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentProcessor.BLUESNAP_NAMER.getValue(),
                PaymentProcessor.PAYPAL_EMEA.getValue(), PaymentProcessor.PAYPAL_NAMER.getValue()),
            assertionErrorList);
        // Payment Type
        AssertCollector
            .assertThat(
                "Incorrect Payment Type for PO : " + purchaseOrderId, FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.PAYMENT_TYPE.getHeader()),
                notNullValue(), assertionErrorList);
    }

    /**
     * This method does all assertions related to amount etc calculation
     *
     * @param actualRestHeader
     * @param reportRow
     * @param purchaseOrderId
     * @param quantity
     * @param orderCommand
     * @param invoiceNumber
     * @param creditNoteNumber
     * @param resource
     * @param isNewAcquisition
     * @param isAddSeatsOrder
     * @param assertionErrorList
     */
    private static void assertionsForCalculationPartInFinanceReport(final String actualRestHeader,
        final String reportRow, final String purchaseOrderId, final int quantity, final OrderCommand orderCommand,
        final String invoiceNumber, final String creditNoteNumber, final PelicanPlatform resource,
        final boolean isNewAcquisition, final boolean isAddSeatsOrder, final Double tax,
        final List<AssertionError> assertionErrorList) {

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        helperToGetPriceAmount(purchaseOrder, isNewAcquisition, isAddSeatsOrder, quantity);
        // Price
        if (unitPrice2 == null) {
            AssertCollector.assertThat(
                "Incorrect 'list price' for PO : " + purchaseOrderId, FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.LIST_PRICE.getHeader()),
                equalTo(unitPrice1), assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Incorrect 'list price' for PO : " + purchaseOrderId, FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.LIST_PRICE.getHeader()),
                isOneOf(unitPrice1, unitPrice2), assertionErrorList);
        }

        final String totalOrderAmount1 = String.format("%.2f", Double.parseDouble(totalAmount1) + tax);

        // Total Price, Quantity, Invoice Number, Credit Note Number
        if (orderCommand == OrderCommand.CHARGE) {

            if (totalAmount2 == null && extendedListPrice2 == null && unitDiscount2 == null) {
                AssertCollector
                    .assertThat("Incorrect 'unit discount' for PO : " + purchaseOrderId,
                        FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.UNIT_DISCOUNT.getHeader()),
                        equalTo(unitDiscount1), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total price' for PO : " + purchaseOrderId, FinanceReportUtils
                    .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                    equalTo(totalAmount1), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'extended list price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.EXTENDED_LIST_PRICE.getHeader()),
                    equalTo(extendedListPrice1), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total order price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_ORDER_PRICE.getHeader()),
                    equalTo(totalOrderAmount1), assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect 'unit discount' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.UNIT_DISCOUNT.getHeader()),
                    isOneOf(unitDiscount1, unitDiscount2), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                    isOneOf(totalAmount1, totalAmount2), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'extended list price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.EXTENDED_LIST_PRICE.getHeader()),
                    isOneOf(extendedListPrice1, extendedListPrice2), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total order price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_ORDER_PRICE.getHeader()),
                    isOneOf(totalOrderAmount1, String.format("%.2f", Double.parseDouble(totalAmount2) + tax)),
                    assertionErrorList);
            }
            AssertCollector.assertThat(
                "Incorrect quantity for PO : " + purchaseOrderId, FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.QUANTITY.getHeader()),
                equalTo(String.valueOf(quantity)), assertionErrorList);
            AssertCollector.assertThat("Incorrect Invoice number in Finance Report for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.INVOICE_NUMBER.getHeader()),
                equalTo(invoiceNumber), assertionErrorList);
        } else {

            final String refundTotalPrice1 = String.format("%.2f", -1 * Double.parseDouble(totalAmount1));
            final String refundExtendedListPrice1 = String.format("%.2f", -1 * Double.parseDouble(extendedListPrice1));
            final String refundTotalOrderAmount1 = String.format("%.2f", -1 * Double.parseDouble(totalOrderAmount1));
            String refundUnitDiscount1 = unitDiscount1;
            if (!"0.00".equals(unitDiscount1)) {
                refundUnitDiscount1 = String.format("%.2f", -1 * Double.parseDouble(unitDiscount1));
            }
            final String refundUnitDiscount2 = unitDiscount2;
            if (StringUtils.isNotEmpty(unitDiscount2) && !"0.00".equals(unitDiscount2)) {
                refundUnitDiscount1 = String.format("%.2f", -1 * Double.parseDouble(unitDiscount2));
            }

            if (totalAmount2 == null) {
                AssertCollector.assertThat("Incorrect 'unit discount' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.UNIT_DISCOUNT.getHeader()),
                    equalTo(refundUnitDiscount1), assertionErrorList);
                AssertCollector
                    .assertThat("Incorrect total price for PO : " + purchaseOrderId,
                        FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                        equalTo(refundTotalPrice1), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'extended list price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.EXTENDED_LIST_PRICE.getHeader()),
                    equalTo(refundExtendedListPrice1), assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total order price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_ORDER_PRICE.getHeader()),
                    equalTo(refundTotalOrderAmount1), assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect 'unit discount' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.UNIT_DISCOUNT.getHeader()),
                    isOneOf(refundUnitDiscount1, refundUnitDiscount2), assertionErrorList);
                AssertCollector.assertThat("Incorrect total price for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                    isOneOf(refundTotalPrice1, String.format("%.2f", -1 * Double.parseDouble(totalAmount2))),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect 'extended list price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.EXTENDED_LIST_PRICE.getHeader()),
                    isOneOf(refundExtendedListPrice1,
                        String.format("%.2f", -1 * Double.parseDouble(extendedListPrice2))),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect 'total order price' for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.TOTAL_ORDER_PRICE.getHeader()),
                    isOneOf(refundTotalOrderAmount1,
                        String.format("%.2f", -1 * Double.parseDouble(totalAmount2) + tax)),
                    assertionErrorList);
            }
            AssertCollector.assertThat("Incorrect quantity for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.QUANTITY.getHeader()),
                equalTo(String.valueOf(-1 * quantity)), assertionErrorList);
            AssertCollector.assertThat(
                "Credit Note number should be replaced with invoice number in Finance Report for PO : "
                    + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.INVOICE_NUMBER.getHeader()),
                equalTo(creditNoteNumber), assertionErrorList);
        }
    }

    private static void helperToGetPriceAmount(final PurchaseOrder purchaseOrder, final boolean isNewAcquisition,
        final boolean isAddSeatsOrder, final int quantity) {

        if (isAddSeatsOrder) {
            unitPrice1 = purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getChargeDetails().getUnitPrice();
            totalAmount1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged());
            extendedListPrice1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getTotalPrice());
            unitDiscount1 =
                String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                    .getSubscriptionQuantityResponse().getChargeDetails().getPromotionDiscount() / quantity);
        }

        if (isNewAcquisition && !isAddSeatsOrder) {
            unitPrice1 = purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                .getChargeDetails().getUnitPrice();
            totalAmount1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getAmountCharged());
            extendedListPrice1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getTotalPrice());
            unitDiscount1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getPromotionDiscount() / quantity);

            if (purchaseOrder.getLineItems().getLineItems().size() == 2) {

                unitPrice2 = purchaseOrder.getLineItems().getLineItems().get(1).getOffering().getOfferingResponse()
                    .getChargeDetails().getUnitPrice();
                totalAmount2 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1).getOffering()
                    .getOfferingResponse().getChargeDetails().getAmountCharged());
                extendedListPrice2 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1)
                    .getOffering().getOfferingResponse().getChargeDetails().getTotalPrice());
                unitDiscount2 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1).getOffering()
                    .getOfferingResponse().getChargeDetails().getPromotionDiscount() / quantity);
            }
        }

        if (!isNewAcquisition && !isAddSeatsOrder) {
            unitPrice1 = purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice();
            totalAmount1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0)
                .getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged());
            extendedListPrice1 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0)
                .getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice());
            unitDiscount1 =
                String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalResponse().getChargeDetails().getPromotionDiscount() / quantity);

            if (purchaseOrder.getLineItems().getLineItems().size() == 2) {

                unitPrice2 = purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal()
                    .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice();
                totalAmount2 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1)
                    .getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged());
                extendedListPrice2 = String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1)
                    .getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice());
                unitDiscount2 =
                    String.format("%.2f", purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal()
                        .getSubscriptionRenewalResponse().getChargeDetails().getPromotionDiscount() / quantity);
            }
        }
    }

    private static void assertionsForSubscriptionManagementFields(final String actualRestHeader, final String reportRow,
        final String purchaseOrderId, final String subscriptionId, final String secondSubscriptionId,
        final boolean isAddSeatsOrder, final List<AssertionError> assertionErrorList) {

        // If it is added seats order, 2 column values will not be empty
        if (isAddSeatsOrder) {

            // Contract Modification
            AssertCollector.assertThat("Incorrect value in 'Contract Modification' column for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.CONTRACT_MODIFICATION.getHeader()),
                equalTo(PelicanConstants.ADD_SEATS_ORDER), assertionErrorList);

            // Subscription Added To
            if (secondSubscriptionId == null) {
                AssertCollector.assertThat(
                    "Incorrect value in 'Added Subscription To' column for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.SUBSCRIPTION_ADDED_TO.getHeader()),
                    equalTo(PelicanConstants.EMPTY_STRING), assertionErrorList);
            } else {
                AssertCollector.assertThat(
                    "Incorrect subscription value in 'Added Subscription To' column for PO : " + purchaseOrderId,
                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                        FinanceReportHeaders.SUBSCRIPTION_ADDED_TO.getHeader()),
                    equalTo(secondSubscriptionId), assertionErrorList);
            }
        } else {
            // If not add seats order, then columns will be empty
            AssertCollector.assertThat("Incorrect value in 'Contract Modification' column for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.CONTRACT_MODIFICATION.getHeader()),
                equalTo(PelicanConstants.EMPTY_STRING), assertionErrorList);
            AssertCollector.assertThat("Incorrect value in 'Added Subscription To' column for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_ADDED_TO.getHeader()),
                equalTo(PelicanConstants.EMPTY_STRING), assertionErrorList);
        }

        if (subscriptionId != null && isAddSeatsOrder) {
            // Subscription Id
            AssertCollector.assertThat(
                "Incorrect subscription value in 'Subscription ID' column for PO : " + purchaseOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()),
                equalTo(subscriptionId), assertionErrorList);
        }
    }

    /**
     * This method is a helper method for finance report, it runs invoice job, gets next billing date from subscription,
     * and gets start and end date and calls helper method for assertion
     *
     * @param resource
     * @param triggerResource
     * @param originalSubscriptionId
     * @param addSeatsSubscriptionId
     * @param quantity
     * @param purchaseOrderId
     * @param offering
     * @param findPurchaseOrdersPage
     * @param isNewAcquisition
     * @param isAddSeatsOrder
     * @param purchaseOrderUtils
     * @param refundOption
     * @param nextBillingDate
     * @param tax
     * @param assertionErrorList
     */
    public static void helperForFinanceReportTests(final PelicanPlatform resource,
        final PelicanTriggerClient triggerResource, final String originalSubscriptionId,
        final String addSeatsSubscriptionId, final int quantity, final String purchaseOrderId, final Offerings offering,
        final FindPurchaseOrdersPage findPurchaseOrdersPage, final boolean isNewAcquisition,
        final boolean isAddSeatsOrder, final PurchaseOrderUtils purchaseOrderUtils, final String refundOption,
        final String nextBillingDate, final double tax, final List<AssertionError> assertionErrorList) {

        // running the invoice job
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Depending on refund option, either REFUND, Mark As Refund or CHARGEBACK
        OrderCommand orderCommand = OrderCommand.REFUND;
        if (refundOption != null) {
            if (refundOption.equals(OrderCommand.REFUND.toString())) {

                purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
            } else if (refundOption.equals(OrderCommand.CHARGEBACK.toString())) {

                purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);
                orderCommand = OrderCommand.CHARGEBACK;
            } else if (refundOption.equals(PelicanConstants.MARK_AS_REFUND)) {

                // Click on mark as refunded
                purchaseOrderDetailPage.clickMarkAsRefunded();
                purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);
            }
        }

        purchaseOrderDetailPage.refreshPage();

        // Get Invoice and Credit Note Number
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();
        final String creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();

        final String expStartDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, -3);

        // Get the Finance report using the API using end timestamp after PO is created
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 3);
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        // Assertion for CHARGE order
        assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE, purchaseOrderId, offering, quantity,
            invoiceNumber, null, originalSubscriptionId, addSeatsSubscriptionId, nextBillingDate, isNewAcquisition,
            isAddSeatsOrder, resource, tax, assertionErrorList);

        // Assertion for REFUND or CHARGEBACK orders
        assertionsForLineItems(actualRestHeader, actualReport, orderCommand, purchaseOrderId, offering, quantity,
            invoiceNumber, creditNoteNumber, originalSubscriptionId, addSeatsSubscriptionId, nextBillingDate,
            isNewAcquisition, isAddSeatsOrder, resource, tax, assertionErrorList);
    }

    /**
     * Helper Method to verify Subscription Extension Order Entries in Finance Report when Order Command = CHARGE,
     * REFUND & CHARGEBACK.
     *
     * @param resource
     * @param extensionOrderId
     * @param subscriptionIdNextBillingDateMap
     * @param subscriptionQuantityMap
     * @param targetRenewalDate
     * @param orderCommand
     * @param invoiceNumber
     * @param creditNoteNumber
     * @param paymentType
     * @param paymentOption
     * @param assertionErrorList
     */
    public static void helperForSubscriptionExtensionOrder(final PelicanPlatform resource,
        final String extensionOrderId, final Map<String, String> subscriptionIdNextBillingDateMap,
        final Map<String, Integer> subscriptionQuantityMap, final String targetRenewalDate,
        final OrderCommand orderCommand, final String invoiceNumber, final String creditNoteNumber,
        final PaymentType paymentType, final PaymentOption paymentOption,
        final List<AssertionError> assertionErrorList) {

        final String expStartDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, -3);

        // Get the Finance report using the API using end timestamp after PO is created
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 1);
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        if (orderCommand == OrderCommand.CHARGE) {
            for (final String subscriptionId : subscriptionIdNextBillingDateMap.keySet()) {

                for (final String reportRow : actualReport) {

                    if (FinanceReportUtils
                        .getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.ORDER_NUMBER.getHeader())
                        .equalsIgnoreCase(extensionOrderId)
                        && (FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()).equalsIgnoreCase(subscriptionId))
                        && (FinanceReportUtils
                            .getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.QUANTITY.getHeader())
                            .equalsIgnoreCase(String.valueOf(subscriptionQuantityMap.get(subscriptionId))))) {
                        // Assert on Unit Price and Total Price.
                        commonAssertionsForExtensionOrder(reportRow, actualRestHeader, subscriptionId, extensionOrderId,
                            paymentType, paymentOption, assertionErrorList);

                        final List<LineItem> lineItems =
                            resource.purchaseOrder().getById(extensionOrderId).getLineItems().getLineItems();

                        for (final LineItem lineItem : lineItems) {
                            if (lineItem.getSubscriptionExtension().getSubscriptionExtensionResponse()
                                .getSubscriptionId().equals(subscriptionId)) {

                                AssertCollector.assertThat("Incorrect Prorated Unit Price for : " + extensionOrderId,
                                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                        FinanceReportHeaders.LIST_PRICE.getHeader()),
                                    equalTo(lineItem.getSubscriptionExtension().getSubscriptionExtensionResponse()
                                        .getChargeDetails().getUnitPrice()),
                                    assertionErrorList);

                                AssertCollector.assertThat("Incorrect Prorated Total Amount for : " + extensionOrderId,
                                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                        FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                                    equalTo(String.format("%.2f", lineItem.getSubscriptionExtension()
                                        .getSubscriptionExtensionResponse().getChargeDetails().getTotalPrice())),
                                    assertionErrorList);

                            }
                        }

                        // assert for invoice.
                        AssertCollector.assertThat(
                            "Incorrect Invoice number in Finance Report for PO : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.INVOICE_NUMBER.getHeader()),
                            equalTo(invoiceNumber), assertionErrorList);

                        // assert for Next Billing Date, Proration Start & Proration End Date.
                        AssertCollector.assertThat("Incorrect Next Billing Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.NEXT_BILLING_DATE.getHeader()).split("\\s+")[0],
                            equalTo(targetRenewalDate), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Proration Start Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.SUBSCRIPTION_START_DATE.getHeader()).split("\\s+")[0],
                            equalTo(subscriptionIdNextBillingDateMap.get(subscriptionId)), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Proration End Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.SUBSCRIPTION_END_DATE.getHeader()).split("\\s+")[0],
                            equalTo(DateTimeUtils.addDaysToDate(targetRenewalDate,
                                PelicanConstants.DATE_FORMAT_WITH_SLASH, -1)),
                            assertionErrorList);
                    }
                }
            }
        }
        // This Assert is for REFUND, CHARGEBACK & MARK AS REFUNDED.
        if (orderCommand != OrderCommand.CHARGE) {

            for (final String subscriptionId : subscriptionIdNextBillingDateMap.keySet()) {

                for (final String reportRow : actualReport) {

                    if (FinanceReportUtils
                        .getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.ORDER_NUMBER.getHeader())
                        .equalsIgnoreCase(extensionOrderId)
                        && (FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                            FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()).equalsIgnoreCase(subscriptionId))
                        && (FinanceReportUtils
                            .getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.QUANTITY.getHeader())
                            .equalsIgnoreCase(String.format("%s", -1 * subscriptionQuantityMap.get(subscriptionId))))) {
                        // helper asserts
                        commonAssertionsForExtensionOrder(reportRow, actualRestHeader, subscriptionId, extensionOrderId,
                            paymentType, paymentOption, assertionErrorList);
                        final List<LineItem> lineItems =
                            resource.purchaseOrder().getById(extensionOrderId).getLineItems().getLineItems();

                        for (final LineItem lineItem : lineItems) {
                            if (lineItem.getSubscriptionExtension().getSubscriptionExtensionResponse()
                                .getSubscriptionId().equals(subscriptionId)) {

                                AssertCollector.assertThat("Incorrect Prorated Unit Price for : " + extensionOrderId,
                                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                        FinanceReportHeaders.LIST_PRICE.getHeader()),
                                    equalTo(lineItem.getSubscriptionExtension().getSubscriptionExtensionResponse()
                                        .getChargeDetails().getUnitPrice()),
                                    assertionErrorList);

                                AssertCollector.assertThat("Incorrect Prorated Total Amount for : " + extensionOrderId,
                                    FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                        FinanceReportHeaders.TOTAL_PRICE.getHeader()),
                                    equalTo(String.format("-%.2f", lineItem.getSubscriptionExtension()
                                        .getSubscriptionExtensionResponse().getChargeDetails().getTotalPrice())),
                                    assertionErrorList);
                            }
                        }

                        // assert for credit note number.
                        AssertCollector.assertThat(
                            "Incorrect Credit Note Number in Finance Report for PO : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.INVOICE_NUMBER.getHeader()),
                            equalTo(creditNoteNumber), assertionErrorList);

                        // assert for Next Billing Date, Proration Start & Proration End Date.
                        AssertCollector.assertThat("Incorrect Next Billing Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.NEXT_BILLING_DATE.getHeader()).split("\\s+")[0],
                            equalTo(targetRenewalDate), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Proration Start Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.SUBSCRIPTION_START_DATE.getHeader()).split("\\s+")[0],
                            equalTo(subscriptionIdNextBillingDateMap.get(subscriptionId)), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Proration End Date for : " + extensionOrderId,
                            FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                                FinanceReportHeaders.SUBSCRIPTION_END_DATE.getHeader()).split("\\s+")[0],
                            equalTo(DateTimeUtils.addDaysToDate(targetRenewalDate,
                                PelicanConstants.DATE_FORMAT_WITH_SLASH, -1)),
                            assertionErrorList);
                    }
                }
            }
        }
    }

    /**
     * Common asserts for Subscription Extension when order command is CHARGE, CHARGEBACK & REFUND.
     *
     * @param reportRow
     * @param actualRestHeader
     * @param subscriptionId
     * @param extensionOrderId
     * @param paymentType
     * @param assertionErrorList
     */
    private static void commonAssertionsForExtensionOrder(final String reportRow, final String actualRestHeader,
        final String subscriptionId, final String extensionOrderId, final PaymentType paymentType,
        final PaymentOption paymentOption, final List<AssertionError> assertionErrorList) {
        // Common assertions.
        AssertCollector.assertTrue("Incorrect Sales type for : " + extensionOrderId,
            FinanceReportUtils
                .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.SALE_TYPE.getHeader())
                .equalsIgnoreCase(PelicanConstants.AUTO_RENEWS),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect Contract Modification type for : " + extensionOrderId,
            FinanceReportUtils
                .getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.CONTRACT_MODIFICATION.getHeader())
                .equalsIgnoreCase(PelicanConstants.EXTENSION),
            assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Subscription Modified for : " + extensionOrderId, FinanceReportUtils
                .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.SUBSCRIPTION_ID.getHeader()),
            equalTo(subscriptionId), assertionErrorList);
        // PSP
        if (paymentType == PaymentType.PAYPAL) {
            AssertCollector.assertThat("Incorrect PSP for PO : " + extensionOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.PSP.getHeader()),
                isOneOf(PaymentProcessor.PAYPAL_EMEA.getValue(), PaymentProcessor.PAYPAL_NAMER.getValue()),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect PSP for PO : " + extensionOrderId,
                FinanceReportUtils.getColumnValueFromList(reportRow, actualRestHeader,
                    FinanceReportHeaders.PSP.getHeader()),
                isOneOf(PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentProcessor.BLUESNAP_NAMER.getValue()),
                assertionErrorList);
        }
        // Payment Type
        AssertCollector.assertThat(
            "Incorrect Payment Type for PO : " + extensionOrderId, FinanceReportUtils.getColumnValueFromList(reportRow,
                actualRestHeader, FinanceReportHeaders.PAYMENT_TYPE.getHeader()),
            equalTo(paymentOption.getValue()), assertionErrorList);

    }

    /**
     * Method to generate FR and assert on promotion related fields
     *
     * @param assertionErrorList
     */
    public static void assertionsForPromotionRelatedFields(final PelicanPlatform resource, final String purchaseOrderId,
        final List<JPromotion> listOfPromotions, final List<AssertionError> assertionErrorList) {
        // Get the Finance report using the API using end timestamp after all POs created

        final String expStartDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, -3);
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 5);
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        for (final String reportRow : actualReport) {
            // Get the line of the report with the PO id as OrderNumber!
            if (FinanceReportUtils
                .getColumnValueFromList(reportRow, actualRestHeader, FinanceReportHeaders.ORDER_NUMBER.getHeader())
                .equalsIgnoreCase(purchaseOrderId)) {

                final String promoSubTypeInReport = FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.PROMO_SUB_TYPE.getHeader());
                final String promoSetupAmountInReport = FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.PROMO_SETUP_AMOUNT.getHeader());
                final String promoSetupUnitInReport = FinanceReportUtils.getColumnValueFromList(reportRow,
                    actualRestHeader, FinanceReportHeaders.PROMO_SETUP_UNIT.getHeader());

                for (final JPromotion promotion : listOfPromotions) {

                    final PromotionType promotionType = promotion.getData().getPromotionType();
                    if (promotionType == PromotionType.DISCOUNT_AMOUNT
                        && promotionType.getDisplayName().equals(promoSubTypeInReport)) {
                        AssertCollector.assertThat("Incorrect Promo setup amount",
                            Double.parseDouble(promoSetupAmountInReport),
                            equalTo(promotion.getData().getDiscountAmount()), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Promo setup unit", promoSetupUnitInReport, equalTo("USD"),
                            assertionErrorList);
                        AssertCollector.assertThat("Incorrect promo subtype", promoSubTypeInReport,
                            equalTo(PromotionType.DISCOUNT_AMOUNT.getDisplayName()), assertionErrorList);
                        break;
                    } else if (promotionType == PromotionType.DISCOUNT_PERCENTAGE
                        && promotionType.getDisplayName().equals(promoSubTypeInReport)) {
                        AssertCollector.assertThat("Incorrect Promo setup amount",
                            Double.parseDouble(promoSetupAmountInReport),
                            equalTo(promotion.getData().getDiscountPercent()), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Promo setup unit", promoSetupUnitInReport,
                            equalTo("Percentage"), assertionErrorList);
                        AssertCollector.assertThat("Incorrect promo subtype", promoSubTypeInReport,
                            equalTo(PromotionType.DISCOUNT_PERCENTAGE.getDisplayName()), assertionErrorList);
                        break;
                    } else if (promotionType == PromotionType.SUPPLEMENT_TIME
                        && promotionType.getDisplayName().equals(promoSubTypeInReport)) {
                        AssertCollector.assertThat("Incorrect Promo setup amount", promoSetupAmountInReport,
                            equalTo(String.valueOf(promotion.getData().getTimePeriodCount())), assertionErrorList);
                        AssertCollector.assertThat("Incorrect Promo setup unit", promoSetupUnitInReport,
                            equalTo("month"), assertionErrorList);
                        AssertCollector.assertThat("Incorrect promo subtype", promoSubTypeInReport,
                            equalTo(PromotionType.SUPPLEMENT_TIME.getDisplayName()), assertionErrorList);
                        break;
                    }
                }
            }
        }
    }
}
