package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PurchaseOrdersClient.PurchaseOrderParameter;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfiles;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Test class to test Trial Subscription creation *** Important : Need to add more test cases, as this class was created
 * to handle an emergency
 *
 * @author kishor
 */
public class CreateSubscriptionTest extends BaseTestData {

    private PelicanPlatform resource;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private String subscriptionOfferExternalKey;
    private Subscription trialSubscription;
    private String subscriptionEcStatus;
    private String subscriptionEcLastUpdated;
    private static final String NOT_ADMIN_TOOL_USER = "0";
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSubscriptionTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
    }

    @Test(dataProvider = "createTrialSubscriptionData")
    public void testCreateSubscriptionAPI(final String testDescription, final String pelicanUser,
        final String offerExternalKey, final Currency currency) {

        trialSubscription = resource.subscription().add(pelicanUser, offerExternalKey, currency);
        subscriptionEcStatus =
            DbUtils.selectQuery("Select ec_status from Subscription where id = " + trialSubscription.getId(),
                "ec_status", getEnvironmentVariables()).get(0);
        subscriptionEcLastUpdated =
            DbUtils.selectQuery("Select ec_last_updated from Subscription where id = " + trialSubscription.getId(),
                "ec_last_updated", getEnvironmentVariables()).get(0);

        AssertCollector.assertThat("Invalid user", trialSubscription.getOwnerId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid Subscription Offer", trialSubscription.getCurrentOffer().getName(),
            equalTo(offerExternalKey), assertionErrorList);
        AssertCollector.assertThat("Subscription EC status should be Active", subscriptionEcStatus, equalTo("5"),
            assertionErrorList);
        AssertCollector.assertThat("Subscription EC last modified date should be null", subscriptionEcLastUpdated,
            equalTo(null), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests creating trial subscription for a user who does not exist in Pelican Once the user is added in
     * Pelican, the user should not be added as a Admin Tool User Test cases validates that user is not a Admin Tool
     * User
     */
    @Test(dependsOnMethods = { "testCreateSubscriptionAPI" })
    public void testCreateTrialSubscriptionForNonExistingPelicanUser() {

        // create a random oxygen id and create a trial subscription for the
        // oxygen id
        final String randomUser = "AutoTestSubscriptionUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        trialSubscription = resource.subscription().add(randomUser, subscriptionOfferExternalKey, Currency.USD);

        subscriptionEcStatus =
            DbUtils.selectQuery("Select ec_status from Subscription where id = " + trialSubscription.getId(),
                "ec_status", getEnvironmentVariables()).get(0);
        subscriptionEcLastUpdated =
            DbUtils.selectQuery("Select ec_last_updated from Subscription where id = " + trialSubscription.getId(),
                "ec_last_updated", getEnvironmentVariables()).get(0);

        // Select the result for the added user
        final List<String> selectResult =
            DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where XKEY = '" + randomUser + "'",
                "IS_ADMINTOOL_USER", getEnvironmentVariables());

        // Assertions
        AssertCollector.assertThat("User should NOT be a Admin Tool user", selectResult.get(0),
            equalTo(NOT_ADMIN_TOOL_USER), assertionErrorList);
        AssertCollector.assertThat("Invalid user", trialSubscription.getOwnerId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid Subscription Offer", trialSubscription.getCurrentOffer().getName(),
            equalTo(subscriptionOfferExternalKey), assertionErrorList);
        AssertCollector.assertThat("Subscription EC status should be Active", subscriptionEcStatus, equalTo("5"),
            assertionErrorList);
        AssertCollector.assertThat("Subscription EC last modified date should be null", subscriptionEcLastUpdated,
            equalTo(null), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "createTrialSubscriptionData")
    public Object[][] createTrialSubscriptionData() {
        final Offerings subscriptionPlan =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        subscriptionOfferExternalKey = subscriptionPlan.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Random rnd = new Random();
        final int randomNumericUser = (100000 + rnd.nextInt(900000));

        return new Object[][] {
                { "Create Trial Subscription With Valid User and Valid Subscription Offer", getUser().getExternalKey(),
                        subscriptionOfferExternalKey, Currency.USD },
                { "Create Trial Subscription With Non Existing Numeric User and Valid Subscription Offer",
                        Integer.toString(randomNumericUser), subscriptionOfferExternalKey, Currency.USD } };
    }

    /*
     * @Author: Sumant Manda Test case for US5200 Description: 1) Make sure New User when registered doesnt have Payment
     * Profile 2) When subscribed to Trail Subscription, No Purchase Order should be generated
     */
    @Test
    public void testNoPaymentProfileNoPOTrialSubscription() {

        // Create New Pelican User to AUTO Family with dynamic Username
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(),
            "Automation_Test_User_" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS"));
        final User user = new UserUtils().createPelicanUser(userParams, getEnvironmentVariables());

        // Get the list of all Paymentprofiles associated to the new user, which
        // is obvious
        final Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", user.getId());
        paramsMap.put(PurchaseOrderParameter.SKIP_COUNT.getName(), "true");
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        if (Integer.parseInt(paymentProfiles.getTotal()) == 0) {
            LOGGER.info("No PaymentProfiles found for New User, Assert Pass");
        } else {
            AssertCollector.assertThat("Found Payment Profile for New user", paymentProfiles.getTotal(), equalTo("0"),
                assertionErrorList);
        }

        // Create Trial Subscription
        final Offerings subscriptionPlan =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String subscriptionOfferExternalKey =
            subscriptionPlan.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Subscription trialSubscription =
            resource.subscription().add(user.getExternalKey(), subscriptionOfferExternalKey, Currency.USD);
        AssertCollector.assertThat("Invalid user", trialSubscription.getOwnerId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid Subscription Offer", trialSubscription.getCurrentOffer().getName(),
            equalTo(subscriptionOfferExternalKey), assertionErrorList);

        // Verify again for paymentprofile associated for the new User
        if (Integer.parseInt(paymentProfiles.getTotal()) == 0) {
            LOGGER.info("No PaymentProfiles found for New User, Assert Pass");
        } else {
            AssertCollector.assertThat("Found Payment Profile for New user", paymentProfiles.getTotal(), equalTo("0"),
                assertionErrorList);
        }

        // Verify Purchase Order for the User in tempestdb using the
        // SubscriptionId
        final List<Integer> PurchaseOrderlist =
            DbUtils.getPurchaseOrderfromSubscription(trialSubscription.getId(), getEnvironmentVariables());

        // Make sure No Purchase Orders are associated to the User when
        // subscribed to the trail subscription
        if (PurchaseOrderlist.size() == 0) {
            LOGGER.info("No Purchase Order found for New User with Trial subscription, Assert Pass");
        } else {
            AssertCollector.assertThat("Found Purchase Order for New user with trail subscription",
                PurchaseOrderlist.size(), equalTo(1), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);

    }
}
