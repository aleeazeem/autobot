package com.autodesk.bsm.pelican.api.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.AddDefaultStore;
import com.autodesk.bsm.pelican.api.pojos.json.DefaultStoreData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This is a test class which will test "assign a default store" api
 *
 * @author vineel
 */
public class AddDefaultStoreTest extends BaseTestData {

    private PelicanPlatform resource;
    private static JStore store;
    private static JStore newStore;
    private DefaultStoreData defaultStoreData;
    private static final String type = PelicanConstants.DEFAULT_STORE_TYPE;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setupTest() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        store = storeApiUtils.addStore(Status.ACTIVE, Country.MX, Currency.MXN, null, false);
        newStore = storeApiUtils.addStore(Status.ACTIVE, Country.GB, Currency.GBP, null, false);
    }

    /**
     * This is a test method which will test assigning a default store to a store type and country
     */
    @Test
    public void testAssignDefaultStore() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect type in the api response", assignedDefaultStore.getData().getType(),
            equalTo(PelicanConstants.DEFAULT_STORE_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect id returned in the api response", assignedDefaultStore.getData().getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test editing the assigned default store to a store type and country
     */
    @Test
    public void testEditAssignedDefaultStore() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect type in the api response", assignedDefaultStore.getData().getType(),
            equalTo(PelicanConstants.DEFAULT_STORE_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect id returned in the api response", assignedDefaultStore.getData().getId(),
            notNullValue(), assertionErrorList);
        final AddDefaultStore newDefaultStore = new AddDefaultStore();
        final DefaultStoreData newDefaultStoreData = constructDefaultStoreObject(newStore);
        newDefaultStore.setData(newDefaultStoreData);
        final AddDefaultStore editedDefaultStore = resource.defaultStore().addDefaultStore(newDefaultStore);
        AssertCollector.assertThat("Incorrect type in the api response", editedDefaultStore.getData().getType(),
            equalTo(PelicanConstants.DEFAULT_STORE_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect id returned in the api response", editedDefaultStore.getData().getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test assigning the same default store to a multiple countries
     */
    @Test
    public void testAssigningDefaultStoreForMultipleCountries() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect type in the api response", assignedDefaultStore.getData().getType(),
            equalTo(PelicanConstants.DEFAULT_STORE_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect id returned in the api response", assignedDefaultStore.getData().getId(),
            notNullValue(), assertionErrorList);
        defaultStoreData.setCountryCode("GB");
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore editedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect type in the api response", editedDefaultStore.getData().getType(),
            equalTo(PelicanConstants.DEFAULT_STORE_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect id returned in the api response", editedDefaultStore.getData().getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test assigning a default store for a non-existing store type
     */
    @Test
    public void testAssignDefaultStoreWithNonExistingStoreType() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStoreData.setStoreType("abcd");
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect error response in the api response",
            assignedDefaultStore.getErrors().get(0).getDetail(), equalTo(PelicanErrorConstants.STORE_TYPE_NOT_FOUND),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test assigning a default store for a non-existing country
     */
    @Test
    public void testAssignDefaultStoreWithNonExistingCountry() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStoreData.setCountryCode("abcd");
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect error response in the api response",
            assignedDefaultStore.getErrors().get(0).getDetail(), equalTo(PelicanErrorConstants.INVALID_COUNTRY_CODE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test assigning a default store for a non-existing default store
     */
    @Test
    public void testAssignDefaultStoreWithNonExistingDefaultStore() {

        final AddDefaultStore defaultStore = new AddDefaultStore();
        defaultStoreData = constructDefaultStoreObject(store);
        defaultStoreData.setStore("abcd");
        defaultStore.setData(defaultStoreData);
        final AddDefaultStore assignedDefaultStore = resource.defaultStore().addDefaultStore(defaultStore);
        AssertCollector.assertThat("Incorrect error response in the api response",
            assignedDefaultStore.getErrors().get(0).getDetail(), equalTo(PelicanErrorConstants.STORE_NOT_FOUND),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    private DefaultStoreData constructDefaultStoreObject(final JStore store) {

        defaultStoreData = new DefaultStoreData();
        defaultStoreData.setType(type);
        defaultStoreData.setStoreType(store.getStoreType());
        defaultStoreData.setStore(store.getExternalKey());
        defaultStoreData.setCountryCode(store.getCountries().get(0).getCountry());

        return defaultStoreData;
    }
}
