package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.GatewayResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionExtension.SubscriptionExtensionResponse;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Helper class to do assertions for Purchase Order Tests.
 *
 * @author t_joshv
 */
public class HelperForPurchaseOrder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelperForPurchaseOrder.class.getSimpleName());

    /**
     * This method calculates & asserts total amount, discounts for line items level and total
     *
     * @param lineItems list of line item.
     * @param purchaseOrder purchase order.
     * @param priceOfferingAmountMap price id to amount map.
     * @param promotionsMap promotion id to promotion map.
     * @param vatPercentage vat percentage.
     * @param assertionErrorList
     */
    public static void assertionForLineItemCalculation(final List<LineItem> lineItems,
        final PurchaseOrder purchaseOrder, final Map<String, String> priceOfferingAmountMap,
        final Map<String, JPromotion> promotionsMap, final Double vatPercentage,
        final List<AssertionError> assertionErrorList) {

        Double totalAmountBeforePromotionForLineItem = 0.00;
        Double promotionDiscountForLineItem = 0.00;
        Double subTotalAfterPromotionForLineItem = 0.00;
        Double subTotalAfterPromotion = 0.00;

        Double totalTaxes = 0.00;
        Double totalAmount;
        Double subTotalAfterPromotionsWithTax;
        int quantity;

        // Assert line item totals
        for (final LineItem reqLineItem : lineItems) {

            // get quantity
            quantity = reqLineItem.getOffering().getOfferingRequest().getQuantity();

            // check if line's price id exist in map.
            if (priceOfferingAmountMap.containsKey(reqLineItem.getOffering().getOfferingRequest().getPriceId())) {

                // get amount
                final String offeringAmount =
                    priceOfferingAmountMap.get(reqLineItem.getOffering().getOfferingRequest().getPriceId());

                totalAmountBeforePromotionForLineItem =
                    subTotalAfterPromotionForLineItem = Double.parseDouble(offeringAmount) * quantity;

                // check if promotion exist.
                if (reqLineItem.getPromotionReferences() != null && promotionsMap
                    .containsKey(reqLineItem.getPromotionReferences().getPromotionReference().getId())) {

                    final JPromotion promotion =
                        promotionsMap.get(reqLineItem.getPromotionReferences().getPromotionReference().getId());

                    if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                        promotionDiscountForLineItem = promotion.getData().getDiscountAmount() * quantity;
                    }
                    if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {
                        promotionDiscountForLineItem =
                            totalAmountBeforePromotionForLineItem * promotion.getData().getDiscountPercent() / 100;
                    }

                    subTotalAfterPromotionForLineItem =
                        totalAmountBeforePromotionForLineItem - promotionDiscountForLineItem;
                }

                // Calculate additional tax/fee per item
                if (reqLineItem.getAdditionalFees() != null) {
                    totalTaxes += Double.parseDouble(reqLineItem.getAdditionalFees().getAdditionalFee().getAmount());
                }

                subTotalAfterPromotion += subTotalAfterPromotionForLineItem;
            }
        }

        // Calculate VAT
        if (vatPercentage != 0.00) {
            subTotalAfterPromotionsWithTax = subTotalAfterPromotion / vatPercentage;
            totalTaxes += subTotalAfterPromotionsWithTax;
        }

        totalAmount = subTotalAfterPromotion + totalTaxes;

        AssertCollector.assertThat("Purchase Order amount charged is incorrect",
            purchaseOrder.getTransactions().getTransactions().get(0).getGatewayResponse().getAmountCharged(),
            equalTo(String.format("%.2f", totalAmount)), assertionErrorList);
    }

    /**
     * Method to calculate total Amount Charge for Subscription Quantity Request.
     *
     * @param addSeatPurchaseOrder
     * @param priceOfferingAmountMap
     * @param vatPercentage
     * @param promotionMap
     * @param nextBillingDate
     * @param totalNumberOfDaysInBillingCycle
     * @param assertionErrorList
     * @throws ParseException
     */
    public static void assertionForAddSeatsLineItemAmountChargedCalculation(final PurchaseOrder addSeatPurchaseOrder,
        final Map<String, String> priceOfferingAmountMap, final Double vatPercentage,
        final Map<String, JPromotion> promotionMap, final String nextBillingDate,
        final Double totalNumberOfDaysInBillingCycle, final List<AssertionError> assertionErrorList) {

        Double totalAmountBeforePromotionForLineItem;
        Double promotionDiscountForLineItem = 0.00;
        Double subTotalAfterPromotionForLineItem;
        Double subTotalAfterPromotion = 0.00;

        Double totalTaxes = 0.00;
        Double totalAmount;
        BigDecimal proratedUnitPrice;
        int quantity = 0;
        Double subTotalAfterPromotionsWithTax;

        // get total prorated days
        final int proratedDays =
            (int) DateTimeUtils.getDaysInBillingCycle(addSeatPurchaseOrder.getCreationTime(), nextBillingDate);

        // Iterate over each line item.
        for (final LineItem reqLineItem : addSeatPurchaseOrder.getLineItems().getLineItems()) {

            // get quantity
            quantity =
                Integer.parseInt(reqLineItem.getSubscriptionQuantity().getSubscriptionQuantityRequest().getQuantity());

            // check if line's price id exist in map.
            if (priceOfferingAmountMap
                .containsKey(reqLineItem.getSubscriptionQuantity().getSubscriptionQuantityRequest().getPriceId())) {

                // get Unit amount
                final double unitAmount = Double.parseDouble(priceOfferingAmountMap
                    .get(reqLineItem.getSubscriptionQuantity().getSubscriptionQuantityRequest().getPriceId()));

                proratedUnitPrice = new BigDecimal((unitAmount / totalNumberOfDaysInBillingCycle) * proratedDays)
                    .setScale(2, RoundingMode.HALF_UP);

                // get total price = proratedUnitPrice.
                totalAmountBeforePromotionForLineItem =
                    subTotalAfterPromotionForLineItem = proratedUnitPrice.doubleValue();

                // check for promotion
                if (reqLineItem.getPromotionReferences() != null
                    && promotionMap.containsKey(reqLineItem.getPromotionReferences().getPromotionReference().getId())) {

                    final JPromotion promotion =
                        promotionMap.get(reqLineItem.getPromotionReferences().getPromotionReference().getId());

                    if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                        promotionDiscountForLineItem =
                            (promotion.getData().getDiscountAmount() / totalNumberOfDaysInBillingCycle) * proratedDays;

                    }
                    if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {
                        promotionDiscountForLineItem = ((unitAmount * promotion.getData().getDiscountPercent() / 100)
                            / totalNumberOfDaysInBillingCycle) * proratedDays;
                    }

                    subTotalAfterPromotionForLineItem =
                        totalAmountBeforePromotionForLineItem - promotionDiscountForLineItem;
                }
                if (reqLineItem.getAdditionalFees().getAdditionalFee() != null) {
                    totalTaxes += Double.parseDouble(reqLineItem.getAdditionalFees().getAdditionalFee().getAmount());
                }

                subTotalAfterPromotion += subTotalAfterPromotionForLineItem;
            }
        }
        if (vatPercentage != null) {
            subTotalAfterPromotionsWithTax = subTotalAfterPromotion / vatPercentage;
            totalTaxes += subTotalAfterPromotionsWithTax;
        }
        totalAmount = subTotalAfterPromotion * quantity + totalTaxes;

        LOGGER.info("SubTotalAmount charge for all line items" + totalAmount);

        AssertCollector.assertThat("Add Seats Purchase Order amount charged is incorrect",
            addSeatPurchaseOrder.getTransactions().getTransactions().get(0).getGatewayResponse().getAmountCharged(),
            equalTo(String.format("%.2f", totalAmount)), assertionErrorList);

    }

    /***
     * Helper Method for Subscription Extension Order to calculate Amount Charge.
     *
     * @param assertionErrorList
     *
     * @throws ParseException
     * @throws NumberFormatException
     */
    public static void subscriptionExtensionAmountCharged(final PurchaseOrder subscriptionExtensionOrder,
        final List<String> billingFrequency, final Map<String, String> priceOfferingAmountMap,
        final List<AssertionError> assertionErrorList) throws NumberFormatException, ParseException {

        double totalAmount = 0.00;
        int count = 0;

        final List<LineItem> lineItems = subscriptionExtensionOrder.getLineItems().getLineItems();

        // Assert line item totals
        for (final LineItem reqLineItem : lineItems) {

            final String renewalDate =
                reqLineItem.getSubscriptionExtension().getSubscriptionExtensionRequest().getSubscriptionRenewalDate();

            final String targetRenewalDate =
                reqLineItem.getSubscriptionExtension().getSubscriptionExtensionRequest().getTargetRenewalDate();

            // get total pro-rated days
            final double proratedDays = DateTimeUtils.getDaysInBillingCycle(renewalDate, targetRenewalDate);

            // get total days in monthly cycle. getPreviousBillingDate
            final double totalNumberOfDaysInBillingCycle = DateTimeUtils.getDaysInBillingCycle(
                DateTimeUtils.getPreviousBillingDate(renewalDate, billingFrequency.get(count)), renewalDate);

            // get Quantity.
            final int quantity = Integer
                .parseInt(reqLineItem.getSubscriptionExtension().getSubscriptionExtensionRequest().getQuantity());

            final double unitPrice = Double.parseDouble(priceOfferingAmountMap
                .get(reqLineItem.getSubscriptionExtension().getSubscriptionExtensionRequest().getPriceId()));

            final BigDecimal prorationFactor =
                (new BigDecimal((proratedDays / totalNumberOfDaysInBillingCycle) * unitPrice)).setScale(2,
                    RoundingMode.HALF_UP);

            final double amountBeforeTax = prorationFactor.doubleValue() * quantity;
            double taxAmount = 0.00;

            if (reqLineItem.getAdditionalFees() != null) {
                taxAmount = Double.parseDouble(reqLineItem.getAdditionalFees().getAdditionalFee().getAmount());
            }

            final double amountPerLineItem = amountBeforeTax + taxAmount;
            totalAmount += amountPerLineItem;
            count++;
            // Assertion on Charge Detail
            assertionForSubscriptionExtensionAmountCharged(reqLineItem, proratedDays, quantity, prorationFactor,
                amountBeforeTax, assertionErrorList);
        }

        AssertCollector.assertThat(
            "Subscription Extension Order amount charged is incorrect", subscriptionExtensionOrder.getTransactions()
                .getTransactions().get(0).getGatewayResponse().getAmountCharged(),
            equalTo(String.format("%.2f", totalAmount)), assertionErrorList);

    }

    /**
     * Helper method to assert on calculated Amount for Subscription Extension Process PO for PENDING.
     *
     * @param assertionErrorList
     */
    private static void assertionForSubscriptionExtensionAmountCharged(final LineItem reqLineItem,
        final double proratedDays, final int quantity, final BigDecimal prorationFactor, final double amountBeforeTax,
        final List<AssertionError> assertionErrorList) {

        final SubscriptionExtensionResponse subscriptionExtensionResponse =
            reqLineItem.getSubscriptionExtension().getSubscriptionExtensionResponse();
        AssertCollector.assertThat("Incorrect Proration Days",
            subscriptionExtensionResponse.getChargeDetails().getProrationDays(), equalTo((int) proratedDays),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity", subscriptionExtensionResponse.getChargeDetails().getQuantity(),
            equalTo(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect ProratedUnitPrice",
            subscriptionExtensionResponse.getChargeDetails().getUnitPrice(), equalTo(prorationFactor.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Amount Charged Before Tax",
            subscriptionExtensionResponse.getChargeDetails().getTotalPrice(), equalTo((float) amountBeforeTax),
            assertionErrorList);
    }

    /**
     * Helper method to assert on FulFillment Group.
     *
     * @param assertionErrorList
     */
    public static void assertionForSubscriptionExtensionFulfillment(final PurchaseOrder subscriptionExtensionOrder,
        final String strategy, final String status, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect Fulfillment Strategy",
            subscriptionExtensionOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStrategy().toString(),
            equalTo(strategy), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Status",
            subscriptionExtensionOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStatus().toString(),
            equalTo(status), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Strategy at PO ",
            subscriptionExtensionOrder.getFulFillmentStatus().toString(), equalTo(status), assertionErrorList);
    }

    /**
     * Helper method to assert on Transaction Activity.
     *
     * @param assertionErrorList
     */
    public static void assertionForSubscriptionExtensionTransactionActivity(
        final PurchaseOrder subscriptionExtensionOrder, final int activityIndex, final String type, final String state,
        final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect Transaction Activity type", subscriptionExtensionOrder.getTransactions()
            .getTransactions().get(activityIndex).getTransactionType().toString(), equalTo(type), assertionErrorList);

        AssertCollector.assertThat("Incorrect Transaction Activity state", subscriptionExtensionOrder.getTransactions()
            .getTransactions().get(activityIndex).getGatewayResponse().getState(), equalTo(state), assertionErrorList);
    }

    /**
     * Helper method to assert on SubscriptionQuantityRequest enriched field.
     *
     * @param initialPurchaseOrder
     * @param addSeatPurchaseOrder
     * @param assertionErrorList
     */
    public static void assertionForAddSeatsEnrichField(final PurchaseOrder initialPurchaseOrder,
        final PurchaseOrder addSeatPurchaseOrder, final List<AssertionError> assertionErrorList) {
        // Assertion of Enriched Filed
        AssertCollector.assertThat("Incorrect OfferID",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getOfferId(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest()
                .getOfferId()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect OfferExternalKey",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getOfferExternalKey(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest()
                .getOfferExternalKey()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect OfferingExternalKey",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getOfferingExternalKey(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest()
                .getOfferingExternalKey()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect OfferingID",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getOfferingExternalKey(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest()
                .getOfferingExternalKey()),
            assertionErrorList);
    }

    /**
     * Helper method to assert on SubscriptionQuantityResponse field.
     *
     * @param initialPurchaseOrder
     * @param addSeatPurchaseOrder
     * @param assertionErrorList
     */
    public static void assertionForAddSeatsResponseField(final PurchaseOrder initialPurchaseOrder,
        final PurchaseOrder addSeatPurchaseOrder, final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect Currency Name",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getCurrencyName(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                .getCurrencyName()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Offering Type",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getOfferingType(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                .getOfferingType().toString()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Product Line Name",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getProductLineName(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                .getProductLineName()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Product Line Code",
            addSeatPurchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getProductLineCode(),
            equalTo(initialPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                .getProductLineCode()),
            assertionErrorList);
    }

    /**
     * Assertions for Gateway Response of Purchase Order. As of now, this method validates billing & payment related
     * fields.
     *
     * @param purchaseOrder
     * @param transactionIndex
     * @param paymentMethod
     * @param paymentProfile
     * @param assertionErrorList
     */
    public static void assertionsForGatewayResponse(final PurchaseOrder purchaseOrder, final int transactionIndex,
        final Payment.PaymentMethod paymentMethod, final PaymentProfile paymentProfile,
        final PurchaseOrderDetailPage purchaseOrderDetailPage, final int atTransactionIndex,
        final String paymentGateway, final List<AssertionError> assertionErrorList) {

        final GatewayResponse gatewayResponse =
            purchaseOrder.getTransactions().getTransactions().get(transactionIndex).getGatewayResponse();
        AssertCollector.assertThat("Incorrect payment type", gatewayResponse.getPaymentType(),
            equalTo(PaymentType.DIRECT_DEBIT.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment method", gatewayResponse.getPaymentMethod(),
            equalTo(paymentMethod.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing city", gatewayResponse.getBillingCity(),
            equalTo(paymentProfile.getDirectDebitPayment().getCity()), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing country", gatewayResponse.getBillingCountry(),
            equalTo(paymentProfile.getDirectDebitPayment().getCountryCode()), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing postal code", gatewayResponse.getBillingPostalCode(),
            equalTo(paymentProfile.getDirectDebitPayment().getZipCode()), assertionErrorList);
        if (paymentMethod == Payment.PaymentMethod.ACH) {
            AssertCollector.assertThat("Incorrect state province", gatewayResponse.getBillingStateProvince(),
                equalTo(paymentProfile.getDirectDebitPayment().getState()), assertionErrorList);
        }

        AssertCollector.assertThat("Incorrect payment type in transaction",
            purchaseOrderDetailPage.getTransactionPaymentType(atTransactionIndex),
            equalTo(PaymentType.DIRECT_DEBIT.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment gateway in transaction",
            purchaseOrderDetailPage.getTransactionPaymentGateway(atTransactionIndex), equalTo(paymentGateway),
            assertionErrorList);
    }
}
