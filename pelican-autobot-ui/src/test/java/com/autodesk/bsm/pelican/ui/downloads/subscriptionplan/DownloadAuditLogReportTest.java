package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This test class test the Download Functionality of Audit Log Report
 *
 * @author Shweta Hegde
 */
public class DownloadAuditLogReportTest extends SeleniumWebdriver {

    private AuditLogReportPage auditLogReportPage;
    private String adminToolUserId;
    private AuditLogReportHelper auditLogReportHelper;
    private boolean featureFlagValue;
    private static final int NUMBER_OF_COLUMNS = 12;
    private static final int NUMBER_OF_COLUMNS_WITH_FEATURE_FLAG = 11;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadAuditLogReportTest.class.getSimpleName());

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        adminToolUserId = getEnvironmentVariables().getUserId();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        featureFlagValue =
            bankingConfigurationPropertiesPage.getFeatureFlag(PelicanConstants.SHOW_ES_LOGGING_DELAY_ON_REPORT);
    }

    /**
     * Verify all headers of columns in Downloaded Audit Log Report.
     * <p>
     * Report should show 10 columns and name of the headers of each column should be Parent Parent Entity Id, Entity
     * Type, Entity Id, Change Date, User ID, User Name, User External Key, Action, Description ES logging delay (sec)
     */
    @Test(dataProvider = "entityForDownload")
    public void testAuditLogReportInDownloadedFile(final String entity, final String userId,
        final ArrayList subEntities, final String startDate, final String endDate) throws IOException {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        auditLogReportPage.generateReport(startDate, endDate, entity, null, userId, true);

        // Get the file name with file path
        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        if (featureFlagValue) {
            AssertCollector.assertThat("Incorrect header for 'Entity Type'", fileData[0][0],
                equalTo(PelicanConstants.ENTITY_TYPE_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Object Id", fileData[0][1],
                equalTo(PelicanConstants.OBJECT_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Object'", fileData[0][2],
                equalTo(PelicanConstants.OBJECT_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Parent Object Id'", fileData[0][3],
                equalTo(PelicanConstants.PARENT_OBJECT_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Parent Object'", fileData[0][4],
                equalTo(PelicanConstants.PARENT_OBJECT_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Change Date'", fileData[0][5],
                equalTo(PelicanConstants.CHANGE_DATE_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User Id'", fileData[0][6],
                equalTo(PelicanConstants.USER_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User Name'", fileData[0][7],
                equalTo(PelicanConstants.USER_NAME_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User External Key'", fileData[0][8],
                equalTo(PelicanConstants.USER_EXTERNAL_KEY_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Action'", fileData[0][9],
                equalTo(PelicanConstants.ACTION_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Description'", fileData[0][10],
                equalTo(PelicanConstants.DESCRIPTION_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'ES Logging Delay'", fileData[0][11],
                equalTo(PelicanConstants.ES_LOGGING_DELAY_FIELD), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect header for 'Entity Type'", fileData[0][0],
                equalTo(PelicanConstants.ENTITY_TYPE_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Object Id", fileData[0][1],
                equalTo(PelicanConstants.OBJECT_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Object'", fileData[0][2],
                equalTo(PelicanConstants.OBJECT_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Parent Object Id'", fileData[0][3],
                equalTo(PelicanConstants.PARENT_OBJECT_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Parent Object'", fileData[0][4],
                equalTo(PelicanConstants.PARENT_OBJECT_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Change Date'", fileData[0][5],
                equalTo(PelicanConstants.CHANGE_DATE_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User Id'", fileData[0][6],
                equalTo(PelicanConstants.USER_ID_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User Name'", fileData[0][7],
                equalTo(PelicanConstants.USER_NAME_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'User External Key'", fileData[0][8],
                equalTo(PelicanConstants.USER_EXTERNAL_KEY_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Action'", fileData[0][9],
                equalTo(PelicanConstants.ACTION_FIELD), assertionErrorList);
            AssertCollector.assertThat("Incorrect header for 'Description'", fileData[0][10],
                equalTo(PelicanConstants.DESCRIPTION_FIELD), assertionErrorList);
        }
        final int rowSize = (fileData.length > 20) ? 20 : fileData.length;

        for (int i = 1; i < rowSize; i++) {

            AssertCollector.assertTrue("Incorrect Entity type in column", subEntities.contains(fileData[i][0]),
                assertionErrorList);
            AssertCollector.assertTrue("Incorrect date range : " + fileData[i][5], DateTimeUtils.isDateInRange(
                startDate, endDate, fileData[i][5], PelicanConstants.DATE_FORMAT_WITH_SLASH), assertionErrorList);
            if (userId != null) {
                AssertCollector.assertThat("Incorrect User Id in column", fileData[i][6], equalTo(userId),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify Download file field name is as per Entity Detail Page naming convention
     *
     * @param startDate
     * @param endDate
     * @param entity
     * @param userId
     * @param subEntities
     * @throws IOException
     */
    @Test(dataProvider = "entityForDownload")
    public void testDescriptionFieldNameIsAsPerEntityDetailPage(final String entity, final String userId,
        final ArrayList subEntities, final String startDate, final String endDate) throws IOException {
        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(startDate, endDate, entity, null, userId, true);

        // Get the file name with file path
        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        if (featureFlagValue) {
            AssertCollector.assertThat("Total number of columns are incorrect", fileData[0].length,
                is(NUMBER_OF_COLUMNS), assertionErrorList);
        } else {
            AssertCollector.assertThat("Total number of columns are incorrect", fileData[0].length,
                is(NUMBER_OF_COLUMNS_WITH_FEATURE_FLAG), assertionErrorList);
        }

        final int rowSize = (fileData.length > 20) ? 20 : fileData.length;

        // Get index of randomly selected row
        int selectedRowIndex = auditLogReportResultPage.selectRowRandomlyFromFirstPage(rowSize);
        if (selectedRowIndex == 0) {
            selectedRowIndex += 1;
        }
        final String columnEntityType = fileData[selectedRowIndex][0];
        String description;
        if (featureFlagValue) {
            description = fileData[selectedRowIndex][10];
        } else {
            description = fileData[selectedRowIndex][9];
        }

        AssertCollector.assertThat("Description values is not present column values", description, notNullValue(),
            assertionErrorList);
        LOGGER.info("Description Column value:" + description);

        if (auditLogReportHelper.isDescriptionHasValues(description, columnEntityType)) {
            final HashMap<String, List<String>> descriptionPropertyValues =
                auditLogReportResultPage.parseDescription(description);
            Set<String> descriptionFields = descriptionPropertyValues.keySet();
            final Set<String> propertyFields = AuditLogReportHelper.PropertyFieldName.get(columnEntityType);

            // Remove value with .xlsx.
            descriptionFields = AuditLogReportHelper.removeXlsxValueFromDescriptionFields(descriptionFields);

            AssertCollector.assertTrue("Field names for " + columnEntityType + " is not as per entity Detail Page",
                propertyFields.containsAll(descriptionFields), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "entityForDownload")
    private Object[][] getDownloadEntity() {
        return new Object[][] {
                { null, null,
                        new ArrayList<>(ImmutableList.of(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, "SubscriptionOffer",
                            "SubscriptionPrice", "SubscriptionEntitlement", PelicanConstants.DESCRIPTOR)),
                        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH) },
                { PelicanConstants.ENTITY_FEATURE, null,
                        new ArrayList<>(ImmutableList.of(PelicanConstants.ENTITY_FEATURE)),
                        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH) },
                { PelicanConstants.ENTITY_BASIC_OFFERING, adminToolUserId,
                        new ArrayList<>(ImmutableList.of(PelicanConstants.ENTITY_BASIC_OFFERING, "SubscriptionPrice",
                            "SubscriptionEntitlement", PelicanConstants.DESCRIPTOR)),
                        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 4),
                        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1) } };
    }
}
