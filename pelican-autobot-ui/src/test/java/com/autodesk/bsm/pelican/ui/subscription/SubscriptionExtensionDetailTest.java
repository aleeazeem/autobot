package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * This test class verifies the purchase Order Page in Admin Tool for Subscription Extension Orders.
 *
 * @author t_joshv
 */
public class SubscriptionExtensionDetailTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private SubscriptionDetailPage subscriptionDetailPage;
    private FindSubscriptionsPage findSubscriptionPage;
    private FindPurchaseOrdersPage findPurchaseOrderPage;
    private BuyerUser buyerUser;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionExtensionDetailTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        findPurchaseOrderPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

    }

    /**
     * Method to verify Decline Subscription Extension Order sets subscription with Next Billing Date to previous
     * NBD,Pending Payment Flag to False And Captures Decline Activity.Step 1: submit New Acquisition Order for BIC and
     * Meta subscription. Step 2: Submit Subscription Extension Order. Step 3: Process it For DECLINE after AUTH or
     * PENDING according to Data Provider..
     *
     * @throws ParseException
     * @throws NumberFormatException
     * @throws IOException
     */
    @Test(dataProvider = "declinedExtensionOrder")
    public void testSuccessDeclineSubscriptionExtensionOrder(final List<String> subscriptionIds,
        final OrderState orderState, final int transactionIndex, final int transactionIndexForOrder)
        throws NumberFormatException, ParseException, IOException {

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIds);
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Subscription Extension Purchase Order
        PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.PAYPAL, PurchaseOrder.OrderCommand.AUTHORIZE, new ArrayList<>(ImmutableList.of("10", "11")),
                buyerUser);

        if (orderState == OrderState.PENDING) {
            // Processing for PENDING Order State.
            subscriptionExtensionOrder =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrder.getId());
        }

        // Processing for DECLINE Order State.
        subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, subscriptionExtensionOrder.getId());

        // Navigate to Subscription Detail page.
        for (int i = 0; i < subscriptionIds.size(); i++) {

            // Navigate to Subscription Detail Page
            final String SubscriptionId = subscriptionMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()).get(i);

            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(SubscriptionId);

            // Assert on Subscription Activity for Purchase Order.
            AssertCollector.assertTrue("Incorrect Pending Payment Flag",
                subscriptionDetailPage.getPendingPaymentFlag().equalsIgnoreCase(PelicanConstants.FALSE),
                assertionErrorList);

            // Assert on Subscription Activity for Purchase Order.
            AssertCollector.assertThat("Incorrect Purchase Order captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
                equalTo(subscriptionExtensionOrder.getId()), assertionErrorList);

            // Assert on Subscription Activity Type.
            AssertCollector.assertThat("Correct Activity Type is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
                equalTo(PelicanConstants.EXTENSION_DECLINED), assertionErrorList);

            // verify subscription event api for Extension activity
            final JSubscriptionEvents subscriptionEvents =
                resource.getSubscriptionEventsClient().getSubscriptionEvents(SubscriptionId, null);
            final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
            AssertCollector.assertThat(
                "Activity is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getEventType(), equalTo(PelicanConstants.EXTENSION_DECLINED),
                assertionErrorList);
            AssertCollector.assertThat(
                "Requestor is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getRequesterName(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat(
                "Purchase order is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getPurchaseOrderId(), equalTo(subscriptionExtensionOrder.getId()),
                assertionErrorList);
            AssertCollector.assertThat(
                "Memo is not correct for for get subscription api subscription id " + SubscriptionId,
                subscriptionEventsData.getMemo(), equalTo(null), assertionErrorList);
        }
        // Assertion on Purchase Order Detail Page.

        findPurchaseOrderPage.findPurchaseOrderById(subscriptionExtensionOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrderPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);

        // Assert on Transaction Activity.
        AssertCollector.assertTrue("Incorrect Transactions activity state", purchaseOrderDetailPage
            .getTransactionState(transactionIndex).equalsIgnoreCase(OrderState.DECLINED.toString()),
            assertionErrorList);

        AssertCollector
            .assertThat("Incorrect Transactions activity for Amount",
                purchaseOrderDetailPage.getTransactionAmount(transactionIndex).split(" ")[0],
                equalTo(
                    String.format("%,.2f",
                        Double.parseDouble(subscriptionExtensionOrder.getTransactions().getTransactions()
                            .get(transactionIndexForOrder).getGatewayResponse().getAmountCharged()))),
                assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Process Subscription Extension Order Updates subscription with Next Billing Date, Step 1: submit
     * New Acquisition Order for BIC and Meta subscription. Step 2: for created subscription submit Subscription
     * extension order.Step 3: Process Extension Order for PENDING & CHARGE. Step 4: Verify for NBD set to Target
     * Renewal Date, Pending Payment Flag set to False.
     *
     * @throws ParseException
     * @throws NumberFormatException
     * @throws IOException
     */
    @Test(dataProvider = "paymentTypeForExtensionOrder")
    public void testSuccessProcessSubscriptionExtensionOrder(final List<String> subscriptionIds,
        final Payment.PaymentType paymentType) throws NumberFormatException, ParseException, IOException {

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIds);
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Subscription Extension Purchase Order
        PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap, paymentType,
                PurchaseOrder.OrderCommand.AUTHORIZE, new ArrayList<>(ImmutableList.of("10", "11")), buyerUser);

        // Processing for PENDING Order State.
        if (paymentType == PaymentType.PAYPAL) {
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, subscriptionExtensionOrder.getId());
        } else {

            subscriptionExtensionOrder =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrder.getId());
        }

        // Processing for CHARGE Order State.
        subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrder.getId());

        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();
        // Check Next Billing Updated with Renewal Target Date.
        for (int i = 0; i < 2; i++) {

            // Navigate to Subscription Detail Page
            final String SubscriptionId = subscriptionMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()).get(i);
            final String expectedNBD = subscriptionMap.get(LineItemParams.TARGET_RENEWAL_DATE.getValue()).get(i);
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(SubscriptionId);

            // Assert on NBD for subscription.
            AssertCollector.assertThat("Incorrect Next Billing Date for Subscription",
                subscriptionDetailPage.getNextBillingDate().split(" ")[0], equalTo(expectedNBD), assertionErrorList);

            // Assert on Subscription Activity for Purchase Order.
            AssertCollector.assertTrue("Incorrect Pending Payment Flag",
                subscriptionDetailPage.getPendingPaymentFlag().equalsIgnoreCase(PelicanConstants.FALSE),
                assertionErrorList);

            // Assert on Subscription Activity for Purchase Order.
            AssertCollector.assertThat("Incorrect Purchase Order captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder(),
                equalTo(subscriptionExtensionOrderId), assertionErrorList);

            // Assert on Subscription Activity for Grant Days.
            AssertCollector.assertThat("Correct Grant days is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(String.format("%d Days",
                    (subscriptionExtensionOrder.getLineItems().getLineItems().get(i).getSubscriptionExtension()
                        .getSubscriptionExtensionResponse().getChargeDetails().getProrationDays()))),
                assertionErrorList);

            // Assert on Subscription Activity for Amount Charged.
            AssertCollector.assertThat("Correct Total Amount Charged is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge().split(" ")[0],
                equalTo((String.format("%,.2f",
                    (subscriptionExtensionOrder.getLineItems().getLineItems().get(i).getSubscriptionExtension()
                        .getSubscriptionExtensionResponse().getChargeDetails().getAmountCharged())))),
                assertionErrorList);

            // Assert on Subscription Activity Type.
            AssertCollector.assertThat("Correct Activity Type is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity(),
                equalTo(PelicanConstants.EXTENSION), assertionErrorList);

            final String nextBillingDate = subscriptionExtensionOrder.getLineItems().getLineItems().get(i)
                .getSubscriptionExtension().getSubscriptionExtensionRequest().getSubscriptionRenewalDate();
            final String expectedMemo =
                String.format("Next billing date was changed from %s to %s", nextBillingDate, expectedNBD);
            // Assert on Subscription Activity Memo.
            AssertCollector.assertThat("Correct Memo is not captured under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(), equalTo(expectedMemo),
                assertionErrorList);

            // verify subscription event api for Extension activity
            final JSubscriptionEvents subscriptionEvents =
                resource.getSubscriptionEventsClient().getSubscriptionEvents(SubscriptionId, null);
            final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
            AssertCollector.assertThat(
                "Activity is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getEventType(), equalTo(PelicanConstants.EXTENSION), assertionErrorList);
            AssertCollector.assertThat(
                "Requestor is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getRequesterName(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat(
                "Purchase order is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getPurchaseOrderId(), equalTo(subscriptionExtensionOrderId), assertionErrorList);
            AssertCollector.assertThat(
                "Memo is not correct for get subscription api for subscription id " + SubscriptionId,
                subscriptionEventsData.getMemo(), equalTo(expectedMemo), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify Refunding (through REFUND or CHARGED-BACK)Extension Order before it approaches its Next Billing
     * Date. On Refund it will Reset NBD from Target Renewal Date to Subscription Renewal Date. Step 1: Place new
     * Acquisition Order. Step 2: Submit Extension Order, process it for PENDING & CHARGE. Step 3: Refund Extension
     * Order before its Original NBD. Step 3: verify subscription's NBD resets to its Original NBD from the Target
     * Renewal Date.
     */
    @Test(dataProvider = "orderTypeForExtensionOrder")
    public void testExtensionOrderRefundOrChargedBackBeforeNBDResetRenewalDate(final OrderCommand orderCommand,
        final String orderCommandForMemoActivity) {

        // Create a purchase order and get BIC subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get Meta subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 3)), null, true, true, buyerUser);

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // HashMap to save Next Billing Date for each subscription.
        final Map<String, String> subscriptionIdNextBillingDateMap = new HashMap<>();
        subscriptionIdNextBillingDateMap.put(subscriptionId1,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12));
        subscriptionIdNextBillingDateMap.put(subscriptionId2,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order for AUTH
        PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Process Extension Order for PENDING & CHARGE.
        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        // check NBD on subscription detail page. It should be Target Renewal Date.
        for (final String SubscriptionId : subscriptionIdNextBillingDateMap.keySet()) {
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(SubscriptionId);
            final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();
            AssertCollector.assertThat("Incorrect Next Billing Date on Subscription Detail Page",
                (DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13)),
                equalTo((nextBillingDate)), assertionErrorList);
        }
        // On Refund Or Charged-Back Next Billing Date resets to original NBD.
        subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(orderCommand, subscriptionExtensionOrderId);

        // check NBD on subscription detail page. It should reset to Subscription Renewal Date.
        for (final String SubscriptionId : subscriptionIdNextBillingDateMap.keySet()) {
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(SubscriptionId);
            AssertCollector.assertThat("Incorrect Next Billing Date on Subscription Detail Page",
                subscriptionDetailPage.getNextBillingDate(),
                equalTo((subscriptionIdNextBillingDateMap.get(SubscriptionId))), assertionErrorList);
            AssertCollector.assertTrue("Incorrect Subscription Status on Subscription Detail Page",
                subscriptionDetailPage.getStatus().equalsIgnoreCase(PelicanConstants.ACTIVE_STATUS),
                assertionErrorList);

            // Assert on Subscription Activity Memo for Reseting NBD.
            AssertCollector.assertThat("Subscription Activity Memo captured is incorrect",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
                equalTo(String.format("%s PO #%s.\nReset the next billing date from %s to %s",
                    orderCommandForMemoActivity, subscriptionExtensionOrderId,
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    subscriptionIdNextBillingDateMap.get(SubscriptionId))),
                assertionErrorList);

            // Assert on Purchase Order Transaction Activity on REFUND.
            findPurchaseOrderPage.findPurchaseOrderById(subscriptionExtensionOrderId);
            final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrderPage.clickOnSubmit();

            // Assert on Transaction Activity.
            AssertCollector.assertTrue("Incorrect Transactions activity state",
                (purchaseOrderDetailPage.getTransactionType(5).equalsIgnoreCase(orderCommand.toString())),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Transactions activity for Amount",
                purchaseOrderDetailPage.getTransactionAmount(5).split(" ")[0],
                equalTo(String.format("%,.2f", Double.parseDouble(subscriptionExtensionOrder.getTransactions()
                    .getTransactions().get(3).getGatewayResponse().getAmountCharged()))),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify Refunding(through REFUND or CHARGED-BACK) Extension Order after it approaches its Next Billing
     * Date. On Refund it will Expire the Subscription if Quantity goes to zero. Step 1: Place new Acquisition Order.
     * Step 2: Submit Extension Order, process it for PENDING & CHARGE. Step 3: Refund Extension Order after its
     * Original NBD. Step 4: Subscription is Expired on Quantity goes to zero.
     */
    @Test(dataProvider = "orderTypeForExtensionOrder")
    public void testExtensionOrderRefundOrChargedBackAfterNBDExpiresSubscriptionOnQuantityGoesZero(
        final OrderCommand orderCommand, final String orderCommandForMemoActivity) {

        // Create a purchase order and get BIC subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get Meta subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 3)), null, true, true, buyerUser);

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // final HashMap to save Next final Billing Date for final each subscription.
        final List<String> subscriptionIdList = new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order for AUTH.
        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Process Extension Order for PENDING & CHARGE.
        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        for (final String SubscriptionId : subscriptionIdList) {
            updateSubscriptionRenewalDateAndTargetRenewalDate(subscriptionExtensionOrderId, SubscriptionId,
                DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(2),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(5), PelicanConstants.DATE_FORMAT_WITH_SLASH,
                    PelicanConstants.DATE_FORMAT_WITH_SLASH));
        }

        // On Refund Or Charged-back Next Billing Date resets to original NBD.
        purchaseOrderUtils.processPurchaseOrder(orderCommand, subscriptionExtensionOrderId);

        // check NBD on subscription detail page.
        for (final String SubscriptionId : subscriptionIdList) {
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(SubscriptionId);

            AssertCollector.assertThat("Incorrect Subscription Status on Subscription Detail Page",
                subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify Refunding(through REFUND or CHARGED-BACK) Extension Order after approaches its Next Billing Date
     * with quantity left will not expire the Subscription. Step 1: Place new Acquisition Order. Step 2: Submit
     * Extension Order, process it for PENDING & CHARGE. Step 3: submit and process add seats order for BIC
     * subscription. Step 4: Refund Extension Order after its Original NBD. Step 5: Verify quantity reduces on
     * Subscription and subscription is still active for BIC and for META its Expired.
     */
    @Test(dataProvider = "orderTypeForExtensionOrder")
    public void testExtensionOrderRefundOrChargedBackAfterNBDWithAddSeatsReduceQuantity(final OrderCommand orderCommand,
        final String orderCommandForMemoActivity) {

        // Create a purchase order and get BIC subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get META subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 3)), null, true, true, buyerUser);

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create List for both the subscriptions.
        final List<String> subscriptionIdList = new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order for AUTH.
        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Process Extension Order for PENDING & CHARGE.
        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        // Submit Add Seats Order for bic subscription.
        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId1);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicYearlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, getBuyerUser());

        // Processing for PENDING Order State.
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForAddedSeats.getId());
        // Processing for CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForAddedSeats.getId());

        // Simulating the scenario where Subscription Renewal Date(Original NBD) is in Past and Target Renewal Date is
        // in Future by updating PO XML.
        // In Automation, We are placing New Acquisition and Extension On Same Day 2/1
        // Place New Acquisition : 2/1 , NBD : 3/1
        // Placing Extn Order on : 2/1 for TRD : 3/15
        // In Order to Simulate Scenario to be between NBD : 3/1 and TRD: 3/15
        // Updating Data base for PO XML with below query, where NBD is in Past and TRD is in future.
        // so after updating PO XML with below query, it will look like NBD : 1/30 and TRD : 2/6

        for (final String SubscriptionId : subscriptionIdList) {
            updateSubscriptionRenewalDateAndTargetRenewalDate(subscriptionExtensionOrderId, SubscriptionId,
                DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(2),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(5), PelicanConstants.DATE_FORMAT_WITH_SLASH,
                    PelicanConstants.DATE_FORMAT_WITH_SLASH));
        }

        // On Refund or Charged-back it Expires Subscription for Quantity equal to Zero or less.
        purchaseOrderUtils.processPurchaseOrder(orderCommand, subscriptionExtensionOrderId);

        // For Subscription with ADD SEATS should be Active as it has quantity left after Refunding Extension Order.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId1);

        AssertCollector.assertThat("Incorrect Subscription Status on Subscription Detail Page",
            subscriptionDetailPage.getStatus(), equalTo(Status.ACTIVE.toString()), assertionErrorList);

        AssertCollector.assertThat("Incorrect Subscription Quantity on Subscription Detail Page",
            subscriptionDetailPage.getQuantity(), equalTo(2), assertionErrorList);

        AssertCollector.assertThat("Incorrect Subscription NBD on Subscription Detail Page",
            subscriptionDetailPage.getNextBillingDate(),
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13)),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect Subscription Activity Memo on Subscription Detail Page",
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),
            equalTo(String.format("%s PO #%s.\nReduced %s seats.", orderCommandForMemoActivity,
                subscriptionExtensionOrderId, "4")),
            assertionErrorList);

        // For Meta Subscription , on REFUND/CHARGED-BACK quantity goes to Zero, Hence status should be Expired.
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId2);

        AssertCollector.assertThat("Incorrect Subscription Status on Subscription Detail Page",
            subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to receive payment type.
     */
    @DataProvider(name = "paymentTypeForExtensionOrder")
    public Object[][] getPaymentTypeForDeclinedExtensionOrder() {
        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId3 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId4 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        return new Object[][] {
                { new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)), PaymentType.CREDIT_CARD },
                { new ArrayList<>(ImmutableList.of(subscriptionId3, subscriptionId4)), PaymentType.PAYPAL } };
    }

    /**
     * DataProvider to receive DECLINED order state after AUTH & PENDING state.
     */
    @DataProvider(name = "declinedExtensionOrder")
    public Object[][] getTestDataForDeclinedExtensionOrder() {
        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        return new Object[][] {
                { new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)), OrderState.PENDING, 4, 2 },
                { new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)), OrderState.AUTHORIZED, 3, 1 } };
    }

    @DataProvider(name = "orderTypeForExtensionOrder")
    public Object[][] getOrderTypeForExtensionOrder() {
        return new Object[][] { { OrderCommand.REFUND, OrderState.REFUNDED.getValue() },
                { OrderCommand.CHARGEBACK, "Charged-back" } };
    }

    /**
     * Change Renewal Date and Target Renewal Date in PO XML.
     * <p>
     * Note:This is to simulate Renewal date in past and Target Renewal date in Future.
     */
    private void updateSubscriptionRenewalDateAndTargetRenewalDate(final String purchaseOrderId,
        final String subscriptionId, final String subscriptionRenewalDate, final String targetRenewalDate) {

        // Get Payload from DB.
        final String payload = DbUtils.selectQuery("select PAYLOAD from purchase_order where id = " + purchaseOrderId,
            "PAYLOAD", getEnvironmentVariables()).get(0);

        LOGGER.info("current payload: " + payload);

        // Set fulfillmentGroup status to Pending in payload
        final String updatedPayload =
            updatePayload(payload, subscriptionId, subscriptionRenewalDate, targetRenewalDate);

        LOGGER.info("updated payload: " + updatedPayload);

        // Update payload to DB.
        DbUtils.updateTableInDb("purchase_order", "PAYLOAD", "'" + updatedPayload + "'", "id", purchaseOrderId,
            getEnvironmentVariables());
    }

    /**
     * Update Purchase Order Payload
     *
     * @return updatedPayload
     */
    private static String updatePayload(final String payload, final String subscriptionId,
        final String subscriptionRenewalDate, final String targetRenewalDate) {
        try {
            final Document document = Util.loadXMLFromString(payload);
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final Node subscriptionExtensionRequestNode = (Node) xPath.evaluate(
                "/purchaseOrder/lineItems/lineItem/subscriptionExtension/subscriptionExtensionRequest[@subscriptionId='"
                    + subscriptionId + "']",
                document, XPathConstants.NODE);

            final NamedNodeMap attr = subscriptionExtensionRequestNode.getAttributes();
            final Node nodeAttr = attr.getNamedItem("subscriptionRenewalDate");
            nodeAttr.setTextContent(subscriptionRenewalDate);

            final Node nodeAttr1 = attr.getNamedItem("targetRenewalDate");
            nodeAttr1.setTextContent(targetRenewalDate);

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return payload;
    }
}
