package com.autodesk.bsm.pelican.api.financereport;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to test for Finance Report for Subscription Extension Orders.
 *
 * @author t_joshv
 */
public class FinanceReportForSubscriptionExtensionOrderTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());
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
     * Method to test Finance Report for Subscription Extension Order for command type CHARGE and REFUND for NAMER &
     * EMEA for BIC & Meta Subscription with Paypal and Credit Card Payment Type for Monthly and Yearly Subscription.
     * Step 1: Submit and Process New Acquisition Order. Step 2: Submit and Process Subscription Extension Order. Step
     * 3: Check Finance Report.
     *
     * @param paymentType
     * @param command
     * @param priceList
     * @param targetRenewalDateList
     * @param taxList
     */
    @Test(dataProvider = "extensionOrder")
    public void testFinanceReportForSubscriptionExtensionOrder(final PaymentType paymentType,
        final OrderCommand command, final List<String> priceList, final List<String> targetRenewalDateList,
        final List<String> taxList, final PaymentOption paymentOption) {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType,
            new HashMap<>(ImmutableMap.of(priceList.get(0), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType,
            new HashMap<>(ImmutableMap.of(priceList.get(1), 10)), null, true, true, buyerUser);

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // HashMap for Subscription Quantity Map.
        final Map<String, Integer> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(subscriptionId1, 4);
        subscriptionQuantityMap.put(subscriptionId2, 10);

        // HashMap to save Next Billing Date for each subscription.
        final Map<String, String> subscriptionIdNextBillingDateMap = new HashMap<>();
        subscriptionIdNextBillingDateMap.put(subscriptionId1,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        subscriptionIdNextBillingDateMap.put(subscriptionId2,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), priceList);
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), targetRenewalDateList);

        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap, paymentType,
                PurchaseOrder.OrderCommand.AUTHORIZE, taxList, buyerUser);

        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();

        if (paymentType == PaymentType.PAYPAL) {
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, subscriptionExtensionOrderId);
        } else {
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);
        }

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        // running the invoice job
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            subscriptionExtensionOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(subscriptionExtensionOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();

        String creditNoteNumber = null;
        // Finance Report Assertion for CHARGE.
        HelperForAssertionsOfFinanceReport.helperForSubscriptionExtensionOrder(resource, subscriptionExtensionOrderId,
            subscriptionIdNextBillingDateMap, subscriptionQuantityMap,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13), OrderCommand.CHARGE,
            invoiceNumber, creditNoteNumber, paymentType, paymentOption, assertionErrorList);

        // Finance Report Assertion for REFUND.
        if (command == OrderCommand.REFUND) {
            // Process Extension Order for REFUND.
            purchaseOrderUtils.processPurchaseOrder(command, subscriptionExtensionOrderId);
            purchaseOrderDetailPage.refreshPage();
            creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();
            HelperForAssertionsOfFinanceReport.helperForSubscriptionExtensionOrder(resource,
                subscriptionExtensionOrderId, subscriptionIdNextBillingDateMap, subscriptionQuantityMap,
                DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13), command,
                invoiceNumber, creditNoteNumber, paymentType, paymentOption, assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data Provider for Payment type, order command, EMEA & NAMER price id , target renewal dates and subscription
     * renewal dates for all subscriptions.
     *
     * @return
     */
    @DataProvider(name = "extensionOrder")
    private Object[][] getDataForExtensionOrder() {
        return new Object[][] {
                { PaymentType.PAYPAL, OrderCommand.CHARGE,
                        new ArrayList<>(ImmutableList.of(getMetaMonthlyUkPriceId(), getBicYearlyUkPriceId())),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        null, PaymentOption.PAYPAL },
                { PaymentType.CREDIT_CARD, OrderCommand.REFUND,
                        new ArrayList<>(ImmutableList.of(getBicMonthlyUsPriceId(), getMetaYearlyUsPriceId())),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        new ArrayList<>(ImmutableList.of("10", "11")), PaymentOption.VISA } };
    }

    /**
     * Test Method to verify Finance Report for Extension Order CHARGEBACK. Step 1: Submit and Process New Acquisition
     * Order. Step 2: Submit and Process Subscription Extension Order. Step 3: Check Finance Report.
     */
    @Test
    public void testFinanceReportForExtensionChargedBackOrder() {
        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 2)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // HashMap for Subscription Quantity Map.
        final Map<String, Integer> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(subscriptionId1, 2);

        // HashMap to save Next Billing Date for each subscription.
        final Map<String, String> subscriptionIdNextBillingDateMap = new HashMap<>();
        subscriptionIdNextBillingDateMap.put(subscriptionId1,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 40))));

        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        // Process the order for CHARGEBACK.

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, subscriptionExtensionOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(subscriptionExtensionOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailpage = findPurchaseOrdersPage.clickOnSubmit();

        purchaseOrderDetailpage.refreshPage();
        final String creditNoteNumber = purchaseOrderDetailpage.getCreditNoteNumber();

        // Finance Report Assertion for CHARGEBACK.
        HelperForAssertionsOfFinanceReport.helperForSubscriptionExtensionOrder(resource, subscriptionExtensionOrderId,
            subscriptionIdNextBillingDateMap, subscriptionQuantityMap,
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 40), OrderCommand.CHARGEBACK,
            null, creditNoteNumber, PaymentType.CREDIT_CARD, PaymentOption.VISA, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify Finance Report for Extension Order Mark As REFUND. Step 1: Submit and Process New
     * Acquisition Order. Step 2: Submit and Process Subscription Extension Order. Step 3: Check Finance Report.
     */
    @Test
    public void testFinanceReportForExtensionMarkAsRefundOrder() {
        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 2)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // HashMap for Subscription Quantity Map.
        final Map<String, Integer> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(subscriptionId1, 2);

        // HashMap to save Next Billing Date for each subscription.
        final Map<String, String> subscriptionIdNextBillingDateMap = new HashMap<>();
        subscriptionIdNextBillingDateMap.put(subscriptionId1,
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 40))));

        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, subscriptionExtensionOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, subscriptionExtensionOrderId);

        // Click Mark As Refund From Admin Tool.

        findPurchaseOrdersPage.findPurchaseOrderById(subscriptionExtensionOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailpage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailpage.clickMarkAsRefunded();
        purchaseOrderDetailpage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND_NOTES);
        final String creditNoteNumber = purchaseOrderDetailpage.getCreditNoteNumber();

        // Assert Finance Report Fields for MARK AS REFUNDED.
        HelperForAssertionsOfFinanceReport.helperForSubscriptionExtensionOrder(resource, subscriptionExtensionOrderId,
            subscriptionIdNextBillingDateMap, subscriptionQuantityMap,
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 40), null, null,
            creditNoteNumber, PaymentType.CREDIT_CARD, PaymentOption.VISA, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
