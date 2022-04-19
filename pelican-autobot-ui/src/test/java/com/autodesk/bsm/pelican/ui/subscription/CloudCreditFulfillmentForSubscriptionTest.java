package com.autodesk.bsm.pelican.ui.subscription;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.CC_SERVICE_PRIVILEGE_DESCRIPTION;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.CC_UNIT_OF_MEASURE_UNIT_AVAILABLE;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.CLOUD_CREDITS_TRLCR_GRANTED_IN_CONVERGENT_CHARGING;
import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.soap.convergentcharging.QuerySubscriptionBalanceResponse;
import com.autodesk.bsm.pelican.soap.convergentcharging.ServicePrivilege;
import com.autodesk.bsm.pelican.soap.convergentcharging.Unit;
import com.autodesk.bsm.pelican.soapclient.ConvergentChargingSoapClient;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.List;

/**
 * Test class to validate Cloud Credit Fulfillment and Re-trigger functionality for Subscription.
 *
 * @author t_mohav
 */
public class CloudCreditFulfillmentForSubscriptionTest extends SeleniumWebdriver {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CloudCreditFulfillmentForSubscriptionTest.class.getSimpleName());
    private PelicanPlatform resource;
    private static FindSubscriptionsPage findSubscriptionsPage;
    private String subscriptionOfferExternalKey;

    /**
     * Data setup - open a admin tool page and login into it
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final AddSubscriptionPlanPage addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.TRL, "OfferingDetails1 (DC020500)",
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")", SupportLevel.BASIC, null,
            true);

        // Add Currency Amount Entitlement
        final String currencyName = getTrialCurrencyName() + " (" + getTrialCurrencyName() + ")";
        addSubscriptionPlan.addOneTimeCurrencyAmountEntitlement("100", currencyName, 0);

        subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        addSubscriptionPlan.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, false);

        final String priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        addSubscriptionPlan.addPricesInOffer(1, getStoreUs().getName(), getPricelistExternalKeyUs(), "40",
            priceStartDate, priceEndDate);

        // Click on Save
        addSubscriptionPlan.clickOnSave(false);

    }

    /**
     * This test method verifies that trial cloud credits are granted to existing user when a trial subscription is
     * created for existing user. Trial subscription plan should have one time entitlement as Currency with TRLCR.
     */
    @Test
    public void testCloudCreditFulfillmentForExistingUserForTrialSubscription() {

        // Get the provisioned CloudCredit Value from Convergent Charging - Before clicking Retrigger button
        final BigInteger availableCloudCreditValueBefore = getAvailableCloudCreditValueFromConvergentCharging();

        // Create a Subscription Trial Using the API
        final Subscription trialSubscription = resource.subscription()
            .add(getEnvironmentVariables().getUserExternalKey(), subscriptionOfferExternalKey, Currency.USD);
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        final String trialSubscriptionId = trialSubscription.getId();
        LOGGER.info("Trial Subscription Id : " + trialSubscriptionId);

        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(trialSubscriptionId);

        final List<SubscriptionActivity> subscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final int subscriptionActivityListSize = subscriptionActivityList.size();

        if (subscriptionActivityListSize < 4) {
            Assert.fail("Subscription activity list size should be greater than or equal to 4");
        }
        AssertCollector.assertThat(
            "Subscription activity memo for Cloud Credit Fulfillment is not correct for subscription id: "
                + trialSubscriptionId,
            subscriptionActivityList.get(3).getMemo(),
            equalTo(String.format(PelicanConstants.CLOUD_CREDITS_TRLCR_GRANTED_IN_CONVERGENT_CHARGING, "100")),
            assertionErrorList);

        AssertCollector.assertThat(
            "Subscription activity memo for Cloud Credit Fulfillment call sent to triggers is not correct for subscription id: "
                + trialSubscriptionId,
            subscriptionActivityList.get(2).getMemo(),
            equalTo(String.format(PelicanConstants.CLOUD_CREDITS_TRLCR_MESSAGE_SENT_TO_QUEUE, "100")),
            assertionErrorList);

        // Get the provisioned CloudCredit Value from Convergent Charging - After clicking Re trigger button
        final BigInteger availableCloudCreditValueAfter = getAvailableCloudCreditValueFromConvergentCharging();

        // Validate available Cloud Credits value increased by 100 in the QuerySubscriptionBalanceResponse
        AssertCollector.assertThat("Credit not Provisioned in Convergent Charging",
            availableCloudCreditValueAfter.subtract(availableCloudCreditValueBefore).intValueExact(), equalTo(100),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method create verifies that after clicking Re-trigger Cloud Credit Fulfillment cloud credits are provisioned
     * in Convergent Charging.
     *
     */
    @Test
    public void testReTriggerCloudCreditFulfillmentForExistingUser() {
        // Create a Subscription Trial Using the API
        final Subscription trialSubscription = resource.subscription()
            .add(getEnvironmentVariables().getUserExternalKey(), subscriptionOfferExternalKey, Currency.USD);
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        final String trialSubscriptionId = trialSubscription.getId();
        LOGGER.info("Trial Subscription Id : " + trialSubscriptionId);

        // Update subscription_currency_fulfillment table with FULFILLMENT_STATUS as "3"
        final String updateSubscriptionCurrencyFulfillmentQuery = "UPDATE subscription_currency_fulfillment\n"
            + "SET FULFILLMENT_STATUS = 3\n" + "WHERE SUBSCRIPTION_ID = " + trialSubscriptionId;
        DbUtils.updateQuery(updateSubscriptionCurrencyFulfillmentQuery, getEnvironmentVariables());

        // Find the above created Subscription
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(trialSubscriptionId);

        // Validate 'Re-trigger Cloud Credit Fulfillment' button is not present
        AssertCollector.assertTrue("Re-trigger Cloud Credit Fulfillment is NOT present",
            subscriptionDetailPage.isReTriggerCloudCreditFulfillmentButtonPresent(), assertionErrorList);

        // Get the provisioned CloudCredit Value from Convergent Charging - Before clicking Retrigger button
        final BigInteger availableCloudCreditValueBefore = getAvailableCloudCreditValueFromConvergentCharging();

        // Click on the Re-trigger Cloud Credit Fulfillment
        subscriptionDetailPage.clickReTriggerCloudCreditFulfillmentButton();

        // Validate "Cloud Credits (100 TRLCR). Granted in Convergent Charging." is displayed in the Subscription
        // Activity
        final List<SubscriptionActivity> subscriptionActivities = subscriptionDetailPage.getSubscriptionActivity();
        if (subscriptionActivities.size() > 3) {
            final SubscriptionActivity subscriptionActivity = subscriptionActivities.get(3);
            AssertCollector.assertThat("Failed to Provision cloud credit in Convergent Charging",
                subscriptionActivity.getMemo().trim(),
                equalTo(String.format(CLOUD_CREDITS_TRLCR_GRANTED_IN_CONVERGENT_CHARGING, "100")), assertionErrorList);
        }

        // Validate 'Re-trigger Cloud Credit Fulfillment' button is not present
        AssertCollector.assertFalse("Re-trigger Cloud Credit Fulfillment is still present",
            subscriptionDetailPage.isReTriggerCloudCreditFulfillmentButtonPresent(), assertionErrorList);

        // Get the provisioned CloudCredit Value from Convergent Charging - After clicking Retrigger button
        final BigInteger availableCloudCreditValueAfter = getAvailableCloudCreditValueFromConvergentCharging();

        // Validate available Cloud Credits value increased by 100 in the QuerySubscriptionBalanceResponse
        if (null != availableCloudCreditValueBefore && null != availableCloudCreditValueAfter) {
            AssertCollector.assertThat(
                "Credit not Provisioned in Convergent Charging for subscription id: " + trialSubscriptionId,
                availableCloudCreditValueAfter.subtract(availableCloudCreditValueBefore).intValueExact(), equalTo(100),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to get the value of Available Cloud Credit Value
     *
     * @return available Cloud Credit Value QuerySubscriptionBalance Response
     */
    private BigInteger getAvailableCloudCreditValueFromConvergentCharging() {
        // Get the response Object from Convergent charging
        final QuerySubscriptionBalanceResponse response =
            ConvergentChargingSoapClient.getInstance().getQuerySubscriptionBalanceResponseObject(
                getEnvironmentVariables().getUserContractNumberForConvergentCharging());
        if (response != null) {
            // Getting the first contract from the list
            final QuerySubscriptionBalanceResponse.ListOfContracts.Contract contract =
                response.getListOfContracts().getContract().get(0);

            // Find out and return the value of "available" unit
            for (final ServicePrivilege servicePrivilege : contract.getListOfServicePrivileges()
                .getServicePrivilege()) {
                if (servicePrivilege.getDescription().equalsIgnoreCase(CC_SERVICE_PRIVILEGE_DESCRIPTION)) {
                    for (final Unit unit : servicePrivilege.getListOfUnits().getUnit()) {
                        if (unit.getType().equalsIgnoreCase(CC_UNIT_OF_MEASURE_UNIT_AVAILABLE)) {
                            LOGGER.info(
                                "Available Cloud Credit Value QuerySubscriptionBalance Response:" + unit.getValue());
                            return unit.getValue();
                        }
                    }
                }
            }
        } else {
            LOGGER.info("Error occurred while sending SOAP Request to Server");
        }
        return null;
    }
}
