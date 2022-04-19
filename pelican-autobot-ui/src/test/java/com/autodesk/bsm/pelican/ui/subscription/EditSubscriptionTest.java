package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test class to verify edit subscription functionality.
 *
 * @author jains
 */

public class EditSubscriptionTest extends SeleniumWebdriver {

    private FindSubscriptionsPage findSubscriptionsPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private String bicActiveSubscriptionId;
    private static final String TIME_SUFFIX = " 00:00:00";
    private static final String UTC_SUFFIX = " UTC";
    private AdminToolPage adminToolPage;
    private static final String NON_QA_GCSO_EDIT_SUBSCRIPTION_USER_EXTERNAL_KEY =
        "Automation_test_non_qa_and_gcso_edit_user";
    private static final Logger LOGGER = LoggerFactory.getLogger(EditSubscriptionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final UserUtils userUtils = new UserUtils();

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final BuyerUser buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
        // create a bic subscription
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        bicActiveSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("bicActiveSubscriptionId: " + bicActiveSubscriptionId);

    }

    /**
     * This test verifies that Edit subscription fields are recorded under subscription activity. This also verifies the
     * audit log.
     *
     * @param newNextBillingDate
     * @param newExpirationDate
     * @param newSubscriptionStatus
     * @param notes
     */
    @Test(dataProvider = "editSubscriptionData")
    public void testEditSubscription(final String newNextBillingDate, final String newExpirationDate,
        final String newSubscriptionStatus, final String notes) {
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicActiveSubscriptionId);
        // get current data before editing the subscription
        final String oldNextBillingDate = subscriptionDetailPage.getCompleteNextBillingDate();
        final String oldExpirationDate =
            subscriptionDetailPage.getCompleteExpirationDate().equals(PelicanConstants.HIPHEN) ? null
                : subscriptionDetailPage.getCompleteExpirationDate();
        final String oldSubscriptionStatus = subscriptionDetailPage.getStatus();

        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        subscriptionDetailPage =
            editSubscriptionPage.editASubscription(newNextBillingDate, newExpirationDate, newSubscriptionStatus, notes);

        // construct expected Subscription Activity Memo
        final String expextedSubscriptionActivityMemo =
            getExpectedSubscriptionActivityMemo(oldNextBillingDate, newNextBillingDate, oldExpirationDate,
                newExpirationDate, oldSubscriptionStatus, newSubscriptionStatus, notes);
        SubscriptionActivity lastSubscriptionActivity;
        if (null != newSubscriptionStatus
            && newSubscriptionStatus.equalsIgnoreCase(SubscriptionStatus.CANCELLED.getDisplayName())) {
            // If subscription status is set to cancelled from Edit Subscription link, an additional activity of cancel
            // is also logged so we need to get the second last activity.
            final List<SubscriptionActivity> subscriptionActivityList =
                subscriptionDetailPage.getSubscriptionActivity();
            lastSubscriptionActivity = Iterables.get(subscriptionActivityList, (subscriptionActivityList.size() - 2));
            AssertCollector.assertThat(
                "Subscription activity - Cancelled is not recorded for subscription id: " + bicActiveSubscriptionId,
                subscriptionDetailPage.getLastSubscriptionActivity().getActivity(), equalTo(PelicanConstants.CANCEL),
                assertionErrorList);
        } else {
            lastSubscriptionActivity = subscriptionDetailPage.getLastSubscriptionActivity();
        }

        AssertCollector.assertThat(
            "Subscription activity - Memo is not correct for subscription id: " + bicActiveSubscriptionId,
            lastSubscriptionActivity.getMemo(), equalTo(expextedSubscriptionActivityMemo), assertionErrorList);

        AssertCollector.assertThat(
            "Subscription activity - Activity is not correct for subscription id: " + bicActiveSubscriptionId,
            lastSubscriptionActivity.getActivity(), equalTo(PelicanConstants.EDIT), assertionErrorList);

        AssertCollector.assertThat(
            "Subscription activity - Requester is not correct for subscription id: " + bicActiveSubscriptionId,
            lastSubscriptionActivity.getRequestor(),
            equalTo(getEnvironmentVariables().getUserName() + " (" + getEnvironmentVariables().getUserId() + ")"),
            assertionErrorList);

        // verify audit log
        final boolean isAuditLogFound = SubscriptionAuditLogHelper.helperToQueryDynamoDbForEditSubscription(
            bicActiveSubscriptionId, DateTimeUtils.getAuditLogDate(oldNextBillingDate, true),
            DateTimeUtils.getAuditLogDate(newNextBillingDate, false),
            DateTimeUtils.getAuditLogDate(oldExpirationDate, true),
            DateTimeUtils.getAuditLogDate(newExpirationDate, false), oldSubscriptionStatus, newSubscriptionStatus,
            assertionErrorList);
        AssertCollector.assertTrue("Audit log is not found for edit subscription", isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that Edit Subscription link is visible only to QA Role and GCSO Edit Subscription role.
     *
     * @param role
     */
    @Test(dataProvider = "roles")
    public void testEditSubscriptionWithQAandGcsoEditSubscriptionRole(final Role role) {
        final HashMap<String, String> userParams = new HashMap<>();
        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());
        List<String> requiredRoleList = new ArrayList<>();

        if (role == Role.QA_ONLY) {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.QA_ROLE_ONLY_USER);
            requiredRoleList = rolesHelper.getQAOnlyRoleList();

        } else {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.GCSO_EDIT_SUBSCRIPTION_USER_EXTERNAL_KEY);
            requiredRoleList.add(Role.GCSO_EDIT_SUBSCRIPTION.getValue());
        }
        // Add read only role
        requiredRoleList.addAll(rolesHelper.getReadOnlyRoleList());

        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // Create new user and log in with the newly create user.
        final UserUtils userUtils = new UserUtils();
        userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage, getEnvironmentVariables());

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicActiveSubscriptionId);
        final boolean isEditSubscriptionLinkPresent = subscriptionDetailPage.isEditSubscriptionLinkDisplayed();

        AssertCollector.assertTrue("Edit Subscription link should be present for Role: " + role.getValue(),
            isEditSubscriptionLinkPresent, assertionErrorList);

        // Logout from specific user with special permission
        adminToolPage.logout();
        // Login back with original user
        adminToolPage.login();
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test verifies that Edit Subscription link is not visible to any role other than QA Role and GCSO Edit
     * Subscription role.
     */
    @Test
    public void testEditSubscriptionWithoutQAandGcsoEditSubscriptionRole() {
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), NON_QA_GCSO_EDIT_SUBSCRIPTION_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());

        // create role list with all roles except qa and gcso edit subscription role
        final List<String> requiredRoleList = rolesHelper.getAllRolesList();
        requiredRoleList.remove(Role.QA_ONLY.getValue());
        requiredRoleList.remove(Role.GCSO_EDIT_SUBSCRIPTION.getValue());

        // create user and login with new user
        final UserUtils userUtils = new UserUtils();
        userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage, getEnvironmentVariables());

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicActiveSubscriptionId);

        final boolean isEditSubscriptionLinkPresent = subscriptionDetailPage.isEditSubscriptionLinkDisplayed();

        AssertCollector.assertFalse(
            "Edit Subscription link should not be present for roles other than QA and GCSO Edit subscription",
            isEditSubscriptionLinkPresent, assertionErrorList);

        // Logout from specific user with special permission
        adminToolPage.logout();
        // Login back with original user
        adminToolPage.login();
        AssertCollector.assertAll(assertionErrorList);

    }

    @DataProvider(name = "editSubscriptionData")
    public static Object[][] getEditSubscriptionData() {
        return new Object[][] {
                { DateTimeUtils.getNowMinusDays(0), DateTimeUtils.getNowPlusDays(5), SubscriptionStatus.PENDING.name(),
                        "notes" },
                { DateTimeUtils.getNowPlusDays(2), null, null, null },
                { null, null, SubscriptionStatus.CANCELLED.name(), null } };
    }

    @DataProvider(name = "roles")
    public static Object[][] getRoleName() {
        return new Object[][] { { Role.QA_ONLY }, { Role.GCSO_EDIT_SUBSCRIPTION } };
    }

    /**
     * Method to construct expected subscription activity memo.
     *
     * @param oldNextBillingDate
     * @param newNextBillingDate
     * @param oldExpirationDate
     * @param newExpirationDate
     * @param oldSubscriptionStatus
     * @param newSubscriptionStatus
     * @param notes
     * @return String
     */
    private String getExpectedSubscriptionActivityMemo(final String oldNextBillingDate, final String newNextBillingDate,
        final String oldExpirationDate, final String newExpirationDate, final String oldSubscriptionStatus,
        final String newSubscriptionStatus, final String notes) {
        final StringBuilder expectedSubscriptionActivity = new StringBuilder();

        if (newSubscriptionStatus != null) {
            // Adding new line character to memo for a new entry.
            if (expectedSubscriptionActivity.length() != 0) {
                expectedSubscriptionActivity.append("\n");
            }
            expectedSubscriptionActivity
                .append("Status: " + oldSubscriptionStatus + " to " + newSubscriptionStatus + ".");
            LOGGER.info("Status added to expected Memo");
        }

        if (newExpirationDate != null) {
            // Adding new line character to memo for a new entry.
            if (expectedSubscriptionActivity.length() != 0) {
                expectedSubscriptionActivity.append("\n");
            }
            expectedSubscriptionActivity.append(
                "Expiration Date: " + oldExpirationDate + " to " + newExpirationDate + TIME_SUFFIX + UTC_SUFFIX + ".");

            LOGGER.info("Expiration date added to expected Memo");
        }

        if (newNextBillingDate != null) {
            if (expectedSubscriptionActivity.length() != 0) {
                expectedSubscriptionActivity.append("\n");
            }
            expectedSubscriptionActivity.append("Next Billing Date: " + oldNextBillingDate + " to " + newNextBillingDate
                + TIME_SUFFIX + UTC_SUFFIX + ".");
            LOGGER.info("Next billing date added to expected Memo");
        }

        if (notes != null) {
            // Adding new line character to memo for a new entry.
            if (expectedSubscriptionActivity.length() != 0) {
                expectedSubscriptionActivity.append("\n");
            }
            expectedSubscriptionActivity.append(notes);
            LOGGER.info("Notes added to expected Memo");
        }
        return expectedSubscriptionActivity.toString();
    }
}
