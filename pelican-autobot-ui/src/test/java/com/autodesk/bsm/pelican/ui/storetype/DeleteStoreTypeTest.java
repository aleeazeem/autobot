package com.autodesk.bsm.pelican.ui.storetype;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.AddStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.DeleteStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.StoreTypeDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * This is the test class for deleting a store type in the admin tool
 *
 * @author vineel
 */
public class DeleteStoreTypeTest extends SeleniumWebdriver {

    private final Random random = new Random();
    private static StoreTypeDetailsPage storeTypeDetailPage;
    private static AddStoreTypePage addStoreTypePage;
    private static DeleteStoreTypePage deleteStoreTypePage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        storeTypeDetailPage = adminToolPage.getPage(StoreTypeDetailsPage.class);
        addStoreTypePage = adminToolPage.getPage(AddStoreTypePage.class);
        deleteStoreTypePage = adminToolPage.getPage(DeleteStoreTypePage.class);
    }

    /**
     * Delete a store type that is not associated with a store
     *
     * @result Store type is successfully deleted
     */
    @Test
    public void testDeleteStoreType() {

        final int i = random.nextInt(10000);

        addStoreTypePage.addStoreType("New Store Type " + i, "NEW_STORETYPE_" + i);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);
        final String id = storeTypeDetailPage.getId();
        final String name = storeTypeDetailPage.getName();
        final String extKey = storeTypeDetailPage.getExternalKey();

        deleteStoreTypePage = storeTypeDetailPage.deleteStoreType();

        AssertCollector.assertThat("StoreType is not deleted properly", deleteStoreTypePage.getId(), equalTo(id),
            assertionErrorList);
        AssertCollector.assertThat("StoreType is not deleted properly", deleteStoreTypePage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("StoreType is not deleted properly", deleteStoreTypePage.getExternalKey(),
            equalTo(extKey), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
