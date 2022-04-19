package com.autodesk.bsm.pelican.api.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlanClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementData;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferData;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test : Get Offering By Id
 *
 * @author Shweta Hegde
 */

public class GetOfferingByIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private HttpError httpError;
    private Object apiResponse;
    private Offerings bicOffering;
    private Offerings bicOfferingWithoutOffers;
    private Offerings bicOfferingWithOfferNoPrice;
    private Offerings basicOffering;
    private Offerings basicOfferingWithoutPrices;
    private Offerings offerings;
    private SubscriptionOffer subscriptionOffer;
    private String bicOfferingId;
    private String metaOfferingId;
    private String basicOfferingId;
    private String bicOfferingIdWithoutOffers;
    private String bicOfferingIdWithOfferNoPrice;
    private String basicOfferingIdWithoutPrices;
    private SubscriptionPlanClient subscriptionPlanResource;
    private HashMap<String, String> requestBody;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Create Subscription Plans
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.NEW, SupportLevel.BASIC, UsageType.COM);
        bicOfferingId = bicOffering.getOfferings().get(0).getId();

        bicOfferingWithoutOffers = subscriptionPlanApiUtils.addSubscriptionPlan(resource, OfferingType.BIC_SUBSCRIPTION,
            Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOfferingIdWithoutOffers = bicOfferingWithoutOffers.getOfferings().get(0).getId();

        bicOfferingWithOfferNoPrice = subscriptionPlanApiUtils.addSubscriptionPlan(resource,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.BASIC, UsageType.COM);
        bicOfferingIdWithOfferNoPrice = bicOfferingWithOfferNoPrice.getOfferings().get(0).getId();
        subscriptionOffer = subscriptionPlanApiUtils.addOffer(bicOfferingIdWithOfferNoPrice);
        bicOfferingWithOfferNoPrice = resource.offerings().getOfferingById(bicOfferingIdWithOfferNoPrice,
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);

        final Offerings metaOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.NEW, SupportLevel.ADVANCED, UsageType.COM);
        metaOfferingId = metaOffering.getOfferings().get(0).getId();
        // create Basic Offerings
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.NEW, UsageType.COM, null);
        basicOfferingId = basicOffering.getOfferings().get(0).getId();
        basicOfferingWithoutPrices = basicOfferingApiUtils.addBasicOffering(resource, OfferingType.PERPETUAL,
            MediaType.DVD, Status.NEW, UsageType.COM, null, null);
        basicOfferingIdWithoutPrices = basicOfferingWithoutPrices.getOfferings().get(0).getId();
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
     * This method tests Get Offering By Id for a Subscription Plan in NEW state, No include parameters are sent
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and Prices
     * should be null
     */
    @Test
    public void testGetOfferingByIdForNewSubscriptionPlan() {

        apiResponse = resource.offerings().getOfferingById(bicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Entitlements should not be returned",
                offerings.getOfferings().get(0).getOneTimeEntitlements().size(), equalTo(0), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.NEW);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, No include parameters are sent
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and Prices
     * should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForNewSubscriptionPlan" })
    public void testGetOfferingByIdForActiveSubscriptionPlan() {
        // Subscription plan created are in "New" State. It is set to "Active"
        // for the testing purpose
        // Subscription plan id and status passed to ACTIVATE subscription plan
        requestBody = new HashMap<>();
        requestBody.put("subscriptionPlanId", bicOfferingId);
        requestBody.put("status", Status.ACTIVE.toString());
        subscriptionPlanResource.update(requestBody);
        apiResponse = resource.offerings().getOfferingById(bicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Offering of plan with offer but no price, getOfferingById should return the offer
     */
    @Test
    public void testGetOfferingByIdWithOfferNoPrice() {
        apiResponse = resource.offerings().getOfferingById(bicOfferingIdWithOfferNoPrice,
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan size in offering should 1",
                offerings.getIncluded().getBillingPlans().size(), equalTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan ID didnt match",
                offerings.getIncluded().getBillingPlans().get(0).getId(), equalTo(subscriptionOffer.getData().getId()),
                assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOfferingWithOfferNoPrice, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, OFFERS is sent in include parameter
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers should not be
     * null and Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveSubscriptionPlan" })
    public void testGetOfferingByIdWithOffersForSubscriptionPlan() {

        apiResponse = resource.offerings().getOfferingById(bicOfferingId, PelicanConstants.INCLUDE_OFFERS_PARAMS);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing Plan Id",
                offerings.getIncluded().getBillingPlans().get(0).getId(),
                equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect offer state",
                offerings.getIncluded().getBillingPlans().get(0).getStatus(),
                equalToIgnoringCase(Status.NEW.toString()), assertionErrorList);
            AssertCollector.assertThat("Prices should be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices(), is(nullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, PRICES is sent in include parameter
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and Prices
     * should not be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveSubscriptionPlan" })
    public void testGetOfferingByIdWithPricesForSubscriptionPlan() {

        apiResponse = resource.offerings().getOfferingById(bicOfferingId, PelicanConstants.INCLUDE_PRICES_PARAMS);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect price id", offerings.getIncluded().getPrices().get(0).getId(),
                equalTo(bicOffering.getIncluded().getPrices().get(0).getId()), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, OFFERS & PRICES is sent in include
     * parameter Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and
     * Prices should not be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveSubscriptionPlan" })
    public void testGetOfferingByIdWithOffersAndPricesForSubscriptionPlan() {
        // add addtional details to an offering
        final String shortDescription = "short description of offering";
        final String longDescription = "long description of offering";
        final String smallImageUrl = "small pelican url";
        final String buttonDisplayName = "pelican";
        addAddtionalDetailsToAnOffering(bicOfferingId, shortDescription, longDescription, smallImageUrl,
            buttonDisplayName);

        apiResponse =
            resource.offerings().getOfferingById(bicOfferingId, PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing Plan Id",
                offerings.getIncluded().getBillingPlans().get(0).getId(),
                equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect offer state",
                offerings.getIncluded().getBillingPlans().get(0).getStatus(),
                equalToIgnoringCase(Status.NEW.toString()), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect cutom date under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getCustomDate(), equalTo("false"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect value of day under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getDay(), equalTo("0"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect value of month under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getMonth(), equalTo("0"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect small description",
                offerings.getOfferings().get(0).getShortDescription(), equalTo(shortDescription), assertionErrorList);
            AssertCollector.assertThat("Incorrect long desription",
                offerings.getOfferings().get(0).getLongDescription(), equalTo(longDescription), assertionErrorList);
            AssertCollector.assertThat("Incorrect small image url", offerings.getOfferings().get(0).getSmallImageURL(),
                equalTo(smallImageUrl), assertionErrorList);
            AssertCollector.assertThat("Incorrect button display name",
                offerings.getOfferings().get(0).getButtonDisplayName(), equalTo(buttonDisplayName), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.ACTIVE);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    private void addAddtionalDetailsToAnOffering(final String offeringId, final String shortDescription,
        final String longDescription, final String smallImageUrl, final String buttonDisplayName) {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(offeringId);
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        editSubscriptionPlanPage.editAdditionalData(shortDescription, longDescription, smallImageUrl, null, null,
            buttonDisplayName);
        editSubscriptionPlanPage.clickOnSave(false);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in CANCELED state, No parameter is sent in include
     * parameter Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and
     * Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdWithOffersForSubscriptionPlan",
            "testGetOfferingByIdWithPricesForSubscriptionPlan",
            "testGetOfferingByIdWithOffersAndPricesForSubscriptionPlan" })
    public void testGetOfferingByIdForCanceledSubscriptionPlan() {
        // Subscription plan created are in "New" State. It is "CANCEL"ed for
        // the testing purpose
        // Subscription plan id and status passed to cancel subscription plan
        requestBody = new HashMap<>();
        requestBody.put("subscriptionPlanId", bicOfferingId);
        requestBody.put("status", "CANCELED");
        subscriptionPlanResource.update(requestBody);
        apiResponse = resource.offerings().getOfferingById(bicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.CANCELED);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in CANCELED state with Canceled Offers, Offers is
     * sent in include parameter Validation is covered for Subscription Plan Id, External Key, Offering Type, Product
     * Line & Offers should not be null and Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForCanceledSubscriptionPlan" })
    public void testGetOfferingByIdWithOffersForSubscriptionPlanWithCanceledOffers() {

        final String offerId =
            bicOffering.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().get(0).getId();
        // update the offer status to "Canceled" in database
        DbUtils.updateTableInDb("SUBSCRIPTION_OFFER", "STATUS", "3", "ID", offerId, getEnvironmentVariables());
        apiResponse = resource.offerings().getOfferingById(bicOfferingId, "offers");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing Plan Id",
                offerings.getIncluded().getBillingPlans().get(0).getId(),
                equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect offer state",
                offerings.getIncluded().getBillingPlans().get(0).getStatus(),
                equalToIgnoringCase(Status.CANCELED.toString()), assertionErrorList);
            AssertCollector.assertThat("Prices should be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices(), is(nullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.CANCELED);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan with "Expired" prices PRICES is sent in include
     * parameter Validation is covered for Subscription Plan Id, External Key, OfferingType, Product Line & Offers and
     * Prices should not be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForCanceledSubscriptionPlan" })
    public void testGetOfferingByIdWithPricesForSubscriptionPlanWithExpiredPrices() {

        final String priceId =
            bicOffering.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().get(0).getId();
        // update the price to "Expired" in database, set to past dates
        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "START_DATE", "'2000-01-01 00:00:00.000000'", "ID", priceId,
            getEnvironmentVariables());
        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "END_DATE", "'2015-12-31 23:59:59.999000'", "ID", priceId,
            getEnvironmentVariables());
        apiResponse = resource.offerings().getOfferingById(bicOfferingId, "prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect price id", offerings.getIncluded().getPrices().get(0).getId(),
                equalTo(bicOffering.getIncluded().getPrices().get(0).getId()), assertionErrorList);
            commonAssertions(offerings, bicOffering, Status.CANCELED);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, which does not have any offers
     * OFFERS is sent in include parameter Validation is covered for Subscription Plan Id, External Key, Offering Type,
     * Product Line & Offers and Prices should be null
     */
    @Test
    public void testGetOfferingByIdWithOffersForSubscriptionPlanWithoutOffers() {
        apiResponse = resource.offerings().getOfferingById(bicOfferingIdWithoutOffers, "offers");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            commonAssertions(offerings, bicOfferingWithoutOffers, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Subscription Plan in ACTIVE state, which does not have any prices
     * PRICES is sent in include parameter Validation is covered for Subscription Plan Id, External Key, Offering Type,
     * Product Line & Offers should not be null and Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdWithOffersForSubscriptionPlanWithoutOffers" })
    public void testGetOfferingByIdWithPricesForSubscriptionPlanWithoutPrices() {
        subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            addSubscriptionOfferToPlan(BillingFrequency.MONTH, Status.NEW), bicOfferingIdWithoutOffers);
        apiResponse = resource.offerings().getOfferingById(bicOfferingIdWithoutOffers, "prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plans should not be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans().getLinkage().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should not be null",
                offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect offer state",
                offerings.getIncluded().getBillingPlans().get(0).getStatus(),
                equalToIgnoringCase(Status.NEW.toString()), assertionErrorList);
            AssertCollector.assertThat("Prices should be null in links",
                offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices(), is(nullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
        }
        commonAssertions(offerings, bicOfferingWithoutOffers, Status.ACTIVE);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in NEW state, No Parameter is sent in Include
     * Parameters Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and
     * Prices should be null
     */
    @Test
    public void testGetOfferingByIdForNewBasicOffering() {

        apiResponse = resource.offerings().getOfferingById(basicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.NEW);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in ACTIVE state, No Parameter is sent in Include
     * Parameters Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and
     * Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForNewBasicOffering" })
    public void testGetOfferingByIdForActiveBasicOffering() {

        // Basic offering created are in "New" State. It is "ACTIVE"ted for the
        // testing purpose
        DbUtils.updateTableInDb("OFFERING", "STATUS", "1", "ID", basicOfferingId, getEnvironmentVariables());
        apiResponse = resource.offerings().getOfferingById(basicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in ACTIVE state, OFFERS is sent in include parameter
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveBasicOffering" })
    public void testGetOfferingByIdWithOffersForBasicOffering() {

        apiResponse = resource.offerings().getOfferingById(basicOfferingId, "offers");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Prices should be null in links",
                offerings.getOfferings().get(0).getLinks().getPrices(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in ACTIVE state, PRICES is sent in include parameter
     * Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Prices should not be
     * null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveBasicOffering" })
    public void testGetOfferingByIdWithPricesForBasicOffering() {

        apiResponse = resource.offerings().getOfferingById(basicOfferingId, "prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getOfferings().get(0).getLinks().getPrices().getLinkage().size(), greaterThanOrEqualTo(1),
                assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect price id", offerings.getIncluded().getPrices().get(0).getId(),
                equalTo(basicOffering.getIncluded().getPrices().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in ACTIVE state, OFFERS & PRICES is sent in include
     * parameter Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Prices
     * should not be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForActiveBasicOffering" })
    public void testGetOfferingByIdWithOffersAndPricesForBasicOffering() {

        apiResponse = resource.offerings().getOfferingById(basicOfferingId, "offers,prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getOfferings().get(0).getLinks().getPrices().getLinkage().size(), greaterThanOrEqualTo(1),
                assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect price id", offerings.getIncluded().getPrices().get(0).getId(),
                equalTo(basicOffering.getIncluded().getPrices().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.ACTIVE);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in CANCELED state, No Parameter is sent in Include
     * Parameters Validation is covered for Subscription Plan Id, External Key, Offering Type, Product Line & Offers and
     * Prices should be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdWithOffersForBasicOffering",
            "testGetOfferingByIdWithPricesForBasicOffering", "testGetOfferingByIdWithOffersAndPricesForBasicOffering" })
    public void testGetOfferingByIdForCanceledBasicOffering() {
        // Basic offering created are in "New" State. It is "CANCEL"ed for the
        // testing purpose
        DbUtils.updateTableInDb("OFFERING", "STATUS", "3", "ID", basicOfferingId, getEnvironmentVariables());
        apiResponse = resource.offerings().getOfferingById(basicOfferingId, null);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null in links",
                offerings.getOfferings().get(0).getLinks().getBillingPlans(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.CANCELED);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering with "Expired" prices PRICES is sent in include
     * parameter Validation is covered for Subscription Plan Id, External Key, OfferingType, Product Line & Prices
     * should not be null
     */
    @Test(dependsOnMethods = { "testGetOfferingByIdForCanceledBasicOffering" })
    public void testGetOfferingByIdWithPricesForBasicOfferingWithExpiredPrices() {

        final String priceId = basicOffering.getIncluded().getPrices().get(0).getId();
        // update the price to "Expired" in database, set to past dates
        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "START_DATE", "'2010-01-01 00:00:00.000000'", "ID", priceId,
            getEnvironmentVariables());
        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "END_DATE", "'2016-01-31 00:00:00.000000'", "ID", priceId,
            getEnvironmentVariables());
        apiResponse = resource.offerings().getOfferingById(basicOfferingId, "prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Billing Plan should be null", offerings.getIncluded().getBillingPlans().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Prices should not be null in links",
                offerings.getOfferings().get(0).getLinks().getPrices().getLinkage().size(), greaterThanOrEqualTo(1),
                assertionErrorList);
            AssertCollector.assertThat("Prices should not be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(1), assertionErrorList);
            AssertCollector.assertThat("Incorrect price id", offerings.getIncluded().getPrices().get(0).getId(),
                equalTo(basicOffering.getIncluded().getPrices().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOffering, Status.CANCELED);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for a Basic Offering in NEW state, which does not have any prices PRICES is
     * sent in include parameter Validation is covered for Subscription Plan Id, External Key, Offering Type, Product
     * Line & Prices should be null
     */
    @Test
    public void testGetOfferingByIdWithPricesForBasicOfferingWithoutPrices() {
        apiResponse = resource.offerings().getOfferingById(basicOfferingIdWithoutPrices, "prices");
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Offerings.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to Offerings type
            offerings = (Offerings) apiResponse;
            AssertCollector.assertThat("Prices should be null in links",
                offerings.getOfferings().get(0).getLinks().getPrices(), is(nullValue()), assertionErrorList);
            AssertCollector.assertThat("Prices should be null", offerings.getIncluded().getPrices().size(),
                greaterThanOrEqualTo(0), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
                is(UsageType.COM), assertionErrorList);
            commonAssertions(offerings, basicOfferingWithoutPrices, Status.NEW);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for an INVALID ID Error Message and status is validated
     */
    @Test
    public void testErrorWithInvalidOfferingIdForGetOfferingById() {

        final String invalidOfferingId = "987654321";
        apiResponse = resource.offerings().getOfferingById(invalidOfferingId, null);
        offerings = (Offerings) apiResponse;
        AssertCollector.assertThat("Invalid error status", offerings.getErrors().get(0).getStatus(), is(400),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error message", offerings.getErrors().get(0).getDetail(),
            equalTo("Invalid offering Id : 987654321"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for NON NUMERIC ID Error Message and status is validated
     */
    @Test
    public void testErrorWithNonNumericOfferingIdForGetOfferingById() {

        final String nonNumericOfferingId = "abcdefghijkl";
        apiResponse = resource.offerings().getOfferingById(nonNumericOfferingId, null);
        offerings = (Offerings) apiResponse;
        AssertCollector.assertThat("Invalid error status", offerings.getErrors().get(0).getStatus(), is(400),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error message", offerings.getErrors().get(0).getDetail(),
            equalTo("Offering Id must be a numeric."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for INVALID PARAMS for Subscription Plan Error Message and status is
     * validated
     */
    @Test
    public void testErrorWithInvalidIncludeParamsForGetOfferingByIdForSubscriptionPlan() {

        apiResponse = resource.offerings().getOfferingById(metaOfferingId, "autodesk");
        offerings = (Offerings) apiResponse;
        AssertCollector.assertThat("Invalid error status", offerings.getErrors().get(0).getStatus(), is(400),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error message", offerings.getErrors().get(0).getDetail(),
            equalTo("Invalid include value : autodesk"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for INVALID PARAMS for Basic Offering Error Message and status is validated
     */
    @Test
    public void testErrorWithInvalidIncludeParamsForGetOfferingByIdForBasicOffering() {

        apiResponse = resource.offerings().getOfferingById(metaOfferingId, "autodesk");
        offerings = (Offerings) apiResponse;
        AssertCollector.assertThat("Invalid error status", offerings.getErrors().get(0).getStatus(), is(400),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error message", offerings.getErrors().get(0).getDetail(),
            equalTo("Invalid include value : autodesk"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get Offering By Id for included as entitlements. Moreover, it tests the prices for all offers
     * and store id, store external key and price list external key which are associated with their offers.
     */
    @Test
    public void testOffersPricesAndEntitlements() {
        final String offeringExternalKey = "Maya_Offering" + RandomStringUtils.randomAlphabetic(6);
        final String monthlyOfferExternalKey = "Maya_Offer_Monthly" + RandomStringUtils.randomAlphabetic(6);
        final String yearlyOfferExternalKey = "Maya_Offer_yearly" + RandomStringUtils.randomAlphabetic(6);
        final String startDateActive = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String endDateActive = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 10);
        final String startDateExpired = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 20);
        final String endDateExpired = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 20);
        final String startDateFuture = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 12);
        final String endDateFuture = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 20);

        final Offerings subscriptionPlan = subscriptionPlanApiUtils.addPlanWithProductLine(
            getProductLineExternalKeyMaya(), OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED,
            UsageType.COM, resource, offeringExternalKey, null);
        final String subscriptionPlanId = subscriptionPlan.getOffering().getId();

        // Add monthly offer to Subscription plan
        final SubscriptionOffer monthlyOffer = subscriptionPlanApiUtils
            .helperToAddSubscriptionOfferToPlan(monthlyOfferExternalKey, BillingFrequency.MONTH, 1, Status.ACTIVE);
        final String offerMonthlyId =
            subscriptionPlanApiUtils.addSubscriptionOffer(resource, monthlyOffer, subscriptionPlanId).getData().getId();

        // add active, expired and future prices to a monthly offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(100, getPricelistExternalKeyUs(),
                startDateActive, endDateActive),
            subscriptionPlanId, offerMonthlyId);
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(80, getPricelistExternalKeyUs(),
                startDateExpired, endDateExpired),
            subscriptionPlanId, offerMonthlyId);
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(120, getPricelistExternalKeyUs(),
                startDateFuture, endDateFuture),
            subscriptionPlanId, offerMonthlyId);

        // Add yearly offer to Subscription plan
        final SubscriptionOffer yearlyOffer = subscriptionPlanApiUtils
            .helperToAddSubscriptionOfferToPlan(yearlyOfferExternalKey, BillingFrequency.YEAR, 1, Status.ACTIVE);
        final String offerYearlyId =
            subscriptionPlanApiUtils.addSubscriptionOffer(resource, yearlyOffer, subscriptionPlanId).getData().getId();
        // add namer and emea prices to a yearly offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(1000, getPricelistExternalKeyUs(),
                startDateActive, endDateActive),
            subscriptionPlanId, offerYearlyId);
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(800, getPricelistExternalKeyUk(),
                startDateActive, endDateActive),
            subscriptionPlanId, offerYearlyId);

        final List<Item> itemsList = addItemsInOffering(subscriptionPlanId, 2);

        final Offerings offerings =
            resource.offerings().getOfferingById(subscriptionPlanId, PelicanConstants.INCLUDE_ALL_PARAMS_FOR_OFFERING);

        AssertCollector.assertThat("Offering id is not correct in response", offerings.getOfferings().get(0).getId(),
            equalTo(subscriptionPlanId), assertionErrorList);
        AssertCollector.assertThat("Product line is not correct for offering: " + subscriptionPlanId,
            offerings.getOfferings().get(0).getProductLineName(), equalTo(getProductLineExternalKeyMaya()),
            assertionErrorList);
        AssertCollector.assertThat("Monthly offer id is not correct for subscription plan: " + subscriptionPlanId,
            offerings.getIncluded().getBillingPlans().get(0).getId(), equalTo(offerMonthlyId), assertionErrorList);
        AssertCollector.assertThat("Yearly offer id is not correct for subscription plan: " + subscriptionPlanId,
            offerings.getIncluded().getBillingPlans().get(1).getId(), equalTo(offerYearlyId), assertionErrorList);

        // assertions on start date, end date, store id, store external key and price list
        assertionsForEachPrice(0, offerings, offerMonthlyId, startDateActive, endDateActive, getStoreUs());
        assertionsForEachPrice(1, offerings, offerMonthlyId, startDateExpired, endDateExpired, getStoreUs());
        assertionsForEachPrice(2, offerings, offerMonthlyId, startDateFuture, endDateFuture, getStoreUs());
        assertionsForEachPrice(3, offerings, offerYearlyId, startDateActive, endDateActive, getStoreUs());
        assertionsForEachPrice(4, offerings, offerMonthlyId, startDateActive, endDateActive, getStoreUk());

        // Assertions for entitlements.
        for (final JSubscriptionEntitlementData entitlement : offerings.getOfferings().get(0)
            .getOneTimeEntitlements()) {
            final int index = offerings.getOfferings().get(0).getOneTimeEntitlements().indexOf(entitlement);
            AssertCollector.assertThat(
                "Entitlement id is not found for offering: " + offerings.getOfferings().get(0).getId(),
                entitlement.getId(), equalTo(itemsList.get(index).getId()), assertionErrorList);
            AssertCollector.assertThat(
                "Entitlement name is not found for offering: " + offerings.getOfferings().get(0).getId(),
                entitlement.getName(), equalTo(itemsList.get(index).getName()), assertionErrorList);
            AssertCollector.assertThat(
                "Entitlement external key is not found for offering: " + offerings.getOfferings().get(0).getId(),
                entitlement.getExternalKey(), equalTo(itemsList.get(index).getExternalKey()), assertionErrorList);
            AssertCollector.assertThat(
                "Entitlement type is not found for offering: " + offerings.getOfferings().get(0).getId(),
                entitlement.getEntityType(), equalTo(EntityType.ITEM), assertionErrorList);
            AssertCollector.assertThat(
                "Entitlement licensing model externalKey is not found for offering: "
                    + offerings.getOfferings().get(0).getId(),
                entitlement.getLicensingModelExternalKey(),
                equalTo(PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Common assertions at data level.
     *
     * @param offeringActual
     * @param offeringExprected
     * @param expectedStatus
     */
    private void commonAssertions(final Offerings offeringActual, final Offerings offeringExprected,
        final Status expectedStatus) {
        AssertCollector.assertThat("Unable to find offerings", offeringActual, is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offerings id", offeringActual.getOfferings().get(0).getId(),
            equalTo(offeringExprected.getOfferings().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering type", offeringActual.getOfferings().get(0).getOfferingType(),
            equalTo(offeringExprected.getOfferings().get(0).getOfferingType()), assertionErrorList);
        AssertCollector.assertThat("Incorrect External Key", offeringActual.getOfferings().get(0).getExternalKey(),
            equalTo(offeringExprected.getOfferings().get(0).getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", offeringActual.getOfferings().get(0).getProductLine(),
            equalTo(offeringExprected.getOfferings().get(0).getProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line name",
            offeringActual.getOfferings().get(0).getProductLineName(),
            equalTo(offeringExprected.getOfferings().get(0).getProductLineName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offeringActual.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(expectedStatus.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type", offeringActual.getOfferings().get(0).getUsageType(),
            is(UsageType.COM), assertionErrorList);
    }

    /**
     * Method to create items and add them into offering.
     *
     * @param offeringId
     * @param numberOfItemsInOffering
     * @return list of items
     */
    private List<Item> addItemsInOffering(final String offeringId, final int numberOfItemsInOffering) {
        // add entitlements to an offering.
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final List<Item> itemsList = new ArrayList<>();
        for (int i = 0; i < numberOfItemsInOffering; i++) {
            final Item item = featureApiUtils.addFeature(null, null, null);
            itemsList.add(item);
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(offeringId, item.getId(), null, true);
        }
        return itemsList;
    }

    /**
     * Method to add assertions on prices.
     *
     * @param indexOfPrices
     * @param offerings
     * @param offerId
     * @param startDate
     * @param endDate
     * @param store
     */
    private void assertionsForEachPrice(final int indexOfPrices, final Offerings offerings, final String offerId,
        final String startDate, final String endDate, final JStore store) {
        AssertCollector.assertThat("Active Start date of first price is not correct for offer: " + offerId,
            offerings.getIncluded().getPrices().get(indexOfPrices).getStartDate(),
            equalTo(DateTimeUtils.changeDateFormat(startDate, PelicanConstants.DATE_FORMAT_WITH_SLASH,
                PelicanConstants.DATE_TIME_FORMAT)),
            assertionErrorList);
        AssertCollector.assertThat("Active end date of first price is not correct for offer: " + offerId,
            offerings.getIncluded().getPrices().get(indexOfPrices).getEndDate(), equalTo(DateTimeUtils
                .changeDateFormat(endDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT)),
            assertionErrorList);
        AssertCollector.assertThat("Store id of first price is not correct for offer: " + offerId,
            offerings.getIncluded().getPrices().get(indexOfPrices).getStoreId(), equalTo(store.getId()),
            assertionErrorList);
        AssertCollector.assertThat("Store external key of first price is not correct for offer: " + offerId,
            offerings.getIncluded().getPrices().get(indexOfPrices).getStoreExternalKey(),
            equalTo(store.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Price list external key of first price is not correct for offer: " + offerId,
            offerings.getIncluded().getPrices().get(indexOfPrices).getPriceListExternalKey(),
            equalTo(store.getIncluded().getPriceLists().get(0).getExternalKey()), assertionErrorList);
    }

    /**
     * Helper Method to create subscription offer to a plan
     *
     * @return SubscriptionOffer
     */
    private SubscriptionOffer addSubscriptionOfferToPlan(final BillingFrequency billingFrequency, final Status status) {

        final SubscriptionOffer subscriptionOffer = new SubscriptionOffer();
        final String subscriptionOfferExternalKey = "SQA_Sub_Offer_" + RandomStringUtils.randomAlphabetic(6);
        final SubscriptionOfferData subscriptionOfferData = new SubscriptionOfferData();
        subscriptionOfferData.setExternalKey(subscriptionOfferExternalKey);
        subscriptionOfferData.setName(subscriptionOfferExternalKey);
        subscriptionOfferData.setType("offer");
        subscriptionOfferData.setStatus(status);
        subscriptionOfferData.setBillingFrequency(billingFrequency);
        subscriptionOfferData.setBillingFrequencyCount(1);
        subscriptionOffer.setData(subscriptionOfferData);

        return subscriptionOffer;
    }
}
