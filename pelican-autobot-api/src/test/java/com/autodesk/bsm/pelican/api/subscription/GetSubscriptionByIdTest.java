package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Test Case : Get Subscription By Id API
 *
 * @author Shweta Hegde
 */
public class GetSubscriptionByIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private Object apiResponse;
    private PurchaseOrder purchaseOrderForBicCreditCard;
    private Subscription subscription;
    private static String subscriptionIdForBicCreditCard;
    private HttpError httpError;
    private String priceIdForBic;
    private String priceIdForMeta;
    private Offerings bicOfferings;
    private Offerings metaOfferings;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int quantity = 1;
    private static final int creditDays = 0;
    private UsageType bicOfferingsUsageType;
    private SupportLevel bicOfferingsSupportLevel;
    private UsageType metaOfferingsUsageType;
    private SupportLevel metaOfferingsSupportLevel;
    private boolean isSubscriptionCanceled;
    private static final String INCLUDE_ENTITLEMENTS = "offering.entitlements";
    private static final String UPPERCASE_INCLUDE_ENTITLEMENTS = "OFFERING.ENTITLEMENTS";
    private static final String ITEM = "ITEM";
    private String bicCommercialEntitlement1;
    private String bicCommercialEntitlement2;
    private String purchaseOrderForBicCreditCardId;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private FindSubscriptionPlanPage findSubscriptionPlanPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOfferingsUsageType = bicOfferings.getOfferings().get(0).getUsageType();
        bicOfferingsSupportLevel = bicOfferings.getOfferings().get(0).getSupportLevel();

        // Add two entitlements to the plan
        bicCommercialEntitlement1 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);
        bicCommercialEntitlement2 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);

        metaOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        metaOfferingsUsageType = metaOfferings.getOfferings().get(0).getUsageType();
        metaOfferingsSupportLevel = metaOfferings.getOfferings().get(0).getSupportLevel();

        priceIdForMeta = metaOfferings.getIncluded().getPrices().get(0).getId();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        apiResponse = null;
    }

    /**
     * This method tests subscription of BIC pending PO with credit card
     *
     */
    @Test(dataProvider = "getPackagingTypes")
    public void getSubscriptionByIdXmlWithPackagingType(final PackagingType packagingType,
        final PackagingType packagingTypeDisplayName) {

        // creating BiC Offering with different Packaging Types
        final Offerings bicOfferingsWithIc = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicOfferingIdWithIc = bicOfferingsWithIc.getOfferings().get(0).getId();
        final String priceIdForBicWithIc = bicOfferingsWithIc.getIncluded().getPrices().get(0).getId();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferingIdWithIc);
        final EditSubscriptionPlanPage editSubscriptionPlanDetailPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanDetailPage.editPackagingType(packagingType);
        editSubscriptionPlanDetailPage.clickOnSave(false);

        // Get purchase order for BIC with credit card
        purchaseOrderForBicCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBicWithIc, getBuyerUser(), quantity);
        purchaseOrderForBicCreditCardId = purchaseOrderForBicCreditCard.getId();
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderForBicCreditCardId,
            ECStatus.ACCEPT);
        // get purchase order api response
        purchaseOrderForBicCreditCard = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard.getId());
        // get subscription id from purchase order
        subscriptionIdForBicCreditCard = purchaseOrderForBicCreditCard.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard); // get
        // subscription by id if apiresponse is instance of http error
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Subscription.class), assertionErrorList);
        } else {
            // else type cast to subscription
            subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Incorrect Packaging Type ",
                subscription.getSubscriptionPlan().getPackagingType().getDisplayName(),
                equalTo(packagingTypeDisplayName.getDisplayName()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests subscription of BIC charged PO with credit card
     *
     * @throws ParseException
     */
    @Test
    public void getSubscriptionByIdForChargedBicCreditCardOrder() throws ParseException {

        // Get purchase order for BIC with credit card
        purchaseOrderForBicCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBic, getBuyerUser(), quantity);
        purchaseOrderForBicCreditCardId = purchaseOrderForBicCreditCard.getId();
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderForBicCreditCardId,
            ECStatus.ACCEPT);
        // process the PO to 'charged' state
        purchaseOrderForBicCreditCard =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard.getId());
        // get subscription id from purchase order
        subscriptionIdForBicCreditCard = purchaseOrderForBicCreditCard.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // get subscription by id
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Subscription.class), assertionErrorList);
        } else {
            subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                equalTo(subscriptionIdForBicCreditCard), assertionErrorList);
            AssertCollector.assertThat("Incorrect status ", subscription.getStatus(), equalTo(Status.ACTIVE.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect stored Payment profile ", subscription.getStorePaymentProfileId(),
                equalTo(
                    purchaseOrderForBicCreditCard.getPayment().getStoredProfilePayment().getStoredPaymentProfileId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect credit days", subscription.getCreditDays(), is(creditDays),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect next billing price amount", subscription.getNextBillingPriceAmount(),
                equalTo(bicOfferings.getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering type ", subscription.getOfferingType(),
                equalTo(OfferingType.BIC_SUBSCRIPTION.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect current offer id ", subscription.getCurrentOffer().getId(),
                equalTo(bicOfferings.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription plan name ",
                subscription.getSubscriptionPlan().getName(), equalTo(bicOfferings.getOfferings().get(0).getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect price id ", subscription.getPrice().getId(), equalTo(priceIdForBic),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect currency", subscription.getPrice().getCurrency(),
                equalTo(bicOfferings.getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
            AssertCollector.assertThat("Incorrect amount", subscription.getPrice().getAmount(),
                equalTo(bicOfferings.getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", subscription.getPrice().getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect next billing date",
                DateTimeUtils.getDateStamp(subscription.getNextBillingDate()),
                equalTo(
                    DateTimeUtils.getDateStamp(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_TIME_WITH_ZONE,
                        Integer
                            .parseInt(bicOfferings.getIncluded().getBillingPlans().get(0).getBillingPeriodCount())))),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Store External key", subscription.getPrice().getStoreExternalKey(),
                equalTo(getStoreExternalKeyUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Pricelist External Key",
                subscription.getPrice().getPricelistExternalKey(), equalTo(getPricelistExternalKeyUs()),
                assertionErrorList);
            helperToValidateAssertions(subscription, bicOfferingsUsageType, bicOfferingsSupportLevel, 6,
                BillingFrequency.MONTH.toString());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the expired subscription id with BiC Subscription and Credit Card payment
     */
    @Test(dependsOnMethods = { "getSubscriptionByIdForChargedBicCreditCardOrder" })
    public void testSuccessWithBicCreditCardExpiredSubscriptionId() {

        // using the existing subscription to expire
        isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionIdForBicCreditCard,
            CancellationPolicy.IMMEDIATE_NO_REFUND);
        if (isSubscriptionCanceled) {
            Util.waitInSeconds(TimeConstants.THREE_SEC);
            apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard);
            if (apiResponse instanceof HttpError) {
                httpError = (HttpError) apiResponse;
                AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                    instanceOf(Subscription.class), assertionErrorList);
            } else {
                subscription = (Subscription) apiResponse;
                AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                    equalTo(subscriptionIdForBicCreditCard), assertionErrorList);
                AssertCollector.assertThat("Incorrect status ", subscription.getStatus(),
                    equalTo(Status.EXPIRED.toString()), assertionErrorList);

                helperToValidateAssertions(subscription, bicOfferingsUsageType, bicOfferingsSupportLevel, 6,
                    BillingFrequency.MONTH.toString());
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the canceled subscription id with BiC Subscription and Paypal payment
     */
    @Test
    public void testSuccessWithBicPaypalCanceledSubscriptionId() {

        // submit PO for BIC with Paypal
        PurchaseOrder purchaseOrderForBicPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForBic, getBuyerUser(), quantity);
        final String purchaseOrderForBicPaypalId = purchaseOrderForBicPaypal.getId();
        // process the PO to 'pending' state
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), purchaseOrderForBicPaypalId);
        // process the PO to 'charged' state
        purchaseOrderForBicPaypal =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicPaypal.getId());
        // get subscription id from purchase order
        final String subscriptionIdForBicPaypal = purchaseOrderForBicPaypal.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // using the existing subscription to cancel
        isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionIdForBicPaypal,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (isSubscriptionCanceled) {
            Util.waitInSeconds(TimeConstants.THREE_SEC);
            apiResponse = resource.subscription().getById(subscriptionIdForBicPaypal);
            if (apiResponse instanceof HttpError) {
                httpError = (HttpError) apiResponse;
                AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                    instanceOf(Subscription.class), assertionErrorList);
            } else {
                subscription = (Subscription) apiResponse;
                AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                    equalTo(subscriptionIdForBicPaypal), assertionErrorList);
                AssertCollector.assertThat("Incorrect status ", subscription.getStatus(),
                    equalTo(Status.CANCELLED.toString()), assertionErrorList);

                helperToValidateAssertions(subscription, bicOfferingsUsageType, bicOfferingsSupportLevel, 6,
                    BillingFrequency.MONTH.toString());
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests subscription of Meta Charged PO with paypal
     *
     * @throws ParseException
     */
    @Test
    public void getSubscriptionByIdForChargedMetaPaypalOrder() throws ParseException {
        // submit PO for meta with Paypal
        PurchaseOrder purchaseOrderForMetaPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, getBuyerUser(), quantity);
        final String purchaseOrderForMetaPaypalId = purchaseOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), purchaseOrderForMetaPaypalId);
        purchaseOrderForMetaPaypal =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForMetaPaypal.getId());
        // get purchase order api response
        purchaseOrderUtils.fulfillRequest(purchaseOrderForMetaPaypal, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrderForMetaPaypal = resource.purchaseOrder().getById(purchaseOrderForMetaPaypal.getId());
        final String subscriptionIdForMetaPaypal = purchaseOrderForMetaPaypal.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // get subscription by id
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        apiResponse = resource.subscription().getById(subscriptionIdForMetaPaypal);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Subscription.class), assertionErrorList);
        } else {
            subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                equalTo(subscriptionIdForMetaPaypal), assertionErrorList);
            AssertCollector.assertThat("Incorrect status ", subscription.getStatus(), equalTo(Status.ACTIVE.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect credit days", subscription.getCreditDays(), is(creditDays),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect next billing price amount", subscription.getNextBillingPriceAmount(),
                equalTo(metaOfferings.getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering type ", subscription.getOfferingType(),
                equalTo(OfferingType.META_SUBSCRIPTION.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect current offer id ", subscription.getCurrentOffer().getId(),
                equalTo(metaOfferings.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription plan name ",
                subscription.getSubscriptionPlan().getName(), equalTo(metaOfferings.getOfferings().get(0).getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect price id ", subscription.getPrice().getId(), equalTo(priceIdForMeta),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect currency", subscription.getPrice().getCurrency(),
                equalTo(metaOfferings.getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
            AssertCollector.assertThat("Incorrect amount", subscription.getPrice().getAmount(),
                equalTo(metaOfferings.getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", subscription.getPrice().getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect next billing date",
                DateTimeUtils.getDateStamp(subscription.getNextBillingDate()),
                equalTo(
                    DateTimeUtils.getDateStamp(DateTimeUtils.getNowAsUTCPlusYears(PelicanConstants.DATE_TIME_WITH_ZONE,
                        Integer
                            .parseInt(metaOfferings.getIncluded().getBillingPlans().get(0).getBillingPeriodCount())))),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Store External key", subscription.getPrice().getStoreExternalKey(),
                equalTo(getStoreExternalKeyUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Pricelist External Key",
                subscription.getPrice().getPricelistExternalKey(), equalTo(getPricelistExternalKeyUs()),
                assertionErrorList);
            helperToValidateAssertions(subscription, metaOfferingsUsageType, metaOfferingsSupportLevel, 1,
                BillingFrequency.YEAR.toString());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the negative scenario with invalid subscription id
     */
    @Test
    public void testErrorWithInvalidSubscriptionId() {

        final String invalidSubscriptionId = "1234567";
        apiResponse = resource.subscription().getById(invalidSubscriptionId);

        final HttpError httpError = (HttpError) apiResponse;

        AssertCollector.assertThat("Incorrect error detail", httpError.getErrorMessage(),
            equalTo("Subscription not found for Id : " + invalidSubscriptionId), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", httpError.getStatus(), is(404), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test methods tests negative scenario when Alphabetic subscription id is sent in Find Subscription By Id
     */
    @Test
    public void testErrorWithNonNumericSubscriptionId() {

        final String nonNumericSubscriptionId = "Test$*&&*";
        apiResponse = resource.subscription().getById(nonNumericSubscriptionId);

        final HttpError httpError = (HttpError) apiResponse;
        AssertCollector.assertThat("Incorrect error detail", httpError.getErrorMessage(),
            equalTo("For input string: \"" + nonNumericSubscriptionId + "\""), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", httpError.getStatus(), is(400), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests positive scenario when valid subscription id and include value is sent in Find
     * Subscription By Id
     */
    @Test
    public void testSuccessGetSubscriptionIdWithValidInclude() {
        // Get purchase order for BIC with credit card
        purchaseOrderForBicCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBic, getBuyerUser(), quantity);
        purchaseOrderForBicCreditCardId = purchaseOrderForBicCreditCard.getId();
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderForBicCreditCardId,
            ECStatus.ACCEPT);
        purchaseOrderForBicCreditCard =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard.getId());
        // get subscription id from purchase order
        subscriptionIdForBicCreditCard = purchaseOrderForBicCreditCard.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        subscription = resource.subscription().getById(subscriptionIdForBicCreditCard, INCLUDE_ENTITLEMENTS);
        final List<Entitlement> oneTimeEntitlements =
            subscription.getSubscriptionPlan().getOneTimeEntitlements().getEntitlements();
        AssertCollector.assertThat("Incorrect number of one time entitlements", oneTimeEntitlements.size(), equalTo(2),
            assertionErrorList);

        int numMatchingEntitlementsFound = 0;
        for (final Entitlement oneTimeEntitlement : oneTimeEntitlements) {
            if (oneTimeEntitlement.getId().equals(bicCommercialEntitlement1)) {
                numMatchingEntitlementsFound++;
            } else if (oneTimeEntitlement.getId().equals(bicCommercialEntitlement2)) {
                numMatchingEntitlementsFound++;
            }
            AssertCollector.assertThat("Invalid one time entitlement name", oneTimeEntitlement.getName(),
                notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid one time entitlement external key", oneTimeEntitlement.getExternalKey(),
                notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid one time entitlement type", oneTimeEntitlement.getType(), equalTo(ITEM),
                assertionErrorList);
            AssertCollector.assertThat("Invalid one time entitlement item type external key",
                oneTimeEntitlement.getItemTypeExternalKey(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid one time entitlement licensing model external key",
                oneTimeEntitlement.getLicensingModelExternalKey(), notNullValue(), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect number of matching one time entitlements", numMatchingEntitlementsFound,
            equalTo(2), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests negative scenario with valid subscription id and invalid include value
     */
    @Test(dependsOnMethods = { "testSuccessGetSubscriptionIdWithValidInclude" })
    public void testErrorWithInvalidIncludeValue() {

        apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard, RandomStringUtils.random(9));
        final HttpError httpError = (HttpError) apiResponse;

        AssertCollector.assertThat("Incorrect status", httpError.getStatus(), is(400), assertionErrorList);
        AssertCollector.assertThat("Incorrect code", httpError.getErrorCode(), equalTo(990002), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", httpError.getErrorMessage(),
            containsString("Invalid 'include' query parameter"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests negative scenario with invalid include value (Test case-sensitive)
     */
    @Test(dependsOnMethods = { "testSuccessGetSubscriptionIdWithValidInclude" })
    public void testErrorWithUpperCaseIncludeValue() {

        apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard, UPPERCASE_INCLUDE_ENTITLEMENTS);
        final HttpError httpError = (HttpError) apiResponse;

        AssertCollector.assertThat("Incorrect status", httpError.getStatus(), is(400), assertionErrorList);
        AssertCollector.assertThat("Incorrect code", httpError.getErrorCode(), equalTo(990002), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", httpError.getErrorMessage(),
            containsString("Invalid 'include' query parameter"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies that quantity to reduce value is 0 in api response when DB has null value for this field.
     */
    @Test
    public void testGetSubscriptionByIdXmlQuantityToReduceWithNullValue() {
        final String sqlQuery = PelicanDbConstants.SELECT_SQL_FOR_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL
            + getEnvironmentVariables().getAppFamilyId();
        final List<Map<String, String>> resultList = DbUtils.selectQuery(sqlQuery, getEnvironmentVariables());
        String subscriptionId = null;
        if (resultList.size() > 0) {
            subscriptionId = resultList.get(0).get("ID");
        } else {
            final String updateQuery =
                PelicanDbConstants.UPDATE_SQL_TO_SET_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL + subscriptionId;
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        }
        subscription = resource.subscription().getById(subscriptionId);

        AssertCollector.assertThat("Incorrect subscription id.", subscription.getId(), equalTo(subscriptionId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity to reduce value.", subscription.getQtyToReduce(), equalTo(0),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method helps to assert different values for many test methods
     *
     * @param subscription
     * @param usageType
     * @param supportLevel
     * @param billingCount
     * @param billingType
     */
    private void helperToValidateAssertions(final Subscription subscription, final UsageType usageType,
        final SupportLevel supportLevel, final int billingCount, final String billingType) {

        AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Application Family Id ", subscription.getApplicationFamilyId(),
            equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect owner id ", subscription.getOwnerId(), equalTo(getBuyerUser().getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect owner external key", subscription.getOwnerExternalKey(),
            equalTo(getBuyerUser().getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Plan usage type",
            subscription.getSubscriptionPlan().getUsageType(), equalTo(usageType), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Plan support level",
            subscription.getSubscriptionPlan().getSupportLevel(), equalTo(supportLevel.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Billing count",
            subscription.getBillingOption().getBillingPeriod().getCount(), is(billingCount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Billing type",
            subscription.getBillingOption().getBillingPeriod().getType(), equalTo(billingType), assertionErrorList);
        AssertCollector.assertThat("Incorrect export control last modified", subscription.getLastModified(),
            notNullValue(), assertionErrorList);
    }

    /**
     * DataProvider to pass Price Id and boolean flag to identify for packagingType if its IC or None
     *
     */
    @DataProvider(name = "getPackagingTypes")
    public Object[][] getPackagingTypes() {
        return new Object[][] { { PackagingType.INDUSTRY_COLLECTION, PackagingType.IC },
                { PackagingType.VERTICAL_GROUPING, PackagingType.VG } };
    }
}
