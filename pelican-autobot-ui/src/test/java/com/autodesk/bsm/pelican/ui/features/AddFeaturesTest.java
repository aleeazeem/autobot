package com.autodesk.bsm.pelican.ui.features;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

/**
 * This test class tests adding features. It also verifies Audit Log for Item
 *
 * @author Vineel Yerragudi and Shweta Hegde
 */
public class AddFeaturesTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static AddFeaturePage addFeaturePage;
    private static FeatureDetailPage featureDetailPage;
    private String featureTypeId;
    private String featureName;
    private String featureTypeName;
    private String appId;
    private String userId;
    private UserUtils userUtils;
    private RolesHelper rolesHelper;
    private boolean isNonOfferingManagerUserLoggedIn;
    private static HashMap<String, String> userParams;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;

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

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();

        final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);

        featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

        // Navigate to the add feature page and add a feature
        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName, featureTypeName);
        final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
        featureTypeId = featureTypeDetailPage.getId();

        rolesHelper = new RolesHelper(getEnvironmentVariables());
        isNonOfferingManagerUserLoggedIn = false;
        userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), getEnvironmentVariables().getUserExternalKey());
        userUtils = new UserUtils();
    }

    /**
     * This method tests adding a new feature and verifying audit log
     *
     * @param status
     */
    @Test(dataProvider = "featurestatus")
    public void testAddFeature(final String status) {

        // Navigate to the add feature page and add a feature
        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        addFeaturePage.addFeature(featureTypeName, featureName, featureName, status);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        final String featureId = featureDetailPage.getFeatureId();

        String featureStatus = status;
        if (status == null) {
            featureStatus = PelicanConstants.YES;
        }
        HelperForCommonAssertionsOfFeature.commonAssertionsOfFeature(featureDetailPage, featureId, featureName,
            featureName, featureTypeName, featureStatus, assertionErrorList);

        final String active =
            (featureStatus.equalsIgnoreCase(PelicanConstants.YES)) ? PelicanConstants.TRUE.toLowerCase()
                : PelicanConstants.FALSE.toLowerCase();
        // Verify the CREATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, null, featureTypeId, null, featureName, null,
            featureName, null, null, Action.CREATE, userId, null, null, active, assertionErrorList);

        // Verify Audit Log report results for feature.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null,
                featureId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, null, featureName,
            null, featureName, null, featureTypeName + " (" + featureTypeId + ")", null, active, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test whether to determine only Offering Manager is able to add/edit/inactivate a feature.
     *
     */
    @Test
    public void testNonOfferingManagerInactivateAFeature() {

        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        if (!isNonOfferingManagerUserLoggedIn) {
            adminToolPage.login();
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
            // Log in as a non offering-manager user
            final List<String> nonOfferingManagerRoleList = rolesHelper.getNonOfferingManagerRoleList();
            userUtils.createAssignRoleAndLoginUser(userParams, nonOfferingManagerRoleList, adminToolPage,
                getEnvironmentVariables());
            isNonOfferingManagerUserLoggedIn = true;
        }

        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName, featureName, featureName, PelicanConstants.NO);
        featureDetailPage = addFeaturePage.clickOnSave();

        AssertCollector.assertThat("Incorrect header error message for non-offering manager role",
            featureDetailPage.getH2ErrorMessage(), equalTo(PelicanErrorConstants.ERROR_HEADER), assertionErrorList);
        AssertCollector.assertThat("Incorrect header error message for non-offering manager role",
            featureDetailPage.getH3ErrorMessage(), equalTo(PelicanErrorConstants.PERMISSION_DENIED),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

        // login back with svc_p_pelican user
        adminToolPage.logout();
        adminToolPage.login();
    }

    /**
     * This is a data provider to return feature status.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "featurestatus")
    public static Object[][] getFeatureStatus() {
        return new Object[][] { { PelicanConstants.YES }, { PelicanConstants.NO }, { null } };
    }
}
