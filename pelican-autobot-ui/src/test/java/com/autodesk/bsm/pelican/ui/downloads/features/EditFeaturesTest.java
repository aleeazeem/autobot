package com.autodesk.bsm.pelican.ui.downloads.features;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.features.HelperForCommonAssertionsOfFeature;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.EditFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This test class tests editing features. It also verifies Audit Log for Item
 *
 * @author Shweta Hegde
 */
public class EditFeaturesTest extends SeleniumWebdriver {

    private static AddFeaturePage addFeaturePage;
    private static FeatureDetailPage featureDetailPage;
    private static EditFeaturePage editFeaturePage;
    private String featureId;
    private String featureTypeId1;
    private String featureTypeId2;
    private String featureName;
    private String featureTypeName1;
    private String featureTypeName2;
    private String appId;
    private String userId;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private static AddSubscriptionPlanPage addSubscriptionPlan;
    private String productLineNameAndExternalKey;
    private FindFeaturePage findFeaturePage;
    private AuditLogReportResultPage auditLogReportResultPage;
    private AuditLogReportPage auditLogReportPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(EditFeaturesTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        userId = getEnvironmentVariables().getUserId();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        // get application
        final Applications applications = resource.application().getApplications();
        if (applications.getApplications().size() > 0) {
            appId = applications.getApplications().get(applications.getApplications().size() - 1).getId();
        }
        // initialize webdriver
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();
        final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);
        editFeaturePage = adminToolPage.getPage(EditFeaturePage.class);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);

        featureTypeName1 = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

        // Navigate to the add feature page and add a feature
        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName1, featureTypeName1);
        FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
        featureTypeId1 = featureTypeDetailPage.getId();

        featureTypeName2 = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8) + "new";
        // Create another feature type
        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName2, featureTypeName2 + "externalKey");
        featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
        featureTypeId2 = featureTypeDetailPage.getId();

        addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        // Add product Line name + external key
        productLineNameAndExternalKey = getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);

    }

    /**
     * This method tests editing an existing feature and verifying audit log
     */
    @Test
    public void testEditFeature() {

        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName1, featureName, featureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

        final String newFeatureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        final String newFeatureExternalKey = PelicanConstants.FEATURE + "-ext-" + RandomStringUtils.randomAlphabetic(8);

        editFeaturePage = featureDetailPage.clickOnEditFeatureButton();
        editFeaturePage.editFeature(newFeatureName, newFeatureExternalKey, featureTypeName2, null);
        featureDetailPage = editFeaturePage.clickOnUpdateFeatureButton();

        featureId = featureDetailPage.getFeatureId();
        HelperForCommonAssertionsOfFeature.commonAssertionsOfFeature(featureDetailPage, featureId, newFeatureName,
            newFeatureExternalKey, featureTypeName2, PelicanConstants.YES, assertionErrorList);

        // Verify the UPDATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, featureTypeId1, featureTypeId2, featureName,
            newFeatureName, featureName, newFeatureExternalKey, null, null, Action.UPDATE, userId, null, null, null,
            assertionErrorList);

        // Verify Audit Log report results for feature id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null,
                featureId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, featureName,
            newFeatureName, featureName, newFeatureExternalKey, featureTypeName1 + " (" + featureTypeId1 + ")",
            featureTypeName2 + " (" + featureTypeId2 + ")", null, null, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to in-activate a feature tied to a subscription plan
     *
     */
    @Test
    public void testInactivateAFeatureTiedToASubscriptionPlan() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId1);
        final String itemId = item.getId();
        LOGGER.info("Item Id " + itemId);
        // Add Feature Entitlement
        addSubscriptionPlan.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(
                ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
            0);
        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlan.getFeatureName(0)), assertionErrorList);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlan.clickOnSave(true);
        LOGGER.info("Subscription plan id " + subscriptionPlanDetailPage.getId());

        featureDetailPage = findFeaturePage.findFeatureById(itemId);
        editFeaturePage.edit();
        editFeaturePage.editFeature(null, null, null, PelicanConstants.NO);
        editFeaturePage.clickOnUpdateFeatureButton();

        AssertCollector.assertThat("Incorrect status for the feature even after inactivating it",
            featureDetailPage.getActive(), equalTo(PelicanConstants.NO), assertionErrorList);

        final String featureName = featureDetailPage.getFeatureName();
        final String featureExternalKey = featureDetailPage.getFeatureExternalKey();

        // Verify the UPDATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(itemId, null, appId, featureTypeId1, featureTypeId1, featureName,
            featureName, featureExternalKey, featureExternalKey, null, null, Action.UPDATE, userId, null,
            PelicanConstants.TRUE.toLowerCase(), PelicanConstants.FALSE.toLowerCase(), assertionErrorList);

        // Verify Audit Log report results for feature id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null, itemId,
                adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, null, null, null,
            null, null, null, PelicanConstants.TRUE.toLowerCase(), PelicanConstants.FALSE.toLowerCase(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to inactivate and reactivate a feature in admin tool.
     *
     */
    @Test
    public void testInActivateAndActivateFeature() {

        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName1, featureName, featureName, PelicanConstants.NO);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

        HelperForCommonAssertionsOfFeature.commonAssertionsOfFeature(featureDetailPage, featureId, featureName,
            featureName, featureTypeName1, PelicanConstants.NO, assertionErrorList);

        editFeaturePage.edit();
        editFeaturePage.editFeature(null, null, null, PelicanConstants.YES);
        editFeaturePage.clickOnUpdateFeatureButton();

        featureId = featureDetailPage.getFeatureId();
        HelperForCommonAssertionsOfFeature.commonAssertionsOfFeature(featureDetailPage, featureId, featureName,
            featureName, featureTypeName1, PelicanConstants.YES, assertionErrorList);

        // Verify the UPDATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, featureTypeId1, featureTypeId1, featureName,
            featureName, featureName, featureName, null, null, Action.UPDATE, userId, null,
            PelicanConstants.FALSE.toLowerCase(), PelicanConstants.TRUE.toLowerCase(), assertionErrorList);

        // Verify Audit Log report results for feature id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null,
                featureId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, null, null, null,
            null, null, null, PelicanConstants.FALSE.toLowerCase(), PelicanConstants.TRUE.toLowerCase(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case to determine whether inactive features are not displayed when adding a subscription plan
     *
     */
    @Test
    public void testInActiveFeatureNotDisplayedWhileAddingSubscriptionPlan() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        LOGGER.info("Item Id " + itemId);

        featureDetailPage = findFeaturePage.findFeatureById(itemId);
        final String featureName = featureDetailPage.getFeatureName();
        editFeaturePage.edit();
        editFeaturePage.editFeature(null, null, null, PelicanConstants.NO);
        editFeaturePage.clickOnUpdateFeatureButton();
        AssertCollector.assertThat("Incorrect status for the feature even after inactivating it",
            featureDetailPage.getActive(), equalTo(PelicanConstants.NO), assertionErrorList);

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        // Add Feature Entitlement
        final List<String> featureSearchResultsList = addSubscriptionPlan.getResultsOnFeatureSearch(0, featureName);
        AssertCollector.assertThat("Incorrect feature search results header", featureSearchResultsList.get(0),
            equalTo(PelicanConstants.ZERO_RESULTS), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search results body", featureSearchResultsList.get(1),
            equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test the description field in the audit log report.
     *
     */
    @Test
    public void testAuditLogReportDescription() {

        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName1, featureName, featureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

        editFeaturePage = featureDetailPage.clickOnEditFeatureButton();
        featureDetailPage = editFeaturePage.clickOnUpdateFeatureButton();

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        // Verify Audit Log report results for feature id.
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null, featureId,
            adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the description field in the audit log download report.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testAuditLogReportDescriptionInDownloadReport() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName1, featureName, featureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

        editFeaturePage = featureDetailPage.clickOnEditFeatureButton();
        featureDetailPage = editFeaturePage.clickOnUpdateFeatureButton();

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_FEATURE, featureId, adminToolUserId,
            true);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
