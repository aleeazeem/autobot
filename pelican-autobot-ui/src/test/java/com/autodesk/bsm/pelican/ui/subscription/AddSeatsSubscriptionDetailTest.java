package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionEventType;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.AddCreditDaysPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ReduceSeatsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.purchaseorder.PurchaseOrderHelper;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddSeatsSubscriptionDetailTest extends SeleniumWebdriver {

    private FindSubscriptionsPage findSubscriptionPage;
    private AddCreditDaysPage addCreditDaysPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private String nonStoreAmountDiscountPromoId;
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String bicCommercialPriceId;
    private Offerings bicCommercialOffering;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static final int quantityOfPurchaseOrderWithPromotion = 2;
    private static final int quantityOfPurchase = 2;
    private static final String discountAmount = "10.00";
    private String bicCommercialOfferingAmount;
    private ReduceSeatsPage reduceSeatsPage;
    private HashMap<String, Integer> priceQuantityMap;
    private PromotionUtils promotionUtils;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void addSeatsSubscriptionDetailTestSetUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        addCreditDaysPage = adminToolPage.getPage(AddCreditDaysPage.class);
        reduceSeatsPage = adminToolPage.getPage(ReduceSeatsPage.class);

        bicCommercialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicCommercialPriceId = bicCommercialOffering.getIncluded().getPrices().get(0).getId();

        // Creating Regular Promotions for BIC subscriptions non Store wide % Discount
        final JPromotion nonStoreAmountDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicCommercialOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, discountAmount, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        nonStoreAmountDiscountPromoId = nonStoreAmountDiscountPromo.getData().getId();

        // Amount of bic subscription offering.
        bicCommercialOfferingAmount = bicCommercialOffering.getIncluded().getPrices().get(0).getAmount();
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This method verifies while adding seats to existing subscription adds quantity and create new transaction
     * activity.
     *
     */
    @Test
    public void testAddSeatsIncreasesQuantityAndAddNewSubscriptionActivityAndRefundAddSeatsOrder() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String originalSubscriptionIdForAddSeats =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(originalSubscriptionIdForAddSeats, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionIdForAddSeats);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaseOrderIdForAddedSeatsWithInSameSubscription = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING,
            purchaseOrderIdForAddedSeatsWithInSameSubscription, ECStatus.ACCEPT);

        // get Purchase Order
        purchaseOrderForAddedSeats =
            resource.purchaseOrder().getById(purchaseOrderIdForAddedSeatsWithInSameSubscription);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(originalSubscriptionIdForAddSeats);

        // Assert on Quantity after adding Seats to Original Subscription.
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(4), assertionErrorList);

        // Assert on Pending Payment field
        AssertCollector.assertTrue("Incorrect Pending payment Flag",
            subscriptionDetailPage.getPendingPaymentStatus().equalsIgnoreCase(PelicanConstants.TRUE),
            assertionErrorList);

        // Assert on Next billing Date should be as per Proration End Date.
        AssertCollector.assertThat("Incorrect Next Billing Date ", subscriptionDetailPage.getNextBillingDate(),
            equalTo(purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getProrationEndDate().split(" ")[0]),
            assertionErrorList);

        // Assert on Next billing Price Id.
        AssertCollector.assertThat("Incorrect Next Billing Price Id ", subscriptionDetailPage.getNextBillingPriceId(),
            equalTo(purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getPriceId()),
            assertionErrorList);

        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE,
            purchaseOrderIdForAddedSeatsWithInSameSubscription, ECStatus.ACCEPT);

        purchaseOrderForAddedSeats =
            resource.purchaseOrder().getById(purchaseOrderIdForAddedSeatsWithInSameSubscription);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage.refreshPage();

        // Assert on Export Control Status
        AssertCollector.assertThat("Incorrect Export Control Status", subscriptionDetailPage.getExportControlStatus(),
            equalTo(ECStatus.ACCEPT.getDisplayName()), assertionErrorList);

        // Assert on Pending Payment field
        AssertCollector.assertTrue("Incorrect Pending payment Flag",
            subscriptionDetailPage.getPendingPaymentStatus().equalsIgnoreCase(PelicanConstants.FALSE),
            assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat("Correct time stamp is not captured under Subscription Activity",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat("Correct Activity is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(PelicanConstants.ADD_SEATS), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        final String expectedChargedAmount =
            String.format("%,.2f", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getTotalPrice());
        AssertCollector.assertThat("Correct Charged Amount is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
            equalTo(expectedChargedAmount), assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat("Correct Grant days is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat("Correct Purchase Order is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(purchaseOrderIdForAddedSeatsWithInSameSubscription), assertionErrorList);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.", "3")), assertionErrorList);

        // Process Order for Refund.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND,
            purchaseOrderIdForAddedSeatsWithInSameSubscription);

        // getting the Purchase Order Detail with Refund status.
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForAddedSeatsWithInSameSubscription);
        final PurchaseOrderDetailPage refundedPurchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        PurchaseOrderHelper.assertionsForRefundPurchaseOrder(refundedPurchaseOrderDetailPage, assertionErrorList);

        // Navigate to subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(originalSubscriptionIdForAddSeats);

        SubscriptionHelper.assertionsOnSubscriptionAfterRefund(subscriptionDetailPage, 1, Status.ACTIVE,
            purchaseOrderIdForAddedSeatsWithInSameSubscription, 3, assertionErrorList);

        final List<String> count = DbUtils.selectQuery(
            PelicanDbConstants.SELECT_COUNT_OF_POS + purchaseOrderIdForAddedSeatsWithInSameSubscription, "count(*)",
            getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("0"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Add Seats for existing subscription with recurring promotion creates new subscription.
     */
    @Test
    public void testAddSeatsForSubscriptionWithRecurringPromoCreatesNewSubscriptionAndRefundAddSeatsOrder() {

        // create promotion reference for recurring promotion.
        final LineItem.PromotionReferences promotionReferencesForPO = new LineItem.PromotionReferences();
        final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(nonStoreAmountDiscountPromoId);
        promotionReferencesForPO.setPromotionReference(promotionReference);

        // Create a purchase order with recurring promotion and get subscription id.
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(bicCommercialPriceId, quantityOfPurchaseOrderWithPromotion)), false,
            PaymentType.CREDIT_CARD, new HashMap<>(ImmutableMap.of(bicCommercialPriceId, promotionReferencesForPO)),
            buyerUser);

        // get PurchaseOrder Id and Process it for PENDING & CHARGE.
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        // get subscription Id.
        final String originalSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(originalSubscriptionId, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Add Seats Map containing Subscription Id, Price Id and Quantity.
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), String.valueOf(quantityOfPurchase));

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        // get Purchase Order Id
        final String addSeatsPurchaseOrderIdWithoutPromo = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderIdWithoutPromo);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderIdWithoutPromo);

        // get subscription Id for Add Seats.
        final String addSeatsSubscriptionIdWithoutPromo = purchaseOrderForAddedSeats.getLineItems().getLineItems()
            .get(0).getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Assert original subscription and add seats subscription ids are different.
        AssertCollector.assertThat("Original Subscription and Add Seats Subscription should not be same:",
            originalSubscriptionId, not(addSeatsSubscriptionIdWithoutPromo), assertionErrorList);

        // Navigate to Original Subscription Detail Page.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(originalSubscriptionId);

        // Assert on Next Billing Charge.
        AssertCollector
            .assertThat("Incorrect Next Billing Charge on original subscription detail page",
                subscriptionDetailPage.getNextBillingCharge().split(" ")[0],
                equalTo(String.format("%,.2f",
                    (Float.parseFloat(bicCommercialOfferingAmount) * quantityOfPurchaseOrderWithPromotion
                        - Float.parseFloat(discountAmount) * quantityOfPurchaseOrderWithPromotion))),
                assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity on original subscription detail page",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(PelicanConstants.ADD_SEATS), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        final String expectedChargedAmountForAddSeats =
            String.format("%,.2f", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getTotalPrice());
        AssertCollector.assertThat(
            "Incorrect Charged Amount captured under Subscription Activity on original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
            equalTo(expectedChargedAmountForAddSeats), assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat(
                "Incorrect Grant days captured under Subscription Activity on original subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity on original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(String.valueOf(addSeatsPurchaseOrderIdWithoutPromo)), assertionErrorList);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat(
            "Incorrect Memo captured under Subscription Activity on original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.\nCreated subscription #%s", quantityOfPurchase,
                addSeatsSubscriptionIdWithoutPromo)),
            assertionErrorList);

        // Navigate to Add Seats Subscription Detail Page and validate on Amount charged and subscription activity.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithoutPromo);

        // Assert on Next Billing Charge.
        AssertCollector.assertThat("Incorrect Next Billing Charge on Add Seats subscription detail page",
            subscriptionDetailPage.getNextBillingCharge().split(" ")[0],
            equalTo(String.format("%,.2f", Float.parseFloat(bicCommercialOfferingAmount) * quantityOfPurchase)),
            assertionErrorList);

        // Assert on Added Quantity subscription id.
        AssertCollector.assertThat("Incorrect Added Quantity Subscription Id on Add Seats subscription detail page: ",
            subscriptionDetailPage.getAddedQuantitySubscriptionId(), equalTo(originalSubscriptionId),
            assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity on Add Seats subscription detail page",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(OrderCommand.CHARGE.toString()), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        AssertCollector
            .assertThat(
                "Incorrect Charged Amount captured under Subscription Activity on Add Seats subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
                equalTo(
                    String.format("%,.2f",
                        ((purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                            .getSubscriptionQuantityResponse().getChargeDetails().getTotalPrice())))),
                assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat(
                "Incorrect Grant days captured under Subscription Activity on Add Seats subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(String.valueOf(addSeatsPurchaseOrderIdWithoutPromo)), assertionErrorList);

        // Process Order for Refund.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, addSeatsPurchaseOrderIdWithoutPromo);

        // getting the Purchase Order Detail with Refund status.
        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderIdWithoutPromo);
        final PurchaseOrderDetailPage refundedPurchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Assert on Refunded Purchase Order
        PurchaseOrderHelper.assertionsForRefundPurchaseOrder(refundedPurchaseOrderDetailPage, assertionErrorList);

        // Navigate to subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithoutPromo);

        // Assert on Subscription after Refund
        SubscriptionHelper.assertionsOnSubscriptionAfterRefund(subscriptionDetailPage, quantityOfPurchase,
            Status.EXPIRED, null, 2, assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderIdWithoutPromo,
                "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify Add Seats with recurring promotion creates new subscription.
     */
    @Test
    public void testAddSeatsWithRecurringPromoCreatesNewSubscriptionAndRefundAddSeatsOrder() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, quantityOfPurchase);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        final String newAcquisitionPurchaseOrderId = purchaseOrder.getId();

        // get subscription Id
        final String newAcquisitionSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(newAcquisitionSubscriptionId, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Add Seats Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), newAcquisitionSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(),
            String.valueOf(quantityOfPurchaseOrderWithPromotion));
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), nonStoreAmountDiscountPromoId);

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        // get Purchase Order ID.
        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionIdWithPromotion = purchaseOrderForAddedSeats.getLineItems().getLineItems()
            .get(0).getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Assert original subscription and add seats subscription are different.
        AssertCollector.assertThat("Original Subscription and Add Seats Subscription should be different :",
            newAcquisitionSubscriptionId, not(addSeatsSubscriptionIdWithPromotion), assertionErrorList);

        // Navigate to Original Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(newAcquisitionSubscriptionId);

        // Assert on Next Billing Charge
        AssertCollector.assertThat("Incorrect Next Billing Charge on Original subscription detail page",
            subscriptionDetailPage.getNextBillingCharge().split(" ")[0],
            equalTo(String.format("%,.2f", Float.parseFloat(bicCommercialOfferingAmount) * quantityOfPurchase)),
            assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity on Original subscription detail page ",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on Original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(PelicanConstants.ADD_SEATS), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        AssertCollector
            .assertThat(
                "Incorrect Charged Amount captured under Subscription Activity on Original subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
                equalTo(String.format("%.2f",
                    ((purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged())))),
                assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat(
                "Incorrect Grant days captured under Subscription Activity on Original subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity on Original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(String.valueOf(addSeatsPurchaseOrderId)), assertionErrorList);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat(
            "Incorrect Memo captured under Subscription Activity on Original subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.\nCreated subscription #%s", quantityOfPurchase,
                addSeatsSubscriptionIdWithPromotion)),
            assertionErrorList);

        // Navigate to Add Seats Subscription Detail Page and validate on Amount charged and subscription activity.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithPromotion);

        // Assert on Added Quantity subscription id.
        AssertCollector.assertThat("Incorrect Added Quantity Subscription Id: ",
            subscriptionDetailPage.getAddedQuantitySubscriptionId(), equalTo(newAcquisitionSubscriptionId),
            assertionErrorList);

        // Assert on Next Billing Charge
        AssertCollector
            .assertThat("Incorrect Next Billing Charge on Add Seats subscription detail page",
                subscriptionDetailPage.getNextBillingCharge().split(" ")[0],
                equalTo(String.format("%,.2f",
                    Float.parseFloat(bicCommercialOfferingAmount) * quantityOfPurchase
                        - Float.parseFloat(discountAmount) * quantityOfPurchaseOrderWithPromotion)),
                assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity Add Seats subscription detail page",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(OrderCommand.CHARGE.toString()), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        AssertCollector
            .assertThat(
                "Incorrect Charged Amount captured under Subscription Activity on Add Seats subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
                equalTo(String.format("%.2f",
                    ((purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged())))),
                assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat(
                "Incorrect Grant days captured under Subscription Activity on Add Seats subscription detail page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(purchaseOrderForAddedSeats.getId()), assertionErrorList);

        // Process Order for Refund.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, newAcquisitionPurchaseOrderId);

        // getting the Purchase Order Detail with Refund status.
        findPurchaseOrdersPage.findPurchaseOrderById(newAcquisitionPurchaseOrderId);
        final PurchaseOrderDetailPage refundedPurchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Assert on Refunded Purchase Order.
        PurchaseOrderHelper.assertionsForRefundPurchaseOrder(refundedPurchaseOrderDetailPage, assertionErrorList);

        // Navigate to subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(newAcquisitionSubscriptionId);

        // Assert on Subscription after Refund.
        SubscriptionHelper.assertionsOnSubscriptionAfterRefund(subscriptionDetailPage, 2, Status.EXPIRED, null, 2,
            assertionErrorList);

        // Navigate to Added Subscription.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithPromotion);

        // Assert on Quantity.
        AssertCollector.assertThat("Incorrect Quantity left after Refund Process", subscriptionDetailPage.getQuantity(),
            is(2), assertionErrorList);

        // Assert on Subscription Status.
        AssertCollector.assertThat("Incorrect Subscription State after Refund", Status.ACTIVE.toString(),
            equalTo(subscriptionDetailPage.getStatus()), assertionErrorList);

        final List<String> count = DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderId,
            "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Add Seats for subscription with credit days creates new subscription.
     */
    @Test
    public void testAddSeatsForSubscriptionWithCreditDaysCreatesNewSubscriptionAndMarkAsRefundAddSeatsOrder() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, quantityOfPurchase);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(originalSubscriptionId, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Add Credit Days for the user
        addCreditDaysPage.addCreditDays(originalSubscriptionId, 10, "To Satisfy angry user");

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), String.valueOf(quantityOfPurchase));

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        // get Purchase Order Id
        final String addSeatsPurchaseOrderIdWithoutCreditDays = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderIdWithoutCreditDays);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderIdWithoutCreditDays);

        // get subscription Id for Add Seats Order.
        final String addSeatsSubscriptionIdWithoutCreditDays = purchaseOrderForAddedSeats.getLineItems().getLineItems()
            .get(0).getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Assert original subscription and add seats subscription ids are different.
        AssertCollector.assertThat("Original Subscription and Add Seats Subscription should not be same:",
            originalSubscriptionId, not(addSeatsSubscriptionIdWithoutCreditDays), assertionErrorList);

        // Navigate to Original Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(originalSubscriptionId);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity on Original Subscription Detail Page",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on Original Subscription Detail Page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(), equalTo("ADD_SEATS"),
            assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        final String expectedChargedAmount =
            String.format("%,.2f", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged());
        AssertCollector.assertThat(
            "Incorrect Charged Amount captured under Subscription Activity on Original Subscription Detail Page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
            equalTo(expectedChargedAmount), assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector
            .assertThat(
                "Incorrect Grant days captured under Subscription Activity on Original Subscription Detail Page",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                        .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity on Original Subscription Detail Page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(String.valueOf(addSeatsPurchaseOrderIdWithoutCreditDays)), assertionErrorList);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat(
            "Incorrect Memo captured under Subscription Activity on Original Subscription Detail Page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.\nCreated subscription #%s", quantityOfPurchase,
                addSeatsSubscriptionIdWithoutCreditDays)),
            assertionErrorList);

        // Navigate to Add Seats Subscription Detail Page and validate on Amount charged and subscription activity.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithoutCreditDays);

        // Assert on Added Quantity subscription id.
        AssertCollector.assertThat("Incorrect Added Quantity Subscription Id on Add Seats Subscription Detail Page: ",
            subscriptionDetailPage.getAddedQuantitySubscriptionId(), equalTo(originalSubscriptionId),
            assertionErrorList);

        // Assert on Next Billing Charge.
        AssertCollector.assertThat("Incorrect Next Billing Charge on Add Seats subscription detail page",
            subscriptionDetailPage.getNextBillingCharge().split(" ")[0],
            equalTo(String.format("%,.2f", (Float.parseFloat(bicCommercialOfferingAmount) * quantityOfPurchase))),
            assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat(
            "Incorrect time stamp captured under Subscription Activity on Add Seats subscription detail page",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat(
            "Incorrect Activity captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(OrderCommand.CHARGE.toString()), assertionErrorList);

        // Assert on Subscription Activity for Charged Amount.
        final String expectedChargedAmountForAddSeats =
            String.format("%,.2f", purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
                .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getTotalPrice());
        AssertCollector.assertThat(
            "Incorrect Charged Amount captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
            equalTo(expectedChargedAmountForAddSeats), assertionErrorList);

        // Assert on Subscription Activity for Grant Days.
        AssertCollector.assertThat(
            "Incorrect Grant days captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
            equalTo(purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityResponse().getChargeDetails().getProrationDays() + " Days"),
            assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat(
            "Incorrect Purchase Order captured under Subscription Activity on Add Seats subscription detail page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(String.valueOf(addSeatsPurchaseOrderIdWithoutCreditDays)), assertionErrorList);

        // getting the Purchase Order Detail with Refund status.
        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderIdWithoutCreditDays);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // click on Mark as Refunded.
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // Assert on Refunded Purchase Order.
        PurchaseOrderHelper.assertionsForRefundPurchaseOrder(purchaseOrderDetailPage, assertionErrorList);

        // Navigate to subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionIdWithoutCreditDays);

        // Assert on Subscription after Refund.
        SubscriptionHelper.assertionsOnSubscriptionAfterRefund(subscriptionDetailPage, 2, Status.EXPIRED, null, 2,
            assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderIdWithoutCreditDays,
                "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies while Declining Add seats to existing subscription DOES NOT add quantity and create new
     * transaction activity.
     *
     * @throws org.apache.http.ParseException
     * @throws IOException
     */
    @Test
    public void testDeclineAddSeatsDoesNotIncreasesQuantityAndAddNewTransactionActivity()
        throws org.apache.http.ParseException, IOException {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaserOrderIdForAddedSeats,
            ECStatus.ACCEPT);

        // process the purchase order to 'Decline' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.DECLINE, purchaserOrderIdForAddedSeats,
            ECStatus.ACCEPT);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForBicCommercial);

        // get Purchase Order.
        purchaseOrderForAddedSeats = resource.purchaseOrder().getById(purchaserOrderIdForAddedSeats);

        // Assert on Quantity after adding Seats to Original Subscription.
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), equalTo(1), assertionErrorList);

        // Assert on Subscription Activity for Date.
        AssertCollector.assertThat("Correct time stamp is not captured under Subscription Activity",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getDate().split(" ")[0]),
            equalTo((purchaseOrderForAddedSeats.getLastModified()).split(" ")[0]), assertionErrorList);

        // Assert on Subscription Activity for Activity type.
        AssertCollector.assertThat("Correct Activity is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
            equalTo(PelicanConstants.ADD_SEATS_DECLINED), assertionErrorList);

        // Assert on Subscription Activity for Purchase Order.
        AssertCollector.assertThat("Correct Purchase Order is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(purchaserOrderIdForAddedSeats), assertionErrorList);

        // Assert on Subscription Activity for Memo.
        final String expectedMemo = "Reverted the add seats request.";
        AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(), equalTo(expectedMemo),
            assertionErrorList);

        // verify subscription event api for SUB_MIGRATED activity for one subscription
        final JSubscriptionEvents subscriptionEvents =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionIdForBicCommercial, null);
        final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
        AssertCollector.assertThat(
            "Activity is not correct for get subscription api for subscription id " + subscriptionIdForBicCommercial,
            subscriptionEventsData.getEventType(), equalTo(SubscriptionEventType.ADD_SEATS_DECLINED.toString()),
            assertionErrorList);
        AssertCollector.assertThat(
            "Requestor name is not correct for get subscription api for subscription id "
                + subscriptionIdForBicCommercial,
            subscriptionEventsData.getRequesterName(), equalTo(null), assertionErrorList);
        AssertCollector.assertThat(
            "Purchase order is not correct for get subscription api for subscription id "
                + subscriptionIdForBicCommercial,
            subscriptionEventsData.getPurchaseOrderId(), equalTo(purchaserOrderIdForAddedSeats), assertionErrorList);
        AssertCollector.assertThat(
            "Memo is not correct for for get subscription api subscription id " + subscriptionIdForBicCommercial,
            subscriptionEventsData.getMemo().trim(), equalTo(expectedMemo), assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + purchaserOrderIdForAddedSeats, "count(*)",
                getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("0"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify New Acquisition stuck at PENDING state creates New Subscription for Add Seats. scenario : New
     * Acquisition PO : 1001 sub : 123 : qty :5 @PENDING Add Seats on 123 - PO : 1002 sub :231 : 2
     */
    @Test
    public void testAddSeatsCreatesNewSubscriptionWhenOrderIsAtPendingState() {
        PurchaseOrder newAcquisitionOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicCommercialPriceId, buyerUser, 5);

        final String newAcquisitionOrderId = newAcquisitionOrder.getId();

        // process it for pending
        newAcquisitionOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, newAcquisitionOrderId);

        // get subscription Id
        final String subscriptionIdNewAcquisition = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdNewAcquisition);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaserOrderIdForAddedSeats);

        // process the purchase order to 'Decline' state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaserOrderIdForAddedSeats);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeats = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        AssertCollector.assertThat("Added Seats Subscription Id should not be same", subscriptionIdNewAcquisition,
            not(subscriptionIdAddSeats), assertionErrorList);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdAddSeats);
        AssertCollector.assertThat("Incorrect Subscription Status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity ", subscriptionDetailPage.getQuantity(), is(2),
            assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + purchaserOrderIdForAddedSeats, "count(*)",
                getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Add Seats Decline Order for Added Subscription Expires the Subscription. scenario : New
     * Acquisition PO : 1001 sub : 123 : qty :5 @PENDING Add Seats on 123 - PO : 1002 sub :231 : 2 Decline 1002 - sub :
     * 231 : Expired & sub : 123 : Active
     */
    @Test
    public void testDeclineAddSeatsOnAddedSubscriptionExpiresTheSubscription() {
        PurchaseOrder newAcquisitionOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicCommercialPriceId, buyerUser, 5);

        final String newAcquisitionOrderId = newAcquisitionOrder.getId();

        // process it for pending
        newAcquisitionOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, newAcquisitionOrderId);

        // get subscription Id
        final String subscriptionIdNewAcquisition = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdNewAcquisition);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaserOrderIdForAddedSeats);

        // process the purchase order to 'Decline' state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaserOrderIdForAddedSeats);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeats = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        AssertCollector.assertThat("Added Seats Subscription Id should not be same", subscriptionIdNewAcquisition,
            not(subscriptionIdAddSeats), assertionErrorList);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdAddSeats);
        AssertCollector.assertThat("Incorrect Subscription Status", subscriptionDetailPage.getStatus(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity after Expiring Subscription",
            subscriptionDetailPage.getQuantity(), is(2), assertionErrorList);

        // Navigate to Subscription of New Acquisition Order.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdNewAcquisition);
        AssertCollector.assertThat("Incorrect Subscription Status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity after Expiring Subscription",
            subscriptionDetailPage.getQuantity(), is(5), assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + purchaserOrderIdForAddedSeats, "count(*)",
                getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Method to verify Declined Add Seats Order Will Reduce the quantity and expires the subscription if qty reaches to
     * zero but do not touch the subscription. Scenario : New Acquisition PO : 1001 sub : 123 : qty :5 @PENDING Add
     * Seats on 123 - PO : 1002 sub :231 : 2 @PENDING Add Seats on 231 - PO : 1003 sub : 321 :3 @CHARGE Add Seats on 321
     * - PO : 1004 sub : 321 :3 @PENDING Decline 1002 - sub : 231:Expired, qty:2 & sub:123 Active & sub:321 Active.
     * Decline 1004 - sub:321 Active , qty:3
     */
    @Test
    public void testAddSeatsDeclineExpiresSubscriptionIfQuantityGoesZero() {

        // Submit Offering Order.
        PurchaseOrder newAcquisitionOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicCommercialPriceId, buyerUser, 5);

        final String newAcquisitionOrderId = newAcquisitionOrder.getId();

        // process it for pending
        newAcquisitionOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, newAcquisitionOrderId);

        // get subscription Id
        final String subscriptionIdNewAcquisition = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap1 = new HashMap<>();
        subscriptionQuantityMap1.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdNewAcquisition);
        subscriptionQuantityMap1.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap1.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddSeats1 = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap1), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaserOrderIdForAddSeats1 = purchaseOrderForAddSeats1.getId();

        // process the purchase order to 'pending' state
        purchaseOrderForAddSeats1 =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaserOrderIdForAddSeats1);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeats1 = purchaseOrderForAddSeats1.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        AssertCollector.assertThat("Subscription ID for Add Seats and New Acquisition should not be same",
            subscriptionIdNewAcquisition, not(subscriptionIdAddSeats1), assertionErrorList);

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap2 = new HashMap<>();
        subscriptionQuantityMap2.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdAddSeats1);
        subscriptionQuantityMap2.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap2.put(LineItemParams.QUANTITY.getValue(), "3");

        PurchaseOrder purchaseOrderForAddSeats2 = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap2), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaseOrderIdForAddSeats2 = purchaseOrderForAddSeats2.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForAddSeats2);

        purchaseOrderForAddSeats2 =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForAddSeats2);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeat2 = purchaseOrderForAddSeats2.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap3 = new HashMap<>();
        subscriptionQuantityMap3.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdAddSeat2);
        subscriptionQuantityMap3.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap3.put(LineItemParams.QUANTITY.getValue(), "3");

        PurchaseOrder purchaseOrderForAddSeats3 = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap3), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaseOrderIdForAddSeats3 = purchaseOrderForAddSeats3.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForAddSeats3);

        // process the purchase order to 'pending' state
        purchaseOrderForAddSeats3 =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderIdForAddSeats3);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeats3 = purchaseOrderForAddSeats3.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        AssertCollector.assertThat("Both Subscription Id should be same", subscriptionIdAddSeats3,
            equalTo(subscriptionIdAddSeat2), assertionErrorList);

        // Decline purchaseOrderForAddSeats.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaserOrderIdForAddSeats1);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdAddSeats1);
        AssertCollector.assertThat("Incorrect Subscription Status", subscriptionDetailPage.getStatus(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Memo", subscriptionDetailPage.getSubscriptionActivity().get(1).getMemo(),
            equalTo(String.format("Added %s seats.\nCreated subscription #%s", "3", subscriptionIdAddSeat2)),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity after Expiring Subscription",
            subscriptionDetailPage.getQuantity(), is(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Activity after Decline",
            subscriptionDetailPage.getSubscriptionActivity().get(2).getActivity(), equalTo(Status.EXPIRED.toString()),
            assertionErrorList);

        // Navigate to Subscription Detail Page.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdAddSeat2);
        AssertCollector.assertThat("Incorrect Subscription Status", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity after Decline Add Seats Order",
            subscriptionDetailPage.getQuantity(), is(3), assertionErrorList);
        AssertCollector.assertThat("Incorrect Memo Message on Decline",
            subscriptionDetailPage.getSubscriptionActivity().get(2).getMemo(),
            equalTo("Reverted the add seats request."), assertionErrorList);
        AssertCollector.assertThat("Incorrect Purchase Order got Reverted",
            subscriptionDetailPage.getSubscriptionActivity().get(2).getPurchaseOrder(),
            equalTo(purchaseOrderIdForAddSeats3), assertionErrorList);
        AssertCollector.assertThat("Incorrect Activity captured for Decline",
            subscriptionDetailPage.getSubscriptionActivity().get(2).getActivity(),
            equalTo(PelicanConstants.ADD_SEATS_DECLINED), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify add seats successfully create purchase order for Different PriceId with Different Amount And
     * over writes next billing priceId with Active Price Id.
     */
    @Test
    public void testSuccessAddSeatsOnPriceIdChange() {
        // Create Subscription Plan

        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicOfferingId = bicOffering.getOfferings().get(0).getId();

        // Get Price Id associated with Subscription Plan.
        final String bicPriceId = bicOffering.getIncluded().getPrices().get(0).getId();

        // Submit and Process PO with above created price id.
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPriceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionIdForBicOrder =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicOrder, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), bicOfferingId),
            getEnvironmentVariables());
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), bicOfferingId),
            getEnvironmentVariables());

        final SubscriptionOfferPrice offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(450, getPricelistExternalKeyUs(), 0, 12);

        final SubscriptionOfferPrice activePrice =
            subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, offerPrice,
                bicOffering.getOfferings().get(0).getId(), bicOffering.getIncluded().getBillingPlans().get(0).getId());

        final String activePriceId = activePrice.getData().getId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicOrder);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), activePriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaserOrderIdForAddedSeats,
            ECStatus.ACCEPT);

        // Navigate to Subscription Detail Page
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForBicOrder);

        // Assert on Quantity after adding Seats to Original Subscription.
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(4), assertionErrorList);

        // Assert on next billing price id.
        AssertCollector.assertThat("Incorrect Next Billing Price Id ", subscriptionDetailPage.getNextBillingPriceId(),
            equalTo(activePriceId), assertionErrorList);

        final List<String> count =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + purchaserOrderIdForAddedSeats, "count(*)",
                getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("0"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests if quantity and quantity to reduce becomes equal by refunding a added seat then subscription
     * can be renewed with actual quantity. Steps include: - Create subscription with N qty. - Add one seat through
     * purchase order api in created subscription. Quantity will be N+1. - Reduce the quantity with N. - Refund the
     * added seat. Quantity and quantity to reduce will be equal to N. - Renew the subscription - Quantity to reduce
     * will be ignored and subscription will be renewed with N.
     *
     * @param priceId
     * @param quantity
     * @param addSeats
     * @param quantityToReduce
     */
    @Test(dataProvider = "subscriptionRenewalWithSameQuantityAndQuantityToReduce")
    public void testSubscriptionRenewalWithSameQuantityAndQuantityToReduceDefectBic9767(final String priceId,
        final int quantity, final int addSeats, final int quantityToReduce) {

        // create bic subscription
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, quantity);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        // create a purchaseOrder by adding a seat for a subscription which is created above
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), String.valueOf(addSeats));
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);
        final String purchaseOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderIdForAddedSeats,
            ECStatus.ACCEPT);
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrderIdForAddedSeats,
            ECStatus.ACCEPT);

        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat("Seats are not added in subscription: " + subscriptionId,
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity()),
            equalTo((PelicanConstants.ADD_SEATS)), assertionErrorList);

        // reduce the seat for a subscription
        reduceSeatsPage = subscriptionDetailPage.clickOnReduceSeatsLink();
        reduceSeatsPage.clickOnReduceSeats(String.valueOf(quantityToReduce), null);
        AssertCollector.assertThat("Seats are not reduced for subscription: " + subscriptionId,
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity()),
            equalTo((PelicanConstants.REDUCE_SEATS)), assertionErrorList);

        // refund the po of add seats
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.REFUND, purchaseOrderIdForAddedSeats,
            ECStatus.ACCEPT);
        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat("Add seats purchase order is not refunded",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity()),
            equalTo((OrderState.REFUNDED.toString())), assertionErrorList);
        AssertCollector.assertThat("Quantity and quantity to reduce are not same after refunding the po of add seats",
            subscriptionDetailPage.getQuantity(), equalTo(subscriptionDetailPage.getQuantityToReduce()),
            assertionErrorList);

        final List<String> subscriptionIdList = new ArrayList<>();
        subscriptionIdList.add(subscriptionId);
        purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIdList, false,
            PaymentType.CREDIT_CARD, null, true);

        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat("Quantity is not set to " + quantity, subscriptionDetailPage.getQuantity(),
            equalTo(quantity), assertionErrorList);
        AssertCollector.assertThat("Quantity to reduce is not set to 0 after renewal",
            subscriptionDetailPage.getQuantityToReduce(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Charge event is not captured for renewal purchase order",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity()),
            equalTo((PelicanConstants.CHARGE)), assertionErrorList);
        AssertCollector.assertThat("Renewal Purchase order is not created",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder()),
            not(equalTo((null))), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests memo for Add Seats to Added Subscription. Step1 : Submit New Acquisition PO with Recurring
     * promotion (subscription1). Step2 : Add Seats to (subscription1), it will create subscription2. Step3 : Add Seats
     * to subscription2. Step4 : Verify memo is created in subscription2, but not in subscription1, at Step3
     */
    @Test
    public void testAddSeatsToAddedSubscriptionDefectBIC9759() {

        // create promotion reference for recurring promotion.
        final LineItem.PromotionReferences promotionReferences = new LineItem.PromotionReferences();
        final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(nonStoreAmountDiscountPromoId);
        promotionReferences.setPromotionReference(promotionReference);

        // Create a purchase order with recurring promotion
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(bicCommercialPriceId, 5)), false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(bicCommercialPriceId, promotionReferences)), buyerUser);

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
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
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
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Navigate to Subscription Detail Page of original subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(originalSubscriptionId);

        // Assert on Quantity after adding Seats to Original Subscription.
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(5), assertionErrorList);

        int countOfMemo = 0;
        // Assert on Subscription Activity for Memo.
        for (final SubscriptionActivity activity : subscriptionDetailPage.getSubscriptionActivity()) {

            if (activity.getMemo().equals("Added 2 seats.\nCreated subscription #" + addSeatsSubscriptionId)) {
                countOfMemo++;
            }
        }
        AssertCollector.assertThat("Created subscription memo should be entered only once", countOfMemo, is(1),
            assertionErrorList);

        // Navigate to Added Seats Subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionId);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.", "2")), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(7), assertionErrorList);

        final List<String> count = DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderId,
            "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("0"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "subscriptionRenewalWithSameQuantityAndQuantityToReduce")
    private Object[][] getTestDataForSubscriptionRenewalWithSameQuantityAndQuantityToReduce() {
        return new Object[][] {
                // priceId, quantity, addSeats, quantityToReduce
                { getBicMonthlyUsPriceId(), 2, 1, 2 }, { getBicYearlyUsPriceId(), 1, 1, 1 }, };
    }

    /**
     * Test Method to verify CHARGEBACK for Add Seats. Scenario 1: Step1: Submit and Process New Acquisition with
     * Recurring Promo. Step 2: Submit and Process Add Seats Order, here add seats ll create new subscription.Step 3:
     * CHARGEBACK Add Seats Order. Step 4:ChargedBack Add Seats Order expires newly added subscription as quantity goes
     * to zero on CHARGED-BACK. Scenario 2: Step1: Submit and Process New Acquisition Order. Step 2: Submit and Process
     * Add Seats Order, Here add seats will increase subscription quantity. Step 3: CHARGED-BACK add seats order. Step
     * 4: Verify on CHARGED-BACK Add Seats Order, it will reduce quantity at subscription level.
     */
    @Test(dataProvider = "chargeBackScenario")
    public void testChargeBackForAddSeatsReducesSubscriptionQuantityOrExpiresSubscription(
        final boolean isRecurringPromo) {
        PurchaseOrder purchaseOrder;
        if (isRecurringPromo) {
            // create promotion reference for recurring promotion.
            final LineItem.PromotionReferences promotionReferences = new LineItem.PromotionReferences();
            final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
            promotionReference.setId(nonStoreAmountDiscountPromoId);
            promotionReferences.setPromotionReference(promotionReference);

            // Create a purchase order with recurring promotion
            purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
                new HashMap<>(ImmutableMap.of(bicCommercialPriceId, 5)), false, Payment.PaymentType.CREDIT_CARD,
                new HashMap<>(ImmutableMap.of(bicCommercialPriceId, promotionReferences)), buyerUser);

            // get PurchaseOrder Id and Process it for PENDING & CHARGE.
            final String purchaseOrderId = purchaseOrder.getId();

            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
            purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        } else {
            final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
            priceQuantityMap.put(bicCommercialPriceId, 5);
            purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap,
                false, buyerUser);

        }

        // get subscription Id.
        final String originalSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Add Seats Map containing Subscription Id, Price Id and Quantity.
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING and CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Process Add Seats Order for CHARGEBACK.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, addSeatsPurchaseOrderId);

        // Navigate to Added Seats Subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionId);

        if (!isRecurringPromo) {
            // Assert on Subscription Activity for Memo.
            AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
                equalTo(String.format("Charged-back PO #%s.\nReduced %s seats.", addSeatsPurchaseOrderId, "2")),
                assertionErrorList);

            // Assert on Subscription Activity for State.
            AssertCollector.assertThat("Correct State is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
                equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);

            AssertCollector
                .assertThat(String.format("Incorrect Subscription Quantity After processing PO %S for CHARGEBACK",
                    addSeatsPurchaseOrderId), subscriptionDetailPage.getQuantity(), is(5), assertionErrorList);
        } else {
            // Assert on Subscription Status.
            AssertCollector.assertThat(
                String.format("Incorrect Subscription Status for subscription %s", addSeatsSubscriptionId),
                subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify add seats with Zero amount Order Adds Seat on Existing subscription. And Captures
     * Subscription activity for add seats.
     */
    @Test
    public void testAddSeatsWithZeroAmountOrderOnSameSubscription() {

        // Creating Non Store wide, Non Recurring, 100% Discount Promotion
        final JPromotion nonStoreWideNonRecurringPecentDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicCommercialOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "100", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 1, null, null);

        final String nonStoreWideNonRecurringAmountDiscountPromoId =
            nonStoreWideNonRecurringPecentDiscountPromo.getData().getId();

        // submit a purchase order to create a commercial subscription
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, 1);

        final PurchaseOrder newAcquisitionOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription and add it to list
        final String subscriptionIdForBicCommercial = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "1");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(),
            nonStoreWideNonRecurringAmountDiscountPromoId);

        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);
        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        // Skip Process Purchase Order For PENDING and process it directly for CHARGE.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // Navigate to Added Seats Subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForBicCommercial);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added %s seats.", "1")), assertionErrorList);
        AssertCollector.assertThat("Correct Amount Charged is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge(), equalTo("0.00 USD"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(2), assertionErrorList);
        AssertCollector.assertTrue("Incorrect Pending Payment Flag ",
            subscriptionDetailPage.getPendingPaymentFlag().equalsIgnoreCase(PelicanConstants.FALSE),
            assertionErrorList);
        final List<String> count = DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderId,
            "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("0"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify add seats with Zero amount Order Adds Seat on Newly created subscription. And Captures
     * Subscription activity for add seats.
     */
    @Test
    public void testAddSeatsWithZeroAmountOrderWithNewlyCreatedSubscription() {

        // Creating Non Store wide, Recurring, 100% Discount Promotion.
        final JPromotion nonStoreWideRecurringPercentDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicCommercialOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "100", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final String nonStoreWideRecurringAmountDiscountPromoId =
            nonStoreWideRecurringPercentDiscountPromo.getData().getId();

        // submit a purchase order to create a commercial subscription
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, 1);

        final PurchaseOrder newAcquisitionOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription and add it to list
        final String subscriptionIdForBicCommercial = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), bicCommercialPriceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "1");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(),
            nonStoreWideRecurringAmountDiscountPromoId);

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);
        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        // Skip Process Purchase Order For PENDING and process it directly for CHARGE.

        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // Navigate to Added Seats Subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(addSeatsSubscriptionId);

        // Assert on Subscription Activity for Memo.
        AssertCollector.assertThat("Field Added to Subscription Id is incorrect",
            subscriptionDetailPage.getAddedToSubscriptionId(), equalTo(subscriptionIdForBicCommercial),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Quantity After adding Seats ",
            subscriptionDetailPage.getQuantity(), is(1), assertionErrorList);

        // Navigate to Original Subscription
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForBicCommercial);
        AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("Added 1 seats.\nCreated subscription #%s", addSeatsSubscriptionId)),
            assertionErrorList);
        AssertCollector.assertThat("Correct Amount Charged is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge(), equalTo("0.00 USD"),
            assertionErrorList);
        AssertCollector.assertThat("Correct Purchase Order is not captured under Subscription Activity",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
            equalTo(addSeatsPurchaseOrderId), assertionErrorList);

        final List<String> count = DbUtils.selectQuery(PelicanDbConstants.SELECT_COUNT_OF_POS + addSeatsPurchaseOrderId,
            "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect number of subscriptions", count.get(0), equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "chargeBackScenario")
    private Object[][] getChargeBackScenario() {
        return new Object[][] {
                // recurringPromo
                { true }, { false } };
    }

}
