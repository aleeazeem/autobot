package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class contains helper methods to validate audit log entries related to subscription plan.
 *
 * @author jains
 */

public class SubscriptionPlanAuditLogHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanAuditLogHelper.class.getSimpleName());

    /**
     * This method contains audit data validation when one time entitlement is added/deleted/updated from subscription
     * plan.
     *
     * @param isNewFeatureAdd
     * @param assertionErrorList
     *
     * @return Boolean
     */
    public static Boolean validateSubscriptionPlanEntitlement(final String subscriptionPlanId,
        final String entitlementIdOldValue, final String entitlementIdNewValue, final Action action,
        final boolean isNewFeatureAdd, final List<AssertionError> assertionErrorList) {
        String oldEntitlementIdValue = null;
        String newEntitlementIdValue = null;
        // Adding [] to the entitlement value since it is a list in audit log
        if (entitlementIdOldValue != null) {
            oldEntitlementIdValue = "[" + entitlementIdOldValue + "]";
        }
        if (isNewFeatureAdd) {
            if (entitlementIdNewValue != null) {
                newEntitlementIdValue = "[" + entitlementIdOldValue + ", " + entitlementIdNewValue + "]";
            }
        } else {
            if (entitlementIdNewValue != null) {
                newEntitlementIdValue = "[" + entitlementIdNewValue + "]";
            }
        }

        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscriptionPlan(subscriptionPlanId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            if (auditLogEntry.getAction().equals(action.toString())) {
                final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
                LOGGER.info("audit data: " + auditData.toString());

                // Assertion on entitlement id
                if (!(oldEntitlementIdValue == null && newEntitlementIdValue == null)) {
                    AssertCollector.assertThat("Incorrect oneTimeEntitlements old value",
                        auditData.get("oneTimeEntitlements").getOldValue(), equalTo(oldEntitlementIdValue),
                        assertionErrorList);

                    AssertCollector.assertThat("Incorrect oneTimeEntitlements new value",
                        auditData.get("oneTimeEntitlements").getNewValue(), equalTo(newEntitlementIdValue),
                        assertionErrorList);
                }

                if (!(Action.CREATE.equals(action))) {
                    // Assertions on last modified date
                    AssertCollector.assertThat("Incorrect last modified date old value",
                        auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), notNullValue(),
                        assertionErrorList);
                } else {
                    AssertCollector.assertThat("Incorrect last modified date old value",
                        auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), equalTo(null), assertionErrorList);
                }

                AssertCollector.assertThat("Incorrect last modified date new value",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Last modified date old and new value should not be same",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(),
                    not(auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue()), assertionErrorList);
                return true;
            }
        }
        return false;
    }

    /**
     * This method contains audit data validation for subscription entitlement table when one time entitlement is
     * added/deleted/updated in the table.
     *
     * @param subscriptionPlanIdOldValue
     * @param subscriptionPlanIdNewValue
     * @param featureIdOldValue
     * @param featureIdNewValue
     * @param idOldValue
     * @param idNewValue
     * @param licensingModelOldValue
     * @param licensingModelNewValue
     * @param coreProductsOldValue
     * @param coreProductsNewValue
     * @param action
     * @param assertionErrorList
     * @return
     */
    public static Boolean validateFeatureEntitlementInDynamoDB(final String subscriptionPlanIdOldValue,
        final String subscriptionPlanIdNewValue, final String featureIdOldValue, final String featureIdNewValue,
        final String idOldValue, final String idNewValue, final String licensingModelOldValue,
        final String licensingModelNewValue, final String coreProductsOldValue, final String coreProductsNewValue,
        final Action action, final List<AssertionError> assertionErrorList) {

        List<Map<String, AttributeValue>> items;
        String entitlementTypeOldValue = null;
        String entitlementTypeNewValue = null;
        String grantTypeOldValue = null;
        String grantTypeNewValue = null;
        String currencyIdOldValue = null;
        String currencyIdNewValue = null;

        // Querying dynamodb with not null id(this id is same as in subscription entitlement table)
        if (action == Action.CREATE) {
            items = DynamoDBUtil.subscriptionEntitlement(idNewValue);
            entitlementTypeNewValue = "ONE_TIME_PLAN";
            grantTypeNewValue = PelicanConstants.ITEM;
            currencyIdNewValue = "0";
        } else {
            items = DynamoDBUtil.subscriptionEntitlement(idOldValue);
            if (action == Action.DELETE) {
                entitlementTypeOldValue = "ONE_TIME_PLAN";
                grantTypeOldValue = PelicanConstants.ITEM;
                currencyIdOldValue = "0";
            }
        }

        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (auditLogEntry.getAction().equals(action.toString())) {
                // Validation on id, when action is update then id will not be updated and that's why
                // condition is in if block
                if ((idOldValue != null || idNewValue != null) && !(action == Action.UPDATE)) {
                    LOGGER.info("Validating id from subscription entitlement table");
                    AssertCollector.assertThat("Incorrect id(id in subscription_entitlement table) old value",
                        auditData.get("id").getOldValue(), equalTo(idOldValue), assertionErrorList);
                    AssertCollector.assertThat("Incorrect id(id in subscription_entitlement table) new value",
                        auditData.get("id").getNewValue(), equalTo(idNewValue), assertionErrorList);
                }

                // Validation on feature id/itemid
                if (featureIdOldValue != null || featureIdNewValue != null) {
                    LOGGER.info("Validating feature id");
                    AssertCollector.assertThat("Incorrect feature id old value", auditData.get("itemId").getOldValue(),
                        equalTo(featureIdOldValue), assertionErrorList);
                    AssertCollector.assertThat("Incorrect feature id new value", auditData.get("itemId").getNewValue(),
                        equalTo(featureIdNewValue), assertionErrorList);
                }

                if (action == Action.CREATE || action == Action.DELETE) {
                    // Validation on entitlementType
                    LOGGER.info("Validating entitlementType");
                    AssertCollector.assertThat("Incorrect entitlementType old value",
                        auditData.get("entitlementType").getOldValue(), equalTo(entitlementTypeOldValue),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect entitlementType new value",
                        auditData.get("entitlementType").getNewValue(), equalTo(entitlementTypeNewValue),
                        assertionErrorList);

                    // Validation on grant type
                    LOGGER.info("Validating grant type");
                    AssertCollector.assertThat("Incorrect grant type old value",
                        auditData.get("grantType").getOldValue(), equalTo(grantTypeOldValue), assertionErrorList);
                    AssertCollector.assertThat("Incorrect grant type new value",
                        auditData.get("grantType").getNewValue(), equalTo(grantTypeNewValue), assertionErrorList);

                    // Validation on currency id
                    LOGGER.info("Validating currency id");
                    AssertCollector.assertThat("Incorrect currency id old value",
                        auditData.get("amount.currencyId").getOldValue(), equalTo(currencyIdOldValue),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect currency id new value",
                        auditData.get("amount.currencyId").getNewValue(), equalTo(currencyIdNewValue),
                        assertionErrorList);

                }

                // Validation on licensing model
                if (licensingModelOldValue != null || licensingModelNewValue != null) {
                    LOGGER.info("Validating licensing model");
                    AssertCollector.assertThat("Incorrect licensing model old value",
                        auditData.get("licensingModel").getOldValue(), equalTo(licensingModelOldValue),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect licensing model new value",
                        auditData.get("licensingModel").getNewValue(), equalTo(licensingModelNewValue),
                        assertionErrorList);
                }

                if (coreProductsOldValue != null || coreProductsNewValue != null) {

                    final String[] expectedCoreProducts =
                        coreProductsNewValue.replace("[", "").replace("]", "").split(",");
                    final Set<String> coreProductSets = new HashSet<>();
                    for (final String coreProduct : expectedCoreProducts) {
                        coreProductSets.add(coreProduct.trim());
                    }
                    final String[] actualCoreProducts =
                        auditData.get("coreProducts").getNewValue().replace("[", "").replace("]", "").split(",");
                    for (final String coreProduct : actualCoreProducts) {
                        coreProductSets.remove(coreProduct.trim());
                    }

                    // validation on core products
                    AssertCollector.assertThat("Incorrect core product old value",
                        auditData.get("coreProducts").getOldValue(), equalTo(coreProductsOldValue), assertionErrorList);
                    AssertCollector.assertThat("Incorrect core product old value", coreProductSets.size(), is(0),
                        assertionErrorList);
                }

                // Validation on related id(subscription plan id)
                if (subscriptionPlanIdOldValue != null || subscriptionPlanIdNewValue != null) {
                    LOGGER.info("Validating subscription plan id");
                    AssertCollector.assertThat("Incorrect feature id old value",
                        auditData.get(PelicanConstants.RELATED_ID).getOldValue(), equalTo(subscriptionPlanIdOldValue),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect feature id new value",
                        auditData.get(PelicanConstants.RELATED_ID).getNewValue(), equalTo(subscriptionPlanIdNewValue),
                        assertionErrorList);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Method which queries dynamo db and validates fields
     *
     * @param oldId
     * @param newId
     * @param oldCurrencyId
     * @param newCurrencyId
     * @param oldAmount
     * @param newAmount
     * @param action
     * @param assertionErrorList
     */
    public static void assertionsOnCurrencyAmountEntitlement(final String relatedId, final String oldId,
        final String newId, final String oldCurrencyId, final String newCurrencyId, final String oldAmount,
        final String newAmount, final Action action, final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscriptionEntitlement(newId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (auditLogEntry.getAction().equals(action.toString())) {

                // Validation on related id(subscription plan id)
                if (action == Action.CREATE && relatedId != null) {
                    AssertCollector.assertThat("Incorrect related id old value",
                        auditData.get(PelicanConstants.RELATED_ID).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect related id new value",
                        auditData.get(PelicanConstants.RELATED_ID).getNewValue(), equalTo(relatedId),
                        assertionErrorList);
                }

                // Validation on entitlement id
                if (oldId != null || newId != null) {
                    AssertCollector.assertThat("Incorrect entitlement id old value",
                        auditData.get(PelicanConstants.ID).getOldValue(), equalTo(oldId), assertionErrorList);
                    AssertCollector.assertThat("Incorrect entitlement id new value",
                        auditData.get(PelicanConstants.ID).getNewValue(), equalTo(newId), assertionErrorList);
                }
                // Validation on grant type id
                if (action == Action.CREATE) {
                    AssertCollector.assertThat("Incorrect grant type old value",
                        auditData.get(PelicanConstants.GRANT_TYPE).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect grant type new value",
                        auditData.get(PelicanConstants.GRANT_TYPE).getNewValue(),
                        equalTo(PelicanConstants.CURRENCY_ENTITLEMENT), assertionErrorList);
                }

                // Validation on currency id
                if (oldId != null || newId != null) {
                    AssertCollector.assertThat("Incorrect currency id old value",
                        auditData.get(PelicanConstants.AMOUNT_CURRENCY_ID).getOldValue(), equalTo(oldCurrencyId),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect currency id new value",
                        auditData.get(PelicanConstants.AMOUNT_CURRENCY_ID).getNewValue(), equalTo(newCurrencyId),
                        assertionErrorList);
                }

                // Validation on amount
                if (oldId == null || newId != null) {
                    AssertCollector.assertThat("Incorrect amount old value",
                        auditData.get(PelicanConstants.AMOUNT_AMOUNT).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect amount id new value",
                        auditData.get(PelicanConstants.AMOUNT_AMOUNT).getNewValue(), equalTo(newAmount),
                        assertionErrorList);
                }

                // Validation on amount
                if (oldId != null && newId != null) {
                    AssertCollector.assertThat("Incorrect amount old value",
                        auditData.get(PelicanConstants.AMOUNT_AMOUNT).getOldValue(), equalTo(oldAmount),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect amount id new value",
                        auditData.get(PelicanConstants.AMOUNT_AMOUNT).getNewValue(), equalTo(newAmount),
                        assertionErrorList);
                }
            }
        }
    }

    /**
     * This method is to query Dynamo DB
     *
     * @param subscriptionPlanDynamoQuery
     * @param assertionErrorList
     * @return boolean
     */
    public static boolean helperToQueryDynamoDb(final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery,
        final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items =
            DynamoDBUtil.subscriptionPlan(subscriptionPlanDynamoQuery.getSubscriptionPlanId());
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {

            if (auditLogEntry.getAction().equals(subscriptionPlanDynamoQuery.getAction().toString())) {

                final SubscriptionPlanDynamoQuery subscriptionPlanQuery =
                    new SubscriptionPlanDynamoQuery.Builder(subscriptionPlanDynamoQuery)
                        .setAuditData(auditLogEntry.getChangeDetailsAsMap()).build();

                if (subscriptionPlanDynamoQuery.getAction() == Action.UPDATE) {
                    subscriptionPlanQuery.setSubscriptionPlanId(null);
                }
                commonAssertionsForCreateAndUpdateSubscriptionPlanAuditLog(subscriptionPlanQuery, assertionErrorList);
                if (subscriptionPlanDynamoQuery.getFileName() != null) {
                    AssertCollector.assertThat("Incorrect upload file name", auditLogEntry.getFileName(),
                        equalTo(subscriptionPlanDynamoQuery.getFileName()), assertionErrorList);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * This method does common assertion of Subscription Plan create and update
     *
     * @param subscriptionPlanBuilder
     * @param assertionErrorList
     */
    public static void commonAssertionsForCreateAndUpdateSubscriptionPlanAuditLog(
        final SubscriptionPlanDynamoQuery subscriptionPlanBuilder, final List<AssertionError> assertionErrorList) {

        final String newId = subscriptionPlanBuilder.getSubscriptionPlanId();

        final Map<String, ChangeDetails> auditData = subscriptionPlanBuilder.getAuditData();
        // Assertion on id
        if (newId != null) {
            AssertCollector.assertThat("Incorrect id old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(newId), assertionErrorList);
        }

        // Assertion on name
        if (subscriptionPlanBuilder.getOldName() == null && subscriptionPlanBuilder.getNewName() != null) {
            AssertCollector.assertThat("Incorrect name old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.NAME).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect name new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(subscriptionPlanBuilder.getNewName()),
                assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldName() != null && subscriptionPlanBuilder.getNewName() != null) {
            AssertCollector.assertThat("Incorrect name old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(subscriptionPlanBuilder.getOldName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect name new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(subscriptionPlanBuilder.getNewName()),
                assertionErrorList);
        }

        // Assertion on external key
        if (subscriptionPlanBuilder.getOldExternalKey() == null
            && subscriptionPlanBuilder.getNewExternalKey() != null) {
            AssertCollector.assertThat("Incorrect external key old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewExternalKey()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldExternalKey() != null
            && subscriptionPlanBuilder.getNewExternalKey() != null) {
            AssertCollector.assertThat("Incorrect external key old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldExternalKey()), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewExternalKey()), assertionErrorList);
        }

        // Assertion on Offering Type
        if (subscriptionPlanBuilder.getOldOfferingType() == null
            && subscriptionPlanBuilder.getNewOfferingType() != null) {
            AssertCollector.assertThat("Incorrect offering type old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_TYPE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering type new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_TYPE).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewOfferingType().toString()), assertionErrorList);
        }

        // Assertion on status
        if (subscriptionPlanBuilder.getOldStatus() == null && subscriptionPlanBuilder.getNewStatus() != null) {
            AssertCollector.assertThat("Incorrect status old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.STATUS).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect status new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.STATUS).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewStatus().toString()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldStatus() != null && subscriptionPlanBuilder.getNewStatus() != null) {
            AssertCollector.assertThat("Incorrect status old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.STATUS).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldStatus().toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect status new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.STATUS).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewStatus().toString()), assertionErrorList);
        }

        // Assertion on Cancellation Policy
        if (subscriptionPlanBuilder.getOldCancellationPolicy() == null
            && subscriptionPlanBuilder.getNewCancellationPolicy() != null) {
            AssertCollector.assertThat("Incorrect cancellation policy old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.CANCELLATION_POLICY).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect cancellation policy new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.CANCELLATION_POLICY).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewCancellationPolicy().toString()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldCancellationPolicy() != null
            && subscriptionPlanBuilder.getNewCancellationPolicy() != null) {
            AssertCollector.assertThat("Incorrect cancellation policy old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.CANCELLATION_POLICY).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldCancellationPolicy().toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect cancellation policy new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.CANCELLATION_POLICY).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewCancellationPolicy().toString()), assertionErrorList);
        }

        // Assertion on Usage Type
        if (subscriptionPlanBuilder.getOldUsageType() == null && subscriptionPlanBuilder.getNewUsageType() != null) {
            AssertCollector.assertThat("Incorrect usage type old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.USAGE_TYPE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.USAGE_TYPE).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewUsageType().getUploadName()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldUsageType() != null
            && subscriptionPlanBuilder.getNewUsageType() != null) {
            AssertCollector.assertThat("Incorrect usage type old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.USAGE_TYPE).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldUsageType().getUploadName()), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.USAGE_TYPE).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewUsageType().getUploadName()), assertionErrorList);
        }

        // Assertion on Offering detail
        if (subscriptionPlanBuilder.getOldOfferingDetailId() == null
            && subscriptionPlanBuilder.getNewOfferingDetailId() != null) {
            AssertCollector.assertThat("Incorrect offering detail old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering detail new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewOfferingDetailId()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldOfferingDetailId() != null
            && subscriptionPlanBuilder.getNewOfferingDetailId() != null) {
            AssertCollector.assertThat("Incorrect offering detail old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldOfferingDetailId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering detail new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewOfferingDetailId()), assertionErrorList);
        }

        // Assertion on product line
        if (subscriptionPlanBuilder.getOldProductLine() == null
            && subscriptionPlanBuilder.getNewProductLine() != null) {
            AssertCollector.assertThat("Incorrect product line old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect product line new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewProductLine()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldProductLine() != null
            && subscriptionPlanBuilder.getNewProductLine() != null) {
            AssertCollector.assertThat("Incorrect product line old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldProductLine()), assertionErrorList);
            AssertCollector.assertThat("Incorrect product line new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewProductLine()), assertionErrorList);
        }

        // Assertion on Support Level
        if (subscriptionPlanBuilder.getOldSupportLevel() == null
            && subscriptionPlanBuilder.getNewSupportLevel() != null) {
            AssertCollector.assertThat("Incorrect support level old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect support level new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewSupportLevel().toString()), assertionErrorList);
        } else if (subscriptionPlanBuilder.getOldSupportLevel() != null
            && subscriptionPlanBuilder.getNewSupportLevel() != null) {
            AssertCollector.assertThat("Incorrect support level old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getOldValue(),
                equalTo(subscriptionPlanBuilder.getOldSupportLevel().toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect support level new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewSupportLevel().toString()), assertionErrorList);
        }

        // Assertion on Expiration Reminder Email's Enabled
        if (subscriptionPlanBuilder.getNewExpReminderEmailEnabled() != null) {
            final String expected = null;
            AssertCollector.assertThat(
                "Incorrect Expiration Reminder Email Enabled old value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXPIRATION_REMINDER_EMAILS_ENABLED).getOldValue(),
                subscriptionPlanBuilder.getOldExpReminderEmailEnabled() == null ? equalTo(expected)
                    : equalTo(subscriptionPlanBuilder.getOldExpReminderEmailEnabled().toLowerCase(Locale.US)),
                assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Expiration Reminder Email Enabled new value for Subscription Plan : " + newId,
                auditData.get(PelicanConstants.EXPIRATION_REMINDER_EMAILS_ENABLED).getNewValue(),
                equalTo(subscriptionPlanBuilder.getNewExpReminderEmailEnabled().toLowerCase(Locale.US)),
                assertionErrorList);
        }
    }

    /**
     * This method contains audit data validation when a subscription offer is added/deleted/updated from a subscription
     * plan.
     *
     * @param assertionErrorList
     */
    public static void validateSubscriptionOffer(final String subscriptionOfferId, final String oldPlanId,
        final String newPlanId, final String oldSubscriptionOfferName, final String newSubscriptionOfferName,
        final String oldExternalKey, final String newExternalKey, final String oldBillingPeriod,
        final String newBillingPeriod, final String oldStatus, final String newStatus, final Action action,
        final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscriptionOffer(subscriptionOfferId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit subscription offer data not found", items.size(), greaterThanOrEqualTo(1),
            assertionErrorList);
        boolean auditDataFound = false;

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (Action.CREATE == action && Action.CREATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    equalTo(subscriptionOfferId), assertionErrorList);
                AssertCollector.assertThat("Invalid old subscription plan id",
                    auditData.get(PelicanConstants.PLAN_ID).getOldValue(), equalTo(oldPlanId), assertionErrorList);
                AssertCollector.assertThat("Invalid new subscription plan id",
                    auditData.get(PelicanConstants.PLAN_ID).getNewValue(), equalTo(newPlanId), assertionErrorList);
                AssertCollector.assertThat("Invalid old subscription offer name",
                    auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldSubscriptionOfferName),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new subscription offer name",
                    auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newSubscriptionOfferName),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old subscription offer external key",
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new subscription offer external key",
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod old value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getOldValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod new value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getNewValue(),
                    equalTo(newBillingPeriod), assertionErrorList);
                AssertCollector.assertThat("Invalid old subscription offer status",
                    auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus), assertionErrorList);
                AssertCollector.assertThat("Invalid new subscription offer status",
                    auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus), assertionErrorList);
                auditDataFound = true;
            } else if (Action.UPDATE == action && Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old subscription offer name",
                    auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldSubscriptionOfferName),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new subscription offer name",
                    auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newSubscriptionOfferName),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod old value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getOldValue(),
                    equalTo(oldBillingPeriod), assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod new value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getNewValue(),
                    equalTo(newBillingPeriod), assertionErrorList);
                auditDataFound = true;
            } else if (Action.DELETE == action && Action.DELETE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    equalTo(subscriptionOfferId), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod old value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getOldValue(),
                    equalTo(newBillingPeriod), assertionErrorList);
                AssertCollector.assertThat("Invalid billingPeriod new value",
                    auditData.get(PelicanConstants.AUDIT_DATA_BILLING_OPTION_BILLING_PERIOD).getNewValue(), nullValue(),
                    assertionErrorList);
                auditDataFound = true;
            }
            AssertCollector.assertThat("Invalid OfferingId", auditLogEntry.getOfferId(), equalTo(subscriptionOfferId),
                assertionErrorList);
        }
        AssertCollector.assertThat(action.toString() + " subscription offer audit data not found", auditDataFound,
            equalTo(true), assertionErrorList);
    }

    /**
     * This method contains audit data validation when a subscription price is added/deleted/updated from a subscription
     * offer.
     *
     * @param assertionErrorList
     */
    public static void validateSubscriptionPrice(final String subscriptionPlanId, final String subscriptionOfferId,
        final String subscriptionPriceId, final String oldPrice, final String newPrice, final Currency oldCurrency,
        final Currency newCurrency, final String oldStartDate, final String newStartDate, final String oldEndDate,
        final String newEndDate, final Action action, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscriptionPrice(subscriptionPriceId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit subscription price data not found", items.size(), greaterThanOrEqualTo(1),
            assertionErrorList);
        boolean auditDataFound = false;

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (Action.CREATE == action && Action.CREATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    equalTo(subscriptionPriceId), assertionErrorList);
                AssertCollector.assertThat("Invalid amount old value",
                    auditData.get(PelicanConstants.AMOUNT).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid amount new value",
                    auditData.get(PelicanConstants.AMOUNT).getNewValue(), equalTo(newPrice), assertionErrorList);
                AssertCollector.assertThat("Invalid old currency id",
                    auditData.get(PelicanConstants.CURRENCY).getOldValue(),
                    equalTo(oldCurrency != null ? String.valueOf(oldCurrency.getCode()) : oldCurrency),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new currency id",
                    auditData.get(PelicanConstants.CURRENCY).getNewValue(),
                    equalTo(newCurrency != null ? String.valueOf(newCurrency.getCode()) : newCurrency),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old start date",
                    auditData.get(PelicanConstants.START_DATE).getOldValue(), equalTo(oldStartDate),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new start date",
                    auditData.get(PelicanConstants.START_DATE).getNewValue(), equalTo(newStartDate),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old end date",
                    auditData.get(PelicanConstants.END_DATE).getOldValue(), equalTo(oldEndDate), assertionErrorList);
                AssertCollector.assertThat("Invalid new end date",
                    auditData.get(PelicanConstants.END_DATE).getNewValue(), greaterThanOrEqualTo(newEndDate),
                    assertionErrorList);
                auditDataFound = true;
            } else if (Action.UPDATE == action && Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid amount old value",
                    auditData.get(PelicanConstants.AMOUNT).getOldValue(), equalTo(oldPrice), assertionErrorList);
                AssertCollector.assertThat("Invalid amount new value",
                    auditData.get(PelicanConstants.AMOUNT).getNewValue(), equalTo(newPrice), assertionErrorList);
                auditDataFound = true;
            } else if (Action.DELETE == action && Action.DELETE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    equalTo(subscriptionPriceId), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid amount old value",
                    auditData.get(PelicanConstants.AMOUNT).getOldValue(), equalTo(newPrice), assertionErrorList);
                AssertCollector.assertThat("Invalid amount new value",
                    auditData.get(PelicanConstants.AMOUNT).getNewValue(), nullValue(), assertionErrorList);
                auditDataFound = true;
            }
            AssertCollector.assertThat("Invalid OfferId", auditLogEntry.getOfferId(), equalTo(subscriptionOfferId),
                assertionErrorList);
            AssertCollector.assertThat("Invalid OfferingId", auditLogEntry.getOfferingId(), equalTo(subscriptionPlanId),
                assertionErrorList);
        }
        AssertCollector.assertTrue(action.toString() + " subscription price audit data not found", auditDataFound,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method contains audit data validation when a localized or non-localized descriptor corresponding to a
     * subscription plan OR subscription offer is added/updated
     *
     * @param assertionErrorList
     */
    public static void validateSubscriptionPlanOrOfferDescriptor(final String descriptorIdFromTable,
        final String subscriptionPlanId, final String subscriptionOfferId, final String oldAppFamilyId,
        final String newAppFamilyId, final String oldDescriptorId, final String newDescriptorId,
        final String oldDescriptorValue, final String newDescriptorValue, final String oldLanguage,
        final String newLanguage, final String oldCountry, final String newCountry, final Action action,
        final List<AssertionError> assertionErrorList) {

        final List<Map<String, AttributeValue>> items = DynamoDBUtil.descriptor(descriptorIdFromTable);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit subscription plan/offer descriptor data not found", items.size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        boolean auditDataFound = false;

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (Action.CREATE == action && Action.CREATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    equalTo(descriptorIdFromTable), assertionErrorList);
                AssertCollector.assertThat("Invalid appFamilyId old value",
                    auditData.get(PelicanConstants.APP_FAMILY_ID).getOldValue(), equalTo(oldAppFamilyId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid appFamilyId new value",
                    auditData.get(PelicanConstants.APP_FAMILY_ID).getNewValue(), equalTo(newAppFamilyId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid descriptorId old value",
                    auditData.get(PelicanConstants.DEFINITION_ID).getOldValue(), equalTo(oldDescriptorId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid descriptorId new value",
                    auditData.get(PelicanConstants.DEFINITION_ID).getNewValue(), equalTo(newDescriptorId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid descriptor old value",
                    auditData.get(PelicanConstants.VALUE).getOldValue(), equalTo(oldDescriptorValue),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid descriptor new value",
                    auditData.get(PelicanConstants.VALUE).getNewValue(), equalTo(newDescriptorValue),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid entityId old value",
                    auditData.get(PelicanConstants.AUDIT_DATA_ENTITY_ID).getOldValue(), nullValue(),
                    assertionErrorList);
                if (subscriptionOfferId != null) {
                    AssertCollector.assertThat("Invalid entityId new value",
                        auditData.get(PelicanConstants.AUDIT_DATA_ENTITY_ID).getNewValue(),
                        equalTo(subscriptionOfferId), assertionErrorList);
                } else {
                    AssertCollector.assertThat("Invalid entityId new value",
                        auditData.get(PelicanConstants.AUDIT_DATA_ENTITY_ID).getNewValue(), equalTo(subscriptionPlanId),
                        assertionErrorList);
                }
                AssertCollector.assertThat("Invalid language old value",
                    auditData.get(PelicanConstants.LANGUAGE).getOldValue(), equalTo(oldLanguage), assertionErrorList);
                AssertCollector.assertThat("Invalid language new value",
                    auditData.get(PelicanConstants.LANGUAGE).getNewValue(), equalTo(newLanguage), assertionErrorList);
                AssertCollector.assertThat("Invalid country old value",
                    auditData.get(PelicanConstants.COUNTRY).getOldValue(), equalTo(oldCountry), assertionErrorList);
                AssertCollector.assertThat("Invalid country new value",
                    auditData.get(PelicanConstants.COUNTRY).getNewValue(), equalTo(newCountry), assertionErrorList);
                AssertCollector.assertThat("Invalid created timestamp old value",
                    auditData.get(PelicanConstants.TIMESTAMP_CREATED).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid created timestamp new value",
                    auditData.get(PelicanConstants.TIMESTAMP_CREATED).getNewValue(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid lastModified timestamp old value",
                    auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getOldValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "lastModified timestamp new value is NOT equal to created timestamp new value",
                    auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getNewValue(),
                    equalTo(auditData.get(PelicanConstants.TIMESTAMP_CREATED).getNewValue()), assertionErrorList);
                auditDataFound = true;
            } else if (Action.UPDATE == action && Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid descriptor old value",
                    auditData.get(PelicanConstants.VALUE).getOldValue(), equalTo(oldDescriptorValue),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid descriptor new value",
                    auditData.get(PelicanConstants.VALUE).getNewValue(), equalTo(newDescriptorValue),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid lastModified timestamp old value",
                    auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getOldValue(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "lastModified timestamp new value is NOT greater than lastModified timestamp" + " old value",
                    auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getNewValue(),
                    greaterThan(auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getOldValue()),
                    assertionErrorList);
                auditDataFound = true;
            }
            AssertCollector.assertThat("Invalid EntityType", auditLogEntry.getEntityType(),
                equalTo(PelicanConstants.DESCRIPTOR), assertionErrorList);
            AssertCollector.assertThat("Invalid EntityId", auditLogEntry.getEntityId(), equalTo(descriptorIdFromTable),
                assertionErrorList);
            AssertCollector.assertThat("Invalid OfferId", auditLogEntry.getOfferId(), equalTo(subscriptionOfferId),
                assertionErrorList);
            AssertCollector.assertThat("Invalid OfferingId", auditLogEntry.getOfferingId(), equalTo(subscriptionPlanId),
                assertionErrorList);
        }
        AssertCollector.assertThat(action.toString() + " subscription plan/offer descriptor audit data not found",
            auditDataFound, equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
