package com.autodesk.bsm.pelican.ui.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.StoreAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.CountryPriceList;
import com.autodesk.bsm.pelican.ui.entities.PriceList;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.stores.AddPriceListPage;
import com.autodesk.bsm.pelican.ui.pages.stores.AddStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.FindStoreTypePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PropertiesComparator;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class AddStoreTest extends SeleniumWebdriver {

    private static FindStoreTypePage findStoreTypePage;
    private static AddStorePage addStorePage;
    private static StoreDetailPage storeDetailPage;
    private static FindStoresPage findStoresPage;
    private static AddPriceListPage addPriceListPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddStoreTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        storeDetailPage = adminToolPage.getPage(StoreDetailPage.class);
        addStorePage = adminToolPage.getPage(AddStorePage.class);
        findStoreTypePage = adminToolPage.getPage(FindStoreTypePage.class);
        addPriceListPage = adminToolPage.getPage(AddPriceListPage.class);
        findStoresPage = adminToolPage.getPage(FindStoresPage.class);

    }

    /**
     * Validate correct fields in store details page
     *
     * @Result: Correct fields
     */
    @Test
    public void verifyAllStoreDetailsFields() {

        // Create Store
        final String storeName = "Store " + RandomStringUtils.randomAlphanumeric(8);

        final String storeExtKey = storeName.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();

        storeDetailPage = StoreHelper.createStore(addStorePage, storeName, storeExtKey, storeTypeName, false);
        final String storeId = storeDetailPage.getId();
        LOGGER.info("ID of Store:" + storeId);

        final GenericDetails details = findStoresPage.findById(storeId);

        final List<String> expLabels = new ArrayList<>();

        expLabels.add("Application Family");
        expLabels.add("ID");
        expLabels.add("External Key");
        expLabels.add("Name");
        expLabels.add("Type");
        expLabels.add("Status");
        expLabels.add("Send Tax Invoices Emails");
        expLabels.add("Vat Percent");
        expLabels.add("Sold To CSN");
        expLabels.add("Created");
        expLabels.add("Last Modified");

        // Validate that all field in store detail page exists
        AssertCollector.assertThat("Incorrect labels", details.getAllFieldLabels(), equalTo(expLabels),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add store with all required fields
     *
     * @result Store is created successfully
     */
    @Test
    public void testaddSimpleStoreSuccessAndVerifyDynamoDbLogs() {

        // Store Type
        final String storeTypeName = getStoreTypeNameBic();

        final String storeTypeId = findStoreTypePage.findByValidExtKey(storeTypeName).getId();

        // Create store and add store type
        final String storeName = "SQA_Sub_Plan_" + RandomStringUtils.randomAlphabetic(5);
        final String storeExternalKey = storeName.replace(" ", "_").toUpperCase();
        final String vatPercentage = "10";
        final String soldToCsn = "12345";
        addStorePage.addStore(storeName, storeExternalKey, null, storeTypeName, false, vatPercentage, soldToCsn);
        storeDetailPage = addStorePage.clickAddStore();
        final String createdStoreId = storeDetailPage.getId();
        LOGGER.info("ID of Store:" + createdStoreId);

        AssertCollector.assertThat("Not Able to create store", createdStoreId, notNullValue(), assertionErrorList);

        final boolean createStoreAuditLogFound =
            StoreAuditLogHelper.helperToValidateDynamoDbForStore(createdStoreId, null, storeExternalKey, null,
                storeName, null, storeTypeId, null, Status.NEW, null, "0.1", null, soldToCsn, assertionErrorList);

        AssertCollector.assertTrue("Create Store Audit Log not found for Store id : " + createdStoreId,
            createStoreAuditLogFound, assertionErrorList);
        AssertCollector.assertThat("Incorrect store name:", storeDetailPage.getName(), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect External Key:", storeDetailPage.getExternalKey(),
            equalTo(storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect store type:", storeDetailPage.getType(), equalTo(storeTypeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect vat percent:", storeDetailPage.getVatPercent().split("%")[0],
            equalTo(vatPercentage), assertionErrorList);
        AssertCollector.assertThat("Incorrect Sold To Csn", storeDetailPage.getSoldToCsn(), equalTo(soldToCsn),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Add store with multiple price list and assigned countries.
     *
     * @result Store is created successfully
     */
    @Test
    public void testStoreWithPriceAndCountriesAndVerifyDynamoDbLogs() {

        // Store Type
        final String storeTypeName = getStoreTypeNameBic();
        final String storeTypeId = findStoreTypePage.findByValidExtKey(storeTypeName).getId();

        // Get # of existing store
        final int existingStoreCount = findStoresPage.findByIdDefaultSearch().getTotalItems();

        // Create New Store
        final String storeName = "New Store " + RandomStringUtils.randomAlphanumeric(8);
        final String storeExternalKey = storeName.replace(" ", "_").toUpperCase();

        // Add Store.
        addStorePage.addStore(storeName, storeExternalKey, null, storeTypeName, false, null, null);
        final StoreDetailPage addStore = addStorePage.clickAddStore();
        final String addStoreId = addStore.getId();

        final boolean createStoreAuditLogFound =
            StoreAuditLogHelper.helperToValidateDynamoDbForStore(addStoreId, null, storeExternalKey, null, storeName,
                null, storeTypeId, null, Status.NEW, null, null, null, null, assertionErrorList);

        AssertCollector.assertTrue("Create Store Audit Log not found for Store id : " + addStoreId,
            createStoreAuditLogFound, assertionErrorList);

        // Navigate back to Store Detail Page.
        StoreDetailPage addedStoreDetailPage = findStoresPage.findById(addStoreId);

        // Validate that there's no price list predefined
        AssertCollector.assertThat("Incorrect count in price list grid",
            addedStoreDetailPage.getPriceListGrid().getTotalRows(), equalTo(0), assertionErrorList);

        // Add multiple price lists to the newly created store
        final List<Currency> currencies = new ArrayList<>();
        currencies.add(Currency.USD);
        currencies.add(Currency.CAD);
        currencies.add(Currency.MXN);

        final List<PriceList> priceLists = new ArrayList<>();
        for (int i = 0; i < currencies.size(); i++) {
            final String name = "North America " + currencies.get(i).toString();
            final String extKey = name.replace(" ", "_") + RandomStringUtils.randomAlphanumeric(8);

            final PriceList priceList = new PriceList();
            priceList.setCurrency(currencies.get(i));
            priceList.setName(name);
            priceList.setExternalKey(extKey);

            addPriceListPage = addedStoreDetailPage.addPriceList();
            addPriceListPage.addPriceList(name, extKey, currencies.get(i).getLongDescription());
            addedStoreDetailPage = addPriceListPage.clickOnAddPriceList();

            final String id = addedStoreDetailPage.getPriceListGrid().getPriceList(i).getId();
            priceList.setId(id);
            priceLists.add(priceList);
        }

        // Validate correct count after adding the price lists
        AssertCollector.assertThat("Incorrect count in price list grid", addStore.getPriceListGrid().getTotalRows(),
            equalTo(currencies.size()), assertionErrorList);

        // Validate each price list
        final PropertiesComparator comparator = new PropertiesComparator();

        final List<PriceList> createdPriceList = new ArrayList<>();

        for (int i = 0; i < currencies.size(); i++) {
            final PriceList actPriceList = addedStoreDetailPage.getPriceListGrid().getPriceList(i);
            createdPriceList.add(actPriceList);
        }

        // iterating over the results from the comparison
        for (final PropertiesComparator.PropertyComparisonResult result : comparator.getResults()) {
            AssertCollector.assertTrue("Incorrect value for '" + result.getProperty() + "': " + result.toString(),
                result.isEqual(), assertionErrorList);
        }

        // Validate AuditLog for PriceList
        StoreAuditLogHelper.helperToValidateDynamoDbForCreatePriceListForStore(addStoreId, priceLists,
            assertionErrorList);

        // Validate that there's no assigned countries predefined
        AssertCollector.assertThat("Incorrect count in countries grid",
            addedStoreDetailPage.getCountryGrid().getTotalRows(), equalTo(0), assertionErrorList);

        // Assign the country to price list
        final List<CountryPriceList> countries = new ArrayList<>();
        for (final PriceList priceList : priceLists) {
            final CountryPriceList country = new CountryPriceList();

            final Currency currency = priceList.getCurrency();
            if (currency == Currency.USD) {
                country.setAssignedCountry(Country.US);
            } else if (currency == Currency.CAD) {
                country.setAssignedCountry(Country.CA);
            } else if (currency == Currency.MXN) {
                country.setAssignedCountry(Country.MX);
            } else {
                throw new RuntimeException("Unknown currency: " + currency.toString());
            }
            country.setAssignedPriceList(priceList.getName());
            country.setCurrency(currency);
            countries.add(country);

            Util.waitInSeconds(0L);
            addedStoreDetailPage.assignCountryToPriceList(country.getAssignedCountry(), country.getAssignedPriceList());
        }

        // Validate # of assigned countries
        AssertCollector.assertThat("Incorrect count in countries grid",
            addedStoreDetailPage.getCountryGrid().getTotalRows(), equalTo(countries.size()), assertionErrorList);

        // Validate each assigned country - sorted by country
        for (int i = 0; i < countries.size(); i++) {
            final CountryPriceList actualCountry = addedStoreDetailPage.getCountryGrid().getAssignedCountry(i);
            boolean found = false;
            for (final CountryPriceList country : countries) {
                if (actualCountry.getCurrency() == country.getCurrency()) {
                    found = true;
                }
            }
            AssertCollector.assertThat("Unable to find currency in country grid", found, is(true), assertionErrorList);
        }

        // iterating over the results from the comparison
        for (final PropertiesComparator.PropertyComparisonResult result : comparator.getResults()) {
            AssertCollector.assertThat("Incorrect value for '" + result.getProperty() + "': " + result.toString(),
                result.isEqual(), equalTo(true), assertionErrorList);
        }

        // Validate AuditLog for PriceList
        StoreAuditLogHelper.helperToValidateDynamoDbForCreatedCountryForStore(addStoreId,
            DateTimeUtils.getNowAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT), assertionErrorList);

        // Validate # of stores after adding the new one
        AssertCollector.assertThat("Incorrect number of stores after adding a new one",
            findStoresPage.findByIdDefaultSearch().getTotalItems(), greaterThanOrEqualTo(existingStoreCount + 1),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Validate Status fields on store detail page
     *
     * @Result: Correct fields including Status
     */

    @Test
    public void testStatusFieldForStore() {

        // Store Type
        final String storeTypeExternalKey = getStoreTypeNameBic();

        // Create Store
        final String storeName = "US_Store_" + RandomStringUtils.randomAlphabetic(5);
        addStorePage.addStore(storeName, storeName.toUpperCase(), null, storeTypeExternalKey, false, null, null);
        storeDetailPage = addStorePage.clickAddStore();

        final String storeStatus = storeDetailPage.getStatus();
        LOGGER.info("Store Status:" + storeStatus);

        AssertCollector.assertThat("Status field in store edit page does not exist", storeStatus,
            equalTo(Status.NEW.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add duplicate external key to price list - Duplicate new external key in same store - Edit an existing price list
     * and change the external key - Duplicate external key in another store with same store type - Duplicate external
     * key in another store with different store type
     *
     * @result Got error message when duplicate key is used
     */
    @Test
    public void testDuplicatePriceListExtKeyThrowsAnError() {

        // Create Store 1
        LOGGER.info("--- Create store ---");
        final String store1Name = "Store " + RandomStringUtils.randomAlphanumeric(8);

        final String store1ExtKey = store1Name.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();

        StoreDetailPage storeDetailPage =
            StoreHelper.createStore(addStorePage, store1Name, store1ExtKey, storeTypeName, true);
        final String store1Id = storeDetailPage.getId();
        LOGGER.info("ID of Store 1" + store1Id);

        final List<String> extKeys = storeDetailPage.getPriceListGrid().getColumnValues("External Key");
        final String priceList1ExtKey = extKeys.get(0);
        LOGGER.info("Store 1 price list external key: " + priceList1ExtKey);

        // Create Store 2
        final String store2Name = "SQA_Sub_Plan_" + RandomStringUtils.randomAlphabetic(5);
        final String store2ExtKey = store2Name.replace(" ", "_").toUpperCase();
        storeDetailPage = StoreHelper.createStore(addStorePage, store2Name, store2ExtKey, storeTypeName, false);
        final String store2Id = storeDetailPage.getId();

        LOGGER.info("ID of Store 2" + store2Id);

        // Add price list with existing ext key in this Store 2
        final AddPriceListPage addPriceListPage = storeDetailPage.addPriceList();
        addPriceListPage.addPriceList("something1", priceList1ExtKey, Currency.USD.getLongDescription());

        addPriceListPage.submit();

        final String expErrorMsg = "This value is already in use by another price list.";
        AssertCollector.assertThat("Incorrect error message for duplicated external key in same store type",
            addPriceListPage.getExternalKeyErrorMessage(), equalTo(expErrorMsg), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
