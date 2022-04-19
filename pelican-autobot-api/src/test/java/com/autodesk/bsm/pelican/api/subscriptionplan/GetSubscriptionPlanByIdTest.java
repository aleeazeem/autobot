package com.autodesk.bsm.pelican.api.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlanClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
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
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Get A Subscription Plan By Id api tests
 *
 * @author jains
 *
 */
public class GetSubscriptionPlanByIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private Object apiResponse;
    private HttpError httpError;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionPlanClient subscriptionPlanResource;
    private String subscriptionPlanIdWithIc;
    private String subscriptionPlanIdWithVg;
    private String subscriptionPlanIdWithoutIc;

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
        subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());

        // Create A Subscription Plan with industrial collection
        String subscriptionPlanExtKey = PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String productLineNameAndExternalKey =
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.INDUSTRY_COLLECTION,
            true);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithIc = subscriptionPlanDetailPage.getId();

        // Create A Subscription Plan with Vertical grouping
        subscriptionPlanExtKey = PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.VERTICAL_GROUPING,
            true);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithVg = subscriptionPlanDetailPage.getId();

        // Create A Subscription Plan with None
        subscriptionPlanExtKey = PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.NONE, true);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanIdWithoutIc = subscriptionPlanDetailPage.getId();
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
     * Test to verify subscription plan with plan id without offer id. Includes bic/meta/active/cancelled plans.
     *
     * @param offeringExternalKey
     * @param offeringType
     * @param offeringStatus
     * @param supportLevel
     * @param usageType
     * @param offerExternalKeyList
     * @param billingFrequency
     * @param billingFrequencyCount
     * @param offerStatus
     */
    @Test(dataProvider = "getPlanData")
    public void testSubscriptionPlanWithOutOfferId(final String offeringExternalKey, final OfferingType offeringType,
        final Status offeringStatus, final SupportLevel supportLevel, final UsageType usageType,
        final List<String> offerExternalKeyList, final BillingFrequency billingFrequency,
        final int billingFrequencyCount, final Status offerStatus) {
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final List<String> offerIdList = new ArrayList<>();
        final Offerings offerings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            offeringType, Status.ACTIVE, supportLevel, usageType, resource, offeringExternalKey, null);

        // Add offer to plan
        final String subscriptionPlanId = offerings.getOffering().getId();
        for (final String anOfferExternalKeyList : offerExternalKeyList) {
            final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
                subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(anOfferExternalKeyList, billingFrequency,
                    billingFrequencyCount, Status.ACTIVE),
                subscriptionPlanId);
            final String offerId = subscriptionOffer.getData().getId();
            offerIdList.add(offerId);
        }
        // Add one time entitlement to plan
        final String entitlementId =
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(subscriptionPlanId, null, null, true);

        // cancel plan if status is cancelled
        if (offeringStatus == Status.CANCELED) {
            final HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("subscriptionPlanId", subscriptionPlanId);
            requestBody.put("status", Status.CANCELED.name());
            subscriptionPlanResource.update(requestBody);
        }

        apiResponse = resource.subscriptionPlan().getById(subscriptionPlanId, null);

        if (apiResponse instanceof HttpError) { // if get subscription plans api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to SubscriptionPlan type
            subscriptionPlan = (SubscriptionPlan) apiResponse;

            commonAssertionsForSubscriptionPlan(subscriptionPlan, subscriptionPlanId, offeringExternalKey, supportLevel,
                offeringStatus, entitlementId);

            final List<com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionOffer> subscriptionOfferList =
                subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers();
            // assertions on offer
            AssertCollector.assertThat("Offer list size is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.size(), equalTo(2), assertionErrorList);

            for (int i = 0; i < subscriptionOfferList.size(); i++) {
                AssertCollector.assertThat("Offer id " + i + " is not correct for plan id " + subscriptionPlanId,
                    subscriptionOfferList.get(i).getId(), equalTo(offerIdList.get(i)), assertionErrorList);

                AssertCollector.assertThat(
                    "Offer External key " + i + 1 + " is not correct for plan id " + subscriptionPlanId,
                    subscriptionOfferList.get(i).getExternalKey(), equalTo(offerExternalKeyList.get(i)),
                    assertionErrorList);

                AssertCollector.assertThat(
                    "Subscription plan id is not correct under offers for offer " + i + 1 + " for plan id "
                        + subscriptionPlanId,
                    subscriptionOfferList.get(i).getPlanId(), equalTo(subscriptionPlanId), assertionErrorList);
                AssertCollector.assertThat(
                    "Subscription plan external is not correct under offers for offer " + i + 1 + " for plan id "
                        + subscriptionPlanId,
                    subscriptionOfferList.get(i).getPlanExternalKey(), equalTo(offeringExternalKey),
                    assertionErrorList);

                AssertCollector.assertThat("Offer status is not correct for plan id " + subscriptionPlanId,
                    subscriptionOfferList.get(i).getStatus(), equalTo(offerStatus.toString()), assertionErrorList);

                AssertCollector.assertThat("Incorrect offer app family id for plan id" + subscriptionPlanId,
                    subscriptionOfferList.get(i).getApplFamilyId(), equalTo(getEnvironmentVariables().getAppFamilyId()),
                    assertionErrorList);

                AssertCollector.assertThat("OfferingType didnt match", subscriptionPlan.getOfferingType(),
                    equalTo(offeringType.toString()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Description : This method gets subscription plans with id with Data Provider we get both IC and Non IC.
     */
    @Test(dataProvider = "getSubscriptionPlanIdForPackagingType")
    public void getSubscriptionPlanByIdForPackagingType(final String subscriptionPlanId,
        final PackagingType packagingType) {

        apiResponse = resource.subscriptionPlan().getById(subscriptionPlanId, null);
        if (apiResponse instanceof HttpError) { // if get subscription plans api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to SubscriptionPlan type
            subscriptionPlan = (SubscriptionPlan) apiResponse;
            if (packagingType.getDisplayName().equals(PackagingType.IC.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, excepted IC", subscriptionPlan.getPackagingType(),
                    equalTo(PackagingType.IC), assertionErrorList);
            } else if (packagingType.getDisplayName().equals(PackagingType.VG.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, excepted VG", subscriptionPlan.getPackagingType(),
                    equalTo(PackagingType.VG), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid PackagingType, excepted NONE", subscriptionPlan.getPackagingType(),
                    nullValue(), assertionErrorList);
            }
            AssertCollector.assertThat("Unable to find subscription plans", subscriptionPlan, is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription plan id", subscriptionPlan.getId(),
                equalTo(subscriptionPlanId), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription plan status", subscriptionPlan.getStatus(),
                equalTo(Status.ACTIVE.toString()), assertionErrorList);
            AssertCollector.assertThat("OfferingType didnt match", subscriptionPlan.getOfferingType(),
                equalTo(OfferingType.BIC_SUBSCRIPTION.toString()), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify subscription plan with plan id and offer id. Includes bic/meta/active/cancelled plans.
     *
     * @param offeringExternalKey
     * @param offeringType
     * @param offeringStatus
     * @param supportLevel
     * @param usageType
     * @param offerExternalKeyList
     * @param billingFrequency
     * @param billingFrequencyCount
     * @param offerStatus
     */
    @Test(dataProvider = "getPlanData")
    public void testSubscriptionPlanWithOfferId(final String offeringExternalKey, final OfferingType offeringType,
        final Status offeringStatus, final SupportLevel supportLevel, final UsageType usageType,
        final List<String> offerExternalKeyList, final BillingFrequency billingFrequency,
        final int billingFrequencyCount, final Status offerStatus) {
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final List<String> offerIdList = new ArrayList<>();

        final Offerings offerings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            offeringType, Status.ACTIVE, supportLevel, usageType, resource, offeringExternalKey, null);

        // add offer to plan
        final String subscriptionPlanId = offerings.getOffering().getId();
        for (final String anOfferExternalKeyList : offerExternalKeyList) {
            final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
                subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(anOfferExternalKeyList, billingFrequency,
                    billingFrequencyCount, Status.ACTIVE),
                subscriptionPlanId);
            final String offerId = subscriptionOffer.getData().getId();
            offerIdList.add(offerId);
        }
        // Add one time entitlement to plan
        final String entitlementId =
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(subscriptionPlanId, null, null, true);

        // cancel plan if status is cancelled
        if (offeringStatus == Status.CANCELED) {
            final HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("subscriptionPlanId", subscriptionPlanId);
            requestBody.put("status", Status.CANCELED.name());
            subscriptionPlanResource.update(requestBody);
        }

        apiResponse = resource.subscriptionPlan().getById(subscriptionPlanId, offerIdList.get(1));

        if (apiResponse instanceof HttpError) { // if get subscription plans api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to SubscriptionPlan type
            subscriptionPlan = (SubscriptionPlan) apiResponse;
            commonAssertionsForSubscriptionPlan(subscriptionPlan, subscriptionPlanId, offeringExternalKey, supportLevel,
                offeringStatus, entitlementId);

            final List<com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionOffer> subscriptionOfferList =
                subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers();
            // assertions on offer
            AssertCollector.assertThat("Offer list size is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.size(), equalTo(1), assertionErrorList);

            AssertCollector.assertThat("Offer id is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.get(0).getId(), equalTo(offerIdList.get(1)), assertionErrorList);

            AssertCollector.assertThat("Offer External key is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.get(0).getExternalKey(), equalTo(offerExternalKeyList.get(1)),
                assertionErrorList);

            AssertCollector.assertThat(
                "Subscription plan id is not correct under offers for plan id " + subscriptionPlanId,
                subscriptionOfferList.get(0).getPlanId(), equalTo(subscriptionPlanId), assertionErrorList);
            AssertCollector.assertThat(
                "Subscription plan external is not correct under offers for plan id " + subscriptionPlanId,
                subscriptionOfferList.get(0).getPlanExternalKey(), equalTo(offeringExternalKey), assertionErrorList);

            AssertCollector.assertThat("Offer status is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.get(0).getStatus(), equalTo(offerStatus.toString()), assertionErrorList);

            AssertCollector.assertThat("Incorrect offer app family id for plan id" + subscriptionPlanId,
                subscriptionOfferList.get(0).getApplFamilyId(), equalTo(getEnvironmentVariables().getAppFamilyId()),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test verifies that if subscription plan is called with an invalid numeric offer id then no subscription
     * offer will be returned.
     *
     * @param offeringExternalKey
     * @param offeringType
     * @param offeringStatus
     * @param supportLevel
     * @param usageType
     * @param offerExternalKeyList
     * @param billingFrequency
     * @param billingFrequencyCount
     * @param offerStatus
     */
    @Test(dataProvider = "getPlanData")
    public void testSubscriptionPlanWithInvalidNumericOfferId(final String offeringExternalKey,
        final OfferingType offeringType, final Status offeringStatus, final SupportLevel supportLevel,
        final UsageType usageType, final List<String> offerExternalKeyList, final BillingFrequency billingFrequency,
        final int billingFrequencyCount, final Status offerStatus) {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final List<String> offerIdList = new ArrayList<>();

        final Offerings offerings = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            offeringType, Status.ACTIVE, supportLevel, usageType, resource, offeringExternalKey, null);

        final String subscriptionPlanId = offerings.getOffering().getId();

        // Add one offer to plan
        final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(offerExternalKeyList.get(0), billingFrequency,
                billingFrequencyCount, Status.ACTIVE),
            subscriptionPlanId);
        final String offerId = subscriptionOffer.getData().getId();
        offerIdList.add(offerId);

        // Add one time entitlement to plan
        final String entitlementId =
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(subscriptionPlanId, null, null, true);

        // cancel plan if status is cancelled
        if (offeringStatus == Status.CANCELED) {
            final HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("subscriptionPlanId", subscriptionPlanId);
            requestBody.put("status", Status.CANCELED.name());
            subscriptionPlanResource.update(requestBody);
        }

        // get subscription plan with invalid offer id
        apiResponse = resource.subscriptionPlan().getById(subscriptionPlanId, "111111111");

        if (apiResponse instanceof HttpError) { // if get subscription plans api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(SubscriptionPlans.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to SubscriptionPlan type
            subscriptionPlan = (SubscriptionPlan) apiResponse;
            commonAssertionsForSubscriptionPlan(subscriptionPlan, subscriptionPlanId, offeringExternalKey, supportLevel,
                offeringStatus, entitlementId);

            final List<com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionOffer> subscriptionOfferList =
                subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers();
            // assertions on offer
            AssertCollector.assertThat("Offer list size is not correct for plan id " + subscriptionPlanId,
                subscriptionOfferList.size(), equalTo(0), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    @DataProvider(name = "getPlanData")
    public Object[][] getSubscriptionPlanData() {
        return new Object[][] { /*
                                 * (String offeringExternalKey, OfferingType offeringType, Status offeringStatus,
                                 * SupportLevel supportLevel, UsageType usageType, String offerExternalKey,
                                 * BillingFrequency billingFrequency, int billingFrequencyCount, Status offerStatus)
                                 */ { PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM,
                Lists.newArrayList(PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                    PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8)),
                BillingFrequency.MONTH, 2, Status.ACTIVE },

                { PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                        OfferingType.BIC_SUBSCRIPTION, Status.CANCELED, SupportLevel.ADVANCED, UsageType.TRL,
                        Lists.newArrayList(
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8)),
                        BillingFrequency.QUARTER, 2, Status.CANCELED },

                { PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                        OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM,
                        Lists.newArrayList(
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8)),
                        BillingFrequency.MONTH, 2, Status.ACTIVE },

                { PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                        OfferingType.BIC_SUBSCRIPTION, Status.CANCELED, SupportLevel.ADVANCED, UsageType.COM,
                        Lists.newArrayList(
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8),
                            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(8)),
                        BillingFrequency.YEAR, 2, Status.CANCELED },

        };
    }

    private void commonAssertionsForSubscriptionPlan(SubscriptionPlan subscriptionPlan, final String subscriptionPlanId,
        final String offeringExternalKey, final SupportLevel supportLevel, final Status offeringStatus,
        final String entitlementId) {
        subscriptionPlan = (SubscriptionPlan) apiResponse;
        // assertions on offering
        AssertCollector.assertThat("Incorrect subscription plan id", subscriptionPlan.getId(),
            equalTo(subscriptionPlanId), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan name for plan id " + subscriptionPlanId,
            subscriptionPlan.getName(), equalTo(offeringExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan external key for plan id " + subscriptionPlanId,
            subscriptionPlan.getExternalKey(), equalTo(offeringExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan app family id for plan id" + subscriptionPlanId,
            subscriptionPlan.getApplicationFamilyId(), equalTo(getEnvironmentVariables().getAppFamilyId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan support level for plan id" + subscriptionPlanId,
            subscriptionPlan.getSupportLevel(), equalTo(supportLevel.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan status for plan id " + subscriptionPlanId,
            subscriptionPlan.getStatus(), equalTo(offeringStatus.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line code for plan id " + subscriptionPlanId,
            subscriptionPlan.getProductLine().getCode(), equalTo(getProductLineExternalKeyMaya()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line name for plan id " + subscriptionPlanId,
            subscriptionPlan.getProductLine().getName(), equalTo(getProductLineExternalKeyMaya()), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of entitlements for plan id " + subscriptionPlanId,
            subscriptionPlan.getOneTimeEntitlements().getEntitlements().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect entitlement id for plan id " + subscriptionPlanId,
            subscriptionPlan.getOneTimeEntitlements().getEntitlements().get(0).getId(), equalTo(entitlementId),
            assertionErrorList);
    }

    /**
     * DataProvider to pass SubscriptionPlan Id and boolean flag to identify for packagingType if its IC or None
     *
     */
    @DataProvider(name = "getSubscriptionPlanIdForPackagingType")
    public Object[][] getSubscriptionPlanId() {
        return new Object[][] { { subscriptionPlanIdWithIc, PackagingType.IC },
                { subscriptionPlanIdWithVg, PackagingType.VG }, { subscriptionPlanIdWithoutIc, PackagingType.NONE } };
    }
}
