package com.autodesk.bsm.pelican.api.financereport;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.AddCreditDaysPage;
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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is particularly for Finance Report testing of Add Seats Purchase Orders
 *
 * @author Shweta Hegde on 6/14/17.
 */
public class FinanceReportForAddSeatsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PelicanTriggerClient triggerResource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private Offerings bicOffering;
    private String priceIdForBicOffering;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private String nonStoreWideCashDiscountPromoId;
    private String storeWidePercentagePromoId;
    private AddCreditDaysPage addCreditDaysPage;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static final Double TAX = 10.00;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        triggerResource = new PelicanTriggerClient(getEnvironmentVariables());

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Add subscription plans
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        priceIdForBicOffering = bicOffering.getIncluded().getPrices().get(0).getId();

        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        addCreditDaysPage = adminToolPage.getPage(AddCreditDaysPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        // Creating Regular Promotions for BIC subscriptions non Store wide % Discount
        final JPromotion nonStoreWideCashDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 5, null, null);

        nonStoreWideCashDiscountPromoId = nonStoreWideCashDiscountPromo.getData().getId();

        final JPromotion storeWidePercentagePromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, "5", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 1, null, null);

        storeWidePercentagePromoId = storeWidePercentagePromo.getData().getId();

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This test case is a happy path scenario With data provider "false" Step1 : Create a New Acquisition Purchase
     * Order, with no promotion Step2 : Add Seats to the PO with no promotion Step3 : Verify finance report that no new
     * subscription is created and "Added Subscription To" column is empty
     * <p>
     * With data provider "true" Step1 : Create a Renewal Purchase Order, with no promotion Step2 : Add Seats to the PO
     * with no promotion Step3 : Verify finance report that no new subscription is created and "Added Subscription
     * To"column is empty Step4 : Refund the order
     *
     * @param isAddSeatsToRenewal
     */
    @Test(dataProvider = "getBooleanValue")
    public void testFinanceReportForAddSeatsToExistingNewAcquisitionOrRenewalSubscription(
        final boolean isAddSeatsToRenewal) {

        // Submit New Acquisition PO
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, 4)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        if (isAddSeatsToRenewal) {
            // Submit Renewal Order
            purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD, null,
                new ArrayList<>(ImmutableList.of(bicSubscriptionId)), false, true, buyerUser);
        }

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), bicSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // Submit Add Seats PO
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        // Process the order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId);

        // Finance Report Validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, bicSubscriptionId,
            null, 2, addSeatsPurchaseOrderId, bicOffering, findPurchaseOrdersPage, true, true, purchaseOrderUtils,
            OrderCommand.REFUND.toString(), subscriptionDetailPage.getNextBillingDate(), 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests Add Seats with original PO having one time and recurring promotion With data provider
     * "true", One Time Promotion Step1 : Create a New Acquisition Purchase Order, with one time promotion Step2 : Add
     * Seats to the PO with no promotion Step3 : Verify finance report that no new subscription is created and "Added
     * Subscription To" column is empty
     * <p>
     * With data provider "false", Recurring Promotion Step1 : Create a New Acquisition Purchase Order, with recurring
     * promotion Step2 : Add Seats to the PO with no promotion Step3 : Verify finance report that new subscription is
     * created, and "Added Subscription To" column has original Subscription Id Step4 : Refund the order
     *
     * @param isOneTimePromotion
     */
    @Test(dataProvider = "getBooleanValue")
    public void testFinanceReportForAddSeatsWithOriginalPurchaseOrderWithOneTimeOrRecurringPromotion(
        final boolean isOneTimePromotion) {

        // create promotion reference for one time or recurring promotion.
        final PromotionReferences promotionReferences = new LineItem.PromotionReferences();
        final PromotionReference promotionReference = new LineItem.PromotionReference();
        if (isOneTimePromotion) {
            promotionReference.setId(storeWidePercentagePromoId);
        } else {
            promotionReference.setId(nonStoreWideCashDiscountPromoId);
        }
        promotionReferences.setPromotionReference(promotionReference);

        // Create a purchase order with one time or recurring promotion
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, 5)), false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, promotionReferences)), buyerUser);

        // get PurchaseOrder Id and Process it for PENDING & CHARGE.
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // get subscription Id.
        String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Add Seats Map containing Subscription Id, Price Id and Quantity.
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        String addSeatsSubscriptionId;
        // Filling addSeatsSubscriptionId only for recurring promotion, otherwise it will be empty
        if (!isOneTimePromotion) {
            addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();
        } else {
            addSeatsSubscriptionId = originalSubscriptionId;
            originalSubscriptionId = null;
        }

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);
        // Finance Report validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId, originalSubscriptionId, 5, addSeatsPurchaseOrderId, bicOffering,
            findPurchaseOrdersPage, true, true, purchaseOrderUtils, OrderCommand.REFUND.toString(),
            subscriptionDetailPage.getNextBillingDate(), 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests Add Seats with Add Seats PO having one time and recurring promotion With data provider
     * "true" , One Time Promotion Step1 : Create a New Acquisition Purchase Order, with no promotion Step2 : Add Seats
     * to the PO with one time promotion Step3 : Verify finance report that no new subscription is created and "Added
     * Subscription To" column is empty
     * <p>
     * With data provider "false", Recurring Promotion Step1 : Create a New Acquisition Purchase Order, with no
     * promotion Step2 : Add Seats to the PO with recurring promotion Step3 : Verify finance report that new
     * subscription is created, and "Added Subscription To" column has original Subscription Id Step4 : Charge back the
     * order
     *
     * @param isOneTimePromotion
     */
    @Test(dataProvider = "getBooleanValue")
    public void testFinanceReportForAddSeatsWithOneTimeOrRecurringPromotion(final boolean isOneTimePromotion) {

        // Submit New Acquisition PO
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBicOffering, 10);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Add Seats Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "25");

        // Add subscription depending on one time or recurring promotion
        if (isOneTimePromotion) {
            subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), storeWidePercentagePromoId);
        } else {
            subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(),
                nonStoreWideCashDiscountPromoId);
        }

        // Submit PO for Add Seats
        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Get subscription Id for Add Seats Order, only for recurring promotion
        String addSeatsSubscriptionId;
        if (!isOneTimePromotion) {
            addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();
        } else {
            addSeatsSubscriptionId = originalSubscriptionId;
            originalSubscriptionId = null;
        }

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);
        // Finance Report Validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId, originalSubscriptionId, 25, addSeatsPurchaseOrderId, bicOffering,
            findPurchaseOrdersPage, true, true, purchaseOrderUtils, OrderCommand.CHARGEBACK.toString(),
            subscriptionDetailPage.getNextBillingDate(), TAX, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests creating new subscription when Renewal and Add Seats both have recurring promotion Step1 :
     * Create a New Acquisition Purchase Order, with recurring promotion Step2 : Create a Renewal Purchase Order, with
     * recurring promotion Step3 : Add Seats to the PO with recurring promotion Step4 : Verify finance report that new
     * subscription is created, and "Added Subscription To" column has original Subscription Id Step4 : Mark As Refund
     * the order
     */
    @Test
    public void testFinanceReportForRenewalAndAddSeatsWithRecurringPromotion() {

        // create promotion reference for recurring promotion.
        PromotionReferences promotionReferences = new PromotionReferences();
        PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(nonStoreWideCashDiscountPromoId);
        promotionReferences.setPromotionReference(promotionReference);

        // Create a purchase order with recurring promotion and get subscription id.
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, 5)), false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, promotionReferences)), buyerUser);

        // get PurchaseOrder Id and Process it for PENDING & CHARGE.
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // get subscription Id.
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create Promotion reference for renewal order
        final HashMap<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        promotionReferences = new PromotionReferences();
        promotionReference = new PromotionReference();
        promotionReference.setId(nonStoreWideCashDiscountPromoId);
        promotionReferences.setPromotionReference(promotionReference);
        pricePromoReferencesMap.put(priceIdForBicOffering, promotionReferences);
        pricePromoReferencesMap.put(originalSubscriptionId, promotionReferences);

        // Submit Renewal order
        purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser,
            new ArrayList<>(ImmutableList.of(originalSubscriptionId)), false, PaymentType.CREDIT_CARD,
            pricePromoReferencesMap, true);

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "10");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), nonStoreWideCashDiscountPromoId);

        // Submit Add Seats Order
        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);

        // Finance Report validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId, originalSubscriptionId, 10, addSeatsPurchaseOrderId, bicOffering,
            findPurchaseOrdersPage, true, true, purchaseOrderUtils, PelicanConstants.MARK_AS_REFUND,
            subscriptionDetailPage.getNextBillingDate(), TAX, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests new subscription gets created when credit days are there in the subscription Step1 : Create
     * a New Acquisition subscription Step2 : Add 10 Credit Days Step3 : Add Seats to the same subscription Step4 :
     * Verify finance report that new subscription is created, and "Added Subscription To" column has original
     * Subscription Id Step4 : Refund the order
     */
    @Test
    public void testFinanceReportForAddSeatsWithCreditDays() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBicOffering, 8);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Add Credit Days for the user
        addCreditDaysPage.addCreditDays(originalSubscriptionId, 10, "Adding Credit days");

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "4");

        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);

        // Finance Report Validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId, originalSubscriptionId, 4, addSeatsPurchaseOrderId, bicOffering,
            findPurchaseOrdersPage, true, true, purchaseOrderUtils, OrderCommand.REFUND.toString(),
            subscriptionDetailPage.getNextBillingDate(), TAX, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests new subscription gets created when seats are added with recurring promotion to the added
     * subscription with recurring promotion Step1 : Create a new subscription, with no promotion (ID :123) Step2 : Add
     * Seats with recurring promotion, new subscription gets created (234) Step3 : Add Seats with recurring promotion to
     * 234, new subscription gets created, (345) Step4 : Verify finance report that new subscription is created, and
     * "Added Subscription To" column has original Subscription Id (234) Step5 : Charge back the order
     */
    @Test
    public void testFinanceReportForAddSeatsToAddedSeatsCreatedSubscription() {

        // Submit New Acquisition PO
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBicOffering, 10);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "12");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), nonStoreWideCashDiscountPromoId);

        PurchaseOrder purchaseOrderForAddedSeats1 =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId1 = purchaseOrderForAddedSeats1.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId1);
        purchaseOrderForAddedSeats1 =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId1);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionId1 = purchaseOrderForAddedSeats1.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), addSeatsSubscriptionId1);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "15");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), nonStoreWideCashDiscountPromoId);

        PurchaseOrder purchaseOrderForAddedSeats2 =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId2 = purchaseOrderForAddedSeats2.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId2);
        purchaseOrderForAddedSeats2 =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId2);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionId2 = purchaseOrderForAddedSeats2.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId2);

        // Fiance Report Validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId2, addSeatsSubscriptionId1, 15, addSeatsPurchaseOrderId2, bicOffering,
            findPurchaseOrdersPage, true, true, purchaseOrderUtils, OrderCommand.CHARGEBACK.toString(),
            subscriptionDetailPage.getNextBillingDate(), TAX, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests seats are added to the same subscription when price id changes Step1 : Create a new
     * subscription Step2 : Expire the existing subscription, by updating the DB Step3 : Add another Active Price Step4
     * : Add Seats with active price id Step5 : Verify in finance report seats are added to the same subscription,
     * "Added Subscription To" column is empty Step6 : Mark As Refund the order
     */
    @Test
    public void testFinanceReportForAddSeatsWithPriceIdChange() {

        // Create Subscription Plan
        Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicOfferingId1 = bicOffering1.getOfferings().get(0).getId();

        // Get Price Id associated with Subscription Plan.
        final String bicPriceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();

        // Creating 2 prices together, so that offering cache in subscription service is updated
        final SubscriptionOfferPrice offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(450, getPricelistExternalKeyUs(), 13, 15);

        final SubscriptionOfferPrice activePrice = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            offerPrice, bicOffering1.getOfferings().get(0).getId(),
            bicOffering1.getIncluded().getBillingPlans().get(0).getId());

        final String activePriceId = activePrice.getData().getId();

        // Submit and Process PO with above created price id.
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPriceId1, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT),
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), bicPriceId1),
            getEnvironmentVariables());

        // Changing DB for subscription_price table with active date
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE,
                DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_FORMAT),
                DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_TIME_FORMAT, 16), activePriceId),
            getEnvironmentVariables());

        bicOffering1 = resource.offerings().getOfferingById(bicOfferingId1, "prices");

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), activePriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        final PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, String.valueOf(TAX), buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            3, addSeatsPurchaseOrderId, bicOffering1, findPurchaseOrdersPage, true, true, purchaseOrderUtils,
            PelicanConstants.MARK_AS_REFUND, subscriptionDetailPage.getNextBillingDate(), TAX, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "getBooleanValue")
    private Object[][] getBooleanValue() {
        return new Object[][] { { true }, { false } };
    }

    /**
     * This method tests finance report when Add Seats to a added seats Step1 : Submit New Acquisition PO with Recurring
     * promotion (subscription1). Step2 : Add Seats to (subscription1), it will create subscription2. Step3 : Add Seats
     * to subscription2. Step4 : Verify in finance report seats are added to the same subscription, "Added Subscription
     * To" column is empty Step6 : Mark As Refund the order
     */
    @Test
    public void testFinanceReportForAddSeatsToAddedSubscription() {
        // create promotion reference for recurring promotion.
        final PromotionReferences promotionReferences = new LineItem.PromotionReferences();
        final PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(nonStoreWideCashDiscountPromoId);
        promotionReferences.setPromotionReference(promotionReference);

        // Create a purchase order with recurring promotion
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, 5)), false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, promotionReferences)), buyerUser);

        // get PurchaseOrder Id and Process it for PENDING & CHARGE.
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // get subscription Id.
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Add Seats Map containing Subscription Id, Price Id and Quantity.
        Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), addSeatsSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(addSeatsSubscriptionId);

        // Finance Report validation
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource,
            addSeatsSubscriptionId, null, 2, addSeatsPurchaseOrderId, bicOffering, findPurchaseOrdersPage, true, true,
            purchaseOrderUtils, PelicanConstants.MARK_AS_REFUND, subscriptionDetailPage.getNextBillingDate(), 0.00,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
