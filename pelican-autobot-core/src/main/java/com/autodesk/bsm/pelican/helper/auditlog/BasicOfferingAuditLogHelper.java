package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;

public class BasicOfferingAuditLogHelper {

    private static String oldId;
    private static String newId;

    /**
     * This method is to query Dynamo DB for Basic Offering
     *
     * @param assertionErrorList
     */
    public static boolean helperToValidateDynamoDbForBasicOffering(final String basicOfferingId, final String oldName,
        final String newName, final String oldExternalKey, final String newExternalKey,
        final com.autodesk.bsm.pelican.enums.OfferingType oldOfferingType,
        final com.autodesk.bsm.pelican.enums.OfferingType newOfferingType, final Status oldStatus,
        final Status newStatus, final String oldProductLine, final String newProductLine, final String oldMediaType,
        final String newMediaType, final String oldLanguageCode, final String newLanguageCode,
        final String oldOfferingDetailId, final String newOfferingDetailId, final SupportLevel oldSupportLevel,
        final SupportLevel newSupportLevel, final UsageType oldUsageType, final UsageType newUsageType,
        final String oldProperties, final String newProperties, final Action action, final String fileName,
        final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.basicOffering(basicOfferingId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(action.toString())) {

                if (action == Action.CREATE) {
                    oldId = null;
                }
                newId = basicOfferingId;
                if (action == Action.UPDATE) {
                    oldId = null;
                    newId = null;
                }
                commonAssertionsForCreateUpdateDeleteBasicOfferingAuditLog(auditData, basicOfferingId, oldId, newId,
                    oldName, newName, oldExternalKey, newExternalKey, oldOfferingType, newOfferingType, oldStatus,
                    newStatus, oldProductLine, newProductLine, oldMediaType, newMediaType, oldLanguageCode,
                    newLanguageCode, oldOfferingDetailId, newOfferingDetailId, oldSupportLevel, newSupportLevel,
                    oldUsageType, newUsageType, oldProperties, newProperties, assertionErrorList);

                AssertCollector.assertThat("Incorrect upload file name", auditLogEntry.getFileName(), equalTo(fileName),
                    assertionErrorList);
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to do common assertion for Basic Offering CREATE, UPDATE and DELETE
     *
     * @param assertionErrorList
     */
    private static void commonAssertionsForCreateUpdateDeleteBasicOfferingAuditLog(
        final Map<String, ChangeDetails> auditData, final String basicOfferingId, final String oldId,
        final String newId, final String oldName, final String newName, final String oldExternalKey,
        final String newExternalKey, final com.autodesk.bsm.pelican.enums.OfferingType oldOfferingType,
        final com.autodesk.bsm.pelican.enums.OfferingType newOfferingType, final Status oldStatus,
        final Status newStatus, final String oldProductLine, final String newProductLine, final String oldMediaType,
        final String newMediaType, final String oldLanguageCode, final String newLanguageCode,
        final String oldOfferingDetailId, final String newOfferingDetailId, final SupportLevel oldSupportLevel,
        final SupportLevel newSupportLevel, final UsageType oldUsageType, final UsageType newUsageType,
        final String oldProperties, final String newProperties, final List<AssertionError> assertionErrorList) {

        // Assertion on id
        if (oldId == null && newId != null) {
            AssertCollector.assertThat("Incorrect id old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(newId), assertionErrorList);
        }

        // Assertion on name
        if (oldName == null && newName != null) {
            AssertCollector.assertThat("Incorrect name old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.NAME).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect name new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newName), assertionErrorList);
        } else if (oldName != null && newName != null) {
            AssertCollector.assertThat("Incorrect name old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldName), assertionErrorList);
            AssertCollector.assertThat("Incorrect name new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newName), assertionErrorList);
        }

        // Assertion on external key
        if (oldExternalKey == null && newExternalKey != null) {
            AssertCollector.assertThat("Incorrect external key old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                assertionErrorList);
        } else if (oldExternalKey != null && newExternalKey != null) {
            AssertCollector.assertThat("Incorrect external key old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect external key new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                assertionErrorList);
        }

        // Assertion on Offering Type
        if (oldOfferingType == null && newOfferingType != null) {

            AssertCollector.assertThat("Incorrect offering type old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_TYPE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering type new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_TYPE).getNewValue(), equalTo(newOfferingType.toString()),
                assertionErrorList);
        }

        // Assertion on status
        if (oldStatus == null && newStatus != null) {
            AssertCollector.assertThat("Incorrect status old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.STATUS).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect status new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()),
                assertionErrorList);
        } else if (oldStatus != null && newStatus != null) {
            AssertCollector.assertThat("Incorrect status old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect status new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()),
                assertionErrorList);
        }

        // Assertion on Offering detail
        if (oldOfferingDetailId == null && newOfferingDetailId != null) {
            AssertCollector.assertThat("Incorrect offering detail old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering detail new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getNewValue(), equalTo(newOfferingDetailId),
                assertionErrorList);
        } else if (oldOfferingDetailId != null && newOfferingDetailId != null) {
            AssertCollector.assertThat("Incorrect offering detail old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getOldValue(), equalTo(oldOfferingDetailId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect offering detail new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.OFFERING_DETAIL_ID).getNewValue(), equalTo(newOfferingDetailId),
                assertionErrorList);
        }

        // Assertion on product line
        if (oldProductLine == null && newProductLine != null) {
            AssertCollector.assertThat("Incorrect product line old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect product line new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getNewValue(), equalTo(newProductLine),
                assertionErrorList);
        } else if (oldProductLine != null && newProductLine != null) {
            AssertCollector.assertThat("Incorrect product line old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getOldValue(), equalTo(oldProductLine),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect product line new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PRODUCT_LINE).getNewValue(), equalTo(newProductLine),
                assertionErrorList);
        }

        // Assertion on Media Type
        if (oldMediaType == null && newMediaType != null) {
            AssertCollector.assertThat("Incorrect media type old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.MEDIA_TYPE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect media type new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.MEDIA_TYPE).getNewValue(), equalTo(newMediaType), assertionErrorList);
        } else if (oldMediaType != null && newMediaType != null) {
            AssertCollector.assertThat("Incorrect media type old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.MEDIA_TYPE).getOldValue(), equalTo(oldMediaType), assertionErrorList);
            AssertCollector.assertThat("Incorrect media type new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.MEDIA_TYPE).getNewValue(), equalTo(newMediaType), assertionErrorList);
        }

        // Assertion on Language Code
        if (oldLanguageCode == null && newLanguageCode != null) {
            AssertCollector.assertThat("Incorrect language code old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.LANGUAGE_CODE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect language code new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.LANGUAGE_CODE).getNewValue(), equalTo(newLanguageCode),
                assertionErrorList);
        } else if (oldLanguageCode != null && newLanguageCode != null) {
            AssertCollector.assertThat("Incorrect language code old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.LANGUAGE_CODE).getOldValue(), equalTo(oldLanguageCode),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect language code new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.LANGUAGE_CODE).getNewValue(), equalTo(newLanguageCode),
                assertionErrorList);
        }

        // Assertion on Support Level
        if (oldSupportLevel == null && newSupportLevel != null) {
            AssertCollector.assertThat("Incorrect support level old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect support level new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getNewValue(), equalTo(newSupportLevel.toString()),
                assertionErrorList);
        } else if (oldSupportLevel != null && newSupportLevel != null) {
            AssertCollector.assertThat("Incorrect support level old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getOldValue(), equalTo(oldSupportLevel.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect support level new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.SUPPORT_LEVEL).getNewValue(), equalTo(newSupportLevel.toString()),
                assertionErrorList);
        }

        // Assertion on Usage Type
        if (oldUsageType == null && newUsageType != null) {
            AssertCollector.assertThat("Incorrect usage type old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.USAGE_TYPE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.USAGE_TYPE).getNewValue(), equalTo(newUsageType.getUploadName()),
                assertionErrorList);
        } else if (oldUsageType != null && newSupportLevel != null) {
            AssertCollector.assertThat("Incorrect usage type old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.USAGE_TYPE).getOldValue(), equalTo(oldUsageType.getUploadName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.USAGE_TYPE).getNewValue(), equalTo(newUsageType.getUploadName()),
                assertionErrorList);
        }

        // Assertion on Properties
        if (oldProperties == null && newProperties != null) {
            AssertCollector.assertThat("Incorrect properties old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect properties new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), notNullValue(), assertionErrorList);
        } else if (oldProperties != null && newProperties != null) {
            AssertCollector.assertThat("Incorrect properties old value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect properties new value for Basic Offering : " + basicOfferingId,
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), notNullValue(), assertionErrorList);
        }
    }

    /**
     * This method helps to query Dynamo DB for Basic Offering Price
     *
     * @param assertionErrorList
     */
    public static boolean helperToValidateDynamoDbForBasicOfferingPrice(final String priceId, String oldBasicOfferingId,
        String newBasicOfferingId, final String oldPriceList, final String newPriceList,
        final String oldStoreExternalKey, final String newStoreExternalKey, final String oldAmount,
        final String newAmount, final String oldCurrencyId, final String newCurrencyId, final String oldStartDate,
        final String newStartDate, final String oldEndDate, final String newEndDate, final Action action,
        final String fileName, final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.subscriptionPrice(priceId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(action.toString())) {

                if (action == Action.CREATE) {
                    oldId = null;
                    oldBasicOfferingId = null;

                }
                newId = priceId;
                if (action == Action.UPDATE) {
                    newId = null;
                    newBasicOfferingId = null;
                    oldBasicOfferingId = null;
                    oldId = null;

                }
                commonAssertionForCreateUpdateDeleteBasicOfferingPrice(auditData, priceId, oldId, newId,
                    oldBasicOfferingId, newBasicOfferingId, oldPriceList, newPriceList, oldAmount, newAmount,
                    oldCurrencyId, newCurrencyId, oldStartDate, newStartDate, oldEndDate, newEndDate,
                    assertionErrorList);

                if (fileName != null) {
                    AssertCollector.assertThat("Incorrect upload file name", auditLogEntry.getFileName(),
                        equalTo(fileName), assertionErrorList);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * This method does common assertion for Basic Offering Price
     *
     * @param assertionErrorList
     */
    private static void commonAssertionForCreateUpdateDeleteBasicOfferingPrice(
        final Map<String, ChangeDetails> auditData, final String basicOfferingPriceId, final String oldId,
        final String newId, final String oldBasicOfferingId, final String newBasicOfferingId, final String oldPriceList,
        final String newPriceList, final String oldAmount, final String newAmount, final String oldCurrencyId,
        final String newCurrencyId, final String oldStartDate, final String newStartDate, final String oldEndDate,
        final String newEndDate, final List<AssertionError> assertionErrorList) {

        // Assertion on id
        if (oldId == null && newId != null) {
            AssertCollector.assertThat(
                "Incorrect price id old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.ID).getOldValue(), equalTo(oldId), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect price id new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(newId), assertionErrorList);
        }

        // Assertion on Basic Offering Id
        if (oldBasicOfferingId == null && newBasicOfferingId != null) {
            AssertCollector.assertThat(
                "Incorrect Basic Offering Id old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.OFFERING_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Basic Offering Id new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.OFFERING_ID).getNewValue(), equalTo(newBasicOfferingId),
                assertionErrorList);
        }

        // Assertion on Price list
        if (oldPriceList == null && newPriceList != null) {
            AssertCollector.assertThat(
                "Incorrect Basic Offering Price List old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.PRICE_LIST_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Basic Offering Price List new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.PRICE_LIST_ID).getNewValue(), equalTo(newPriceList), assertionErrorList);
        }

        // Assertion on Amount
        if (oldAmount == null && newAmount != null) {
            AssertCollector.assertThat("Incorrect amount old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.AMOUNT).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect amount new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.AMOUNT).getNewValue(), equalTo(newAmount), assertionErrorList);
        } else if (oldAmount != null && newAmount != null) {
            AssertCollector.assertThat("Incorrect amount old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.AMOUNT).getOldValue(), equalTo(oldAmount), assertionErrorList);
            AssertCollector.assertThat("Incorrect amount new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.AMOUNT).getNewValue(), equalTo(newAmount), assertionErrorList);
        }

        // Assertion on Currency
        if (oldCurrencyId == null && newCurrencyId != null) {
            AssertCollector.assertThat(
                "Incorrect currency old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.CURRENCY).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect currency new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.CURRENCY).getNewValue(), equalTo(newCurrencyId), assertionErrorList);
        }

        // Assertion on start date
        if (oldStartDate == null && newStartDate != null) {
            AssertCollector.assertThat(
                "Incorrect start date old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.START_DATE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect start date new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.START_DATE).getNewValue(), equalTo(newStartDate), assertionErrorList);
        } else if (oldStartDate != null && newStartDate != null) {
            AssertCollector.assertThat(
                "Incorrect start date old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.START_DATE).getOldValue(), equalTo(oldStartDate), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect start date new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.START_DATE).getNewValue(), equalTo(newStartDate), assertionErrorList);
        }

        // Assertion on end date
        if (oldEndDate == null && newEndDate != null) {
            AssertCollector.assertThat(
                "Incorrect end date old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.END_DATE).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect end date new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.END_DATE).getNewValue(), equalTo(newEndDate), assertionErrorList);
        } else if (oldStartDate != null && newStartDate != null) {
            AssertCollector.assertThat(
                "Incorrect end date old value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.END_DATE).getOldValue(), equalTo(oldEndDate), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect end date new value for Basic Offering Price : " + basicOfferingPriceId,
                auditData.get(PelicanConstants.END_DATE).getNewValue(), equalTo(newEndDate), assertionErrorList);
        }
    }

    /**
     * This method contains audit data validation when a localized or non-localized descriptor corresponding to a basic
     * Offering is added or updated
     *
     * @param assertionErrorList
     */
    public static void validateBasicOfferingDescriptors(final String descriptorIdFromTable,
        final String basicOfferingId, final String oldAppFamilyId, final String newAppFamilyId,
        final String oldDescriptorId, final String newDescriptorId, final String oldDescriptorValue,
        final String newDescriptorValue, final String oldLanguage, final String newLanguage, final String oldCountry,
        final String newCountry, final Action action, final List<AssertionError> assertionErrorList) {

        final List<Map<String, AttributeValue>> items = DynamoDBUtil.descriptor(descriptorIdFromTable);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit Basic Offering Descriptor data not found", items.size(),
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
                if (basicOfferingId != null) {
                    AssertCollector.assertThat("Invalid entityId new value",
                        auditData.get(PelicanConstants.AUDIT_DATA_ENTITY_ID).getNewValue(), equalTo(basicOfferingId),
                        assertionErrorList);
                } else {
                    AssertCollector.assertThat("Invalid entityId new value",
                        auditData.get(PelicanConstants.AUDIT_DATA_ENTITY_ID).getNewValue(), equalTo(basicOfferingId),
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
        }
        AssertCollector.assertThat(action.toString() + " Basic Offer descriptor audit data not found", auditDataFound,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
