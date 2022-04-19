package com.autodesk.bsm.pelican.api.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfiles;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.user.GDPRResponse;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.beust.jcommander.internal.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.ParseException;
import org.eclipse.jetty.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class to cover scenarios for GDPR User Request.
 */
public class GdprUserTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private PurchaseOrderUtils purchaseOrderUtils;
    private UserUtils userUtils;
    private Map<String, String> paramsMap;
    private FindUserPage findUserPage;
    private String taskId = "gdpr delete system ID";
    private String eventType = "gdpr.delete";

    @BeforeClass
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminTool = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminTool.login();
        resource = new PelicanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily()).platform();
        findUserPage = adminTool.getPage(FindUserPage.class);
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        userUtils = new UserUtils();
        paramsMap = new HashMap<>();
    }

    /**
     * Test Error when GDPR User not found.
     *
     */
    @Test
    public void testErrorWhenGDPRUserNotFound() throws ParseException, IOException {
        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest("testError", eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message on User not found",
            gdprResponse.getStatus().getDescription(), equalTo(PelicanErrorConstants.GDPR_ERROR_NO_USER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.SUCCESS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when Oxygen Id is not present.
     *
     */
    @Test
    public void testErrorOxygenIdNotPassed() throws ParseException, IOException {
        final GDPRResponse gdprResponse = userUtils.gdprUserRequest(null, eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message ", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanErrorConstants.GDPR_ERROR_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.ERROR), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test when type id is not present.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testErrorWhenTypeIdNotPresent() throws ParseException, IOException {
        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());
        final HttpError gdprResponse =
            userUtils.gdprUserRequest(user.getExternalKey(), eventType, null, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus(),
            equalTo(HttpStatus.BAD_REQUEST_400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test when Different event type has been passed.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testWhileOtherEventTypePassed() throws ParseException, IOException {
        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());
        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(user.getExternalKey(), "gdpr.unlock", taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect Error Message ", gdprResponse.getStatus().getDescription(),
            equalTo("No action on lock/unlock"), assertionErrorList);
        AssertCollector.assertThat("Incorrect event type received ", gdprResponse.getStatus().getType(),
            equalTo("waived"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate GDPR Delete Request will not Delete any User Linked to Trial Subscription.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testErrorGdprDeleteForTrialSubscription() throws ParseException, IOException {
        final String userName = RandomStringUtils.randomAlphanumeric(4);
        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userName);
        userMap.put(UserClient.UserParameter.NAME.getName(), userName);

        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());

        new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
            .addPaypalPaymentProfile(user.getId(), Payment.PaymentProcessor.PAYPAL_NAMER.getValue());

        final Offerings trialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.TRL);
        final String trialOfferExternalKey = trialOffering.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String trialOfferingId = trialOffering.getOfferings().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(trialOfferingId, null, null, true);
        // add trial subscription for user.
        resource.subscription().add(user.getExternalKey(), trialOfferExternalKey, Currency.USD);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(user.getExternalKey(), eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanErrorConstants.GDPR_ERROR_PRE_CONDITION_FAILED), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.ERROR), assertionErrorList);

        // navigate to AT field should not be visible.
        AssertCollector.assertThat("Incorrect GDPR Delete Value:", getGdprDeleteValue(user.getExternalKey()),
            equalTo("0"), assertionErrorList);

        paramsMap.put("userExternalKey", user.getExternalKey());
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be present", paymentProfiles.getTotal(), equalTo("1"),
            assertionErrorList);

        final UserDetailsPage userDetailPage = findUserPage.getByExternalKey(user.getExternalKey());
        AssertCollector.assertFalse("GDPR Delete should not be visible", userDetailPage.isGdprDeleteFieldPresent(),
            assertionErrorList);
        AssertCollector.assertFalse("GDPR Delete Time should not be visible", userDetailPage.isGdprDeleteDateVisible(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Error for User Linked to Active Subscription.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testErrorGdprDeleteForUserWithActiveSubscription() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanErrorConstants.GDPR_ERROR_PRE_CONDITION_FAILED), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.ERROR), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("0"), assertionErrorList);

        paramsMap.put("userExternalKey", buyerUser.getExternalKey());
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be present", paymentProfiles.getTotal(), equalTo("1"),
            assertionErrorList);
        // navigate to AT feild should not be visible.
        final UserDetailsPage userDetailPage = findUserPage.getByExternalKey(buyerUser.getExternalKey());
        AssertCollector.assertFalse("GDPR Delete should not be visible", userDetailPage.isGdprDeleteFieldPresent(),
            assertionErrorList);
        AssertCollector.assertFalse("GDPR Delete Time should not be visible", userDetailPage.isGdprDeleteDateVisible(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Error for User External key linked to Cancel Subscription.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testErrorGdprDeleteForUserWithCancelSubscription() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);
        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanErrorConstants.GDPR_ERROR_PRE_CONDITION_FAILED), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.ERROR), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("0"), assertionErrorList);

        paramsMap.put("userExternalKey", buyerUser.getExternalKey());
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be present", paymentProfiles.getTotal(), equalTo("1"),
            assertionErrorList);

        final UserDetailsPage userDetailPage = findUserPage.getByExternalKey(buyerUser.getExternalKey());
        AssertCollector.assertFalse("GDPR Deleted should not be visible", userDetailPage.isGdprDeleteFieldPresent(),
            assertionErrorList);
        AssertCollector.assertFalse("GDPR Deleted Date should not be visible", userDetailPage.isGdprDeleteDateVisible(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Success for User External key linked to Expired Subscription.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSuccessGdprDeleteForUserWithExpiredSubscription() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);
        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanConstants.GDPR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.SUCCESS), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("1"), assertionErrorList);

        paramsMap.put("userExternalKey", buyerUser.getExternalKey());
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be present", paymentProfiles.getTotal(), equalTo("1"),
            assertionErrorList);

        final UserDetailsPage userDetailPage = findUserPage.getByExternalKey(buyerUser.getExternalKey());
        final boolean isGdprDeletedVisible = userDetailPage.isGdprDeleteFieldPresent();
        AssertCollector.assertTrue("GDPR Deleted should be visible", isGdprDeletedVisible, assertionErrorList);
        final boolean isGDPRDeletedDateVisible = userDetailPage.isGdprDeleteDateVisible();
        AssertCollector.assertTrue("GDPR Deleted Date should be visible", isGDPRDeletedDateVisible, assertionErrorList);

        AssertCollector.assertThat("Incorrect GDPR Deleted value", userDetailPage.getGDPRDelete(), equalTo("Yes"),
            assertionErrorList);
        AssertCollector.assertThat("GDPR Deleted Date should not be null",
            userDetailPage.getGDPRDeleteLastModified().split(" ")[0],
            equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Error for User External key linked to Delinquent Subscription.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testErrorGdprDeleteForUserWithDelinquentSubscription() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);
        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // change NBD in past by 3 Days.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(3),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_NEXT_BILLING_DATE,
            changedNextBillingDate, subscriptionId), getEnvironmentVariables());

        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            Lists.newArrayList(subscriptionId), false, PaymentType.CREDIT_CARD, null, true, buyerUser);
        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        // Processing Renewal order to PENDING State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);

        // Processing Renewal order to DECLINE State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderId);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanErrorConstants.GDPR_ERROR_PRE_CONDITION_FAILED), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.ERROR), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("0"), assertionErrorList);
        paramsMap.put("userExternalKey", buyerUser.getExternalKey());
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be present", paymentProfiles.getTotal(), equalTo("2"),
            assertionErrorList);

        final UserDetailsPage userDetailPage = findUserPage.getByExternalKey(buyerUser.getExternalKey());
        AssertCollector.assertFalse("GDPR Deleted should not be visible", userDetailPage.isGdprDeleteFieldPresent(),
            assertionErrorList);
        AssertCollector.assertFalse("GDPR Deleted Date should not be visible", userDetailPage.isGdprDeleteDateVisible(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Success for User External key linked to Purchase Order Submitted for AUTH. .
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSuccessGdprDeleteForUserWithAuthPurchaseOrder() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();

        new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
            .addPaypalPaymentProfile(buyerUser.getId(), Payment.PaymentProcessor.PAYPAL_NAMER.getValue());

        purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1)
            .getId();

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect error message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanConstants.GDPR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect response code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.SUCCESS), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("1"), assertionErrorList);

        paramsMap.put("userExternalKey", buyerUser.getExternalKey());

        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should be more than one", paymentProfiles.getTotal(), equalTo("2"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Success for Orphan Payment Profile .
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSuccessDeleteOrphanPaymentProfileForGDPRDeleteUser() throws ParseException, IOException {

        final BuyerUser buyerUser = getBuyerUserForGDPRRequest();

        new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
            .addPaypalPaymentProfile(buyerUser.getId(), Payment.PaymentProcessor.PAYPAL_NAMER.getValue());

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(buyerUser.getExternalKey(), eventType, taskId, getEnvironmentVariables());
        AssertCollector.assertThat("Incorrect Message", gdprResponse.getStatus().getDescription(),
            equalTo(PelicanConstants.GDPR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.SUCCESS), assertionErrorList);

        final String gdprDeleteValue = getGdprDeleteValue(buyerUser.getExternalKey());
        AssertCollector.assertThat("Incorrect GDPR Delete value ", gdprDeleteValue, equalTo("1"), assertionErrorList);

        paramsMap.put("userExternalKey", buyerUser.getExternalKey());

        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        AssertCollector.assertThat("Payment profile should not be present", paymentProfiles.getTotal(), equalTo("0"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This tests that email recovery table entry is deleted when the GDPR Delete Request API is called. It also tests
     * the integration between Platform Forge API and Triggers Email Recovery table delete API.
     *
     * Step1: Insert 2 email payload into email_recovery table for the Pelican user. Step2: Call the GDPR delete API for
     * the user created. Step3: Verify that there are no entries in the table for the user.
     *
     * @throws IOException
     */
    @Test
    public void testEmailRecoveryTableEntryIsDeletedForGdprDeleteUser() throws IOException {

        final String gdprDeleteUserExternalKey = getBuyerUserForGDPRRequest().getExternalKey();
        final String idForOrderComplete = RandomStringUtils.randomNumeric(6);

        final String insertOrderCompleteEmailQuery =
            "insert into email_recovery (id, guid, msg_data, created, last_modified) \n" + "values\n" + "("
                + idForOrderComplete + ", '0f969eae-1e28-4859-892b-ed2cc7942751877923784765', \n"
                + "'{\"recipientSrc\":{\"name\":\"oxygen\",\"id\":\"" + gdprDeleteUserExternalKey + "\"},"
                + "\"templateId\":\"bic/OrderComplete\",\"country\":\"US\",\"senderData\":"
                + "{\"address\":\"no-reply@autodeskcommunications.com\",\"name\":\"Autodesk\","
                + "\"bcc\":[{}]},\"templateData\":{\"country\":\"US\","
                + "\"orderNumber\":2074351,\"orderDate\":\"September 10, 2017\",\"paymentProfile\":"
                + "{\"paymentType\":\"VISA\",\"last4Digits\":\"9818\"},\"lineItems\":[{\"type\":\"CLOUD_CREDITS\","
                + "\"offeringType\":\"CURRENCY\",\"productLineName\":\"CloudCreditDgwJRl\",\"quantity\":1,"
                + "\"unitPrice\":\"10.00\",\"totalPrice\":\"10.00\",\"creditDaysDiscount\":\"0.00\","
                + "\"totalDiscount\":\"0.00\",\"amountCharged\":\"10.00\",\"offerName\":\"CloudCreditm2mpN\"}],"
                + "\"totalOrderPrice\":\"10.00\",\"totalPromotionDiscount\":\"0.00\",\"totalCreditDaysDiscount\":\"0.00\","
                + "\"totalDiscount\":\"0.00\",\"totalTaxAmount\":\"0.00\",\"currency\":\"USD\"}}',\n"
                + "'2018-02-23 19:45:55', '2018-02-23 19:45:55');";
        DbUtils.insertOrUpdateQueryFromWorkerDb(insertOrderCompleteEmailQuery, getEnvironmentVariables());

        final String idForCancelBilling = RandomStringUtils.randomNumeric(6);

        final String insertCancelBillingEmailQuery =
            "insert into email_recovery (id, guid, msg_data, created, last_modified) \n" + "values\n" + "("
                + idForCancelBilling + ", '0f969eae-1e28-76348-727b-ed2c6745427518779237847674535', \n"
                + "'{\"recipientSrc\":{\"name\":\"oxygen\",\"id\":\"" + gdprDeleteUserExternalKey + "\"},"
                + "\"templateId\":\"bic/CancelAutomaticBilling\",\"senderData\""
                + ":{\"address\":\"no-reply@autodeskcommunications.com\",\"name\":\"Autodesk\"},\"templateData\""
                + ":{\"productLineName\":\"AUTO_PRODUCT_LINE_MAYA\",\"subscriptionId\":36015545861,\"offering\":{}}}',\n"
                + "'2017-02-23 00:45:55', '2017-02-23 00:45:55');";

        DbUtils.insertOrUpdateQueryFromWorkerDb(insertCancelBillingEmailQuery, getEnvironmentVariables());

        AssertCollector.assertThat("There should be 2 entries for the user in email_recovery table",
            getCountFromEmailRecoveryTable(gdprDeleteUserExternalKey), is(2), assertionErrorList);

        final GDPRResponse gdprResponse =
            userUtils.gdprUserRequest(gdprDeleteUserExternalKey, eventType, taskId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect Code ", gdprResponse.getStatus().getCode(),
            equalTo(PelicanConstants.SUCCESS), assertionErrorList);

        AssertCollector.assertThat("There should not be any entries for the user in email_recovery table",
            getCountFromEmailRecoveryTable(gdprDeleteUserExternalKey), is(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    private int getCountFromEmailRecoveryTable(final String gdprDeleteUserExternalKey) {
        return Integer.parseInt(DbUtils.selectQueryFromWorkerDb(
            "select count(*) from email_recovery where msg_data like '%" + gdprDeleteUserExternalKey + "%';",
            getEnvironmentVariables()).get(0).get("count(*)"));
    }

    /**
     * Add User and Payment profile and Return Buyer User.
     *
     * @return
     */
    private BuyerUser getBuyerUserForGDPRRequest() {

        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());

        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setExternalKey(user.getExternalKey());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        return buyerUser;
    }

    private String getGdprDeleteValue(final String externalKey) {
        return DbUtils.selectQuery(PelicanDbConstants.SELECT_GDPR_DELETE_FROM_NAMED_PARTY + "'" + externalKey + "'",
            "GDPR_DELETED", getEnvironmentVariables()).get(0);
    }
}
