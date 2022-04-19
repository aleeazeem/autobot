package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.httpclient.HttpStatus;
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
 * This class is specifically for Submit and Process of Subscription Extension Purchase Orders Created by Shweta Hegde
 * on 4/19/17.
 * <p>
 * This class is inherited from Selenium Webdriver, because to change Align Billing Feature Flag in Admin tool
 */
public class SubmitAndProcessSubscriptionExtensionTest extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private HttpError httpError;
    private static final String INVALID_ID = "5638757836";
    private String priceIdForSubscriptionOffering1;
    private String priceIdForSubscriptionOffering2;
    private Map<String, String> priceOfferingAmountMap;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private BuyerUser buyerUser;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        priceIdForSubscriptionOffering1 = subscriptionOffering1.getIncluded().getPrices().get(0).getId();
        priceIdForSubscriptionOffering2 = subscriptionOffering2.getIncluded().getPrices().get(0).getId();

        // Initialize maps and Added to PriceId/Amount Map
        priceOfferingAmountMap = new HashMap<>();
        priceOfferingAmountMap.put(getBicYearlyUsPriceId(),
            getBicSubscriptionPlan().getIncluded().getPrices().get(2).getAmount());
        priceOfferingAmountMap.put(getMetaMonthlyUsPriceId(),
            getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering2,
            subscriptionOffering2.getIncluded().getPrices().get(0).getAmount());

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This method tests 2 cases: 1. Test Subscription Extension Success with single line item 2. Test quantity is
     * ignored when quantity is sent in subscription extension request
     */
    @Test
    public void testSuccessWithSingleSubscriptionExtension() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String targetRenewalDate =
            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.QUANTITY.getValue(), new ArrayList<>(ImmutableList.of("10")));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(ImmutableList.of(targetRenewalDate)));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Purchase Order related assertions
        AssertCollector.assertThat("Purchase Order Id should be generated", purchaseOrder.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(PurchaseOrder.OrderState.AUTHORIZED.toString()), assertionErrorList);
        // Subscription Extension related assertions
        AssertCollector
            .assertThat("Incorrect subscription id",
                purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionExtension()
                    .getSubscriptionExtensionRequest().getSubscriptionId(),
                equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect subscription quantity", purchaseOrder.getLineItems().getLineItems().get(0)
                .getSubscriptionExtension().getSubscriptionExtensionRequest().getQuantity(),
            equalTo("4"), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription renewal date",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionExtension()
                .getSubscriptionExtensionRequest().getSubscriptionRenewalDate(),
            equalTo(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6)),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription target date",
            purchaseOrder.getLineItems().getLineItems().get(0).getSubscriptionExtension()
                .getSubscriptionExtensionRequest().getTargetRenewalDate(),
            equalTo(targetRenewalDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when target renewal date is NOT given
     */
    @Test
    public void testSubscriptionExtensionErrorWhenTargetRenewalDateIsNotGiven() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.TARGET_RENEWAL_DATE_REQUIRED_ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when price id not matching with subscription id
     */
    @Test
    public void testSubscriptionExtensionErrorWhenPriceIdIsNotMatchingTheSubscription() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(getBicYearlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7))));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.PRICE_NOT_MATCHING_SUBSCRIPTION_ERROR, getBicYearlyUsPriceId(),
                subscriptionId)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error Target Renewal Date is less than Next Billing Date
     */
    @Test
    public void testSubscriptionExtensionErrorWhenTargetRenewalDateIsLesserNextBillingDate() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.QUANTITY.getValue(), new ArrayList<>(ImmutableList.of("10")));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 178))));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.TARGET_RENEWAL_DATE_SHOULD_BE_GREATER_THAN_NBD_ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when subscription is not ACTIVE, it is either CANCELLED or EXPIRED
     *
     * @param cancellationPolicy
     */
    @Test(dataProvider = "dataForSubscriptionNotActive")
    public void testSubscriptionExtensionErrorWhenSubscriptionIdIsNotActive(
        final CancellationPolicy cancellationPolicy) {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        resource.subscription().cancelSubscription(subscriptionId, cancellationPolicy);

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7))));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanConstants.SUBSCRIPTION_WITH_ID + subscriptionId + PelicanErrorConstants.NOT_ACTIVE_ERROR),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider to pass cancellation policies
     *
     * @return cancellationPolicy
     */
    @DataProvider(name = "dataForSubscriptionNotActive")
    private Object[][] getSubscriptionCancellationPolicy() {
        return new Object[][] { { CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD },
                { CancellationPolicy.IMMEDIATE_NO_REFUND } };
    }

    /**
     * Test Error when price id not given in subscription extension request This also tests, price id takes precedence
     * over other required fields
     *
     * @param paymentType
     * @param param
     */
    @Test(dataProvider = "dataForPriceRequiredFieldError")
    public void testSubscriptionExtensionErrorWhenPriceIdIsNotGiven(final Payment.PaymentType paymentType,
        final LineItemParams param) {

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(param.getValue(), new ArrayList<>(ImmutableList.of("")));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            paymentType, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.PRICE_ID_REQUIRED_ERROR), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for price id error
     *
     * @return
     */
    @DataProvider(name = "dataForPriceRequiredFieldError")
    private Object[][] getPriceRequiredFields() {
        return new Object[][] { { Payment.PaymentType.CREDIT_CARD, LineItemParams.PRICE_ID },
                { Payment.PaymentType.PAYPAL, LineItemParams.SUBSCRIPTION_ID },
                { Payment.PaymentType.CREDIT_CARD, LineItemParams.TARGET_RENEWAL_DATE } };
    }

    /**
     * Test Error When price id not found
     */
    @Test
    public void testSubscriptionExtensionErrorWhenPriceIdNotFound() {

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(ImmutableList.of(INVALID_ID)));
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), new ArrayList<>(ImmutableList.of(INVALID_ID)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 178))));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_ID_NOT_FOUND_ERROR + INVALID_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests 2 cases 1. Invalid subscription id 2. Subscription id not given
     *
     * @param subscriptionId
     * @param errorMessage
     */
    @Test(dataProvider = "dataForSubscriptionRequiredFieldError")
    public void testSubscriptionExtensionErrorWhenSubscriptionIdIsNotGiven(final String subscriptionId,
        final String errorMessage) {

        final Map<String, List<String>> subscriptionMap = new HashMap<>();

        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(), equalTo(errorMessage),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test Error when submit PO for Subscription Extension with Next Renewal Date in Past throws an Error.
     */
    @Test
    public void testErrorForSubscriptionExtensionWithSubscriptionsNextBillingDateInPast() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // get one date to change Subscriptions next billing date
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(-15),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, getEnvironmentVariables().getAppFamily(),
            getEnvironmentVariables(), changedNextBillingDate);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15))));

        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_NEXT_BILLING_DATE_IN_PAST), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "dataForSubscriptionRequiredFieldError")
    private Object[][] getSubscriptionRequiredFields() {
        return new Object[][] { { "", PelicanErrorConstants.SUBSCRIPTION_ID_REQUIRED_ERROR },
                { INVALID_ID, PelicanErrorConstants.SUBSCRIPTION_ID_NOT_FOUND_ERROR + INVALID_ID } };
    }

    /**
     * Test Subscription Extension Success with multi line item. This method covers 2 testcases 1. With same target
     * renewal date for all line items 2. With different target renewal date for all line items
     */
    @Test(dataProvider = "dataForTargetRenewalDate")
    public void testSuccessWithMultiLineSubscriptionExtension(final List<String> targetRenewalDates,
        final List<String> renewalDates) {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicYearlyUsPriceId(), 4)), null, true, true, buyerUser);

        final String subscriptionId3 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Adding all subscription ids to a set to validate subscription ids in Submit PO response
        final Set<String> subscriptionIdsSet = new HashSet<>();
        subscriptionIdsSet.add(subscriptionId1);
        subscriptionIdsSet.add(subscriptionId2);
        subscriptionIdsSet.add(subscriptionId3);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal
        // date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2, subscriptionId3)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(
            ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId(), getBicYearlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), targetRenewalDates);

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Purchase Order related assertions
        AssertCollector.assertThat("Purchase Order Id should be generated", purchaseOrder.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Purchase Order status", purchaseOrder.getOrderState(),
            equalTo(PurchaseOrder.OrderState.AUTHORIZED.toString()), assertionErrorList);
        // Subscription Extension related assertions
        for (int i = 0; i < 3; i++) {
            AssertCollector.assertTrue("Incorrect subscription id",
                subscriptionIdsSet.contains(purchaseOrder.getLineItems().getLineItems().get(i)
                    .getSubscriptionExtension().getSubscriptionExtensionRequest().getSubscriptionId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription renewal date",
                purchaseOrder.getLineItems().getLineItems().get(i).getSubscriptionExtension()
                    .getSubscriptionExtensionRequest().getSubscriptionRenewalDate(),
                equalTo(renewalDates.get(i)), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription target date",
                purchaseOrder.getLineItems().getLineItems().get(i).getSubscriptionExtension()
                    .getSubscriptionExtensionRequest().getTargetRenewalDate(),
                equalTo(targetRenewalDates.get(i)), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "dataForTargetRenewalDate")
    private Object[][] getDataForTargetRenewalDate() {
        return new Object[][] {

                // target renewal dates and subscription renewal dates for all subscriptions
                // P1 scenarios
                { new ArrayList<>(
                    ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                        DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                        DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12))) },
                { new ArrayList<>(
                    ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                        DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 14),
                        DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15))),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12))) } };
    }

    /**
     * Method to verify Process Subscription Extension Order Calculates Amount Charge Correct. Data Provider supports
     * Credit card and Paypal payment type , monthly and yearly subscription billing cylce, tax amount and diffent and
     * same Target Renewal Date. Step 1: submit New Acquisition Order for BIC and Meta subscription. Step 2: for created
     * subscription submit Subscription extension order. Step 3: Validate On AUTH, pending payment flag set to TRUE. On
     * PENDING total amount charge is correct And Fulfillment Status is FULFILLED & Fulfillment Strategy is
     * SUBSCRIPTION_EXTENSION.
     *
     * @throws ParseException
     * @throws NumberFormatException
     */
    @Test(dataProvider = "dataForSubscriptionExtension")
    public void testSuccessProcessSubscriptionExtensionOrder(final List<String> subscriptionIds,
        final List<String> targetRenewalDates, final List<String> taxAmounts, final Payment.PaymentType paymentType)
        throws NumberFormatException, ParseException {

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIds);
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(
            ImmutableList.of(getBicYearlyUsPriceId(), getMetaMonthlyUsPriceId(), getBicYearlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), targetRenewalDates);

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap, paymentType,
                PurchaseOrder.OrderCommand.AUTHORIZE, taxAmounts, buyerUser);

        // Adding all subscription ids to a set to validate subscription ids in Submit PO response
        validatePendingPaymentFlag(subscriptionIds, assertionErrorList);

        // Processing for PENDING Order State.
        PurchaseOrder subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        HelperForPurchaseOrder.subscriptionExtensionAmountCharged(subscriptionExtensionOrder,
            new ArrayList<>(ImmutableList.of("YEAR", "MONTH", "YEAR")), priceOfferingAmountMap, assertionErrorList);

        HelperForPurchaseOrder.assertionForSubscriptionExtensionFulfillment(subscriptionExtensionOrder,
            FulFillmentStrategy.SUBSCRIPTION_EXTENSION.toString(), FulFillmentStatus.PENDING.toString(),
            assertionErrorList);

        HelperForPurchaseOrder.assertionForSubscriptionExtensionTransactionActivity(subscriptionExtensionOrder, 1,
            "SALE", OrderCommand.PENDING.toString(), assertionErrorList);

        // Processing for CHARGE Order State.
        subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        HelperForPurchaseOrder.assertionForSubscriptionExtensionFulfillment(subscriptionExtensionOrder,
            FulFillmentStrategy.SUBSCRIPTION_EXTENSION.toString(), FulFillmentStatus.FULFILLED.toString(),
            assertionErrorList);

        HelperForPurchaseOrder.assertionForSubscriptionExtensionTransactionActivity(subscriptionExtensionOrder, 2,
            "SALE", OrderState.APPROVED.toString(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    private static void validatePendingPaymentFlag(final List<String> subscriptionIdsSet,
        final List<AssertionError> assertionErrorList) {
        for (final String subscriptionId : subscriptionIdsSet) {
            final String pendingPayment = DbUtils
                .selectQuery(String.format("select PENDING_PAYMENT from subscription where ID=%s", subscriptionId),
                    "PENDING_PAYMENT", getEnvironmentVariables())
                .get(0);

            // Assert on Pending Payment field
            AssertCollector.assertTrue("Incorrect Pending payment Flag", pendingPayment.equals("1"),
                assertionErrorList);
        }
    }

    @DataProvider(name = "dataForSubscriptionExtension")
    private Object[][] getDataForSubscriptionExtension() {
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

        return new Object[][] {

                // target renewal dates, tax amount, payment type

                { new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2, subscriptionId3)),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))),
                        new ArrayList<>(ImmutableList.of("10", "11", "15")), Payment.PaymentType.CREDIT_CARD },
                { new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2, subscriptionId3)),
                        new ArrayList<>(ImmutableList.of(
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 14),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15),
                            DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 16))),
                        new ArrayList<>(ImmutableList.of("10", "11", "15")), Payment.PaymentType.PAYPAL } };
    }

    /**
     * This method verifies submit subscription extension order is successful even though price is expired
     */
    @Test
    public void testSuccessSubmitSubscriptionExtensionOrderOnExpiredPriceId() {

        final Offerings subscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = subscriptionOffering.getIncluded().getPrices().get(0).getId();

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceId, 2)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.

        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(ImmutableList.of(priceId)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(6),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceId), getEnvironmentVariables());

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect response", purchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Process Subscription Extension Order Honor Price from Auth PO.
     * <p>
     * Step 1: submit New Acquisition Order Step 2: Use subscription to submit subscription extension order and process
     * it for Pending. Step 3: Expire Price Id for Subscription Plan Offer. Step 4: Process Subscription Extension Order
     * for PENDING. It should honor priceId captured at Auth.
     *
     * @throws ParseException
     * @throws NumberFormatException
     */
    @Test
    public void testSuccessProcessSubscriptionExtensionOrderOnExpiredPriceId()
        throws NumberFormatException, ParseException {

        final Offerings subscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = subscriptionOffering.getIncluded().getPrices().get(0).getId();

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceId, 4)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.

        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(), new ArrayList<>(ImmutableList.of(priceId)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, new ArrayList<>(ImmutableList.of("10")),
                buyerUser);

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceId), getEnvironmentVariables());

        // Processing for PENDING Order State.
        final PurchaseOrder subscriptionExtensionOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        HelperForPurchaseOrder.subscriptionExtensionAmountCharged(subscriptionExtensionOrder,
            new ArrayList<>(ImmutableList.of("MONTH")),
            new HashMap<>(ImmutableMap.of(priceId, subscriptionOffering.getIncluded().getPrices().get(0).getAmount())),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error is thrown for Extension Order, if another Extension order for the same subscription is in
     * AUTH or PENDING state Step1: Submit New Acquisition Order Step2: Submit Extension Order1 in AUTH Step3: Submit
     * Extension Order2 in AUTH Step4: Verify error is thrown and PO is not created Step5: Process the extension order1
     * to Pending Step6: Submit Extension Order Step7: Verify error is thrown and PO is not created
     */
    @Test
    public void testErrorOnSubmitExtensionOrderIfAnotherExtensionOrderInProgress() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering2, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering2)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder extensionPurchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, new ArrayList<>(ImmutableList.of("10")),
                buyerUser);

        final String extensionPurchaseOrderId = extensionPurchaseOrder.getId();

        // Submit another extension order, to get error
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error message for subscription extension order",
            httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR, subscriptionId)),
            assertionErrorList);

        // Process Extension Order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, extensionPurchaseOrderId);

        // Submit another extension order, to get error
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error message for subscription extension order",
            httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR, subscriptionId)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error is thrown for Extension Order, if Renewal order for the same subscription is in AUTH or
     * PENDING state Step1: Submit New Acquisition Order Step2: Submit Renewal Order in AUTH Step3: Submit Extension
     * Order in AUTH Step4: Verify error is thrown and PO is not created Step5: Process the renewal order to Pending
     * Step6: Submit Extension Order in AUTH Step7: Verify error is thrown and PO is not created
     */
    @Test
    public void testErrorOnSubmitExtensionOrderIfRenewalOrderInProgress() {

        // Create a purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering2, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit renewal order
        final PurchaseOrder renewalPurchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.CREDIT_CARD,
                subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);

        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering2)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error message for subscription extension order",
            httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR, subscriptionId)),
            assertionErrorList);

        // Process Renewal Order
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);

        // Submit Extension Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error message for subscription extension order",
            httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR, subscriptionId)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error is thrown for Extension Order, if Add Seats order for the same subscription is in Pending
     * state Step1: Submit New Acquisition Order Step2: Submit Add Seats Order And process to PENDING Step3: Submit
     * Extension Order Step4: Verify error is thrown
     */
    @Test
    public void testErrorOnSubmitExtensionOrderIfAddSeatsOrderInPending() {

        // Create a new acquisition purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForSubscriptionOffering1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        final PurchaseOrder addSeatsPurchaseOrder = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        final String addSeatsPurchaseOrderId = addSeatsPurchaseOrder.getId();
        // Process Add Seats order to Pending
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        httpError = (HttpError) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
            PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect error message for subscription extension order",
            httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.SUBSCRIPTION_HAS_ANOTHER_ORDER_ERROR, subscriptionId)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests NO error is thrown for Extension Order, if Add Seats order for the same subscription is in AUTH
     * state Step1: Submit New Acquisition Order Step2: Submit Add Seats Order in AUTH Step3: Submit Extension Order
     * Step4: Verify no error is thrown
     */
    @Test
    public void testSuccessOnSubmitExtensionOrderIfAddSeatsOrderInAuth() {

        // Create a new acquisition purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForSubscriptionOffering1);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");

        // Submit Add Seats Order
        purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
            PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, buyerUser);

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect response", purchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error is thrown during extension order fulfillment if subscription is expired Step1: Submit New
     * Acquisition order Step2: Submit Extension order in AUTH Step3: Expire the subscription Step4: Process the
     * Extension order to Pending Step5: Verify that error is thrown that subscription is not active
     */
    @Test
    public void testErrorDuringPOFulfillmentWhenSubscriptionIsExpired() {

        // Create a new acquisition purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Expire the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        final HttpError httpError =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_NOT_FOUND_ERROR + subscriptionId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error is thrown during extension order fulfillment if subscription is expired Step1: Submit New
     * Acquisition order Step2: Submit Extension order in AUTH Step3: Expire the subscription Step4: Process the
     * Extension order to Charge Step5: Verify that error is thrown that subscription is not active
     */
    @Test
    public void testErrorDuringPOChargeWhenSubscriptionIsExpired() {

        // Create a new acquisition purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        // Expire the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        final HttpError httpError = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_NOT_FOUND_ERROR + subscriptionId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies, Extension Order gets fulfilled and Charged, if subscription is cancelled Step1: Submit New
     * Acquisition order with CHARGE Step2: Submit Subscription Extension with AUTH Step3: Cancel the subscription
     * Step4: Process the PO to Pending Step5: No error is thrown Step6: Process the PO to CHARGE Step7: No error is
     * thrown
     */
    @Test
    public void testSuccessFulfillmentOfExtensionOrderWhenSubscriptionIsCancelled() {

        // Create a new acquisition purchase order and get subscription id
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForSubscriptionOffering1, 4)), null, true, true, buyerUser);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Extension Request, with subscription id, price id and target date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForSubscriptionOffering1)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(), new ArrayList<>(
            ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // Submit Extension Order
        PurchaseOrder extensionPurchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        // Cancel the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // Process PO to Pending
        extensionPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, extensionPurchaseOrder.getId());

        AssertCollector.assertThat("Incorrect response", extensionPurchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);

        // Process PO to Charged
        extensionPurchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, extensionPurchaseOrder.getId());

        AssertCollector.assertThat("Incorrect response", extensionPurchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Multiline Subscription Extension Submit PO is successful when one of the prices is expired
     */
    @Test
    public void testSuccessWithMultiLineSubscriptionExtensionWithOneExpiredPrice() {

        final Offerings subscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = subscriptionOffering.getIncluded().getPrices().get(0).getId();

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceId, 2)), null, true, true, buyerUser);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 10)), null, true, true, buyerUser);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Adding all subscription ids to a set to validate subscription ids in Submit PO response
        final Set<String> subscriptionIdsSet = new HashSet<>();
        subscriptionIdsSet.add(subscriptionId1);
        subscriptionIdsSet.add(subscriptionId2);

        // Prepare request for Subscription Extension Request, with subscription id, price id, quantity and target
        // renewal date
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceId, getMetaMonthlyUsPriceId())));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 13))));

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceId), getEnvironmentVariables());

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                Payment.PaymentType.CREDIT_CARD, PurchaseOrder.OrderCommand.AUTHORIZE, null, buyerUser);

        AssertCollector.assertThat("Incorrect response", purchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
