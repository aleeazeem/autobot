package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.AddBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * This test class test the View and Download Functionality of Audit Log Report in Non AUTO Family(Demo/AUTODESK) to
 * make sure the data from AUTO family are show in other app family
 *
 * @author Sumant Manda
 */
public class ViewOrDownloadAuditLogReportInDemoFamilyTest extends SeleniumWebdriver {

    private AdminToolPage adminToolDemoUser;
    private static String basicOfferingId;
    private static String subscriptionPlanId;
    private static String featureId;
    private AuditLogReportPage auditLogReportPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ViewOrDownloadAuditLogReportInDemoFamilyTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        // Initiating the environment and the appFamily set to AUTO
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final String productLineNameAndExternalKey =
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";
        final AddBasicOfferingPage addBasicOfferingPage = adminToolPage.getPage(AddBasicOfferingPage.class);
        final AddSubscriptionPlanPage addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        final AddFeaturePage addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);
        final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        // Add Basic Offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey,
            RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(8), MediaType.DVD, null, null,
            UsageType.COM, null, null);
        final BasicOfferingDetailPage basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();

        // Get the basic offering id
        basicOfferingId = basicOfferingDetailPage.getId();

        // Add Subscription Plan
        addSubscriptionPlan.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8),
            RandomStringUtils.randomAlphanumeric(8), OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlan.clickOnSave(false);

        // Get the Subscription plan ID
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        // Add a Feature Type and Feature
        final String featureTypeName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);

        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName, featureTypeName);
        addFeatureTypePage.clickOnAddFeatureTypeButton();

        addFeaturePage.addFeature(featureTypeName, PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8),
            PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8), null);
        final FeatureDetailPage featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

        adminToolPage.logout();
        adminToolDemoUser = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolDemoUser.login(getEnvironmentVariables().getOtherAppFamily(), getEnvironmentVariables().getUserName(),
            getEnvironmentVariables().getPassword());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        adminToolDemoUser.logout();
    }

    /*
     * Test to verify Subscription plan related entity changes from AUTO family are NOT shown in `View auditreport` from
     * Demo app family
     */
    @Test
    public void testViewAuditLogSubscriptionPlanInDemoAppfamily() {
        final AuditLogReportResultPage auditLogReportResultPage = auditLogReportPage.generateReport(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, null, false);

        if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
            AssertCollector.assertThat("Demo family shows data from AUTO appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);
        } else {
            AssertCollector.assertThat("Demo family shows data for Demo appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test to verify Feature related entity changes from AUTO family are NOT shown in `View auditreport` from Demo app
     * family
     */
    @Test
    public void testViewAuditLogFeaturesInDemoAppfamily() {
        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_FEATURE, featureId, null, false);

        if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
            AssertCollector.assertThat("Demo family shows data from AUTO appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);
        } else {
            AssertCollector.assertThat("Demo family shows data for Demo appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test to verify Basic Offering related entity changes from AUTO family are NOT shown in `View auditreport` from
     * Demo app family
     */
    @Test
    public void testViewAuditLogBasicOfferinsInDemoAppfamily() {
        final AuditLogReportResultPage auditLogReportResultPage = auditLogReportPage.generateReport(null, null,
            PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId, null, false);

        if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
            AssertCollector.assertThat("Demo family shows data from AUTO appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);
        } else {
            AssertCollector.assertThat("Demo family shows data for Demo appfamily",
                auditLogReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test to verify Subscription plan related entity changes from AUTO family are NOT shown in `Download auditreport`
     * from Demo app family
     */
    @Test
    public void testDownloadAuditLogSubscriptionPlanInDemoAppfamily() {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId,
            null, true);

        // Get the file name with file path
        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        try {
            // Read from the file
            final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
            if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(1),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(2),
                    assertionErrorList);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test to verify Feature related entity changes from AUTO family are NOT shown in `Download auditreport` from Demo
     * app family
     */
    @Test
    public void testDownloadAuditLogFeaturesInDemoAppfamily() {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_FEATURE, featureId, null, true);

        // Get the file name with file path
        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        try {
            // Read from the file
            final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
            if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(1),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(2),
                    assertionErrorList);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test to verify Basic Offering related entity changes from AUTO family are NOT shown in `Download auditreport`
     * from Demo app family
     */
    @Test
    public void testDownloadAuditLogBasicOfferinsInDemoAppfamily() {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId, null,
            true);

        // Get the file name with file path
        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;

        try {
            // Read from the file
            final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
            if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(1),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Total number of rows are incorrect", fileData.length, is(2),
                    assertionErrorList);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
