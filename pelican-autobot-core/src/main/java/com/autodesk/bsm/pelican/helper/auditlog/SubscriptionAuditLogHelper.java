package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * This class serves as a common class for Dynamo DB query of Subscription and assertion
 *
 * @author Shweta Hegde
 */
public class SubscriptionAuditLogHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditLogHelper.class.getSimpleName());

    /**
     * Helper to Query Dynamo DB for refund Subscription.
     *
     * @param subscriptionId
     * @param oldStatus
     * @param newStatus
     * @param oldExpirationDate
     * @param oldDelinquentResolveByDate
     * @param statusChange
     * @param assertionErrorList
     * @return boolean
     */
    public static boolean helperToQueryDynamoDbForSubscriptionWithPOChange(final String subscriptionId,
        final Status oldStatus, final Status newStatus, final String oldExpirationDate,
        final String oldDelinquentResolveByDate, final boolean statusChange,
        final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscription(subscriptionId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(Action.UPDATE.toString())) {

                if (statusChange) {
                    commonAssertionsForAuditLogStatusChangeScenarios(auditData, oldStatus, newStatus, oldExpirationDate,
                        oldDelinquentResolveByDate, assertionErrorList);
                } else {

                    AssertCollector.assertThat("Incorrect pending payment old value",
                        auditData.get(PelicanConstants.PENDING_PAYMENT).getOldValue(),
                        equalTo(StringUtils.lowerCase(PelicanConstants.TRUE)), assertionErrorList);

                    AssertCollector.assertThat("Incorrect pending payment new value",
                        auditData.get(PelicanConstants.PENDING_PAYMENT).getNewValue(),
                        equalTo(StringUtils.lowerCase(PelicanConstants.FALSE)), assertionErrorList);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * This is a method to query the dynamo db for the required subscription
     *
     * @param subscriptionId - subscription id
     * @param oldPlanId - old plan id
     * @param newPlanId - new plan id
     * @param oldOfferId - old offer id
     * @param newOfferId - new offer id
     * @param oldPriceId - old price id
     * @param newPriceId - new price id
     * @param assertionErrorList
     * @return boolean
     */
    public static void helperToQueryDynamoDbForSubscriptionUpdateForMigration(final String subscriptionId,
        final String oldPlanId, final String newPlanId, final String oldOfferId, final String newOfferId,
        final String oldPriceId, final String newPriceId, final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscription(subscriptionId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        if (auditLogEntries.size() > 0) {
            final Map<String, ChangeDetails> auditData = auditLogEntries.get(0).getChangeDetailsAsMap();
            if (auditLogEntries.get(0).getAction().equals(Action.UPDATE.toString())) {
                commonAssertionsForAuditLogSubscriptionPriceUpdateScenarios(auditData, oldPlanId, newPlanId, oldOfferId,
                    newOfferId, oldPriceId, newPriceId, assertionErrorList);
            } else {
                Assert.fail("There are no entries in audit log table for update action for that subscription id:"
                    + subscriptionId);
            }
        } else {
            Assert.fail("There are no entries in audit log table for subscription id:" + subscriptionId);
        }
    }

    /**
     * Common assertions for Subscription change on Mark As Refund/Refund
     *
     * @param assertionErrorList
     */
    public static void commonAssertionsForAuditLogStatusChangeScenarios(final Map<String, ChangeDetails> auditData,
        final Status oldStatus, final Status newStatus, final String oldExpirationDate,
        final String oldDelinquentResolveByDate, final List<AssertionError> assertionErrorList) {

        // assertion on status
        if (oldStatus != null) {
            AssertCollector.assertThat("Invalid subscription status old value",
                auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus.toString()),
                assertionErrorList);
        }

        // Delinquent Resolve By date assertion (this is only specific to
        // DELINQENT to EXPIRED state
        if (oldStatus == Status.DELINQUENT) {

            AssertCollector.assertThat("Incorrect delinquent resolve by date old value",
                auditData.get(PelicanConstants.DELINQUENT_RESOLVE_BY_DATE).getOldValue().split("\\s")[0],
                equalTo(oldDelinquentResolveByDate), assertionErrorList);

            AssertCollector.assertThat("Incorrect delinquent resolve by date new value",
                auditData.get(PelicanConstants.DELINQUENT_RESOLVE_BY_DATE).getNewValue(), nullValue(),
                assertionErrorList);
        }

        if (newStatus != null) {
            AssertCollector.assertThat("Invalid subscription status new value",
                auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()),
                assertionErrorList);
        }

        // assertion on expiration date
        if (oldExpirationDate == null) {

            AssertCollector.assertThat("Incorrect expiration date old value",
                auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue(), equalTo(oldExpirationDate),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect expiration date old value",
                auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue().split("\\s")[0],
                equalTo(oldExpirationDate), assertionErrorList);
        }

        AssertCollector.assertThat("Incorrect expiration date new value",
            auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue().split("\\s")[0],
            equalTo(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_FORMAT)), assertionErrorList);

        // Assertions on last modified date, this is just to make sure it is
        // updated, that is why only not null assertion
        AssertCollector.assertThat("Incorrect last modified date old value",
            auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), notNullValue(), assertionErrorList);

        AssertCollector.assertThat("Incorrect last modified date new value",
            auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);

    }

    /**
     * This is a method to do assertions on subscription price id update scenarios.
     *
     * @param auditData - audit data
     * @param oldPlanId - old plan id
     * @param newPlanId - new plan id
     * @param oldOfferId - old offer id
     * @param newOfferId - new offer id
     * @param oldPriceId - old price id
     * @param newPriceId - new price id
     * @param assertionErrorList
     */
    private static void commonAssertionsForAuditLogSubscriptionPriceUpdateScenarios(
        final Map<String, ChangeDetails> auditData, final String oldPlanId, final String newPlanId,
        final String oldOfferId, final String newOfferId, final String oldPriceId, final String newPriceId,
        final List<AssertionError> assertionErrorList) {

        // Assertion on plan id
        if (oldPlanId != null) {
            AssertCollector.assertThat("Invalid old value for subscription plan id",
                auditData.get(PelicanConstants.PLAN_ID).getOldValue(), equalTo(oldPlanId), assertionErrorList);
        }

        if (newPlanId != null) {
            AssertCollector.assertThat("Invalid new value for subscription plan id",
                auditData.get(PelicanConstants.PLAN_ID).getNewValue(), equalTo(newPlanId), assertionErrorList);
        }

        // Assertions on offer id
        if (oldOfferId != null) {
            AssertCollector.assertThat("Invalid old value for subscription offer id",
                auditData.get(PelicanConstants.OFFER).getOldValue(), equalTo(oldOfferId), assertionErrorList);
        }

        if (newOfferId != null) {
            AssertCollector.assertThat("Invalid new value for subscription offer id",
                auditData.get(PelicanConstants.OFFER).getNewValue(), equalTo(newOfferId), assertionErrorList);
        }

        // Assertions on price id
        if (oldPriceId != null) {
            AssertCollector.assertThat("Invalid old value for subscription price id",
                auditData.get(PelicanConstants.PRICE_ID).getOldValue(), equalTo(oldPriceId), assertionErrorList);
        }

        if (newPriceId != null) {
            AssertCollector.assertThat("Invalid new value for subscription price id",
                auditData.get(PelicanConstants.PRICE_ID).getNewValue(), equalTo(newPriceId), assertionErrorList);
        }

        // Assertions on last modified date
        AssertCollector.assertThat("Incorrect last modified date old value",
            auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), notNullValue(), assertionErrorList);

        AssertCollector.assertThat("Incorrect last modified date new value",
            auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);

    }

    /**
     * This method does query Dynamo DB and validates the assertions
     *
     * @param assertionErrorList
     *
     * @return true if finds the Audit Log, false otherwise.
     */
    public static boolean helperToValidateDynamoDbForCreateSubscriptions(final String subscriptionId,
        final Status newStatus, final String externalKey, final String priceId, final String planId,
        final String ownerId, final String userId, final String fileName,
        final EnvironmentVariables environmentVariables, final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscription(subscriptionId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(Action.CREATE.toString())) {

                commonAssertionsForAuditLogCreateScenarios(auditData, subscriptionId, newStatus, externalKey, priceId,
                    planId, ownerId, environmentVariables, assertionErrorList);

                AssertCollector.assertThat("Incorrect user id", auditLogEntry.getUserId(), equalTo(userId),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect IP", auditLogEntry.getIpAddress(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect upload file name", auditLogEntry.getFileName(), equalTo(fileName),
                    assertionErrorList);
                return true;
            }
        }
        return false;
    }

    /**
     * This method does common assertions
     *
     * @param assertionErrorList
     */
    private static void commonAssertionsForAuditLogCreateScenarios(final Map<String, ChangeDetails> auditData,
        final String subscriptionId, final Status newStatus, final String externalKey, final String priceId,
        final String planId, final String ownerId, final EnvironmentVariables environmentVariables,
        final List<AssertionError> assertionErrorList) {

        // Assertion on id
        AssertCollector.assertThat("Incorrect id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.ID).getNewValue(), equalTo(subscriptionId), assertionErrorList);

        // Assertion on status
        AssertCollector.assertThat("Incorrect status old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.STATUS).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect status new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()), assertionErrorList);

        // Assertion on external key
        if (StringUtils.isNotEmpty(externalKey)) {
            AssertCollector.assertThat("Incorrect external key old value for Subscription : " + subscriptionId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key new value for Subscription : " + subscriptionId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(externalKey), assertionErrorList);
        }

        // Assertion on app family id
        AssertCollector.assertThat("Incorrect app family id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.APP_FAMILY_ID).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect app family id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.APP_FAMILY_ID).getNewValue(), equalTo(environmentVariables.getAppFamilyId()),
            assertionErrorList);

        // Assertion on last modified
        AssertCollector.assertThat("Incorrect last modified old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect last modified new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);

        // Assertion on Price Id
        AssertCollector.assertThat("Incorrect price id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.PRICE_ID).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.PRICE_ID).getNewValue(), equalTo(priceId), assertionErrorList);

        // Assertion on Plan Id
        AssertCollector.assertThat("Incorrect plan id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.PLAN_ID).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect plan id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.PLAN_ID).getNewValue(), equalTo(planId), assertionErrorList);

        // Assertion on quantity
        final String quantity = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_FIELD_FROM_SUBSCRIPTION, PelicanConstants.QUANTITY, subscriptionId),
            PelicanConstants.QUANTITY, environmentVariables).get(0);

        AssertCollector.assertThat("Incorrect quantity old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.QUANTITY).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.QUANTITY).getNewValue(), equalTo(quantity), assertionErrorList);

        // Assertion on CREATED date
        AssertCollector.assertThat("Incorrect created old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.CREATED).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect created new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.CREATED).getNewValue(), notNullValue(), assertionErrorList);

        // Assertion on LAST BILLING CYCLE DAYS
        final String lastBillingCycleDays = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_FIELD_FROM_SUBSCRIPTION, "LAST_BILLING_CYCLE_DAYS", subscriptionId),
            "LAST_BILLING_CYCLE_DAYS", environmentVariables).get(0);

        AssertCollector.assertThat("Incorrect last billing cycle days old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.LAST_BILLING_CYCLE_DAYS).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect last billing cycle days new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.LAST_BILLING_CYCLE_DAYS).getNewValue(), equalTo(lastBillingCycleDays),
            assertionErrorList);

        // Assertion on DAYS CREDITED
        AssertCollector.assertThat("Incorrect days credited old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.DAYS_CREDITED).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect days credited new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.DAYS_CREDITED).getNewValue(), equalTo("0"), assertionErrorList);

        // Assertion on OWNER ID
        AssertCollector.assertThat("Incorrect owner id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.OWNER_ID).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect owner id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.OWNER_ID).getNewValue(), equalTo(ownerId), assertionErrorList);

        // Assertion on OFFER ID
        final String offerId = DbUtils
            .selectQuery(String.format(PelicanDbConstants.SELECT_FIELD_FROM_SUBSCRIPTION, "OFFER_ID", subscriptionId),
                "OFFER_ID", environmentVariables)
            .get(0);

        AssertCollector.assertThat("Incorrect offer id old value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.OFFER).getOldValue(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer id new value for Subscription : " + subscriptionId,
            auditData.get(PelicanConstants.OFFER).getNewValue(), equalTo(offerId), assertionErrorList);

    }

    /**
     * Helper to Query Dynamo DB for edit Subscription.
     *
     * @param subscriptionId
     * @param oldNextBillingDate
     * @param newNextBillingDate
     * @param oldExpirationDate
     * @param newExpirationDate
     * @param oldStatus
     * @param newStatus
     * @param assertionErrorList
     * @return
     */
    public static boolean helperToQueryDynamoDbForEditSubscription(final String subscriptionId,
        final String oldNextBillingDate, final String newNextBillingDate, final String oldExpirationDate,
        final String newExpirationDate, final String oldStatus, final String newStatus,
        final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscription(subscriptionId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(Action.UPDATE.toString())) {
                commonAssertionsForAuditLogEditSubscription(auditData, oldNextBillingDate, newNextBillingDate,
                    oldExpirationDate, newExpirationDate, oldStatus, newStatus, assertionErrorList);
                return true;
            }
        }
        return false;
    }

    /**
     * Common assertions for Edit Subscription.
     *
     * @param auditData
     * @param oldNextBillingDate
     * @param newNextBillingDate
     * @param oldExpirationDate
     * @param newExpirationDate
     * @param oldStatus
     * @param newStatus
     * @param assertionErrorList
     */
    private static void commonAssertionsForAuditLogEditSubscription(final Map<String, ChangeDetails> auditData,
        final String oldNextBillingDate, final String newNextBillingDate, final String oldExpirationDate,
        final String newExpirationDate, final String oldStatus, final String newStatus,
        final List<AssertionError> assertionErrorList) {

        // assertion on next billing date
        if (oldNextBillingDate != null && newNextBillingDate != null) {
            LOGGER.info("Validating next billing date.");
            AssertCollector.assertThat("Invalid next billing date old value",
                auditData.get(PelicanConstants.NEXT_BILLING_DATE_AUDIT_LOG).getOldValue(), equalTo(oldNextBillingDate),
                assertionErrorList);
            AssertCollector.assertThat("Invalid next billing date new value",
                auditData.get(PelicanConstants.NEXT_BILLING_DATE_AUDIT_LOG).getNewValue(), equalTo(newNextBillingDate),
                assertionErrorList);

        }
        // assertion on expiration date
        if ((oldExpirationDate == null && newExpirationDate != null)
            || (oldExpirationDate != null && newExpirationDate != null)) {
            LOGGER.info("Validating expiration date.");
            if (oldExpirationDate != null) {
                AssertCollector.assertThat("Incorrect expiration date old value",
                    auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue().split("\\s+")[0],
                    equalTo(oldExpirationDate.split("\\s+")[0]), assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect expiration date old value",
                    auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue(), equalTo(oldExpirationDate),
                    assertionErrorList);
            }
            AssertCollector.assertThat("Incorrect expiration date new value",
                auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue().split("\\s+")[0],
                equalTo(newExpirationDate.split("\\s+")[0]), assertionErrorList);
        }
        // assertion on status
        if (oldStatus != null && newStatus != null) {
            LOGGER.info("Validating status.");
            AssertCollector.assertThat("Invalid subscription status old value",
                auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus), assertionErrorList);
            AssertCollector.assertThat("Invalid subscription status new value",
                auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus), assertionErrorList);
        }

        // Assertions on last modified date, this is to make that the subscription last modified was changed.
        AssertCollector.assertThat("Last modified date old and new value should not be same",
            auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(),
            not(auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue()), assertionErrorList);
    }

}
