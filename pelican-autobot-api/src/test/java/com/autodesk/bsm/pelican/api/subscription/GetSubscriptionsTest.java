package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
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
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Subscription Api Test: Test methods to test scenarios of all the Subscription APIs
 *
 * @author t_mohag
 */
public class GetSubscriptionsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private Object apiResponse;
    private PurchaseOrderUtils purchaseOrderUtils;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private String bicSubscriptionPlanId;
    private static String bicPrice2;
    private PurchaseOrder purchaseOrderForBicCreditCard2;
    private static final int quantity = 1;
    private Offerings testOfferings;
    private Offerings bicOfferings;
    private Offerings bicOfferings1;
    private HashMap<String, String> requestParametersMap;
    // License models where finite time is false
    private static final int DEFAULT_BLOCK_SIZE = 10;
    private static final int DEFAULT_START_INDEX = 0;
    private Object entity;
    private Subscriptions subscriptions;
    private static final String INCLUDE_ENTITLEMENTS = "offering.entitlements";
    private static final String UPPERCASE_INCLUDE_ENTITLEMENTS = "OFFERING.ENTITLEMENTS";
    private static final String ITEM = "ITEM";
    private String bicCommercialEntitlement1;
    private String bicCommercialEntitlement2;
    private static final String USER_EXTERNAL_KEY_TO_CHECK_PACKAGING = "Automation_test_To_check_Packaging_type_user";
    private static AdminToolPage adminToolPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        testOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Add Bic subscription plan
        bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicPrice2 = bicOfferings.getIncluded().getPrices().get(0).getId();
        bicSubscriptionPlanId = bicOfferings.getOfferings().get(0).getId();

        bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Add two entitlements to the plan
        bicCommercialEntitlement1 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);
        bicCommercialEntitlement2 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);

        purchaseOrderForBicCreditCard2 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicPrice2, getBuyerUser(), quantity);

    }

    @Test
    public void findSubscriptionsByUserExternalKey() {

        // Put request parameters in a Map
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getUserExternalKey());

        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertThat("Incorrect owner external key",
                    subscriptions.getSubscriptions().get(i).getOwnerExternalKey(), is(getUserExternalKey()),
                    assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Subscriptions By owner/user id
     */
    @Test
    public void findSubscriptionsByUserId() {

        // Put request parameters in a Map
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());
        requestParametersMap.put(FieldName.USER_ID.getName(), getUser().getId());

        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertThat("Incorrect owner id", subscriptions.getSubscriptions().get(i).getOwnerId(),
                    is(getUser().getId()), assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Find Subscriptions API for Packaging Type
     */
    @Test(dataProvider = "getPackagingTypes")
    public void findSubscriptionsXMLForPackagingType(final PackagingType packagingType,
        final PackagingType packagingTypeDisplayName) {

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        EditSubscriptionPlanPage editSubscriptionPlanDetailPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);

        // Edit Subscription Plan from AT to change packagingType
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOfferings.getOfferings().get(0).getExternalKey());
        editSubscriptionPlanDetailPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanDetailPage.editPackagingType(packagingType);
        editSubscriptionPlanDetailPage.clickOnSave(false);

        subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOfferings1.getOfferings().get(0).getExternalKey());
        editSubscriptionPlanDetailPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanDetailPage.editPackagingType(packagingType);
        editSubscriptionPlanDetailPage.clickOnSave(false);

        // find user by external key, if doesn't exist then create it.
        User user = new User();
        final Object apiResponse = resource.user().getUserByExternalKey(USER_EXTERNAL_KEY_TO_CHECK_PACKAGING);
        if (apiResponse instanceof HttpError) {
            final Map<String, String> userRequestParam = new HashMap<>();
            userRequestParam.put(UserParameter.NAME.getName(), USER_EXTERNAL_KEY_TO_CHECK_PACKAGING);
            userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), USER_EXTERNAL_KEY_TO_CHECK_PACKAGING);
            user = resource.user().addUser(userRequestParam);
        } else {
            user = (User) apiResponse;
        }

        resource.subscription().add(user.getExternalKey(),
            bicOfferings.getIncluded().getBillingPlans().get(0).getExternalKey(), Currency.USD);
        resource.subscription().add(user.getExternalKey(),
            bicOfferings1.getIncluded().getBillingPlans().get(0).getExternalKey(), Currency.USD);

        // Put request parameters in a Map
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_ID.getName(), user.getId());
        requestParametersMap.put(FieldName.PLAN_ID.getName(), bicOfferings1.getOfferings().get(0).getId());
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), USER_EXTERNAL_KEY_TO_CHECK_PACKAGING);
        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertThat("Incorrect Packaging Type",
                    subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getPackagingType().getDisplayName(),
                    equalTo(packagingTypeDisplayName.getDisplayName()), assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find Subscriptions API works without providing any pagination parameters
     */
    @Test
    public void findSubscriptionsWithUserExternalKey() {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());
        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;
            AssertCollector.assertThat("Unable to get subscriptions using default params. Are there any subscriptions?",
                subscriptions.getSubscriptions(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Total is not null", subscriptions.getTotal(), nullValue(), assertionErrorList);
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void verifyStoreExternalFieldAndPriceListExternalKeyFieldForSubscriptionsByPlanId() {
        bicPrice2 = testOfferings.getIncluded().getPrices().get(0).getId();
        final String planId = testOfferings.getOfferings().get(0).getId();
        // Submit the po with authorize state
        PurchaseOrder puchaseOrderForBic = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicPrice2, getBuyerUser(), quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, puchaseOrderForBic.getId());
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, puchaseOrderForBic.getId());
        // get purchase order api response
        puchaseOrderForBic = resource.purchaseOrder().getById(puchaseOrderForBic.getId());

        // Put request parameters in a Map
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), planId);
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getBuyerUser().getExternalKey());

        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;
            final int total = subscriptions.getTotal();
            if (total > 10) {
                final int startIndex = total - 10;

                // Put request parameters in a Map
                requestParametersMap = new HashMap<>();
                requestParametersMap.put(FieldName.START_INDEX.getName(), String.valueOf(startIndex));
                requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(),
                    getEnvironmentVariables().getUserExternalKey());

                final Subscriptions newSubscriptions =
                    resource.subscriptions().getSubscriptions(requestParametersMap, null);
                final int latestSubscriptionIndex = newSubscriptions.getSubscriptions().size() - 1;
                final Subscription latestSubscription =
                    newSubscriptions.getSubscriptions().get(latestSubscriptionIndex);
                AssertCollector.assertThat(
                    "Unable to get subscriptions using default params. Are there any subscriptions?",
                    subscriptions.getSubscriptions(), is(notNullValue()), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription amount", latestSubscription.getPrice().getAmount(),
                    equalTo(puchaseOrderForBic.getTransactions().getTransactions().get(0).getGatewayResponse()
                        .getAmountCharged()),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "Incorrect Subscription id", latestSubscription.getId(), equalTo(puchaseOrderForBic.getLineItems()
                        .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription quantity", latestSubscription.getQuantity(),
                    equalTo(quantity), assertionErrorList);
                AssertCollector.assertThat("Incorrect status of the subscription", latestSubscription.getStatus(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
                AssertCollector.assertThat("Incorrect store id of a subscription",
                    latestSubscription.getPrice().getStoreId(), equalTo(getStoreIdUs()), assertionErrorList);
                AssertCollector.assertThat("Incorrect price id of a subscription", latestSubscription.getPriceId(),
                    equalTo(bicPrice2), assertionErrorList);
                AssertCollector.assertThat("Incorrect store external key",
                    latestSubscription.getPrice().getStoreExternalKey(), equalTo(getStoreExternalKeyUs()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect pricelist external key",
                    latestSubscription.getPrice().getPricelistExternalKey(), equalTo(getPricelistExternalKeyUs()),
                    assertionErrorList);
                helperToValidateAssertions(subscriptions);
            } else {
                final int latestSubscriptionIndex = subscriptions.getSubscriptions().size() - 1;
                final Subscription latestSubscription = subscriptions.getSubscriptions().get(latestSubscriptionIndex);
                AssertCollector.assertThat(
                    "Unable to get subscriptions using default params. Are there any subscriptions?",
                    subscriptions.getSubscriptions(), is(notNullValue()), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription amount", latestSubscription.getPrice().getAmount(),
                    equalTo(puchaseOrderForBic.getTransactions().getTransactions().get(0).getGatewayResponse()
                        .getAmountCharged()),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "Incorrect Subscription id", latestSubscription.getId(), equalTo(puchaseOrderForBic.getLineItems()
                        .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription quantity", latestSubscription.getQuantity(),
                    equalTo(quantity), assertionErrorList);
                AssertCollector.assertThat("Incorrect status of the subscription", latestSubscription.getStatus(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
                AssertCollector.assertThat("Incorrect store id of a subscription",
                    latestSubscription.getPrice().getStoreId(), equalTo(getStoreIdUs()), assertionErrorList);
                AssertCollector.assertThat("Incorrect price id of a subscription", latestSubscription.getPriceId(),
                    equalTo(bicPrice2), assertionErrorList);
                AssertCollector.assertThat("Incorrect store external key",
                    latestSubscription.getPrice().getStoreExternalKey(), equalTo(getStoreExternalKeyUs()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect pricelist external key",
                    latestSubscription.getPrice().getPricelistExternalKey(), equalTo(getPricelistExternalKeyUs()),
                    assertionErrorList);
                helperToValidateAssertions(subscriptions);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Subscriptions By statuses
     */
    @Test
    public void findSubscriptionsByStatuses() {

        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());
        requestParametersMap.put(FieldName.STATUSES.getName(), Status.ACTIVE.toString());
        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertThat("Incorrect status", subscriptions.getSubscriptions().get(i).getStatus(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Subscriptions By statuses
     */
    @Test
    public void findSubscriptionsByMultipleRequestParameters() {

        // Prepare request body with request parameters
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.STATUSES.getName(),
            Status.ACTIVE.toString() + ',' + Status.EXPIRED.toString());
        requestParametersMap.put(FieldName.USER_ID.getName(), getUser().getId());
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getUserExternalKey());
        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        // put all the status in a set for assertion purpose
        final Set<String> setOfStatuses = new HashSet<>();
        setOfStatuses.add(Status.ACTIVE.toString());
        setOfStatuses.add(Status.EXPIRED.toString());

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertTrue("Incorrect status",
                    setOfStatuses.contains(subscriptions.getSubscriptions().get(i).getStatus()), assertionErrorList);
                AssertCollector.assertThat("Incorrect owner external key",
                    subscriptions.getSubscriptions().get(i).getOwnerExternalKey(), is(getUserExternalKey()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect owner id", subscriptions.getSubscriptions().get(i).getOwnerId(),
                    is(getUser().getId()), assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Subscriptions By days past expired Filters out Subscriptions that have expired more than
     * this many days ago.
     */
    // TODO : Commented out this testclass on 04/19 by Shweta Hegde, since
    // subscription expiration is causing
    // this testcase to fail
    @Test(enabled = false)
    public void findSubscriptionsByDaysPastExpired() throws ParseException {

        final int daysPastExpiration = 1;
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.DAYS_PAST_EXPIRED.getName(), String.valueOf(daysPastExpiration));
        requestParametersMap.put(FieldName.LAST_MODIFIED_AFTER.getName(),
            DateTimeUtils.getNowMinusDays(PelicanConstants.DB_DATE_FORMAT, 1) + " " + PelicanConstants.UTC_TIME_ZONE);
        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {

                // Get Expiration Date of a subscription and today's date
                // Difference between these 2 dates should be lesser than or
                // equal to "daysPastExpired"
                final SimpleDateFormat dateformat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
                final Date expirationDate =
                    dateformat.parse(subscriptions.getSubscriptions().get(i).getExpirationDate());
                final Date currentDate =
                    dateformat.parse(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

                final DateTime today = new DateTime(currentDate);
                final DateTime daysBefore = new DateTime(expirationDate);

                // Getting the difference between dates
                final int actualPastExpirationDays = Days.daysBetween(today, daysBefore).getDays();

                AssertCollector.assertTrue("Incorrect days past expired",
                    (actualPastExpirationDays <= daysPastExpiration), assertionErrorList);

                AssertCollector.assertThat("Incorrect status", subscriptions.getSubscriptions().get(i).getStatus(),
                    equalTo(Status.EXPIRED.toString()), assertionErrorList);

            }
            helperToValidateAssertions(subscriptions);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case is a regression on Find Subscriptions API when PelicanConstants.CONTENT_TYPE_XML is
     * application\xml As of now (7/25/16) XML is NOT supporting Field Name "Subscription[Ids]" This test case validates
     * when subscription id is provided it just ignores and gives the result the same way with default parameters
     */
    @Test
    public void testSubscriptionIdsAreIgnoredWhenContentTypeIsXML() {

        requestParametersMap = new HashMap<>();
        final String subscriptionIds = "12345678901234567890";

        // sending subscription id, but this field will be ignored in XML
        requestParametersMap.put(FieldName.SUBSCRIPTION_IDS.getName(), subscriptionIds);

        entity = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
        subscriptions = (Subscriptions) entity;

        AssertCollector.assertThat("Subscription id field is not ignored.",
            subscriptions.getSubscriptions().get(0).getId(), not(subscriptionIds), assertionErrorList);
        AssertCollector.assertThat("Incorrect block size", subscriptions.getBlockSize(), is(DEFAULT_BLOCK_SIZE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect start index", subscriptions.getStartIndex(), is(DEFAULT_START_INDEX),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests positive scenario when valid include value is provided which returns all entitlement
     * details for Subscriptions.
     */
    @Test
    public void testSuccessGetSubscriptionsWithValidInclude() {

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard2.getId());
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard2.getId());
        // get purchase order api response
        purchaseOrderForBicCreditCard2 = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard2.getId());

        // provide params to getSubscriptions
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), bicSubscriptionPlanId);
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());
        requestParametersMap.put(FieldName.INCLUDE.getName(), INCLUDE_ENTITLEMENTS);
        final Object entity =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        final Subscriptions subscriptions = (Subscriptions) entity;
        AssertCollector.assertThat("No subscriptions found", subscriptions.getSubscriptions().size(), greaterThan(0),
            assertionErrorList);

        int numMatchingEntitlementsFound = 0;
        for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
            final int numEntitlements = subscriptions.getSubscriptions().get(i).getSubscriptionPlan()
                .getOneTimeEntitlements().getEntitlements().size();
            for (int j = 0; j < numEntitlements; j++) {
                final Entitlement oneTimeEntitlement = subscriptions.getSubscriptions().get(i).getSubscriptionPlan()
                    .getOneTimeEntitlements().getEntitlements().get(j);
                if (oneTimeEntitlement.getId().equals(bicCommercialEntitlement1)) {
                    numMatchingEntitlementsFound++;
                } else if (oneTimeEntitlement.getId().equals(bicCommercialEntitlement2)) {
                    numMatchingEntitlementsFound++;
                }
                AssertCollector.assertThat(
                    "Invalid one time entitlement name", subscriptions.getSubscriptions().get(i).getSubscriptionPlan()
                        .getOneTimeEntitlements().getEntitlements().get(j).getName(),
                    notNullValue(), assertionErrorList);
                AssertCollector.assertThat(
                    "Invalid one time entitlement external key", subscriptions.getSubscriptions().get(i)
                        .getSubscriptionPlan().getOneTimeEntitlements().getEntitlements().get(j).getExternalKey(),
                    notNullValue(), assertionErrorList);
                AssertCollector.assertThat(
                    "Invalid one time entitlement type", subscriptions.getSubscriptions().get(i).getSubscriptionPlan()
                        .getOneTimeEntitlements().getEntitlements().get(j).getType(),
                    equalTo(ITEM), assertionErrorList);
                AssertCollector.assertThat("Invalid one time entitlement item type external key",
                    subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getOneTimeEntitlements()
                        .getEntitlements().get(j).getItemTypeExternalKey(),
                    notNullValue(), assertionErrorList);
                AssertCollector
                    .assertThat("Invalid one time entitlement licensing model external key",
                        subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getOneTimeEntitlements()
                            .getEntitlements().get(j).getLicensingModelExternalKey(),
                        notNullValue(), assertionErrorList);
            }
        }
        AssertCollector.assertThat("Incorrect number of matching one time entitlements", numMatchingEntitlementsFound,
            equalTo(2), assertionErrorList);
        helperToValidateAssertions(subscriptions);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests negative scenario with invalid include value
     */
    @Test
    public void testErrorWithInvalidIncludeValue() {
        // provide params to getSubscriptions
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getUserExternalKey());
        requestParametersMap.put(FieldName.INCLUDE.getName(), RandomStringUtils.random(9));
        apiResponse =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
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
    @Test
    public void testErrorWithUpperCaseIncludeValue() {
        // provide params to getSubscriptions
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.INCLUDE.getName(), UPPERCASE_INCLUDE_ENTITLEMENTS);
        apiResponse =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
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
    public void testGetSubscriptionsXmlQuantityToReduceWithNullValue() {
        final Offerings offering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String offeringId = offering.getOfferings().get(0).getId();
        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 1);

        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);

        // Fulfill meta request
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // get purchase order again
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), offeringId);
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());

        subscriptions =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);

        AssertCollector.assertThat("Incorrect subscription id.", subscriptions.getSubscriptions().get(0).getId(),
            equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity to reduce value.",
            subscriptions.getSubscriptions().get(0).getQtyToReduce(), is(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method helps to assert different values for many test methods.
     */
    private void helperToValidateAssertions(final Subscriptions subscriptions) {

        AssertCollector.assertThat("Unable to get subscriptions by user external key. Are there any subscriptions?",
            subscriptions.getSubscriptions(), is(notNullValue()), assertionErrorList);

        for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
            AssertCollector.assertThat("Incorrect owner external key",
                subscriptions.getSubscriptions().get(i).getOwnerExternalKey(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Application Family Id ",
                subscriptions.getSubscriptions().get(i).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing count",
                subscriptions.getSubscriptions().get(i).getBillingOption().getBillingPeriod().getCount(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing type",
                subscriptions.getSubscriptions().get(i).getBillingOption().getBillingPeriod().getType(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Plan usage type",
                subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getUsageType(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Plan support level",
                subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getSupportLevel(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect application family id",
                subscriptions.getSubscriptions().get(i).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        }
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
