package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class verifies the purchase Order Page in Admin Tool to check the Order Details are displayed
 *
 * @author t_mohag
 */
public class OrderDetailsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private JobsClient jobsResource;
    private JPromotion nonStoreWidePercentDiscountPromo;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDetailsTest.class.getSimpleName());
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Creating Regular Promotions for BIC subscriptions Store wide % Discount
        nonStoreWidePercentDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(getBicSubscriptionPlan()), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "10", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 1, null, null);
        LOGGER.info("storeWidePercentDiscountPromo id :" + nonStoreWidePercentDiscountPromo.getData().getId());
        // Add to Map
        final Map<String, JPromotion> promotionsMap = new HashMap<>();
        promotionsMap.put(nonStoreWidePercentDiscountPromo.getData().getId(), nonStoreWidePercentDiscountPromo);
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Test to verify the PO page with order state as AUTHORIZE and displays NULL Invoice number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void emptyInvoiceNumberPurchaseOrderAuthorizeTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);

        // submitting purchase order with authorize command
        final PurchaseOrder authorizePurchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            authorizePurchaseOrder.getId());

        // getting the purchase order page
        findPurchaseOrdersPage.findPurchaseOrderById(authorizePurchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertTrue("Why is invoice number generated ?",
            purchaseOrderDetailPage.getInvoiceNumber().equals("-"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as PENDING and displays NULL Invoice number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void emptyInvoiceNumberPurchaseOrderPendingTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);

        // submitting purchase order with authorize command
        final PurchaseOrder authorizePurchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process purchase order with pending command
        final PurchaseOrder pendingPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, authorizePurchaseOrder.getId());

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            pendingPurchaseOrder.getId());

        // getting the purchase order page

        findPurchaseOrdersPage.findPurchaseOrderById(authorizePurchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertTrue("Why is invoice number generated ?",
            purchaseOrderDetailPage.getInvoiceNumber().equals("-"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as CHARGE and displays Invoice number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void invoiceNumberPurchaseOrderChargedTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);

        // submit purchase order with authorize command
        final PurchaseOrder purchaseOrderCreated = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);

        // process purchase order with pending and charge commands
        final PurchaseOrder pendingPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreated.getId());
        final PurchaseOrder chargedPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, pendingPurchaseOrder.getId());

        // running invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            chargedPurchaseOrder.getId());

        // getting the purchase order page
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderCreated.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Invoice number not generated", purchaseOrderDetailPage.getInvoiceNumber(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as REFUND and displays Invoice number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void invoiceNumberPurchaseOrderRefundTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);

        // submit purchase order with charge command
        final PurchaseOrder chargedPurchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);

        // running invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            chargedPurchaseOrder.getId());

        // getting purchase order with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(chargedPurchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Invoice number not generated", purchaseOrderDetailPage.getInvoiceNumber(),
            notNullValue(), assertionErrorList);
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();

        // process purchase order with refund command
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, chargedPurchaseOrder.getId());

        // getting the purchase order page with refunded command
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        purchaseOrderDetailPage.refreshPage();
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Invoice number changed", purchaseOrderDetailPage.getInvoiceNumber(),
            equalTo(invoiceNumber), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as CHARGE_BACK and displays Invoice number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void invoiceNumberPurchaseOrderChargeBackTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);

        // submit purchase order with charged command
        final PurchaseOrder chargedPurchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);

        // running invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            chargedPurchaseOrder.getId());

        // getting purchase order with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(chargedPurchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Invoice number not generated", purchaseOrderDetailPage.getInvoiceNumber(),
            notNullValue(), assertionErrorList);

        // process purchase order with charge_back command
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, chargedPurchaseOrder.getId());

        // getting purchase order page with charged back command
        purchaseOrderDetailPage.refreshPage();
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertThat("Invoice number changed", purchaseOrderDetailPage.getInvoiceNumber(),
            equalTo(invoiceNumber), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as REFUND, displays credit note number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void creditNoteNumberPurchaseOrderRefundTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUkPriceId(), 1);
        // submit purchase order
        final String chargedPurchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, false, buyerUser);
        // running invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, chargedPurchaseOrderId);

        // getting purchase order page with charge command

        findPurchaseOrdersPage.findPurchaseOrderById(chargedPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // process purchase order with refund command
        final PurchaseOrder refundedPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, chargedPurchaseOrderId);

        // getting the purchase order page with refunded command
        purchaseOrderDetailPage.refreshPage();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Credit note number is not correct", purchaseOrderDetailPage.getCreditNoteNumber(),
            equalTo(refundedPurchaseOrder.getCreditNoteNumber()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page with order state as CHARGE_BACK, displays credit note number in Order Details
     *
     * @return order details on PO page
     */
    @Test
    public void creditNoteNumberPurchaseOrderChargeBackTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        // submit purchase order
        final String chargedPurchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, false, buyerUser);

        // process purchase order with charge_back command
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final PurchaseOrder chargedBackPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, chargedPurchaseOrderId);

        // getting purchase order page with charged back command

        findPurchaseOrdersPage.findPurchaseOrderById(chargedBackPurchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertThat("Credit note number is not correct", purchaseOrderDetailPage.getCreditNoteNumber(),
            equalTo(chargedBackPurchaseOrder.getCreditNoteNumber()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page for renewal PO with order state as REFUND , displays credit note number in Order
     * Details
     *
     * @return order details on PO page
     */
    @Test
    public void creditNoteNumberRenewalPurchaseOrderRefundTest() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);
        // submit purchase order
        final PurchaseOrder chargedPurchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);

        // getting subscription id for bic offering
        final String subscriptionId = chargedPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        // submit renewal purchase order
        final String renewalPurchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.PAYPAL, null,
            Lists.newArrayList(subscriptionId), false, true, buyerUser);

        // process renewal purchase order with refund command
        final PurchaseOrder refundedPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, renewalPurchaseOrderId);
        // getting the purchase order page with refunded command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect order state returned", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Credit note number is not correct", purchaseOrderDetailPage.getCreditNoteNumber(),
            equalTo(refundedPurchaseOrder.getCreditNoteNumber()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page for Add Seats PO with order state as SUBSCRIPTION_QUANTITY , displays all details
     * correctly.
     *
     * @return order details on PO page
     */
    @Test
    public void testProcessAddSeatsOrderPopulatesCorrectDetail() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
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
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");
        subscriptionQuantityMap.put("promotionIds", nonStoreWidePercentDiscountPromo.getData().getId());

        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);
        // Processing for PENDING Order State.
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeats.getId());

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderForAddedSeats.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect order type", purchaseOrderDetailPage.getOrderType(),
            equalTo(PelicanConstants.ADD_SEATS_ORDER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Status", purchaseOrderDetailPage.getFulfillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Id",
            purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1), equalTo(subscriptionIdForBicCommercial),
            assertionErrorList);

        // Assert on Proration Days.
        final int prorationDays = (int) DateTimeUtils.getDaysInBillingCycle(
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getProrationStartDate(),
            purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0).getSubscriptionQuantity()
                .getSubscriptionQuantityRequest().getProrationEndDate());
        AssertCollector.assertThat("Incorrect Proration Days", purchaseOrderDetailPage.getLineItemsProrationDays(1),
            equalTo(String.format("%s", prorationDays)), assertionErrorList);

        // Assert on Unit Price.
        final String unitPrice = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getUnitPrice();
        AssertCollector.assertThat("Incorrect Unit Price",
            purchaseOrderDetailPage.getLineItemsUnitPrice(1).split(" ")[0], equalTo(unitPrice), assertionErrorList);

        // Assert on Discounted Price.
        final float discountPrice = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getPromotionDiscount();
        AssertCollector.assertThat("Incorrect Discounted Price",
            purchaseOrderDetailPage.getLineItemsPromoDiscounts(1).split(" ")[0],
            equalTo(String.format("%.2f", discountPrice)), assertionErrorList);

        // Assert on SubTotal Price.
        final float subTotalPrice = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getChargeDetails().getAmountCharged();
        AssertCollector.assertThat("Incorrect SubTotal Price: ",
            purchaseOrderDetailPage.getLineItemsSubTotal(1).split(" ")[0],
            equalTo(String.format("%.2f", subTotalPrice)), assertionErrorList);

        // Assert on Total Amount Charged.
        final String totalAmountCharge = purchaseOrderForAddedSeats.getTransactions().getTransactions().get(0)
            .getGatewayResponse().getAmountCharged();
        AssertCollector.assertThat("Incorrect SubTotal Price: ",
            purchaseOrderDetailPage.getTotalAmountOrder().split(":")[1].trim().split(" ")[0],
            equalTo(totalAmountCharge), assertionErrorList);

        // Assert on Fulfillment Group:
        AssertCollector.assertThat("Incorrect Fulfillment Group Id:", purchaseOrderDetailPage.getFulfillmentGroupId(1),
            equalTo(purchaseOrderForAddedSeats.getFulFillmentGroups().getFulfillmentGroups().get(0).getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Group Strategy:",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(purchaseOrderForAddedSeats
                .getFulFillmentGroups().getFulfillmentGroups().get(0).getStrategy().toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Group Status:",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1),
            equalTo(
                purchaseOrderForAddedSeats.getFulFillmentGroups().getFulfillmentGroups().get(0).getStatus().toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the record of ec status in transactiontable at time of Pending and Charge.
     */
    @Test(dataProvider = "finalEcStatuses")
    public void testEcStatusesInTransactionTable(final ECStatus finalEcStatusAtPending,
        final ECStatus finalEcStatusAtCharge) {

        LOGGER.info("Submitting a Purchase Order");
        final PurchaseOrder authorizedPoWithUnverified = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);
        final String poId = authorizedPoWithUnverified.getId();

        // process purchase order with pending command and check final ec status in transaction table.
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, poId, finalEcStatusAtPending);
        findPurchaseOrdersPage.findPurchaseOrderById(poId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect EC status in Transaction Table in Pending State",
            purchaseOrderDetailPage.getTransactionEcStatus(3), equalTo(finalEcStatusAtPending.getName()),
            assertionErrorList);
        AssertCollector.assertThat("Date is null in Transaction Table in Pending State",
            purchaseOrderDetailPage.getTransactionDate(3), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Id is null in Transaction Table in Pending State",
            purchaseOrderDetailPage.getTransactionId(3), notNullValue(), assertionErrorList);

        // process purchase order with charge command and check final ec status in transaction table.
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, poId, finalEcStatusAtCharge);
        purchaseOrderDetailPage.refreshPage();

        AssertCollector.assertThat("Incorrect EC status in Transaction Table in Charge State",
            purchaseOrderDetailPage.getTransactionEcStatus(4), equalTo(finalEcStatusAtCharge.getName()),
            assertionErrorList);
        AssertCollector.assertThat("Date is null in Transaction Table in Charge State",
            purchaseOrderDetailPage.getTransactionDate(4), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Id is null in Transaction Table in Charge State",
            purchaseOrderDetailPage.getTransactionId(4), notNullValue(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * data to test final ec statuses in transaction table on Po details Page
     */
    @DataProvider(name = "finalEcStatuses")
    private Object[][] getTestData() {
        return new Object[][] { { ECStatus.UNVERIFIED, ECStatus.REVIEW },

                { ECStatus.REVIEW, ECStatus.BLOCK },

                { ECStatus.REOPEN, ECStatus.ACCEPT },

                { ECStatus.ACCEPT, ECStatus.BLOCK }, { ECStatus.HARDBLOCK, ECStatus.ACCEPT } };
    }

    /**
     * This test case checks Authorize Price is honored for New Acquisition and Auto Renewal Orders Step1 : Place New
     * Acquisition order in AUTH state Step2 : Assert on total value in transaction field values Step3 : Expire the
     * current price and add a new one Step4 : Process the order to PENDING Step5 : Assert on total value, that it is
     * same as of AUTH value Step6 : Process the order to CHARGE Step7 : Assert on total value, that it is same as of
     * AUTH value Step8 : Submit a Renewal Order in AUTH state Step9 : Assert on total value in transaction field values
     * Step10 : Expire the current price and add a new one Step11 : Process the order to PENDING Step12 : Assert on
     * total value, that it is same as of AUTH value Step13 : Process the order to CHARGE Step14 : Assert on total
     * value, that it is same as of AUTH value
     *
     */
    @Test
    public void testAuthorizePriceIsHonoredForNewAcquisitionAndRenewalWithPriceExpiration() {

        final Offerings offerings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // Get the price id of the offering sent from data provider.
        final String priceId = offerings.getIncluded().getPrices().get(0).getId();

        // Submit New Acquisition order
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 4).getId();

        // Get Price Amount
        final String priceAmount =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE + priceId,
                PelicanDbConstants.AMOUNT_DB_FIELD, getEnvironmentVariables()).get(0);

        // Calculate Total Amount and format it
        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String totalAmount =
            formatter.format(Double.parseDouble(priceAmount) * 4).substring(1) + " " + Currency.USD.toString();

        // Assertion on AUTH state and total amount
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Authorize transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(1), equalTo(totalAmount), assertionErrorList);

        final String offeringId = offerings.getOfferings().get(0).getId();
        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());

        // Add New Price
        SubscriptionOfferPrice offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(600, getPricelistExternalKeyUs(), 0, 12);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, offerPrice, offeringId,
            offerings.getIncluded().getBillingPlans().get(0).getId());

        // Process the PO to PENDING and related assertions
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderDetailPage.refreshPage();
        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Pending transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(3), equalTo(totalAmount), assertionErrorList);

        // Process the PO to CHARGE and related assertions
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        if (offerings.getOfferings().get(0).getOfferingType() == OfferingType.META_SUBSCRIPTION) {
            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        }

        purchaseOrderDetailPage.refreshPage();
        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);
        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Charge transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(4), equalTo(totalAmount), assertionErrorList);

        // Submit Renewal Order in AUTH
        final PurchaseOrder renewalPurchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.CREDIT_CARD,
                subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);

        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();
        // Total calculation
        totalAmount = formatter.format(600 * 4).substring(1) + " " + Currency.USD.toString();

        // Assertions on AUTH
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage1 = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect Order State PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Authorize transaction amount for PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getTransactionAmount(1), equalTo(totalAmount), assertionErrorList);

        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());

        offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 0, 12);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, offerPrice, offeringId,
            offerings.getIncluded().getBillingPlans().get(0).getId());

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);
        purchaseOrderDetailPage1.refreshPage();
        AssertCollector.assertThat("Incorrect Order State PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Pending transaction amount for PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getTransactionAmount(3), equalTo(totalAmount), assertionErrorList);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPurchaseOrderId);
        purchaseOrderDetailPage1.refreshPage();
        AssertCollector.assertThat("Incorrect Order State PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Charge transaction amount for PO : " + renewalPurchaseOrderId,
            purchaseOrderDetailPage1.getTransactionAmount(4), equalTo(totalAmount), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests price at AUTH is honored for Add Seats order when next billing date is changed Step1 : Submit
     * New Acquisition order Step2 : Submit Add Seats order in AUTH Step3 : Edit next billing date Step4 : Process add
     * seats order to Pending Step5 : Verify that price is same as of AUTH
     */
    @Test
    public void testAuthorizePriceIsHonoredForAddSeatsOrderWithNBDChange() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicYearlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String purchaseOrderId = purchaseOrderForAddedSeats.getId();

        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        final String totalAmount =
            formatter.format(Double.parseDouble(purchaseOrderForAddedSeats.getTransactions().getTransactions().get(0)
                .getGatewayResponse().getAmountCharged())).substring(1) + " " + Currency.USD.toString();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Authorize transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(1), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subtotal value for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getLineItemsSubTotal(1), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Total Order Amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTotalAmountOrder().split(": ")[1], equalTo(totalAmount), assertionErrorList);

        // get one date to change Subscriptions next billing date
        String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(25),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Processing for PENDING Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeats.getId());

        purchaseOrderDetailPage.refreshPage();

        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Pending transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(3), equalTo(totalAmount), assertionErrorList);

        // get one date to change Subscriptions next billing date
        changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(20),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        // Processing for CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForAddedSeats.getId());

        purchaseOrderDetailPage.refreshPage();

        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Charge transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(4), equalTo(totalAmount), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests price at AUTH is honored for Add Seats order when price is changed Step1 : Submit New
     * Acquisition order Step2 : Submit Add Seats order in AUTH Step3 : Expire the price and add new price Step4 :
     * Process add seats order to Pending Step5 : Verify that price is same as of AUTH
     */
    @Test
    public void testAuthorizePriceIsHonoredForAddSeatsOrderWhenPriceExpired() {

        // Create a separate offering, so that canceling the price can be done, without affecting other classes
        final Offerings offering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String purchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Get Price Amount
        final String priceAmount =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE + priceId,
                PelicanDbConstants.AMOUNT_DB_FIELD, getEnvironmentVariables()).get(0);

        // Calculate Total Amount and format it
        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        final String unitPrice =
            formatter.format(Double.parseDouble(priceAmount)).substring(1) + " " + Currency.USD.toString();
        final String totalAmount =
            formatter.format(Double.parseDouble(priceAmount) * 3).substring(1) + " " + Currency.USD.toString();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.AUTHORIZED, totalAmount, unitPrice, 1, assertionErrorList);

        final String offeringId = offering.getOfferings().get(0).getId();

        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());

        final SubscriptionOfferPrice offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 0, 12);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, offerPrice, offeringId,
            offering.getIncluded().getBillingPlans().get(0).getId());

        // Processing for PENDING Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.PENDING, totalAmount, unitPrice, 3, assertionErrorList);

        // Processing for CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.CHARGED, totalAmount, unitPrice, 4, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests price at Authorize is honored for Subscription Extension Order when Price is expired Step1 :
     * Submit New Acquisition order Step2 : Submit Extension order in AUTH Step3 : Expire the price and add new price
     * Step4 : Process extension order to Pending Step5 : Verify that price is same as of AUTH
     */
    @Test
    public void testAuthorizePriceIsHonoredForSubscriptionExtensionOrder() {

        // Create a separate offering, so that canceling the price can be done, without affecting other classes
        final Offerings offering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceId, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.

        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(ImmutableList.of(priceId)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7))));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrderForSubscriptionExtension =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        final String extensionPurchaseOrderId = purchaseOrderForSubscriptionExtension.getId();

        // Calculate total amount at AUTH
        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        final String totalAmount = formatter.format(Double.parseDouble(purchaseOrderForSubscriptionExtension
            .getTransactions().getTransactions().get(0).getGatewayResponse().getAmountCharged())).substring(1) + " "
            + Currency.USD.toString();

        findPurchaseOrdersPage.findPurchaseOrderById(extensionPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect Order State PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Authorize transaction amount for PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(1), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subtotal value for PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getLineItemsSubTotal(1), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Total Order Amount for PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getTotalAmountOrder().split(": ")[1], equalTo(totalAmount), assertionErrorList);

        final String offeringId = offering.getOfferings().get(0).getId();
        // Changing DB to expire Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_START_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_PRICE_ID_END_DATE,
                DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT), offeringId),
            getEnvironmentVariables());

        // Add New Price
        final SubscriptionOfferPrice offerPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(600, getPricelistExternalKeyUs(), 0, 12);

        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, offerPrice, offeringId,
            offering.getIncluded().getBillingPlans().get(0).getId());

        // Process the PO to PENDING and related assertions
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, extensionPurchaseOrderId);
        purchaseOrderDetailPage.refreshPage();
        AssertCollector.assertThat("Incorrect Order State PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Pending transaction amount for PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(3), equalTo(totalAmount), assertionErrorList);

        // Process the PO to CHARGE and related assertions
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, extensionPurchaseOrderId);
        purchaseOrderDetailPage.refreshPage();

        AssertCollector.assertThat("Incorrect Order State PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Charge transaction amount for PO : " + extensionPurchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(4), equalTo(totalAmount), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This tests that price edit/update while Purchase Order is in Auth, will not affect the CHARGE amount. Step1 :
     * Place new acquisition order in AUTH with amount x. Step2 : Update the price to x+20 by updating DB. Step3 :
     * Process the PO to PENDING and CHARGE. Step4 : Verify that unit price, total price, transaction amount is not
     * changed and AUTH price x is honored.
     */
    @Test
    public void testAuthorizePriceIsHonoredForNewAcquisitionWithPriceUpdate() {

        final Offerings offerings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offerings.getIncluded().getPrices().get(0).getId();

        // Submit New Acquisition order
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 2).getId();

        // Get Price Amount
        final String priceAmount =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE + priceId,
                PelicanDbConstants.AMOUNT_DB_FIELD, getEnvironmentVariables()).get(0);

        // Calculate Total Amount and format it
        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        final String unitPrice =
            formatter.format(Double.parseDouble(priceAmount)).substring(1) + " " + Currency.USD.toString();
        final String totalAmount =
            formatter.format(Double.parseDouble(priceAmount) * 2).substring(1) + " " + Currency.USD.toString();

        // Assertion on AUTH state and total amount
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.AUTHORIZED, totalAmount, unitPrice, 1, assertionErrorList);

        final Double updatedPrice1 = Double.parseDouble(priceAmount) + 20;
        // Changing DB to update Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_PRICE_AMOUNT, String.valueOf(updatedPrice1), priceId),
            getEnvironmentVariables());

        // Process the PO to PENDING and related assertions
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.PENDING, totalAmount, unitPrice, 3, assertionErrorList);

        // Process the PO to CHARGE and related assertions
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        PurchaseOrderHelper.commonAssertionsForPurchaseOrderPage(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.CHARGED, totalAmount, unitPrice, 4, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
