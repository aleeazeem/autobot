package com.autodesk.bsm.pelican.ui.downloads.basicoffering;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.EditBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.FindBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.reports.BasicOfferingsReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Admin Tool's Basic Offerings Reports tests.
 * <p>
 * Validate the correct header and data text are in csv format. This test will run in AUTO application family for
 * BIC-STORE-DEV and DEV_NEW *****************PLEASE READ THIS IF THE TEST IS FAILING**************
 * ******************(VERY IMPORTANT)********************************** For the test to run successfully it assumes few
 * pre-created data Please create the descriptors according to /pelican/testdata/basic_offering_upload.csv
 *
 * @author Kishor
 */
public class BasicOfferingReportsTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private final String AUTO_STATUS_HEADER = "OfferingStatus";
    private final String AUTO_STORE_TYPE_HEADER = "StoreType";
    private String AUTO_PRODUCT_LINE = "AUTO_PROD_LINE";
    private FindBasicOfferingPage findBasicOfferingPage;
    private BasicOfferingsReportPage basicOfferingReportPage;
    private BasicOfferingApiUtils basicOfferingApiUtils;

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
        basicOfferingReportPage = adminToolPage.getPage(BasicOfferingsReportPage.class);
        basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        createBasicOfferingsData();

    }

    /**
     * Verify the basic Offering report without any filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithoutFilters() {

        basicOfferingReportPage.navigateToPage();

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        // Waits for all select boxes to grab the data from ajax calls.
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);
        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with active status filter
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithActiveStatusFilter() {

        // Check only New checkbox keeping other statuses unchecked
        basicOfferingReportPage.deactivateNewCheckbox();
        basicOfferingReportPage.deactivateCanceledCheckbox();
        basicOfferingReportPage.activateActiveCheckbox();

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualOfferingStatus = basicOfferingReportPage.getReportValues(AUTO_STATUS_HEADER);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>Active Status</b>", actualOfferingStatus,
            everyItem(equalTo(Status.ACTIVE.getDisplayName())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with new status filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithNewStatusFilter() {

        // Check only New checkbox keeping other statuses unchecked
        basicOfferingReportPage.activateNewCheckbox();
        basicOfferingReportPage.deactivateCanceledCheckbox();
        basicOfferingReportPage.deactivateActiveCheckbox();

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualOfferingStatus = basicOfferingReportPage.getReportValues(AUTO_STATUS_HEADER);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>New Status</b>", actualOfferingStatus,
            everyItem(equalTo(Status.NEW.getDisplayName())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with canceled status filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithCanceledStatusFilter() {

        final GenericGrid genericGrid =
            findBasicOfferingPage.findBasicOfferingByAdvancedFind(null, null, null, false, false, true);
        if (genericGrid.getTotalItems() == 0) {
            final Offerings basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
                OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);

            final BasicOfferingDetailPage basicOfferingPage =
                findBasicOfferingPage.findBasicOfferingById(basicOffering.getOfferings().get(0).getId());
            basicOfferingPage.edit();
            final EditBasicOfferingPage editBasicOffering = adminToolPage.getPage(EditBasicOfferingPage.class);
            editBasicOffering.editStatus(Status.CANCELED);
            editBasicOffering.clickOnSave();
        }

        // Check only Cancel checkbox keeping other statuses unchecked
        basicOfferingReportPage.activateCanceledCheckbox();
        basicOfferingReportPage.deactivateNewCheckbox();
        basicOfferingReportPage.deactivateActiveCheckbox();
        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualOfferingStatus = basicOfferingReportPage.getReportValues(AUTO_STATUS_HEADER);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>Canceled Status</b>", actualOfferingStatus,
            everyItem(equalToIgnoringCase(Status.CANCELED.toString())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with product line filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithProductLineFilter() {

        // Check all statuses checked
        basicOfferingReportPage.activateCanceledCheckbox();
        basicOfferingReportPage.activateNewCheckbox();
        basicOfferingReportPage.activateActiveCheckbox();

        basicOfferingReportPage.selectProductLine(AUTO_PRODUCT_LINE + " (" + AUTO_PRODUCT_LINE + ")");
        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualOfferingProductLine =
            basicOfferingReportPage.getReportValues(PelicanConstants.DOWNLOAD_PRODUCT_LINE);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>AUTO_PRODUCT_LINE productline</b>",
            actualOfferingProductLine, everyItem(equalTo(AUTO_PRODUCT_LINE)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with store type filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithStoreTypeFilter() {

        // Check all statuses checked
        basicOfferingReportPage.activateCanceledCheckbox();
        basicOfferingReportPage.activateNewCheckbox();
        basicOfferingReportPage.activateActiveCheckbox();
        // Select the storeType in the storeType drop down box
        basicOfferingReportPage.selectStoreType(getStoreTypeNameBic());
        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualOfferingStoreType = basicOfferingReportPage.getReportValues(AUTO_STORE_TYPE_HEADER);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>AUTO_STORE_TYPE store Type</b>",
            actualOfferingStoreType, everyItem(equalTo(getStoreTypeNameBic())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report with Store type and store filter set
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testBasicOfferingReportWithStoreTypeAndStoreFilter() {

        // Check all statuses checked
        basicOfferingReportPage.activateCanceledCheckbox();
        basicOfferingReportPage.activateNewCheckbox();
        basicOfferingReportPage.activateActiveCheckbox();
        // Select the storeType in the storeType drop down box
        basicOfferingReportPage.selectStoreType(getStoreTypeNameBic());
        basicOfferingReportPage.selectStore(getStoreExternalKeyUs());
        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = basicOfferingReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", basicOfferingReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualStoreType = basicOfferingReportPage.getReportValues(AUTO_STORE_TYPE_HEADER);
        final List<String> actualStores = basicOfferingReportPage.getReportValues(PelicanConstants.STORE_FIELD);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>AUTO_STORE_TYPE store Type</b>",
            actualStoreType, everyItem(equalTo(getStoreTypeNameBic())), assertionErrorList);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>AUTO_STORE store</b>", actualStores,
            everyItem(equalTo(getStoreExternalKeyUs())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the basic Offering report without any status filter set
     *
     * @result an error message which says "At least one status must be selected." should be returned
     */
    @Test
    public void testBasicOfferingReportWithoutAnyStatus() {

        // Check all statuses unchecked and verify the error while submit
        basicOfferingReportPage.deactivateCanceledCheckbox();
        basicOfferingReportPage.deactivateNewCheckbox();
        basicOfferingReportPage.deactivateActiveCheckbox();

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        basicOfferingReportPage.submit(TimeConstants.ONE_SEC);

        // Validate the error message from the
        AssertCollector.assertThat("Admin Tool: Invalid Error message", basicOfferingReportPage.getH3ErrorMessage(),
            equalTo("At least one status must be selected."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Utility method to get the expected headers to be validated against the report headers Need to be updated
     * according to the valid headers in proper order
     *
     * @return the valid header column names
     */
    private String getExpectedHeader() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OfferingExternalKey,OfferingName,ProductLine,OfferingType,OfferingStatus,OfferingSupportLevel,");
        sb.append("MediaType,Language,OfferingDetailName,TaxCode,Amount,Currency,PriceList,Store,StoreType,");
        sb.append("PriceStartDate,PriceEndDate,PriceId");
        return sb.toString();
    }

    private void createBasicOfferingsData() {
        final Offerings createdOfferings = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        AUTO_PRODUCT_LINE = createdOfferings.getOfferings().get(0).getProductLine();
    }

}
