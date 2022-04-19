package com.autodesk.bsm.pelican.api.pricequote;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.helpers.HelperForPriceQuote;
import com.autodesk.bsm.pelican.api.pojos.json.JAdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.json.JLineItem;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes.PriceQuoteData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.PurchaseType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class tests the price quote for subscription renewal including promotion, vat tax.
 *
 * @author Muhammad
 *
 */
public class PriceQuotesForSubscriptionRenewalTest extends BaseTestData {

    private PelicanPlatform resource;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PriceQuotes priceQuotes;
    private List<JLineItem> lineItems;
    private Map<Offerings, List<JPromotion>> offeringsJPromotionMap;
    private static Offerings bicOffering1;
    private static Offerings bicOffering2;
    private static String priceId1;
    private static String priceId2;
    private static JPromotion activePromotion;
    private static Subscription subscription1;
    private static String subscriptionId1;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(
            getStoreUk().getIncluded().getPriceLists().get(0).getExternalKey(), OfferingType.BIC_SUBSCRIPTION,
            BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(
            getStoreUk().getIncluded().getPriceLists().get(0).getExternalKey(), OfferingType.BIC_SUBSCRIPTION,
            BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();
        priceId2 = bicOffering2.getIncluded().getPrices().get(0).getId();

        activePromotion = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUk()),
            Lists.newArrayList(bicOffering1), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, "10.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final String purchaseOrder1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder1);
        final PurchaseOrder getPurchaseOrder1 = resource.purchaseOrder().getById(purchaseOrder1);
        subscriptionId1 = getPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
        subscription1 = resource.subscription().getById(subscriptionId1);
    }

    /**
     * This method tests price quote response with two line items with following scenarios. i) get price quote of two
     * line items, one with promotion and other one with credit days. ii) get price quote for line item which have both
     * discounts (promotion and credit days).Promotion take precedence over credit days. iii) get price quote for line
     * item with vat. iv) get price quote with tax. v) get price quote with both tax and vat. Vat take precedence over
     * tax.
     *
     * @param isEstimateVat
     * @param applyAdditionalTax
     * @param creditDays
     */
    @Test(dataProvider = "getDataForPriceQuote")
    public void getPriceQuoteForSubscriptionRenewalWithTwoLineItemsForCreditDaysAndPromotion(
        final boolean isEstimateVat, final boolean applyAdditionalTax, final String creditDays) {

        final String purchaseOrder2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder2);
        final PurchaseOrder getPurchaseOrder2 = resource.purchaseOrder().getById(purchaseOrder2);
        final String subscriptionId2 = getPurchaseOrder2.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        final Subscription subscription2 = resource.subscription().getById(subscriptionId2);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        List<JAdditionalFee> additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), false, isEstimateVat);

        final String targetRenewalDate = DateTimeUtils.getNowPlusDays(30);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(priceId1, 1, additionalFees, PurchaseType.SUBSCRIPTIONRENEWAL, subscriptionId1,
            targetRenewalDate, activePromotion.getData().getId(), creditDays));
        lineItems.add(new JLineItem(priceId2, 2, additionalFees, PurchaseType.SUBSCRIPTIONRENEWAL, subscriptionId2,
            targetRenewalDate, null, "10"));

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);
        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        final String sqlQuery =
            "select vat_percent from store where external_key = '" + getStoreUk().getExternalKey() + "'";
        double vatPercent = 0.00;
        if (requestData.isEstimateVat()) {
            vatPercent =
                (Double.valueOf(DbUtils.selectQuery(sqlQuery, "vat_percent", getEnvironmentVariables()).get(0))) * 100;
        }

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering1, new ArrayList<>(ImmutableList.of(activePromotion)));
        offeringsJPromotionMap.put(bicOffering2, null);

        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercent, PurchaseType.SUBSCRIPTIONRENEWAL.getName(), subscriptionList, getEnvironmentVariables(), false,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests if expired price id is given in request and active price id doesn't exist then expired price id
     * will be applied.
     *
     * @param isEstimateVat
     * @param applyAdditionalTax
     * @param creditDays
     */
    @Test(dataProvider = "getDataForPriceQuote")
    public void getPriceQuoteForSubscriptionRenewalWithExpiredPrice(final boolean isEstimateVat,
        final boolean applyAdditionalTax, final String creditDays) {

        final Offerings bicOfferings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, resource,
            RandomStringUtils.randomAlphabetic(10), null);
        final SubscriptionOffer monthlyOffer = subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(
            RandomStringUtils.randomAlphabetic(10), BillingFrequency.MONTH, 1, Status.ACTIVE);
        final String offerId = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, monthlyOffer, bicOfferings.getOffering().getId()).getData().getId();

        // adding active price id which will be expired later.
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(100, getPricelistExternalKeyUk(),
                DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2)),
            bicOfferings.getOffering().getId(), offerId);

        final Offerings offering = resource.offerings().getOfferingById(bicOfferings.getOffering().getId(),
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
        final String expiredPriceId = offering.getIncluded().getPrices().get(0).getId();

        final String purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, expiredPriceId, getBuyerUser(), 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder);

        // query to expire the priceId.
        final String updateSqlQuery =
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE_WHERE_PRICE_ID,
                DateTimeUtils.getPreviousUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT, 3), expiredPriceId);
        DbUtils.updateQuery(updateSqlQuery, getEnvironmentVariables());

        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(purchaseOrder);
        final String subscriptionId = getPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionId);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        List<JAdditionalFee> additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), false, isEstimateVat);
        final String targetRenewalDate = DateTimeUtils.getNowPlusDays(30);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(expiredPriceId, 1, additionalFees, PurchaseType.SUBSCRIPTIONRENEWAL, subscriptionId,
            targetRenewalDate, null, creditDays));

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);
        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        final String sqlQuery =
            "select vat_percent from store where external_key = '" + getStoreUs().getExternalKey() + "'";
        double vatPercent = 0.00;
        if (requestData.isEstimateVat()) {
            vatPercent =
                (Double.valueOf(DbUtils.selectQuery(sqlQuery, "vat_percent", getEnvironmentVariables()).get(0))) * 100;
        }

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(offering, null);

        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercent, PurchaseType.SUBSCRIPTIONRENEWAL.getName(), subscriptionList, getEnvironmentVariables(), false,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests all mandatory fields of price quotes for subscription renewal.
     *
     * @param priceId
     * @param isSubscriptionId
     * @param purchaseType
     * @param quantity
     * @param targetRenewalDate
     */
    @Test(dataProvider = "getMissingDataForErrorMessages")
    public void testErrorsForPriceQuoteApiForSubscriptionRenewals(final String priceId, final boolean isSubscriptionId,
        final PurchaseType purchaseType, final int quantity, final String targetRenewalDate) {

        String subscriptionId;
        if (!isSubscriptionId) {
            subscriptionId = null;
        } else {
            subscriptionId = subscriptionId1;
        }

        priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), false, false);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems
            .add(new JLineItem(priceId, quantity, null, purchaseType, subscriptionId, targetRenewalDate, null, null));

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);
        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        if (!isSubscriptionId) {
            AssertCollector.assertThat("Error message is not correct for missing subscription id.",
                priceQuotesResponse.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID), assertionErrorList);
        }

        if (priceId.equals("00")) {
            AssertCollector.assertThat("Error message is not correct for invalid price id.",
                priceQuotesResponse.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_NON_ACTIVE_PRICE_ID), assertionErrorList);
        }

        if (quantity < 1) {
            AssertCollector.assertThat("Error message is not correct for quantity less than 0.",
                priceQuotesResponse.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_QUANTITY_ZERO), assertionErrorList);
        }

        if (targetRenewalDate == null) {
            AssertCollector.assertThat("Error message is not correct for missing target renewal date.",
                priceQuotesResponse.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_TARGET_RENEWAL_DATE), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests if expired id is given then at the time of renewal active expired price id will be applied if
     * it exists in same offer. Other wise expired price id will be applied.
     */
    @Test
    public void getPriceQuoteForTwoLineItemsWithExpiredPriceIdInThePresenceOfActivePriceId() {
        final int expiredAmount = 100;
        final int activeAmount = 200;
        final Offerings bicOfferings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, resource,
            RandomStringUtils.randomAlphabetic(10), null);
        final SubscriptionOffer monthlyOffer = subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(
            RandomStringUtils.randomAlphabetic(10), BillingFrequency.MONTH, 1, Status.ACTIVE);
        final String offerId = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, monthlyOffer, bicOfferings.getOffering().getId()).getData().getId();

        // adding active price which will be expired after submitting a purchase order.
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(expiredAmount,
                getPricelistExternalKeyUs(), DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1)),
            bicOfferings.getOffering().getId(), offerId);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(activeAmount,
                getPricelistExternalKeyUs(), DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 8),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 60)),
            bicOfferings.getOffering().getId(), offerId);

        final Offerings offering1 = resource.offerings().getOfferingById(bicOfferings.getOffering().getId(),
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
        final String priceId1 = offering1.getIncluded().getPrices().get(0).getId();

        final String purchaseOrder1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder1);

        final PurchaseOrder getPurchaseOrder1 = resource.purchaseOrder().getById(purchaseOrder1);
        final String subscriptionId1 = getPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionId1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), false, false);
        final String targetRenewalDate = DateTimeUtils.getNowPlusDays(30);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(priceId1, 1, null, PurchaseType.SUBSCRIPTIONRENEWAL, subscriptionId1,
            targetRenewalDate, null, null));

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);
        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(offering1, null);

        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.SUBSCRIPTIONRENEWAL.getName(), subscriptionList, getEnvironmentVariables(), false,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests if expired id is given then at the time of renewal active expired price id will be applied if
     * it exists in same offer. Other wise expired price id will be applied.
     *
     */
    @Test
    public void getPriceQuoteForTwoLineItemsOneWithActiveAndOneWithExpiredPriceId() {
        final int expiredAmount = 100;
        final int activeAmount = 200;
        final Offerings bicOfferings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, resource,
            RandomStringUtils.randomAlphabetic(10), null);
        final SubscriptionOffer monthlyOffer = subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(
            RandomStringUtils.randomAlphabetic(10), BillingFrequency.MONTH, 1, Status.ACTIVE);
        final String offerId = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, monthlyOffer, bicOfferings.getOffering().getId()).getData().getId();

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(expiredAmount,
                getPricelistExternalKeyUs(), DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1)),
            bicOfferings.getOffering().getId(), offerId);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(activeAmount,
                getPricelistExternalKeyUs(), DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 8),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 60)),
            bicOfferings.getOffering().getId(), offerId);

        final Offerings offering1 = resource.offerings().getOfferingById(bicOfferings.getOffering().getId(),
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
        final String priceId1 = offering1.getIncluded().getPrices().get(0).getId();

        final String purchaseOrder1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder1);

        final PurchaseOrder getPurchaseOrder1 = resource.purchaseOrder().getById(purchaseOrder1);
        final String subscriptionId1 = getPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionId1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), false, false);
        final String targetRenewalDate = DateTimeUtils.getNowPlusDays(30);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(priceId1, 1, null, PurchaseType.SUBSCRIPTIONRENEWAL, subscriptionId1,
            targetRenewalDate, null, null));

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);
        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(offering1, null);

        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.SUBSCRIPTIONRENEWAL.getName(), subscriptionList, getEnvironmentVariables(), false,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    @DataProvider(name = "getMissingDataForErrorMessages")
    private Object[][] getMissingDataForErrorMessages() {
        // priceId, isSubscriptionId, subscriptionRenewal, quantity, targetRenewalDate
        return new Object[][] {
                { priceId1, false, PurchaseType.SUBSCRIPTIONRENEWAL, 1, DateTimeUtils.getNowPlusDays(30) },
                { "00", true, PurchaseType.SUBSCRIPTIONRENEWAL, 1, DateTimeUtils.getNowPlusDays(30) },
                { priceId1, true, PurchaseType.SUBSCRIPTIONRENEWAL, 0, DateTimeUtils.getNowPlusDays(30) },
                { priceId1, true, PurchaseType.SUBSCRIPTIONRENEWAL, 1, null }, };
    }

    @DataProvider(name = "getDataForPriceQuote")
    private Object[][] getDataForPriceQuote() {
        return new Object[][] { { true, true, "10" }, { false, true, null }, { false, false, null } };
    }
}
