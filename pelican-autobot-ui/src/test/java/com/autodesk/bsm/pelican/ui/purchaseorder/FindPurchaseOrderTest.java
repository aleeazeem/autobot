package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BuyerFieldsInDb;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.NamedPartyType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderSearchResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.core.Every;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Find or View Purchase Order Test in Admin Tool.
 *
 * @author Vineel.
 */
public class FindPurchaseOrderTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final String INITIAL_EC_STATUS = "Unknown";
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BuyerUser buyerUser;

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Finding Purchase Order By Id
     *
     * @result Creates a purchase order and finds the purchase order by its id
     */
    @Test
    public void testFindPurchaseOrderById() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        // process the purchase order to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect purchase order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Finding Purchase Order By Buyer
     *
     * @result Creates a purchase order and finds the purchase order by its buyer
     */
    @Test
    public void testFindPurchaseOrderByBuyer() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        // process the purchase order to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());
        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, null, null, buyerUser.getName(),
            NamedPartyType.getDefault(), null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);
        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Finding Purchase Order By Order State Authorized
     *
     * @result Creates a purchase order and finds the purchase order by order state as Authorized
     */
    @Test
    public void testFindPurchaseOrderByOrderStateAuthorized() {
        // Get purchase order for BIC with credit card
        purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(),
            buyerUser, 1);

        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, OrderState.AUTHORIZED.toString(), null,
            buyerUser.getName(), NamedPartyType.getDefault(), null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);

        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Finding Purchase Order By Order State Pending
     *
     * @result Creates a purchase order and finds the purchase order by order state as Pending
     */
    @Test
    public void testFindPurchaseOrderByOrderStatePending() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, OrderState.PENDING.toString(), null,
            buyerUser.getName(), NamedPartyType.getDefault(), null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);

        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Finding Purchase Order By Order State Declined
     *
     * @result Creates a purchase order and finds the purchase order by order state as Declined
     */
    @Test
    public void testFindPurchaseOrderByOrderStateDeclined() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'declined' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrder.getId());

        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, OrderState.DECLINED.toString(), null,
            buyerUser.getName(), NamedPartyType.getDefault(), null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);

        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Finding Purchase Order By Order State Refunded
     *
     * @result Creates a purchase order and finds the purchase order by order state as Refunded
     */
    @Test
    public void testFindPurchaseOrderByOrderStateRefunded() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        // process the purchase order to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());
        // process the purchase order to 'refunded' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrder.getId());

        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, OrderState.REFUNDED.toString(), null,
            buyerUser.getName(), NamedPartyType.getDefault(), null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);

        AssertCollector.assertThat("Incorrect purchase order buyer",
            purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", ""),
            equalTo(buyerUser.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order state", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will test whether the initial ec status fields is shown as "-" on the purchase order page and
     * final EC status is Accept.
     */

    @Test
    public void testECStatusValueOnPurchaseOrderDetailPage() {
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicYearlyUsPriceId(), buyerUser, 1);
        AssertCollector.assertThat("Incorrect ec status value returned in the submit po api response",
            purchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(INITIAL_EC_STATUS),
            assertionErrorList);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.ACCEPT);
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrder.getId(),
            ECStatus.ACCEPT);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        AssertCollector.assertThat("Incorrect ec status value returned in the submit po api response",
            purchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(INITIAL_EC_STATUS),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec status value returned in the submit po api response",
            purchaseOrder.getBuyerUser().getFinalExportControlStatus(), equalTo(ECStatus.ACCEPT.getName()),
            assertionErrorList);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrder.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        AssertCollector.assertThat("Initial EC status field value is not correct under buyer section on po page",
            purchaseOrderDetailPage.getInitialECStatus(), equalTo("-"), assertionErrorList);
        AssertCollector.assertThat("Final EC status field value is not correct under buyer section on po page",
            purchaseOrderDetailPage.getFinalECStatus(), equalTo(ECStatus.ACCEPT.getName()), assertionErrorList);
        AssertCollector.assertThat("EC status value is not correct under transactions on po page",
            purchaseOrderDetailPage.getTransactionEcStatus(4), equalTo(ECStatus.ACCEPT.getName()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Finding Purchase Order By Fulfillment Status Fulfilled
     *
     * @result Creates a purchase order and finds the purchase order by Fulfillment Status as Fulfilled.
     */
    @Test
    public void testFindPurchaseOrderByFulfillmentStatusFulfilled() {
        // Get purchase order for BIC with credit card
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(null, OrderState.PENDING.toString(),
            FulfillmentGroup.FulFillmentStatus.FULFILLED.toString(), buyerUser.getName(), NamedPartyType.getDefault(),
            null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(0);

        AssertCollector.assertThat("Incorrect purchase order buyer", buyerUser.getId(),
            equalTo(purchaseOrderDetailPage.getBuyer().split(" ")[1].replace("(", "").replace(")", "")),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order state", OrderState.PENDING.toString(),
            equalTo(purchaseOrderDetailPage.getOrderState()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status",
            FulfillmentGroup.FulFillmentStatus.FULFILLED.toString(),
            equalTo(purchaseOrderDetailPage.getFulfillmentStatus()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Method to verify Advanced Find for Add Seats returns Purchase Order of order type : Add Seats only. Create a Add
     * Seats order, just in case there was no Add Seats order.
     */
    @Test
    public void testAdvancedFindSearchForAddSeatsOrderType() {

        // submit a purchase order to create a commercial subscription
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription and add it to list
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // Submit one order, so that Add Seats search returns some POs.
        purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
            PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        // navigate to find purchase order page then select filter order type = "add seats"
        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(PelicanConstants.ADD_SEATS_ORDER, null, null, null, null,
            null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        // get Order Type Column Values.
        final List<String> orderTypeColumnValues = purchaseOrderSearchResultPage.getValuesFromOrderTypeColumn();

        // Assert Column Values are of type Order Type = "Add Seats"
        AssertCollector.assertThat("Order Type Column Value is incorrect: ", orderTypeColumnValues,
            Every.everyItem(equalTo(PelicanConstants.ADD_SEATS_ORDER)), assertionErrorList);

        // Select first row on purchase order search result page.
        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(1);
        // Assert Purchase Order Detail Page shows order type as Add Seats.
        AssertCollector.assertThat("Order Type Value is incorrect on Purchase Order Detail Page: ",
            purchaseOrderDetailPage.getOrderType(), equalTo(PelicanConstants.ADD_SEATS_ORDER), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Method to verify Advanced Find for subscription extension order returns Purchase Order of order type :
     * Subscription Extension only. Create a Extension order, just in case there was no Extension order.
     */
    @Test
    public void testAdvancedFindSearchForExtensionSeatsOrderType() {
        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String targetRenewalDate =
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId())));
        subscriptionMap.put(LineItemParams.QUANTITY.getValue(), new ArrayList<>(ImmutableList.of("10")));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(ImmutableList.of(targetRenewalDate)));

        // Submit Subscription Extension Purchase Order, so that Advanced find always returns some POs.
        purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap, Payment.PaymentType.CREDIT_CARD,
            PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // navigate to find purchase order page then select filter order type = "Subscription Extension"
        findPurchaseOrdersPage.findPurchaseOrderByAdvanceField(PelicanConstants.SUBSCRIPTION_EXTENSION_ORDER, null,
            null, null, null, null, null, null, null);
        final PurchaseOrderSearchResultPage purchaseOrderSearchResultPage =
            findPurchaseOrdersPage.clickSubmitOnFindPurchaeOrderByAdvanceField();

        // get Order Type Column Values.
        final List<String> orderTypeColumnValues = purchaseOrderSearchResultPage.getValuesFromOrderTypeColumn();

        // Assert Column Values are of type Order Type = "Subscription Extension"
        AssertCollector.assertThat("Order Type Column Value is incorrect: ", orderTypeColumnValues,
            Every.everyItem(equalTo(PelicanConstants.SUBSCRIPTION_EXTENSION_ORDER)), assertionErrorList);

        // Select first row on purchase order search result page.
        final PurchaseOrderDetailPage purchaseOrderDetailPage = purchaseOrderSearchResultPage.selectResultRow(1);

        // Assert Purchase Order Detail Page shows order type as Subscription Extension.
        AssertCollector.assertThat("Order Type Value is incorrect on Purchase Order Detail Page: ",
            purchaseOrderDetailPage.getOrderType(), equalTo(PelicanConstants.SUBSCRIPTION_EXTENSION_ORDER),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page displays the Buyer Details from DB fields and Not PO XML This scenarios are verified
     * by performing some DB updates and loading the PO page for the PO_ID and asserting that against the updated values
     * <p>
     * This test method is supplied the values to be updated by a data provider <b>getDataForBuyerDetailTesting</b>
     *
     * @param purchaseOrderId - purchase order Id to be updated
     * @param fieldToBeUpdated - the FieldValues to be updated. We have a <b>BuyerFieldsInDb</b> Enum for the possible
     *        values
     * @param valueToBeUpdated - The values to be updated
     */
    @Test(dataProvider = "getDataForBuyerDetailTesting")
    public void testFindPurchaseOrderDetailsAfterDBUpdate(final String purchaseOrderId,
        final BuyerFieldsInDb fieldToBeUpdated, final String valueToBeUpdated) {
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect Email", purchaseOrderDetailPage.getEmailAddress(),
            equalTo(buyerUser.getEmail()), assertionErrorList);
        boolean rowUpdated;
        rowUpdated = DbUtils.updateBuyerDetailInPurchaseOrder(purchaseOrderId, fieldToBeUpdated, valueToBeUpdated,
            getEnvironmentVariables());

        if (rowUpdated) {
            findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
            purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
            switch (fieldToBeUpdated) {
                case BILLING_EMAIL:
                    AssertCollector.assertThat("Incorrect Email Displayed", purchaseOrderDetailPage.getEmailAddress(),
                        equalTo(valueToBeUpdated), assertionErrorList);
                    break;
                case BILLING_FIRST_NAME:
                    AssertCollector.assertThat("Incorrect Buyer First Name Displayed",
                        purchaseOrderDetailPage.getFirstName(), equalTo(valueToBeUpdated), assertionErrorList);
                    break;
                case BILLING_LAST_NAME:
                    AssertCollector.assertThat("Incorrect Buyer Last Name Displayed",
                        purchaseOrderDetailPage.getLastName(), equalTo(valueToBeUpdated), assertionErrorList);
                    break;
                case BILLING_LAST_4_DIGITS:
                    AssertCollector.assertThat("Incorrect Last 4 digits Displayed",
                        purchaseOrderDetailPage.getLastFourDigits(), equalTo(valueToBeUpdated), assertionErrorList);
                    break;
                case BILLING_PHONE:
                    AssertCollector.assertThat("Incorrect Billing Phone Displayed",
                        purchaseOrderDetailPage.getPhoneNumber(), equalTo(valueToBeUpdated), assertionErrorList);
                    break;

                default:
                    break;
            }

        } else {
            AssertCollector.assertThat("Database not updated", "Expected True", equalTo("DB Not udpated!"),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testPurchaseOrderWithCountryCodeInBillingInfo() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);
        final BillingInformation billingInfo = PurchaseOrderUtils.getBillingInformation("First Name", "Last Name",
            "AutoTest_Company", "Street 1", "Street 2", "94105", "CA", "San Francisco", Country.US, "0987654321", "",
            PaymentType.CREDIT_CARD.getValue(), "", "", "1234", null);
        final PurchaseOrder purchaseOrderCreated = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, null, billingInfo, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderCreated.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String[] billingAddress = purchaseOrderDetailPage.getBillingAddress().split("\n");
        final String countryCodeString = billingAddress[billingAddress.length - 1];

        AssertCollector.assertThat("Incorrect Country", countryCodeString, equalTo(Country.US.getCountryCode()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @SuppressWarnings("unused")
    @DataProvider(name = "getDataForBuyerDetailTesting")
    private Object[][] getDataForBuyerDetailTesting() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final String firstName_db = "DB_Test_First_Name";
        final String lastFourDigits_db = "9876";
        final BillingInformation billingInfo = PurchaseOrderUtils.getBillingInformation("First Name", "Last Name",
            "AutoTest_Company", "Street 1", "Street 2", "94105", "CA", "San Francisco", Country.US, "0987654321", "",
            PaymentType.CREDIT_CARD.getValue(), "", "", "1234", null);
        final PurchaseOrder purchaseOrderCreated = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, null, billingInfo, buyerUser);
        final String purchaseOrderId = purchaseOrderCreated.getId();

        return new Object[][] { { purchaseOrderId, BuyerFieldsInDb.BILLING_FIRST_NAME, firstName_db },
                { purchaseOrderId, BuyerFieldsInDb.BILLING_LAST_4_DIGITS, lastFourDigits_db } };
    }
}
