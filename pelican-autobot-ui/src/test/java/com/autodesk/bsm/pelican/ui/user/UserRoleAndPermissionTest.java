package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserRoleAndPermissionTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private RolesHelper rolesHelper;
    private static final String EBSO_ROLE = Role.EBSO.getValue();
    private static final String OFFERING_MANAGER_ROLE = Role.OFFERING_MANAGER.getValue();
    private static HashMap<String, String> userParams;
    private String featureId;
    private static FeatureDetailPage featureDetailPage;
    private static FindFeaturePage findFeaturePage;
    private static FindStoresPage findStoresPage;
    private UserUtils userUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleAndPermissionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        rolesHelper = new RolesHelper(getEnvironmentVariables());
        userParams = new HashMap<>();

        // add a feature and get feature id and feature external key
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        featureId = item.getId();

        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);
        findStoresPage = adminToolPage.getPage(FindStoresPage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        userUtils = new UserUtils();
    }

    /**
     * This test method validate that if user sign in without a role of offering manager or EBSO than he cannot click on
     * upload Subscription link from detail page of Subscriptions in Admin Tool
     */
    @Test
    public void testUploadSubscriptionsWithoutRoleOfEbsoAndOfferingManager() {
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.QA_ROLE_ONLY_USER);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // Log in as a non offering-manager user
        final List<String> qaOnlyRoleList = rolesHelper.getQAOnlyRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, qaOnlyRoleList, adminToolPage, getEnvironmentVariables());

        final FindSubscriptionsPage subscriptions = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptions.navigateToSubscriptionsForm();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptions.getDriver().getTitle();
        LOGGER.info(subscriptions.getDriver().getTitle());
        AssertCollector.assertThat("Moved to next Page", subscriptions.getDriver().getTitle(),
            equalTo("Pelican Subscriptions"), assertionErrorList);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final boolean isPresent = subscriptions.isUploadSubscriptionsLinkDisplayed();
        if (isPresent) {
            // try to click on upload Subscription link
            subscriptions.clickOnUploadSubscription();
            Util.waitInSeconds(TimeConstants.THREE_SEC);
            final GenericDetails details = adminToolPage.getPage(GenericDetails.class);
            LOGGER.info(details.getDriver().getTitle());
            AssertCollector.assertThat("Moved to next Page", details.getDriver().getTitle(),
                equalTo("Pelican - Upload Subscription"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);

        adminToolPage.login();
    }

    /**
     * This test method validate that if user sign in with a role of offering manager or ebso than he cannot click on
     * upload Subscription link from detail page of Subscriptions in Admin Tool
     */
    @Test(dataProvider = "roleForTheUser")
    public void testUploadSubscriptionWithoutRoleOfOfferingManager(final String roleOfAUser) {
        List<String> roleList = new ArrayList<>();
        LOGGER.info("Role of a User :" + roleOfAUser);
        if (roleOfAUser.equalsIgnoreCase("EBSO Role")) {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.EBSO_ONLY_USER);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            roleList = rolesHelper.getEBSOOnlyRoleList();
        } else if (roleOfAUser.equalsIgnoreCase("Offering Manager")) {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.OFFERING_MANAGER_ONLY_USER);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            roleList = rolesHelper.getOfferingManagerOnlyRoleList();
        }
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // Log in as a specific role user
        userUtils.createAssignRoleAndLoginUser(userParams, roleList, adminToolPage, getEnvironmentVariables());

        final FindSubscriptionsPage subscriptions = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptions.navigateToSubscriptionsForm();
        subscriptions.getDriver().getTitle();
        LOGGER.info(subscriptions.getDriver().getTitle());
        AssertCollector.assertThat("Moved to next Page", subscriptions.getDriver().getTitle(),
            equalTo("Pelican Subscriptions"), assertionErrorList);
        // try to click on upload Subscription link
        final boolean isPresent = subscriptions.isUploadSubscriptionsLinkDisplayed();
        if (isPresent) {
            subscriptions.clickOnUploadSubscription();
            final GenericDetails details = adminToolPage.getPage(GenericDetails.class);
            LOGGER.info(details.getDriver().getTitle());
            if (EBSO_ROLE.equals(roleOfAUser)) {
                AssertCollector.assertThat("Moved to next Page", details.getDriver().getTitle(),
                    equalTo("Pelican Subscriptions"), assertionErrorList);
            } else if (OFFERING_MANAGER_ROLE.equals(roleOfAUser)) {
                AssertCollector.assertThat("Moved to next Page", details.getDriver().getTitle(),
                    equalTo("Pelican - Upload Subscription"), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);

        adminToolPage.login();
    }

    /**
     * Verify that 'make a copy' of the feature operation is NOT permitted for a user without 'item.add' permission
     */
    @Test
    public void testMakeFeatureCopyPermissionDeniedSuccess() {

        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.ATC_ADMIN_ONLY_USER);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // Log in as a non offering-manager user
        final List<String> atcAdminOnlyRoleList = rolesHelper.getATCAdminOnlyRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, atcAdminOnlyRoleList, adminToolPage,
            getEnvironmentVariables());

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);

        // click 'make a copy' link
        featureDetailPage.clickMakeFeatureCopyLink();
        final GenericDetails errorDetails = adminToolPage.getPage(GenericDetails.class);
        AssertCollector.assertThat("Invalid error message", errorDetails.getH3ErrorMessage(),
            equalTo(PelicanConstants.PERMISSION_DENIED), assertionErrorList);
        errorDetails.clickOnShowDetailsLink();
        AssertCollector.assertThat("Invalid error details", errorDetails.getErrorDetails(),
            equalTo("This operation requires the item.add permission."), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
        adminToolPage.login();
    }

    /**
     * Verify that assign country to a store operation is NOT permitted for a user without 'store.edit' permission
     */
    @Test
    public void testAssignCountryToStorePermissionDeniedSuccess() {

        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.READ_ONLY_USER);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        // Log in as a non offering-manager user
        final List<String> readOnlyRoleList = rolesHelper.getReadOnlyRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, readOnlyRoleList, adminToolPage, getEnvironmentVariables());

        final StoreDetailPage storeDetailPage = findStoresPage.findByValidExtKey(getStoreExternalKeyUs());
        final List<String> priceListNames = storeDetailPage.getPriceListGrid().getColumnValues("Name");
        storeDetailPage.assignCountryToPriceList(Country.DE, priceListNames.get(0));

        final GenericDetails assignCountryErrorGrid = adminToolPage.getPage(GenericDetails.class);
        AssertCollector.assertThat("Invalid error message", assignCountryErrorGrid.getH3ErrorMessage(),
            equalTo(PelicanConstants.PERMISSION_DENIED), assertionErrorList);
        assignCountryErrorGrid.clickOnShowDetailsLink();
        AssertCollector.assertThat("Invalid error details", assignCountryErrorGrid.getErrorDetails(),
            equalTo("This operation requires the store.edit permission."), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
        adminToolPage.login();
    }

    @DataProvider(name = "roleForTheUser")
    public Object[][] getTestData() {
        return new Object[][] { { EBSO_ROLE }, { OFFERING_MANAGER_ROLE } };
    }
}
