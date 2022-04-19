package com.autodesk.bsm.pelican.api.helpers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JAdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.json.JLineItem;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes.PriceQuoteData;
import com.autodesk.bsm.pelican.api.pojos.json.ShipTo;
import com.autodesk.bsm.pelican.api.pojos.json.Shipping;
import com.autodesk.bsm.pelican.api.pojos.json.Totals;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.PurchaseType;
import com.autodesk.bsm.pelican.enums.StateProvince;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to do assertions for Price Quote Tests Created by Shweta Hegde on 12/6/16.
 */
public class HelperForPriceQuote {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelperForPriceQuote.class.getSimpleName());

    /**
     * This method calculates & asserts total amount, discounts for line items level and total
     *
     * @param offeringsPromotionMap
     * @param priceQuotesResponse
     * @param requestData
     * @param vatPercentage
     * @param purchaseType TODO
     * @param subscriptionList TODO
     * @param environmentVariables TODO
     * @param isMerge TODO
     * @param assertionErrorList
     * @param PelicanPlatform resource
     */
    public static void assertionForLineItemCalculation(final Map<Offerings, List<JPromotion>> offeringsPromotionMap,
        final PriceQuotes priceQuotesResponse, final PriceQuotes.PriceQuoteData requestData, final Double vatPercentage,
        final String purchaseType, final List<Subscription> subscriptionList,
        final EnvironmentVariables environmentVariables, final Boolean isMerge,
        final List<AssertionError> assertionErrorList) {

        BigDecimal subTotalForLineItem = null;
        BigDecimal subTotalBeforePromotionsForLineItem;
        BigDecimal subTotalAfterPromotionsForLineItem = null;
        BigDecimal discountForLineItem = new BigDecimal(0);
        BigDecimal unitPriceAfterPromotions = null;
        BigDecimal unitPriceAfterPromotionsWithoutProration = null;
        BigDecimal unitPriceBeforePromotions;
        BigDecimal unitPriceBeforePromotionsWithoutProration;
        BigDecimal subTotalWithTax;
        BigDecimal subTotalAfterPromotionsWithTax = new BigDecimal(0);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal subTotalAfterPromotions = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(0);
        BigDecimal taxes = new BigDecimal(0);
        BigDecimal shipping = new BigDecimal(0);
        BigDecimal subTotalForLineItemWithRounding = new BigDecimal(0);
        BigDecimal unitDiscountForLineItem = new BigDecimal(0);
        BigDecimal unitDiscountForLineItemWithoutProration = new BigDecimal(0);
        final PelicanPlatform pelicanResource = new PelicanClient(environmentVariables).platform();

        int itemQuantity;
        final Set<String> pricesIdsSet = new HashSet<>();
        int totalQuantityOfOneLineItem = 0;
        String sqlQuery;

        // Assert line item totals
        for (final JLineItem item : priceQuotesResponse.getData().getLineItems()) {
            for (final Offerings offering : offeringsPromotionMap.keySet()) {
                if (item.getPriceId() != null) {

                    if (item.getPriceId().equals(offering.getIncluded().getPrices().get(0).getId())) {

                        itemQuantity = item.getQuantity();
                        sqlQuery = PelicanDbConstants.SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE + item.getPriceId();
                        BigDecimal unitPrice = new BigDecimal(DbUtils
                            .selectQuery(sqlQuery, PelicanDbConstants.AMOUNT_DB_FIELD, environmentVariables).get(0));
                        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);

                        if (PurchaseType.OFFERING.getName().equalsIgnoreCase(purchaseType)) {
                            BigDecimal price = new BigDecimal(offering.getIncluded().getPrices().get(0).getAmount());
                            price = price.setScale(2, RoundingMode.HALF_UP);
                            subTotalForLineItem = price.multiply(BigDecimal.valueOf(itemQuantity));
                            subTotalForLineItemWithRounding = subTotalForLineItem.setScale(2, RoundingMode.HALF_UP);

                            if (requestData.getCartUpdated()) {
                                // Calculate promotion discount
                                if (offeringsPromotionMap.get(offering) != null) {
                                    final List<JPromotion> promotionList = offeringsPromotionMap.get(offering);

                                    if (promotionList.size() > 0) {

                                        final JPromotion promotion = promotionList.get(0);
                                        promotionList.remove(0);
                                        discountForLineItem = getDiscountPerLineItem(promotion, offering, itemQuantity,
                                            subTotalForLineItemWithRounding);
                                    } else {
                                        discountForLineItem = new BigDecimal(0);
                                    }
                                }
                                subTotalAfterPromotionsForLineItem =
                                    subTotalForLineItem.subtract(discountForLineItem).setScale(2, RoundingMode.HALF_UP);
                                unitPriceAfterPromotions =
                                    subTotalAfterPromotionsForLineItem.divide(BigDecimal.valueOf(itemQuantity));
                                unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotions;
                            } else {
                                subTotalAfterPromotionsForLineItem = subTotalForLineItem;
                                unitPriceAfterPromotions = subTotalForLineItem.divide(BigDecimal.valueOf(itemQuantity))
                                    .setScale(2, RoundingMode.HALF_UP);
                                unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotions;
                            }
                        } else if (PurchaseType.SUBSCRIPTIONQUANTITY.getName().equalsIgnoreCase(purchaseType)
                            && subscriptionList != null) {

                            for (final Subscription subscription : subscriptionList) {
                                if (item.getSubscriptionId().equalsIgnoreCase(subscription.getId())) {
                                    final String billingType =
                                        subscription.getBillingOption().getBillingPeriod().getType();
                                    final int numberOfDays = Util.getCountOfBillingDays(subscription.getId(),
                                        pelicanResource, environmentVariables);
                                    LOGGER.info("Number of days between past billing date and next billing date are: "
                                        + numberOfDays);
                                    final String nextBillingDate = subscription.getNextBillingDate();
                                    LOGGER.info("Next Billing Date: " + nextBillingDate);
                                    final Integer proratedDays = getProratedDays(nextBillingDate);
                                    LOGGER
                                        .info("Prorated Days in the response: " + item.getTotals().getProrationDays());
                                    AssertCollector.assertThat("Incorrect number of proration days",
                                        Integer.parseInt(item.getTotals().getProrationDays()), equalTo(proratedDays),
                                        assertionErrorList);
                                    unitPriceBeforePromotions = getUnitPriceAmount(proratedDays,
                                        new BigDecimal(subscription.getNextBillingPriceAmount()), numberOfDays);
                                    unitPriceBeforePromotionsWithoutProration = getUnitPriceBeforePromotions(
                                        new BigDecimal(subscription.getNextBillingPriceAmount()),
                                        subscription.getQuantity());
                                    subTotalBeforePromotionsForLineItem = unitPriceBeforePromotions
                                        .setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(itemQuantity));
                                    subTotalForLineItem = subTotalBeforePromotionsForLineItem;
                                    subTotalForLineItemWithRounding =
                                        subTotalForLineItem.setScale(2, RoundingMode.HALF_UP);
                                    if (requestData.getCartUpdated()) {
                                        if (offeringsPromotionMap.get(offering) != null) {
                                            final List<JPromotion> promotionList = offeringsPromotionMap.get(offering);
                                            if (promotionList.size() > 0) {

                                                final JPromotion promotion = promotionList.get(0);
                                                promotionList.remove(0);
                                                unitDiscountForLineItem =
                                                    getUnitProratedDiscountPerLineItemWithBundlePromotion(promotion,
                                                        offering, itemQuantity, unitPrice, numberOfDays, proratedDays,
                                                        priceQuotesResponse, assertionErrorList);
                                                unitDiscountForLineItemWithoutProration =
                                                    getUnitDiscountPerLineItemWithPromotion(promotion, offering,
                                                        itemQuantity, unitPrice, priceQuotesResponse,
                                                        assertionErrorList);
                                            } else {
                                                unitDiscountForLineItem = new BigDecimal(0);
                                                unitDiscountForLineItemWithoutProration = new BigDecimal(0);
                                                discountForLineItem = new BigDecimal(0);
                                            }
                                        }
                                        unitPriceAfterPromotions =
                                            unitPriceBeforePromotions.subtract(unitDiscountForLineItem);
                                        unitPriceAfterPromotionsWithoutProration =
                                            unitPriceBeforePromotionsWithoutProration
                                                .subtract(unitDiscountForLineItemWithoutProration);
                                        subTotalAfterPromotionsForLineItem =
                                            unitPriceAfterPromotions.setScale(2, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(itemQuantity));
                                        discountForLineItem =
                                            subTotalForLineItem.subtract(subTotalAfterPromotionsForLineItem);
                                        unitPriceAfterPromotions =
                                            unitPriceAfterPromotions.setScale(2, RoundingMode.HALF_UP);
                                        subTotalAfterPromotionsForLineItem =
                                            subTotalAfterPromotionsForLineItem.setScale(2, RoundingMode.HALF_UP);
                                    } else {
                                        subTotalAfterPromotionsForLineItem = subTotalForLineItemWithRounding;
                                        unitPriceAfterPromotions = subTotalAfterPromotionsForLineItem
                                            .divide(BigDecimal.valueOf(itemQuantity)).setScale(2, RoundingMode.HALF_UP);
                                        unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotions;
                                    }
                                }
                            }
                        } else if (PurchaseType.SUBSCRIPTIONEXTENSION.getName().equalsIgnoreCase(purchaseType)
                            && subscriptionList != null) {
                            for (final Subscription subscription : subscriptionList) {
                                if (item.getSubscriptionId().equalsIgnoreCase(subscription.getId())) {
                                    final String billingType =
                                        subscription.getBillingOption().getBillingPeriod().getType();
                                    final int numberOfDays = Util.getCountOfBillingDays(subscription.getId(),
                                        pelicanResource, environmentVariables);
                                    LOGGER.info("Number of days between past billing date and next billing date are: "
                                        + numberOfDays);
                                    String nextBillingDate = subscription.getNextBillingDate();
                                    String targetRenewalDate = item.getTargetRenewalDate();
                                    nextBillingDate = nextBillingDate.split(" ")[0];
                                    targetRenewalDate = targetRenewalDate.split(" UTC")[0];
                                    LOGGER.info("Next Billing Date: " + nextBillingDate);
                                    LOGGER.info("Target Renewal Date: " + targetRenewalDate);
                                    final Integer proratedDays =
                                        getProratedDaysBetweenTwoDates(nextBillingDate, targetRenewalDate);
                                    LOGGER
                                        .info("Prorated Days in the response: " + item.getTotals().getProrationDays());
                                    AssertCollector.assertThat("Incorrect number of proration days",
                                        Integer.parseInt(item.getTotals().getProrationDays()), equalTo(proratedDays),
                                        assertionErrorList);
                                    if (isMerge) {
                                        unitPriceBeforePromotions = getUnitPriceAmount(proratedDays,
                                            new BigDecimal(subscription.getNextBillingPriceAmount()), numberOfDays);
                                    } else {
                                        unitPriceBeforePromotions = getUnitPriceAmount(proratedDays,
                                            new BigDecimal(subscription.getNextBillingPriceAmount()), numberOfDays,
                                            itemQuantity);
                                    }
                                    unitPriceBeforePromotionsWithoutProration = getUnitPriceBeforePromotions(
                                        new BigDecimal(subscription.getNextBillingPriceAmount()),
                                        subscription.getQuantity());
                                    subTotalBeforePromotionsForLineItem = unitPriceBeforePromotions
                                        .setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(itemQuantity));
                                    subTotalForLineItem = subTotalBeforePromotionsForLineItem;
                                    subTotalForLineItemWithRounding =
                                        subTotalForLineItem.setScale(2, RoundingMode.HALF_UP);
                                    if (requestData.getCartUpdated()) {
                                        if (offeringsPromotionMap.get(offering) != null) {
                                            final List<JPromotion> promotionList = offeringsPromotionMap.get(offering);
                                            if (promotionList.size() > 0) {

                                                // Logic can change in the future, so keeping this
                                                // code as same without removing the condition
                                                final JPromotion promotion = promotionList.get(0);
                                                promotionList.remove(0);
                                                unitDiscountForLineItem = new BigDecimal(0);
                                                unitDiscountForLineItemWithoutProration = new BigDecimal(0);
                                                discountForLineItem = new BigDecimal(0);
                                            } else {
                                                unitDiscountForLineItem = new BigDecimal(0);
                                                unitDiscountForLineItemWithoutProration = new BigDecimal(0);
                                                discountForLineItem = new BigDecimal(0);
                                            }
                                        }
                                        unitPriceAfterPromotions =
                                            unitPriceBeforePromotions.subtract(unitDiscountForLineItem);
                                        unitPriceAfterPromotionsWithoutProration =
                                            unitPriceBeforePromotionsWithoutProration
                                                .subtract(unitDiscountForLineItemWithoutProration);
                                        subTotalAfterPromotionsForLineItem =
                                            unitPriceAfterPromotions.setScale(2, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(itemQuantity));
                                        discountForLineItem =
                                            subTotalForLineItem.subtract(subTotalAfterPromotionsForLineItem);
                                        unitPriceAfterPromotions =
                                            unitPriceAfterPromotions.setScale(2, RoundingMode.HALF_UP);
                                        subTotalAfterPromotionsForLineItem =
                                            subTotalAfterPromotionsForLineItem.setScale(2, RoundingMode.HALF_UP);
                                    } else {
                                        subTotalAfterPromotionsForLineItem = subTotalForLineItemWithRounding;
                                        unitPriceAfterPromotions = subTotalAfterPromotionsForLineItem
                                            .divide(BigDecimal.valueOf(itemQuantity)).setScale(2, RoundingMode.HALF_UP);
                                        unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotions;
                                    }
                                }
                            }

                        } else if (PurchaseType.SUBSCRIPTIONRENEWAL.getName().equalsIgnoreCase(purchaseType)
                            && subscriptionList != null) {
                            for (final Subscription subscription : subscriptionList) {
                                if (item.getSubscriptionId().equalsIgnoreCase(subscription.getId())) {

                                    final JSubscription getSubscription = pelicanResource.subscriptionJson()
                                        .getSubscription(subscription.getId(), PelicanConstants.CONTENT_TYPE);
                                    unitPrice = (new BigDecimal(getSubscription.getData().getNextBillingPriceAmount()))
                                        .setScale(2, RoundingMode.HALF_UP);

                                    final BigDecimal subscriptionQuantity = new BigDecimal(item.getQuantity());

                                    AssertCollector.assertThat(
                                        "Price id is not correct in response for " + "subscription: "
                                            + subscription.getId(),
                                        item.getPriceId(), equalTo(item.getPriceId()), assertionErrorList);
                                    AssertCollector.assertThat(
                                        "Quantity is not correct in response for " + "subscription: "
                                            + subscription.getId(),
                                        item.getQuantity(), equalTo(item.getQuantity()), assertionErrorList);

                                    AssertCollector.assertThat(
                                        "Unit price is not correct in response for " + "subscription: "
                                            + subscription.getId(),
                                        item.getTotals().getUnitPriceAmount(), equalTo(unitPrice.toString()),
                                        assertionErrorList);

                                    JPromotion promotion = null;
                                    if (offeringsPromotionMap.get(offering) != null) {
                                        final List<JPromotion> promotionList = offeringsPromotionMap.get(offering);
                                        if (promotionList.size() > 0) {
                                            promotion = promotionList.get(0);
                                            promotionList.remove(0);
                                        }
                                    }

                                    unitDiscountForLineItem = getUnitDiscountPerLineItemWithCreditDaysAndPromotion(
                                        offering, item, unitPrice, priceQuotesResponse, promotion, assertionErrorList);
                                    discountForLineItem = unitDiscountForLineItem.multiply(subscriptionQuantity);
                                    subTotalForLineItem = unitPrice.multiply(subscriptionQuantity);
                                    unitPriceAfterPromotions = unitPrice.subtract(discountForLineItem);
                                    subTotalAfterPromotionsForLineItem =
                                        subTotalForLineItem.subtract(discountForLineItem);
                                    unitPriceAfterPromotions = unitPrice.subtract(unitDiscountForLineItem);

                                    if (offeringsPromotionMap.get(offering) != null) {
                                        unitPriceAfterPromotionsWithoutProration = subTotalAfterPromotionsForLineItem;
                                    } else {
                                        unitPriceAfterPromotionsWithoutProration = unitPrice;
                                    }

                                    subTotalForLineItemWithRounding = subTotalForLineItem;

                                    subTotalAfterPromotionsWithTax = subTotalAfterPromotionsForLineItem;
                                }
                            }

                        }

                        BigDecimal additionalFees = new BigDecimal(0);
                        BigDecimal additionalFeeMultiplier;

                        // calculate additional taxes when vatPercentage == 0
                        if (vatPercentage == 0.00) {
                            // Go through each line items in the request, if
                            // price
                            // id of response line item is same as the request,
                            // get the additional fees
                            for (final JLineItem requestLineItem : requestData.getLineItems()) {

                                if (requestLineItem.getSubscriptionId() != null) {

                                    if (requestLineItem.getPriceId().equals(item.getPriceId())
                                        || requestLineItem.getSubscriptionId().equals(item.getSubscriptionId())) {

                                        if (!(requestLineItem.getPurchaseType()
                                            .equals(PurchaseType.SUBSCRIPTIONEXTENSION.getName()))) {
                                            totalQuantityOfOneLineItem += requestLineItem.getQuantity() == null ? 1
                                                : requestLineItem.getQuantity();
                                        } else {

                                            for (final Subscription subscription : subscriptionList) {
                                                if (subscription.getId().equals(requestLineItem.getSubscriptionId())) {
                                                    totalQuantityOfOneLineItem = subscription.getQuantity();
                                                }
                                            }

                                        }

                                        if (requestLineItem.getAdditionalFees() != null) {

                                            for (final JAdditionalFee additionalFee : requestLineItem
                                                .getAdditionalFees()) {

                                                additionalFees =
                                                    additionalFees.add(new BigDecimal(additionalFee.getAmount()));
                                            }
                                            if (!requestData.getCartUpdated()) {
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    if (requestLineItem.getPriceId().equals(item.getPriceId())) {

                                        totalQuantityOfOneLineItem += requestLineItem.getQuantity();

                                        if (requestLineItem.getAdditionalFees() != null) {

                                            for (final JAdditionalFee additionalFee : requestLineItem
                                                .getAdditionalFees()) {

                                                additionalFees =
                                                    additionalFees.add(new BigDecimal(additionalFee.getAmount()));
                                            }
                                            if (!requestData.getCartUpdated()) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (!pricesIdsSet.contains(item.getPriceId()) && requestData.getCartUpdated()) {

                                taxes = taxes.add(additionalFees);
                                pricesIdsSet.add(item.getPriceId());
                            }

                            if (!requestData.getCartUpdated()) {
                                taxes = taxes.add(additionalFees);
                            }

                            if (requestData.getCartUpdated()) {

                                additionalFeeMultiplier = BigDecimal.valueOf(itemQuantity)
                                    .divide(new BigDecimal(totalQuantityOfOneLineItem), 2, BigDecimal.ROUND_HALF_EVEN);
                                final BigDecimal additionalFeesForLineItem =
                                    additionalFeeMultiplier.multiply(additionalFees);
                                subTotalAfterPromotionsWithTax =
                                    subTotalAfterPromotionsForLineItem.add(additionalFeesForLineItem);
                            } else {
                                subTotalAfterPromotionsWithTax = subTotalAfterPromotionsForLineItem.add(additionalFees);
                            }
                        }

                        // If vat percentage is not 0, then calculate subtotal
                        // with
                        // tax
                        if (vatPercentage != 0.00) {

                            // add subtotalForLineItem with vat percentage
                            subTotalWithTax =
                                subTotalForLineItem.add(subTotalForLineItem.divide(BigDecimal.valueOf(vatPercentage)));

                            AssertCollector.assertThat(
                                "SubTotal with tax at item level returned is incorrect for Price Id : "
                                    + item.getPriceId(),
                                item.getTotals().getSubtotalWithTax(),
                                equalTo(String.format("%.2f", subTotalWithTax.setScale(2, RoundingMode.HALF_UP))),
                                assertionErrorList);

                            // add subTotalAfterPromotionsForLineItem with vat
                            // percentage
                            subTotalAfterPromotionsWithTax = subTotalAfterPromotionsForLineItem
                                .add(subTotalAfterPromotionsForLineItem.divide(BigDecimal.valueOf(vatPercentage)));

                            // add all the vat percentage tax with total tax
                            taxes =
                                taxes.add(subTotalAfterPromotionsForLineItem.divide(BigDecimal.valueOf(vatPercentage)));

                            AssertCollector.assertThat(
                                "SubtotalAfterPromotionsWithTax returned is incorrect for Price Id : "
                                    + item.getPriceId(),
                                item.getTotals().getSubtotalAfterPromotionsWithTax(),
                                equalTo(String.format("%.2f",
                                    subTotalAfterPromotionsWithTax.setScale(2, RoundingMode.HALF_UP))),
                                assertionErrorList);
                        }

                        // for some error scenarios, getTotals will be null, so
                        // if
                        // not null do the assertion
                        if (item.getTotals() != null) {
                            AssertCollector.assertThat(
                                "SubTotal at line item level returned is incorrect for Price Id : " + item.getPriceId(),
                                item.getTotals().getSubtotal(),
                                equalTo(String.format("%.2f", subTotalForLineItemWithRounding)), assertionErrorList);
                            AssertCollector.assertThat(
                                "SubtotalAfterPromotions at line item is incorrect for Price Id : " + item.getPriceId(),
                                item.getTotals().getSubtotalAfterPromotions(),
                                equalTo(String.format("%.2f", subTotalAfterPromotionsForLineItem)), assertionErrorList);
                            AssertCollector.assertThat(
                                "Discount at line item level returned is incorrect for Price Id : " + item.getPriceId(),
                                item.getTotals().getDiscount(),
                                equalTo(String.format("%.2f", discountForLineItem.setScale(2, RoundingMode.HALF_UP))),
                                assertionErrorList);
                            AssertCollector.assertThat(
                                "UnitPriceAfterPromotions returned is incorrect for Price Id : " + item.getPriceId(),
                                item.getTotals().getUnitPriceAfterPromotions(),
                                equalTo(
                                    String.format("%.2f", unitPriceAfterPromotions.setScale(2, RoundingMode.HALF_UP))),
                                assertionErrorList);
                            AssertCollector
                                .assertThat(
                                    "UnitPriceAfterPromotionswithout proration returned is incorrect for Price Id : "
                                        + item.getPriceId(),
                                    item.getTotals().getUnitPriceAfterPromotionsWithoutProration(),
                                    equalTo(String.format("%.2f",
                                        unitPriceAfterPromotionsWithoutProration.setScale(2, RoundingMode.HALF_UP))),
                                    assertionErrorList);

                            // Subtotal after promotions with tax, if
                            // vatPercentage
                            // is 0, then additional fees will be
                            // added to calculate and subTotalWithTax would be
                            // null
                            if (vatPercentage == 0.00) {
                                AssertCollector.assertThat(
                                    "SubtotalAfterPromotionsWithTax returned is incorrect for Price Id : "
                                        + item.getPriceId(),
                                    item.getTotals().getSubtotalAfterPromotionsWithTax(),
                                    equalTo(String.format("%.2f",
                                        subTotalAfterPromotionsWithTax.setScale(2, RoundingMode.HALF_UP))),
                                    assertionErrorList);
                                AssertCollector.assertThat(
                                    "SubTotal with tax at item level returned is incorrect for Price Id : "
                                        + item.getPriceId(),
                                    item.getTotals().getSubtotalWithTax(), nullValue(), assertionErrorList);
                            }
                        } else {
                            subTotalForLineItem = new BigDecimal(0);
                            subTotalAfterPromotionsForLineItem = new BigDecimal(0);
                            discountForLineItem = new BigDecimal(0);
                        }

                        totalQuantityOfOneLineItem = 0;
                        // add each line item total to the total calculation
                        subTotal = subTotal.add(subTotalForLineItemWithRounding);
                        discount = discount.add(discountForLineItem.setScale(2, RoundingMode.HALF_UP));
                        subTotalAfterPromotions = subTotalAfterPromotions.add(subTotalAfterPromotionsForLineItem);
                    }
                }
            }
        }

        // calculate shipping and additional fee with it
        if (requestData.getShipping() != null) {
            shipping = new BigDecimal(requestData.getShipping().getTotals().getShipping());

            if (requestData.getShipping().getAdditionalFees() != null) {
                taxes = taxes.add(new BigDecimal(requestData.getShipping().getAdditionalFees().get(0).getAmount()));
            }
        }
        final BigDecimal total = subTotalAfterPromotions.add(shipping).add(taxes);

        if (priceQuotesResponse.getData().getTotals() != null) {
            // Assert data totals, which is the total of all line items
            AssertCollector.assertThat("SubTotal returned is incorrect",
                priceQuotesResponse.getData().getTotals().getSubtotal(), equalTo(String.format("%.2f", subTotal)),
                assertionErrorList);
            AssertCollector.assertThat("SubtotalAfterPromotions returned is incorrect",
                priceQuotesResponse.getData().getTotals().getSubtotalAfterPromotions(),
                equalTo(String.format("%.2f", subTotalAfterPromotions.setScale(2, RoundingMode.HALF_UP))),
                assertionErrorList);
            AssertCollector.assertThat("Discount returned is incorrect",
                priceQuotesResponse.getData().getTotals().getDiscount(),
                equalTo(String.format("%.2f", discount.setScale(2, RoundingMode.HALF_UP))), assertionErrorList);
            AssertCollector.assertThat("Taxes returned is incorrect",
                priceQuotesResponse.getData().getTotals().getTaxes(),
                equalTo(String.format("%.2f", taxes.setScale(2, RoundingMode.HALF_UP))), assertionErrorList);
            AssertCollector.assertThat("Shipping returned is incorrect",
                priceQuotesResponse.getData().getTotals().getShipping(),
                equalTo(String.format("%.2f", shipping.setScale(2, RoundingMode.HALF_UP))), assertionErrorList);
            AssertCollector.assertThat("Total returned is incorrect",
                priceQuotesResponse.getData().getTotals().getTotal(),
                equalTo(String.format("%.2f", total.setScale(2, RoundingMode.HALF_UP))), assertionErrorList);
        }
    }

    /**
     * This method calculates discount per line item based on whether the promotion is bundled or non-bundled for
     * Purchase Type as"Offering"
     *
     * @param promotion
     * @param offering
     * @param itemQuantity
     * @param subTotalForLineItem
     * @return discountPerLineItem
     */
    private static BigDecimal getDiscountPerLineItem(final JPromotion promotion, final Offerings offering,
        final int itemQuantity, final BigDecimal subTotalForLineItem) {

        BigDecimal discountForLineItem = new BigDecimal(0);

        // calculate bundle promotion discount based on least common denominator
        if (promotion.getData().getIsBundledPromo()) {

            // calculate bundle promotion for subscription offers
            if (promotion.getData().getSubscriptionOffers().size() > 0
                && offering.getIncluded().getBillingPlans().size() > 0) {

                for (final JPromotionData.PromotionOfferings promotionOffer : promotion.getData()
                    .getSubscriptionOffers()) {

                    if (promotionOffer.getId().equals(offering.getIncluded().getBillingPlans().get(0).getId())) {

                        discountForLineItem = getBundlePromotionDiscountPerLineItem(promotion,
                            promotionOffer.getQuantity(), itemQuantity, subTotalForLineItem);
                    }
                }
            }
            // calculate bundle promotion for basic offering
            if (promotion.getData().getBasicOfferings().size() > 0
                && offering.getIncluded().getBillingPlans().size() == 0) {

                for (final JPromotionData.PromotionOfferings promotionOffer : promotion.getData().getBasicOfferings()) {

                    if (promotionOffer.getId().equals(offering.getOfferings().get(0).getId())) {

                        discountForLineItem = getBundlePromotionDiscountPerLineItem(promotion,
                            promotionOffer.getQuantity(), itemQuantity, subTotalForLineItem);
                    }
                }
            }
        } else {
            // if not bundle promotion, calculate promotion discount
            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                discountForLineItem = new BigDecimal(promotion.getData().getDiscountAmount())
                    .setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(itemQuantity));
            }

            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {
                discountForLineItem =
                    subTotalForLineItem.multiply(BigDecimal.valueOf(promotion.getData().getDiscountPercent() / 100));
            }
        }
        return discountForLineItem;
    }

    /**
     * This method calculates prorated discount per line item based on whether the promotion is bundled or non-bundled
     *
     * @param promotion
     * @param offering
     * @param itemQuantity
     * @param unitPrice
     * @param periodDays
     * @param proratedDays
     * @param assertionErrorList
     * @return discountPerLineItem
     */
    private static BigDecimal getUnitProratedDiscountPerLineItemWithBundlePromotion(final JPromotion promotion,
        final Offerings offering, final int itemQuantity, BigDecimal unitPrice, final int periodDays,
        final Integer proratedDays, final PriceQuotes priceQuotesResponse,
        final List<AssertionError> assertionErrorList) {

        BigDecimal discountForLineItemPerProratedDays = new BigDecimal(0);
        BigDecimal discountForLineItemPerDay = new BigDecimal(0);

        // calculate bundle promotion discount based on least common denominator
        if (promotion.getData().getIsBundledPromo()) {
            LOGGER.info("Bundled promo is not applied to Add Seats");
            AssertCollector.assertTrue(
                "Incorrect error for - Bundled promotion discount is not applied for SubscriptionPurchase",
                HelperForPriceQuote.parsePqErrors(priceQuotesResponse,
                    PelicanErrorConstants.ADD_SEATS_BUNDLED_PROMO_ERROR_MESSAGE),
                assertionErrorList);
        } else {
            // if not bundle promotion, calculate promotion discount
            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                discountForLineItemPerDay = new BigDecimal(promotion.getData().getDiscountAmount())
                    .divide(new BigDecimal(periodDays), 10, BigDecimal.ROUND_HALF_EVEN);
                discountForLineItemPerProratedDays = discountForLineItemPerDay.multiply(new BigDecimal(proratedDays));

            }

            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {
                unitPrice = unitPrice.multiply(new BigDecimal(proratedDays));
                unitPrice = unitPrice.divide(new BigDecimal(periodDays), 10, BigDecimal.ROUND_HALF_EVEN);
                discountForLineItemPerProratedDays =
                    unitPrice.multiply(BigDecimal.valueOf(promotion.getData().getDiscountPercent() / 100));
            }
        }
        return discountForLineItemPerProratedDays;
    }

    /**
     * This method calculates discount per line item based on whether the promotion is bundled or non-bundled
     *
     * @param promotion
     * @param offering
     * @param itemQuantity
     * @param unitPrice
     * @param assertionErrorList
     * @param periodDays
     * @param proratedDays
     * @return discountPerLineItem
     */
    private static BigDecimal getUnitDiscountPerLineItemWithPromotion(final JPromotion promotion,
        final Offerings offering, final int itemQuantity, final BigDecimal unitPrice,
        final PriceQuotes priceQuotesResponse, final List<AssertionError> assertionErrorList) {

        BigDecimal discountForLineItem = new BigDecimal(0);
        // calculate bundle promotion discount based on least common denominator
        if (promotion.getData().getIsBundledPromo()) {
            LOGGER.info("Bundled promo is not applied to Add Seats");
            AssertCollector.assertTrue(
                "Incorrect error for - Bundled promotion discount is not applied for SubscriptionPurchase",
                HelperForPriceQuote.parsePqErrors(priceQuotesResponse,
                    PelicanErrorConstants.ADD_SEATS_BUNDLED_PROMO_ERROR_MESSAGE),
                assertionErrorList);
        } else {
            // if not bundle promotion, calculate promotion discount
            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                discountForLineItem = new BigDecimal(promotion.getData().getDiscountAmount());
            }

            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {

                discountForLineItem =
                    unitPrice.multiply(BigDecimal.valueOf(promotion.getData().getDiscountPercent() / 100));
            }
        }
        return discountForLineItem;
    }

    private static BigDecimal getUnitDiscountPerLineItemWithCreditDaysAndPromotion(final Offerings offerings,
        final JLineItem item, final BigDecimal unitPrice, final PriceQuotes priceQuotesResponse,
        final JPromotion promotion, final List<AssertionError> assertionErrorList) {

        final String nextBillingDate = item.getTargetRenewalDate();
        BigDecimal unitDiscountForLineItem = new BigDecimal(0);
        // Get days in next billing cycle, it could be 28, 29, 30 or 31 depending on the month.
        if (item.getPromotionId() == null) {
            if (item.getCreditDays() != null) {
                // Get days in next billing cycle, it could be 28, 29, 30 or 31 depending on the month.
                final double daysInNextRenewalBillingCycle = DateTimeUtils.getDaysInBillingCycle(nextBillingDate,
                    DateTimeUtils.addMonthsToDate(nextBillingDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

                final double remainingDaysAfterDiscount =
                    (daysInNextRenewalBillingCycle - Double.valueOf(item.getCreditDays()));
                final BigDecimal oneDayDiscount =
                    unitPrice.divide(new BigDecimal(daysInNextRenewalBillingCycle), MathContext.DECIMAL64);
                final BigDecimal priceForRemainingDays =
                    oneDayDiscount.multiply(new BigDecimal(remainingDaysAfterDiscount), MathContext.DECIMAL64)
                        .setScale(2, RoundingMode.HALF_UP);
                unitDiscountForLineItem = unitPrice.subtract(priceForRemainingDays);

            }
        } else {
            if (item.getPromotionId() != null) {
                unitDiscountForLineItem = getUnitDiscountPerLineItemWithPromotion(promotion, offerings,
                    item.getQuantity(), unitPrice, priceQuotesResponse, assertionErrorList);
            }
        }
        return unitDiscountForLineItem;
    }

    /**
     * This method calculates bundle promotion discount If modules of item quantity and promotion offer quantity is 0,
     * then bundle promotion is applied
     *
     * @param promotion
     * @param promotionQuantity
     * @param itemQuantity
     * @param totalAmountBeforePromotionForLineItem
     * @return BigDecimal
     */
    private static BigDecimal getBundlePromotionDiscountPerLineItem(final JPromotion promotion,
        final int promotionQuantity, final int itemQuantity, final BigDecimal totalAmountBeforePromotionForLineItem) {

        // Set the promotion discount as 0
        BigDecimal promotionDiscountForLineItem = new BigDecimal(0);
        int quantityQualifiedForBundlePromotion;

        // Bundle Promotion is applied to the least common denominator of the
        // quantity
        if (itemQuantity % promotionQuantity == 0) {

            quantityQualifiedForBundlePromotion = itemQuantity / promotionQuantity * promotionQuantity;

            // if promotion type is cash amount, then apply the promotion to the
            // line item
            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_AMOUNT) {
                promotionDiscountForLineItem = BigDecimal.valueOf(promotion.getData().getDiscountAmount())
                    .multiply(BigDecimal.valueOf(quantityQualifiedForBundlePromotion));
            }

            // if promotion type is percentage, then apply the promotion to the
            // line item
            if (promotion.getData().getPromotionType() == PromotionType.DISCOUNT_PERCENTAGE) {
                promotionDiscountForLineItem = totalAmountBeforePromotionForLineItem
                    .multiply(BigDecimal.valueOf(promotion.getData().getDiscountPercent() / 100));
            }
        }
        return promotionDiscountForLineItem;
    }

    /**
     * This method gives shipping details for US address
     *
     * @param isAdditionalFeeAdded
     * @return Shipping
     */
    public static Shipping getNamerShippingDetails(final boolean isAdditionalFeeAdded) {
        return getShippingDetails(com.autodesk.bsm.pelican.enums.Country.US, StateProvince.CALIFORNIA, "San Francisco",
            "1 Market St", "Unit 101", Shipping.ShippingMethod.UPS_GROUND, isAdditionalFeeAdded);
    }

    /**
     * This method gives shipping details for EMEA address
     *
     * @param isAdditionalFeeAdded
     * @return Shipping
     */
    public static Shipping getEmeaShippingDetails(final boolean isAdditionalFeeAdded) {
        return getShippingDetails(com.autodesk.bsm.pelican.enums.Country.FR, null, "Paris", "Quai Panhard et Levassor",
            "Unit 89", Shipping.ShippingMethod.UPS_GROUND_EMEA, isAdditionalFeeAdded);
    }

    /**
     * This method prepares shipping line item and request
     *
     * @param country
     * @param state
     * @param city
     * @param streetAddressLine1
     * @param streetAddressLine2
     * @param shippingMethod
     * @param isAdditionalFeeAdded
     * @return Shipping
     */
    private static Shipping getShippingDetails(final com.autodesk.bsm.pelican.enums.Country country,
        final StateProvince state, final String city, final String streetAddressLine1, final String streetAddressLine2,
        final Shipping.ShippingMethod shippingMethod, final boolean isAdditionalFeeAdded) {
        final Shipping shipping = new Shipping();
        shipping.setShippingMethod(shippingMethod);
        if (isAdditionalFeeAdded) {
            final List<JAdditionalFee> shippingAdditionalFeesList = new ArrayList<>();
            shippingAdditionalFeesList
                .add(new JAdditionalFee(JAdditionalFee.Category.TAX, "3.0", "false", JAdditionalFee.TaxPayer.BUYER));
            shipping.setAdditionalFees(shippingAdditionalFeesList);
        }
        final Totals total = new Totals();
        if (shippingMethod == Shipping.ShippingMethod.UPS_GROUND) {
            total.setShipping("12.61");
        } else {
            total.setShipping("10.00");
        }
        shipping.setTotals(total);
        final ShipTo shipTo = new ShipTo();
        shipTo.setStreetAddressLine1(streetAddressLine1);
        shipTo.setStreetAddressLine2(streetAddressLine2);
        shipTo.setCity(city);
        shipTo.setState(state);
        shipTo.setCountry(country);
        shipping.setShipTo(shipTo);
        return shipping;
    }

    /**
     * This method gives additional fees
     *
     * @return List<JAdditionalFee>
     */
    public static List<JAdditionalFee> getAdditionalFeesDetails() {
        final List<JAdditionalFee> additionalFees = new ArrayList<>();

        additionalFees
            .add(new JAdditionalFee(JAdditionalFee.Category.TAX, "3", "false", JAdditionalFee.TaxPayer.BUYER));
        additionalFees
            .add(new JAdditionalFee(JAdditionalFee.Category.TAX, "5", "false", JAdditionalFee.TaxPayer.BUYER));
        return additionalFees;
    }

    /**
     * This method prepares price quote data
     *
     * @param buyerId
     * @param isCartUpdated TODO
     * @return PriceQuoteData
     */
    public static PriceQuotes.PriceQuoteData getPriceQuoteData(final String buyerId, final boolean isCartUpdated) {
        final PriceQuotes.PriceQuoteData data = new PriceQuotes.PriceQuoteData();
        data.setType(EntityType.CART);
        data.setBuyerId(buyerId);
        data.setCartUpdated(isCartUpdated);
        return data;
    }

    /**
     * This method prepares price quote data.
     *
     * @param buyerId
     * @param isCartUpdated
     * @param isEstimateVat
     * @return
     */
    public static PriceQuotes.PriceQuoteData getPriceQuoteData(final String buyerId, final boolean isCartUpdated,
        final boolean isEstimateVat) {
        final PriceQuotes.PriceQuoteData data = new PriceQuotes.PriceQuoteData();
        data.setType(EntityType.CART);
        data.setBuyerId(buyerId);
        data.setCartUpdated(isCartUpdated);
        data.setEstimateVat(isEstimateVat);
        return data;
    }

    /**
     * This is a method to return the number of prorated days for a subscription
     *
     * @param nextBillingDate
     * @return
     */
    private static Integer getProratedDays(final String nextBillingDate) {
        final DateTime today = new DateTime(DateTimeZone.UTC);
        final String nextBillingDateFormatted = nextBillingDate.split(" UTC")[0];
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(PelicanConstants.DB_DATE_FORMAT);
        final DateTime nextRenewalDate = formatter.parseDateTime(nextBillingDateFormatted);
        final Integer proratedDays =
            Days.daysBetween(today.withTimeAtStartOfDay(), nextRenewalDate.withTimeAtStartOfDay()).getDays();
        LOGGER.info("Prorated days for a subscription: " + proratedDays);
        return proratedDays;

    }

    /**
     * This is a method to return the number of prorated days for a subscription between two dates
     *
     * @param start date
     * @param end date
     * @return
     */
    private static Integer getProratedDaysBetweenTwoDates(final String startDate, final String endDate) {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final DateTime startDay = formatter.parseDateTime(startDate);
        final DateTime endDay = formatter.parseDateTime(endDate);
        final Integer proratedDays =
            Days.daysBetween(startDay.withTimeAtStartOfDay(), endDay.withTimeAtStartOfDay()).getDays();
        LOGGER.info("Prorated days for a subscription: " + proratedDays);
        return proratedDays;

    }

    /**
     * This is a method which will return the unit price amount of the subscription for the specified prorated days
     * accounting quantity into account
     *
     * @param proratedDays
     * @param price
     * @param periodDays
     * @param quantity
     * @return unit price in BigDecimal
     */
    private static BigDecimal getUnitPriceAmount(final Integer proratedDays, final BigDecimal price,
        final int periodDays, final int quantity) {

        final BigDecimal dailyPrice = getDailyPrice(price, periodDays, quantity);

        return dailyPrice.multiply(new BigDecimal(proratedDays));
    }

    /**
     * This is a method which will return the unit price amount of the subscription for the specified prorated days
     *
     * @param proratedDays
     * @param price
     * @param periodDays
     * @return
     */
    private static BigDecimal getUnitPriceAmount(final Integer proratedDays, final BigDecimal price,
        final int periodDays) {

        final BigDecimal dailyPrice = getDailyPrice(price, periodDays);
        return dailyPrice.multiply(new BigDecimal(proratedDays));
    }

    /**
     * This is a method which will return the unit price amount of the subscription before promotions accounting
     * quantity into account
     *
     * @param price
     * @param quantity
     * @return
     */
    private static BigDecimal getUnitPriceBeforePromotions(final BigDecimal price, final int quantity) {

        final BigDecimal priceBeforePromotions = price.divide(new BigDecimal(quantity));
        return priceBeforePromotions.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * This is a method to calculate the daily price of the subscription
     *
     * @param price
     * @param periodDays
     * @return BigDecimal
     */
    private static BigDecimal getDailyPrice(BigDecimal price, final int periodDays, final int quantity) {

        price = price.divide(new BigDecimal(quantity), 10, BigDecimal.ROUND_HALF_EVEN);
        return price.divide(new BigDecimal(periodDays), 10, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * This is a method to calculate the daily price of the subscription
     *
     * @param price
     * @param periodDays
     * @return BigDecimal
     */
    private static BigDecimal getDailyPrice(final BigDecimal price, final int periodDays) {

        return price.divide(new BigDecimal(periodDays), 10, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * Method which parse pricequote response for errors and validate the expeced error
     *
     * @param priceQuotesResponse
     * @param error string
     * @return Boolean, true/false based on search result
     */
    public static boolean parsePqErrors(final PriceQuotes priceQuotesResponse, final String error) {

        if (priceQuotesResponse != null) {
            if (priceQuotesResponse.getErrors() != null) {
                for (int i = 0; i < priceQuotesResponse.getErrors().size(); i++) {
                    if (priceQuotesResponse.getErrors().get(i).getDetail().contentEquals(error)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This is a method to construct and fire the price quotes request and return the response
     *
     * @param priceQuoteRequestBuilder
     * @param assertionErrorList
     * @param resource TODO
     */
    public static PriceQuotes createPriceQuoteRequestDataForOfferingPurchaseType(
        final PriceQuoteRequestBuilder priceQuoteRequestBuilder, final List<AssertionError> assertionErrorList,
        final PelicanPlatform resource) {

        final PriceQuotes priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(priceQuoteRequestBuilder.getBuyerId(), true);
        requestData.setLineItems(priceQuoteRequestBuilder.getLineItems());

        // Adding shipping details
        requestData.setShipping(priceQuoteRequestBuilder.getShipping());

        requestData.setPromotionReferences(priceQuoteRequestBuilder.getPromotionReferencesList());
        priceQuotes.setData(requestData);

        final PriceQuotes priceQuotesResponse =
            priceQuoteRequestBuilder.getResource().priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(priceQuoteRequestBuilder.getOfferingsJPromotionMap(),
            priceQuotesResponse, requestData, 0.00, priceQuoteRequestBuilder.getPurchaseType().getName(),
            priceQuoteRequestBuilder.getSubscriptionList(), priceQuoteRequestBuilder.getEnvironmentVariables(), false,
            assertionErrorList);

        return priceQuotesResponse;
    }

    /**
     * This is a method to create a subscription for a price id and return the created subscriptions
     *
     * @param purchaseOrderUtils
     * @param priceId
     * @param buyerUser
     * @param quantity
     * @param resource
     * @return Subscription
     */
    public static Subscription getSubscriptionIdForSubscriptionManagement(final PurchaseOrderUtils purchaseOrderUtils,
        final String priceId, final BuyerUser buyerUser, final int quantity, final PelicanPlatform resource) {

        final String purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);
        final String subscriptionIdBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        return resource.subscription().getById(subscriptionIdBicCreditCard);
    }
}
