package com.autodesk.bsm.pelican.ui.storetype;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.AddStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.FindStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.StoreTypeDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.StoreTypeSearchResultsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

/**
 * This is a test class to find a store type
 *
 * @author vineel
 */
public class FindStoreTypeTest extends SeleniumWebdriver {

    private static String actualId;
    private static String actualName;
    private static String actualExtKey;
    private static FindStoreTypePage findStoreTypePage;
    private static StoreTypeSearchResultsPage storeTypeSearchResultsPage;
    private static StoreTypeDetailsPage storeTypeDetailPage;
    private final Random random = new Random();

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findStoreTypePage = adminToolPage.getPage(FindStoreTypePage.class);
        storeTypeSearchResultsPage = adminToolPage.getPage(StoreTypeSearchResultsPage.class);
        storeTypeDetailPage = adminToolPage.getPage(StoreTypeDetailsPage.class);
        final AddStoreTypePage addStoreTypePage = adminToolPage.getPage(AddStoreTypePage.class);

        // Add a Store Type
        final int i = random.nextInt(10000);
        final String name = "New Store Type " + i;
        final String extKey = "New Store Type " + i;
        addStoreTypePage.addStoreType(name, extKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();
        actualId = storeTypeDetailPage.getId();
        actualName = storeTypeDetailPage.getName();
        actualExtKey = storeTypeDetailPage.getExternalKey();
    }

    /**
     * Find store type with invalid id
     *
     * @result empty search result
     */
    @Test
    public void findByInvalidId() {

        final String id = "9999999";
        storeTypeSearchResultsPage = findStoreTypePage.findByInvalidId(id);
        final List<String> notFoundTextList = storeTypeSearchResultsPage.getColumnValuesOfId();
        AssertCollector.assertThat("Found store type by invalid id #" + id, notFoundTextList.get(0),
            equalTo("None found"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find store type by invalid external key
     *
     * @result empty search result
     */
    @Test
    public void findByInvalidExternalKey() {

        final String extKey = "some_invalid_extKey";
        storeTypeSearchResultsPage = findStoreTypePage.findByInvalidExtKey(extKey);
        final List<String> notFoundTextList = storeTypeSearchResultsPage.getColumnValuesOfId();
        AssertCollector.assertThat("Found store type by invalid external key #" + extKey, notFoundTextList.get(0),
            equalTo("None found"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find store type by valid id
     *
     * @result A valid StoreTypeDetailPage
     */
    @Test
    public void findByValidId() {

        storeTypeDetailPage = findStoreTypePage.findByValidId(actualId);
        Util.waitInSeconds(0L);
        commonAssertionsForFindStoreType(storeTypeDetailPage);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find store type by valid ext key
     *
     * @result A valid StoreTypeDetailPage
     */
    @Test
    public void findByValidExtKey() {

        storeTypeDetailPage = findStoreTypePage.findByValidExtKey(actualExtKey);
        Util.waitInSeconds(0L);
        commonAssertionsForFindStoreType(storeTypeDetailPage);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a common method for FindStoreType assertions
     */
    private void commonAssertionsForFindStoreType(final StoreTypeDetailsPage storeTypeDetailPage) {
        AssertCollector.assertThat("Incorrect id of a store type", actualId, equalTo(storeTypeDetailPage.getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect name of a store type", actualName, equalTo(storeTypeDetailPage.getName()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ext key of a store type", actualExtKey,
            equalTo(storeTypeDetailPage.getExternalKey()), assertionErrorList);

    }
}
