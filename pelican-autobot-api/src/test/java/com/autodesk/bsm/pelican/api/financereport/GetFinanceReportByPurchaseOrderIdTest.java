package com.autodesk.bsm.pelican.api.financereport;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is specifically for Find Finance Report By Id (Purchase Order Id) API
 *
 * @author Shweta Hegde
 */
public class GetFinanceReportByPurchaseOrderIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private JobsClient jobsResource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        jobsResource = triggerResource.jobs();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * 1. Submit Add Seats Purchase Order 2. Validate Finance Report for CHARGE and REFUND transactions
     */
    @Test
    public void testFinanceReportByIdForAddSeatsPurchaseOrder() {

        // Submit New Acquisition PO
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), bicSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // Submit Add Seats PO
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        // Process the order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, addSeatsPurchaseOrderId);

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, addSeatsPurchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(addSeatsPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();

        final String creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        // Find Purchase Order By id
        final String actualRestHeader = resource.financeReport().getReportHeader(addSeatsPurchaseOrderId);
        final List<String> actualReport = resource.financeReport().getReportData(addSeatsPurchaseOrderId);

        // Finance Report Validation for CHARGE
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            addSeatsPurchaseOrderId, getBicSubscriptionPlan(), 2, invoiceNumber, null, subscriptionId, null,
            nextBillingDate, true, true, resource, 0.00, assertionErrorList);

        // Finance Report Validation for REFUND
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.REFUND,
            addSeatsPurchaseOrderId, getBicSubscriptionPlan(), 2, null, creditNoteNumber, subscriptionId, null,
            nextBillingDate, true, true, resource, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * 1. Submit Purchase Order with BIC and CLDCR line items 2. Validate Finance Report for CHARGE and CHARGEBACK
     * transactions
     */
    @Test
    public void testFinanceReportByIdOfPurchaseOrderNewAcquisitionMultiLineItems() {

        // Create CLOUD offering (Cloud Credits) to buy along with BIC offering
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings currencyOffering =
            basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.CURRENCY, null,
                Status.ACTIVE, 100, UsageType.COM, "Cloud Offering " + RandomStringUtils.randomAlphanumeric(4), null);

        // submit a purchase order with multiple prices
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 5);
        priceQuantityMap.put(currencyOffering.getIncluded().getPrices().get(0).getId(), 2);

        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            priceQuantityMap, null, true, true, buyerUser);

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();

        // Identify the Index for cloud credit strategy and legacy startegy.
        final int fulfilllmentIndexForBic = purchaseOrderDetailPage.getLineItemsQuantity(1).equals("5") ? 1 : 2;
        final String subscriptionId =
            purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(fulfilllmentIndexForBic);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);

        final String creditNoteNumber = purchaseOrder.getCreditNoteNumber();

        // Get the Finance report using the API using PO id
        final String actualRestHeader = resource.financeReport().getReportHeader(purchaseOrderId);
        final List<String> actualReport = resource.financeReport().getReportData(purchaseOrderId);

        // Finance Report Validation for CHARGE for line item1
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            purchaseOrderId, getBicSubscriptionPlan(), 5, invoiceNumber, null, subscriptionId, null, nextBillingDate,
            true, false, resource, 0.00, assertionErrorList);

        // Finance Report Validation for CHARGE for line item2
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            purchaseOrderId, currencyOffering, 2, invoiceNumber, null, subscriptionId, null, nextBillingDate, true,
            false, resource, 0.00, assertionErrorList);

        // Finance Report Validation for CHARGEBACK for line item1
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport,
            OrderCommand.CHARGEBACK, purchaseOrderId, getBicSubscriptionPlan(), 5, null, creditNoteNumber,
            subscriptionId, null, nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        // Finance Report Validation for CHARGEBACK for line item2
        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport,
            OrderCommand.CHARGEBACK, purchaseOrderId, currencyOffering, 2, null, creditNoteNumber, subscriptionId, null,
            nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when purchase order ID is numeric
     */
    @Test
    public void testErrorFinanceReportByIdWithNonNumericValue() {

        final String errorMessage = resource.financeReport().getReportData("abcdefgh").get(0);

        AssertCollector.assertThat("Incorrect error message", errorMessage,
            equalTo(PelicanErrorConstants.PURCHASE_ORDER_ID_SHOULD_BE_NUMERIC), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when purchase order ID is invalid
     */
    @Test
    public void testErrorFinanceReportByIdWithInvalidPurchaseOrderId() {

        final String invalidPOId = "6457638723";
        final String errorMessage = resource.financeReport().getReportData(invalidPOId).get(0);

        AssertCollector.assertThat("Incorrect error message", errorMessage,
            equalTo(String.format(PelicanErrorConstants.PURCHASE_ORDER_NOT_FOUND, invalidPOId)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
