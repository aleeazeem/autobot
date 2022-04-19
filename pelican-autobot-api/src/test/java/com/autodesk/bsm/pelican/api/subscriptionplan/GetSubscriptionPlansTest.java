package com.autodesk.bsm.pelican.api.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.OfferingsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlanClient;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlansClient.Parameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlans;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Get Subscription Plans api tests
 *
 * @author Shweta Hegde
 */
public class GetSubscriptionPlansTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private Map<String, String> params;
    private Object apiResponse;
    private HttpError httpError;
    private SubscriptionPlans subscriptionPlans;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private int blockSize;
    private int startIndex;
    private static final int defaultStartIndex = 0;
    private static final int defaultBlockSize = 10;
    private String productLineExternalKey;
    private String nameOfSubscriptionPlan;
    private String externalKeyOfSubscriptionPlan;
    private String offerExternalKey;
    private String updateStatusOfSubscriptionPlan;
    private Offerings trialActiveSubscriptionPlan;
    private String activeTrialSubscriptionOffer1Id;
    private String activeTrialSubscriptionOffer2Id;
    private String activeTrialSubscriptionOffer4Id;
    private Offerings commercialActiveSubscriptionPlan;
    private String activeCommercialSubscriptionOffer1Id;
    private String activeCommercialSubscriptionOffer2Id;
    private String activeCommercialSubscriptionOffer4Id;
    private String subscriptionPlanIdWithIc;
    private String subscriptionPlanIdWithVg;
    private String subscriptionPlanIdWithoutIc;
    private String subscriptionPlanExtKeyWithIc;
    private String subscriptionPlanExtKeyWithVg;
    private String subscriptionPlanExtKeyWithoutIc;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());

        adminToolPage.login();

        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings offerings1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings offerings2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.QUARTER, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.EDU);
        final Offerings offerings3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.EDU);
        final Offerings offerings4 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.WEEK, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.GOV);

        productLineExternalKey = offerings1.getOfferings().get(0).getProductLine();
        nameOfSubscriptionPlan = offerings2.getOfferings().get(0).getName();
        externalKeyOfSubscriptionPlan = offerings3.getOfferings().get(0).getExternalKey();
        offerExternalKey = offerings4.getIncluded().getBillingPlans().get(0).getExternalKey();
        updateStatusOfSubscriptionPlan = offerings4.getOfferings().get(0).getId();

        // create Subscription plans to get them considering Price Effective
        // dates
        // Create A Trial Subscription Plan
        trialActiveSubscriptionPlan = subscriptionPlanApiUtils.addSubscriptionPlan(resource,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.TRL);
        final String trialActiveSubscriptionPlanId = trialActiveSubscriptionPlan.getOfferings().get(0).getId();

        // Add Active offer to the Trial Subscription Plan
        final SubscriptionOffer activeTrialSubscriptionOffer1 = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.MONTH, 1, Status.ACTIVE),
            trialActiveSubscriptionPlanId);
        activeTrialSubscriptionOffer1Id = activeTrialSubscriptionOffer1.getData().getId();
        // Add Active Price(starting today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 0, 15),
            trialActiveSubscriptionPlanId, activeTrialSubscriptionOffer1Id);

        // Add another active subscription offer to the trial subscription plan
        final SubscriptionOffer activeTrialSubscriptionOffer2 = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 3, Status.ACTIVE),
            trialActiveSubscriptionPlanId);
        activeTrialSubscriptionOffer2Id = activeTrialSubscriptionOffer2.getData().getId();
        // Add Active Price(ending today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 0, 0),
            trialActiveSubscriptionPlanId, activeTrialSubscriptionOffer2Id);

        // Add another active subscription offer to the trial subscription plan
        final SubscriptionOffer activeTrialSubscriptionOffer3 = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.DAY, 1, Status.ACTIVE),
            trialActiveSubscriptionPlanId);
        final String activeTrialSubscriptionOffer3Id = activeTrialSubscriptionOffer3.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 1, 10),
            trialActiveSubscriptionPlanId, activeTrialSubscriptionOffer3Id);

        // Add another active subscription offer to the trial subscription plan
        final SubscriptionOffer activeTrialSubscriptionOffer4 = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.SEMIYEAR, 1,
                Status.ACTIVE),
            trialActiveSubscriptionPlanId);
        activeTrialSubscriptionOffer4Id = activeTrialSubscriptionOffer4.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 1, 10),
            trialActiveSubscriptionPlanId, activeTrialSubscriptionOffer4Id);
        // Add Active Price(ending today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUk(), 0, 0),
            trialActiveSubscriptionPlanId, activeTrialSubscriptionOffer4Id);

        // Add Inactive subscription offer to the trial subscription plan
        final SubscriptionOffer inactiveTrialSubscriptionOffer1 = subscriptionPlanApiUtils.addSubscriptionOffer(
            resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.QUARTER, 1, Status.NEW),
            trialActiveSubscriptionPlanId);
        final String inactiveTrialSubscriptionOffer1Id = inactiveTrialSubscriptionOffer1.getData().getId();
        // Add Active Price(ending today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 0, 5),
            trialActiveSubscriptionPlanId, inactiveTrialSubscriptionOffer1Id);

        // Add another Inactive subscription offer to the trial subscription
        // plan
        final SubscriptionOffer inactiveTrialSubscriptionOffer2 = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null,
                BillingFrequency.SEMIMONTH, 1, Status.NEW), trialActiveSubscriptionPlanId);
        final String inactiveTrialSubscriptionOffer2Id = inactiveTrialSubscriptionOffer2.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(0, getPricelistExternalKeyUs(), 4, 5),
            trialActiveSubscriptionPlanId, inactiveTrialSubscriptionOffer2Id);

        // Create A Commercial Subscription Plan
        commercialActiveSubscriptionPlan = subscriptionPlanApiUtils.addSubscriptionPlan(resource,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String commercialActiveSubscriptionPlanId =
            commercialActiveSubscriptionPlan.getOfferings().get(0).getId();

        // Add Active offer to the Commercial Subscription Plan
        final SubscriptionOffer activeCommercialSubscriptionOffer1 = subscriptionPlanApiUtils.addSubscriptionOffer(
            resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.MONTH, 1, Status.ACTIVE),
            commercialActiveSubscriptionPlanId);
        activeCommercialSubscriptionOffer1Id = activeCommercialSubscriptionOffer1.getData().getId();
        // Add Active Price(starting today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(100, getPricelistExternalKeyUs(), 0, 15),
            commercialActiveSubscriptionPlanId, activeCommercialSubscriptionOffer1Id);

        // Add another active subscription offer to the Commercial subscription
        // plan
        final SubscriptionOffer activeCommercialSubscriptionOffer2 = subscriptionPlanApiUtils.addSubscriptionOffer(
            resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 2, Status.ACTIVE),
            commercialActiveSubscriptionPlanId);
        activeCommercialSubscriptionOffer2Id = activeCommercialSubscriptionOffer2.getData().getId();
        // Add Active Price(ending today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(500, getPricelistExternalKeyUs(), 0, 0),
            commercialActiveSubscriptionPlanId, activeCommercialSubscriptionOffer2Id);

        // Add another active subscription offer to the Commercial subscription
        // plan
        final SubscriptionOffer activeCommercialSubscriptionOffer3 = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null,
                BillingFrequency.SEMIYEAR, 1, Status.ACTIVE), commercialActiveSubscriptionPlanId);
        final String activeCommercialSubscriptionOffer3Id = activeCommercialSubscriptionOffer3.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(500, getPricelistExternalKeyUs(), 1, 10),
            commercialActiveSubscriptionPlanId, activeCommercialSubscriptionOffer3Id);

        // Add another active subscription offer to the Commercial subscription
        // plan
        final SubscriptionOffer activeCommercialSubscriptionOffer4 = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null,
                BillingFrequency.SEMIMONTH, 1, Status.ACTIVE), commercialActiveSubscriptionPlanId);
        activeCommercialSubscriptionOffer4Id = activeCommercialSubscriptionOffer4.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(500, getPricelistExternalKeyUs(), 1, 10),
            commercialActiveSubscriptionPlanId, activeCommercialSubscriptionOffer4Id);
        // Add Active Price(ending today) to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUk(), 0, 0),
            commercialActiveSubscriptionPlanId, activeCommercialSubscriptionOffer4Id);

        // Add Inactive subscription offer to the Commercial subscription plan
        final SubscriptionOffer inactiveCommercialSubscriptionOffer1 =
            subscriptionPlanApiUtils.addSubscriptionOffer(resource,
                subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.WEEK, 1, Status.NEW),
                commercialActiveSubscriptionPlanId);
        final String inactiveCommercialSubscriptionOffer1Id = inactiveCommercialSubscriptionOffer1.getData().getId();
        // Add Active Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 0, 5),
            commercialActiveSubscriptionPlanId, inactiveCommercialSubscriptionOffer1Id);

        // Add another Inactive subscription offer to the Commercial
        // subscription plan
        final SubscriptionOffer inactiveCommercialSubscriptionOffer2 = subscriptionPlanApiUtils.addSubscriptionOffer(
            resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.QUARTER, 1, Status.NEW),
            commercialActiveSubscriptionPlanId);
        final String inactiveCommercialSubscriptionOffer2Id = inactiveCommercialSubscriptionOffer2.getData().getId();
        // Add InActive Price to the active subscription offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(300, getPricelistExternalKeyUs(), 3, 5),
            commercialActiveSubscriptionPlanId, inactiveCommercialSubscriptionOffer2Id);

        // Create A Subscription Plan with industrial collection
        subscriptionPlanExtKeyWithIc = PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String productLineNameAndExternalKey =
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKeyWithIc, subscriptionPlanExtKeyWithIc,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.INDUSTRY_COLLECTION,
            true);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithIc = subscriptionPlanDetailPage.getId();

        // Create A Subscription Plan with Vertical grouping
        subscriptionPlanExtKeyWithVg = PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKeyWithVg, subscriptionPlanExtKeyWithVg,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.VERTICAL_GROUPING,
            true);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithVg = subscriptionPlanDetailPage.getId();

        // Create A Subscription Plan with None
        subscriptionPlanExtKeyWithoutIc =
            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKeyWithoutIc,
            subscriptionPlanExtKeyWithoutIc, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, PackagingType.NONE, true);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithoutIc = subscriptionPlanDetailPage.getId();

    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        params = new HashMap<>();
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
     * Description : This method qualifies for smoke test. This method gets all subscription plans with no arguments.
     * Validation : Validates the payload is not null, subscription plans are more than 1, start index and block size
     * are as default and application family is auto.
     */
    @Test
    public void getAllSubscriptionPlans() {
        // No parameters passed, call to get subscription plans api
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) { // if get subscription plans api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to SubscriptionPlan type
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans", subscriptionPlans.getSubscriptionPlans(),
                is(notNullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with product line in arguments. Validation : Validates the
     * payload is not null, subscription plans are more than 1, start index and block size are as default and
     * application family is auto. And product line of each subscription plan is as expected
     */
    @Test
    public void getSubscriptionPlansByProductLineExternalKey() {
        // Product Line External Key is passed as parameter to call get
        // subscription plans api
        params.put(Parameter.PRODUCT_LINE.getName(), productLineExternalKey);
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat(
                "Unable to find subscription plans with product line external key= " + productLineExternalKey,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of subscription plans ",
                subscriptionPlans.getSubscriptionPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect product line " + productLineExternalKey,
                    subscriptionPlans.getSubscriptionPlans().get(i).getProductLine().getName(),
                    equalTo(productLineExternalKey), assertionErrorList);
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                AssertCollector.assertThat("OfferingType is missing in response",
                    subscriptionPlans.getSubscriptionPlans().get(i).getOfferingType(),
                    isOneOf(OfferingType.BIC_SUBSCRIPTION.toString(), OfferingType.META_SUBSCRIPTION.toString()),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with active product line in arguments. Validation :
     * Validates the payload is not null, subscription plans are more than 1, start index and block size are as default
     * and application family is auto. And product line of each subscription plan is as expected and status is "Active"
     */
    @Test
    public void getSubscriptionPlanByActiveProductLineExternalKey() {
        // Product Line External Key and Status "Active" passed as parameter to
        // call get subscription
        // plans api
        params.put(Parameter.PRODUCT_LINE.getName(), productLineExternalKey);
        params.put(Parameter.STATUS.getName(), Status.ACTIVE.toString());
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat(
                "Unable to find ACTIVE subscription plans with product line external key = " + productLineExternalKey,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of subscription plans",
                subscriptionPlans.getSubscriptionPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            AssertCollector.assertThat("Incorrect product line " + productLineExternalKey,
                subscriptionPlans.getSubscriptionPlans().get(0).getProductLine().getName(),
                equalTo(productLineExternalKey), assertionErrorList);
            AssertCollector.assertThat("The subscription plan is not active",
                subscriptionPlans.getSubscriptionPlans().get(0).getStatus(), equalTo(Status.ACTIVE.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect application family",
                subscriptionPlans.getSubscriptionPlans().get(0).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans according to the block size. Validation : Validates the
     * payload is not null, start index and block size are as default. Application family is auto. And block size is as
     * expected and only "block size" number subscription plans are returned
     */
    @Test
    public void getSubscriptionPlanByProductLineExternalKeyAndBlockSize() {
        blockSize = 15;
        // Product Line External Key and block size passed as parameter to call
        // get subscription plans
        // api
        params.put(Parameter.PRODUCT_LINE.getName(), productLineExternalKey);
        params.put(Parameter.BLOCK_SIZE.getName(), Integer.toString(blockSize));
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat(
                "Unable to find subscription plans with product line external key " + productLineExternalKey,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Block size is not " + blockSize, subscriptionPlans.getBlockSize(),
                is(blockSize), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect product line " + productLineExternalKey,
                    subscriptionPlans.getSubscriptionPlans().get(i).getProductLine().getName(),
                    equalTo(productLineExternalKey), assertionErrorList);
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                AssertCollector.assertThat("OfferingType is missing in response",
                    subscriptionPlans.getSubscriptionPlans().get(i).getOfferingType(),
                    isOneOf(OfferingType.BIC_SUBSCRIPTION.toString(), OfferingType.META_SUBSCRIPTION.toString()),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets a subscription plan with exact name of subscription plan in arguments. Validation
     * : Validates the payload is not null, number of subscription plan is 1, start index and block size are as default
     * and application family is auto. And Subscription plan name is as expected.
     */
    @Test
    public void getSubscriptionPlanByExactName() {
        // Subscription plan name is passed as parameter to call get
        // subscription plans api
        params.put(Parameter.PLAN_NAME.getName(), nameOfSubscriptionPlan);
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with name " + nameOfSubscriptionPlan,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of subscription plans ",
                subscriptionPlans.getSubscriptionPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Subscription plan name is not " + nameOfSubscriptionPlan,
                subscriptionPlans.getSubscriptionPlans().get(0).getName(), equalTo(nameOfSubscriptionPlan),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect application family",
                subscriptionPlans.getSubscriptionPlans().get(0).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets a subscription plan with external key of subscription plan in arguments.
     * Validation : Validates the payload is not null, number of subscription plan is 1, start index and block size are
     * as default and application family is auto. And Subscription plan external key is as expected.
     */
    @Test
    public void getSubscriptionPlanByExternalKey() {
        // Subscription plan external key is passed as parameter to call get
        // subscription plans api
        params.put(Parameter.PLAN_EXT_KEYS.getName(), externalKeyOfSubscriptionPlan);
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat(
                "Unable to find subscription plans with external key " + externalKeyOfSubscriptionPlan,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of subscription plans ",
                subscriptionPlans.getSubscriptionPlans().size(), is(1), assertionErrorList);
            AssertCollector.assertThat("Subscription plan external key is not " + externalKeyOfSubscriptionPlan,
                subscriptionPlans.getSubscriptionPlans().get(0).getExternalKey(),
                equalTo(externalKeyOfSubscriptionPlan), assertionErrorList);
            AssertCollector.assertThat("Incorrect application family",
                subscriptionPlans.getSubscriptionPlans().get(0).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with status "new" in arguments. Validation : Validates the
     * payload is not null, start index and block size are as default and application family is auto. Number of new
     * subscription plans and their status is "New"
     */
    @Test
    public void getNewSubscriptionPlans() {
        final String newStatus = Status.NEW.toString();
        // Status "New" and skip count "false" passed as parameter to call get
        // subscription plans api
        params.put(Parameter.STATUS.getName(), newStatus);
        params.put(Parameter.SKIP_COUNT.getName(), "false");
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with status " + newStatus,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of new subscription plans ", subscriptionPlans.getTotal(),
                is(DbUtils.getTotalNumberOfSubscriptionPlans(Status.NEW.ordinal(), getEnvironmentVariables())),
                assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                AssertCollector.assertThat("Status of subscription plan is not " + newStatus,
                    subscriptionPlans.getSubscriptionPlans().get(i).getStatus(), equalTo(newStatus),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with status "Active" in arguments. Validation : Validates
     * the payload is not null, start index and block size are as default and application family is auto. Number of
     * active subscription plans and their status is "Active"
     */
    @Test
    public void getActiveSubscriptionPlans() {
        final String activeStatus = Status.ACTIVE.toString();
        // Status "Active" and skip count "false" passed as parameter to call
        // get subscription plans api
        params.put(Parameter.STATUS.getName(), activeStatus);
        params.put(Parameter.SKIP_COUNT.getName(), "false");
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with status " + activeStatus,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of active subscription plans ", subscriptionPlans.getTotal(),
                lessThanOrEqualTo(
                    DbUtils.getTotalNumberOfSubscriptionPlans(Status.ACTIVE.ordinal(), getEnvironmentVariables())),
                assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                AssertCollector.assertThat("Status of subscription plan is not " + activeStatus,
                    subscriptionPlans.getSubscriptionPlans().get(i).getStatus(), equalTo(activeStatus),
                    assertionErrorList);
                AssertCollector.assertThat("OfferingType is missing in response",
                    subscriptionPlans.getSubscriptionPlans().get(i).getOfferingType(),
                    isOneOf(OfferingType.BIC_SUBSCRIPTION.toString(), OfferingType.META_SUBSCRIPTION.toString()),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with status "canceled" in arguments. Validation : Validates
     * the payload is not null, start index and block size are as default and application family is auto. Number of
     * canceled subscription plans and their status is "Canceled"
     */
    @Test
    public void getCanceledSubscriptionPlans() {
        // Subscription plan created are in "Active" State. It is canceled for
        // the testing purpose
        final SubscriptionPlanClient subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final HashMap<String, String> requestBody = new HashMap<>();
        // Subscription plan id and status passed to cancel subscription plan
        requestBody.put("subscriptionPlanId", updateStatusOfSubscriptionPlan);
        requestBody.put("status", "CANCELED");
        subscriptionPlanResource.update(requestBody);
        final String canceledStatus = Status.CANCELED.toString();
        // Status "Canceled" and skip count "false" passed as parameter to call
        // get subscription plans
        // api
        params.put(Parameter.STATUS.getName(), canceledStatus);
        params.put(Parameter.SKIP_COUNT.getName(), "false");
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with status " + canceledStatus,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of canceled subscription plans ", subscriptionPlans.getTotal(),
                is(DbUtils.getTotalNumberOfSubscriptionPlans(Status.CANCELED.ordinal(), getEnvironmentVariables())),
                assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                AssertCollector.assertThat("Status of subscription plan is not " + canceledStatus,
                    subscriptionPlans.getSubscriptionPlans().get(i).getStatus(), equalTo(canceledStatus),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method get subscription plans with offer external key in arguments. Validation : Validates the
     * payload is not null, start index and block size are as default and application family is auto. Offer external key
     * of subscription offers of subscription plans
     */
    @Test
    public void getSubscriptionPlansByOfferExternalKey() {
        // Offer external key is passed as parameter to call get subscription
        // plans api
        params.put(Parameter.OFFER_EXT_KEYs.getName(), offerExternalKey);
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with offer external key " + offerExternalKey,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + startIndex, subscriptionPlans.getStartIndex(),
                is(startIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    is(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
                for (int j = 0; j < subscriptionPlans.getSubscriptionPlans().get(i).getSubscriptionOffers()
                    .getSubscriptionOffers().size(); j++) {
                    AssertCollector.assertThat(
                        "Incorrect Offer external key " + offerExternalKey, subscriptionPlans.getSubscriptionPlans()
                            .get(i).getSubscriptionOffers().getSubscriptionOffers().get(j).getExternalKey(),
                        is(offerExternalKey), assertionErrorList);
                    AssertCollector.assertThat("OfferingType is missing in response",
                        subscriptionPlans.getSubscriptionPlans().get(i).getOfferingType(),
                        isOneOf(OfferingType.BIC_SUBSCRIPTION.toString(), OfferingType.META_SUBSCRIPTION.toString()),
                        assertionErrorList);
                }
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method to Get subscription plans external Key for PackagingType.
     */
    @Test(dataProvider = "getSubscriptionPlanIdForPackagingType")
    public void getSubscriptionPlansForPackagingType(final String subscriptionPlanExtKey,
        final String subscriptionPlanId, final PackagingType packagingType) {
        // Offer Id is passed as parameter to call get subscription plans api
        params.put(Parameter.PLAN_EXT_KEYS.getName(), subscriptionPlanExtKey);
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                if (packagingType.getDisplayName().equals(PackagingType.IC.getDisplayName())) {
                    AssertCollector.assertThat("Incorrect Packaging Type value in response, excpeted IC",
                        subscriptionPlans.getSubscriptionPlans().get(i).getPackagingType(), equalTo(PackagingType.IC),
                        assertionErrorList);
                } else if (packagingType.getDisplayName().equals(PackagingType.VG.getDisplayName())) {
                    AssertCollector.assertThat("Incorrect Packaging Type value in response, excpeted VG",
                        subscriptionPlans.getSubscriptionPlans().get(i).getPackagingType(), equalTo(PackagingType.VG),
                        assertionErrorList);
                } else if (packagingType.getDisplayName().equals(PackagingType.NONE.getDisplayName())) {
                    AssertCollector.assertThat("Invalid PackagingType, excpeted NONE",
                        subscriptionPlans.getSubscriptionPlans().get(i).getPackagingType(), nullValue(),
                        assertionErrorList);
                }
                AssertCollector.assertThat("Incorrect Subscription Plan ID",
                    subscriptionPlans.getSubscriptionPlans().get(i).getId(), equalTo(subscriptionPlanId),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with start index as given. Validation : Validates the
     * payload is not null, block size is as default and application family is auto. Start index is as expected.
     */
    @Test
    public void getSubscriptionPlansByStartIndex() {
        startIndex = 4;
        // Start index is passed as parameter to call get subscription plans api
        params.put(Parameter.START_INDEX.getName(), Integer.toString(startIndex));
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans with startIndex " + startIndex,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + startIndex, subscriptionPlans.getStartIndex(),
                is(startIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription plans with start index and block size as given. Validation :
     * Validates the payload is not null, application family is auto. Start index and block size as expected.
     */
    @Test
    public void getSubscriptionPlansByStartIndexAndBlockSize() {
        startIndex = 5;
        blockSize = 15;
        // Start index and block size passed as parameter to call get
        // subscription plans api
        params.put(Parameter.START_INDEX.getName(), Integer.toString(startIndex));
        params.put(Parameter.BLOCK_SIZE.getName(), Integer.toString(blockSize));
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat(
                "Unable to find subscription plans with startIndex " + startIndex + " and block size " + blockSize,
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + startIndex, subscriptionPlans.getStartIndex(),
                is(startIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + blockSize, subscriptionPlans.getBlockSize(),
                is(blockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets all subscription along with "total" attribute in the XML Validation : Validates
     * the payload is not null, start index and block size are as default and application family is auto. XML attribute
     * "total" is displayed. The value is validated with the database.
     */
    @Test
    public void getSubscriptionPlansBySkipCount() {
        // Skip count "false" passed as parameter to call get subscription plans
        // api
        params.put(Parameter.SKIP_COUNT.getName(), "false");
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans when skip count is false",
                subscriptionPlans.getSubscriptionPlans(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect number of total subscription plans", subscriptionPlans.getTotal(),
                lessThanOrEqualTo(DbUtils.getTotalNumberOfSubscriptionPlan(getEnvironmentVariables())),
                assertionErrorList);
            AssertCollector.assertThat("Start Index is not " + defaultStartIndex, subscriptionPlans.getStartIndex(),
                is(defaultStartIndex), assertionErrorList);
            AssertCollector.assertThat("Block Size is not " + defaultBlockSize, subscriptionPlans.getBlockSize(),
                is(defaultBlockSize), assertionErrorList);
            for (int i = 0; i < subscriptionPlans.getSubscriptionPlans().size(); i++) {
                AssertCollector.assertThat("Incorrect application family",
                    subscriptionPlans.getSubscriptionPlans().get(i).getApplicationFamilyId(),
                    equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets TRIAL subscription plans with offers which has Active subscription offer and
     * active price starting today or active today and ending today An ACTIVE offer which has both active and inactive
     * Prices are returned in the response 3 offers which are either INACTIVE or which has INACTIVE prices are not
     * returned in the response Validation : Validates the payload is not null, Offers which are active and has active
     * price
     */
    @Test
    public void testGetTrialSubscriptionPlansWithActiveSubscriptionOfferAndActivePrice() {

        params.put(Parameter.PLAN_EXT_KEYS.getName(),
            trialActiveSubscriptionPlan.getOfferings().get(0).getExternalKey());
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans",
                subscriptionPlans.getSubscriptionPlans().size(), not(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription offers size",
                subscriptionPlans.getSubscriptionPlans().get(0).getSubscriptionOffers().getSubscriptionOffers().size(),
                is(3), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for price starting today", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(0).getId(),
                equalTo(activeTrialSubscriptionOffer1Id), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for price ending today", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(1).getId(),
                equalTo(activeTrialSubscriptionOffer2Id), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for multiple prices", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(2).getId(),
                equalTo(activeTrialSubscriptionOffer4Id), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Description : This method gets COMMERCIAL subscription plans with offers which has Active subscription offer and
     * active price starting today or active today and ending today An ACTIVE offer which has both active and inactive
     * Prices are returned in the response 3 offers which are either INACTIVE or which has INACTIVE prices are not
     * returned in the response Validation : Validates the payload is not null, Offers which are active and has active
     * price
     */
    @Test
    public void testGetCommercialSubscriptionPlansWithActiveSubscriptionOfferAndActivePrice() {

        params.put(Parameter.PLAN_NAME.getName(), commercialActiveSubscriptionPlan.getOfferings().get(0).getName());
        apiResponse = resource.subscriptionPlans().getSubscriptionPlans(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            subscriptionPlans = (SubscriptionPlans) apiResponse;
            AssertCollector.assertThat("Unable to find subscription plans",
                subscriptionPlans.getSubscriptionPlans().size(), not(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription offers size",
                subscriptionPlans.getSubscriptionPlans().get(0).getSubscriptionOffers().getSubscriptionOffers().size(),
                is(3), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for price starting today", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(0).getId(),
                equalTo(activeCommercialSubscriptionOffer1Id), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for price ending today", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(1).getId(),
                equalTo(activeCommercialSubscriptionOffer2Id), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect subscription offer for multiple prices", subscriptionPlans.getSubscriptionPlans().get(0)
                    .getSubscriptionOffers().getSubscriptionOffers().get(2).getId(),
                equalTo(activeCommercialSubscriptionOffer4Id), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify offer price can be added with dates in past for subscription plan through add subscription offer price api
     *
     * @result Date should be added in Past
     */
    @Test
    public void addPricesWithPastDateInSubscriptionOffer() {
        final String effectiveStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 30);
        final String effectiveEndDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15);
        final Offerings subscriptionOffer = subscriptionPlanApiUtils.addSubscriptionPlan(resource,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionPlanId = subscriptionOffer.getOfferings().get(0).getId();

        // Add Active offer to the commercial Subscription Plan
        final SubscriptionOffer activeSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.MONTH, 1, Status.ACTIVE),
            subscriptionPlanId);
        final String subscriptionOfferId = activeSubscriptionOffer.getData().getId();

        // add prices to a subscription offer
        final SubscriptionOfferPrice subscriptionOfferPrice = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(
            resource, subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(0,
                getPricelistExternalKeyUs(), effectiveStartDate, effectiveEndDate),
            subscriptionPlanId, subscriptionOfferId);
        AssertCollector.assertThat("Prices are not added in offer", subscriptionOfferPrice.getData().getId(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error is generated for adding a price in past dates",
            subscriptionOfferPrice.getErrors(), equalTo(null), assertionErrorList);

        // verifying expired prices are not returned in get subscription plan
        // api response
        apiResponse = resource.subscriptionPlan().getById(subscriptionPlanId, null);

        AssertCollector.assertThat("Get Subscription Plan Api is returning price list for expired price",
            subscriptionOffer.getIncluded().getPrices().size(), equalTo(0), assertionErrorList);

        // verifying expired prices are not returned in get offerings api
        // response
        params = new HashMap<>();
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        final Offerings offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Get Offerings Api is returning expired Prices",
            offerings.getErrors().get(0).getStatus(), equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass SubscriptionPlan Id and boolean flag to identify for packagingType if its IC or None
     *
     */
    @DataProvider(name = "getSubscriptionPlanIdForPackagingType")
    public Object[][] getSubscriptionPlanId() {
        return new Object[][] {
                { subscriptionPlanExtKeyWithIc, subscriptionPlanIdWithIc, PackagingType.INDUSTRY_COLLECTION },
                { subscriptionPlanExtKeyWithVg, subscriptionPlanIdWithVg, PackagingType.VERTICAL_GROUPING },
                { subscriptionPlanExtKeyWithoutIc, subscriptionPlanIdWithoutIc, PackagingType.NONE } };
    }
}
