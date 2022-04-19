package com.autodesk.bsm.pelican.api.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.GetDefaultStoreClient.Parameter;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.AddDefaultStore;
import com.autodesk.bsm.pelican.api.pojos.json.DefaultStoreData;
import com.autodesk.bsm.pelican.api.pojos.json.GetDefaultStore;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLines;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This is a test class which will test "get default store" api
 *
 * @author Muhammad
 */
public class GetDefaultStoreTest extends BaseTestData {
    ;
    private PelicanPlatform resource;
    private static JStore store;
    private HashMap<String, String> params;
    private Object apiResponse;
    private HttpError httpError;
    private GetDefaultStore getDefaultStore;
    private static final int DEFAULT_BLOCK_SIZE = 10;
    private static final String COUNTRY_ONE = Country.GB.getCountryCode();
    private static final String COUNTRY_TWO = Country.FR.getCountryCode();
    private static final String COUNTRY_THREE = Country.DE.getCountryCode();
    // total number of countries are depended upon countries which are added above
    private static final int TOTAL_COUNTRIES = 3;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        store = storeApiUtils.addStore(Status.ACTIVE, Country.MX, Currency.MXN, null, false);
        storeApiUtils.addCountryToStoreAndPriceList(store.getIncluded().getPriceLists().get(0).getName(), store.getId(),
            Country.TC);

        // adding one default store for three countries
        addCountryInDefaultStore(store, COUNTRY_ONE);
        addCountryInDefaultStore(store, COUNTRY_TWO);
        addCountryInDefaultStore(store, COUNTRY_THREE);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        params = new HashMap<>();
    }

    /**
     * Smoke Test
     */
    @Test
    public void testGetDefaultStoreWithoutStoreTypeAndCountryAndWithPaginationParameters() {
        params.put(Parameter.SKIP_COUNT.getName(), String.valueOf(false));
        apiResponse = resource.getDefaultStore().getDefaultStore(params);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            getDefaultStore = (GetDefaultStore) apiResponse;
            AssertCollector.assertThat("Errors found in Smoke Test", getDefaultStore.getErrors(), equalTo(null),
                assertionErrorList);
            AssertCollector.assertThat("no Result is found in Smoke Test", getDefaultStore.getData(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Start Index is not correct In Response",
                getDefaultStore.getMeta().getPagination().getstartIndex(), equalTo("0"), assertionErrorList);
            AssertCollector.assertThat("Block Size is not correct In Response",
                getDefaultStore.getMeta().getPagination().getblockSize(), equalTo("10"), assertionErrorList);
            AssertCollector.assertThat("Skip Count is not correct In Response",
                getDefaultStore.getMeta().getPagination().getSkipCount(), equalTo("false"), assertionErrorList);
            AssertCollector.assertThat("Count is not correct In Response",
                Integer.valueOf(getDefaultStore.getMeta().getPagination().getCount()), greaterThanOrEqualTo(0),
                assertionErrorList);
            if (Integer.valueOf(getDefaultStore.getMeta().getPagination().getCount()) > 0) {
                AssertCollector.assertThat("Results are more than default block size", getDefaultStore.getData().size(),
                    lessThanOrEqualTo(DEFAULT_BLOCK_SIZE), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting store type as a filter.
     */
    @Test
    public void testGetDefaultStoreByStoreType() {
        params.put(Parameter.STORE_TYPE.getName(), store.getStoreType());
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        // if get items api returns HTTP Error
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            getDefaultStore = (GetDefaultStore) apiResponse;
            for (int i = 0; i < TOTAL_COUNTRIES; i++) {
                AssertCollector.assertThat("Response doesn't return  the expected country",
                    getDefaultStore.getData().get(i).getCountryCode(),
                    isOneOf(Country.GB.getCountryCode(), Country.DE.getCountryCode(), Country.FR.getCountryCode()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect Store Type", getDefaultStore.getData().get(i).getStoreType(),
                    equalTo(store.getStoreType()), assertionErrorList);
                AssertCollector.assertThat("Incorrect Store External Key", getDefaultStore.getData().get(i).getStore(),
                    equalTo(store.getExternalKey()), assertionErrorList);
            }
            AssertCollector.assertThat("Total results in Response are not correct", getDefaultStore.getData().size(),
                equalTo(TOTAL_COUNTRIES), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting country as a filter.
     */
    @Test
    public void testGetDefaultStoreByCountry() {
        params.put(Parameter.COUNTRY_CODE.getName(), COUNTRY_ONE);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        // if get items api returns HTTP Error
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            getDefaultStore = (GetDefaultStore) apiResponse;
            AssertCollector.assertThat("Total results in Response are not correct", getDefaultStore.getData().size(),
                greaterThan(0), assertionErrorList);
            AssertCollector.assertThat("Country is not Correct in Response",
                getDefaultStore.getData().get(0).getCountryCode(), equalTo(COUNTRY_ONE), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting country and store as filters.
     */
    @Test
    public void testGetDefaultStoreByCountryAndStore() {
        params.put(Parameter.STORE_TYPE.getName(), store.getStoreType());
        params.put(Parameter.COUNTRY_CODE.getName(), COUNTRY_TWO);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ProductLines.class), assertionErrorList);
        } else {
            getDefaultStore = (GetDefaultStore) apiResponse;
            AssertCollector.assertThat("Response doesn't show one result ", getDefaultStore.getData().size(),
                equalTo(1), assertionErrorList);
            AssertCollector.assertThat("Country Code is not correct in Response",
                getDefaultStore.getData().get(0).getCountryCode(), equalTo(COUNTRY_TWO), assertionErrorList);
            AssertCollector.assertThat("Store type is not correct in Response",
                getDefaultStore.getData().get(0).getStoreType(), equalTo(store.getStoreType()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting invalid store type as filters.
     */
    @Test
    public void testGetDefaultStoreByInvalidStoreType() {
        final String invalidStoreType = RandomStringUtils.randomAlphabetic(10);
        params.put(Parameter.STORE_TYPE.getName(), invalidStoreType);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        getDefaultStore = (GetDefaultStore) apiResponse;
        AssertCollector.assertThat("Error Code is not Correct", getDefaultStore.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.ITEM_NOT_FOUND_EXCEPTION_ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", getDefaultStore.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error Message for Store Type is not Correct",
            getDefaultStore.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.STORE_TYPE_ERROR_MESSAGE + " (" + invalidStoreType + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting invalid store type as filters.
     */
    @Test
    public void testGetDefaultStoreByInvalidCountryCode() {
        final String invalidCountryCode = RandomStringUtils.randomAlphabetic(4);
        params.put(Parameter.COUNTRY_CODE.getName(), invalidCountryCode);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        getDefaultStore = (GetDefaultStore) apiResponse;
        AssertCollector.assertThat("Error Code is not Correct", getDefaultStore.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.INVALID_COUNTRY_CODE_EXCEPTION_ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", getDefaultStore.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error Message for Invalid Country Code is not Correct",
            getDefaultStore.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.COUNTRY_CODE_ERROR_MESSAGE + " (" + invalidCountryCode + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the response on selecting invalid store type and invalid country code as filters. If both
     * are invalid than store type error will take precedence over country code error
     */
    @Test
    public void testGetDefaultStoreByInvalidStoreTypeAndInvalidCountryCode() {
        final String invalidStoreType = RandomStringUtils.randomAlphabetic(10);
        final String invalidCountryCode = RandomStringUtils.randomAlphabetic(4);
        params.put(Parameter.COUNTRY_CODE.getName(), invalidCountryCode);
        params.put(Parameter.STORE_TYPE.getName(), invalidStoreType);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        getDefaultStore = (GetDefaultStore) apiResponse;
        AssertCollector.assertThat("Error Code is not Correct", getDefaultStore.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.ITEM_NOT_FOUND_EXCEPTION_ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated", getDefaultStore.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error Message for Store Type is not Correct",
            getDefaultStore.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.STORE_TYPE_ERROR_MESSAGE + " (" + invalidStoreType + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find productLines with invalid blockSize
     */
    @Test(dataProvider = "invalidBlockSizeData")
    public void testGetDefaultStoreWithInvalidBlockSize(final int invalidBlockSize) {
        params.put(Parameter.BLOCK_SIZE.getName(), String.valueOf(invalidBlockSize));
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        getDefaultStore = (GetDefaultStore) apiResponse;
        AssertCollector.assertThat("Error status code is not generated ",
            getDefaultStore.getErrors().get(0).getStatus(), is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        if (invalidBlockSize > 0) {
            AssertCollector.assertThat("Error for block size is not generated",
                getDefaultStore.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_BLOCK_SIZE_GREATER_THAN_1000), assertionErrorList);
        } else {
            AssertCollector.assertThat("Error for block size is not generated",
                getDefaultStore.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_BLOCK_SIZE_LESS_THAN_0), assertionErrorList);
        }
        AssertCollector.assertThat("Error Code is not Correct", getDefaultStore.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.ILLEGAL_ARGUMENT_EXCEPTION_ERROR_CODE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that get Default with start index as negative
     */
    @Test
    public void findProductLinesWithNegativeStartIndex() {
        final String negativeStartIndex = "-3";
        params.put(Parameter.START_INDEX.getName(), negativeStartIndex);
        apiResponse = resource.getDefaultStore().getDefaultStore(params);

        getDefaultStore = (GetDefaultStore) apiResponse;
        AssertCollector.assertThat("Error Code is not Correct", getDefaultStore.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.ILLEGAL_ARGUMENT_EXCEPTION_ERROR_CODE), assertionErrorList);
        AssertCollector.assertThat("Error status code is not generated ",
            getDefaultStore.getErrors().get(0).getStatus(), is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Error for Start Index is not generated",
            getDefaultStore.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_NEGATIVE_START_INDEX), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * data provider to pass invalid block size as a parameter
     *
     * @return invalidBlockSize
     */
    @DataProvider(name = "invalidBlockSizeData")
    public Object[][] getInvalidBlockSizeData() {
        return new Object[][] { { 0 }, { -1 }, { 1001 } };
    }

    /**
     * Method to add default store for a country
     *
     * @return defaultStore for country
     */
    private AddDefaultStore addCountryInDefaultStore(final JStore store, final String country) {
        final DefaultStoreData defaultStoreData = new DefaultStoreData();
        defaultStoreData.setType(PelicanConstants.DEFAULT_STORE_TYPE);
        defaultStoreData.setStoreType(store.getStoreType());
        defaultStoreData.setStore(store.getExternalKey());
        defaultStoreData.setCountryCode(country);
        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStore.setData(defaultStoreData);
        return resource.defaultStore().addDefaultStore(defaultStore);
    }
}
