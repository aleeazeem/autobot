package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.PurchaseOrdersClient.PurchaseOrderParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering.OfferingResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrders;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionExtension.SubscriptionExtensionResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionRenewal.SubscriptionRenewalResponse;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test Case : Get Purchase Orders API
 *
 * @author t_mohag
 */
public class GetPurchaseOrdersTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private HttpError httpError;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int QUANTITY = 1;
    private JobsClient jobsResource;
    private static final String ORDER_COMPLETE_TEMPLATE_ID = "0";
    private static final String AUTO_RENEWAL_COMPLETE_TEMPLATE_ID = "1";
    private static final String ORDER_FULFILLMENT_TEMPLATE_ID = "12";
    private static final String INVOICE_TEMPLATE_ID = "14";
    private static final String CREDIT_NOTE_MEMO_TEMPLATE_ID = "15";
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);

        // Changing the feature flag to FALSE, so that order fulfillment emails
        // are sent immediately
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY,
            false);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        // Changing the feature flag to "true"
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY,
            true);
    }

    /**
     * Test Get Purchase Orders without any filters
     */
    @Test
    public void testGetPurchaseOrdersWithoutAnyFilter() {
        // Get Purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            AssertCollector.assertThat("Unable to retrieve purchase orders", purchaseOrders.getPurchaseOrders(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("No purchase orders returned", purchaseOrders.getPurchaseOrders().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BiC Purchase order with credit card in Charged state This method written to validate the ORDER_COMPLETE
     * email reference id once the order is CHARGED Invoice and Credit note memo email will not be sent because flag is
     * not set for the store
     */
    @Test
    public void testBICPurchaseOrderForNewAcquisitionWithCreditCardWithOnlyOrderCompleteEmail() {
        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);
        final String subscriptionIdBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            purchaseOrderIdForBicCreditCard);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForBicCreditCard);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderCompleteEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForBicCreditCard,
                "TEMPLATE_ID", ORDER_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdBicCreditCard);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(purchaseOrderIdForBicCreditCard, purchaseOrders);
            verifyAssertionsOnNewAcquisition(getBicMonthlyUsPriceId(), QUANTITY, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect order complete email reference id",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId(),
                equalTo(orderCompleteEmailReferenceId), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BiC Purchase order with credit card in Charged state This method written to validate the ORDER_COMPLETE
     * email reference id once the order is CHARGED Invoice Email when invoice is generated and Credit note memo email
     * when "Mark As Refunded'
     */
    @Test
    public void testBICPurchaseOrderForNewAcquisitionWithPaypalWithAllThreeEmails() {
        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, getBicMonthlyUkPriceId(), buyerUser, QUANTITY)
            .getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderIdForBicPaypal);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicPaypal);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicPaypal);
        final String subscriptionIdBicPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            purchaseOrderIdForBicPaypal);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForBicPaypal);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderCompleteEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForBicPaypal,
                "TEMPLATE_ID", ORDER_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Invoice email reference id
        // taken from "EMAIL_REFERENCE" table
        final String invoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForBicPaypal,
                "TEMPLATE_ID", INVOICE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Credit note memo email
        // reference id taken from "EMAIL_REFERENCE" table
        final String creditNoteInvoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForBicPaypal,
                "TEMPLATE_ID", CREDIT_NOTE_MEMO_TEMPLATE_ID, getEnvironmentVariables());

        // Adding email reference ids in set, because the order of email
        // reference ids varies in response.
        final Set<String> emailReferenceIdsSet = new HashSet<>();
        emailReferenceIdsSet.add(orderCompleteEmailReferenceId);
        emailReferenceIdsSet.add(invoiceEmailReferenceId);
        emailReferenceIdsSet.add(creditNoteInvoiceEmailReferenceId);

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdBicPaypal);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(purchaseOrderIdForBicPaypal, purchaseOrders);
            verifyAssertionsOnNewAcquisition(getBicMonthlyUkPriceId(), QUANTITY, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(3), assertionErrorList);
            AssertCollector.assertTrue("Incorrect order complete email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect invoice email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(1).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect credit note email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(2).getReferenceId()),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Meta Purchase order with credit card in Charged state This method written to validate the ORDER_COMPLETE
     * email reference id once the order is CHARGED Invoice Email when invoice is generated and Credit note memo email
     * when "Mark As Refunded'
     */
    @Test
    public void testMetaPurchaseOrderForNewAcquisitionWithCreditCardWithAllFourEmails() {
        // Submit a purchase order with Credit card and process it to charged
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUkPriceId(), buyerUser, QUANTITY);
        final String purchaseOrderIdForMetaCreditCard = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMetaCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMetaCreditCard);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaCreditCard);

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaCreditCard);
        final String subscriptionIdMetaCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            purchaseOrderIdForMetaCreditCard);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMetaCreditCard);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);
        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderCompleteEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID",
                purchaseOrderIdForMetaCreditCard, "TEMPLATE_ID", ORDER_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Order Fulfillment email
        // reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderFulfillmentEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", purchaseOrderIdForMetaCreditCard, "TEMPLATE_ID", ORDER_FULFILLMENT_TEMPLATE_ID,
            getEnvironmentVariables());
        // this query is used in the assertions. Invoice email reference id
        // taken from "EMAIL_REFERENCE" table
        final String invoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID",
                purchaseOrderIdForMetaCreditCard, "TEMPLATE_ID", INVOICE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Credit note memo email
        // reference id taken from "EMAIL_REFERENCE" table
        final String creditNoteInvoiceEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", purchaseOrderIdForMetaCreditCard, "TEMPLATE_ID", CREDIT_NOTE_MEMO_TEMPLATE_ID,
            getEnvironmentVariables());

        // Adding email reference ids in set, because the order of email
        // reference ids varies in response.
        final Set<String> emailReferenceIdsSet = new HashSet<>();
        emailReferenceIdsSet.add(orderCompleteEmailReferenceId);
        emailReferenceIdsSet.add(orderFulfillmentEmailReferenceId);
        emailReferenceIdsSet.add(invoiceEmailReferenceId);
        emailReferenceIdsSet.add(creditNoteInvoiceEmailReferenceId);

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdMetaCreditCard);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(purchaseOrderIdForMetaCreditCard, purchaseOrders);
            verifyAssertionsOnNewAcquisition(getMetaMonthlyUkPriceId(), QUANTITY, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(4), assertionErrorList);
            AssertCollector.assertTrue("Incorrect order complete email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect order fulfillment email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(1).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect invoice email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(2).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect credit note email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(3).getReferenceId()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Meta Purchase order with Paypal in Charged state This method written to validate the ORDER_COMPLETE email
     * reference id once the order is CHARGED Invoice and Credit note memo email will not be sent because flag is not
     * set for the store
     */
    @Test
    public void testMetaPurchaseOrderForNewAcquisitionWithPaypalWithOrderCompleteAndFulfillmentEmail() {
        // Submit a purchase order with Credit card and process it to charged
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL,
            getMetaMonthlyUsPriceId(), buyerUser, QUANTITY);
        final String purchaseOrderIdForMetaPaypal = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderIdForMetaPaypal);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMetaPaypal);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);
        final String subscriptionIdMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            purchaseOrderIdForMetaPaypal);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMetaPaypal);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderCompleteEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForMetaPaypal,
                "TEMPLATE_ID", ORDER_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String orderFulfillmentEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", purchaseOrderIdForMetaPaypal,
                "TEMPLATE_ID", ORDER_FULFILLMENT_TEMPLATE_ID, getEnvironmentVariables());

        // Adding email reference ids in set, because the order of email
        // reference ids varies in response.
        final Set<String> emailReferenceIdsSet = new HashSet<>();
        emailReferenceIdsSet.add(orderCompleteEmailReferenceId);
        emailReferenceIdsSet.add(orderFulfillmentEmailReferenceId);

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdMetaPaypal);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(purchaseOrderIdForMetaPaypal, purchaseOrders);
            verifyAssertionsOnNewAcquisition(getMetaMonthlyUsPriceId(), QUANTITY, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(2), assertionErrorList);
            AssertCollector.assertThat("Incorrect order complete email reference id",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId(),
                equalTo(orderCompleteEmailReferenceId), assertionErrorList);
            AssertCollector.assertThat("Incorrect order fulfillment email reference id",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(1).getReferenceId(),
                equalTo(orderFulfillmentEmailReferenceId), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test the AUTO_RENEWAL_COMPLETE email-reference id in the GetPurchaseOrders response Tests the following
     * combination: 1. Bic subscription 2. Credit card 3. Renewal (Charged and Fulfilled) 4. NO invoice (as the store
     * has 'send tax invoice emails' flag set to false)
     */
    @Test
    public void testBICRenewalWithCreditCardWithOnlyRenewalCompleteEmail() {
        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);
        final String subscriptionIdBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdBicCreditCard);

        // Submit a renewal request
        final PurchaseOrder renewalPurchaseOrderForBicCreditCard =
            purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false,
                PaymentType.CREDIT_CARD, null, true);

        final String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrderForBicCreditCard.getId();
        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            renewalPurchaseOrderIdForBicCreditCard);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderIdForBicCreditCard);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);
        // this query is used in the assertions. Auto renewal order complete
        // email reference id taken from "EMAIL_REFERENCE" table
        final String renewalOrderCompleteEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", renewalPurchaseOrderIdForBicCreditCard, "TEMPLATE_ID",
            AUTO_RENEWAL_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdBicCreditCard);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(renewalPurchaseOrderIdForBicCreditCard, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect auto renewal order complete email reference id",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId(),
                equalTo(renewalOrderCompleteEmailReferenceId), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test the AUTO_RENEWAL_COMPLETE email-reference id in the GetPurchaseOrders response Tests the following
     * combination: 1. Bic subscription 2. Paypal 3. Renewal (Charged and Fulfilled) 4. Invoice Email when invoice is
     * generated 5. Credit note memo
     */
    @Test
    public void testBICRenewalWithPaypalWithAllThreeEmails() {
        // Submit a purchase order with Paypal and process it to charged
        final String purchaseOrderIdForBicPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, getBicMonthlyUkPriceId(), buyerUser, QUANTITY)
            .getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderIdForBicPaypal);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicPaypal);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicPaypal);
        final String subscriptionIdBicPaypalForRenewal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdBicPaypalForRenewal);

        // Submit a renewal request
        final PurchaseOrder renewalPurchaseOrderBicPaypal = purchaseOrderUtils
            .submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false, PaymentType.PAYPAL, null, true);

        final String renewalPurchaseOrderIdBicPaypal = renewalPurchaseOrderBicPaypal.getId();
        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            renewalPurchaseOrderIdBicPaypal);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderIdBicPaypal);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Auto renewal order complete
        // email reference id taken from "EMAIL_REFERENCE" table
        final String renewalOrderCompleteEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", renewalPurchaseOrderIdBicPaypal,
                "TEMPLATE_ID", AUTO_RENEWAL_COMPLETE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. invoice email reference id
        // taken from "EMAIL_REFERENCE" table
        final String invoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", renewalPurchaseOrderIdBicPaypal,
                "TEMPLATE_ID", INVOICE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. invoice email reference id
        // taken from "EMAIL_REFERENCE" table
        final String creditNoteInvoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID", renewalPurchaseOrderIdBicPaypal,
                "TEMPLATE_ID", CREDIT_NOTE_MEMO_TEMPLATE_ID, getEnvironmentVariables());

        // Adding email reference ids in set, because the order of email
        // reference ids varies in response.
        final Set<String> emailReferenceIdsSet = new HashSet<>();
        emailReferenceIdsSet.add(renewalOrderCompleteEmailReferenceId);
        emailReferenceIdsSet.add(invoiceEmailReferenceId);
        emailReferenceIdsSet.add(creditNoteInvoiceEmailReferenceId);

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdBicPaypalForRenewal);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(renewalPurchaseOrderIdBicPaypal, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(3), assertionErrorList);
            AssertCollector.assertTrue("Incorrect auto renewal order complete email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect invoice email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(1).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect credit note memo email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(2).getReferenceId()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test the AUTO_RENEWAL_COMPLETE email-reference id in the GetPurchaseOrders response Tests the following
     * combination: 1. Meta subscription 2. Credit Card 3. Renewal (Charged and Fulfilled) 4. Invoice Email when invoice
     * is generated 5. Credit note memo
     */
    @Test
    public void testMetaRenewalWithCreditCardWithAllThreeEmails() {
        // Submit a purchase order with Credit card and process it to charged
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUkPriceId(), buyerUser, QUANTITY);
        final String purchaseOrderIdForMetaCreditCard = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMetaCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMetaCreditCard);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaCreditCard);

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaCreditCard);
        final String subscriptionIdMetaCreditCardForRenewal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdMetaCreditCardForRenewal);

        // Submit a renewal request
        final PurchaseOrder renewalPurchaseOrderMetaCreditCard = purchaseOrderUtils
            .submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false, PaymentType.PAYPAL, null, true);

        final String renewalPurchaseOrderIdMetaCreditCard = renewalPurchaseOrderMetaCreditCard.getId();
        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            renewalPurchaseOrderIdMetaCreditCard);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderIdMetaCreditCard);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Auto renewal order complete
        // email reference id taken from "EMAIL_REFERENCE" table
        final String renewalOrderCompleteEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", renewalPurchaseOrderIdMetaCreditCard, "TEMPLATE_ID", AUTO_RENEWAL_COMPLETE_TEMPLATE_ID,
            getEnvironmentVariables());
        // this query is used in the assertions. Invoice email reference id
        // taken from "EMAIL_REFERENCE" table
        final String invoiceEmailReferenceId =
            DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID", "PURCHASE_ORDER_ID",
                renewalPurchaseOrderIdMetaCreditCard, "TEMPLATE_ID", INVOICE_TEMPLATE_ID, getEnvironmentVariables());
        // this query is used in the assertions. Credit note email reference id
        // taken from "EMAIL_REFERENCE" table
        final String creditNoteInvoiceEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", renewalPurchaseOrderIdMetaCreditCard, "TEMPLATE_ID", CREDIT_NOTE_MEMO_TEMPLATE_ID,
            getEnvironmentVariables());

        // Adding email reference ids in set, because the order of email
        // reference ids varies in response.
        final Set<String> emailReferenceIdsSet = new HashSet<>();
        emailReferenceIdsSet.add(renewalOrderCompleteEmailReferenceId);
        emailReferenceIdsSet.add(invoiceEmailReferenceId);
        emailReferenceIdsSet.add(creditNoteInvoiceEmailReferenceId);

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(),
            subscriptionIdMetaCreditCardForRenewal);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(renewalPurchaseOrderIdMetaCreditCard, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(3), assertionErrorList);
            AssertCollector.assertTrue("Incorrect Auto renewal order complete email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect invoice email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(1).getReferenceId()),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect credit note memo email reference id",
                emailReferenceIdsSet.contains(
                    purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(2).getReferenceId()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test the AUTO_RENEWAL_COMPLETE email-reference id in the GetPurchaseOrders response Tests the following
     * combination: 1. Meta subscription 2. Paypal 3. Renewal (Charged and Fulfilled) 4. NO invoice (as the store has
     * 'send tax invoice emails' flag set to false)
     */
    @Test
    public void testMetaRenewalWithPaypalWithOnlyRenewalOrderCompleteEmail() {
        // Submit a purchase order with Credit card and process it to charged
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL,
            getMetaMonthlyUsPriceId(), buyerUser, QUANTITY);
        final String purchaseOrderIdForMetaPaypal = purchaseOrder.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderIdForMetaPaypal);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMetaPaypal);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);
        final String subscriptionIdMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdMetaPaypal);

        // Submit a renewal request
        final PurchaseOrder renewalPurchaseOrderMetaPaypal = purchaseOrderUtils
            .submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false, PaymentType.PAYPAL, null, true);

        final String renewalPurchaseOrderIdMetaPaypal = renewalPurchaseOrderMetaPaypal.getId();
        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            renewalPurchaseOrderIdMetaPaypal);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderIdMetaPaypal);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // this query is used in the assertions. Order Complete email reference
        // id taken from "EMAIL_REFERENCE" table
        final String renewalOrderCompleteEmailReferenceId = DbUtils.selectQuery("EMAIL_REFERENCE", "REFERENCE_ID",
            "PURCHASE_ORDER_ID", renewalPurchaseOrderIdMetaPaypal, "TEMPLATE_ID", AUTO_RENEWAL_COMPLETE_TEMPLATE_ID,
            getEnvironmentVariables());

        // Using multi parameters to find purchase orders
        final Map<String, String> purchaseOrderParams = new HashMap<>();
        purchaseOrderParams.put(PurchaseOrderParameter.BUYER_USER_ID.getName(), buyerUser.getId());
        purchaseOrderParams.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionIdMetaPaypal);

        // Get Purchase orders
        final Object apiResponse = resource.purchaseOrders().getPurchaseOrders(purchaseOrderParams);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            final PurchaseOrders purchaseOrders = (PurchaseOrders) apiResponse;
            commonAssertionsForPurchaseOrders(renewalPurchaseOrderIdMetaPaypal, purchaseOrders);
            AssertCollector.assertThat("Incorrect 'SentEmails' size",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().size(), is(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect auto renewal order complete email reference id",
                purchaseOrders.getPurchaseOrders().get(0).getSentEmails().getEmail().get(0).getReferenceId(),
                equalTo(renewalOrderCompleteEmailReferenceId), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test get purchase orders api returns subscrition start and end date and time within respones.
     *
     */
    @Test
    public void testGetPurchaseOrdersReturnsSubscriptionTimeStampInResponseForRenewalOrders() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final OfferingResponse offeringResponse =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse();
        final String subscriptionId = offeringResponse.getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription id, currency id/currency name
        final HashMap<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_NAME.getValue(),
            new ArrayList<>(ImmutableList.of(Currency.USD.toString())));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(),
            new ArrayList<>(ImmutableList.of(String.valueOf(Currency.USD.getCode()))));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        final Map<String, String> filterParameter = new HashMap<>();
        filterParameter.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), subscriptionId);
        final PurchaseOrders purchaseOrders = resource.purchaseOrders().getPurchaseOrders(filterParameter);

        final SubscriptionRenewalResponse subscriptionRenewalResponse = purchaseOrders.getPurchaseOrders().get(0)
            .getLineItems().getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse();

        final String subscriptionStartDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);

        final String subscriptionEndDate =
            DateTimeUtils.addDaysToDate(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, -1);

        AssertCollector
            .assertThat(
                "Incorrect Subscription Start Time", subscriptionRenewalResponse.getSubscriptions().getSubscription()
                    .get(0).getSubscriptionPeriodStartDate().split(" ")[0],
                equalTo(subscriptionStartDate), assertionErrorList);
        AssertCollector
            .assertThat(
                "Incorrect Subscription End Time", subscriptionRenewalResponse.getSubscriptions().getSubscription()
                    .get(0).getSubscriptionPeriodEndDate().split(" ")[0],
                equalTo(subscriptionEndDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test Get purchase orders api returns subscription start and end time stamp in response for subscription extension
     * order.
     */
    @Test
    public void testGetPurchaseOrdersReturnsSubscriptionTimeStampInResponseForExtensionOrders() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String targetRenewalDate =
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.QUANTITY.getValue(), new ArrayList<>(ImmutableList.of("2")));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(ImmutableList.of(targetRenewalDate)));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);
        final String extensionOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, extensionOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, extensionOrderId);

        final Map<String, String> filterParameter = new HashMap<>();
        filterParameter.put(PurchaseOrderParameter.SUBSCRIPTION_ID.getName(), bicSubscriptionId);
        final PurchaseOrders purchaseOrders = resource.purchaseOrders().getPurchaseOrders(filterParameter);

        final SubscriptionExtensionResponse subscriptionExtensionResponse = purchaseOrders.getPurchaseOrders().get(0)
            .getLineItems().getLineItems().get(0).getSubscriptionExtension().getSubscriptionExtensionResponse();

        final String subscriptionStartDate =
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1);

        final String subscriptionEndDate =
            DateTimeUtils.addDaysToDate(targetRenewalDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, -1);

        AssertCollector.assertThat(
            "Incorrect Subscription Start Time", subscriptionExtensionResponse.getSubscriptions().getSubscription()
                .get(0).getSubscriptionPeriodStartDate().split(" ")[0],
            equalTo(subscriptionStartDate), assertionErrorList);
        AssertCollector
            .assertThat(
                "Incorrect Subscription End Time", subscriptionExtensionResponse.getSubscriptions().getSubscription()
                    .get(0).getSubscriptionPeriodEndDate().split(" ")[0],
                equalTo(subscriptionEndDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    private void commonAssertionsForPurchaseOrders(final String purchaseOrderId, final PurchaseOrders purchaseOrders) {
        AssertCollector.assertThat("No purchase orders returned", purchaseOrders.getPurchaseOrders().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrders.getPurchaseOrders().get(0).getId(),
            equalTo(purchaseOrderId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrders.getPurchaseOrders().get(0).getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment status ",
            purchaseOrders.getPurchaseOrders().get(0).getFulFillmentStatus(), equalTo(FulFillmentStatus.FULFILLED),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser Email id ",
            purchaseOrders.getPurchaseOrders().get(0).getBuyerUser().getEmail(), equalTo(buyerUser.getEmail()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser External key",
            purchaseOrders.getPurchaseOrders().get(0).getBuyerUser().getExternalKey(),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser Id ",
            purchaseOrders.getPurchaseOrders().get(0).getBuyerUser().getId(), equalTo(buyerUser.getId()),
            assertionErrorList);
    }

    private void verifyAssertionsOnNewAcquisition(final String priceId, final int quantity,
        final PurchaseOrders purchaseOrders) {
        AssertCollector.assertThat(
            "Incorrect Offering Request Price Id ", purchaseOrders.getPurchaseOrders().get(0).getLineItems()
                .getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(priceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", purchaseOrders.getPurchaseOrders().get(0).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(), is(quantity), assertionErrorList);

    }

}
