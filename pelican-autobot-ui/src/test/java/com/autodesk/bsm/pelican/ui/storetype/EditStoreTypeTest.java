package com.autodesk.bsm.pelican.ui.storetype;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.storetype.AddStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.EditStoreTypePage;
import com.autodesk.bsm.pelican.ui.pages.storetype.StoreTypeDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * This is a test class for editing a store type
 *
 * @author vineel
 */
public class EditStoreTypeTest extends SeleniumWebdriver {

    private final Random random = new Random();
    private static StoreTypeDetailsPage storeTypeDetailPage;
    private static AddStoreTypePage addStoreTypePage;
    private static EditStoreTypePage editStoreTypePage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        storeTypeDetailPage = adminToolPage.getPage(StoreTypeDetailsPage.class);
        addStoreTypePage = adminToolPage.getPage(AddStoreTypePage.class);
        editStoreTypePage = adminToolPage.getPage(EditStoreTypePage.class);
    }

    /**
     * Edit store type with new name.
     *
     * @result Store type is updated with new name successfully
     */
    @Test
    public void testEditNewName() {

        // Create a new store type
        final int i = random.nextInt(10000);

        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;

        addStoreTypePage.addStoreType(name, extKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();
        final String oldStoreTypeName = storeTypeDetailPage.getName();
        storeTypeDetailPage.editStoreType();
        final String newName = name.replace(Status.NEW.getDisplayName(), "Edited");
        editStoreTypePage.editStoreType(newName, extKey);
        editStoreTypePage.clickOnSubmit();
        final String newStoreTypeName = storeTypeDetailPage.getName();

        AssertCollector.assertThat("Incorrect old store type name", oldStoreTypeName, equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store type edited name", newStoreTypeName, equalTo(newName),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Edit store type with existing name
     *
     * @result Store type is updated with duplicated name successfully
     */
    @Test
    public void testEditDuplicatedName() {

        // Create a new store type
        final int i = random.nextInt(10000);

        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;

        addStoreTypePage.addStoreType(name, extKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();
        final String oldStoreTypeName = storeTypeDetailPage.getName();
        storeTypeDetailPage.editStoreType();
        editStoreTypePage.editStoreType(name, extKey);
        editStoreTypePage.clickOnSubmit();
        final String newStoreTypeName = storeTypeDetailPage.getName();

        AssertCollector.assertThat("Incorrect old store type name", oldStoreTypeName, equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store type edited name", newStoreTypeName, equalTo(name),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Edit store type with new external key
     *
     * @result Store type is updated with external key successfully
     */
    @Test
    public void testEditNewExternalKey() {

        // Create a new store type
        final int i = random.nextInt(10000);

        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;

        addStoreTypePage.addStoreType(name, extKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();

        final String oldStoreTypeExtKey = storeTypeDetailPage.getExternalKey();
        storeTypeDetailPage.editStoreType();
        final String newExtKey = name.replace(Status.NEW.getDisplayName(), "Edited");
        editStoreTypePage.editStoreType(name, newExtKey);
        editStoreTypePage.clickOnSubmit();
        final String newStoreTypeExtKey = storeTypeDetailPage.getExternalKey();

        AssertCollector.assertThat("Incorrect old store type name", oldStoreTypeExtKey, equalTo(extKey),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store type edited name", newStoreTypeExtKey, equalTo(newExtKey),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Edit store type with existing external key
     *
     * @result Not allow. Should get error message that this is an existing external key
     */
    @Test
    public void testEditDuplicatedExternalKey() {

        // Create a new store type
        final int i = random.nextInt(10000);

        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;

        addStoreTypePage.addStoreType(name, extKey);
        addStoreTypePage.clickOnSubmit();

        final int j = random.nextInt(10000);

        final String newName = "New Store Type " + j;
        final String newExtKey = "NEW_STORETYPE_" + j;

        addStoreTypePage.addStoreType(newName, newExtKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();

        // Edit the existing store type
        storeTypeDetailPage.editStoreType();
        editStoreTypePage.editStoreType(name, extKey);
        editStoreTypePage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect error message when editing external key to an existing key",
            editStoreTypePage.getExtKeyErrorMessage(), equalTo("This value is already in use by another store type."),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Edit store type and remove name and required fields
     *
     * @result Not all. Should get error message that these are required fields
     */
    @Test
    public void testremoveRequiredFieldsInStoreType() {

        final String expError = "Required";

        // Create a new store type
        final int i = random.nextInt(10000);

        final String name = "New Store Type " + i;
        final String extKey = "NEW_STORETYPE_" + i;

        addStoreTypePage.addStoreType(name, extKey);
        storeTypeDetailPage = addStoreTypePage.clickOnSubmit();

        // Get # of existing store type
        storeTypeDetailPage.editStoreType();
        editStoreTypePage.editStoreType("", "");
        editStoreTypePage.clickOnSubmit();

        AssertCollector.assertThat("Name: incorrect error message when required field is not supplied",
            editStoreTypePage.getNameErrorMessage(), equalTo(expError), assertionErrorList);
        AssertCollector.assertThat("Ext Key: incorrect error message when required field is not supplied",
            editStoreTypePage.getExtKeyErrorMessage(), equalTo(expError), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
