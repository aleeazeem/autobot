package com.autodesk.bsm.pelican.api.productline;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.ProductLinesClient.Parameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.item.Items;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLines;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This test class is written to test Get ProductLines api test. Following are the parameters which can be passed
 * Country Code Store ID or External Key Samples of Response in attached below { "included": [], "errors": null, "data":
 * [ { "type": "productLine", "id": "1005", "externalKey": "TESTLINE1", "name": "testprodLine" } ] }
 *
 * @author Muhammad
 */
public class FindProductLinesTest extends BaseTestData {

    private PelicanPlatform resource;
    private StoreApiUtils storeApiUtils;
    private static String storeId;
    private static final String PRODUCT_LINE_EXTERNAL_KEY = RandomStringUtils.randomAlphabetic(18);
    private static ProductLine productLine;
    private static ProductLines productLines;
    private Object apiResponse;
    private HashMap<String, String> params;
    private HttpError httpError;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static String externalKeyOfPriceList;
    private static final int TOTAL_SUBSCRIPTIONS_PRODUCTLINES = 3;
    private static final String ERROR_CODE = "990002";
    private static final String ERROR_CODE_FOR_COUTNRY = "420001";
    private static final String ERROR_CODE_FOR_STORE = "400001";
    private static final String ERROR_MESSAGE = "Either Store Id or Store External Key is required.";
    private static final String COUNTRY_CODE_ERROR_MESSAGE = "Country code is required.";
    private static final String STORE_ERROR_MESSAGE = "Store not found.";
    private static final String INVALID_COUNTRY_CODE = "Invalid country code. ";
    private static final String INVALID_PAGINATION_PARAMETERS = "Invalid pagination parameters.";
    private static final Logger LOGGER = LoggerFactory.getLogger(FindProductLinesTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // add a store with price list
        final JStore store = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        externalKeyOfPriceList = store.getIncluded().getPriceLists().get(0).getExternalKey();
        storeId = store.getId();
        LOGGER.info("Id of a Store is: " + storeId);

        // add product line
        LOGGER.info("External Key of Product Line: " + PRODUCT_LINE_EXTERNAL_KEY);
        productLine = subscriptionPlanApiUtils.addProductLine(PRODUCT_LINE_EXTERNAL_KEY);
        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(PRODUCT_LINE_EXTERNAL_KEY,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            externalKeyOfPriceList, 500);
        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(PRODUCT_LINE_EXTERNAL_KEY,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            externalKeyOfPriceList, 500);
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
     * Test case to get Productline which has status False
     */
    @Test
    public void testGetProductLinesWithStatusAsFalse() {
        final String productLineExternalKey = PRODUCT_LINE_EXTERNAL_KEY + "_" + PelicanConstants.FALSE;
        // add product line with status False
        LOGGER.info("External Key of Product Line: " + productLineExternalKey);
        final ProductLine productLine = subscriptionPlanApiUtils.addProductLine(productLineExternalKey, false);
        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            externalKeyOfPriceList, 500);

        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);

        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            // Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to product lines
            productLines = (ProductLines) apiResponse;
            ProductLineData productLineData = new ProductLineData();
            for (int index = 0; index < productLines.getProductLineData().size(); index++) {
                if (productLines.getProductLineData().get(index).getExternalKey()
                    .equalsIgnoreCase(productLineExternalKey)) {
                    productLineData = productLines.getProductLineData().get(index);
                }
            }
            AssertCollector.assertThat("Incorrect Product lines id", productLineData.getId(),
                equalTo(productLine.getData().getId()), assertionErrorList);
            LOGGER.info("" + productLineData.getId() + ":" + productLine.getData().getId());
            AssertCollector.assertThat("Product Line isActive is not False", productLineData.getIsActive(), is(false),
                assertionErrorList);
            LOGGER.info("" + productLineData.getIsActive());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to get Productlines, against product line which was created with status Null
     */
    @Test
    public void testGetProductLinesWithProductLineStatusNullOnCreate() {
        final String productLineExternalKey = PRODUCT_LINE_EXTERNAL_KEY + "_" + "NULL";
        // add product line with Status null
        LOGGER.info("External Key of Product Line: " + productLineExternalKey);
        final ProductLine productLine = subscriptionPlanApiUtils.addProductLine(productLineExternalKey, null);
        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            externalKeyOfPriceList, 500);

        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);

        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            // Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to product lines
            productLines = (ProductLines) apiResponse;
            ProductLineData productLineData = new ProductLineData();
            for (int index = 0; index < productLines.getProductLineData().size(); index++) {
                if (productLines.getProductLineData().get(index).getExternalKey()
                    .equalsIgnoreCase(productLineExternalKey)) {
                    productLineData = productLines.getProductLineData().get(index);
                }
            }
            AssertCollector.assertThat("Incorrect Product lines id", productLineData.getId(),
                equalTo(productLine.getData().getId()), assertionErrorList);
            LOGGER.info("" + productLineData.getId() + ":" + productLine.getData().getId());
            AssertCollector.assertThat("Product Line isActive is not True", productLineData.getIsActive(), is(true),
                assertionErrorList);
            LOGGER.info("" + productLineData.getIsActive());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the response after providing valid store and country code
     */
    @Test
    public void testGetProductLinesByValidCountryCodeAndValidStoreExternalKey() {
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);
        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            // Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to product lines
            productLines = (ProductLines) apiResponse;
            ProductLineData productLineData = new ProductLineData();
            for (int index = 0; index < productLines.getProductLineData().size(); index++) {
                if (productLines.getProductLineData().get(index).getExternalKey()
                    .equalsIgnoreCase(PRODUCT_LINE_EXTERNAL_KEY)) {
                    productLineData = productLines.getProductLineData().get(index);
                }
            }
            AssertCollector.assertThat("Incorrect Product lines id", productLineData.getId(),
                equalTo(productLine.getData().getId()), assertionErrorList);
            LOGGER.info("" + productLineData.getId() + ":" + productLine.getData().getId());
            AssertCollector.assertThat("Product Line isActive is not True", productLineData.getIsActive(), is(true),
                assertionErrorList);
            LOGGER.info("" + productLineData.getIsActive());

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the error in response if store is not provided as a parameter
     */
    @Test
    public void testGetProductLinesApiWithoutIdOrExternalKeyOfStore() {
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;
        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error Message of store is not generated",
            productLines.getErrors().get(0).getDetail(), equalTo(ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the error in response if country code is not provided as a parameter
     */
    @Test
    public void testGetProductLinesApiWithoutCountryCode() {
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;
        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error Message for Country Code is not Generated",
            productLines.getErrors().get(0).getDetail(), equalTo(COUNTRY_CODE_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the error in response if invalid store external Key provided as a parameter
     */
    @Test
    public void testGetProductLinesApiWithInvalidStoreExternalKey() {
        final String invalidStoreExternalKey = RandomStringUtils.randomAlphabetic(12);
        params.put(Parameter.STORE_EXTERNAL_KEY.getName(), invalidStoreExternalKey);
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;
        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE_FOR_STORE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated ", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error for store is not generated", productLines.getErrors().get(0).getDetail(),
            equalTo(STORE_ERROR_MESSAGE + " (" + invalidStoreExternalKey + ")"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the error in response if invalid country code is provided as a parameter
     */
    @Test
    public void testGetProductLinesApiWithInvalidCountryCode() {
        final String invalidCountryCode = RandomStringUtils.randomAlphabetic(4);
        params.put(Parameter.COUNTRY_CODE.getName(), invalidCountryCode);
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;
        AssertCollector.assertThat("Error code is not generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE_FOR_COUTNRY), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error message for country code is not generated",
            productLines.getErrors().get(0).getDetail(), equalTo(INVALID_COUNTRY_CODE + "(" + invalidCountryCode + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies if both externalkey and id of store is provided along with country code than store id will
     * take precedence over external key even if invalid external key is provided.
     */
    @Test
    public void testGetProductLinesApiWithValidCountryCodeValidStoreIdInvalidStoreExternalKey() {
        final String invalidStoreExternalKey = RandomStringUtils.randomAlphabetic(12);
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        params.put(Parameter.STORE_EXTERNAL_KEY.getName(), invalidStoreExternalKey);
        apiResponse = resource.productLines().getProductLines(params);
        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            // Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLine.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to product lines
            productLines = (ProductLines) apiResponse;
            AssertCollector.assertThat("Unable to get id of productLines", productLines.getProductLineData().size(),
                equalTo(1), assertionErrorList);
            AssertCollector.assertThat("External key of product line is not correct",
                productLines.getProductLineData().get(0).getExternalKey(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Unable to get productLines", productLines.getProductLineData().get(0).getId(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Unable to get productLines", productLines.getProductLineData().get(0).getName(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Unable to get productLines", productLines.getProductLineData().get(0).getType(),
                is(notNullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test Verifies that in Get Product Lines Api, product lines will not be returned which have inActive offers
     * in subscription plans and basic offerings, associated with given store and country code.
     */
    @Test
    public void testGetProductLinesApiReturnsProductLinesWithInActivePrices() {
        final JStore store = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String externalKeyOfPriceList = store.getIncluded().getPriceLists().get(0).getExternalKey();
        final String storeId = store.getId();

        final String effectiveStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 30);
        final String effectiveEndDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15);
        final Offerings subscriptionPlan = subscriptionPlanApiUtils.addSubscriptionPlan(resource,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionPlanId = subscriptionPlan.getOfferings().get(0).getId();

        // Add Active offer to the commercial Subscription Plan
        final SubscriptionOffer activeSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.MONTH, 1, Status.ACTIVE),
            subscriptionPlanId);
        final String subscriptionOfferId = activeSubscriptionOffer.getData().getId();

        // add prices to a subscription offer
        subscriptionPlanApiUtils
            .addPricesToSubscriptionOffer(
                resource, subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOfferWithDates(0,
                    externalKeyOfPriceList, effectiveStartDate, effectiveEndDate),
                subscriptionPlanId, subscriptionOfferId);

        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        apiResponse = resource.productLines().getProductLines(params);
        if (apiResponse instanceof HttpError) { // if get product lines api
            // returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLine.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to items
            productLines = (ProductLines) apiResponse;
            AssertCollector.assertThat("Api Returned product lines", productLines.getProductLineData().size(),
                equalTo(0), assertionErrorList);
            AssertCollector.assertThat("Error is not correct", productLines.getErrors(), equalTo(null),
                assertionErrorList);
            AssertCollector.assertThat("Count is not correct in Response",
                productLines.getMetaPagination().getPagination().getCount(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Skip Count is not correct in Response",
                productLines.getMetaPagination().getPagination().getSkipCount(), equalTo("true"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests get product lines api returns upto 500 productlines. It can be achieved by changing the
     * value of TOTAL_SUBSCRIPTIONS_PRODUCTLINES to 500 at class level. MoreOver this method also tests skip count as
     * false which return total number of product lines which are associated with both Country Code and Store.
     */
    @Test
    public void testCountAndGetMultipleProductLinesByCountryCodeAndStoreId() {

        final JStore store = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String externalKeyOfPriceList = store.getIncluded().getPriceLists().get(0).getExternalKey();
        for (int i = 0; i < TOTAL_SUBSCRIPTIONS_PRODUCTLINES; i++) {
            final String productLineExternalKey = RandomStringUtils.randomAlphabetic(10);
            subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
            subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey,
                OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH,
                1, externalKeyOfPriceList, 500);
        }
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), store.getId());
        params.put(Parameter.SKIP_COUNT.getName(), String.valueOf(false));
        apiResponse = resource.productLines().getProductLines(params);

        if (apiResponse instanceof HttpError) { // if get items api returns HTTP
            // Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Items.class), assertionErrorList);
        } else {
            // typecasting Object type apiResponse to items
            productLines = (ProductLines) apiResponse;
            LOGGER
                .info("Total Number of ProductLines are: " + String.valueOf(productLines.getProductLineData().size()));
            AssertCollector.assertThat("Total Number of Product Lines are not correct",
                productLines.getProductLineData().size(), equalTo(TOTAL_SUBSCRIPTIONS_PRODUCTLINES),
                assertionErrorList);
            AssertCollector.assertThat("Count is not correct in Response",
                Integer.toString(productLines.getProductLineData().size()),
                equalTo(productLines.getMetaPagination().getPagination().getCount()), assertionErrorList);
            AssertCollector.assertThat("Count is not correct in Response",
                productLines.getMetaPagination().getPagination().getSkipCount(), equalTo("false"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find productLines with blockSize parameter value as zero
     */
    @Test
    public void findProductLinesWithBlockSizeZero() {
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        params.put(Parameter.BLOCK_SIZE.getName(), String.valueOf(0));
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;

        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated ", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error for store is not generated", productLines.getErrors().get(0).getDetail(),
            equalTo(INVALID_PAGINATION_PARAMETERS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find productLines with large blockSize parameter value greater than 1000
     */
    @Test
    public void findProductLinesWithLargeBlockSize() {
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        params.put(Parameter.BLOCK_SIZE.getName(), String.valueOf(1001));
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;

        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated ", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error for Block Size is not generated", productLines.getErrors().get(0).getDetail(),
            equalTo(INVALID_PAGINATION_PARAMETERS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find productLines with start index as negative
     */
    @Test
    public void findProductLinesWithNegativeStartIndex() {
        final String negativeStartIndex = "-3";
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), storeId);
        params.put(Parameter.START_INDEX.getName(), negativeStartIndex);
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;

        AssertCollector.assertThat("Error Code is not Generated", productLines.getErrors().get(0).getCode(),
            equalTo(ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated ", productLines.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error for Start Index is not generated",
            productLines.getErrors().get(0).getDetail(), equalTo(INVALID_PAGINATION_PARAMETERS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find productLines with start index
     */
    @Test
    public void findProductLinesWithStartIndex() {
        final String startIndex = "1";
        final int TotalNumberOfProductLines = 3;
        final JStore store = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String externalKeyOfPriceList = store.getIncluded().getPriceLists().get(0).getExternalKey();
        for (int i = 0; i < TotalNumberOfProductLines; i++) {
            final String productLineExternalKey = RandomStringUtils.randomAlphabetic(10);
            subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
            subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey,
                OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH,
                1, externalKeyOfPriceList, 500);
        }
        params.put(Parameter.COUNTRY_CODE.getName(), Country.US.getCountryCode());
        params.put(Parameter.STORE_ID.getName(), store.getId());
        params.put(Parameter.START_INDEX.getName(), startIndex);
        apiResponse = resource.productLines().getProductLines(params);
        productLines = (ProductLines) apiResponse;
        AssertCollector.assertThat("Total Number of Product Lines are not correct",
            productLines.getProductLineData().size(), equalTo(TotalNumberOfProductLines - Integer.valueOf(startIndex)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
