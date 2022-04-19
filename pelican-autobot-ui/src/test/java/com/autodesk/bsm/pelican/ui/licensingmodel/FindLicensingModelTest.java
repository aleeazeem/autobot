package com.autodesk.bsm.pelican.ui.licensingmodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.AddLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.FindLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.LicensingModelDetailPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.LicensingModelSearchResultsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This Test class tests the Add Licensing Model scenarios
 *
 * @author mandas
 */
public class FindLicensingModelTest extends SeleniumWebdriver {

    private FindLicensingModelPage findLicensingModelPage;
    private LicensingModelDetailPage licensingModelDetailPage;
    private LicensingModelSearchResultsPage licensingModelSearchResultsPage;
    private String licenseModelId = null;
    private String name;
    private String description;
    private String externalKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindLicensingModelTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final AddLicensingModelPage addLicensingModelPage = adminToolPage.getPage(AddLicensingModelPage.class);
        findLicensingModelPage = adminToolPage.getPage(FindLicensingModelPage.class);
        licensingModelDetailPage = adminToolPage.getPage(LicensingModelDetailPage.class);
        licensingModelSearchResultsPage = adminToolPage.getPage(LicensingModelSearchResultsPage.class);

        name = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        description = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        externalKey = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        licensingModelDetailPage =
            addLicensingModelPage.addLicencingModel(name, description, externalKey, false, false);
        licenseModelId = licensingModelDetailPage.getId();
    }

    /**
     * Test to Find a Licensing Model
     */
    @Test
    public void testFindLicensingModelByIdSuccess() {

        licensingModelDetailPage = findLicensingModelPage.findById(licenseModelId);
        AssertCollector.assertThat("Could NOT find Licensing Model ID", licensingModelDetailPage.getId(),
            equalTo(licenseModelId), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Name did not match", licensingModelDetailPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Description did not match",
            licensingModelDetailPage.getDescription(), equalTo(description), assertionErrorList);
        AssertCollector.assertThat("Licensing Model External Key did not match",
            licensingModelDetailPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to Find a Licensing Model
     */
    @Test
    public void testFindLicensingModelByExternalKeySuccess() {

        licensingModelDetailPage = findLicensingModelPage.findByExternalKey(externalKey);
        AssertCollector.assertThat("Could NOT find Licensing Model ID", licensingModelDetailPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Name did not match", licensingModelDetailPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Description did not match",
            licensingModelDetailPage.getDescription(), equalTo(description), assertionErrorList);
        AssertCollector.assertThat("Licensing Model External Key did not match",
            licensingModelDetailPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Default Find for Licensing Model without ID
     */
    @Test
    public void testFindLicensingModelWithEmptyId() {

        licensingModelSearchResultsPage = findLicensingModelPage.findByEmptyId();
        final List<String> expHeaders = new ArrayList<>();
        expHeaders.add(PelicanConstants.ID_FIELD);
        expHeaders.add(PelicanConstants.EXTERNAL_KEY_FIELD);
        expHeaders.add(PelicanConstants.NAME_FIELD);

        AssertCollector.assertThat("Incorrect column headers for Licensing Model search results page", expHeaders,
            equalTo(licensingModelSearchResultsPage.getGrid().getColumnHeaders()), assertionErrorList);

        licensingModelSearchResultsPage.getLastPage();

        final int licensingModelResultCount =
            licensingModelSearchResultsPage.getColumnValues(PelicanConstants.NAME_FIELD).size();
        LOGGER.info("Number of LicensingModel results: " + licensingModelResultCount);

        GenericDetails licensingModelDetailPage = null;
        if (licensingModelResultCount > 1) {
            // Click on Licensing Model with NAME
            for (int i = 0; i <= licensingModelResultCount - 1; i++) {
                if (licensingModelSearchResultsPage.getColumnValues(PelicanConstants.NAME_FIELD).get(i).equals(name)) {
                    licensingModelDetailPage =
                        licensingModelSearchResultsPage.clickResultColumnWithName(PelicanConstants.NAME_FIELD, i);
                }
            }
        } else if (licensingModelResultCount == 1) {
            if (licensingModelSearchResultsPage.getColumnValues(PelicanConstants.NAME_FIELD).get(0).equals(name)) {
                licensingModelDetailPage =
                    licensingModelSearchResultsPage.clickResultColumnWithName(PelicanConstants.NAME_FIELD, 0);
            }
        } else {
            // This should never happen as we create Licensing Model in BeforeClass
            Assert.fail("Total License Model Rows in Search Page are 0");
        }

        if (licensingModelDetailPage.getHeader().equals(PelicanConstants.LICENSING_MODEL_HEADER)) {
            AssertCollector.assertThat("Incorrect Licensing Model ID",
                licensingModelDetailPage.getFieldValueByKey(PelicanConstants.ID_FIELD), equalTo(licenseModelId),
                assertionErrorList);
        } else {
            Assert.fail("Incorrect page header");
        }

        AssertCollector.assertAll(assertionErrorList);
    }
}
