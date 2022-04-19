package com.autodesk.bsm.pelican.ui.downloads.features;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FindFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.UploadFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.UploadFeaturesStatusPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Admin Tool's Upload Features tests. On Admin Tool's Main Tab navigate to Catalog -> Features ->Upload Features
 * Validate if the features xlsx file has been uploaded
 *
 * @author Vineel
 */
public class UploadFeaturesTest extends SeleniumWebdriver {

    private static final String FILE_NAME = "UploadFeatures.xlsx";
    private static final String INVALID_FILE_NAME = "invalid_file_name.csv";
    private AdminToolPage adminToolPage;
    private UploadFeaturePage uploadFeaturePage;
    private UploadUtils uploadUtils;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private PelicanPlatform resource;
    private ItemType itemType;
    private ItemType itemType1;
    private String appId;
    private final String FEATURETYPE_NAME = "FeatureType";
    private static FindFeatureTypePage findFeatureTypePage;
    private static FeatureTypeDetailPage featureTypeDetailPage;
    private static FeatureSearchResultsPage featureSearchResultsPage;
    private static FindFeaturePage findFeaturePage;
    private static FeatureDetailPage featureDetailPage;
    private static final String FEATUREHEADER = "CreateFeature";
    private static final String HEADER_TITLE = "Pelican - View Upload Status";
    private String featureId;
    private UploadFeaturesStatusPage uploadFeaturesStatusPage;
    private String uploadStatus;
    private String userId;
    private AuditLogReportHelper auditLogReportHelper;
    private AuditLogReportPage auditLogReportPage;
    private String adminToolUserId;
    private String featureName3;
    private String featureExternalKey3;
    private String newFeatureName;
    private String newFeatureExternalKey;
    private AuditLogReportResultPage auditLogReportResultPage;
    private AddFeaturePage addFeaturePage;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFeaturesTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        userId = getEnvironmentVariables().getUserId();
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);
        addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);
        adminToolUserId = getEnvironmentVariables().getUserId();
        uploadFeaturePage = adminToolPage.getPage(UploadFeaturePage.class);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final Applications applications = resource.application().getApplications();
        if (applications.getApplications().size() > 0) {
            appId = applications.getApplications().get(applications.getApplications().size() - 1).getId();
        }
        String itemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        itemType = resource.itemType().addItemType(appId, itemTypeName);

        itemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        itemType1 = resource.itemType().addItemType(appId, itemTypeName);
        findFeatureTypePage = adminToolPage.getPage(FindFeatureTypePage.class);
        featureTypeDetailPage = adminToolPage.getPage(FeatureTypeDetailPage.class);
        featureSearchResultsPage = adminToolPage.getPage(FeatureSearchResultsPage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);

        featureName3 = "Feature_" + RandomStringUtils.randomAlphanumeric(10);
        featureExternalKey3 = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(10);

        newFeatureName = "Feature_" + RandomStringUtils.randomAlphanumeric(10);
        newFeatureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(10);

    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        if (uploadUtils.deleteFilefromLocal(FILE_NAME)) {
            LOGGER.info(FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(FILE_NAME + " is NOT deleted from /testdata");
        }
    }

    /**
     * Method to upload Features file containing feature type, feature name and feature external key It also verifies
     * Audit log for feature CREATE
     */
    @Test
    public void testFeatureCreatedSuccessfullyByuploadFeaturesFile() throws IOException {

        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName3, itemType.getName(), featureExternalKey3,
            PelicanConstants.YES, false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        // Validate admin tool's upload status page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's upload status page header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);

        // Find Created Feature in the admin tool
        featureDetailPage = findFeaturePage.findFeatureByExternalKey(featureExternalKey3);
        featureId = featureDetailPage.getFeatureId();

        AssertCollector.assertThat("Incorrect feature id is created", featureDetailPage.getFeatureId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name is created", featureDetailPage.getFeatureName(),
            equalTo(featureName3), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key is created",
            featureDetailPage.getFeatureExternalKey(), equalTo(featureExternalKey3), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type for the feature is created",
            featureDetailPage.getFeatureType(), equalTo(itemType.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for the feature created", featureDetailPage.getActive(),
            equalTo(PelicanConstants.YES), assertionErrorList);

        // Verify the CREATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, null, itemType.getId(), null, featureName3,
            null, featureExternalKey3, null, null, Action.CREATE, userId, FILE_NAME, null,
            PelicanConstants.TRUE.toLowerCase(), assertionErrorList);

        // Verify Audit Log report results for feature.
        final HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper
            .verifyAuditLogReportResults(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
                DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
                PelicanConstants.ENTITY_FEATURE, null, featureId, adminToolUserId, Action.CREATE.toString(), null,
                assertionErrorList);
        if (descriptionPropertyValues != null) {
            FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, null,
                featureName3, null, featureExternalKey3, null, itemType.getName() + " (" + itemType.getId() + ")", null,
                PelicanConstants.TRUE.toLowerCase(), assertionErrorList);

            // feature file Name
            AssertCollector.assertTrue("File Name value not found in audit log report",
                descriptionPropertyValues.containsKey(FILE_NAME), assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Audit Log Report Entry not Found in Admin Tool for " + PelicanConstants.ENTITY_FEATURE + featureId,
                descriptionPropertyValues, notNullValue(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test(dependsOnMethods = "testFeatureCreatedSuccessfullyByuploadFeaturesFile")
    public void testFeatureUpdateSuccessfullyByUploadFeaturesFile() throws IOException {

        final String featureName2 = "Feature_Name_" + RandomStringUtils.randomAlphanumeric(10);
        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName2, itemType1.getName(), featureExternalKey3,
            PelicanConstants.NO, false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        // Find Created Feature in the admin tool
        featureDetailPage = findFeaturePage.findFeatureByExternalKey(featureExternalKey3);
        featureId = featureDetailPage.getFeatureId();

        AssertCollector.assertThat("Incorrect feature id is created", featureDetailPage.getFeatureId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name is created", featureDetailPage.getFeatureName(),
            equalTo(featureName2), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key is created",
            featureDetailPage.getFeatureExternalKey(), equalTo(featureExternalKey3), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type for the feature is created",
            featureDetailPage.getFeatureType(), equalTo(itemType1.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for the feature created", featureDetailPage.getActive(),
            equalTo(PelicanConstants.NO), assertionErrorList);

        // Verify the UPDATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, null, itemType.getId(), itemType1.getId(),
            featureName3, featureName2, null, null, null, null, Action.UPDATE, userId, FILE_NAME,
            PelicanConstants.TRUE.toLowerCase(), PelicanConstants.FALSE.toLowerCase(), assertionErrorList);

        // Verify Audit Log report results for feature id.
        final HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper
            .verifyAuditLogReportResults(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
                DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
                PelicanConstants.ENTITY_FEATURE, null, featureId, adminToolUserId, Action.UPDATE.toString(), null,
                assertionErrorList);

        if (descriptionPropertyValues != null) {

            FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, featureName3,
                featureName2, null, null, itemType.getName() + " (" + itemType.getId() + ")",
                itemType1.getName() + " (" + itemType1.getId() + ")", PelicanConstants.TRUE.toLowerCase(),
                PelicanConstants.FALSE.toLowerCase(), assertionErrorList);
            // feature file Name
            AssertCollector.assertTrue("File Name value not found in audit log report",
                descriptionPropertyValues.containsKey(FILE_NAME), assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Audit Log Report Entry not Found in Admin Tool for " + PelicanConstants.ENTITY_FEATURE + featureId,
                descriptionPropertyValues, notNullValue(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method is written to do load test and validating audit logs. This can be used with any number of features.
     * Just need to change the number in for loop
     */
    @Test
    public void test10FeatureCreatedSuccessfullyByUploadFeaturesFile() throws IOException {

        final Map<String, String> featureMap = new HashMap<>();
        boolean append;

        for (int i = 0; i < 10; i++) {

            append = i != 0;
            String status;
            if (i % 2 == 0) {
                status = PelicanConstants.YES;
            } else {
                status = PelicanConstants.NO;
            }
            final String featureName = "Feature_Name_" + RandomStringUtils.randomAlphanumeric(10);
            final String featureExternalKey = "Feature_ExternalKey_" + RandomStringUtils.randomAlphanumeric(10);
            featureMap.put(featureExternalKey, featureName);

            // Create a excel file with feature headers and data
            createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName, itemType.getName(), featureExternalKey,
                status, append);
        }

        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        int count = 0;
        do {
            getDriver().navigate().refresh();
            Wait.pageLoads(getDriver());
            uploadStatus = uploadFeaturesStatusPage.getUploadStatus();

            if (uploadStatus.equals(Status.COMPLETED.toString())) {

                Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
                for (final String featureExternalKey : featureMap.keySet()) {
                    // Find Created Feature in the admin tool
                    featureDetailPage = findFeaturePage.findFeatureByExternalKey(featureExternalKey);
                    featureId = featureDetailPage.getFeatureId();

                    AssertCollector.assertThat("Incorrect feature id is created", featureDetailPage.getFeatureId(),
                        notNullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect feature name is created", featureDetailPage.getFeatureName(),
                        equalTo(featureMap.get(featureExternalKey)), assertionErrorList);
                    AssertCollector.assertThat("Incorrect feature external key is created",
                        featureDetailPage.getFeatureExternalKey(), equalTo(featureExternalKey), assertionErrorList);
                    AssertCollector.assertThat("Incorrect feature type for the feature is created",
                        featureDetailPage.getFeatureType(), equalTo(itemType.getName()), assertionErrorList);

                    final String newStatus = featureDetailPage.getActive();
                    LOGGER.info("New Status :" + newStatus);
                    String newStatusValue = null;
                    if (newStatus.equalsIgnoreCase(PelicanConstants.YES)) {
                        newStatusValue = PelicanConstants.TRUE.toLowerCase();
                    } else if (newStatus.equalsIgnoreCase(PelicanConstants.NO)) {
                        newStatusValue = PelicanConstants.FALSE.toLowerCase();
                    }

                    LOGGER.info("New Status Value :" + newStatusValue);
                    // Verify Audit Log report results for feature.
                    FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, null, itemType.getId(), null,
                        featureMap.get(featureExternalKey), null, featureExternalKey, null, null, Action.CREATE, userId,
                        FILE_NAME, null, newStatusValue, assertionErrorList);

                    // Verify Audit Log report results for feature.
                    final HashMap<String, List<String>> descriptionPropertyValues =
                        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE,
                            null, featureId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

                    if (descriptionPropertyValues != null) {
                        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues,
                            null, featureMap.get(featureExternalKey), null, featureExternalKey, null,
                            itemType.getName() + " (" + itemType.getId() + ")", null, newStatusValue,
                            assertionErrorList);

                        // feature file Name
                        AssertCollector.assertTrue("File Name value not found in audit log report",
                            descriptionPropertyValues.containsKey(FILE_NAME), assertionErrorList);
                    } else {
                        AssertCollector.assertThat("Audit Log Report Entry not Found in Admin Tool for "
                            + PelicanConstants.ENTITY_FEATURE + featureId, descriptionPropertyValues, notNullValue(),
                            assertionErrorList);
                    }
                }
            } else {
                Util.waitInSeconds(TimeConstants.THREE_SEC);
                count++;
            }
        } while (!uploadStatus.equals(Status.COMPLETED.toString()) && count < 3);
        if (uploadStatus.equals(Status.FAILED.toString())) {
            Assert.fail("Upload Feature Failed");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to Upload features file to verify UTF8 FileName
     */
    @Test
    public void testUploadFeatureVerifyUTF8FileName() throws IOException {

        final String itemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        final ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);

        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(10);
        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(10);
        final String UTF_FILE_NAME = "UploadFeatures" + "赛巴巴.xlsx";

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(UTF_FILE_NAME, FEATUREHEADER, featureName, itemType.getName(), featureExternalKey,
            PelicanConstants.NO, false);

        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, UTF_FILE_NAME);

        if (uploadUtils.deleteFilefromLocal(UTF_FILE_NAME)) {
            LOGGER.info(UTF_FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(UTF_FILE_NAME + " is NOT deleted from /testdata");
        }

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        // Validate pagination (i.e. results per page)
        AssertCollector.assertThat(
            "Expected " + UTF_FILE_NAME + " File, but found "
                + uploadFeaturesStatusPage.getColumnValues("File Name").get(0) + " File",
            uploadFeaturesStatusPage.getColumnValues("File Name").get(0), equalTo(UTF_FILE_NAME), assertionErrorList);

        // Find Created Feature in the admin tool
        findFeaturePage.findFeatureByExternalKey(featureExternalKey);

        // Validate the created feature in the admin tool
        AssertCollector.assertThat("Incorrect feature id is created", featureDetailPage.getFeatureId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name is created", featureDetailPage.getFeatureName(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key is created",
            featureDetailPage.getFeatureExternalKey(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type for the feature is created",
            featureDetailPage.getFeatureType(), equalTo(itemType.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for the feature created", featureDetailPage.getActive(),
            equalTo(PelicanConstants.NO), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * A test method to upload the features file without specifying feature header
     */
    @Test
    public void testUploadFeatureWithoutFeatureHeader() throws IOException {

        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create a Xlsx file with required header parameters
        uploadFeaturesStatusPage = createXlsxFileHeaders(FILE_NAME, featureName, featureExternalKey);
        getDriver().navigate().refresh();
        Wait.pageLoads(getDriver());
        uploadStatus = uploadFeaturesStatusPage.getUploadStatus();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect upload job status", uploadStatus,
            equalToIgnoringCase(PelicanConstants.JOB_STATUS_FAILED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * A test method to upload the features file without specifying feature name
     */
    @Test
    public void testUploadFeatureWithoutFeatureName() throws IOException {

        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create a Xlsx file with required header parameters
        uploadFeaturesStatusPage = createXlsxFileHeaders(FILE_NAME, "", featureExternalKey);

        getDriver().navigate().refresh();
        Wait.pageLoads(getDriver());
        uploadStatus = uploadFeaturesStatusPage.getUploadStatus();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect upload job status", uploadStatus,
            equalToIgnoringCase(PelicanConstants.JOB_STATUS_FAILED), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * A test method to upload the features file with non-existing feature type
     */
    @Test
    public void testUploadFeatureWithNonExistingFeatureType() throws IOException {

        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create a Xlsx file with required header parameters
        uploadFeaturesStatusPage = createXlsxFileHeaders(FILE_NAME, featureName, featureExternalKey);

        getDriver().navigate().refresh();
        Wait.pageLoads(getDriver());
        uploadStatus = uploadFeaturesStatusPage.getUploadStatus();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect upload job status", uploadStatus,
            equalToIgnoringCase(PelicanConstants.JOB_STATUS_FAILED), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to upload Features file without specifying the feature external key
     */
    @Test
    public void testFeatureCreatedSuccessfullyWithoutSpecifyingFeatureExternalKey() throws IOException {

        final String itemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        final ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);

        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(10);

        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName, itemType.getName(), "", PelicanConstants.YES,
            false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        // Validate admin tool's upload status page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's upload status page header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);

        // Find Created Feature in the admin tool
        final FeatureTypeSearchResultsPage searchResults =
            findFeatureTypePage.findFeatureTypeByName(itemType.getName());
        searchResults.selectResultRow(1);

        featureTypeDetailPage.clickOnShowAllFeaturesLink();
        final GenericGrid featureSearchResultGrid = featureSearchResultsPage.getFeatureSearchResults();

        // Validate the created feature in the admin tool
        AssertCollector.assertThat("Incorrect number of features added to the feature type",
            featureSearchResultGrid.getTotalItems(), equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify invalid File upload Error message
     */
    @Test
    public void testInvalidFileUploadErrorMessage() {
        uploadUtils.uploadFeature(adminToolPage, INVALID_FILE_NAME);
        final String actualErrorMsg = uploadFeaturePage.getH3ErrorMessage();
        final String expectedErrorMsg = "Please upload a valid XLSX file that is not password-protected.";
        AssertCollector.assertThat("Incorrect error message", actualErrorMsg, equalTo(expectedErrorMsg),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify the view upload status page headers
     */
    @Test
    public void testUploadStatusPageHeadersTest() throws IOException {

        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName, itemType.getName(), featureExternalKey,
            PelicanConstants.YES, false);

        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        LOGGER.info(getDriver().getCurrentUrl());
        final List<String> columnHeaders = uploadFeaturesStatusPage.getColumnHeaders();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        // Validate column headers
        AssertCollector.assertThat("ID header not found", columnHeaders.contains("ID"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Created Date header not found", columnHeaders.contains("Created Date"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("App Family header not found", columnHeaders.contains("App Family"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Last Modified header not found", columnHeaders.contains("Last Modified"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Created By header not found", columnHeaders.contains("Created By"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("File ID header not found", columnHeaders.contains("File Name"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Triggers Job ID header not found", columnHeaders.contains("Triggers Job ID"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Entity Type header not found", columnHeaders.contains("Entity Type"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Status header not found", columnHeaders.contains("Status"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Errors header not found", columnHeaders.contains("Errors"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify upload status page results
     */
    @Test
    public void testUploadStatusPageResultsTest() throws IOException, ParseException {

        final Date currentTime =
            java.util.Calendar.getInstance(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE)).getTime();
        final String featureName = "Feature_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName, itemType.getName(), featureExternalKey,
            PelicanConstants.YES, false);

        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        LOGGER.info(getDriver().getCurrentUrl());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadFeaturesStatusPage.refreshPage();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);
        // Validate pagination (i.e. results per page)
        AssertCollector.assertThat("Incorrect number of results per page",
            uploadFeaturesStatusPage.getColumnValues("ID").size(), lessThanOrEqualTo(20), assertionErrorList);
        // Assertions on the value under Created Date (i.e. most recently
        // created record or not)
        final Date mostRecentCreatedDate =
            dateFormat.parse(uploadFeaturesStatusPage.getColumnValues("Created Date").get(0));
        AssertCollector.assertThat("Incorrect created date", mostRecentCreatedDate, is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Most recently created record not found", mostRecentCreatedDate,
            greaterThanOrEqualTo(currentTime), assertionErrorList);
        // Assertions on the values under each header
        final List<String> actualAppFamilyId = uploadFeaturesStatusPage.getColumnValues("App Family");
        AssertCollector.assertThat("AdminTool: Found uploads other than <b>AUTO Family</b>", actualAppFamilyId,
            everyItem(equalTo("2001")), assertionErrorList);
        AssertCollector.assertThat("Incorrect created by",
            uploadFeaturesStatusPage.getColumnValues("Created By").get(0), equalTo("svc_p_pelican"),
            assertionErrorList);

        AssertCollector.assertThat(
            "Expected " + FILE_NAME + " File, but found " + uploadFeaturesStatusPage.getColumnValues("File Name").get(0)
                + " File",
            uploadFeaturesStatusPage.getColumnValues("File Name").get(0), equalTo(FILE_NAME), assertionErrorList);

        final String entityType = uploadFeaturesStatusPage.getColumnValues("Entity Type").get(0);
        AssertCollector.assertThat("Incorrect entity type",
            entityType.matches("BASIC_OFFERING|SUBSCRIPTION_PLAN|ITEM|SUBSCRIPTION|BIC Release"), equalTo(true),
            assertionErrorList);
        final String status = uploadFeaturesStatusPage.getColumnValues("Status").get(0);
        AssertCollector.assertThat("Incorrect status", status.matches("NOT_STARTED|COMPLETED|FAILED|IN_PROGRESS"),
            equalTo(true), assertionErrorList);

        final List<String> statusList = uploadFeaturesStatusPage.getColumnValues("Status");
        for (int i = 0; i < statusList.size(); i++) {
            if (statusList.get(i).equals(Status.FAILED.toString())) {
                AssertCollector.assertThat("Incorrect errors",
                    uploadFeaturesStatusPage.getColumnValues("Errors").get(i), containsString("View Errors"),
                    assertionErrorList);
                final String actualErrorMessage = uploadFeaturesStatusPage
                    .getValue("errors_" + uploadFeaturesStatusPage.getColumnValues("ID").get(i));
                String expectedErrorMessage = null;
                try {
                    expectedErrorMessage = DbUtils.getUploadErrorMessage(
                        uploadFeaturesStatusPage.getColumnValues("ID").get(i), getEnvironmentVariables());
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                AssertCollector.assertThat("Error message not matched", actualErrorMessage,
                    equalTo(expectedErrorMessage), assertionErrorList);
                break;
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify creation of features with duplicate feature names through upload.
     */
    @Test
    public void testCreateFeaturesWithDuplicateNameThroughUpload() throws IOException {

        final String newItemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        final ItemType newItemType = resource.itemType().addItemType(appId, newItemTypeName);

        // Navigate to the add feature page and add a feature
        final String newFeatureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        addFeaturePage.addFeature(newItemTypeName, newFeatureName, newFeatureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();

        final String featureExternalKey = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureExternalKey1 = "ExternalKey_" + RandomStringUtils.randomAlphanumeric(5);

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, newFeatureName, newItemType.getName(), featureExternalKey,
            PelicanConstants.YES, false);

        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, newFeatureName, newItemType.getName(), featureExternalKey1,
            PelicanConstants.YES, true);
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        LOGGER.info(getDriver().getCurrentUrl());

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);

        AssertCollector.assertThat(
            "Expected " + FILE_NAME + " File, but found " + uploadFeaturesStatusPage.getColumnValues("File Name").get(0)
                + " File",
            uploadFeaturesStatusPage.getColumnValues("File Name").get(0), equalTo(FILE_NAME), assertionErrorList);

        uploadFeaturesStatusPage.refreshPage();

        // Find the feature type by feature type name

        final FeatureTypeSearchResultsPage searchResults =
            findFeatureTypePage.findFeatureTypeByName(newItemType.getName());
        searchResults.selectResultRow(1);

        // click on find features for this type link
        featureTypeDetailPage.clickOnShowAllFeaturesLink();
        final GenericGrid featureSearchResultGrid = featureSearchResultsPage.getFeatureSearchResults();

        // Validate the created feature in the admin tool
        AssertCollector.assertThat("Incorrect number of features added to the feature type",
            featureSearchResultGrid.getTotalItems(), equalTo(3), assertionErrorList);
        featureSearchResultGrid.selectResultRow(1);

        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(),
            equalTo(newFeatureName), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(newFeatureName), assertionErrorList);

        // Validate the created feature through upload
        featureSearchResultsPage.navigateToPreviousMessage();

        featureSearchResultGrid.selectResultRow(2);

        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(),
            equalTo(newFeatureName), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(featureExternalKey), assertionErrorList);

        featureSearchResultsPage.navigateToPreviousMessage();

        // Validate the created feature through upload
        featureSearchResultGrid.selectResultRow(3);

        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(),
            equalTo(newFeatureName), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(featureExternalKey1), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify updation of features with duplicate feature names through upload.
     */
    @Test
    public void testUpdateFeaturesWithDuplicateNameThroughUpload() throws IOException {

        final String newItemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        final ItemType newItemType = resource.itemType().addItemType(appId, newItemTypeName);

        // Navigate to the add feature page and add a feature
        final String newFeatureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        addFeaturePage.addFeature(newItemTypeName, newFeatureName, newFeatureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();

        final String featureName1 = "Feature_Name_" + RandomStringUtils.randomAlphanumeric(5);
        final String featureName2 = "Feature_Name_" + RandomStringUtils.randomAlphanumeric(5);

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName1, newItemType.getName(), newFeatureName,
            PelicanConstants.YES, false);

        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, featureName2, newItemType.getName(), newFeatureName,
            PelicanConstants.YES, true);
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        LOGGER.info(getDriver().getCurrentUrl());

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo(HEADER_TITLE),
            assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", uploadFeaturesStatusPage.getHeader(),
            equalTo("Upload Status"), assertionErrorList);

        AssertCollector.assertThat(
            "Expected " + FILE_NAME + " File, but found " + uploadFeaturesStatusPage.getColumnValues("File Name").get(0)
                + " File",
            uploadFeaturesStatusPage.getColumnValues("File Name").get(0), equalTo(FILE_NAME), assertionErrorList);

        uploadFeaturesStatusPage.refreshPage();

        // Find the feature type by feature type name

        final FeatureTypeSearchResultsPage searchResults =
            findFeatureTypePage.findFeatureTypeByName(newItemType.getName());
        searchResults.selectResultRow(1);

        // click on find features for this type link
        featureTypeDetailPage.clickOnShowAllFeaturesLink();
        final GenericGrid featureSearchResultGrid = featureSearchResultsPage.getFeatureSearchResults();

        // Validate the created feature in the admin tool
        AssertCollector.assertThat("Incorrect number of features added to the feature type",
            featureSearchResultGrid.getTotalItems(), equalTo(1), assertionErrorList);
        featureSearchResultGrid.selectResultRow(1);

        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(), equalTo(featureName2),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(newFeatureName), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with COMPLETED job status
     *
     * @result Valid header and data is returned
     */
    @Test(dependsOnMethods = "testFeatureCreatedSuccessfullyByuploadFeaturesFile")
    public void testFindJobsWithCompletedJobStatusFilter() {

        final UploadFeaturesStatusPage viewUploadStatusPage = adminToolPage.getPage(UploadFeaturesStatusPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_COMPLETED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_ITEMS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails

        viewUploadStatusPage.submit();

        final GenericGrid uploadFeaturesStatusPage = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate job status as 'Completed'
        final List<String> actualJobStatus = uploadFeaturesStatusPage.getColumnValues("Status");
        for (final String status : actualJobStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found jobs other than <b>COMPELTED</b>", status,
                    equalTo(Status.COMPLETED.toString()), assertionErrorList);
            }
        }

        // Assert that job entity is 'ITEM'
        assertEntityType(uploadFeaturesStatusPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with IN PROGRESS job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testFindJobsWithInProgressJobStatusFilter() {

        final UploadFeaturesStatusPage viewUploadStatusPage = adminToolPage.getPage(UploadFeaturesStatusPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_IN_PROGRESS_DROPDOWN);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_ITEMS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails

        viewUploadStatusPage.submit();

        final GenericGrid uploadFeaturesStatusPage = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        final int totalItems = uploadFeaturesStatusPage.getTotalItems();
        AssertCollector.assertThat("Admin Tool: Got no data", totalItems, greaterThanOrEqualTo(0), assertionErrorList);

        if (totalItems > 0) {
            // Validate job status as 'In-progress'
            final List<String> actualJobStatus = uploadFeaturesStatusPage.getColumnValues("Status");
            for (final String status : actualJobStatus) {
                if (!status.isEmpty()) {
                    AssertCollector.assertThat("AdminTool: Found jobs other than <b>IN_PROGRESS</b>", status,
                        equalTo("IN_PROGRESS"), assertionErrorList);
                }
            }
            // Assert that entity type is 'ITEM'
            assertEntityType(uploadFeaturesStatusPage);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with FAILED job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testFindJobsWithFailedJobStatusFilter() {

        final UploadFeaturesStatusPage viewUploadStatusPage = adminToolPage.getPage(UploadFeaturesStatusPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_FAILED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_ITEMS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails

        viewUploadStatusPage.submit();

        final GenericGrid uploadFeaturesStatusPage = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", uploadFeaturesStatusPage.getTotalItems(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate job status as 'Failed'
        final List<String> actualJobStatus = uploadFeaturesStatusPage.getColumnValues("Status");
        for (final String status : actualJobStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found jobs other than <b>FAILED</b>", status,
                    equalTo(Status.FAILED.toString()), assertionErrorList);
            }
        }
        // Assert that job entity type is 'ITEM'
        assertEntityType(uploadFeaturesStatusPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the description in the audit log report.
     */
    @Test
    public void testDescriptionInAuditLogReport() throws IOException {

        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, newFeatureName, itemType.getName(), newFeatureExternalKey,
            PelicanConstants.YES, false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);
        // upload the same file again to update the feature
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        // Find Created Feature in the admin tool
        featureDetailPage = findFeaturePage.findFeatureByExternalKey(newFeatureExternalKey);
        featureId = featureDetailPage.getFeatureId();

        // Verify Audit Log report results for feature.

        auditLogReportHelper.verifyAuditLogReportResults(
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
            PelicanConstants.ENTITY_FEATURE, null, featureId, adminToolUserId, Action.UPDATE.toString(), null,
            assertionErrorList);

        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_FEATURES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the test case to test the description in the download audit log report.
     */
    @Test
    public void testDescriptionInDownloadAuditLogReport() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);

        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, FEATUREHEADER, newFeatureName, itemType.getName(), newFeatureExternalKey,
            PelicanConstants.YES, false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);
        // upload the same file again to update the feature
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        // Find Created Feature in the admin tool
        featureDetailPage = findFeaturePage.findFeatureByExternalKey(newFeatureExternalKey);
        featureId = featureDetailPage.getFeatureId();

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_FEATURE, featureId, adminToolUserId,
            true);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_FEATURES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to assert the job entity
     */
    private void assertEntityType(final GenericGrid result) {
        // Validate job entity as 'ITEM'
        final List<String> actualEntityType = result.getColumnValues("Entity Type");
        for (final String entityType : actualEntityType) {
            AssertCollector.assertThat("AdminTool: Found entity type other than <b>ITEM</b>", entityType,
                equalTo("ITEM"), assertionErrorList);
        }
    }

    /**
     * Method to create and write xls with Items ,Item Types ,Item Group headers and data.
     */
    private void createXlsxAndWriteData(final String FILE_NAME, final String featureHeader, final String featureName,
        final String featureType, final String featureExternalKey, final String active, final boolean append)
        throws IOException {
        final XlsUtils utils = new XlsUtils();

        // Add Feature header values to list
        final ArrayList<String> columnHeaders = new ArrayList<>();
        final ArrayList<String> columnData = new ArrayList<>();

        columnHeaders.add("#CreateFeature,featureName,featureType,externalKey,active");
        columnData.add(featureHeader + "," + featureName + "," + featureType + "," + featureExternalKey + "," + active);

        // Create an excel file with features column headers and column data
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, append);
    }

    /**
     * Method to create a Xlsx file and upload it in the Feature Upload Section
     *
     * @return GenericGrid
     */
    private UploadFeaturesStatusPage createXlsxFileHeaders(final String FILE_NAME, final String featureName,
        final String featureExternalKey) throws IOException {

        // Create a excel file with feature headers and data
        createXlsxAndWriteData(FILE_NAME, "", featureName, itemType.getName(), featureExternalKey, PelicanConstants.YES,
            false);
        // upload created excel file in the admin tool
        uploadFeaturesStatusPage = uploadUtils.uploadFeature(adminToolPage, FILE_NAME);

        return uploadFeaturesStatusPage;
    }

}
