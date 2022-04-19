package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is specifically for Submit and Process of Subscription Renewal Purchase Orders Created by Shweta Hegde
 * This class is inherited from Selenium Webdriver, because to change Consolidated Billing Feature Flag in Admin tool
 */
public class SubmitAndProcessSubscriptionRenewalTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private HttpError httpError;
    private String priceIdForBicSubscriptionOffering2;
    private String bicOfferingAmount;
    private Map<String, List<String>> subscriptionMap;
    private JPromotion activeNonStoreWideDiscountAmountPromo;
    private static final String discountAmount = "10.00";
    private static final int quantity = 10;
    private FindSubscriptionsPage findSubscriptionsPage;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubmitAndProcessSubscriptionRenewalTest.class.getSimpleName());
    private BuyerUser buyerUser;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings bicSubscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Price id for bic offering
        priceIdForBicSubscriptionOffering2 = bicSubscriptionOffering2.getIncluded().getPrices().get(0).getId();
        bicOfferingAmount = bicSubscriptionOffering2.getIncluded().getPrices().get(0).getAmount();

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        activeNonStoreWideDiscountAmountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicSubscriptionOffering2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, discountAmount, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 3, null, null);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.IDEMPOTENT_RENEWALS, true);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.IDEMPOTENT_RENEWALS, false);
        }
    }

    /**
     * Test Subscription Renewal Success with multiple line items
     */
    @Test
    public void testSuccessSubscriptionRenewalWithMultiLineItems() {

        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, quantity)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), quantity)), null, true, true, buyerUser);

        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String metaSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId, metaSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector
            .assertThat("Incorrect BIC subscription id",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalRequest().getSubscriptionId(),
                equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Meta subscription id", purchaseOrder.getLineItems().getLineItems().get(1)
                .getSubscriptionRenewal().getSubscriptionRenewalRequest().getSubscriptionId(),
            equalTo(metaSubscriptionId), assertionErrorList);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector
            .assertThat("Incorrect BIC Subscription id",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalRequest().getSubscriptionId(),
                equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Meta Subscription id", purchaseOrder.getLineItems().getLineItems().get(1)
                .getSubscriptionRenewal().getSubscriptionRenewalRequest().getSubscriptionId(),
            equalTo(metaSubscriptionId), assertionErrorList);

        // Assertions on Amount details
        AssertCollector.assertThat("Incorrect total amount for BIC Subscription",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getChargeDetails().getAmountCharged(),
            is(Float.parseFloat(bicOfferingAmount) * quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect total amount for Meta Subscription",
            purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getChargeDetails().getAmountCharged(),
            is(Float.parseFloat(getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getAmount()) * quantity),
            assertionErrorList);

        // Assertion on next billing date
        AssertCollector.assertThat("Incorrect next billing date for BIC Subscription",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getNextBillingDate().split(" ")[0],
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12)),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing date for Meta Subscription",
            purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getNextBillingDate().split(" ")[0],
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when subscription ids are repeated
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenSubscriptionIdsAreRepeated() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId, subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.USD.toString())));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.DUPLICATE_SUBSCRIPTION_IDS_ERROR + "[" + subscriptionId + "]"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when subscription ids belong to different price lists
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenSubscriptionIdsBelongToDifferentPriceLists() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.GBP.toString())));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_IDS_BELONG_TO_DIFFERENT_PRICE_LIST_ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when subscription ids are not found
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenSubscriptionIdsAreNotFound() {

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), new ArrayList<>(ImmutableList.of("64735784")));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.GBP.toString())));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, false, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTIONS_NOT_FOUND_ERROR + " [64735784]"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when subscription id is not provided
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenSubscriptionIdIsNotProvided() {

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), new ArrayList<>(ImmutableList.of("")));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, false, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_ID_REQUIRED_FOR_RENEWALS_ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when currency id and currency name do not match
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenCurrencyIdAndCurrencyNameDoNotMatch() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(),
            new ArrayList<>(ImmutableList.of(String.valueOf(Currency.GBP.getCode()))));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.CURRENCY_ID_NAME_DO_NOT_MATCH, Currency.USD.toString(),
                String.valueOf(Currency.GBP.getCode()))),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when invalid currency name and id are provided
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenInvalidCurrencyNameIdProvided() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(), new ArrayList<>(ImmutableList.of("abcde")));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.INVALID_CURRENCY_NAME_ERROR, "abcde")), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test success renewal order has subscription with next billing date in future when FF is OFF.
     */
    @Test
    public void testSuccessRenewalOrderWithSubscriptionHasNextBillingDateInFutureWhenFFIsOff() {
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 4)), null, true, false, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.IDEMPOTENT_RENEWALS, false);
        // Prepare request for Subscription Renewal Request.
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));

        // Submit Renewal PO for Subscription with NBD in future.
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
                Payment.PaymentType.CREDIT_CARD, null, false, buyerUser);

        AssertCollector.assertThat("Incorrect Order State", purchaseOrder.getOrderState(),
            is(OrderState.AUTHORIZED.toString()), assertionErrorList);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.IDEMPOTENT_RENEWALS, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when renewal order has subscription with next billing date in future.
     */
    @Test
    public void testErrorRenewalOrderWithSubscriptionHasNextBillingDateInFuture() {
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 4)), null, true, false, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request.
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));

        // Submit Renewal PO for Subscription with NBD in future.
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, false, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect error message", httpError.getErrorMessage(), equalTo(String
                .format(PelicanErrorConstants.ERROR_MESSAGE_FOR_SUBSCRIPTION_RENEWAL_DATE_IN_FUTURE, subscriptionId)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when renewal order has submitted for a subscription with renewal PO in process.
     */
    @Test
    public void testErrorOnRenewalOrderForASubscriptionWithPendingPaymentTrue() {
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request.
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));

        // Submit Renewal PO for Subscription with NBD in future.
        purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap, Payment.PaymentType.CREDIT_CARD,
            null, true, buyerUser);

        // Submit Renewal PO for Subscription with NBD in future.
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, false, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(), equalTo(String
            .format(PelicanErrorConstants.ERROR_MESSAGE_FOR_SUBSCRIPTION_RENEWAL_WITH_PENDING_PAYMENT, subscriptionId)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error when subscription is not belong to the store external key provided
     */
    @Test
    public void testErrorSubscriptionRenewalOrderWhenSubscriptionIdsBelongToDifferentStoresGivenStoreExternalKey() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.GBP.toString())));

        // Submit Subscription renewal Purchase Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, getStoreExternalKeyUs(), true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTIONS_SHOULD_BELONG_TO_SAME_STORE_ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error scenario when no currency id or currency name is provided when submitting renewal PO
     */
    @Test
    public void testErrorWhenNoCurrencyIdOrCurrencyNameProvidedInRenewalSubmitPO() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), "")));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.CURRENCY_ID_OR_NAME_REQUIRED_ERROR), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal with PO decline
     */
    @Test
    public void testSubscriptionRenewalWithMultiLineItemsProcessToDecline() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrder.getId());

        // PO status assertion
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);

        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId1);

        // Subscription related assertions
        AssertCollector.assertThat("Incorrect Subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.DELINQUENT.toString()), assertionErrorList);

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId2);

        // Subscription related assertions
        AssertCollector.assertThat("Incorrect Subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.DELINQUENT.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal with PO cancel
     */
    @Test
    public void testSubscriptionRenewalWithMultiLineItemsProcessToCancel() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CANCEL, purchaseOrder.getId());

        // PO status assertion
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.SUBMITTED.toString()), assertionErrorList);

        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId1);

        // Subscription related assertions
        AssertCollector.assertThat("Incorrect Subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId2);

        // Subscription related assertions
        AssertCollector.assertThat("Incorrect Subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal Success with multiple line items with multi billing cycle promotion
     */
    @Test
    public void testSuccessSubscriptionRenewalWithMultiLineItemsWithPromotion() {

        final LineItem.PromotionReferences promotionReferencesForPO = new LineItem.PromotionReferences();
        final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(activeNonStoreWideDiscountAmountPromo.getData().getId());
        promotionReferencesForPO.setPromotionReference(promotionReference);

        final int quantityOfPurchaseOrderWithPromotion = 2;
        // Create a purchase order with multi billing cycle promotion and get subscription id
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, quantityOfPurchaseOrderWithPromotion)),
            false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, promotionReferencesForPO)), buyerUser);

        String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String metaSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId, metaSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));
        subscriptionMap.put(LineItemParams.PROMOTION_REFERENCE.getValue(),
            new ArrayList<>(Arrays.asList(activeNonStoreWideDiscountAmountPromo.getData().getId(), null)));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector
            .assertThat("Incorrect BIC Subscription id",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalRequest().getSubscriptionId(),
                equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Meta Subscription id", purchaseOrder.getLineItems().getLineItems().get(1)
                .getSubscriptionRenewal().getSubscriptionRenewalRequest().getSubscriptionId(),
            equalTo(metaSubscriptionId), assertionErrorList);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector
            .assertThat("Incorrect BIC Subscription id",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalRequest().getSubscriptionId(),
                equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Meta Subscription id", purchaseOrder.getLineItems().getLineItems().get(1)
                .getSubscriptionRenewal().getSubscriptionRenewalRequest().getSubscriptionId(),
            equalTo(metaSubscriptionId), assertionErrorList);

        // Assertions on Amount details
        AssertCollector.assertThat("Incorrect total amount for BIC Subscription",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getChargeDetails().getAmountCharged(),
            is(Float.parseFloat(bicOfferingAmount) * quantityOfPurchaseOrderWithPromotion
                - Float.parseFloat(discountAmount) * quantityOfPurchaseOrderWithPromotion),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect total amount for Meta Subscription",
            purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getChargeDetails().getAmountCharged(),
            is(Float.parseFloat(getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getAmount()) * quantity),
            assertionErrorList);

        // Assertion on next billing date
        AssertCollector.assertThat("Incorrect next billing date for BIC Subscription",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getNextBillingDate().split(" ")[0],
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12)),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing date for Meta Subscription",
            purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse()
                .getNextBillingDate().split(" ")[0],
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that grant days are calculated correctly on subscription renewal.
     */
    @Test
    public void testSubscriptionGrantDaysWithMultipleRenewal() {
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getId(), 1);

        // submit a purchase order with monthly billing cycle
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, true, buyerUser);
        final String subscriptionIdForRenewal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Renewal subscription id: " + subscriptionIdForRenewal);

        final List<String> subscriptionIdList = new ArrayList<>();
        subscriptionIdList.add(subscriptionIdForRenewal);

        // Renew the subscription 7 times and validate grant days
        for (int i = 1; i <= 7; i++) {

            purchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIdList,
                false, Payment.PaymentType.CREDIT_CARD, null, true);
            final SubscriptionDetailPage subscriptionDetailPage =
                findSubscriptionsPage.findBySubscriptionId(subscriptionIdForRenewal);
            final Integer expectedGrantDays = (int) DateTimeUtils.getDaysInBillingCycle(
                DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                subscriptionDetailPage.getNextBillingDate());

            // refresh the page after subscription renewal
            subscriptionDetailPage.refreshPage();
            final List<SubscriptionActivity> subscriptionActivityList =
                subscriptionDetailPage.getSubscriptionActivity();
            AssertCollector.assertThat(i + " renewal PO id is not correct",
                subscriptionActivityList.get(i + 1).getPurchaseOrder(), equalTo(purchaseOrder.getId()),
                assertionErrorList);
            AssertCollector.assertThat("Grant days are not correct after " + i + " renewal",
                subscriptionActivityList.get(i + 1).getGrant(),
                equalTo(expectedGrantDays + " " + PelicanConstants.DAYS_CAMEL_CASE), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Success auto renewal when one of the subscriptions price is expired. Submit PO & process PO is still
     * successful. Step1 : Submit BIC and Meta New Acquisition order Step2 : Expire bic price Step3 : Submit renewal
     * order for BIC and Meta subscriptions Step4 : Verify submit and process PO was successful
     */
    @Test
    public void testSuccessSubscriptionRenewalWithExpiredPrice() {

        // Create an offering and expire the price
        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Price id for bic offering
        final String priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBic, 3)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 4)), null, true, true, buyerUser);

        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String metaSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceIdForBic), getEnvironmentVariables());

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId, metaSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        // Purchase Order related assertions
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal Authorize request when subscription is expired
     */
    @Test
    public void testSubscriptionRenewalAuthOnExpiredSubscription() {

        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, quantity)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(),
            new ArrayList<>(ImmutableList.of(String.valueOf(Currency.USD.getCode()))));

        // Expire the Subscription by invoking Refund PO
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId);
        AssertCollector.assertThat("Incorrect Subscription status after Refund", subscriptionDetailPage.getStatus(),
            equalTo(SubscriptionStatus.EXPIRED.toString()), assertionErrorList);

        // Submit Subscription renewal Purchase Order and try to renew the above expired subscription
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.ERROR_MESSAGE_FOR_EXPIRED_SUBSCRIPTION, bicSubscriptionId)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal Authorize request when one of the subscriptions is expired on PO containing multiple
     * line items
     */
    @Test
    public void testSubscriptionRenewalAuthOnExpiredSubscriptionWithMultipleLineItems() {

        final String bicPurchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(
            Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionOffering2, quantity)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(bicPurchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        final String metaPurchaseOrderId =
            purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
                new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), quantity)), null, true, true, buyerUser);

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(metaPurchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String metaSubscriptionId = resource.purchaseOrder().getById(metaPurchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId, metaSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString(), Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));

        // Expire the Meta Subscription by invoking Refund PO
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, metaPurchaseOrderId);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(metaSubscriptionId);
        AssertCollector.assertThat("Incorrect Subscription status after Refund", subscriptionDetailPage.getStatus(),
            equalTo(SubscriptionStatus.EXPIRED.toString()), assertionErrorList);

        // Submit Subscription renewal Purchase Order and try to renew the above expired subscription
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.ERROR_MESSAGE_FOR_EXPIRED_SUBSCRIPTION, metaSubscriptionId)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
