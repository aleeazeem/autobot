package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.PromotionUtils;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class PromotionAuditLogHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionAuditLogHelper.class.getSimpleName());

    /**
     * Verify dynamoDB for CREATE/ADD promotion data
     *
     * @param assertionErrorList
     */
    public static void verifyAuditDataForAddPromotion(final String expectedPromoId, final Promotion promoRequest,
        final JStore store, final String timestamp, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.promotion(expectedPromoId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit promotion data not found", items.size(), equalTo(1), assertionErrorList);
        boolean createPromotionAuditDataFound = false;
        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                equalTo(expectedPromoId), assertionErrorList);
            AssertCollector.assertThat("Invalid old promo name", auditData.get(PelicanConstants.NAME).getOldValue(),
                nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new promo name", auditData.get(PelicanConstants.NAME).getNewValue(),
                equalTo(promoRequest.getName()), assertionErrorList);
            AssertCollector.assertThat("Invalid old promo description",
                auditData.get(PelicanConstants.DESCRIPTION).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new promo description",
                auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), equalTo(promoRequest.getDescription()),
                assertionErrorList);
            if (promoRequest.getActivatePromotion()) {
                AssertCollector.assertThat("Invalid new promo state",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STATE).getNewValue(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid new promo state",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STATE).getNewValue(), equalTo(Status.NEW.toString()),
                    assertionErrorList);
            }
            AssertCollector.assertThat("Invalid old promo sub type",
                auditData.get(PelicanConstants.SUBTYPE).getOldValue(), nullValue(), assertionErrorList);
            if (PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT.equals(promoRequest.getDiscountType())) {
                AssertCollector.assertThat("Invalid new promo sub type",
                    auditData.get(PelicanConstants.SUBTYPE).getNewValue(),
                    equalTo(PromotionType.DISCOUNT_PERCENTAGE.toString()), assertionErrorList);
                AssertCollector.assertThat("Invalid old promo discount percent",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_DISCOUNT_PERCENT).getOldValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo discount percent",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_DISCOUNT_PERCENT).getNewValue(),
                    equalTo(String.valueOf(Double.valueOf(promoRequest.getPercentage()) / 100.0)), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo standalone value",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getNewValue(),
                    equalTo(
                        promoRequest.getStandalonePromotion() != null ? promoRequest.getStandalonePromotion() : "true"),
                    assertionErrorList);
            } else if (PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT.equals(promoRequest.getDiscountType())) {
                AssertCollector.assertThat("Invalid new promo sub type",
                    auditData.get(PelicanConstants.SUBTYPE).getNewValue(),
                    equalTo(PromotionType.SUPPLEMENT_TIME.toString()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo standalone value",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getNewValue(),
                    equalTo(
                        promoRequest.getStandalonePromotion() != null ? promoRequest.getStandalonePromotion() : "YES"),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old promo time period type",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_TYPE).getOldValue(),
                    equalTo(promoRequest.getTimePeriodType()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo time period type",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_TYPE).getNewValue(),
                    equalTo(promoRequest.getTimePeriodType()), assertionErrorList);
                AssertCollector.assertThat("Invalid old promo time period count",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_COUNT).getOldValue(),
                    equalTo(promoRequest.getTimePeriodCount()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo time period count",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_COUNT).getNewValue(),
                    equalTo(promoRequest.getTimePeriodCount()), assertionErrorList);
            } else if (PromotionUtils.DISCOUNT_TYPE_AMOUNT.equals(promoRequest.getDiscountType())) {
                final List<Map<String, AttributeValue>> promoPriceListDiscountItems =
                    DynamoDBUtil.getEntityByPromotionIdAuditlogItemAWSDynamodb("PromotionPriceListDiscount",
                        expectedPromoId, timestamp);
                final List<AuditLogEntry> promoPriceListDiscountAuditLogEntries =
                    DynamoDBUtil.getAuditLogEntries(promoPriceListDiscountItems);
                AssertCollector.assertThat("Audit promotion price list discount data not found",
                    promoPriceListDiscountItems.size(), equalTo(1), assertionErrorList);
                final Map<String, ChangeDetails> promoPriceListDiscountAuditData =
                    promoPriceListDiscountAuditLogEntries.get(0).getChangeDetailsAsMap();
                AssertCollector.assertThat("Invalid new promo sub type",
                    auditData.get(PelicanConstants.SUBTYPE).getNewValue(),
                    equalTo(PromotionType.DISCOUNT_AMOUNT.toString()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo standalone value",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getNewValue(),
                    equalTo(
                        promoRequest.getStandalonePromotion() != null ? promoRequest.getStandalonePromotion() : "true"),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old promo list discount amount",
                    promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getOldValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo list discount amount",
                    promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getNewValue(),
                    equalTo(promoRequest.getAmount()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid old promo max uses",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new promo max uses",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getNewValue(),
                equalTo(promoRequest.getMaxUses()), assertionErrorList);
            AssertCollector.assertThat("Invalid old promo max uses per user",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getOldValue(), nullValue(),
                assertionErrorList);
            AssertCollector.assertThat("Invalid new promo max uses per user",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getNewValue(),
                equalTo(promoRequest.getMaxUsesPerUser()), assertionErrorList);
            AssertCollector.assertThat("Invalid old promo store wide",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_WIDE).getOldValue(), nullValue(),
                assertionErrorList);
            AssertCollector.assertThat("Invalid new promo store wide",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_WIDE).getNewValue(),
                equalTo(String.valueOf(promoRequest.getStoreWide())), assertionErrorList);
            AssertCollector.assertThat("Invalid old promo standalone value",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getOldValue(), nullValue(),
                assertionErrorList);
            AssertCollector.assertThat("Invalid old promo store ids",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getOldValue(), nullValue(),
                assertionErrorList);
            AssertCollector.assertThat("Invalid new promo store ids",
                auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getNewValue(), containsString(store.getId()),
                assertionErrorList);
            createPromotionAuditDataFound = true;
            AssertCollector.assertThat("Invalid promo ID", auditLogEntry.getEntityId(), equalTo(expectedPromoId),
                assertionErrorList);
        }

        // Promotion Code Audit Log Verification
        verifyAuditDataForPromotionCode(expectedPromoId, timestamp, assertionErrorList);

        AssertCollector.assertThat("CREATE promotion audit data not found", createPromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify dynamoDB for UPDATE/EDIT/DELETE store corresponding to the promotion
     *
     * @param assertionErrorList
     */
    public static void verifyAuditDataForPromotionStoreEditAndDelete(final Promotion promotion, final String oldStoreId,
        final String newStoreId, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.promotion(promotion.getId());
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit promotion data not found", items.size(), equalTo(7), assertionErrorList);
        boolean updatePromotionAuditDataFound = false;
        boolean deletePromotionAuditDataFound = false;
        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (Action.UPDATE.toString().equals(auditLogEntry.getAction())
                && auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS) != null) {
                AssertCollector.assertThat("Invalid old promo last modified",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo last modified",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);
                if (auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getNewValue() != null
                    && auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getNewValue().contains(newStoreId)) {
                    AssertCollector.assertThat("Invalid old promo store ids",
                        auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getOldValue(),
                        containsString(oldStoreId), assertionErrorList);
                    updatePromotionAuditDataFound = true;
                }
            } else if (Action.DELETE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid new promo id", auditData.get(PelicanConstants.ID).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo description",
                    auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo name", auditData.get(PelicanConstants.NAME).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo effective date",
                    auditData.get(PelicanConstants.EFFECTIVE_DATE).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo expiration date",
                    auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo last modified date",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses per user",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo standalone",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo state",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STATE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo store ids",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo store wide",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_WIDE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo subtype",
                    auditData.get(PelicanConstants.SUBTYPE).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo time period count",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_COUNT).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo time period type",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_TIME_PERIOD_TYPE).getNewValue(), nullValue(),
                    assertionErrorList);
                deletePromotionAuditDataFound = true;
            }
            AssertCollector.assertThat("Invalid promo ID", auditLogEntry.getEntityId(), equalTo(promotion.getId()),
                assertionErrorList);
        }
        AssertCollector.assertThat("UPDATE promotion audit data not found", updatePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("DELETE promotion audit data not found", deletePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify dynamoDB for UPDATE/EDIT promotion data
     *
     * @param assertionErrorList
     */
    public static void verifyAuditDataForEditPromotion(final Promotion oldPromotion, final Promotion modifiedPromotion,
        final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.promotion(modifiedPromotion.getId());
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit promotion data not found", items.size(), greaterThanOrEqualTo(2),
            assertionErrorList);
        boolean updatePromotionAuditDataFound = false;
        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            if (Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
                String oldPromoDiscountType = PromotionType.SUPPLEMENT_TIME.toString();
                if (PromotionUtils.DISCOUNT_STR.equals(oldPromotion.getPromotionType())) {
                    oldPromoDiscountType = getPromotionType(oldPromotion.getDiscountType()).toString();
                }

                if (oldPromoDiscountType.equals(auditData.get(PelicanConstants.SUBTYPE).getOldValue())
                    && getPromotionType(modifiedPromotion.getDiscountType()).toString()
                        .equals(auditData.get(PelicanConstants.SUBTYPE).getNewValue())) {
                    AssertCollector.assertThat("Invalid old promo name",
                        auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldPromotion.getName()),
                        assertionErrorList);
                    AssertCollector.assertThat("Invalid new promo name",
                        auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(modifiedPromotion.getName()),
                        assertionErrorList);
                    AssertCollector.assertThat("Invalid old promo description",
                        auditData.get(PelicanConstants.DESCRIPTION).getOldValue(),
                        equalTo(oldPromotion.getDescription()), assertionErrorList);
                    AssertCollector.assertThat("Invalid new promo description",
                        auditData.get(PelicanConstants.DESCRIPTION).getNewValue(),
                        equalTo(modifiedPromotion.getDescription()), assertionErrorList);
                    AssertCollector.assertThat("Invalid old promo sub type",
                        auditData.get(PelicanConstants.SUBTYPE).getOldValue(), equalTo(oldPromoDiscountType),
                        assertionErrorList);
                    AssertCollector.assertThat("Invalid new promo sub type",
                        auditData.get(PelicanConstants.SUBTYPE).getNewValue(),
                        equalTo(getPromotionType(modifiedPromotion.getDiscountType()).toString()), assertionErrorList);
                    AssertCollector.assertThat("Invalid old promo max uses",
                        auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getOldValue(),
                        equalTo(oldPromotion.getMaxUses()), assertionErrorList);
                    AssertCollector.assertThat("Invalid new promo max uses",
                        auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getNewValue(),
                        equalTo(modifiedPromotion.getMaxUses()), assertionErrorList);
                    AssertCollector.assertThat("Invalid old promo max uses per user",
                        auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getOldValue(),
                        equalTo(oldPromotion.getMaxUsesPerUser()), assertionErrorList);
                    AssertCollector.assertThat("Invalid new promo max uses per user",
                        auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getNewValue(),
                        equalTo(modifiedPromotion.getMaxUsesPerUser()), assertionErrorList);
                    AssertCollector.assertThat("Invalid promo effective dates",
                        auditData.get(PelicanConstants.EFFECTIVE_DATE).getOldValue(),
                        not(auditData.get(PelicanConstants.EFFECTIVE_DATE).getNewValue()), assertionErrorList);
                    AssertCollector.assertThat("Invalid promo expiration dates",
                        auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue(),
                        not(auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue()), assertionErrorList);
                    updatePromotionAuditDataFound = true;
                }
            }

            AssertCollector.assertThat("Invalid promo ID", auditLogEntry.getEntityId(),
                equalTo(modifiedPromotion.getId()), assertionErrorList);
        }

        final String timestamp = DateTimeUtils.getNowAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT);

        // Promotion Code Audit Log Verification
        verifyAuditDataForPromotionCode(modifiedPromotion.getId(), timestamp, assertionErrorList);

        AssertCollector.assertThat("UPDATE promotion audit data not found", updatePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
    }

    /**
     * Verify dynamoDB for EXTEND promotion (i.e. update)
     *
     * @param assertionErrorList
     */
    public static void verifyAuditDataForExtendPromotion(final JPromotion oldPromotion,
        final PromotionDetailsPage modifiedPromotion, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.promotion(modifiedPromotion.getId());
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit promotion data not found", items.size(), equalTo(1), assertionErrorList);
        boolean updatePromotionAuditDataFound = false;
        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            if (Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
                AssertCollector.assertThat("Invalid old promo max uses",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getOldValue(),
                    equalTo(oldPromotion.getData().getMaxUses().toString()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getNewValue(),
                    equalTo(modifiedPromotion.getMaximumNumberOfUses()), assertionErrorList);
                AssertCollector.assertThat("Invalid old promo max uses per user",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getOldValue(),
                    equalTo(oldPromotion.getData().getMaxUsesPerUser().toString()), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses per user",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getNewValue(),
                    equalTo(modifiedPromotion.getMaximumUsesPerUser()), assertionErrorList);
                AssertCollector.assertThat("Invalid promo expiration dates",
                    auditData.get(PelicanConstants.EXPIRATION_DATE).getOldValue(),
                    not(auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue()), assertionErrorList);
                updatePromotionAuditDataFound = true;
            }
            AssertCollector.assertThat("Invalid promo ID", auditLogEntry.getEntityId(),
                equalTo(modifiedPromotion.getId()), assertionErrorList);
        }
        AssertCollector.assertThat("UPDATE promotion audit data not found", updatePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify dynamoDB for UPDATE price list amount corresponding to a discount amount promotion. Also, verify dynamoDB
     * for DELETE promotion
     *
     * @param assertionErrorList
     */
    public static void verifyAuditDataForDiscountAmountPromotion(final Promotion oldPromotion,
        final Promotion modifiedPromotion, final String timestamp, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.promotion(oldPromotion.getId());
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit promotion data not found", items.size(), equalTo(3), assertionErrorList);
        boolean updatePromotionAuditDataFound = false;
        boolean deletePromotionAuditDataFound = false;
        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old promo last modified",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getOldValue(), notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo last modified",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), notNullValue(), assertionErrorList);

                final List<Map<String, AttributeValue>> promoPriceListDiscountItems =
                    DynamoDBUtil.getEntityByPromotionIdAuditlogItemAWSDynamodb("PromotionPriceListDiscount",
                        oldPromotion.getId(), timestamp);
                final List<AuditLogEntry> promoPriceListDiscountAuditLogEntries =
                    DynamoDBUtil.getAuditLogEntries(promoPriceListDiscountItems);
                AssertCollector.assertThat("Audit promotion price list discount data not found",
                    promoPriceListDiscountItems.size(), equalTo(4), assertionErrorList);

                boolean firstCreateActionFlag = false;
                boolean firstDeleteActionFlag = false;

                for (final AuditLogEntry promoPriceListDiscountAuditLogEntry : promoPriceListDiscountAuditLogEntries) {
                    final Map<String, ChangeDetails> promoPriceListDiscountAuditData =
                        promoPriceListDiscountAuditLogEntry.getChangeDetailsAsMap();

                    if (Action.CREATE.toString().equals(promoPriceListDiscountAuditLogEntry.getAction())) {

                        AssertCollector.assertThat("Invalid initial discount amount",
                            promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getOldValue(), nullValue(),
                            assertionErrorList);

                        if (!firstCreateActionFlag) {
                            firstCreateActionFlag = true;
                            AssertCollector.assertThat("Invalid old updated discount amount",
                                promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getNewValue(),
                                equalTo(oldPromotion.getValue()), assertionErrorList);
                        } else {
                            AssertCollector.assertThat("Invalid new updated discount amount",
                                promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getNewValue(),
                                equalTo(modifiedPromotion.getValue()), assertionErrorList);
                        }

                    }
                    if (Action.DELETE.toString().equals(promoPriceListDiscountAuditLogEntry.getAction())) {

                        if (!firstDeleteActionFlag) {
                            firstDeleteActionFlag = true;
                            AssertCollector.assertThat("Invalid old updated discount amount",
                                promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getOldValue(),
                                equalTo(oldPromotion.getValue()), assertionErrorList);
                        } else {
                            AssertCollector.assertThat("Invalid new updated discount amount",
                                promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getOldValue(),
                                equalTo(modifiedPromotion.getValue()), assertionErrorList);
                        }

                        AssertCollector.assertThat("Invalid deleted discount amount",
                            promoPriceListDiscountAuditData.get(PelicanConstants.AMOUNT).getNewValue(), nullValue(),
                            assertionErrorList);

                    }
                }

                updatePromotionAuditDataFound = true;
            } else if (Action.DELETE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid new promo id", auditData.get(PelicanConstants.ID).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo description",
                    auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo name", auditData.get(PelicanConstants.NAME).getNewValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo effective date",
                    auditData.get(PelicanConstants.EFFECTIVE_DATE).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo expiration date",
                    auditData.get(PelicanConstants.EXPIRATION_DATE).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo last modified date",
                    auditData.get(PelicanConstants.LAST_MODIFIED).getNewValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo max uses per user",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_MAX_USES_PER_USER).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo standalone",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STANDALONE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo state",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STATE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo store ids",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_IDS).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo store wide",
                    auditData.get(PromotionUtils.AUDIT_DATA_PROMO_STORE_WIDE).getNewValue(), nullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo subtype",
                    auditData.get(PelicanConstants.SUBTYPE).getNewValue(), nullValue(), assertionErrorList);
                deletePromotionAuditDataFound = true;
            }
            AssertCollector.assertThat("Invalid promo ID", auditLogEntry.getEntityId(), equalTo(oldPromotion.getId()),
                assertionErrorList);
        }

        // Promotion Code Audit Log Verification
        verifyAuditDataForPromotionCode(oldPromotion.getId(), timestamp, assertionErrorList);

        AssertCollector.assertThat("UPDATE promotion audit data not found", updatePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("DELETE promotion audit data not found", deletePromotionAuditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify dynamoDB for CREATE\UPDATE\DELETE promo code for a promotion.
     *
     * @param assertionErrorList
     */
    private static void verifyAuditDataForPromotionCode(final String promotionId, final String timestamp,
        final List<AssertionError> assertionErrorList) {
        LOGGER.info("verifyAuditDataForPromotionCode for PromotionID: " + promotionId);

        final List<Map<String, AttributeValue>> promoCodeItems =
            DynamoDBUtil.getEntityByPromotionIdAuditlogItemAWSDynamodb("PromotionCode", promotionId, timestamp);

        final List<AuditLogEntry> promoCodeAuditLogEntries = DynamoDBUtil.getAuditLogEntries(promoCodeItems);
        AssertCollector.assertThat("Audit promotion code data not found", promoCodeItems.size(), notNullValue(),
            assertionErrorList);

        for (final AuditLogEntry auditLogEntry : promoCodeAuditLogEntries) {
            final Map<String, ChangeDetails> promoCodeAuditData = auditLogEntry.getChangeDetailsAsMap();
            LOGGER.info("verifyAuditDataForPromotionCode Action: " + auditLogEntry.getAction());

            if (Action.CREATE.toString().equals(auditLogEntry.getAction())
                && promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE) != null) {

                AssertCollector.assertThat("Invalid old promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getOldValue(), nullValue(),
                    assertionErrorList);

                AssertCollector.assertThat("Invalid new promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getNewValue(), notNullValue(),
                    assertionErrorList);

            } else if (Action.UPDATE.toString().equals(auditLogEntry.getAction())
                && promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE) != null) {

                AssertCollector.assertThat("Invalid old promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getOldValue(), notNullValue(),
                    assertionErrorList);

                AssertCollector.assertThat("Invalid new promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getNewValue(), notNullValue(),
                    assertionErrorList);

            } else if (Action.DELETE.toString().equals(auditLogEntry.getAction())
                && promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE) != null) {

                AssertCollector.assertThat("Invalid old promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getOldValue(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new promo code",
                    promoCodeAuditData.get(PelicanConstants.DISCOUNT_CODE).getNewValue(), nullValue(),
                    assertionErrorList);
            }
        }

    }

    /**
     * Method to get the promotion type
     *
     * @return PromotionType
     */
    private static PromotionType getPromotionType(final String value) {
        if (PromotionUtils.DISCOUNT_TYPE_PERCENTAGE.equals(value)) {
            return PromotionType.DISCOUNT_PERCENTAGE;
        } else if (PromotionUtils.DISCOUNT_TYPE_SUPPLEMENT_TIME.equals(value)) {
            return PromotionType.SUPPLEMENT_TIME;
        }

        return PromotionType.DISCOUNT_AMOUNT;
    }
}
