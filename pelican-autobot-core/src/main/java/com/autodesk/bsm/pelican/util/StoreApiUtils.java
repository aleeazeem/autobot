package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.CountryData;
import com.autodesk.bsm.pelican.api.pojos.json.Data;
import com.autodesk.bsm.pelican.api.pojos.json.JCountry;
import com.autodesk.bsm.pelican.api.pojos.json.JPriceList;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Store;
import com.autodesk.bsm.pelican.api.pojos.json.StoreType;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

public class StoreApiUtils {

    private static final String type = "storeType";
    private static final String storeTypeText = "store";
    private static final String PRICELIST_TYPE = "pricelist";
    private static final String COUNTRY_TYPE = "storeCountry";
    private static final String appendString = "SQA_Test_Util_";
    private EnvironmentVariables environmentVariables;

    public StoreApiUtils(final EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * A method used to add a store type, store, price list and country to a store
     *
     * @return JStore Object
     */
    public JStore addStore(final PelicanPlatform resource, final Status status, final Country countryCode,
        final Currency currency, final Double vatPercent, final String soldToCsn, final boolean sendTaxInvoicesEmails,
        String storeExternalKey, String priceListExternalKey, final String storeTypeExternalKey) {

        // Add a store
        final Store store = new Store();
        if (storeExternalKey == null) {
            storeExternalKey = appendString + RandomStringUtils.randomAlphabetic(8);
        }
        final Data storeData = new Data();
        storeData.setExternalKey(storeExternalKey);
        storeData.setName(storeExternalKey);
        storeData.setType(storeTypeText);
        storeData.setStatus(status);
        storeData.setStoreType(storeTypeExternalKey);
        storeData.setVatPercent(vatPercent);
        if (soldToCsn != null) {
            storeData.setSoldToCsn(soldToCsn);
        } else {
            storeData.setSoldToCsn(Integer.toString(RandomUtils.nextInt(10000000)));
        }
        storeData.setSendTaxInvoicesEmails(sendTaxInvoicesEmails);
        store.setData(storeData);
        final Store newStore = addStore(resource, store);
        final String storeId = newStore.getData().getId();

        // Add priceList
        final JPriceList priceList = new JPriceList();
        if (priceListExternalKey == null) {
            priceListExternalKey = appendString + RandomStringUtils.randomAlphabetic(6);
        }
        final Data priceListData = new Data();
        priceListData.setExternalKey(priceListExternalKey);
        priceListData.setName(priceListExternalKey);
        priceListData.setType(PRICELIST_TYPE);
        priceListData.setCurrency(currency);
        priceList.setData(priceListData);
        addPriceList(resource, priceList, storeId);

        // Assign a country
        final JCountry country = new JCountry();
        final CountryData countryData = new CountryData();
        countryData.setCountryCode(countryCode);
        countryData.setType(COUNTRY_TYPE);
        countryData.setPriceList(priceListExternalKey);
        country.setData(countryData);
        addCountry(resource, country, storeId);

        return resource.stores().getStore(storeId);
    }

    /**
     * A method used to add a store type, store, price list and country to a store
     *
     * @return Jstore Object
     */
    public JStore addStore(final Status status, final Country countryCode, final Currency currency,
        final String soldToCsn, final boolean sendTaxInvoicesEmails) {
        // Add a store type
        final StoreType storeType = new StoreType();
        final String storeTypeExternalKey = appendString + RandomStringUtils.randomAlphabetic(10);
        final Data data = new Data();
        data.setExternalKey(storeTypeExternalKey);
        data.setName(storeTypeExternalKey);
        data.setType(type);
        storeType.setData(data);
        final PelicanPlatform resource = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());
        addNewStoreType(resource, storeType);
        return addStore(resource, status, countryCode, currency, null, soldToCsn, sendTaxInvoicesEmails, null, null,
            storeTypeExternalKey);
    }

    /*
     * A method which is used to add a price list
     *
     * @ Param - Application Family, StoreId, Currency
     *
     * @Return - JPricelist Object
     */
    public JPriceList addPriceList(final String storeId, final Currency currency) {
        return addPriceListWithCountry(storeId, currency, Country.CA);
    }

    /*
     * A method which is used to add a price list
     *
     * @ Param - StoreId, Currency, Country
     *
     * @Return - JPricelist Object
     */
    public JPriceList addPriceListWithCountry(final String storeId, final Currency currency, final Country country) {
        final String priceListExternalKey = appendString + RandomStringUtils.randomAlphabetic(6);
        final JPriceList newPriceList = addPriceListWithExternalKey(storeId, priceListExternalKey, currency);
        // Adding the priceList External Key to the pojo for the usage in test classes, as the API response will not
        // have the external key in it
        newPriceList.getData().setExternalKey(priceListExternalKey);

        // Assign a country
        final PelicanPlatform resource = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());
        final JCountry jCountry = new JCountry();
        final CountryData countryData = new CountryData();
        countryData.setCountryCode(country);
        countryData.setType(COUNTRY_TYPE);
        countryData.setPriceList(priceListExternalKey);
        jCountry.setData(countryData);
        addCountry(resource, jCountry, storeId);

        return newPriceList;
    }

    /*
     * A method which is used to add a price list
     *
     * @ Param - StoreId, Currency
     *
     * @Return - JPricelist Object
     */
    public JPriceList addPriceListWithExternalKey(final String storeId, final String priceListExternalKey,
        final Currency currency) {

        final PelicanPlatform resource = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());
        // Add priceList
        final JPriceList priceList = new JPriceList();
        final Data priceListData = new Data();
        priceListData.setExternalKey(priceListExternalKey);
        priceListData.setName(priceListExternalKey);
        priceListData.setType(PRICELIST_TYPE);
        priceListData.setCurrency(currency);
        priceList.setData(priceListData);
        final JPriceList newPriceList = addPriceList(resource, priceList, storeId);
        // Adding the priceList External Key to the pojo for the usage in test
        // classes, as the API response will not have the external key in it
        newPriceList.getData().setExternalKey(priceListExternalKey);
        return newPriceList;
    }

    /*
     * A method which is used to Add a Store Without price list and country
     *
     * @ Param - Application Family, Status
     *
     * @ Return - Store Object
     */
    public Store addStoreWithoutPriceListAndCountry(final Status status) {
        final String storeTypeExternalKey = appendString + RandomStringUtils.randomAlphabetic(10);
        final String storeExternalKey = appendString + RandomStringUtils.randomAlphabetic(8);
        final Store newStore = addStoreWithExternalKey(storeTypeExternalKey, storeExternalKey, status);
        // setting the External Key used to create the store
        newStore.getData().setExternalKey(storeExternalKey);
        newStore.getData().setStoreType(storeTypeExternalKey);
        newStore.getData().setName(storeExternalKey);
        return newStore;
    }

    /*
     * A method which is used to Add a Store Without price list and country
     *
     * @ Param - SToreType External Key, Store ExternalKey Status
     *
     * @ Return - Store Object
     */
    public Store addStoreWithExternalKey(final String storeTypeExternalKey, final String storeExternalKey,
        final Status status) {

        final PelicanPlatform resource = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());

        // Add a store type
        final StoreType storeType = new StoreType();
        final Data data = new Data();
        data.setExternalKey(storeTypeExternalKey);
        data.setName(storeTypeExternalKey);
        data.setType(type);
        storeType.setData(data);
        addNewStoreType(resource, storeType);

        // Add a store
        final Store store = new Store();
        final Data storeData = new Data();
        storeData.setExternalKey(storeExternalKey);
        storeData.setName(storeExternalKey);
        storeData.setType(storeTypeText);
        storeData.setStatus(status);
        storeData.setStoreType(storeTypeExternalKey);
        store.setData(storeData);
        final Store newStore = addStore(resource, store);
        // setting the External Key used to create the store for the assisting
        // test as response data will not have these details
        newStore.getData().setExternalKey(storeExternalKey);
        newStore.getData().setStoreType(storeTypeExternalKey);
        return newStore;
    }

    /*
     * A method which is used to add a country to store and price list
     *
     * @ Param - Application Family, StoreI and Country Code
     *
     * @ Return - JCountry object
     */
    public JCountry addCountryToStoreAndPriceList(final String priceListExternalKey, final String storeId,
        final Country countryCode) {

        final PelicanPlatform resource = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());

        // Assign a country
        final JCountry country = new JCountry();
        final CountryData countryData = new CountryData();
        countryData.setCountryCode(countryCode);
        countryData.setType(COUNTRY_TYPE);
        countryData.setPriceList(priceListExternalKey);
        country.setData(countryData);
        return addCountry(resource, country, storeId);

    }

    /**
     * A method to generate a random storeType external key
     *
     * @return string
     */
    public String getRandomStoreTypeExtKey() {
        return appendString + RandomStringUtils.randomAlphabetic(10);
    }

    /*
     * A method used to create store type
     *
     * @ Param - Pelican Platform and Store Type
     *
     * @ Return storeType object
     */
    public StoreType addNewStoreType(final PelicanPlatform resource, final StoreType storeType) {
        return resource.storeType().addStoreType(storeType);
    }

    /*
     * A method used to add a store
     *
     * @ Param - Pelican Platform and Store Object
     *
     * @ Return - Store Object
     */
    private Store addStore(final PelicanPlatform resource, final Store store) {
        return resource.createStore().addStore(store);
    }

    /*
     * A method used to add a price list
     *
     * @ param - Pelican Platform, JPricelist and Store Id
     *
     * @ Return - JPricelist
     */
    private JPriceList addPriceList(final PelicanPlatform resource, final JPriceList priceList, final String storeId) {
        return resource.priceList().addPriceList(priceList, storeId);
    }

    /*
     * A method used to add country
     *
     * @ Param - Pelican Platform, JCountry and StoreId
     *
     * @ Return - JCountry
     */
    private JCountry addCountry(final PelicanPlatform resource, final JCountry country, final String storeId) {
        return resource.country().addCountry(country, storeId);
    }
}
