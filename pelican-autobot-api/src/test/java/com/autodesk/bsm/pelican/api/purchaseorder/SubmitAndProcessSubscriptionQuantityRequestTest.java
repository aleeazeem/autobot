package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is inherited from Selenium Webdriver, because to change Add Seats Feature Flag in Admin tool
 */
public class SubmitAndProcessSubscriptionQuantityRequestTest extends SeleniumWebdriver {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubmitAndProcessSubscriptionQuantityRequestTest.class.getSimpleName());
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String priceIdForBic1;
    private String priceIdForBic2;
    private String priceIdForBic3;
    private JPromotion storeWidePercentDiscountPromo;
    private JPromotion nonStoreWideAmountDiscountPromo;
    private JPromotion storeWidePercentDiscountBundledPromo;
    private JPromotion supplementTimePromo;
    private Map<String, JPromotion> promotionsMap;
    private HashMap<String, Integer> priceQuantityMap;
    private Map<String, String> priceOfferingAmountMap;
    private Map<String, String> subscriptionQuantityMap;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private FindSubscriptionsPage findSubscriptionsPage;
    private BuyerUser buyerUser;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        promotionsMap = new HashMap<>();

        // Creating 3 offerings for BIC and Meta.
        final Offerings bicSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicSubscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicSubscriptionOffering3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Creating price id for BIC and Meta.
        priceIdForBic1 = bicSubscriptionOffering1.getIncluded().getPrices().get(0).getId();
        priceIdForBic2 = bicSubscriptionOffering2.getIncluded().getPrices().get(0).getId();
        priceIdForBic3 = bicSubscriptionOffering3.getIncluded().getPrices().get(0).getId();

        // Initialize maps and Added to PriceId/Amount Map
        priceOfferingAmountMap = new HashMap<>();
        priceOfferingAmountMap.put(priceIdForBic1,
            bicSubscriptionOffering1.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBic2,
            bicSubscriptionOffering2.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBic3,
            bicSubscriptionOffering3.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(getMetaMonthlyUsPriceId(),
            getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getAmount());

        // Creating Regular Promotions for BIC subscriptions Store wide % Discount
        storeWidePercentDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicSubscriptionOffering2), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                "10", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        LOGGER.info("storeWidePercentDiscountPromo id :" + storeWidePercentDiscountPromo.getData().getId());
        // Add to Map
        promotionsMap.put(storeWidePercentDiscountPromo.getData().getId(), storeWidePercentDiscountPromo);

        // Creating Regular Promotions for BIC subscriptions non Store wide Amount Discount
        nonStoreWideAmountDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicSubscriptionOffering1), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, "40", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        LOGGER.info("nonStoreWideAmountDiscountPromo id :" + nonStoreWideAmountDiscountPromo.getData().getId());
        // Add to Map
        promotionsMap.put(nonStoreWideAmountDiscountPromo.getData().getId(), nonStoreWideAmountDiscountPromo);

        // Creating Bundled Promotion for BIC subscriptions Store wide % Discount.
        final List<BundlePromoOfferings> offeringsForstoreWidePercentDiscountPromo = new ArrayList<>();
        offeringsForstoreWidePercentDiscountPromo
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering3, 3, true));

        storeWidePercentDiscountBundledPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForstoreWidePercentDiscountPromo, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER
            .info("storeWidePercentDiscountBundledPromo id: " + storeWidePercentDiscountBundledPromo.getData().getId());

        // Add to Map
        promotionsMap.put(storeWidePercentDiscountBundledPromo.getData().getId(), storeWidePercentDiscountBundledPromo);

        // Creating Supplement Promotions for BIC subscriptions Store wide % Discount
        supplementTimePromo =
            promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicSubscriptionOffering2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, null, DateTimeUtils.getUTCFutureExpirationDate(), "2", "MONTH", null, null, null);
        LOGGER.info("supplementTimePromo id :" + supplementTimePromo.getData().getId());
        // Add to Map
        promotionsMap.put(supplementTimePromo.getData().getId(), supplementTimePromo);

        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Verify Submit Purchase Order for add seats creates PO successfully for Active Subscription Id.
     */
    @Test
    public void testSubmitPurchaseOrderAddsSeatsSuccessfully() {

        // submit a purchase order to create a commercial subscription
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic1, 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription and add it to list
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        // get subscription.
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForBicCommercial);

        AssertCollector.assertThat("Incorrect Order State of the Purchase Order",
            purchaseOrderForAddedSeats.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Price Id for the line item", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityRequest().getPriceId(),
            equalTo(priceIdForBic1), assertionErrorList);
        AssertCollector.assertTrue("Incorrect Proration Start Date",
            DateTimeUtils.isSameDate(
                purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                    .getSubscriptionQuantityRequest().getProrationStartDate(),
                DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                PelicanConstants.DATE_FORMAT_WITH_SLASH),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Proration End Date",
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getProrationEndDate().split("\\s+")[0],
            equalTo(subscriptionDetailPage.getNextBillingDate()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Store External Key", purchaseOrderForAddedSeats.getStoreExternalKey(),
            equalTo(purchaseOrder.getStoreExternalKey()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Submit Purchase Order for add seats throws an error for Expired Subscription.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsAnErrorForExpiredSubscription() {
        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic1, 2);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // cancel the subscription
        resource.subscription().cancelSubscription(subscriptionIdForBicCommercial,
            CancellationPolicy.IMMEDIATE_NO_REFUND);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForBicCommercial);
        // Verify Subscription is Expired.
        AssertCollector.assertThat("Incorrect Subscription status after expiring ", subscriptionDetailPage.getStatus(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        // Create Line Item for Subscription Quantity.
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // submit Purchase Order for Add Seats using Expired Subscription.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Error Message  ", httpError.getErrorMessage(), equalTo(String
                .format(PelicanErrorConstants.SUBSCRIPTION_INACTIVE_ERROR_MESSAGE, subscriptionIdForBicCommercial)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Submit PO throws an error when price id belongs to different offer and offering.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsAnErrorForDifferentPriceId() {
        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic1, 2);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_PRICE_ID_NOT_MATCHING_ERROR_MESSAGE,
                priceIdForBic2, subscriptionIdForBicCommercial)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Submit PO throws an error when quantity is negative.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsAnErrorOnNegativeQuantity() {
        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic1, 2);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "-2");

        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_ZERO_OR_NEGATIVE_QUANTITY_ERROR_MESSAGE,
                purchaseOrder.getTransactions().getTransactions().get(0).getGatewayResponse().getAmountCharged())),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Submit PO throws an error when price is expired. Step1 : Create New Acquisition order Step2 : Expire the
     * price Step3 : Submit Add Seats order Step4 : Verify error is thrown
     */
    @Test
    public void testErrorForSubmitSubscriptionQuantityPurchaseOrderWithExpiredPrice() {

        // Create an offering, so that the price can be expired
        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Creating price id for BIC and Meta.
        final String priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic, 4);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceIdForBic), getEnvironmentVariables());

        // submit Purchase Order for Add Seats using different PriceId from Subscription.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.PRICE_ID_NOT_ACTIVE_ERROR, priceIdForBic)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Process PO is success when price is expired. Step1 : Create New Acquisition order Step2 : Submit PO in
     * AUTH Step3 : Update the price to expire Step4 : Process PO to PENDING Step5 : Process PO is success
     */
    @Test
    public void testSuccessProcessSubscriptionQuantityPurchaseOrderWithExpiredPrice() {

        // Create an offering, so that the price can be expired
        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Creating price id for BIC and Meta.
        final String priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic, 2);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        PurchaseOrder addSeatsPurchaseOrder = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceIdForBic), getEnvironmentVariables());

        // submit Purchase Order for Add Seats using different PriceId from Subscription.
        addSeatsPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrder.getId());

        AssertCollector.assertThat("Incorrect response", addSeatsPurchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Submit PO throws an error when quantity is "0".
     */
    @Test
    public void testErrorForSubscriptionQuantityPurchaseOrderWithZeroQuantity() {
        // submit a purchase order for BIC subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic1, 2);

        // get Purchase Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get Subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "0");

        // submit Purchase Order for Add Seats using different PriceId from
        // Subscription.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_ZERO_OR_NEGATIVE_QUANTITY_ERROR_MESSAGE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Verify Submit PO throws an error for non BIC subscription offering.
     */
    @Test
    public void testErrorForAddSeatsForNonBicOfferingType() {
        // submit a purchase order for Meta subscription.
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 2);

        // get Purchase Order
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // get Subscription Id
        final String subscriptionIdForMeta =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // submit Purchase Order for Add Seats For Meta Offering Type
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForMeta);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getMetaMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // submit Purchase Order for Add Seats for Meta Subscription Offering.
        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage().split(":")[1].trim(),
            equalTo(PelicanErrorConstants.NON_BIC_SUBSCRIPTION_ERROR_MESSAGE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Process Purchase Order for Pending State successfully process PO and create Fulfillment group.
     *
     */
    @Test(dataProvider = "dataForAddSeatsLineItem")
    public void testProcessPurchaseOrderWithPendingStateCreateFulfillmentGroupSuccessfully(final String priceId,
        final PaymentType paymentType, final String taxAmount, final int updateNextBillingDate) {

        // submit a purchase order to create a commercial subscription
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final com.autodesk.bsm.pelican.api.pojos.subscription.Subscription subscription =
            resource.subscription().getById(subscriptionIdForBicCommercial);
        final String nextBillingDate = subscription.getNextBillingDate();

        final Double billingCycleDays =
            DateTimeUtils.getDaysInBillingCycle(purchaseOrder.getCreationTime(), nextBillingDate);

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate =
            DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(updateNextBillingDate),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), paymentType, OrderCommand.AUTHORIZE, taxAmount, buyerUser);

        // Processing for PENDING Order State.
        if (paymentType == PaymentType.PAYPAL) {
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderForAddedSeats.getId());
        } else {
            purchaseOrderForAddedSeats =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeats.getId());
        }

        purchaseOrderForAddedSeats = resource.purchaseOrder().getById(purchaseOrderForAddedSeats.getId());
        // Assert for PENDING state ,FULFILLMENT STATUS and Subscription ID.
        AssertCollector.assertThat("Incorrect Order State of the Purchase Order",
            purchaseOrderForAddedSeats.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);

        AssertCollector.assertThat("Incorrect Fulfillment Status", purchaseOrderForAddedSeats.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);

        // get subscription.
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForBicCommercial);

        AssertCollector.assertThat("Incorrect SubscriptionId",
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getSubscriptions().getSubscription().get(0).getId(),
            equalTo(subscriptionIdForBicCommercial), assertionErrorList);

        AssertCollector.assertThat("Incorrect Store External Key", purchaseOrderForAddedSeats.getStoreExternalKey(),
            equalTo(purchaseOrder.getStoreExternalKey()), assertionErrorList);

        // Assert on Proration Start Date
        AssertCollector.assertTrue("Incorrect Proration Start Date",
            DateTimeUtils.isSameDate(
                purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                    .getSubscriptionQuantityRequest().getProrationStartDate(),
                DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                PelicanConstants.DATE_FORMAT_WITH_SLASH),
            assertionErrorList);

        // Assert on Proration End Date
        AssertCollector.assertThat("Incorrect Proration End Date",
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getProrationEndDate().split("\\s+")[0],
            equalTo(subscriptionDetailPage.getNextBillingDate()), assertionErrorList);

        // Assertions for Enriched Fields.
        HelperForPurchaseOrder.assertionForAddSeatsEnrichField(purchaseOrder, purchaseOrderForAddedSeats,
            assertionErrorList);

        // Assertion of Response Field
        HelperForPurchaseOrder.assertionForAddSeatsResponseField(purchaseOrder, purchaseOrderForAddedSeats,
            assertionErrorList);

        // Assert of Amount Charge.
        HelperForPurchaseOrder.assertionForAddSeatsLineItemAmountChargedCalculation(purchaseOrderForAddedSeats,
            priceOfferingAmountMap, null, null, subscriptionDetailPage.getNextBillingDate(), billingCycleDays,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test add seats with promotions
     *
     * @param priceId
     * @param promotionReference
     * @param paymentType
     * @param taxAmount
     */
    @Test(dataProvider = "dataForAddSeatsLineItemWithPromotion")
    public void testProcessPurchaseOrderWithPromotion(final String priceId, final String promotionReference,
        final PaymentType paymentType, final String taxAmount) {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final com.autodesk.bsm.pelican.api.pojos.subscription.Subscription subscription =
            resource.subscription().getById(subscriptionIdForBicCommercial);
        final String nextBillingDate = subscription.getNextBillingDate();

        final Double billingCycleDays =
            DateTimeUtils.getDaysInBillingCycle(purchaseOrder.getCreationTime(), nextBillingDate);

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");
        subscriptionQuantityMap.put("promotionIds", promotionReference);

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), paymentType, OrderCommand.AUTHORIZE, taxAmount, buyerUser);
        // Processing for PENDING Order State.
        if (paymentType == PaymentType.PAYPAL) {
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderForAddedSeats.getId());
        } else {
            purchaseOrderForAddedSeats =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeats.getId());
        }
        // get subscription.
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForBicCommercial);
        // Assert of Amount Charge.
        HelperForPurchaseOrder.assertionForAddSeatsLineItemAmountChargedCalculation(purchaseOrderForAddedSeats,
            priceOfferingAmountMap, null, promotionsMap, subscriptionDetailPage.getNextBillingDate(), billingCycleDays,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Add Seats throws an Error on using Bundled Promotion.
     *
     */
    @Test
    public void testErrorOnBundledPromoUseForAddSeatsOrder() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic3, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic3);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(),
            storeWidePercentDiscountBundledPromo.getData().getId());

        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        AssertCollector.assertThat("Incorrect Error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage().split(": ")[1].trim(),
            equalTo(PelicanErrorConstants.ADD_SEATS_BUNDLED_PROMO_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify Process Purchase Order for Add Seats throws an error for Supplement Promotion.
     *
     */
    @Test
    public void testErrorOnInSupplementPromoForAddSeats() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(),
            supplementTimePromo.getData().getId());

        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        AssertCollector.assertThat("Incorrect Error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage().split(": ")[1].trim(),
            equalTo(PelicanErrorConstants.ADD_SEATS_SUPPLEMENT_PROMOTION_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test Error Scenario for Add Seats with Next Billing Date in Past.
     */
    @Test
    public void testErrorForAddSeatsWithSubscriptionNextBillingDateInPast() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(-15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "1");

        final HttpError httpError = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        AssertCollector.assertThat("Incorrect Error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_NEXT_BILLING_DATE_IN_PAST), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies Seats are added to the same subscription when subscription renewal is not started Step1 :
     * Create new acquisition purchase order Step2 : Create Add Seats PO in AUTH, PENDING, CHARGE Step3 : Verify seats
     * are added to the same subscription and proration days are 0, amount charged is $0
     */
    @Test
    public void testAddSeatsWhenAddSeatsIsFulfilledBeforeRenewalOnNBD() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        // Submit New Acquisition Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // get one date to change Subscriptions next billing date.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getCurrentDate(),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        // Submit Add Seats Order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Process the add seats order
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Added seats subscription id is different than original subscription id",
            addSeatsSubscriptionId, equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Amount charged should be $0.00",
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged(),
            is((float) 0.0), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect prorated days", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getProrationDays(),
            is(0), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", subscriptionDetailPage.getQuantity(), is(4),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies new subscription is created if Add Seats fulfillment happens if Renewal order is Pending
     * or charged With Order Command "PENDING" Step1 : Create new acquisition purchase order Step2 : Create Add Seats PO
     * in AUTH Step3 : Create renewal order with AUTH and then PENDING Step4 : Process Add Seats PO to Pending Step5 :
     * Verify new subscription is created and proration days should be 1
     * <p>
     * With Order Command "CHARGED" Step1 : Create new acquisition purchase order Step2 : Create Add Seats PO in AUTH
     * Step3 : Create renewal order with AUTH and then PENDING, CHARGE Step4 : Process Add Seats PO to Pending Step5 :
     * Verify new subscription is created and proration days should be 1
     */
    @Test(dataProvider = "getRenewalStatus")
    public void testNewSubscriptionCreationWhenAddSeatsIsFulfilledWhenRenewalAuthPendingOrCharged(
        final OrderCommand orderCommand) {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        // Submit New Acquisition Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // get one date to change Subscriptions next billing date.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(1),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        // Submit Add Seats Order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Submit Renewal Order
        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            Lists.newArrayList(subscriptionId), false, PaymentType.CREDIT_CARD, null, true, buyerUser);
        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        // Processing renewal for PENDING Order State.
        if (orderCommand == OrderCommand.PENDING) {
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);
        }

        // Process the renewal order to charge, depending on data provider
        if (orderCommand == OrderCommand.CHARGE) {
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPurchaseOrderId);
        }

        // Process Add Seats Order
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);

        AssertCollector.assertThat("New subscription is not created", addSeatsSubscriptionId, not(subscriptionId),
            assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect prorated days", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getProrationDays(),
            is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", subscriptionDetailPage.getQuantity(), is(3),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "getRenewalStatus")
    private Object[][] getRenewalStatus() {
        return new Object[][] { { OrderCommand.AUTHORIZE }, { OrderCommand.PENDING }, { OrderCommand.CHARGE } };
    }

    /**
     * This test case verifies Seats are added to the same subscription when subscription is DELINQUENT. (In Pending)
     * Step1 : Create new acquisition purchase order Step2 : Create Add Seats PO in AUTH Step3 : Create renewal order
     * with AUTH and then PENDING then DECLINE, so the subscription is in DELINQUENT status Step4 : Process Add Seats PO
     * to Pending Step5 : Verify seats are added to the same subscription and proration days are till next billing date
     */
    @Test
    public void testAddSeatsWhenAddSeatsIsFulfilledWhenRenewalDeclined() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // get one date to change Subscriptions next billing date.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(1),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        // Submit Add Seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Submit Renewal Order
        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            Lists.newArrayList(subscriptionId), false, PaymentType.CREDIT_CARD, null, true, buyerUser);
        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        // Processing Renewal order to PENDING State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);

        // Processing Renewal order to DECLINE State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderId);

        // Process Add Seats order to PENDING state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Added seats subscription id is different than original subscription id",
            addSeatsSubscriptionId, equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect prorated days", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getProrationDays(),
            is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", subscriptionDetailPage.getQuantity(), is(4),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies Seats are added to the same subscription when subscription is canceled. (In Pending)
     * Step1 : Create new acquisition purchase order Step2 : Create Add Seats PO in AUTH Step3 : Cancel the subscription
     * Step4 : Process Add Seats PO to Pending Step5 : Verify seats are added to the same subscription and proration
     * days are till next billing date
     */
    @Test
    public void testAddSeatsWhenAddSeatsIsFulfilledAndSubscriptionIsCanceled() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        // Submit New Acquisition Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // get one date to change Subscriptions next billing date.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        // Submit Add seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        // Cancel the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // Process the PO
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Added seats subscription id is different than original subscription id",
            addSeatsSubscriptionId, equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect prorated days", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getProrationDays(),
            is(15), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", subscriptionDetailPage.getQuantity(), is(4),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies Error is thrown in Process Pending PO, when subscription is EXPIRED. Step1 : Create new
     * acquisition purchase order Step2 : Create Add Seats PO in AUTH Step3 : Expire the subscription Step4 : Process
     * Add Seats PO to Pending Step5 : Verify error is thrown that, subscription could not be found.
     */
    @Test
    public void testErrorInAddSeatsFulfillmentWhenSubscriptionIsExpired() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);

        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Expire the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        // Process the PO
        final HttpError httpError =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);

        AssertCollector.assertThat("Incorrect error for Add Seats fulfillment of Expired Subscription",
            httpError.getErrorMessage(), equalTo(PelicanErrorConstants.SUBSCRIPTION_NOT_FOUND_ERROR + subscriptionId),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testProcessPurchaseOrderWithPendingStateCreateFulfillmentGroupSuccessfully. It sends
     * combination of Credit Card and Paypal with different Tax.
     *
     * @return Object[][]
     */
    @SuppressWarnings("unused")
    @DataProvider(name = "dataForAddSeatsLineItem")
    private Object[][] getTestDataForAddSeatsLineItem() {
        return new Object[][] { { priceIdForBic1, PaymentType.CREDIT_CARD, "10", 15 },
                { priceIdForBic2, PaymentType.PAYPAL, "0", 0 } };
    }

    /**
     * Data provider for testProcessPurchaseOrderWithPromotion. It sends combination of Promotions to add Seats. String
     * priceIdForInitialOrder, String promotionReference, String paymentType, String taxAmount, String billingFrequency
     *
     * @return Object[][]
     */
    @SuppressWarnings("unused")
    @DataProvider(name = "dataForAddSeatsLineItemWithPromotion")
    private Object[][] getPromotionDataForAddSeats() {
        return new Object[][] {
                { priceIdForBic2, storeWidePercentDiscountPromo.getData().getId(), PaymentType.CREDIT_CARD, "10" },
                { priceIdForBic1, nonStoreWideAmountDiscountPromo.getData().getId(), PaymentType.PAYPAL, "0" }, };
    }

    /**
     * This method tests new subscription is created if Add Seats fulfillment happens after Extension order charged
     * Step1: Submit New Acquisition order with Charge Step2: Submit Add Seats order with AUTH Step3: Submit Extension
     * order and process to CHARGE Step4: Fulfill Add Seats order Step5: Verify new subscription is created
     *
     * @param orderCommand
     */
    @Test(dataProvider = "getRenewalStatus")
    public void testNewSubscriptionIsCreatedIfAddSeatsOrderFulfilledWhenExtensionOrderIsPendingOrCharged(
        final OrderCommand orderCommand) {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, 1);
        // Submit New Acquisition Order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic2);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(ImmutableList.of(priceIdForBic2)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        final PurchaseOrder purchaseOrderForExtension =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        final String extensionPurchaseOrderId = purchaseOrderForExtension.getId();

        // Processing extension for PENDING Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, extensionPurchaseOrderId);

        // Process the extension order to charge, depending on data provider
        if (orderCommand == OrderCommand.CHARGE) {
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, extensionPurchaseOrderId);
        }

        // Process Add Seats Order
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);

        AssertCollector.assertThat("New subscription should have been created", addSeatsSubscriptionId,
            not(subscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect prorated days", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getProrationDays(),
            is(365), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", subscriptionDetailPage.getQuantity(), is(3),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
