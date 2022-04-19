package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering.OfferingResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Shipping;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionQuantity.SubscriptionQuantityResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Transaction;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.DeclineReason;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.StateProvince;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test Case : Get Purchase Order API
 *
 * @author Shweta Hegde
 */
public class GetPurchaseOrderByIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private String purchaseOrderIdForBicCreditCard;
    private PaymentProfile paymentProfile;
    private String priceIdForBic2;
    private String priceIdForCloud;
    private PurchaseOrderUtils purchaseOrderUtils;
    private Offerings bicOfferings2;
    private JobsClient jobsResource;
    private JPromotion amountDiscountPromo;
    private JPromotion percentageDiscountPromo;
    private JPromotion supplementTimePromo;
    private HashMap<String, PromotionReferences> pricePromoReferencesMap;
    private PromotionReferences promotionReferences;
    private PromotionReference promotionReference;
    private Double unitPrice;
    private Double amountCharged;
    private Double discountAmount;
    private Double totalAmount;
    private Double unitPriceForSecondLineItem;
    private Double totalAmountForSecondLineItem;
    private Double amountChargedForSecondLineItem;
    private static final int QUANTITY = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPurchaseOrderByIdTest.class.getSimpleName());
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BuyerUser buyerUser;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Creating 3 offerings for BIC, Meta and Perpetual
        bicOfferings2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.QUARTER, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings cloudOfferings = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.CURRENCY, MediaType.USB, Status.ACTIVE, UsageType.COM, null);

        // create multiple promotions to submit PO for BIC product
        amountDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        percentageDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings2), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, "25", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        supplementTimePromo =
            promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOfferings2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
                null, DateTimeUtils.getUTCFutureExpirationDate(), "10", "DAY", null, null, null);
        // Creating price id for BIC, Meta and Perpetual
        priceIdForBic2 = bicOfferings2.getIncluded().getPrices().get(0).getId();
        priceIdForCloud = cloudOfferings.getIncluded().getPrices().get(0).getId();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        paymentProfile = new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
            .addCreditCardPaymentProfile(getUser().getId(), PaymentProcessor.BLUESNAP_NAMER.getValue());

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG, true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG,
                false);
        }
    }

    /**
     * Test Bic Purchase order with credit card in Authorize state
     */
    @Test
    public void getBICAuthorizedPurchaseOrderWithCreditCard() {
        // Get purchase order Id to test the api
        purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY)
            .getId();
        // Get Purchase Order by id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);

        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(),
            equalTo(purchaseOrderIdForBicCreditCard), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.AUTHORIZED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering Request Price Id ",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
            is(QUANTITY), assertionErrorList);
        AssertCollector.assertThat("Shipping is not null", purchaseOrder.getShipping(), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_NAMER.getValue()), assertionErrorList);
        helperToValidateAssertions(purchaseOrder, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Bic Purchase order with credit card in Pending state
     */
    @Test(dependsOnMethods = { "getBICAuthorizedPurchaseOrderWithCreditCard" })
    public void getBICPendingPurchaseOrderWithCreditCard() {
        // Purchase order created in previous method is processed to "Pending'
        // state and validate get purchase order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        // api response
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(),
            equalTo(purchaseOrderIdForBicCreditCard), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment status ", purchaseOrder.getFulFillmentStatus().toString(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering Request Price Id ",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment strategy",
            purchaseOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStrategy(),
            is(FulFillmentStrategy.BIC), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment group status ",
            purchaseOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStatus().toString(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
            is(QUANTITY), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_NAMER.getValue()), assertionErrorList);
        helperToValidateAssertions(purchaseOrder, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test meta Purchase order with Paypal in Charged state
     */
    @Test
    public void getMetaChargedPurchaseOrderWithPaypal() {
        final int quantity = 3;
        final String purchaseOrderIdForMetaPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, getMetaMonthlyUsPriceId(), buyerUser, quantity)
            .getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderIdForMetaPaypal);

        // Purchase order created in previous method is processed to "Charged'
        // state and validate get purchase order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMetaPaypal);
        final PurchaseOrder purchaseOrderForMeta = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);

        purchaseOrderUtils.fulfillRequest(purchaseOrderForMeta, FulfillmentCallbackStatus.Created);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForMetaPaypal);// api
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(),
            equalTo(purchaseOrderIdForMetaPaypal), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment status ", purchaseOrder.getFulFillmentStatus().toString(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering Request Price Id ",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(getMetaMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment strategy",
            purchaseOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStrategy(),
            is(FulFillmentStrategy.LEGACY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment group status ",
            purchaseOrder.getFulFillmentGroups().getFulfillmentGroups().get(0).getStatus().toString(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity", purchaseOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getChargeDetails().getQuantity(), is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect unit price",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getChargeDetails()
                .getUnitPrice(),
            equalTo(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
        AssertCollector.assertThat("Incorrect total price",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getChargeDetails()
                .getTotalPrice(),
            is(Float.parseFloat(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getAmount()) * quantity),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.PAYPAL_EMEA.getValue()), assertionErrorList);
        helperToValidateAssertions(purchaseOrder, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify whether we are able to a submit a purchase order for Cloud Credits with payment as Paypal Profile.
     *
     * @result validates the Paypal Stored Profile Payment Id, Purchase order Order state and Fulfillment Status
     */
    @Test
    public void purchaseOrderWithPaypalProfileForCloudCredits() {
        // submit PO for BIC with Paypal
        PurchaseOrder purchaseOrderForCloudPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForCloud, getBuyerUser(), QUANTITY);
        // process the PO to 'pending' state
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), purchaseOrderForCloudPaypal.getId());
        // process the PO to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForCloudPaypal.getId());
        // get purchase order api response
        purchaseOrderForCloudPaypal = resource.purchaseOrder().getById(purchaseOrderForCloudPaypal.getId());
        AssertCollector.assertThat("Incorrect order state", purchaseOrderForCloudPaypal.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment Status",
            purchaseOrderForCloudPaypal.getFulFillmentStatus().toString(),
            equalTo(FulFillmentStatus.FULFILLED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify whether we are able to a renew a BIC Subscription with payment as Paypal Profile.
     *
     * @result validates the Paypal Stored Profile Payment Id, Subscription Id and Subscription Offer Id
     */
    @Test
    public void renewOrderWithPaypalForBicSubscription() {
        // submit PO for BIC with Paypal
        PurchaseOrder purchaseOrderForBicPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser, QUANTITY);
        // process the PO to 'pending' state
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderForBicPaypal.getId());
        // process the PO to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicPaypal.getId());
        // get purchase order api response
        purchaseOrderForBicPaypal = resource.purchaseOrder().getById(purchaseOrderForBicPaypal.getId());
        // get the created subscription id
        final String subscriptionIdForBicPaypal = purchaseOrderForBicPaypal.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        // submit Subscription Renewal PO for BIC with Paypal
        PurchaseOrder renewalOrderForBicPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForBicPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        // process the PO to 'pending' state
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalOrderForBicPaypal.getId());
        // process the PO to 'charged' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalOrderForBicPaypal.getId());
        // get purchase order api response
        renewalOrderForBicPaypal = resource.purchaseOrder().getById(renewalOrderForBicPaypal.getId());
        // get the next billing date of the subscription
        final String subscriptionNextBillingDateForBicPaypal = renewalOrderForBicPaypal.getLineItems().getLineItems()
            .get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse().getNextBillingDate();
        AssertCollector.assertThat("Incorrect Subscription Id",
            renewalOrderForBicPaypal.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getSubscriptionId(),
            equalTo(subscriptionIdForBicPaypal), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Subscription Offer Id", renewalOrderForBicPaypal.getLineItems().getLineItems().get(0)
                .getSubscriptionRenewal().getSubscriptionRenewalResponse().getSubscriptionOfferId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription next billing date",
            renewalOrderForBicPaypal.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getNextBillingDate(),
            equalTo(subscriptionNextBillingDateForBicPaypal), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will submit a purchase order with shipping info to generate a invoice number and verify the
     * country is picked up from shipping not from stored payment profile
     *
     * @result Country will be picked up from Shipping info in Invoice number
     */
    @Test
    public void invoiceNumberGenerationWithShippingAndSpp() {
        // add shipping details
        final Shipping shippingInformation = PurchaseOrderUtils.getShippingInformation("Enrique", "Iglesias",
            "532 Jones St", "apt # 007", "YokoHama", StateProvince.CALIFORNIA, "94102", Country.MX);
        final Map<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);

        // submit complete PO with three steps process Authorize, Pending and
        // Charge
        final PurchaseOrder newPurchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo(priceQuantityMap, true,
                PaymentType.CREDIT_CARD, null, null, shippingInformation, buyerUser);
        final String poId = newPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, poId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, poId);

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // Get the invoiceNumber in the 'GetPurchaseOrderById' response
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);
        final String invoiceNumber = getPurchaseOrder.getInvoiceNumber();

        AssertCollector.assertThat("Why invoice number is empty ?", invoiceNumber, notNullValue(), assertionErrorList);
        if (getPurchaseOrder.getShipping().getShipTo().getCountry() != null) {
            AssertCollector.assertThat("Country was not picked up from Shipping info",
                (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
                equalTo((shippingInformation.getShipTo().getCountry()).toString()), assertionErrorList);
        } else {
            AssertCollector.assertThat("Country is picked up from spp",
                (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
                not(equalTo((newPurchaseOrder.getPayment().getCreditCard().getCountryCode()))), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will submit a purchase order with billing, shipping info to generate a invoice number and verify
     * the country is picked up from shipping in the presence of billingInfo
     *
     * @result Country will be picked up from Shipping info in Invoice number
     */
    @Test
    public void invoiceNumberGenerationWithShippingAndBillingInfo() {
        // add shipping details
        final Shipping shippingInformation = PurchaseOrderUtils.getShippingInformation("Enrique", "Iglesias",
            "532 Jones St", "apt # 007", "YokoHama", StateProvince.CALIFORNIA, "94102", Country.MX);
        // add billing details
        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Cruise",
            "HollyWood", "mission", "impossible 6", "94007", "CALIFORNIA", "San Francisco", Country.FR, "(420)840-0007",
            "007", PaymentType.CREDIT_CARD.getValue(), "VISA", "0418", "4444", null);
        // add price id with number of quantities
        final Map<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);

        // submit complete PO with three steps process Authorize, Pending and
        // Charge
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithShippingAndBillingInfo(priceQuantityMap,
                true, PaymentType.CREDIT_CARD, shippingInformation, billingInformation, buyerUser);
        final String poId = newPurchase.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);

        AssertCollector.assertThat("Why invoice number is empty ?", getPurchaseOrder.getInvoiceNumber(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from shipping info",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            equalTo((shippingInformation.getShipTo().getCountry()).toString()), assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from Billing info",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            not(equalTo((billingInformation.getCountryCode()).toString())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will submit a purchase order with billing to generate a invoice number and verify the country is
     * picked up from billing info is shipping info is not provided
     *
     * @result Country will be picked up from billing info if shipping info is not available in Invoice number
     */
    @Test
    public void invoiceNumberGenerationWithBillingInfo() {
        // add billing details
        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Cruise",
            "HollyWood", "mission", "impossible 6", "94007", "CALIFORNIA", "San Francisco", Country.FR, "(420)840-0007",
            "007", PaymentType.CREDIT_CARD.getValue(), "VISA", "0418", "4444", null);

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);
        final PurchaseOrder newPurchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, true, PaymentType.CREDIT_CARD, null, billingInformation, buyerUser);
        final String poId = newPurchaseOrder.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);
        final String invoiceNumber = getPurchaseOrder.getInvoiceNumber();

        AssertCollector.assertThat("Why invoice number is empty ?", invoiceNumber, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from Billing info",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            equalTo((billingInformation.getCountryCode()).toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will submit a purchase order without billing and shipping details to generate a invoice number
     *
     * @result Country will be picked up from stored payment profile if shipping and billing info are not provided in
     *         Invoice number
     */
    @Test
    public void invoiceNumberGenerationWithoutBillingAndShippingInfo() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);
        final PurchaseOrder newPurchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, null, null, buyerUser);
        final String poId = newPurchaseOrder.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);
        final String invoiceNumber = getPurchaseOrder.getInvoiceNumber();

        AssertCollector.assertThat("Why invoice number is empty ?", invoiceNumber, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from Stored Payment Profile",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            equalTo(paymentProfile.getCreditCard().getCountryCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Po will be submitted with Paypal purchase This test case will submit a purchase order with shipping details to
     * generate a invoice number
     *
     * @result Country will be picked up from shipping info even country is present in ssp for invoiceNumber
     */
    @Test
    public void invoiceNumberGenerationWithShippingAndSppPaypal() {
        // add shipping details
        final Shipping shippingInformation = PurchaseOrderUtils.getShippingInformation("Enrique", "Iglesias",
            "532 Jones St", "apt # 007", "YokoHama", StateProvince.CALIFORNIA, "94102", Country.MX);
        final Map<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);

        // submit complete PO with the steps process Authorize, Pending and
        // Charge
        final PurchaseOrder newPurchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithShippingAndBillingInfo(priceQuantityMap,
                false, PaymentType.PAYPAL, shippingInformation, null, buyerUser);
        final String poId = newPurchaseOrder.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);
        final String invoiceNumber = getPurchaseOrder.getInvoiceNumber();

        AssertCollector.assertThat("Why invoice number is empty ?", invoiceNumber, notNullValue(), assertionErrorList);
        if (getPurchaseOrder.getShipping().getShipTo().getCountry() != null) {
            AssertCollector.assertThat("Country was not picked up from Shipping info",
                (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
                equalTo((shippingInformation.getShipTo().getCountry()).toString()), assertionErrorList);
        } else {
            AssertCollector.assertThat("Country is picked up from spp",
                (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
                not(equalTo((newPurchaseOrder.getPayment().getCreditCard().getCountryCode()))), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Po will be submitted with Paypal purchase This test case will submit a purchase order with billing, shipping info
     * to generate a invoice number and verify the country will be picked up from shipping even the country exists in
     * billingInfo and spp
     *
     * @result Country will be picked up from Shipping info for Invoice number
     */
    @Test
    public void invoiceNumberGenerationWithShippingBillingSppPayPal() {
        // add shipping details
        final Shipping shippingInformation = PurchaseOrderUtils.getShippingInformation("Enrique", "Iglesias",
            "532 Jones St", "apt # 007", "YokoHama", StateProvince.CALIFORNIA, "94102", Country.MX);
        // add billing details
        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Cruise",
            "HollyWood", "mission", "impossible 6", "94007", "CALIFORNIA", "San Francisco", Country.FR, "(420)840-0007",
            "007", PaymentType.CREDIT_CARD.getValue(), "VISA", "0418", "4444", null);
        // add price id with number of quantities
        final Map<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);

        // submit complete PO with three steps process Authorize, Pending and
        // Charge
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithShippingAndBillingInfo(priceQuantityMap,
                true, PaymentType.PAYPAL, shippingInformation, billingInformation, buyerUser);
        final String poId = newPurchase.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);

        AssertCollector.assertThat("Why invoice number is empty ?", getPurchaseOrder.getInvoiceNumber(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from shipping info",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            equalTo((shippingInformation.getShipTo().getCountry()).toString()), assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from Billing info",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            not(equalTo((billingInformation.getCountryCode()).toString())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Po will be submitted with Paypal purchase This test case will submit a purchase order without billing and
     * shipping details to generate a invoice number
     *
     * @result Country will be picked up from stored payment profile if shipping and billing info are not provided for
     *         invoiceNumber
     */
    @Test
    public void invoiceNumberGenerationWithoutBillingAndShippingInfoPayPal() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);
        final PurchaseOrder newPurchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
        final String poId = newPurchaseOrder.getId();

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, poId);

        // get po call to check the invoiceNumber into it
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(poId);
        final String invoiceNumber = getPurchaseOrder.getInvoiceNumber();

        AssertCollector.assertThat("Why invoice number is empty ?", invoiceNumber, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Country was not picked up from Stored Payment Profile",
            (getPurchaseOrder.getInvoiceNumber()).substring(4, 6),
            equalTo(paymentProfile.getCreditCard().getCountryCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests BiC Subscription with Multi seats and with Percentage discount promotion
     */
    @Test
    public void testSuccessPurchaseOrderWithMultipleSeatsAndPercentageDiscountPromo() {
        final int quantity = 10;
        // submit a purchase order with price and promotion
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        promotionReferences = new PromotionReferences();
        promotionReference = new PromotionReference();
        promotionReference.setId(percentageDiscountPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        priceQuantityMap.put(priceIdForBic2, quantity);
        pricePromoReferencesMap.put(bicOfferings2.getIncluded().getPrices().get(0).getId(), promotionReferences);
        // submit a purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(bicOfferings2.getIncluded().getPrices().get(0).getAmount());
        discountAmount = percentageDiscountPromo.getData().getDiscountPercent() * 0.01 * unitPrice * quantity;
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount - discountAmount;

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status ", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering Request Price Id ",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(priceIdForBic2), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
            is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect discount amount", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getPromotionDiscount(),
            equalTo(discountAmount.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests BiC Subscription with multi seats and supplement time promotion
     */
    @Test
    public void testSuccessPurchaseOrderWithMultipleSeatsAndSupplementTimePromo() {
        final int quantity = 50;
        // submit a purchase order with price and promotion
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        promotionReferences = new PromotionReferences();
        promotionReference = new PromotionReference();
        promotionReference.setId(supplementTimePromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        priceQuantityMap.put(priceIdForBic2, quantity);
        pricePromoReferencesMap.put(bicOfferings2.getIncluded().getPrices().get(0).getId(), promotionReferences);
        // submit a purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(bicOfferings2.getIncluded().getPrices().get(0).getAmount());
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount;

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status ", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering Request Price Id ",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(priceIdForBic2), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
            is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests BiC and Meta Subscription with multi lines and multi seats
     */
    @Test
    public void testSuccessPurchaseOrderWithBicAndMetaMultiLineMultipleSeats() {
        final int quantity = 150;
        // submit a purchase order with BiC subscription price and Meta
        // Subscription price
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), quantity);
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), quantity);
        // submit a purchase order
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, true, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getAmount());
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount;

        unitPriceForSecondLineItem =
            Double.parseDouble(getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getAmount());
        totalAmountForSecondLineItem = unitPriceForSecondLineItem * quantity;
        amountChargedForSecondLineItem = totalAmountForSecondLineItem;

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity",
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
            is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.PAYPAL_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price for first line item", purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount for first line item", purchaseOrder.getLineItems().getLineItems().get(0)
                .getOffering().getOfferingResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount for first line item", purchaseOrder.getLineItems().getLineItems().get(0)
                .getOffering().getOfferingResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price for second line item", purchaseOrder.getLineItems().getLineItems().get(1)
                .getOffering().getOfferingResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount for second line item", purchaseOrder.getLineItems().getLineItems().get(1)
                .getOffering().getOfferingResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount for second line item", purchaseOrder.getLineItems().getLineItems().get(1)
                .getOffering().getOfferingResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test methods tests BiC Subscription Renewal with multiple seats with Amount Discount promotion
     */
    @Test
    public void testSuccessAutoRenewalPurchaseOrderWithMultipleSeatsAndAmountDiscountPromo() {
        final int quantity = 5;
        // submit a purchase order with price and promotion
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        promotionReferences = new PromotionReferences();
        promotionReference = new PromotionReference();
        promotionReference.setId(amountDiscountPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        priceQuantityMap.put(priceIdForBic2, quantity);
        pricePromoReferencesMap.put(bicOfferings2.getIncluded().getPrices().get(0).getId(), promotionReferences);
        // submit a purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        String purchaseOrderId = purchaseOrder.getId();

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);

        pricePromoReferencesMap.put(subscriptionId, promotionReferences);
        purchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, listOfSubscriptions, false,
            PaymentType.CREDIT_CARD, pricePromoReferencesMap, true);
        purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(bicOfferings2.getIncluded().getPrices().get(0).getAmount());
        discountAmount = amountDiscountPromo.getData().getDiscountAmount() * quantity;
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount - discountAmount;

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status ", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription id", purchaseOrder.getLineItems().getLineItems().get(0)
            .getSubscriptionRenewal().getSubscriptionRenewalResponse().getSubscriptionId(), equalTo(subscriptionId),
            assertionErrorList);
        AssertCollector
            .assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalResponse().getChargeDetails().getQuantity(),
                is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect discount amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getPromotionDiscount(),
            equalTo(discountAmount.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests BiC Subscription Renewal with Multi seats and with Percentage discount promotion
     */
    @Test
    public void testSuccessAutoRenewalPurchaseOrderWithMultipleSeatsAndPercentageDiscountPromo() {
        final int quantity = 10;
        // submit a purchase order with price and promotion
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        pricePromoReferencesMap = new HashMap<>();
        promotionReferences = new PromotionReferences();
        promotionReference = new PromotionReference();
        promotionReference.setId(percentageDiscountPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        priceQuantityMap.put(priceIdForBic2, quantity);
        pricePromoReferencesMap.put(bicOfferings2.getIncluded().getPrices().get(0).getId(), promotionReferences);

        // submit a purchase order
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);

        pricePromoReferencesMap.put(subscriptionId, promotionReferences);
        purchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, listOfSubscriptions, false,
            PaymentType.CREDIT_CARD, pricePromoReferencesMap, true);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(bicOfferings2.getIncluded().getPrices().get(0).getAmount());
        discountAmount = percentageDiscountPromo.getData().getDiscountPercent() * 0.01 * unitPrice * quantity;
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount - discountAmount;

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status ", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription id", purchaseOrder.getLineItems().getLineItems().get(0)
            .getSubscriptionRenewal().getSubscriptionRenewalResponse().getSubscriptionId(), equalTo(subscriptionId),
            assertionErrorList);
        AssertCollector
            .assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                    .getSubscriptionRenewalResponse().getChargeDetails().getQuantity(),
                is(quantity), assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect unit price", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice(),
            equalTo(String.format("%.2f", unitPrice)), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect discount amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getPromotionDiscount(),
            equalTo(discountAmount.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect charge amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged(),
            is(amountCharged.floatValue()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect total amount", purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice(),
            is(totalAmount.floatValue()), assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests BiC Subscription Renewal with multi line items with multi seats
     */
    @Test
    public void testSuccessAutoRenewalPurchaseOrderWithBicMultiLineMultipleSeats() {
        final int quantity = 100;
        final int quantityForSecondLineItem = 300;
        final Set<Integer> quantitySet = new HashSet<>();
        quantitySet.add(quantity);
        quantitySet.add(quantityForSecondLineItem);

        // submit a purchase order with multiple prices
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic2, quantity);
        priceQuantityMap.put(getBicMonthlyUsPriceId(), quantityForSecondLineItem);
        // submit a purchase order
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, true, buyerUser);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String subscriptionIdForSecondLineItem =
            purchaseOrder.getLineItems().getLineItems().get(1).getOffering().getOfferingResponse().getSubscriptionId();

        final Set<String> subscriptionIdsSet = new HashSet<>();
        subscriptionIdsSet.add(subscriptionId);
        subscriptionIdsSet.add(subscriptionIdForSecondLineItem);

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);
        listOfSubscriptions.add(subscriptionIdForSecondLineItem);
        purchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, listOfSubscriptions, false,
            PaymentType.PAYPAL, null, true);

        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);

        // calculating amount details to do assertions
        unitPrice = Double.parseDouble(bicOfferings2.getIncluded().getPrices().get(0).getAmount());
        totalAmount = unitPrice * quantity;
        amountCharged = totalAmount;

        unitPriceForSecondLineItem =
            Double.parseDouble(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getAmount());
        totalAmountForSecondLineItem = unitPriceForSecondLineItem * quantityForSecondLineItem;
        amountChargedForSecondLineItem = totalAmountForSecondLineItem;

        final Set<String> unitPriceSet = new HashSet<>();
        unitPriceSet.add(String.format("%.2f", unitPrice));
        unitPriceSet.add(String.format("%.2f", unitPriceForSecondLineItem));

        final Set<Float> amountChargedSet = new HashSet<>();
        amountChargedSet.add(amountCharged.floatValue());
        amountChargedSet.add(amountChargedForSecondLineItem.floatValue());

        final Set<Float> totalAmountSet = new HashSet<>();
        totalAmountSet.add(totalAmount.floatValue());
        totalAmountSet.add(totalAmountForSecondLineItem.floatValue());

        // API response from Get Purchase Order
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect purchase id ", purchaseOrder.getId(), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order state ", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect fulfillment status ", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertTrue(
            "Incorrect subscription id for first line item", subscriptionIdsSet.contains(purchaseOrder.getLineItems()
                .getLineItems().get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse().getSubscriptionId()),
            assertionErrorList);
        AssertCollector.assertTrue(
            "Incorrect subscription id for second line item", subscriptionIdsSet.contains(purchaseOrder.getLineItems()
                .getLineItems().get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse().getSubscriptionId()),
            assertionErrorList);
        AssertCollector.assertTrue(
            "Incorrect QUANTITY for first line item ", quantitySet.contains(purchaseOrder.getLineItems().getLineItems()
                .get(0).getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getQuantity()),
            assertionErrorList);
        AssertCollector.assertTrue(
            "Incorrect QUANTITY for second line item ", quantitySet.contains(purchaseOrder.getLineItems().getLineItems()
                .get(1).getSubscriptionRenewal().getSubscriptionRenewalResponse().getChargeDetails().getQuantity()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect payment processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.PAYPAL_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertTrue("Incorrect unit price for first line item",
            unitPriceSet.contains(purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect charge amount for first line item",
            amountChargedSet.contains(purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect total amount for first line item",
            totalAmountSet.contains(purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect unit price for second line item",
            unitPriceSet.contains(purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect charge amount for second line item",
            amountChargedSet.contains(purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect total amount for second line item",
            totalAmountSet.contains(purchaseOrder.getLineItems().getLineItems().get(1).getSubscriptionRenewal()
                .getSubscriptionRenewalResponse().getChargeDetails().getTotalPrice()),
            assertionErrorList);

        helperToValidateAssertions(purchaseOrder, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Declined reason in get purchase Order Api.
     */
    @Test(dataProvider = "dataForDeclineReason")
    public void testGetPurchaseOrderApiWithDeclineReason(final DeclineReason declineReason) {
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        // process purchase order with pending and charge commands
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.DECLINE, purchaseOrder.getId(),
            declineReason);
        LOGGER.info(
            "Purchase Order " + purchaseOrder.getId() + " is submitted with " + "Decline Reason: " + declineReason);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String declineReasonInGetPurchaseOrder = purchaseOrder.getDeclineReason();
        final String subscriptionIdInInGetPurchaseOrderApi =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String orderStateInGetPurchaseOrder = purchaseOrder.getOrderState();

        AssertCollector.assertThat("Incorrect Decline Reason", declineReasonInGetPurchaseOrder,
            equalTo(declineReason.toString()), assertionErrorList);
        AssertCollector.assertThat("Subscription is not created for declined Order",
            subscriptionIdInInGetPurchaseOrderApi, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Order state is not Declined ", orderStateInGetPurchaseOrder,
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that txnDate date is not null for all the transactions for mark as refund order.
     */
    @Test
    public void testTxnDateForMarkAsRefund() {
        final String todayDate = DateTimeUtils.getNowMinusDays(0).substring(0, 10);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // getting purchase order
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final List<Transaction> transactionList = purchaseOrder.getTransactions().getTransactions();

        for (final Transaction aTransactionList : transactionList) {
            final String txnDate = aTransactionList.getGatewayResponse().getTxnDate();
            AssertCollector.assertThat("Transaction date is not correct.", txnDate.substring(0, 10), equalTo(todayDate),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Get purchase order by id for New acquisition returns subscription start and end date in response at line
     * item level.
     *
     * @throws ParseException
     */
    @Test
    public void testSubscriptionStartAndEndDateForNewAcquisition() throws ParseException {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final OfferingResponse offeringResponse =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse();

        final String subscriptionStartDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);

        final String subscriptionEndDate =
            DateTimeUtils.addDaysToDate(DateTimeUtils.getNextBillingDate(subscriptionStartDate, "MONTH"),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, -1);

        AssertCollector.assertThat("Incorrect Subscription Start Time",
            offeringResponse.getSubscriptions().getSubscription().get(0).getSubscriptionPeriodStartDate().split(" ")[0],
            equalTo(subscriptionStartDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription End Time",
            offeringResponse.getSubscriptions().getSubscription().get(0).getSubscriptionPeriodEndDate().split(" ")[0],
            equalTo(subscriptionEndDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Get purchase order by id for Add Seats Order returns subscription start and end date in response at line
     * item level.
     *
     */
    @Test
    public void testSubscriptionStartAndEndDateForAddSeatsOrder() {
        // submit a purchase order to create a commercial subscription
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // get subscription and add it to list
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);
        final String addSeatPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatPurchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(addSeatPurchaseOrderId);
        final SubscriptionQuantityResponse OfferingResponse = purchaseOrder.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse();

        final String subscriptionStartDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String subscriptionEndDate =
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 14);

        AssertCollector.assertThat("Incorrect Subscription Start Time",
            OfferingResponse.getSubscriptions().getSubscription().get(0).getSubscriptionPeriodStartDate().split(" ")[0],
            equalTo(subscriptionStartDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription End Time",
            OfferingResponse.getSubscriptions().getSubscription().get(0).getSubscriptionPeriodEndDate().split(" ")[0],
            equalTo(subscriptionEndDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testDeclineReasonInGetPurchaseOrderApi
     *
     * @return Object[][]
     */
    @SuppressWarnings("unused")
    @DataProvider(name = "dataForDeclineReason")
    private Object[][] getTestDataForDeclinedOrder() {
        return new Object[][] { { DeclineReason.OTHER_REASON }, { DeclineReason.PAYMENT_PROCESSOR_DECLINED },
                { DeclineReason.EXPORT_CONTROL_BLOCKED }, { DeclineReason.EXPORT_CONTROL_UNRESOLVED }, };
    }

    private void helperToValidateAssertions(final PurchaseOrder purchaseOrder,
        final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Unable to find purchase orders", purchaseOrder, is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect application family ", purchaseOrder.getAppFamilyId(),
            equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser Email id ", purchaseOrder.getBuyerUser().getEmail(),
            equalTo(getBuyerUser().getEmail()), assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser External key", purchaseOrder.getBuyerUser().getExternalKey(),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect BuyerUser Id ", purchaseOrder.getBuyerUser().getId(),
            equalTo(buyerUser.getId()), assertionErrorList);
    }

    /**
     * Submit and process purchase order using ACH direct debit payment method. Validate that last 4 digits are derived
     * from Billing Information.
     */
    @Test
    public void testSuccessSubmitPOForACHDirectDebitPayment() {

        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Wills", "ABC",
            "Main St", "", "94007", "CALIFORNIA", "San Francisco", Country.US, "(123)840-0007", "",
            PaymentType.DIRECT_DEBIT.getValue(), null, null, "6789", Payment.PaymentMethod.ACH.getValue());

        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitPurchaseOrderUsingDirectDebitPaymentProfile(
            Payment.PaymentMethod.ACH, Payment.PaymentProcessor.BLUESNAP_NAMER.getValue(), getBicMonthlyUsPriceId(),
            buyerUser, 1, billingInformation);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect Order state", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment status", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Incorrect price Id used",
            purchaseOrder.getLineItems().getLineItems().get(0).getLineItemTotals().getPriceIdUsed(),
            equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Invalid Payment Processor ", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(Payment.PaymentProcessor.BLUESNAP_NAMER.getValue()), assertionErrorList);

        final PaymentProfile paymentProfile = resource.paymentProfile()
            .getPaymentProfile(purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId());

        final String paymentGateway = PaymentProcessor.BLUESNAP_NAMER.getValue() + " ("
            + getEnvironmentVariables().getBluesnapNamerPaymentGatewayId() + ")";

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect last 4 digit of direct payment",
            purchaseOrderDetailPage.getLastFourDigits(), equalTo("6789"), assertionErrorList);

        HelperForPurchaseOrder.assertionsForGatewayResponse(purchaseOrder, 1, Payment.PaymentMethod.ACH, paymentProfile,
            purchaseOrderDetailPage, 3, paymentGateway, assertionErrorList);
        HelperForPurchaseOrder.assertionsForGatewayResponse(purchaseOrder, 2, Payment.PaymentMethod.ACH, paymentProfile,
            purchaseOrderDetailPage, 4, paymentGateway, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Submit and process purchase order using SEPA direct debit payment method. Validate that last 4 digits are derived
     * from Billing Information.
     */
    @Test
    public void testSuccessSubmitPOForSEPADirectDebitPayment() {

        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Wills", "ABC",
            "Main St", "", "94007", "", "Paris", Country.FR, "(123)840-0007", "", PaymentType.DIRECT_DEBIT.getValue(),
            null, null, "5915", Payment.PaymentMethod.SEPA.getValue());

        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitPurchaseOrderUsingDirectDebitPaymentProfile(Payment.PaymentMethod.SEPA,
                PaymentProcessor.BLUESNAP_EMEA.getValue(), getBicMonthlyUkPriceId(), buyerUser, 1, billingInformation);
        final String purchaseOrderId = purchaseOrder.getId();
        AssertCollector.assertThat("Purchase order Id should not be null", purchaseOrderId, notNullValue(),
            assertionErrorList);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect Order state", purchaseOrder.getOrderState(),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Fulfillment status", purchaseOrder.getFulFillmentStatus(),
            equalTo(FulFillmentStatus.FULFILLED), assertionErrorList);
        AssertCollector.assertThat("Invalid Payment Processor ", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat("Mandate Id should be present", purchaseOrder.getPayment().getMandateId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Mandate Date should be present",
            purchaseOrder.getPayment().getMandateDate().split(" ")[0],
            equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
        AssertCollector.assertThat("Invoice number is not generated ", purchaseOrder.getInvoiceNumber(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect price Id used",
            purchaseOrder.getLineItems().getLineItems().get(0).getLineItemTotals().getPriceIdUsed(),
            equalTo(getBicMonthlyUkPriceId()), assertionErrorList);

        final PaymentProfile paymentProfile = resource.paymentProfile()
            .getPaymentProfile(purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId());

        final String paymentGateway = PaymentProcessor.BLUESNAP_EMEA.getValue() + " ("
            + getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId() + ")";

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat("Incorrect last 4 digit of direct payment",
            purchaseOrderDetailPage.getLastFourDigits(), equalTo("5915"), assertionErrorList);

        HelperForPurchaseOrder.assertionsForGatewayResponse(purchaseOrder, 1, Payment.PaymentMethod.SEPA,
            paymentProfile, purchaseOrderDetailPage, 3, paymentGateway, assertionErrorList);
        HelperForPurchaseOrder.assertionsForGatewayResponse(purchaseOrder, 2, Payment.PaymentMethod.SEPA,
            paymentProfile, purchaseOrderDetailPage, 4, paymentGateway, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
