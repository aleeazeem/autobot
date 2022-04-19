package com.autodesk.bsm.pelican.ui.licensingmodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.AddLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.EditLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.FindLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.LicensingModelDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This Test class tests the Edit Licensing Model scenarios
 *
 * @author mandas
 */
public class EditLicensingModelTest extends SeleniumWebdriver {

    private EditLicensingModelPage editLicensingModelPage;
    private FindLicensingModelPage findLicensingModelPage;
    private LicensingModelDetailPage licensingModelDetailPage;
    private String licenseModelID = null;
    private String name;
    private String description;
    private static String externalKey;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final AddLicensingModelPage addLicensingModelPage = adminToolPage.getPage(AddLicensingModelPage.class);
        editLicensingModelPage = adminToolPage.getPage(EditLicensingModelPage.class);
        findLicensingModelPage = adminToolPage.getPage(FindLicensingModelPage.class);
        licensingModelDetailPage = adminToolPage.getPage(LicensingModelDetailPage.class);

        name = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        description = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        externalKey = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        licensingModelDetailPage =
            addLicensingModelPage.addLicencingModel(name, description, externalKey, false, false);
        licenseModelID = licensingModelDetailPage.getId();
    }

    /**
     * Test to Edit a Licensing Model with valid data
     */
    @Test
    public void testEditLicensingModelSuccess() {

        licensingModelDetailPage = findLicensingModelPage.findById(licenseModelID);
        editLicensingModelPage = licensingModelDetailPage.clickOnEdit();
        licensingModelDetailPage = editLicensingModelPage.editLicencingModel(name + PelicanConstants.APPEND_EDIT_TEXT,
            description + PelicanConstants.APPEND_EDIT_TEXT, externalKey + PelicanConstants.APPEND_EDIT_TEXT, false,
            false);
        licenseModelID = licensingModelDetailPage.getId();
        AssertCollector.assertThat("Could NOT find Product Line ID", licensingModelDetailPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Licensing Model Name did not match", licensingModelDetailPage.getName(),
            equalTo(name + PelicanConstants.APPEND_EDIT_TEXT), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Description did not match",
            licensingModelDetailPage.getDescription(), equalTo(description + PelicanConstants.APPEND_EDIT_TEXT),
            assertionErrorList);
        AssertCollector.assertThat("Licensing Model External Key did not match",
            licensingModelDetailPage.getExternalKey(), equalTo(externalKey + PelicanConstants.APPEND_EDIT_TEXT),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to Edit a Licensing Model with invalid data
     */
    @Test(dataProvider = "dataForEditLicensingModel")
    public void testEditLicensingModelErrorScenarios(final String name, final String externalKey) {

        licensingModelDetailPage = findLicensingModelPage.findById(licenseModelID);
        editLicensingModelPage = licensingModelDetailPage.clickOnEdit();
        licensingModelDetailPage =
            editLicensingModelPage.editLicencingModel(name, description, externalKey, false, false);
        AssertCollector.assertThat("Edit Licensing Model with empty fields is Successful!",
            editLicensingModelPage.getErrorMessageFromFormHeader(),
            equalTo(PelicanErrorConstants.INVALID_LICENSING_MODEL_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider to return data required to Licensing Model error scenarios
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "dataForEditLicensingModel")
    public static Object[][] editLicensingModel() {
        return new Object[][] { { "", externalKey }, { externalKey, "" } };
    }

}
