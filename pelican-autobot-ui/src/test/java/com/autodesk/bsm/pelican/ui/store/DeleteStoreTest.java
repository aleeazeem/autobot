package com.autodesk.bsm.pelican.ui.store;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.StoreAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.PriceList;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.stores.AddStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.CountryGrid;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.PriceListDetailPage;
import com.autodesk.bsm.pelican.ui.pages.stores.PriceListGrid;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.FindStoreTypePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class DeleteStoreTest extends SeleniumWebdriver {

    private static AddStorePage addStorePage;
    private static FindStoresPage findStoresPage;
    private static FindStoreTypePage findStoreTypePage;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteStoreTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addStorePage = adminToolPage.getPage(AddStorePage.class);
        findStoreTypePage = adminToolPage.getPage(FindStoreTypePage.class);
        findStoresPage = adminToolPage.getPage(FindStoresPage.class);
    }

    /**
     * Delete store. Create store with price lists and assign countries to price list. Validate that - Unable to delete
     * store if there's existing price list - Unable to delete price list if there's existing associated country - Most
     * delete assigned countries, then price list, the store
     *
     * @result Store is deleted if there's no price list nor countries defined
     */
    @Test
    public void testSuccessToDeleteStore() {

        // Create store with price list and assign countries
        LOGGER.info("--- Create store ---");
        final String storeName = "Store " + RandomStringUtils.randomAlphanumeric(8);

        final String storeExtKey = storeName.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();
        final String storeTypeId = findStoreTypePage.findByValidExtKey(storeTypeName).getId();

        StoreDetailPage storeDetailPage =
            StoreHelper.createStore(addStorePage, storeName, storeExtKey, storeTypeName, true);
        final String storeId = storeDetailPage.getId();

        // Delete store - should get error
        LOGGER.info("--- Delete store with associated price list ---");
        storeDetailPage.deleteStoreAndConfirm();

        AssertCollector.assertThat("Incorrect deletion error", storeDetailPage.getH3ErrorMessage(),
            equalTo("Cannot delete a store that has one or more price lists associated with it."), assertionErrorList);

        LOGGER.info("logging error msg ----->" + storeDetailPage.getH3ErrorMessage());

        // Get all price list ids associated with this store
        final List<PriceList> priceLists = new ArrayList<>();
        final PriceListGrid priceListGrid = storeDetailPage.getPriceListGrid();
        final int rowCount = priceListGrid.getTotalRows();
        for (int i = 0; i < rowCount; i++) {
            final PriceList priceList = new PriceList();

            priceList.setId(priceListGrid.getPriceList(i).getId());
            priceList.setName(priceListGrid.getPriceList(i).getName());
            priceList.setExternalKey(priceListGrid.getPriceList(i).getExternalKey());
            priceList.setCurrency(priceListGrid.getPriceList(i).getCurrency());
            priceLists.add(priceList);
        }

        // Delete price list - should get error because a country is referencing it
        storeDetailPage = findStoresPage.findById(storeId);
        LOGGER.info("--- Delete price list with countries assigned ---");
        final PriceListDetailPage priceListPage = storeDetailPage.selectPriceList(0).deleteWithConfirmToHandleError();
        priceListPage.getH3ErrorMessage();

        AssertCollector.assertThat("Incorrect deletion error", priceListPage.getH3ErrorMessage(),
            equalTo("Cannot delete a price list that has one or more countries assigned to it."), assertionErrorList);

        storeDetailPage = findStoresPage.findById(storeId);
        // Delete all assigned countries
        LOGGER.info("--- Delete all countries ---");

        final CountryGrid countryGrid = storeDetailPage.getCountryGrid();
        final int countriesCount = countryGrid.getTotalRows();
        for (int i = 0; i < countriesCount; i++) {
            countryGrid.deleteAssignedCountry(0); // it's always the first row
        }

        AssertCollector.assertThat("Unable to delete all assigned countries", countryGrid.getTotalRows(), equalTo(0),
            assertionErrorList);

        // Validate audit Log for Countries
        StoreAuditLogHelper.helperToValidateDynamoDbForDeletedCountryForStore(storeId,
            DateTimeUtils.getNowAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT), assertionErrorList);

        // Delete all prices lists
        LOGGER.info("--- Delete all price lists ---");

        for (int i = 0; i < priceLists.size(); i++) {
            storeDetailPage = storeDetailPage.selectPriceList(0).deleteWithConfirm();
        }

        AssertCollector.assertThat("Unable to delete price list", storeDetailPage.getPriceListGrid().getTotalRows(),
            is(0), assertionErrorList);

        // Validate audit Log for Price
        StoreAuditLogHelper.helperToValidateDynamoDbForDeletedPriceListForStore(storeId, priceLists,
            assertionErrorList);

        // Delete store
        LOGGER.info("--- Delete store with no association ---");

        final boolean storeDeleted = storeDetailPage.deleteStoreAndConfirm();
        AssertCollector.assertTrue("Unable to delete store", storeDeleted, assertionErrorList);

        // Validate Audit Log Data for Store.
        final boolean deleteStoreAuditLogFound =
            StoreAuditLogHelper.helperToValidateDynamoDbForStore(storeId, storeExtKey, null, storeName, null,
                storeTypeId, null, Status.NEW, null, null, null, null, null, assertionErrorList);

        AssertCollector.assertTrue("Delete Store Audit Log not found for Store id : " + storeId,
            deleteStoreAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
