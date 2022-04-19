package com.autodesk.bsm.pelican.ui.store;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.StoreAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.CountryPriceList;
import com.autodesk.bsm.pelican.ui.entities.PriceList;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.stores.AddStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.EditPriceListPage;
import com.autodesk.bsm.pelican.ui.pages.stores.EditStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit Store test class.
 *
 * @author t_joshv
 */
public class EditStoreTest extends SeleniumWebdriver {

    private static StoreDetailPage storeDetailPage;
    private static AddStorePage addStorePage;
    private static FindStoresPage findStoresPage;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditStoreTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        storeDetailPage = adminToolPage.getPage(StoreDetailPage.class);
        addStorePage = adminToolPage.getPage(AddStorePage.class);
        findStoresPage = adminToolPage.getPage(FindStoresPage.class);
    }

    /**
     * Validate that store can move from any status to any status
     *
     * @Result: Status is updated successfully
     */

    @Test(dataProvider = "statusData")
    public void verifySuccessEditStoreStatus(final String storeIdForStatusTest, final Status fromStatus,
        final Status toStatus) {

        storeDetailPage = findStoresPage.findById(storeIdForStatusTest);

        // verify that status is equal to toStatus
        AssertCollector.assertThat("Current Status is incorrect", storeDetailPage.getStatus(),
            equalTo(fromStatus.toString()), assertionErrorList);

        final EditStorePage editStorePage = storeDetailPage.editStore();
        editStorePage.editStore(toStatus.getDisplayName(), null, false, null, null, null, null);
        storeDetailPage = editStorePage.clickOnSaveChanges();

        // verify that status is equal to toStatus
        AssertCollector.assertThat("Status Type is not updated", storeDetailPage.getStatus(),
            equalTo(toStatus.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validates the Store and it's properties can be modified.
     *
     * @result Successful
     */
    @Test
    public void testEditStoresPropertiesSuccessfully() {

        final String storeName = "Store " + RandomStringUtils.randomAlphanumeric(8);

        final String storeExtKey = storeName.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();
        final String soldToCsnText = "123456";
        final String storeVat = "10";
        final String expectedStoreVat = "10%";

        StoreDetailPage storeDetailPage =
            StoreHelper.createStore(addStorePage, storeName, storeExtKey, storeTypeName, true);
        final String storeId = storeDetailPage.getId();

        final String editStoreName = storeName + "_Edited";
        final String editStoreExtKey = storeExtKey + "_Edited";

        final EditStorePage editStorePage = storeDetailPage.editStore();

        editStorePage.editStore(Status.ACTIVE.getDisplayName(), null, true, storeVat, soldToCsnText,
            storeName + "_Edited", storeExtKey + "_Edited");

        storeDetailPage = editStorePage.clickOnSaveChanges();

        AssertCollector.assertThat("Incorrect StoreName after Edit", storeDetailPage.getName(), equalTo(editStoreName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Store Ext Key after Edit", storeDetailPage.getExternalKey(),
            equalTo(editStoreExtKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect Vat after Edit", storeDetailPage.getVatPercent(),
            equalTo(expectedStoreVat), assertionErrorList);
        AssertCollector.assertThat("Incorrect soldToCsnText after Edit", storeDetailPage.getSoldToCsn(),
            equalTo(soldToCsnText), assertionErrorList);

        final boolean editStoreAuditLogFound =
            StoreAuditLogHelper.helperToValidateDynamoDbForStore(storeId, storeExtKey, editStoreExtKey, storeName,
                editStoreName, null, null, Status.NEW, Status.ACTIVE, null, null, null, null, assertionErrorList);

        AssertCollector.assertTrue("Edit Store Audit Log not found for Store id : " + storeId, editStoreAuditLogFound,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Edit price list associated with country
     *
     * @result Price list and assigned country is updated accordingly
     */
    @Test
    public void testEditPriceList() {

        // Create store with price list and assign countries
        LOGGER.info("--- Create store ---");
        final String storeName = "Store " + RandomStringUtils.randomAlphanumeric(8);
        final String storeExtKey = storeName.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();

        StoreDetailPage storeDetailPage =
            StoreHelper.createStore(addStorePage, storeName, storeExtKey, storeTypeName, true);
        final String storeId = storeDetailPage.getId();

        // Get existing assigned price list
        final List<PriceList> existingPriceList = new ArrayList<>();
        int totalRows = storeDetailPage.getPriceListGrid().getTotalRows();
        for (int i = 0; i < totalRows; i++) {
            existingPriceList.add(storeDetailPage.getPriceListGrid().getPriceList(i));
        }

        // Get existing assigned countries
        final List<CountryPriceList> existingCountries = new ArrayList<>();
        totalRows = storeDetailPage.getCountryGrid().getTotalRows();
        for (int i = 0; i < totalRows; i++) {
            existingCountries.add(storeDetailPage.getCountryGrid().getAssignedCountry(i));
        }

        // Get existing price lists and update name and external key to have edit as suffix
        totalRows = storeDetailPage.getPriceListGrid().getTotalRows();
        AssertCollector.assertThat("Incorrect total rows for assigned Prices", totalRows,
            equalTo(existingPriceList.size()), assertionErrorList);

        final List<PriceList> priceLists = new ArrayList<>();

        for (int i = 0; i < totalRows; i++) {

            final String storeEditedName = storeDetailPage.getPriceListGrid().getPriceList(i).getName() + "_Edited";
            final String storeEditedExternalKey =
                storeDetailPage.getPriceListGrid().getPriceList(i).getExternalKey() + "_Edited";

            final EditPriceListPage editPriceListPage = storeDetailPage.selectPriceList(i).editPriceList();
            editPriceListPage.editStore(storeEditedName, storeEditedExternalKey);

            storeDetailPage = editPriceListPage.clickOnSubmit();
            storeDetailPage.refreshPage();

            final PriceList actualPriceList = storeDetailPage.getPriceListGrid().getPriceList(i);
            priceLists.add(actualPriceList);

            AssertCollector.assertThat("Incorrect price list name after edit", actualPriceList.getName(),
                equalTo(storeEditedName), assertionErrorList);

            AssertCollector.assertThat("Incorrect price list ext key after edit", actualPriceList.getExternalKey(),
                equalTo(storeEditedExternalKey), assertionErrorList);
        }

        // Validate all countries are updated correctly on the grid
        totalRows = storeDetailPage.getCountryGrid().getTotalRows();
        AssertCollector.assertThat("Incorrect total rows for assigned countries", totalRows,
            equalTo(existingCountries.size()), assertionErrorList);
        boolean foundPriceList;

        for (int i = 0; i < totalRows; i++) {
            final CountryPriceList actualCountry = storeDetailPage.getCountryGrid().getAssignedCountry(i);
            foundPriceList = false;
            for (final PriceList priceList : priceLists) {
                if (priceList.getCurrency().equals(actualCountry.getCurrency())) {
                    foundPriceList = true;
                    AssertCollector.assertThat(
                        "Incorrect price list name for assigned country: " + actualCountry.getAssignedCountry(),
                        actualCountry.getAssignedPriceList(), equalTo(priceList.getName()), assertionErrorList);
                }
            }
            AssertCollector.assertTrue(
                "Unable to find price list in assigned country: " + actualCountry.getAssignedCountry(), foundPriceList,
                assertionErrorList);
        }

        StoreAuditLogHelper.helperToValidateDynamoDbForUpdatedPriceListForStore(storeId, priceLists,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @DataProvider(name = "statusData")
    private Object[][] getStatusData() {

        final String storeName = "Store " + RandomStringUtils.randomAlphanumeric(8);

        final String storeExtKey = storeName.replace(" ", "_").toUpperCase();
        final String storeTypeName = getStoreTypeNameBic();

        storeDetailPage = StoreHelper.createStore(addStorePage, storeName, storeExtKey, storeTypeName, true);
        final String storeIdForStatusTest = storeDetailPage.getId();

        return new Object[][] { { storeIdForStatusTest, Status.NEW, Status.ACTIVE },
                { storeIdForStatusTest, Status.ACTIVE, Status.CANCELED },
                { storeIdForStatusTest, Status.CANCELED, Status.ACTIVE } };
    }
}
