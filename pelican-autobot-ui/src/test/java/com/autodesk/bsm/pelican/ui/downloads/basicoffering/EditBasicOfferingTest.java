package com.autodesk.bsm.pelican.ui.downloads.basicoffering;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.AddBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.EditBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class EditBasicOfferingTest extends SeleniumWebdriver {

    private static String productLineNameAndExternalKey;
    private static AddBasicOfferingPage addBasicOfferingPage;
    private static EditBasicOfferingPage editBasicOfferingPage;
    private static BasicOfferingDetailPage basicOfferingDetailPage;
    private static AuditLogReportHelper auditLogReportHelper;
    private static AuditLogReportResultPage auditLogReportResultPage;
    private static AuditLogReportPage auditLogReportPage;
    private static String adminToolUserId;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        // Initiating the environment and the appFamily set to AUTO
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        productLineNameAndExternalKey =
            getProductLineExternalKeyRevit() + " (" + getProductLineExternalKeyRevit() + ")";

        addBasicOfferingPage = adminToolPage.getPage(AddBasicOfferingPage.class);
        editBasicOfferingPage = adminToolPage.getPage(EditBasicOfferingPage.class);

        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        adminToolUserId = getEnvironmentVariables().getUserId();
    }

    /**
     * This is a test which will test whether product line is a required field in edit basic offering
     */
    @Test
    public void testProductLineAsRequiredInEditBasicOffering() {

        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey,
            RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(8), MediaType.DVD, null, null,
            UsageType.COM, null, null);
        addBasicOfferingPage.clickOnSave();
        editBasicOfferingPage.clickOnEditOfferingButton();
        editBasicOfferingPage.editProductLine(null);
        editBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final String productLineErrorMessage = editBasicOfferingPage.getProductLineErrorMessage();
        commonAssertionsForErrorMessages(productLineErrorMessage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the random external key if no external key is provided by the user when
     * editing a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testRandomExternalKeyGeneratedInEditBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = editBasicOffering("");
        final HashMap<String, String> offeringFieldsMap = getFieldsFromBasicOffering(basicOfferingDetailPage);
        commonAssertionsForBasicOfferingFields(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, status, offeringFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the user entered external key if external key is provided by the user
     * when editing a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testManuallyEnteredExternalKeyGenerationInEditBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        final String newName = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = editBasicOffering(newName);
        final HashMap<String, String> offeringFieldsMap = getFieldsFromBasicOffering(basicOfferingDetailPage);
        commonAssertionsForBasicOfferingFields(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, newName,
            MediaType.DVD, null, status, offeringFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the error message for external key if external key is provided by the
     * user already exists when editing a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testDuplicateExternalKeyGeneratedInEditBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        final String newName = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, status, null, null, null);
        editBasicOffering(newName);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, newName, null,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = editBasicOffering(newName);
        final String externalKeyErrorMessage = editBasicOfferingPage.getExternalKeyErrorMessage();
        AssertCollector.assertThat("Incorrect external key error message for duplicate basic offering external key",
            externalKeyErrorMessage, equalTo(PelicanErrorConstants.DUPLICATE_VALUE_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the description in the audit log report.
     *
     */
    @Test
    public void testDescriptionInAuditLog() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, Status.ACTIVE, null, null, null);
        addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        editBasicOfferingPage.clickOnEditOfferingButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        basicOfferingDetailPage = editBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final String offeringId = basicOfferingDetailPage.getId();
        // Query Audit Log Report for each subscription plan1
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
            offeringId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);
        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the description in the download audit log report.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testDescriptionInDownloadAuditLogReport() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, Status.ACTIVE, null, null, null);
        addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        editBasicOfferingPage.clickOnEditOfferingButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        basicOfferingDetailPage = editBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final String offeringId = basicOfferingDetailPage.getId();

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, offeringId,
            adminToolUserId, true);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the common method for assertions of the required fields on the basic offering page
     *
     * @param productLineErrorMessage - Error message displayed on the product line field
     */
    private void commonAssertionsForErrorMessages(final String productLineErrorMessage) {
        AssertCollector.assertThat("Incorrect error message for required product line field", productLineErrorMessage,
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
    }

    /**
     * This method stores the fields in a basic offering into a hashmap
     *
     * @return HashMap<String,String>
     */
    private HashMap<String, String> getFieldsFromBasicOffering(final BasicOfferingDetailPage basicOfferingDetailPage) {

        final HashMap<String, String> offeringFieldsMap = new HashMap<>();
        offeringFieldsMap.put("Id", basicOfferingDetailPage.getId());
        offeringFieldsMap.put("Name", basicOfferingDetailPage.getName());
        offeringFieldsMap.put("ExternalKey", basicOfferingDetailPage.getExternalKey());
        offeringFieldsMap.put("OfferingType", basicOfferingDetailPage.getOfferingType());
        offeringFieldsMap.put("Status", basicOfferingDetailPage.getStatus());
        offeringFieldsMap.put("MediaType", basicOfferingDetailPage.getMediaType());
        offeringFieldsMap.put("UsageType", basicOfferingDetailPage.getUsageType());
        offeringFieldsMap.put("ProductLine", basicOfferingDetailPage.getProductLine());
        offeringFieldsMap.put("OfferingDetail", basicOfferingDetailPage.getOfferingDetail());

        return offeringFieldsMap;

    }

    /**
     * This is a method which will assert the fields on the basic offering page
     */
    private void commonAssertionsForBasicOfferingFields(final OfferingType offeringType, final String productLine,
        final String name, final String externalKey, final MediaType mediaType, final String offeringDetail,
        final Status status, final HashMap<String, String> offeringFieldsMap) {

        AssertCollector.assertThat("Incorrect id in the basic offering", offeringFieldsMap.get("Id"), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect name of the basic offering", offeringFieldsMap.get("Name"), equalTo(name),
            assertionErrorList);
        if (externalKey == null) {
            AssertCollector.assertThat("Incorrect external key prefix for the basic plan",
                offeringFieldsMap.get("ExternalKey").split("-")[0], equalTo("BO"), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key length for the basic offering",
                offeringFieldsMap.get("ExternalKey").split("-")[1].length(), equalTo(12), assertionErrorList);

        } else {
            AssertCollector.assertThat("Incorrect external key of the basic offering",
                offeringFieldsMap.get("ExternalKey"), equalTo(externalKey), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect offering type of the basic offering",
            offeringFieldsMap.get("OfferingType"), equalTo(offeringType.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the basic offering", offeringFieldsMap.get("Status"),
            equalTo(status.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect media type of the basic offering", offeringFieldsMap.get("MediaType"),
            equalTo(mediaType.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering detail of the basic offering",
            offeringFieldsMap.get("OfferingDetail"), equalTo(offeringDetail), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line of the basic offering",
            offeringFieldsMap.get("ProductLine").split(" ")[0], equalTo(productLine.split(" ")[0]), assertionErrorList);

    }

    /**
     * This method will edit the external key of a basic offering
     *
     * @return BasicOfferingDetailPage
     */
    private BasicOfferingDetailPage editBasicOffering(final String externalKey) {
        addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        editBasicOfferingPage.clickOnEditOfferingButton();
        editBasicOfferingPage.editExternalKey(externalKey);
        basicOfferingDetailPage = editBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        return basicOfferingDetailPage;
    }

    @DataProvider(name = "BasicOfferingStatuses")
    public Object[][] getBasicOfferingStatuses() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE } };
    }
}
