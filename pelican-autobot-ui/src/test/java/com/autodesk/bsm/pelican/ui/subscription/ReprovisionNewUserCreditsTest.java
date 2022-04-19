package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.SubscriptionCurrencyFulfillmentStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ReprovisionNewUserCreditsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for Reprovision New User Credits.
 *
 * @author jains
 *
 */
public class ReprovisionNewUserCreditsTest extends SeleniumWebdriver {

    private ReprovisionNewUserCreditsPage reprovisionNewUserCreditsPage;
    private AdminToolPage adminToolPage;
    private final String rabbitMqMessage = "Number of subscriptions found for reprovisioning new user credits : %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReprovisionNewUserCreditsTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        reprovisionNewUserCreditsPage = adminToolPage.getPage(ReprovisionNewUserCreditsPage.class);
    }

    /**
     * This test method verifies that reprovision SUBSCRIPTIONUSER credit call is sent to trigger for given subscription
     * id. 1) Call to triggers is sent only if fulfillment status for SUBSCRIPTIONUSER for a particular subscription in
     * subscription_currency_fulfillment table is pending or failed. 2) If the user is a new user in convergent charging
     * then credits are granted otherwise call is ignored. In any case we log the subscription activity either as
     * granted or ignored.
     *
     * @param subscriptionCurrencyFulfillmentStatus
     */
    @Test(dataProvider = "subscriptionCurrencyFulfillmentStatus")
    public void testReprovisionCloudCreditForSubscriptionUserDefectBic9873(
        final int subscriptionCurrencyFulfillmentStatus) {
        final HashMap<String, String> subscriptionMap = getEligibleSubscription(subscriptionCurrencyFulfillmentStatus);
        final String subscriptionId = subscriptionMap.get("subscriptionId");
        final String subscriptionCurrencyFulfillmentId = subscriptionMap.get("subscriptionCurrencyFulfillmentId");
        reprovisionNewUserCreditsPage = reprovisionNewUserCreditsPage.reprovisionNewUserCredits(subscriptionId);
        AssertCollector.assertThat("Info message for RabbitMQ is not correct.",
            reprovisionNewUserCreditsPage.getInfoMessage(), equalTo(String.format(rabbitMqMessage, 1)),
            assertionErrorList);

        // verify subscription activity
        final FindSubscriptionsPage findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat(
            "Subscription activity for SUBSCRIPTIONUSER is not correct for subscription id " + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getMemo(),
            equalTo("Cloud Credits (1 SUBSCRIPTIONUSER). Ignored."), assertionErrorList);

        // verify subscription_currency_fulfillment table
        final String selectSql = "Select FULFILLMENT_STATUS from subscription_currency_fulfillment where id = %s";
        final List<Map<String, String>> resultMapList =
            DbUtils.selectQuery(String.format(selectSql, subscriptionCurrencyFulfillmentId), getEnvironmentVariables());
        AssertCollector.assertThat("Fulfillment status is not updated to Fulfilled.",
            resultMapList.get(0).get("FULFILLMENT_STATUS"),
            equalTo(Integer.toString(SubscriptionCurrencyFulfillmentStatus.FULFILLED.ordinal())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that reprovision SUBSCRIPTIONUSER credit call is NOT sent to RabbiMQ if fulfillment
     * status for SUBSCRIPTIONUSER in subscription_currency_fulfillment table is NOT pending.
     */
    @Test
    public void testReprovisionCreditMessageWithNonEligibleSubscription() {
        final String subscriptionId =
            getEligibleSubscription(SubscriptionCurrencyFulfillmentStatus.FULFILLED.ordinal()).get("subscriptionId");
        reprovisionNewUserCreditsPage = reprovisionNewUserCreditsPage.reprovisionNewUserCredits(subscriptionId);
        AssertCollector.assertThat("Info message for RabbitMQ is not correct.",
            reprovisionNewUserCreditsPage.getInfoMessage(), equalTo(String.format(rabbitMqMessage, 0)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that subscription id list can only be numeric.
     */
    @Test
    public void testInvalidCharacterError() {
        reprovisionNewUserCreditsPage = reprovisionNewUserCreditsPage.reprovisionNewUserCredits("abc");
        AssertCollector.assertThat("Page title is not correct.", reprovisionNewUserCreditsPage.getTitle(),
            equalTo("Reprovision New User Credits"), assertionErrorList);
        AssertCollector.assertThat("Error mesage for invalid charcter is not correct.",
            reprovisionNewUserCreditsPage.getError(), equalTo("Found invalid character(s) in Subscription Id List."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that maximum 200 allowed subscriptions are allowed.
     */
    @Test
    public void testMaximumSubscriptionAllowedError() {
        final StringBuffer subscriptionIdString = new StringBuffer();
        for (int i = 1; i <= 201; i++) {
            subscriptionIdString.append(i + ",");
        }
        // Remove comma at the end of string
        subscriptionIdString.deleteCharAt(subscriptionIdString.length() - 1);

        reprovisionNewUserCreditsPage =
            reprovisionNewUserCreditsPage.reprovisionNewUserCredits(subscriptionIdString.toString());
        AssertCollector.assertThat("Error mesage for maximum allowed subscription id is not correct.",
            reprovisionNewUserCreditsPage.getError(), equalTo("Subscription Id List cannot exceed 200."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that Reprovision New User Credits is NOT accessible to non-EBSO user.
     */
    @Test
    public void testNonEBSOUserPermissionForReprovisionNewUserCredit() {
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());

        // Log in as a non-ebso user
        final List<String> nonEbsoRoleList = rolesHelper.getNonEbsoRoleList();
        final UserUtils userUtils = new UserUtils();

        userUtils.createAssignRoleAndLoginUser(userParams, nonEbsoRoleList, adminToolPage, getEnvironmentVariables());

        reprovisionNewUserCreditsPage.navigateToReprovisionCreditPage();
        reprovisionNewUserCreditsPage.clickOnShowDetailsLink();
        AssertCollector.assertThat("Permission denied error message for Non-Ebso user is not correct.",
            reprovisionNewUserCreditsPage.getErrorDetails(),
            equalTo("This operation requires the credits.reprovision permission."), assertionErrorList);

        // Logout from non EBSO user
        adminToolPage.logout();
        // Login back with EBSO user
        adminToolPage.login();
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "subscriptionCurrencyFulfillmentStatus")
    public Object[][] getTestDataForSubscriptionCurrencyFulfillmentStatus() {
        return new Object[][] { { SubscriptionCurrencyFulfillmentStatus.PENDING.ordinal() },
                { SubscriptionCurrencyFulfillmentStatus.FAILED.ordinal() } };
    }

    // Method to find eligible subscription in db which can be used for Reprovision New User cloud credit.
    private HashMap<String, String> getEligibleSubscription(final int subscriptionCurrencyFulfillmentStatus) {
        final HashMap<String, String> subscriptionMap = new HashMap<>();
        String subscriptionId = "";
        String subscriptionCurrencyFulfillmentId = "";

        // Sql to check if there is a valid subscription in subscription_currency_fulfillment
        final String selectSql =
            "select scf.id, subscription_id" + "\n from subscription_currency_fulfillment scf, subscription s"
                + "\n where scf.subscription_id = s.id and" + "\n s.app_family_id = "
                + getEnvironmentVariables().getAppFamilyId() + " and " + "\n s.owner_id = "
                + getEnvironmentVariables().getUserId() + "\n and CURRENCY_SKU = 'SUBSCRIPTIONUSER'"
                + " order by id desc" + "\n limit 1";

        final List<Map<String, String>> resultMapList = DbUtils.selectQuery(selectSql, getEnvironmentVariables());

        // Inserting a record in subscription_currency_fulfillment table in dev2 only if there is no eligible
        // subscription found.
        // Not inserting in stage on purpose since its not ideal.
        if (resultMapList.size() == 0
            && getEnvironmentVariables().getEnvironmentType().equals(PelicanConstants.DEFAULT_ENVIRONMENT)) {
            LOGGER.info("No record found in db to run the test.");
            final String selectSubscription =
                "select ID from subscription where app_family_id =  " + getEnvironmentVariables().getAppFamilyId()
                    + " and owner_id = " + getEnvironmentVariables().getUserId() + " order by id desc limit 1";
            final List<Map<String, String>> resultMapListSubscription =
                DbUtils.selectQuery(selectSubscription, getEnvironmentVariables());
            subscriptionId = resultMapListSubscription.get(0).get("ID");
            final String todayDate = "'" + DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(15),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT) + " 00:00:00" + "'";
            final String insertQuery =
                "insert into subscription_currency_fulfillment(created, last_modified, subscription_id, currency_sku, fulfillment_status, quantity) \n"
                    + " values (%s , %s, %s, 'SUBSCRIPTIONUSER', %s, 1)";
            DbUtils.updateQuery(
                String.format(insertQuery, todayDate, todayDate, subscriptionId, subscriptionCurrencyFulfillmentStatus),
                getEnvironmentVariables());

            final String selectSqlSCF =
                "select ID from subscription_currency_fulfillment" + "\n where subscription_id = %s ";
            final List<Map<String, String>> resultMapListSCF =
                DbUtils.selectQuery(String.format(selectSqlSCF, subscriptionId), getEnvironmentVariables());
            subscriptionCurrencyFulfillmentId = resultMapListSCF.get(0).get("ID");
        } // Failing the test if no eligible subscription is found and environment is not dev2.
        else if (resultMapList.size() == 0
            && !getEnvironmentVariables().getEnvironmentType().equals(PelicanConstants.DEFAULT_ENVIRONMENT)) {
            Assert.fail("There is no record in db to run the test");
        } // If a eligible subscription is found then updating the FULFILLMENT_STATUS
        else {
            subscriptionId = resultMapList.get(0).get("SUBSCRIPTION_ID");
            subscriptionCurrencyFulfillmentId = resultMapList.get(0).get("ID");
            final String updateSql =
                "update subscription_currency_fulfillment set FULFILLMENT_STATUS = %s, CURRENCY_SKU = 'SUBSCRIPTIONUSER' where id = %s";

            DbUtils.updateQuery(
                String.format(updateSql, subscriptionCurrencyFulfillmentStatus, subscriptionCurrencyFulfillmentId),
                getEnvironmentVariables());

        }
        LOGGER.info("Subscription Id: " + subscriptionId);
        LOGGER.info("subscriptionCurrencyFulfillmentId: " + subscriptionCurrencyFulfillmentId);
        subscriptionMap.put("subscriptionId", subscriptionId);
        subscriptionMap.put("subscriptionCurrencyFulfillmentId", subscriptionCurrencyFulfillmentId);
        return subscriptionMap;
    }
}
