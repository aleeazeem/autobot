package com.autodesk.bsm.pelican.ui.purchaseorder;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionEventType;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.Subscription;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.AddCreditDaysPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.CancelSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableMap;

import org.apache.http.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test case specially to test refund of AUTO family
 *
 * @author Shweta Hegde
 */
public class RefundPurchaseOrderTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private FindSubscriptionsPage findSubscriptionPage;
    private AddCreditDaysPage creditDaysPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String purchaseOrderId;
    private HashMap<String, Integer> priceQuantityMap;
    private HashMap<String, PromotionReferences> pricePromoReferencesMap;
    private PromotionReferences promotionReferences;
    private String priceIdOfBicOffering;
    private String priceIdOfMetaOffering;
    private String activeNonStoreWideCashAmountPromoId;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolAutoUser = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolAutoUser.login();

        findPurchaseOrdersPage = adminToolAutoUser.getPage(FindPurchaseOrdersPage.class);
        findSubscriptionPage = adminToolAutoUser.getPage(FindSubscriptionsPage.class);
        creditDaysPage = adminToolAutoUser.getPage(AddCreditDaysPage.class);

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Add subscription Offers
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdOfBicOffering = bicOffering.getIncluded().getPrices().get(0).getId();

        final Offerings metaOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdOfMetaOffering = metaOffering.getIncluded().getPrices().get(0).getId();

        final JPromotion activeNonStoreWideDiscountPercentPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, newArrayList(getStoreUs()),
                newArrayList(bicOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "100", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final JPromotion activeNonStoreWideCashAmountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            newArrayList(getStoreUs()), newArrayList(bicOffering), promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, null, "10", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        activeNonStoreWideCashAmountPromoId = activeNonStoreWideCashAmountPromo.getData().getId();

        promotionReferences = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(activeNonStoreWideDiscountPercentPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This method tests zero amount order with promotion which nullifies the total amount. -It verifies the total
     * amount is zero, - after REFUND button is clicked, new pop up window opens - Verify after that order state is NOT
     * REFUNDED.
     */
    @Test
    public void testSuccessZeroAmountOrderWithPromotionsCannotBeRefunded() {
        // submit a purchase order with price and promotion
        priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 1);
        pricePromoReferencesMap.put(priceIdOfBicOffering, promotionReferences);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        purchaseOrderId = purchaseOrder.getId();

        // Get purchase order details
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Total amount should be zero",
            purchaseOrderDetailPage.getTotalAmountOrder().split(" ")[3], equalTo("0.00"), assertionErrorList);

        // Click on REFUND and verify that it is not refund able
        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\\.")[0];

        AssertCollector.assertThat("Incorrect error message for zero amount order refund", errorMessage,
            equalTo(PelicanErrorConstants.ZERO_AMOUNT_ORDER_REFUND), assertionErrorList);
        AssertCollector.assertThat("Incorrect order status", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests zero amount order with Credit Days which nullifies the total amount of next purchase -It
     * verifies the total amount is zero, - after REFUND button is clicked, new pop up window opens - Verify after that
     * order state is NOT REFUNDED.
     */
    @Test
    public void testSuccessZeroAmountOrderWithCreditDaysCannotBeRefunded() {
        // Submit initial purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL,
            priceIdOfMetaOffering, buyerUser, 1);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // get the subscription id to renew a subscription
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        findSubscriptionPage.findBySubscriptionId(subscriptionId);
        // Add 31 credit days. So that next billing price is 0.00
        creditDaysPage.addCreditDays(subscriptionId, 31, "Adding credit days to nullify next billing price");

        // Submit a purchase order for the subscription
        final Object purchaseOrderOfSubscription = purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(
            PaymentType.PAYPAL, subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);
        purchaseOrder = (PurchaseOrder) purchaseOrderOfSubscription;
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // Get purchase order details
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Total amount should be zero",
            purchaseOrderDetailPage.getTotalAmountOrder().split(" ")[3], equalTo("0.00"), assertionErrorList);

        // Click on REFUND and verify that it is not refund able
        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\\.")[0];

        AssertCollector.assertThat("Incorrect error message for zero amount order refund", errorMessage,
            equalTo(PelicanErrorConstants.ZERO_AMOUNT_ORDER_REFUND), assertionErrorList);
        AssertCollector.assertThat("Incorrect order status", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests zero amount order with promotion which nullifies the total amount. -It verifies the total
     * amount is zero, - after MARK AS REFUND button is clicked, new pop up window opens - Verify after that order state
     * is NOT REFUNDED.
     */
    @Test
    public void testSuccessZeroAmountOrderWithPromotionsCannotBeMarkedAsRefunded() {
        // submit a purchase order with price and promotion
        priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 1);
        pricePromoReferencesMap.put(priceIdOfBicOffering, promotionReferences);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        purchaseOrderId = purchaseOrder.getId();

        // Get purchase order details
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Total amount should be zero",
            purchaseOrderDetailPage.getTotalAmountOrder().split(" ")[3], equalTo("0.00"), assertionErrorList);

        // Click on MARK AS REFUND and verify that it is not refund able
        purchaseOrderDetailPage.clickMarkAsRefunded();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\\.")[0];

        AssertCollector.assertThat("Incorrect error message for zero amount order refund", errorMessage,
            equalTo(PelicanErrorConstants.ZERO_AMOUNT_ORDER_REFUND), assertionErrorList);
        AssertCollector.assertThat("Incorrect order status", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests zero amount order with Credit Days which nullifies the total amount of next purchase -It
     * verifies the total amount is zero, - after MARKED AS REFUND button is clicked, new pop up window opens - Verify
     * after that order state is NOT REFUNDED.
     */
    @Test
    public void testSuccessZeroAmountOrderWithCreditDaysCannotBeMarkedAsRefunded() {

        // Submit initial purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL,
            priceIdOfMetaOffering, buyerUser, 1);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // get the subscription id to renew a subscription
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        findSubscriptionPage.findBySubscriptionId(subscriptionId);
        // Add 31 credit days. So that next billing price is 0.00
        creditDaysPage.addCreditDays(subscriptionId, 31, "Adding credit days to nullify next billing price");

        // Submit a purchase order for the subscription
        final Object purchaseOrderOfSubscription = purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(
            PaymentType.PAYPAL, subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);
        purchaseOrder = (PurchaseOrder) purchaseOrderOfSubscription;
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // Get purchase order details
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Total amount should be zero",
            purchaseOrderDetailPage.getTotalAmountOrder().split(" ")[3], equalTo("0.00"), assertionErrorList);

        // Click on MARK AS REFUND and verify that it is not refund able
        purchaseOrderDetailPage.clickMarkAsRefunded();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\\.")[0];

        AssertCollector.assertThat("Incorrect error message for zero amount order refund", errorMessage,
            equalTo(PelicanErrorConstants.ZERO_AMOUNT_ORDER_REFUND), assertionErrorList);
        AssertCollector.assertThat("Incorrect order status", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests zero amount order subscription can be cancelled. -It verifies the subscription status
     */
    @Test
    public void testSuccessZeroAmountOrderSubscriptionCanBeCancelled() {
        // submit a purchase order with price and promotion
        priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 1);
        pricePromoReferencesMap.put(priceIdOfBicOffering, promotionReferences);

        // submit a purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // get the subscription id to renew a subscription
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.IMMEDIATE_NO_REFUND, null);

        final Subscription subscription = findSubscriptionPage.assignAllFieldsToSubscription();
        AssertCollector.assertThat("Incorrect subscription status", subscription.getStatus(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests Refund Message for Add Seats order when renewal is in progress Step1 : Submit new
     * acquisition order Step2 : Submit Add Seats order Step3 : Submit Renewal in AUTH Step4 : Refund Add Seats order
     * Step5 : Verify warning message pop up
     */
    @Test
    public void testRefundMessageForAddSeatsOrderWhenRenewalInProgress() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 5);
        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Submit Renewal Order
        purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(newArrayList(subscriptionId), false,
            PaymentType.CREDIT_CARD, null, true, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\n")[0];

        AssertCollector.assertThat("Incorrect error message for add seats order refund after renewal", errorMessage,
            equalTo(PelicanErrorConstants.ADD_SEATS_REFUND_AFTER_RENEWAL), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests Refund Message for Add Seats order is renewal completed Step1 : Submit new acquisition order
     * Step2 : Submit Add Seats order Step3 : Reduce seats Step4 : Submit Renewal in AUTH and process to pending and
     * charge Step5 : Submit and Process Add Seats Order. Step 6: Refund Add Seats order Step7 : Verify warning message
     * pop up
     */
    @Test
    public void testRefundMessageForAddSeatsOrderWithReduceSeatAfterRenewal() {

        // create promotion reference for recurring promotion.
        final LineItem.PromotionReferences promotionReferencesForPO = new LineItem.PromotionReferences();
        final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(activeNonStoreWideCashAmountPromoId);
        promotionReferencesForPO.setPromotionReference(promotionReference);

        // Create a purchase order with recurring promotion and get subscription id.
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdOfBicOffering, 5)), false, PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdOfBicOffering, promotionReferencesForPO)), buyerUser);

        // get PurchaseOrder Id and Process it for PENDING & CHARGE.
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // get subscription Id.
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();
        // Reduce seat
        resource.reduceSeatsBySubscriptionId().reduceSeats(addSeatsSubscriptionId, "1");

        // Submit Renewal Order
        purchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(newArrayList(addSeatsSubscriptionId),
            false, PaymentType.CREDIT_CARD, null, true, buyerUser);

        final String renewalPurchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPurchaseOrderId);

        // Submit Add Seats Order to add seats quantity.
        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap1 = new HashMap<>();
        subscriptionQuantityMap1.put(LineItemParams.SUBSCRIPTION_ID.getValue(), addSeatsSubscriptionId);
        subscriptionQuantityMap1.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap1.put(LineItemParams.QUANTITY.getValue(), "3");
        // Submit Add Seats order
        final PurchaseOrder purchaseOrderForAddedSeatsOnRenewedOrder =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap1),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String purchaseOrderForAddedSeatsOnRenewedOrderId = purchaseOrderForAddedSeatsOnRenewedOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeatsOnRenewedOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForAddedSeatsOnRenewedOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\n")[0];

        AssertCollector.assertThat("Incorrect error message for add seats order refund after renewal", errorMessage,
            equalTo(PelicanErrorConstants.ADD_SEATS_REFUND_AFTER_RENEWAL), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test cases tests Subscription status is ACTIVE and Quantity is unchanged when Add Seats Order is Marked as
     * refunded after renewal
     */
    @Test
    public void testMarkAsRefundForAddSeatsOrderWithReduceSeatAfterRenewal() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 5);
        // Submit New Acquisition PO
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Submit Renewal Order
        purchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(newArrayList(subscriptionId), false,
            PaymentType.CREDIT_CARD, null, true, buyerUser);

        final String renewalPurchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPurchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        AssertCollector.assertThat("Incorrect order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);

        final SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Incorrect subscription quantity", subscriptionDetailPage.getQuantity(), is(8),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Refund Add Seats Order with Reduce seats displays message when quantity goes below or equal to
     * zero.Step 1: Submit New Acquisition Order. Step 2: Submit Add Seats Order and Process it for PENDING followed by
     * CHARGE. Step 3 : Reduce seats. Step 4 : Refund Add Seats Order.
     */
    @Test(dataProvider = "addSeatsRefundWithReducedSeats")
    public void testRefundForAddSeatsOrderWithReduceSeatBeforeRenewal(final String promotionId,
        final String reduceSeatsQuantity, final String expectedErrorMessage) {
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 5);
        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");
        if (promotionId != null) {
            subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), promotionId);
        }

        // Submit Add Seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Process it for PENDING and CHARGE.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);
        if (promotionId != null) {
            subscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getSubscriptionId();
        }

        // Reduce seat
        resource.reduceSeatsBySubscriptionId().reduceSeats(subscriptionId, reduceSeatsQuantity);
        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\n")[0];

        AssertCollector.assertThat("Incorrect error message for add seats order refund", errorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case has been added to capture the cause of intermittent failure. Step 1: Submit New Acquisition Order.
     * Step 2: Submit Add Seats Order and Process it for PENDING followed by CHARGE. Step 3 : Reduce seats. Step 4 :
     * Refund Add Seats Order. Step 5: Reduce seats with -ve qty. Step 6: Refund Add Seats Order.
     */
    @Test
    public void testRefundAddSeatsWithMultipleReduceSeatsRequest() {
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdOfBicOffering, 5);
        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdOfBicOffering);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Reduce seat
        resource.reduceSeatsBySubscriptionId().reduceSeats(subscriptionId, "5");

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickRefund();
        final String errorMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\n")[0];
        AssertCollector.assertThat("Incorrect error message for add seats order refund.", errorMessage,
            equalTo(PelicanErrorConstants.ADD_SEATS_REFUND_BEFORE_RENEWAL), assertionErrorList);
        // Reduce seat
        resource.reduceSeatsBySubscriptionId().reduceSeats(subscriptionId, "-2");

        purchaseOrderDetailPage.refreshPage();
        purchaseOrderDetailPage.clickRefund();
        final String refundMessage = purchaseOrderDetailPage.getPopUpMessageOnRefund().split("\n")[0];

        AssertCollector.assertThat(
            "Proration End Date Should be Populated", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityRequest().getProrationEndDate(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for add seats order refund.", refundMessage,
            equalTo(PelicanErrorConstants.REFUND_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify processing PO for Refund or Chargeback for Expired Subscription will not update subscription
     * expiration date. step1 : place a new acquisition order and renew it. Step 2: cancel the subscription which
     * Expires the subscription Step 3: Refund and ChargeBack Renewal and new acquisition Order respectively. It should
     * not modify Subscription Expiration Date.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testRefundPurchaseOrderForExpiredSubscriptionWillNotUpdateExpirationDate()
        throws ParseException, IOException {
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 5);
        final PurchaseOrder newAcquisitionOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String newAcquisitionOrderId = newAcquisitionOrder.getId();
        final String subscriptionId = resource.purchaseOrder().getById(newAcquisitionOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // Renew Order
        final PurchaseOrder renewalOrder1 = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser,
            Arrays.asList(subscriptionId), false, PaymentType.CREDIT_CARD, null, true);
        final String renewalOrder1Id = renewalOrder1.getId();

        // Expire the subscription with cancel Immediate no Refund.
        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        subscriptionDetailPage.clickOnCancelSubscription().cancelASubscription(CancellationPolicy.IMMEDIATE_NO_REFUND,
            "Expiring the Subscription");
        subscriptionDetailPage.refreshPage();
        final String subscriptionExpirationDate = subscriptionDetailPage.getCompleteExpirationDate();

        // Refund Renewal Order after some delay.
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        findPurchaseOrdersPage.findPurchaseOrderById(renewalOrder1Id);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess("Refund");

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        final String expirationDateAfterRefund = subscriptionDetailPage.getCompleteExpirationDate();
        AssertCollector.assertThat("Expiration Date Should not be updated on Refund", expirationDateAfterRefund,
            equalTo(subscriptionExpirationDate), assertionErrorList);

        // ChargeBack New Acquisition Order after some delay.
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, newAcquisitionOrderId);
        subscriptionDetailPage.refreshPage();
        final String expirationDateAfterChargeBack = subscriptionDetailPage.getCompleteExpirationDate();
        AssertCollector.assertThat("Expiration Date Should not be updated on ChargeBack", expirationDateAfterChargeBack,
            equalTo(subscriptionExpirationDate), assertionErrorList);

        // Verify there should not be more than one expiration event for subscription.
        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put("eventTypes", SubscriptionEventType.EXPIRED.toString());
        final Object apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        final JSubscriptionEvents subscriptionEvents = (JSubscriptionEvents) apiResponse;

        AssertCollector.assertThat("There should not be more than one expire event",
            subscriptionEvents.getEventsData().size(), equalTo(1), assertionErrorList);

        AssertCollector.assertThat("Event type: EXPIRED is not correct for subscription: " + subscriptionId,
            subscriptionEvents.getEventsData().get(0).getEventType(), equalTo(SubscriptionEventType.EXPIRED.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Audit log of BIC Subscription Change when its Purchase Order is Marked As Refunded 1. Create a
     * purchase order 2. Click on Mark As Refund 3. Verify audit log for the subscription change
     */
    @Test
    public void testAuditLogOfBICSubscriptionWhenPurchaseOrderMarkAsRefund() {

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 3)), false, buyerUser);

        purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // click on Mark As Refund
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        final boolean updateSubscriptionLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, Status.ACTIVE,
                Status.EXPIRED, null, null, true, assertionErrorList);

        AssertCollector.assertTrue("Update Subscription Audit Log not found for Subscription id : " + subscriptionId,
            updateSubscriptionLogFound, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Audit log of Canceled Subscription Change when its Purchase Order is Marked As Refunded 1.
     * Create a purchase order 2. Click on Mark As Refund 3. Verify audit log for the subscription change
     */
    @Test
    public void testAuditLogOfCanceledSubscriptionWhenPurchaseOrderMarkAsRefund() {

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 1)), false, buyerUser);

        purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // click on Mark As Refund
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        final String bicCommercialNextBillingDate = DateTimeUtils.getNowAsUTCPlusMonths(
            PelicanConstants.DATE_TIME_FORMAT,
            Integer.parseInt(getBicSubscriptionPlan().getIncluded().getBillingPlans().get(0).getBillingPeriodCount()));

        final boolean updateSubscriptionLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId,
                Status.CANCELLED, Status.EXPIRED, bicCommercialNextBillingDate, null, true, assertionErrorList);

        AssertCollector.assertTrue("Update Subscription Audit Log not found for Subscription id : " + subscriptionId,
            updateSubscriptionLogFound, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Audit log of Delinquent Subscription Change when its Purchase Order is Marked As Refunded 1.
     * Create a purchase order 2. Click on Mark As Refund 3. Verify audit log for the subscription change
     */
    @Test
    public void testAuditLogOfDelinquentSubscriptionWhenPurchaseOrderMarkAsRefund() {

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(
            new HashMap<>(ImmutableMap.of(getBicYearlyUkPriceId(), 2)), false, buyerUser);

        purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionId);

        // Submit a renewal request with DECLINE order command. After this
        // subscription will be in
        // Delinquent status
        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            subscriptionIds, false, PaymentType.CREDIT_CARD, null, true, buyerUser);
        final String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderIdForBicCreditCard);

        // click on Mark As Refund
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // DelinquentResolveByDate = nextBillingDate + renewalGracePeriod, this
        // is the calculation
        final boolean updateSubscriptionLogFound = SubscriptionAuditLogHelper
            .helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, Status.DELINQUENT, Status.EXPIRED, null,
                DateTimeUtils.addDaysToDate(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_FORMAT),
                    PelicanConstants.DATE_TIME_FORMAT, PelicanConstants.RENEWAL_GRACE_PERIOD_IN_DAYS),
                true, assertionErrorList);

        AssertCollector.assertTrue("Update Subscription Audit Log not found for Subscription id : " + subscriptionId,
            updateSubscriptionLogFound, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Data Provider to test add seats Refund Scenario with Reduced Seats Quantity and Promotion.
     *
     * @return
     */
    @DataProvider(name = "addSeatsRefundWithReducedSeats")
    private Object[][] getTestDataForAddSeatsRefundWithReducedSeats() {
        return new Object[][] { { null, "5", PelicanErrorConstants.ADD_SEATS_REFUND_BEFORE_RENEWAL },
                { null, "6", PelicanErrorConstants.ADD_SEATS_REFUND_BEFORE_RENEWAL },
                { null, "2", PelicanErrorConstants.REFUND_MESSAGE },
                { activeNonStoreWideCashAmountPromoId, "0", PelicanErrorConstants.REFUND_MESSAGE },
                { activeNonStoreWideCashAmountPromoId, "1", PelicanErrorConstants.ADD_SEATS_REFUND_BEFORE_RENEWAL } };
    }

}
