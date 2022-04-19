package com.autodesk.bsm.pelican.api.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.GetPriceByPriceId;
import com.autodesk.bsm.pelican.api.pojos.json.GetPriceByPriceId.Included;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This is a test class which will test "get price by price id" api
 *
 * @author Muhammad
 *
 */
public class GetPriceByPriceIdTest extends BaseTestData {

    private PelicanPlatform resource;
    private GetPriceByPriceId getPriceByPriceId;
    private Object apiResponse;
    private HttpError httpError;
    private static final String PRICE = "price";
    private static final String ERROR_DETAIL = "Price not found for Id : ";

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * This method tests the response of api with valid active price id. If active price id is passed as a parameter
     * then included and errors will be null.
     */
    @Test
    public void testGetPriceByActivePriceId() {
        apiResponse = resource.getPriceIdClient().getPriceById(getBicMonthlyUsPriceId());
        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Price.class), assertionErrorList);
        } else {
            getPriceByPriceId = (GetPriceByPriceId) apiResponse;
            AssertCollector.assertThat("Error is found for valid and active price id", getPriceByPriceId.getErrors(),
                equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Included size is not 0 for valid and active price id",
                getPriceByPriceId.getIncluded().size(), equalTo(0), assertionErrorList);
            AssertCollector.assertThat("Type is not correct under data", getPriceByPriceId.getData().getType(),
                equalTo(PRICE), assertionErrorList);
            AssertCollector.assertThat("Id is not correct under data", getPriceByPriceId.getData().getId(),
                equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
            AssertCollector.assertThat("Currency is not correct under data", getPriceByPriceId.getData().getCurrency(),
                equalTo(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
            AssertCollector.assertThat("Amount is not correct under data", getPriceByPriceId.getData().getAmount(),
                equalTo(getBicSubscriptionPlan().getIncluded().getPrices().get(0).getAmount()), assertionErrorList);
            AssertCollector.assertThat("Status is not correct under data", getPriceByPriceId.getData().getStatus(),
                equalTo(Status.ACTIVE.toString()), assertionErrorList);
            AssertCollector.assertThat("Price list id is not correct under data",
                getPriceByPriceId.getData().getPricelistId(),
                equalTo(getStoreUs().getIncluded().getPriceLists().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Price list external key is not correct under data",
                getPriceByPriceId.getData().getPriceListExternalKey(), equalTo(getPricelistExternalKeyUs()),
                assertionErrorList);
            AssertCollector.assertThat("Store id is not correct under data", getPriceByPriceId.getData().getStoreId(),
                equalTo(getStoreUs().getId()), assertionErrorList);
            AssertCollector.assertThat("Store external key is not correct under data",
                getPriceByPriceId.getData().getStoreExternalKey(), equalTo(getStoreUs().getExternalKey()),
                assertionErrorList);

            AssertCollector.assertAll(assertionErrorList);
        }
    }

    /**
     * This method tests the errors scenario for get price by price id api.
     */
    @Test(dataProvider = "invalidPriceId")
    public void testGetPriceByInvalidPriceId(final String invalidPriceId) {
        apiResponse = resource.getPriceIdClient().getPriceById(invalidPriceId);
        getPriceByPriceId = (GetPriceByPriceId) apiResponse;
        AssertCollector.assertThat("Detail message is not correct for invalid id",
            getPriceByPriceId.getErrors().get(0).getDetail(), equalTo(ERROR_DETAIL + invalidPriceId),
            assertionErrorList);
        AssertCollector.assertThat("Code is not correct for invalid id", getPriceByPriceId.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.TFE_ENITY_NOT_FOUND_EXCEPTION), assertionErrorList);
        AssertCollector.assertThat("Status Code is not correct for invalid id",
            getPriceByPriceId.getErrors().get(0).getStatus(), equalTo(404), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the response of get price by price id api if expired price id is provided. In response, expired
     * price id will be returned (as data) along with active price id (as included) which belongs to same price list
     * which expired price id belongs.
     */
    @Test
    public void testGetPriceByInActivePriceId() {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionPlan = subscriptionPlanApiUtils.addPlanWithProductLine(
            getProductLineExternalKeyMaya(), OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED,
            UsageType.COM, resource, RandomStringUtils.randomAlphanumeric(10), null);
        final String subscriptionPlanId = subscriptionPlan.getOffering().getId();

        // Add monthly offer to Subscription plan
        final SubscriptionOffer monthlyOffer = subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(
            RandomStringUtils.randomAlphanumeric(10), BillingFrequency.MONTH, 1, Status.ACTIVE);
        final String offerMonthlyId =
            subscriptionPlanApiUtils.addSubscriptionOffer(resource, monthlyOffer, subscriptionPlanId).getData().getId();

        // Add price with past dates in an offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(100, getPricelistExternalKeyUs(),
                DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 20),
                DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 10)),
            subscriptionPlanId, offerMonthlyId);
        // Add price with active dates
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(200, getPricelistExternalKeyUs(),
                DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 10)),
            subscriptionPlanId, offerMonthlyId);

        final Offerings offerings =
            resource.offerings().getOfferingById(subscriptionPlanId, PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);

        apiResponse = resource.getPriceIdClient().getPriceById(offerings.getIncluded().getPrices().get(0).getId());
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Price.class), assertionErrorList);
        } else {
            getPriceByPriceId = (GetPriceByPriceId) apiResponse;

            // Assertions for active price which will be returned under included.
            final Price activePrice = offerings.getIncluded().getPrices().get(1);
            final Included activePriceUnderIncluded = getPriceByPriceId.getIncluded().get(0);
            AssertCollector.assertThat("Error is found for valid and active price id", getPriceByPriceId.getErrors(),
                equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Type is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getType(), equalTo(PRICE), assertionErrorList);
            AssertCollector.assertThat("Id is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getId(), equalTo(offerings.getIncluded().getPrices().get(1).getId()),
                assertionErrorList);
            AssertCollector.assertThat("Currency is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getCurrency(),
                equalTo(offerings.getIncluded().getPrices().get(1).getCurrency()), assertionErrorList);
            AssertCollector.assertThat("Amount is not correct under included for price: " + activePrice,
                activePriceUnderIncluded.getAmount(), equalTo(offerings.getIncluded().getPrices().get(1).getAmount()),
                assertionErrorList);
            AssertCollector.assertThat("Status is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getStatus(), equalTo(Status.ACTIVE.toString()), assertionErrorList);
            AssertCollector.assertThat("Price list id is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getPriceListId(),
                equalTo(getStoreUs().getIncluded().getPriceLists().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat(
                "Price list external key is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getPriceListExternalKey(), equalTo(getPricelistExternalKeyUs()),
                assertionErrorList);
            AssertCollector.assertThat("Store id is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getStoreId(), equalTo(getStoreUs().getId()), assertionErrorList);
            AssertCollector.assertThat(
                "Store external key is not correct under included for active price: " + activePrice,
                activePriceUnderIncluded.getStoreExternalKey(), equalTo(getStoreUs().getExternalKey()),
                assertionErrorList);

            // Assertions for expired price which will be returned under included.
            final Price expiredPrice = offerings.getIncluded().getPrices().get(1);
            final Price expiredPriceUnderData = getPriceByPriceId.getData();
            AssertCollector.assertThat("Type is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getType(), equalTo(PRICE), assertionErrorList);
            AssertCollector.assertThat("Id is not correct under data for expired price:" + expiredPrice,
                expiredPriceUnderData.getId(), equalTo(offerings.getIncluded().getPrices().get(0).getId()),
                assertionErrorList);
            AssertCollector.assertThat("Currency is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getCurrency(), equalTo(offerings.getIncluded().getPrices().get(0).getCurrency()),
                assertionErrorList);
            AssertCollector.assertThat("Amount is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getAmount(), equalTo(offerings.getIncluded().getPrices().get(0).getAmount()),
                assertionErrorList);
            AssertCollector.assertThat("Status is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
            AssertCollector.assertThat("Price list id is not correct under data for expired price:" + expiredPrice,
                expiredPriceUnderData.getPricelistId(),
                equalTo(getStoreUs().getIncluded().getPriceLists().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat(
                "Price list external key is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getPriceListExternalKey(), equalTo(getPricelistExternalKeyUs()),
                assertionErrorList);
            AssertCollector.assertThat("Store id is not correct under data for expired price: " + expiredPrice,
                expiredPriceUnderData.getStoreId(), equalTo(getStoreUs().getId()), assertionErrorList);
            AssertCollector.assertThat("Store external key is not correct under data for expired price:" + expiredPrice,
                expiredPriceUnderData.getStoreExternalKey(), equalTo(getStoreUs().getExternalKey()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * data provider to pass invalid price id as a parameter
     *
     * @return invalidPriceId
     */
    @DataProvider(name = "invalidPriceId")
    public Object[][] getInvalidPriceId() {
        return new Object[][] { { "-1" }, { "123456788" }, { "0" } };
    }
}
