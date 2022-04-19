package com.autodesk.bsm.pelican.api.financereport;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The test class have finance report tests for different type of purchases 1. BIC, Meta, Basic Offering Single line
 * item 2. New Acquisition Purchases with CreditCard and Paypal BIC, Meta 3. Single line item Renewal Purchases with
 * Credit Card and Paypal 4. BIC, Meta, Basic Offering Multi line item New Acquisition Purchases with Credit Card and
 * Paypal 5. BIC, Meta Multi line item Renewal Purchases with Credit Card and Paypal All these combinations are tested
 * with different promotions
 *
 * @author Shweta Hegde
 */
public class FinanceReportTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PelicanTriggerClient triggerResource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private Offerings bicOffering1;
    private Offerings bicOffering2;
    private Offerings bicOffering3;
    private Offerings metaOffering1;
    private Offerings metaOffering2;
    private Offerings basicOffering1;
    private String bicPrice1;
    private String bicPrice2;
    private String bicPrice3;
    private String metaPrice1;
    private String metaPrice2;
    private String basicPrice1;
    private JPromotion discountAmountPromo1;
    private JPromotion discountPercentagePromo1;
    private JPromotion supplementPromo;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private JobsClient jobsResource;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        jobsResource = triggerResource.jobs();

        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        // Add subscription plans
        bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        metaOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        metaOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        bicPrice1 = bicOffering1.getIncluded().getPrices().get(0).getId();
        bicPrice2 = bicOffering2.getIncluded().getPrices().get(0).getId();
        bicPrice3 = bicOffering3.getIncluded().getPrices().get(0).getId();
        metaPrice1 = metaOffering1.getIncluded().getPrices().get(0).getId();
        metaPrice2 = metaOffering2.getIncluded().getPrices().get(0).getId();

        // Add basic offering
        basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 100, UsageType.COM, null, null);

        basicPrice1 = basicOffering1.getIncluded().getPrices().get(0).getId();

        // Add promotions for subscription offers
        discountAmountPromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering1, metaOffering1, bicOffering2, metaOffering2, bicOffering3),
                promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, "10.00",
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        discountPercentagePromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering1, metaOffering1, bicOffering2, metaOffering2, bicOffering3),
                promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "10.00", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        supplementPromo = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOffering1, metaOffering1, bicOffering2, metaOffering2, bicOffering3),
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, null,
            DateTimeUtils.getUTCFutureExpirationDate(), "2", "MONTH", null, null, null);

        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Test to validate CHARGE, REFUND & CHARGE BACK transactions for NEW ACQUISITION are added in finance report when
     * an EBSO user clicks on Mark as Refunded in AT/Refund/Chargeback
     */
    @Test(dataProvider = "subscriptionPlanAndBasicOfferingDetailsForNewAcquisitionSingleLineItem")
    public void testFinanceReportForNewAcquisitionSingleLineItem(final String priceId, final int quantity,
        final PaymentType paymentType, final Offerings offering, final String refundOption) {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, quantity);

        final String purchaseOrderId =
            purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, priceQuantityMap, null, true, true, buyerUser);

        String subscriptionId = null;
        String nextBillingDate = null;
        if (offering.getOfferings().get(0).getOfferingType() == OfferingType.BIC_SUBSCRIPTION
            || offering.getOfferings().get(0).getOfferingType() == OfferingType.META_SUBSCRIPTION) {

            subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems().get(0)
                .getOffering().getOfferingResponse().getSubscriptionId();

            final SubscriptionDetailPage subscriptionDetailPage =
                findSubscriptionsPage.findBySubscriptionId(subscriptionId);
            nextBillingDate = subscriptionDetailPage.getNextBillingDate();
        }

        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            quantity, purchaseOrderId, offering, findPurchaseOrdersPage, true, false, purchaseOrderUtils, refundOption,
            nextBillingDate, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass different offering type, payment type and quantity for Single Line Item and new acquisition
     */
    @DataProvider(name = "subscriptionPlanAndBasicOfferingDetailsForNewAcquisitionSingleLineItem")
    private Object[][] getTestDataForNewAcquisitionSingleLineItem() {
        return new Object[][] { { metaPrice1, 1, PaymentType.PAYPAL, metaOffering1, PelicanConstants.MARK_AS_REFUND },
                { bicPrice2, 100, PaymentType.CREDIT_CARD, bicOffering2, OrderCommand.REFUND.toString() },
                { basicPrice1, 4, PaymentType.PAYPAL, basicOffering1, OrderCommand.CHARGEBACK.toString() } };
    }

    /**
     * Test to validate CHARGE and REFUND/CHARGEBACK transactions for AUTO RENEWAL are added in finance report when an
     * EBSO user clicks on Mark as Refunded in AT/Refund/Chargeback
     */
    @Test(dataProvider = "subscriptionPlanDetailsForAutoRenewalSingleLineItem")
    public void testFinanceReportForAutoRenewalSingleLineItem(final String priceId, final int quantity,
        final PaymentType paymentType, final Offerings offering, final String refundOption) {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, quantity);

        String purchaseOrderId =
            purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, priceQuantityMap, null, true, true, buyerUser);

        // getting purchase order detail page with charge

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);

        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, null, listOfSubscriptions, false,
            true, buyerUser);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            quantity, purchaseOrderId, offering, findPurchaseOrdersPage, false, false, purchaseOrderUtils, refundOption,
            subscriptionDetailPage.getNextBillingDate(), 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass different offering type, payment type and quantity for Single Line Item and auto renewal
     */
    @DataProvider(name = "subscriptionPlanDetailsForAutoRenewalSingleLineItem")
    private Object[][] getTestDataForAutoRenewalSingleLineItem() {
        return new Object[][] {
                { metaPrice1, 2, PaymentType.PAYPAL, metaOffering1, OrderCommand.CHARGEBACK.toString() },
                { bicPrice2, 100, PaymentType.CREDIT_CARD, bicOffering2, PelicanConstants.MARK_AS_REFUND } };
    }

    /**
     * This method tests Finance Report for multi line New Acquisition Purchase order Charge and Refund, when EBSO user
     * clicks on Mark as Refunded in AT/Refund/Chargeback
     */
    @Test(dataProvider = "subscriptionPlanAndBasicOfferingDetailsForNewAcquisitionMultiLineItems")
    public void testFinanceReportWithNewAcquisitionMultiLineItems(final String priceId1, final String priceId2,
        final int quantity1, final int quantity2, final PaymentType paymentType, final Offerings offering1,
        final Offerings offering2, final String refundOption) {

        // get the start time
        final String expStartDateTime = DateTimeUtils.getNowAsUTC(PelicanConstants.DB_DATE_FORMAT);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // submit a purchase order with multiple prices
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId1, quantity1);
        priceQuantityMap.put(priceId2, quantity2);

        final String purchaseOrderId =
            purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, priceQuantityMap, null, true, true, buyerUser);
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);
        final String subscriptionIdForSecondLineItem = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(2);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        OrderCommand orderCommand = OrderCommand.REFUND;
        if (refundOption.equals(OrderCommand.REFUND.toString())) {

            purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        } else if (refundOption.equals(OrderCommand.CHARGEBACK.toString())) {

            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);
            orderCommand = OrderCommand.CHARGEBACK;
        } else if (refundOption.equals(PelicanConstants.MARK_AS_REFUND)) {

            // Click on mark as refunded
            purchaseOrderDetailPage.clickMarkAsRefunded();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);
        }

        purchaseOrderDetailPage.refreshPage();
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();
        final String creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();

        // Get the Finance report using the API using end timestamp after PO is
        // created
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 3);
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            purchaseOrderId, offering1, quantity1, invoiceNumber, null, subscriptionId, subscriptionIdForSecondLineItem,
            nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, orderCommand,
            purchaseOrderId, offering1, quantity1, null, creditNoteNumber, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            purchaseOrderId, offering2, quantity2, invoiceNumber, null, subscriptionId, subscriptionIdForSecondLineItem,
            nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, orderCommand,
            purchaseOrderId, offering2, quantity2, null, creditNoteNumber, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, true, false, resource, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass different offering type, payment type and quantity for Multi Line Item and New Acquisition
     */
    @DataProvider(name = "subscriptionPlanAndBasicOfferingDetailsForNewAcquisitionMultiLineItems")
    private Object[][] getTestDataNewAcquisitionMultiLineItems() {
        return new Object[][] {
                { metaPrice1, metaPrice2, 1, 3, PaymentType.CREDIT_CARD, metaOffering1, metaOffering2,
                        OrderCommand.REFUND.toString() },
                { bicPrice1, bicPrice3, 30, 200, PaymentType.PAYPAL, bicOffering1, bicOffering3,
                        OrderCommand.CHARGEBACK.toString() } };
    }

    /**
     * This method tests Finance Report for multi line multi seats for BiC Subscription Renewal and Meta Subscription
     * Renewal and ChargeBack, EBSO user clicks on Mark as Refunded in AT/Refund/Chargeback
     */
    @Test(dataProvider = "subscriptionPlanDetailsForAutoRenewalMultiLineItems")
    public void testFinanceReportWithAutoRenewalMultiLine(final String priceId1, final String priceId2,
        final int quantity1, final int quantity2, final PaymentType paymentType, final Offerings offering1,
        final Offerings offering2, final String refundOption) {

        // get the start time
        final String expStartDateTime = DateTimeUtils.getNowAsUTC(PelicanConstants.DB_DATE_FORMAT);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // submit a purchase order with multiple prices
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId1, quantity1);
        priceQuantityMap.put(priceId2, quantity2);

        final String newAcquisitionPurchaseOrderId =
            purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, priceQuantityMap, null, true, true, buyerUser);

        findPurchaseOrdersPage.findPurchaseOrderById(newAcquisitionPurchaseOrderId);
        PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);
        final String subscriptionIdForSecondLineItem = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(2);
        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);
        listOfSubscriptions.add(subscriptionIdForSecondLineItem);

        // Renew the Order.
        final String renewedPurchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(paymentType, null,
            listOfSubscriptions, false, true, buyerUser);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, renewedPurchaseOrderId);
        findPurchaseOrdersPage.findPurchaseOrderById(renewedPurchaseOrderId);
        purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final OrderCommand orderCommand = OrderCommand.REFUND;
        if (refundOption.equals(OrderCommand.REFUND.toString())) {

            purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, renewedPurchaseOrderId);
            Util.waitInSeconds(TimeConstants.THREE_SEC);
        } else if (refundOption.equals(PelicanConstants.MARK_AS_REFUND)) {
            // Click on mark as refunded
            purchaseOrderDetailPage.clickMarkAsRefunded();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);
        }

        purchaseOrderDetailPage.refreshPage();
        final String invoiceNumber = purchaseOrderDetailPage.getInvoiceNumber();
        final String creditNoteNumber = purchaseOrderDetailPage.getCreditNoteNumber();

        // Get the Finance report using the API using end timestamp after PO is created
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 3);
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            renewedPurchaseOrderId, offering1, quantity1, invoiceNumber, null, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, false, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, orderCommand,
            renewedPurchaseOrderId, offering1, quantity1, null, creditNoteNumber, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, false, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, OrderCommand.CHARGE,
            renewedPurchaseOrderId, offering2, quantity2, invoiceNumber, null, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, false, false, resource, 0.00, assertionErrorList);

        HelperForAssertionsOfFinanceReport.assertionsForLineItems(actualRestHeader, actualReport, orderCommand,
            renewedPurchaseOrderId, offering2, quantity2, null, creditNoteNumber, subscriptionId,
            subscriptionIdForSecondLineItem, nextBillingDate, false, false, resource, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass different offering type, payment type and quantity for Multi Line Item and New Acquisition
     */
    @DataProvider(name = "subscriptionPlanDetailsForAutoRenewalMultiLineItems")
    private Object[][] getTestDataAutoRenewalMultiLineItems() {
        return new Object[][] {
                { metaPrice1, metaPrice2, 1, 3, PaymentType.CREDIT_CARD, metaOffering1, metaOffering2,
                        OrderCommand.REFUND.toString() },
                { bicPrice1, bicPrice3, 30, 200, PaymentType.PAYPAL, bicOffering1, bicOffering3,
                        PelicanConstants.MARK_AS_REFUND } };
    }

    /**
     * Step1: Create Meta Order in AUTH, process to PENDING and CHARGE Step2: REFUND the purchase order Step3: Fulfill
     * the PO Step4: Verify Finance Report
     */
    @Test
    public void testFinanceReportFulfillmentForMetaAfterRefund() throws ParseException {

        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUsPriceId(), buyerUser, 2);

        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);

        final String nextBillingDate = DateTimeUtils.getNextBillingDate(
            DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH), BillingFrequency.MONTH.toString());

        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            2, purchaseOrderId, getMetaSubscriptionPlan(), findPurchaseOrdersPage, true, false, purchaseOrderUtils,
            null, nextBillingDate, 0.00, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This tests that price edit/update while Purchase Order is in Auth, will not affect the CHARGE amount. Step1 :
     * Place new acquisition order in AUTH with amount x. Step2 : Update the price to x+20 by updating DB. Step3 :
     * Process the PO to PENDING and CHARGE. Step4 : Verify that unit price, total price etc are same as what was
     * promised in AUTH.
     */
    @Test
    public void testFinanceReportWhenPriceIsHonoredAtAuth() {

        final Offerings offerings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offerings.getIncluded().getPrices().get(0).getId();

        final int quantity = 2;
        // Submit New Acquisition order
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, quantity).getId();

        // Get Price Amount
        final String priceAmount =
            DbUtils.selectQuery(PelicanDbConstants.SELECT_AMOUNT_FROM_SUBSCRIPTION_PRICE + priceId,
                PelicanDbConstants.AMOUNT_DB_FIELD, getEnvironmentVariables()).get(0);

        final Double updatedPrice1 = Double.parseDouble(priceAmount) + 20;
        // Changing DB to update Price Id.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_PRICE_AMOUNT, String.valueOf(updatedPrice1), priceId),
            getEnvironmentVariables());

        // Process the PO to PENDING
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        // Process the PO to CHARGE
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            quantity, purchaseOrderId, offerings, findPurchaseOrdersPage, true, false, purchaseOrderUtils,
            OrderCommand.REFUND.toString(), nextBillingDate, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test the promo setup column headers and values in finance report for New Acquisition, BIC, Single
     * Line item with Discount Amount Promotion.
     */
    @Test
    public void testFinanceReportPromotionFieldsWithNewAcquisitionSingleLineWithDiscountAmountPromo() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        final String priceId = bicOffering1.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.put(priceId, 1);

        // Submit purchase order
        final Map<JPromotion, Offerings> promoOfferingsMap = new HashMap<>();
        promoOfferingsMap.put(discountAmountPromo1, bicOffering1);

        final Map<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        final PromotionReferences promotionReferences = new PromotionReferences();

        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(promoOfferingsMap.keySet().iterator().next().getData().getId());
        promotionReferences.setPromotionReference(promotionReference);

        priceQuantityMap.put(priceId, 1);
        pricePromoReferencesMap.put(priceId, promotionReferences);
        // Submit PO with AUTHORIZE, PENDING and CHARGE
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        // Get the Finance report and assert data
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            1, purchaseOrderId, bicOffering1, findPurchaseOrdersPage, true, false, purchaseOrderUtils,
            OrderCommand.REFUND.toString(), nextBillingDate, 0.00, assertionErrorList);
        HelperForAssertionsOfFinanceReport.assertionsForPromotionRelatedFields(resource, purchaseOrder.getId(),
            new ArrayList<>(ImmutableList.of(discountAmountPromo1)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test the promo setup column headers and values in finance report for Auto renewal, Meta, Single Line
     * item with Percentage Promotion.
     */
    @Test
    public void testFinanceReportPromotionFieldsWithRenewalSingleLineWithPercentagePromo() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        final String priceId = metaOffering1.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.put(priceId, 1);

        // Submit purchase order
        final Map<JPromotion, Offerings> promoOfferingsMap = new HashMap<>();
        promoOfferingsMap.put(discountPercentagePromo1, metaOffering1);

        final Map<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        final PromotionReferences promotionReferences = new PromotionReferences();

        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(promoOfferingsMap.keySet().iterator().next().getData().getId());
        promotionReferences.setPromotionReference(promotionReference);

        priceQuantityMap.put(priceId, 1);
        pricePromoReferencesMap.put(priceId, promotionReferences);
        // Submit PO with AUTHORIZE, PENDING and CHARGE
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // get purchase order api response
        final PurchaseOrder savedNewPO = resource.purchaseOrder().getById(purchaseOrder.getId());
        final List<String> subscriptionIdList = new ArrayList<>();
        final String subscriptionId =
            savedNewPO.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscriptionIdList.add(subscriptionId);
        final Map<String, PromotionReferences> subscriptionPromoReferencesMap = new HashMap<>();
        subscriptionPromoReferencesMap.put(subscriptionId, promotionReferences);
        purchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIdList, false,
            PaymentType.CREDIT_CARD, subscriptionPromoReferencesMap, true);

        final String purchaseOrderId = purchaseOrder.getId();

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        // Get the Finance report and assert data
        HelperForAssertionsOfFinanceReport.helperForFinanceReportTests(resource, triggerResource, subscriptionId, null,
            1, purchaseOrderId, metaOffering1, findPurchaseOrdersPage, false, false, purchaseOrderUtils,
            OrderCommand.CHARGEBACK.toString(), nextBillingDate, 0.00, assertionErrorList);
        HelperForAssertionsOfFinanceReport.assertionsForPromotionRelatedFields(resource, purchaseOrder.getId(),
            new ArrayList<>(ImmutableList.of(discountPercentagePromo1)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test the promo setup column headers and values in finance report for New Acquisition, BIC, Multi Line
     * item with all three kind of promotions.
     */
    @Test
    public void testFinanceReportPromotionFieldsWithMultiLineItemsAndAllPromotions() {

        // Submit purchase order
        final Map<JPromotion, Offerings> promoOfferingsMap = new HashMap<>();
        promoOfferingsMap.put(discountAmountPromo1, bicOffering1);
        promoOfferingsMap.put(discountPercentagePromo1, bicOffering2);
        promoOfferingsMap.put(supplementPromo, bicOffering3);

        final Map<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        final Map<String, Integer> priceQuantityMap = new HashMap<>();

        // Handle multiple line items
        for (final Map.Entry<JPromotion, Offerings> entry : promoOfferingsMap.entrySet()) {
            priceQuantityMap.put(entry.getValue().getIncluded().getPrices().get(0).getId(), 1);
        }

        for (final Map.Entry<JPromotion, Offerings> entry : promoOfferingsMap.entrySet()) {
            final PromotionReferences promotionReferences = new PromotionReferences();
            final PromotionReference promoRef = new PromotionReference();
            promoRef.setId(entry.getKey().getData().getId());
            promotionReferences.setPromotionReference(promoRef);
            pricePromoReferencesMap.put(entry.getValue().getIncluded().getPrices().get(0).getId(), promotionReferences);
        }
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        // Get the Finance report and assert data
        HelperForAssertionsOfFinanceReport.assertionsForPromotionRelatedFields(resource, purchaseOrder.getId(),
            new ArrayList<>(ImmutableList.of(discountAmountPromo1, discountPercentagePromo1, supplementPromo)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method will test the validation of the generate finance report api with last modified date ranges
     * greater than 31 days
     */
    @Test
    public void testErrorDisplayedWithGreaterDateRanges() {

        final List<String> entityDateList =
            DateTimeUtils.getStartDateAndEndDateGreaterRanges(PelicanConstants.DB_DATE_FORMAT);

        final String errorMessage = resource.financeReport()
            .getReportData(null, null, null, null, entityDateList.get(0), entityDateList.get(1)).get(0);
        // Get the generate finance report api error response as string and
        // parse the error message in the string
        AssertCollector.assertThat("Incorrect error message in the api response", errorMessage,
            equalTo(PelicanErrorConstants.INVALID_LAST_MODIFIED_DATE_RANGE
                + getEnvironmentVariables().getFinanceReportDateRange() + " " + PelicanConstants.DAYS_LOWER_CASE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests the error displayed if you don't provide any search parameters in the api request
     */
    @Test
    public void testErrorDisplayedWithoutAnyParameters() {

        final String errorMessage = resource.financeReport().getReportData(null, null, null, null, null, null).get(0);
        AssertCollector.assertThat("Incorrect error message in the api response", errorMessage,
            equalTo(PelicanErrorConstants.EMPTY_DATE_RANGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests the displayed error message if we only provide one field of last modified date
     */
    @Test
    public void testErrorMessageDisplayedForOneDateParameterProvided() {

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        // Get the generate finance report api error response as string and
        // parse the error message in the string
        final String errorMessage =
            resource.financeReport().getReportData(null, null, null, null, entityDateList.get(0), null).get(0);
        AssertCollector.assertThat("Incorrect error message in the api response", errorMessage,
            equalTo(PelicanErrorConstants.EMPTY_LAST_MODIFIED_END_DATE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests the error displayed if the last modified start date is later than the last modified end date
     */
    @Test
    public void testErrorDisplayedForStartDateLaterThanEndDate() {

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        // Get the generate finance report api error response as string and
        // parse the error message in the string
        final String errorMessage = resource.financeReport()
            .getReportData(null, null, null, null, entityDateList.get(1), entityDateList.get(0)).get(0);

        AssertCollector.assertThat("Incorrect error message in the api response", errorMessage,
            equalTo(PelicanErrorConstants.INVALID_LAST_MODIFIED_END_DATE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests whether there are no records in the finance report for future last modified start date and
     * end dates
     */
    @Test
    public void testFinanceReportForFutureLastModifiedStartAndEndDates() {

        final List<String> entityDateList = DateTimeUtils.getFutureStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        // Get the finance report header using the generate finance report api
        // with the order start date and end date in future
        final String actualFinanceReportHeader = resource.financeReport().getReportHeader(entityDateList.get(0),
            entityDateList.get(1), null, null, null, null);
        final List<String> headerList = Arrays.asList(actualFinanceReportHeader.split(","));
        final String actualField = headerList.get(51);
        final List<String> actualReport = resource.financeReport().getReportData(null, null, null, null,
            entityDateList.get(0), entityDateList.get(1));

        AssertCollector.assertThat("Incorrect finance report", actualReport.size(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Last modified Date is not found in the finance report header", actualField,
            equalTo("Last Modified Date"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
