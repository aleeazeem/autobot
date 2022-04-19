package com.autodesk.bsm.pelican.api.store;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isOneOf;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JPriceList;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.PriceList;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod.Descriptor;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod.Destination;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.StateProvince;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.stores.EditStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.ShippingMethodsPage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * GetStore api test : Test methods to test scenarios of getStore API
 *
 * @author kishor
 */
public class StoreTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private static final String AUTOMATION_STORE_TYPE = "AUTO_GETSTORE_API";
    private StoreApiUtils storeApiUtils;
    private static FindStoresPage findStorePage;
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setupTest() {

        initializeDriver(getEnvironmentVariables());
        storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findStorePage = adminToolPage.getPage(FindStoresPage.class);
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        getDriver().manage().timeouts().implicitlyWait(45, TimeUnit.SECONDS);
    }

    /**
     * Verify getStore API with ID when store doesn't have any country associated with it
     *
     * @result response will return the store with the Id but without any country or price list associated with it
     */
    @Test
    public void getStoreByIdWithoutCountriesTest() {
        final Country[] assignedcountryList = {};
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();
        final JStore store = resource.stores().getStore(createdStoreId);
        LOGGER.info("The Store Id : " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(createdStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid number of price lists in the response",
            store.getIncluded().getPriceLists().size(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Invalid number of countries in the response", store.getCountries().size(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with ID when price lists and shipping methods are associated with the store
     *
     * @result Appropriate response returned with the store with countries, price lists and shipping methods for each
     *         country,
     */
    @Test
    public void getStoreByIdWithShippingMethodsTest() {
        final Country[] assignedcountryList = { Country.US };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();

        final HashMap<StateProvince, Country> destinationMap = new HashMap<>();
        destinationMap.put(StateProvince.CALIFORNIA, Country.US);
        destinationMap.put(StateProvince.ALABAMA, Country.US);

        LOGGER.info("PriceListMap : " + priceListMap);

        final List<Destination> destinationList = getTestDestinations(destinationMap);
        // Adding shipping methods
        addShippingMethod("USD_2", destinationList);
        final JStore store = resource.stores().getStore(createdStoreId);
        LOGGER.info("The Store Id From API: " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(createdStoreExternalKey), assertionErrorList);
        // If Number of countries is at least one and shipping methods are
        // there, then verify the destinations and state code
        final List<ShippingMethod> shippingMethodsInResponse = store.getIncluded().getShippingMethods();
        final List<com.autodesk.bsm.pelican.api.pojos.json.Country> countriesInResponse = store.getCountries();
        final List<PriceList> priceListInResponse = store.getIncluded().getPriceLists();
        // Not asserting the destinations against the destination test data, because there might have
        // other shipping methods which
        // have same countries as destination and that gets included in the destination list of shipping
        // methods
        AssertCollector.assertThat("Invalid Number of Destinations", shippingMethodsInResponse.size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Invalid number of countries", countriesInResponse.size(),
            equalTo(countriesAssigned.size()), assertionErrorList);

        final List<String> countriesInDestinations = new ArrayList<>();
        for (final ShippingMethod eachShippingMethod : shippingMethodsInResponse) {
            // Check for each destination in the list
            for (final Destination eachDestination : eachShippingMethod.getDestinations()) {
                if (eachDestination.getState() != null) {
                    AssertCollector.assertThat("Invalid State Code in Destinations",
                        eachDestination.getState().length(), equalTo(2), assertionErrorList);
                }
                countriesInDestinations.add(eachDestination.getCountry());
            }
        }
        // Verify the countries available in the store and the countries should
        // be available in the shipping methods destination
        for (final com.autodesk.bsm.pelican.api.pojos.json.Country country : countriesInResponse) {
            final String[] countryCodeList = new String[assignedcountryList.length];
            for (int i = 0; i < assignedcountryList.length; i++) {
                countryCodeList[i] = assignedcountryList[i].getCountryCode();
            }
            AssertCollector.assertThat("Invalid Country in response", country.getCountry(), isOneOf(countryCodeList),
                assertionErrorList);
            AssertCollector.assertThat("Countries expected in shippingMethods not available", country.getCountry(),
                isOneOf(countriesInDestinations.toArray()), assertionErrorList);
            AssertCollector.assertThat("One pricelist per country expected in store,But not available",
                country.getLinks().getPriceLists().getLinkage().getType(), equalTo(EntityType.PRICE_LIST),
                assertionErrorList);
            AssertCollector.assertThat("One Shipping Method per country expected in store,But not available",
                country.getLinks().getShippingMethods().getLinkage().size(), greaterThanOrEqualTo(1),
                assertionErrorList);
        }
        // validating priceLists in the response
        AssertCollector.assertThat("Invalid number of pricelists", priceListInResponse.size(),
            equalTo(priceListMap.size()), assertionErrorList);

        final List<Currency> currenciesInPriceList = new ArrayList<>();
        for (final PriceList priceList : priceListInResponse) {
            currenciesInPriceList.add(Currency.getByValue(priceList.getCurrency()));
        }
        LOGGER.info("currencies :" + currenciesInPriceList);
        AssertCollector.assertThat("Currency not available in pricelist", currenciesInPriceList,
            containsInAnyOrder(priceListMap.values().toArray()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Countries filter when the store have no shipping methods associated with it
     *
     * @result Appropriate response returned with the store with countries, price lists and NO shipping methods for the
     *         country,
     */
    @Test
    public void getStoreByIdWithCountriesWithoutShippingMethodsTest() {

        /*
         * Assuming a random country which doesn't have a shipping method assigned to. If a shipping method have
         * destination as the country mentioned in the test this test will fail IN this test I have picked Taicos Island
         * assuming that nobody have any shipping methods destination assigned to TC
         */
        final Country[] assignedcountryList = { Country.TC };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.TC, Currency.USD);

        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String storeExternalKey = createdStore.getData().getExternalKey();
        final JStore store = resource.stores().getStore(createdStoreId);
        LOGGER.info("The Store Id : " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid number of price lists in the response",
            store.getIncluded().getPriceLists().size(), equalTo(priceListMap.size()), assertionErrorList);
        AssertCollector.assertThat("Invalid currency in price lists in the response",
            store.getIncluded().getPriceLists().get(0).getCurrency(), equalTo("USD"), assertionErrorList);
        AssertCollector.assertThat("Invalid type of pricelist in the response",
            store.getIncluded().getPriceLists().get(0).getType(), equalTo(EntityType.PRICE_LIST), assertionErrorList);
        AssertCollector.assertThat("Invalid pricelist name in the response",
            store.getIncluded().getPriceLists().get(0).getName(), containsString(storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(storeExternalKey), assertionErrorList);
        final List<com.autodesk.bsm.pelican.api.pojos.json.Country> countriesInResponse = store.getCountries();
        for (final com.autodesk.bsm.pelican.api.pojos.json.Country country : countriesInResponse) {
            AssertCollector.assertThat("Invalid Country in response", Country.getByCode(country.getCountry()),
                isOneOf(assignedcountryList), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with ID when Store is in NEW Status
     *
     * @result Appropriate error response returned
     */
    @Test
    public void getStoreByIdWhenStoreStatusNewTest() {

        final Country[] assignedcountryList = { Country.US };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.NEW, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();

        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.INVALID_STORE_ID;
        // Calling getSTore API
        // Getting the error response
        final Errors errorResponse = resource.stores().getStore(createdStoreId).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with ID when Store is in Canceled Status
     *
     * @result Appropriate error response returned
     */
    @Test
    public void getStoreByIdWhenStoreStatusCanceledTest() {

        final Country[] assignedcountryList = { Country.US };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        // update the store status as Cancelled using Admin Tool as No API to create a Cancelled Store
        updateStoreStatusThroughUI(createdStoreId, Status.CANCELED);
        // wait for the status to get saved!!
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.INVALID_STORE_ID;
        // Getting the error response
        final Errors errorResponse = resource.stores().getStore(createdStoreId).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API filter by external Key
     *
     * @result Appropriate response returned with the stores with countries, price lists and shipping methods for the
     *         country mentioned in the filter
     */
    @Test
    public void getStoreByExternalKeyWithShippingMethodsTest() {

        final Country[] assignedcountryList = { Country.US };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();

        final HashMap<StateProvince, Country> destinationMap = new HashMap<>();
        destinationMap.put(StateProvince.CALIFORNIA, Country.US);
        destinationMap.put(StateProvince.ALABAMA, Country.US);

        final List<Destination> destinationList = getTestDestinations(destinationMap);
        // Adding shipping methods
        addShippingMethod("USD_2", destinationList);

        // Get Store API with External Key as filter
        final JStore store = resource.stores().getStoreByExternalKey(createdStoreExternalKey);
        LOGGER.info("The Store Id : " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(createdStoreExternalKey), assertionErrorList);

        final List<ShippingMethod> shippingMethodsInResponse = store.getIncluded().getShippingMethods();
        AssertCollector.assertThat("Invalid Number of Destinations", shippingMethodsInResponse.size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        for (final ShippingMethod eachShippingMethod : shippingMethodsInResponse) {
            // Check for each destination in the list
            for (final Destination eachDestination : eachShippingMethod.getDestinations()) {
                if (eachDestination.getState() != null) {
                    AssertCollector.assertThat("Invalid State Code in Destinations",
                        eachDestination.getState().length(), equalTo(2), assertionErrorList);
                }
            }
        }
        AssertCollector.assertThat("Invalid number of price lists in the response",
            store.getIncluded().getPriceLists().size(), equalTo(priceListMap.size()), assertionErrorList);
        AssertCollector.assertThat("Invalid currency in price lists in the response",
            store.getIncluded().getPriceLists().get(0).getCurrency(), equalTo("USD"), assertionErrorList);
        AssertCollector.assertThat("Invalid type of pricelist in the response",
            store.getIncluded().getPriceLists().get(0).getType(), equalTo(EntityType.PRICE_LIST), assertionErrorList);
        AssertCollector.assertThat("Invalid pricelist name in the response",
            store.getIncluded().getPriceLists().get(0).getName(), containsString(createdStoreExternalKey),
            assertionErrorList);
        AssertCollector.assertThat("Invalid Number of Destinations", shippingMethodsInResponse.size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        final List<com.autodesk.bsm.pelican.api.pojos.json.Country> countriesInResponse = store.getCountries();
        for (final com.autodesk.bsm.pelican.api.pojos.json.Country country : countriesInResponse) {
            final String[] countryCodeList = new String[assignedcountryList.length];
            // Getting the CountryCodes list to verify the codes against the response
            for (int i = 0; i < assignedcountryList.length; i++) {
                countryCodeList[i] = assignedcountryList[i].getCountryCode();
            }
            AssertCollector.assertThat("Invalid Country in response", country.getCountry(), isOneOf(countryCodeList),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API filter by external Key when there are no shipping methods
     *
     * @result Appropriate response returned with the store with countries, price lists and NO shipping methods for the
     *         country mentioned in the filter
     */
    @Test
    public void getStoreByExternalKeyWithoutShippingMethodsTest() {

        final Country[] assignedcountryList = { Country.TC };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.TC, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();

        // Get Store API with External Key as filter
        final JStore store = resource.stores().getStoreByExternalKey(createdStoreExternalKey);
        LOGGER.info("The Store Id : " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(createdStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid number of price lists in the response",
            store.getIncluded().getPriceLists().size(), equalTo(priceListMap.size()), assertionErrorList);
        AssertCollector.assertThat("Invalid currency in price lists in the response",
            store.getIncluded().getPriceLists().get(0).getCurrency(), equalTo("USD"), assertionErrorList);
        AssertCollector.assertThat("Invalid type of pricelist in the response",
            store.getIncluded().getPriceLists().get(0).getType(), equalTo(EntityType.PRICE_LIST), assertionErrorList);
        AssertCollector.assertThat("Invalid pricelist name in the response",
            store.getIncluded().getPriceLists().get(0).getName(), containsString(createdStoreExternalKey),
            assertionErrorList);
        final List<com.autodesk.bsm.pelican.api.pojos.json.Country> countriesInResponse = store.getCountries();
        for (final com.autodesk.bsm.pelican.api.pojos.json.Country country : countriesInResponse) {
            final String[] countryCodeList = new String[assignedcountryList.length];
            // Getting the CountryCodes list to verify the codes against the response
            for (int i = 0; i < assignedcountryList.length; i++) {
                countryCodeList[i] = assignedcountryList[i].getCountryCode();
            }
            AssertCollector.assertThat("Invalid Country in response", country.getCountry(), isOneOf(countryCodeList),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API filter by external Key when there are no shipping methods
     *
     * @result Appropriate response returned
     */
    @Test
    public void getStoreByExternalKeyWithoutCountriesTest() {

        final Country[] assignedcountryList = {};
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();
        // Get Store API with External Key as filter
        final JStore store = resource.stores().getStoreByExternalKey(createdStoreExternalKey);
        LOGGER.info("The Store Id : " + store.getId());
        AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId), assertionErrorList);
        AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
            equalTo(createdStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid number of price lists in the response",
            store.getIncluded().getPriceLists().size(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Invalid Number of Destinations", store.getIncluded().getShippingMethods().size(),
            equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Country name and store type external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void getStoresByCountryAndTypeTest() {
        final Country[] assignedcountryList = { Country.US, Country.CA };
        final HashMap<Country, Currency> priceListMap = new HashMap<>();
        priceListMap.put(Country.US, Currency.USD);
        priceListMap.put(Country.CA, Currency.CAD);

        final List<Country> countriesAssigned = Arrays.asList(assignedcountryList);

        final HashMap<StateProvince, Country> destinationMap = new HashMap<>();
        destinationMap.put(StateProvince.CALIFORNIA, Country.US);
        destinationMap.put(StateProvince.ALABAMA, Country.US);
        destinationMap.put(StateProvince.ALBERTA, Country.CA);

        final List<Destination> destinationList = getTestDestinations(destinationMap);
        // Adding shipping methods
        addShippingMethod("USD_2", destinationList);

        // Creating a store using create store API
        final com.autodesk.bsm.pelican.api.pojos.json.Store createdStore =
            createStoreUsingAPI(Status.ACTIVE, priceListMap, countriesAssigned);
        final String createdStoreId = createdStore.getData().getId();
        final String createdStoreExternalKey = createdStore.getData().getExternalKey();
        final String createdStoreTypeExternalKey = createdStore.getData().getStoreType();

        // Get Store API with Country and Type as filter
        final List<JStore> stores = new LinkedList<>();
        List<com.autodesk.bsm.pelican.api.pojos.json.Country> countriesInResponse;
        // Parse for all Countries assigned
        for (final Country country : assignedcountryList) {
            stores.addAll(resource.stores().getStoresByCountry(country.getCountryCode(), createdStoreTypeExternalKey));
        }

        // Makes sure we received a proper response with at least one store
        // which we created
        AssertCollector.assertThat("Invalid store count", stores.size(), greaterThanOrEqualTo(1), assertionErrorList);
        for (final JStore store : stores) {
            // We need to/can verify the details of the store which we inserted
            // for this test
            if (store.getExternalKey().equalsIgnoreCase(createdStoreExternalKey)) {
                LOGGER.info("The Store Id : " + store.getId());
                AssertCollector.assertThat("Invalid store Id", store.getId(), equalTo(createdStoreId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid External Key in the Response", store.getExternalKey(),
                    equalTo(createdStoreExternalKey), assertionErrorList);
                final List<ShippingMethod> shippingMethodsInResponse = store.getIncluded().getShippingMethods();
                AssertCollector.assertThat("Invalid Number of Destinations", shippingMethodsInResponse.size(),
                    greaterThanOrEqualTo(1), assertionErrorList);
                for (final ShippingMethod eachShippingMethod : shippingMethodsInResponse) {
                    // Check for each destination in the list
                    for (final Destination eachDestination : eachShippingMethod.getDestinations()) {
                        if (eachDestination.getState() != null) {
                            AssertCollector.assertThat("Invalid State Code in Destinations",
                                eachDestination.getState().length(), equalTo(2), assertionErrorList);
                        }
                    }
                }
                AssertCollector.assertThat("Invalid Number of Destinations", shippingMethodsInResponse.size(),
                    greaterThanOrEqualTo(1), assertionErrorList);
            }
            countriesInResponse = store.getCountries();
            // Validating the only country available in the countries list as filtered using a country
            final String[] countryCodeList = new String[assignedcountryList.length];
            // Getting the CountryCodes list to verify the codes against the response
            for (int i = 0; i < assignedcountryList.length; i++) {
                countryCodeList[i] = assignedcountryList[i].getCountryCode();
            }
            AssertCollector.assertThat("Invalid Country in response", countriesInResponse.get(0).getCountry(),
                isOneOf(countryCodeList), assertionErrorList);
            AssertCollector.assertThat("Invalid Store Type in the response", store.getStoreType(),
                equalTo(createdStoreTypeExternalKey), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Invalid Store Id in the URL
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByInvalidStoreIdTest() {
        // Getting the error response
        final String storeId = "0000";
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.INVALID_STORE_ID;
        final Errors errorResponse = resource.stores().getStore(storeId).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Empty Store Id in the URL
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByEmptyStoreIdTest() {
        // Getting the error response
        final String storeId = "";
        GetStoreErrorMessages expErrorMessage;
        // Different Error messages in EMEA and Namer
        expErrorMessage = GetStoreErrorMessages.MISSING_PARAM_FILTER;
        final Errors errorResponse = resource.stores().getStore(storeId).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with valid country and a valid storeType's external key in the filters when the country
     * doesn't have any stores defined
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoresByCountryWithNoStoresTest() {
        // Getting the error response
        // Taking a random country! Will fail if the country mentioned here have any stores defined
        final String countryName = "DJ";
        final String storeTypeExternalKey = getStoreTypeNameBic();
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.NO_STORE_FOUND;
        final Errors errorResponse =
            resource.stores().getStoresByCountry(countryName, storeTypeExternalKey).get(0).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            equalTo(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Invalid country and a valid storeType's external key in the filters
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoresByIncorrectCountryTest() {
        // Getting the error response
        final String countryName = "Invalid";
        final String storeTypeExternalKey = getStoreTypeNameBic();
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.INVALID_COUNTRY;
        final Errors errorResponse =
            resource.stores().getStoresByCountry(countryName, storeTypeExternalKey).get(0).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            equalTo(expErrorMessage.getErrorTitle() + " " + countryName), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with a valid country but Invalid storeType's external key in the filters
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoresByIncorrectStoreTypeTest() {
        // Getting the error response
        final String countryName = "US";
        final String storeTypeExternalKey = "Invalid";
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.INVALID_STORE_TYPE;
        final Errors errorResponse =
            resource.stores().getStoresByCountry(countryName, storeTypeExternalKey).get(0).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            equalTo(expErrorMessage.getErrorTitle() + " " + storeTypeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with an empty country but valid storeType's external key in the filters
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoresByEmptyCountryTest() {
        // Getting the error response
        final String countryName = " ";
        GetStoreErrorMessages expErrorMessage;
        // Different Error messages in EMEA and Namer
        expErrorMessage = GetStoreErrorMessages.MISSING_PARAM_FILTER;
        final Errors errorResponse =
            resource.stores().getStoresByCountry(countryName, AUTOMATION_STORE_TYPE).get(0).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            equalTo(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with a valid country but empty storeType's external key in the filters
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoresByEmptyStoreTypeTest() {
        // Getting the error response
        final String countryName = "US";
        final String storeTypeExternalKey = " ";
        GetStoreErrorMessages expErrorMessage;
        // Different Error messages in EMEA and Namer
        expErrorMessage = GetStoreErrorMessages.MISSING_PARAM_FILTER;
        final Errors errorResponse =
            resource.stores().getStoresByCountry(countryName, storeTypeExternalKey).get(0).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            equalTo(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Invalid store external key in the filter
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByInvalidExternalKeyTest() {
        final String storeExternalKey = "Invalid";
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.NO_STORE_FOUND_FOR_EXTERNALKEY;
        // Getting the error response
        final Errors errorResponse = resource.stores().getStoreByExternalKey(storeExternalKey).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle() + " " + storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with Empty store external key in the filter
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByEmptyExternalKeyTest() {
        final String storeExternalKey = "";
        GetStoreErrorMessages expErrorMessage;
        // Different Error messages in EMEA and Namer
        System.out.println("ENV is  : " + getEnvironmentVariables().getEnvironmentType());
        expErrorMessage = GetStoreErrorMessages.MISSING_PARAM_FILTER;
        // Getting the error response
        final Errors errorResponse = resource.stores().getStoreByExternalKey(storeExternalKey).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with empty space(" ") store external key in the filter
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByEmptySpaceExternalKeyTest() {
        final String storeExternalKey = " ";
        GetStoreErrorMessages expErrorMessage;
        // Different Error messages in EMEA and Namer
        expErrorMessage = GetStoreErrorMessages.MISSING_PARAM_FILTER;
        // Getting the error response
        final Errors errorResponse = resource.stores().getStoreByExternalKey(storeExternalKey).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle()), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify getStore API with wildcard special character (*) as store external key in the filter
     *
     * @result appropriate error responses validated
     */
    @Test
    public void getStoreByWildCardExternalKeyTest() {
        final String storeExternalKey = "*";
        final GetStoreErrorMessages expErrorMessage = GetStoreErrorMessages.NO_STORE_FOUND_FOR_EXTERNALKEY;
        // Getting the error response
        final Errors errorResponse = resource.stores().getStoreByExternalKey(storeExternalKey).getErrors();
        AssertCollector.assertThat("Invalid Error Code", errorResponse.getCode(),
            equalTo(String.valueOf(expErrorMessage.getErrorCode())), assertionErrorList);
        AssertCollector.assertThat("Invalid Error Title", errorResponse.getDetail(),
            containsString(expErrorMessage.getErrorTitle() + " " + storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Invalid Http Status Code", errorResponse.getStatus(),
            equalTo(expErrorMessage.getHttpStatusCode()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Update the status of a given Store
     */
    private void updateStoreStatusThroughUI(final String storeId, final Status storeStatus) {

        final StoreDetailPage storeDetailPage = findStorePage.findById(storeId);
        final EditStorePage editStore = storeDetailPage.editStore();
        editStore.editStore(storeStatus.getDisplayName(), null, false, null, null, null, null);
        editStore.clickOnSaveChanges();
    }

    private String addShippingMethod(final String shippingMethodName, final List<Destination> destinationList) {
        final ShippingMethodsPage shippingPage = adminToolPage.getPage(ShippingMethodsPage.class);
        ShippingMethod shippingMethod = new ShippingMethod();
        final GenericDetails shippingMethodDetails = shippingPage.findByExtKey(shippingMethodName);
        // Removing the shipping method if already exists
        if (shippingMethodDetails != null) {
            LOGGER.info("Shipping method already exists, preparing to delete");
            shippingMethod.setId(shippingMethodDetails.getValueByField("ID"));
            shippingPage.delete(shippingMethod.getId());
        }
        final Descriptor shippingMethodDesc = new Descriptor();
        shippingMethodDesc.setDelveryTime("Test ShippingMethod used for Automation");
        shippingMethodDesc.setName(shippingMethodName);
        shippingMethod.setDescriptor(shippingMethodDesc);
        shippingMethod.setDestinations(destinationList);
        shippingMethod.setExternalKey(shippingMethodName);
        shippingMethod.setAgentId("1");
        final Price shippingPrice = new Price();
        shippingPrice.setAmount("1");
        shippingPrice.setCurrency("USD");
        shippingMethod.setPrice(shippingPrice);
        shippingMethod = shippingPage.add(shippingMethod);
        LOGGER.info("The ShippinMethod Id is : " + shippingMethod.getId());
        return shippingMethod.getId();

    }

    private List<Destination> getTestDestinations(final HashMap<StateProvince, Country> destinationMap) {
        final List<Destination> destinationList = new ArrayList<>();
        Destination destination;
        for (final Entry<StateProvince, Country> destinationEntry : destinationMap.entrySet()) {
            destination = new Destination();
            destination.setState(destinationEntry.getKey().getStateName());
            destination.setCountry(destinationEntry.getValue().getCountryCode());
            destinationList.add(destination);
        }
        return destinationList;
    }

    private com.autodesk.bsm.pelican.api.pojos.json.Store createStoreUsingAPI(final Status status,
        final Map<Country, Currency> priceList, final List<Country> assignCountryList) {

        final com.autodesk.bsm.pelican.api.pojos.json.Store store =
            storeApiUtils.addStoreWithoutPriceListAndCountry(status);
        final String storeId = store.getData().getId();
        final String storeExternalKey = store.getData().getExternalKey();
        int count = 0;
        for (final Map.Entry<Country, Currency> mapEntry : priceList.entrySet()) {
            final Country country = mapEntry.getKey();
            final Currency currency = mapEntry.getValue();
            final JPriceList priceListResponse =
                storeApiUtils.addPriceListWithExternalKey(storeId, storeExternalKey + count, currency);
            for (final Country countryToBeAssigned : assignCountryList) {
                if (country.getCountryCode().equalsIgnoreCase(countryToBeAssigned.getCountryCode())) {
                    storeApiUtils.addCountryToStoreAndPriceList(priceListResponse.getData().getExternalKey(), storeId,
                        countryToBeAssigned);
                }
            }
            count++;
        }
        return store;
    }

    private enum GetStoreErrorMessages {

        INVALID_STORE_ID("Store not found for Id :", 990013, 404),
        INVALID_COUNTRY("Invalid country:", 990002, 400),
        INVALID_STORE_TYPE("Invalid store type :", 990002, 400),
        MISSING_PARAM_FILTER("Either Subscription Id or Price Id or Store External Key or both "
            + "Country and Store Type should be provided", 990002, 400),
        NO_STORE_FOUND("No stores found.", 990013, 404),
        NO_STORE_FOUND_FOR_EXTERNALKEY("Store not found for externalKey :", 990013, 404);

        private final String errorTitle;
        private final int errorCode;
        private final int httpStatusCode;

        GetStoreErrorMessages(final String errorMessage, final int errorCode, final int httpStatus) {
            this.errorTitle = errorMessage;
            this.errorCode = errorCode;
            this.httpStatusCode = httpStatus;
        }

        String getErrorTitle() {
            return errorTitle;
        }

        int getErrorCode() {
            return errorCode;
        }

        int getHttpStatusCode() {
            return httpStatusCode;
        }
    }
}
