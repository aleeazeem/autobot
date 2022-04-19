package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

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
 * @author: t_joshv Class to test Emails for Subscription Extension Orders.
 */
public class SubscriptionExtensionOrderEmailTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;

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
    }

    /**
     * Test to verify Credit Note Email for Extension Order contains Proration Start Date, Proration End Date and
     * Subscription ID.
     *
     * @param paymentType
     * @param priceList
     * @param targetRenewalDateList
     */
    @Test(dataProvider = "extensionOrder")
    public void testCreditNoteEmailForSubscriptionExtensionOrder(final PaymentType paymentType,
        final List<String> priceList, final List<String> targetRenewalDateList, final String paymentOption) {

        // Create a purchase order and get BIC subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType,
            new HashMap<>(ImmutableMap.of(priceList.get(0), 4)), null, true, true, getBuyerUser());

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get META subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType,
            new HashMap<>(ImmutableMap.of(priceList.get(1), 10)), null, true, true, getBuyerUser());

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

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal
        // date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), priceList);
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), targetRenewalDateList);

        final PurchaseOrder subscriptionExtensionOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap, paymentType,
                PurchaseOrder.OrderCommand.AUTHORIZE, null, getBuyerUser());
        final String subscriptionExtensionOrderId = subscriptionExtensionOrder.getId();

        if (paymentType == PaymentType.PAYPAL) {
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(),
                subscriptionExtensionOrder.getId());
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

        // Helper Method to validate Order Complete Email is Found with all required fields.
        PelicanDefaultEmailValidations.extensionOrderComplete(subscriptionExtensionOrderId,
            subscriptionIdNextBillingDateMap, targetRenewalDateList.get(0), paymentOption, getEnvironmentVariables());

        // Helper Method to validate Invoice Email is Found with all required fields.
        PelicanDefaultEmailValidations.extensionInvoice(subscriptionExtensionOrderId, subscriptionIdNextBillingDateMap,
            targetRenewalDateList.get(0), invoiceNumber, paymentOption, getEnvironmentVariables());

        // Process Extension Order For REFUND.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, subscriptionExtensionOrderId);

        // Get Credit Note Number on REFUND.
        purchaseOrderDetailPage.refreshPage();
        final String creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();

        // Helper Method to validate Email is Found with all required fields.
        PelicanDefaultEmailValidations.extensionCreditNoteMemo(subscriptionExtensionOrderId,
            subscriptionIdNextBillingDateMap, targetRenewalDateList.get(0), invoiceNumber, creditNoteNumber,
            paymentOption, getEnvironmentVariables());

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data Provider for Different Payment Types.
     *
     * @return
     */
    @DataProvider(name = "extensionOrder")
    private Object[][] getDataForExtensionOrder() {
        return new Object[][] {

                { PaymentType.CREDIT_CARD,
                        new ArrayList<>(ImmutableList.of(getBicYearlyUkPriceId(), getMetaMonthlyUkPriceId())),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        PaymentOption.VISA.getValue() },
                { PaymentType.PAYPAL,
                        new ArrayList<>(ImmutableList.of(getBicYearlyUkPriceId(), getMetaMonthlyUkPriceId())),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        PaymentOption.PAYPAL.getValue() } };
    }

}
