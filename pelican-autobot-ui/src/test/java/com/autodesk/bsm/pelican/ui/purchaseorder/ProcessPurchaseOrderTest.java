package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.ProcessOrderErrorPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.ProcessPurchaseOrderPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class tests Process PO functionality from Admin Tool on PO page.
 *
 * @author Shweta Hegde
 */
public class ProcessPurchaseOrderTest extends SeleniumWebdriver {

    private PurchaseOrderUtils purchaseOrderUtils;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private AdminToolPage adminToolPage;
    private String requestedByAtUser;
    private PelicanPlatform resource;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);

        requestedByAtUser =
            getEnvironmentVariables().getUserName() + " (" + getEnvironmentVariables().getUserId() + ")";

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Test Purchase Order Command Type is mandatory for Process PO.
     */
    @Test
    public void testErrorProcessPOPopUpMandatesCommandType() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUkPriceId(), 2);
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, false, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(null, null, null, null);
        processPurchaseOrderPage.clickOnConfirm();

        AssertCollector.assertThat("Incorrect error message for PO : " + purchaseOrderId,
            processPurchaseOrderPage.getErrorMessageList().get(0), equalTo("Invalid value"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test notes is mandatory for Process PO.
     */
    @Test
    public void testErrorProcessPOPopUpMandatesNotes() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUkPriceId(), 3);
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, false, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.CHARGEBACK.toString(), null, null, null);
        processPurchaseOrderPage.clickOnConfirm();

        AssertCollector.assertThat("Incorrect error message for PO : " + purchaseOrderId,
            processPurchaseOrderPage.getErrorMessageList().get(0), equalTo("Required"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that without EBSO role, this "Process PO Manually" link is not visible.
     */
    @Test
    public void testProcessPOLinkIsVisibleToOnlyEBSORole() {

        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), "UserWithoutEbso");
        userParams.put(UserClient.UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());
        final List<String> requiredRoleList = rolesHelper.getGCSOOnlyRoleList();

        // Create new user, assign GCSO role and log in with that user.
        final UserUtils userUtils = new UserUtils();
        userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage, getEnvironmentVariables());

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUkPriceId(), 3);
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, false, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertFalse("Process Po link should not be visible",
            purchaseOrderDetailPage.isProcessPoLinkDisplayed(), assertionErrorList);

        // Logout from user with GCSO permission
        adminToolPage.logout();
        // Login with original user
        adminToolPage.login();

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Process PO through Admin Tool changes Order State from AUTHORIZED to PENDING. And process the PO to CHARGE.
     * Validate the properties Name and Value for both the transactions. Also verifies transaction requested by column
     * and audit logs for subscription.
     */
    @Test
    public void testProcessPoFromAuthToPendingAndPendingToCharge() {

        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUkPriceId(), buyerUser, 2);

        final String purchaseOrderId = purchaseOrder.getId();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);

        final String pendingNotes = "Processing to Pending";
        ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.PENDING.toString(), ECStatus.ACCEPT.getName(),
            pendingNotes, null);
        // check SPP Note shows the SPP used at submitting PO.
        AssertCollector.assertThat("Incorrect SPP notes", processPurchaseOrderPage.getSppNote(),
            equalTo("submitted spp: " + purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId()
                + " (" + PaymentOption.VISA.getValue() + ")"),
            assertionErrorList);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.PENDING,
            "pending_notes", pendingNotes, 1, 3, requestedByAtUser, assertionErrorList);
        AssertCollector.assertThat("Incorrect Final EC Status", purchaseOrderDetailPage.getFinalECStatus(),
            equalTo(ECStatus.ACCEPT.getName()), assertionErrorList);

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        // Audit Log validation for subscription creation (PO AUTH to PENDING)
        SubscriptionAuditLogHelper.helperToValidateDynamoDbForCreateSubscriptions(subscriptionId, Status.ACTIVE, null,
            getBicMonthlyUkPriceId(), getBicSubscriptionPlan().getOfferings().get(0).getId(),
            getEnvironmentVariables().getUserId(), getEnvironmentVariables().getUserId(), null,
            getEnvironmentVariables(), assertionErrorList);

        final String chargeNotes = "Processing to Charge";
        processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.CHARGE.toString(), null, chargeNotes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.CHARGED,
            "charged_notes", chargeNotes, 2, 4, requestedByAtUser, assertionErrorList);

        // Audit log validation for Update of the subscription for PO PENDING to CHARGE
        SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, null, null, null,
            null, false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Process PO through Admin Tool changes Order State from AUTHORIZED to CHARGED. And process the PO to
     * CHARGEBACK. Validate the properties Name and Value for both the transactions. Also verifies transaction requested
     * by column and audit logs for subscription.
     */
    @Test
    public void testProcessPoFromAuthToChargeAndToChargeBack() {

        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUkPriceId(), buyerUser, 2).getId();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);

        final String chargeNotes = "Processing to Charge";
        ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.CHARGE.toString(), null, chargeNotes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.CHARGED,
            "charged_notes", chargeNotes, 1, 3, requestedByAtUser, assertionErrorList);

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        // Audit Log validation for subscription creation for PO AUTH to CHARGE
        SubscriptionAuditLogHelper.helperToValidateDynamoDbForCreateSubscriptions(subscriptionId, Status.ACTIVE, null,
            getBicMonthlyUkPriceId(), getBicSubscriptionPlan().getOfferings().get(0).getId(),
            getEnvironmentVariables().getUserId(), getEnvironmentVariables().getUserId(), null,
            getEnvironmentVariables(), assertionErrorList);

        final String chargeBackNotes = "Processing to Chargeback";
        processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.CHARGEBACK.toString(), null, chargeBackNotes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId,
            OrderState.CHARGED_BACK, "charged_back_notes", chargeBackNotes, 2, 4, requestedByAtUser,
            assertionErrorList);

        // Audit log validation for Update of the subscription for PO CHARGE to CHARGEBACK
        SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, Status.ACTIVE,
            Status.EXPIRED, null, null, true, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Process PO through Admin Tool changes Order State from PENDING to DECLINED. Validate the properties Name and
     * Value for both the transactions. Also verifies transaction requested by column and audit logs for subscription.
     */
    @Test
    public void testProcessPoFromPendingToDecline() {

        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUkPriceId(), buyerUser, 2).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Requested By for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionRequestedBy(3).split(" ")[0],
            equalTo(getEnvironmentVariables().getPartnerId()), assertionErrorList);

        final String declineNotes = "Processing to Decline";
        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.DECLINE.toString(), ECStatus.BLOCK.getName(),
            declineNotes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.DECLINED,
            "declined_notes", declineNotes, 1, 4, requestedByAtUser, assertionErrorList);
        AssertCollector.assertThat("Incorrect Final EC Status", purchaseOrderDetailPage.getFinalECStatus(),
            equalTo(ECStatus.BLOCK.getName()), assertionErrorList);

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        // Audit log validation for Update of the subscription for PO PENDING to DECLINE
        SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, Status.ACTIVE,
            Status.EXPIRED, null, null, true, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Process PO through Admin Tool changes Order State from CHARGE to REFUND. Validate the properties Name and
     * Value for both the transactions. Also verifies transaction requested by column and audit logs for subscription.
     */
    @Test
    public void testProcessPoFromChargeToRefund() {

        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUkPriceId(), buyerUser, 2).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String refundNotes = "Processing to Refund";
        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.REFUND.toString(), null, refundNotes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.REFUNDED,
            "refunded_notes", refundNotes, 1, 5, requestedByAtUser, assertionErrorList);

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        // Audit log validation for Update of the subscription for PO CHARGE to REFUND
        SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionWithPOChange(subscriptionId, Status.ACTIVE,
            Status.EXPIRED, null, null, true, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error message when trying to Process the PO to the current state. 1. Error thrown when
     * processing PENDING PO to PENDING. 2. Error thrown when processing DECLINE PO to REFUND.
     */
    @Test
    public void testErrorWhenProcessingToTheCurrentStateOrIncorrectState() {

        // Create an order in AUTH status
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUkPriceId(), buyerUser, 2).getId();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Verify that order is in AUTH status
        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);

        // Process the order to PENDING
        final String notes = "Processing the PO";
        ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.PENDING.toString(), null, notes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.PENDING.toString()), assertionErrorList);

        // Process the order from PENDING to PENDING and verify that error is thrown
        processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.PENDING.toString(), null, notes, null);
        ProcessOrderErrorPage processOrderErrorPage = processPurchaseOrderPage.clickOnConfirmError();

        AssertCollector.assertThat("Incorrect error message for PO : " + purchaseOrderId,
            processOrderErrorPage.getH3ErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.PROCESS_PO_ERROR, OrderCommand.PENDING.toString())),
            assertionErrorList);

        processOrderErrorPage.navigateToPreviousMessage();
        // Click on Cancel and verify that page is returned to PurchaseOrderDetailPage
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnCancel();
        // Process the PO to DECLINE and it is successful, since order can go from PENDING to DECLINE.
        purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.DECLINE.toString(), null, notes, null);
        purchaseOrderDetailPage = processPurchaseOrderPage.clickOnConfirm();

        // Process the order from DECLINE to CHARGE and verify error is thrown.
        purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.CHARGE.toString(), null, notes, null);
        processOrderErrorPage = processPurchaseOrderPage.clickOnConfirmError();

        AssertCollector.assertThat("Incorrect error message for PO : " + purchaseOrderId,
            processOrderErrorPage.getH3ErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.PROCESS_PO_ERROR, OrderCommand.CHARGE.toString())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test error is thrown at Process PO Pending of Add Seats order, when subscription is expired.
     */
    @Test
    public void testErrorWhenAddSeatsPurchaseOrderIsProcessingAndSubscriptionIsExpired() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);

        // Submit New Acquisition PO
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Expire the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.PENDING.toString(), null, "Processing", null);
        final ProcessOrderErrorPage processOrderErrorPage = processPurchaseOrderPage.clickOnConfirmError();

        AssertCollector.assertThat("Incorrect error message", processOrderErrorPage.getH3ErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_NOT_FOUND_ERROR + subscriptionId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test PAYPAL PO processing from Auth to Pending.
     */
    @Test
    public void testPaypalPoProcessPendingFromAuth() {
        // submit purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser, 1, null, PaymentProcessor.PAYPAL_NAMER.getValue(),
            null, null, null);
        final String purchaseOrderId = purchaseOrder.getId();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final ProcessPurchaseOrderPage processPurchaseOrderPage = purchaseOrderDetailPage.clickOnProcessPoLink();
        final String pendingProcessNotes = "Processing Paypal Order to Pending with NAMER payment profile";
        processPurchaseOrderPage.processPurchaseOrder(OrderCommand.PENDING.toString(), null, pendingProcessNotes,
            getPaymentProfileIdForPaypalNamer() + " (" + Payment.PaymentOption.PAYPAL.getValue() + ")");

        AssertCollector.assertFalse("Default SPP note should not be present",
            processPurchaseOrderPage.isSppDefaultNoteVisible(), assertionErrorList);
        processPurchaseOrderPage.clickOnConfirm();
        PurchaseOrderHelper.commonAssertionsForProperties(purchaseOrderDetailPage, purchaseOrderId, OrderState.PENDING,
            "pending_notes", pendingProcessNotes, 1, 3, requestedByAtUser, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
