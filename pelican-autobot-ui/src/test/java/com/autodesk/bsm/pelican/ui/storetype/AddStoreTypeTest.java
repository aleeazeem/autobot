package com.autodesk.bsm.pelican.ui.storetype;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.AddStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.StoreTypeDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Test Store Type creation, edition and deletion
 *
 * @author vineel
 */
public class AddStoreTypeTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private final Random random = new Random();
    private static StoreTypeDetailsPage storeTypeDetailPage;
    private static AddStoreTypePage addStoreTypePage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        storeTypeDetailPage = adminToolPage.getPage(StoreTypeDetailsPage.class);
        addStoreTypePage = adminToolPage.getPage(AddStoreTypePage.class);
    }

    /**
     * Add store type
     *
     * @result Store type is created successfully
     */
    @Test
    public void testAddStoreType() {

        final int i = random.nextInt(10000);
        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;
        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);

        // Validate store type is added successfully
        commonAssertionsForAddStoreType(name, extKey);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Create Store Type with duplicated name
     *
     * @result Store Type is successfully created
     */
    @Test
    public void testAddDuplicateName() {

        final int i = random.nextInt(10000);
        final String name = "New Store Type " + i;
        final String extKey = name.replace(" ", "_").toUpperCase();

        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);

        // Create the duplicate store type with different ext key
        final String newExtKey = "dup_ext_key_"
            + DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_NO_SEPARATOR) + "_" + random.nextInt(100);
        addStoreTypePage.addStoreType(name, newExtKey);
        addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);

        // Validate store type is added successfully
        commonAssertionsForAddStoreType(name, newExtKey);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Create Store Type with duplicated external key
     *
     * @result Should not be able to save with duplicated external key
     */
    @Test
    public void testAddDuplicateExternalKey() {

        final int i = random.nextInt(10000);
        final String name = "New Store Type " + i;
        final String extKey = name.replace(" ", "_").toUpperCase();

        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);

        // Create the duplicate store type with different ext key
        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();
        Util.waitInSeconds(0L);

        AssertCollector.assertThat("Incorrect error message when adding duplicated ext key",
            addStoreTypePage.getH3ErrorMessage(), equalTo("This value is already in use by another store type."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate required fields
     *
     * @result Empty required fields are flagged
     */
    @Test
    public void testrequiredFields() {

        final String expError = "Required";

        final String name = "";
        final String extKey = "";
        final AddStoreTypePage addStoreTypePage = adminToolPage.getPage(AddStoreTypePage.class);
        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();

        AssertCollector.assertThat("Name: incorrect error message when required field is not supplied",
            addStoreTypePage.getNameErrorMessage(), equalTo(expError), assertionErrorList);
        AssertCollector.assertThat("Ext Key: incorrect error message when required field is not supplied",
            addStoreTypePage.getExtKeyErrorMessage(), equalTo(expError), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method for common assertions for a store type
     *
     * @param External Key
     */
    private void commonAssertionsForAddStoreType(final String name, final String extKey) {
        AssertCollector.assertThat("Incorrect store type id", storeTypeDetailPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store type name", storeTypeDetailPage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store type external key", storeTypeDetailPage.getExternalKey(),
            equalTo(extKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect store type created date", storeTypeDetailPage.getCreated(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store type last modified", storeTypeDetailPage.getLastModified(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store type application family",
            storeTypeDetailPage.getApplicationFamily(), notNullValue(), assertionErrorList);
    }
}
