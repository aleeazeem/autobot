package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is created for validation of "Description" column in Audit Log Report for Subscription Plan Assertions are
 * for Subscription Plan, Subscription Offer, Subscription Price
 *
 * @author Shweta Hegde
 */
public class SubscriptionAndBasicOfferingsAuditLogReportHelper {

    /**
     * This method does assertion for subscription plan (top entity) in audit log report
     *
     * @param descriptionPropertyValues
     * @param oldSubscriptionPlanName
     * @param newSubscriptionPlanName
     * @param oldExternalKey
     * @param newExternalKey
     * @param oldOfferingType
     * @param newOfferingType
     * @param oldStatus
     * @param newStatus
     * @param oldCancellationPolicy
     * @param newCancellationPolicy
     * @param oldUsageType
     * @param newUsageType
     * @param oldOfferingDetailId
     * @param newOfferingDetailId
     * @param oldProductLineId
     * @param newProductLineId
     * @param oldSupportLevel
     * @param newSupportLevel
     * @param environmentVariables
     * @param oldPackagingType TODO
     * @param newPackagingType TODO
     * @param assertionErrorList
     */
    public static void assertionsForSubscriptionPlanInAuditLogReportDescription(
        final Map<String, List<String>> descriptionPropertyValues, final String oldSubscriptionPlanName,
        final String newSubscriptionPlanName, final String oldExternalKey, final String newExternalKey,
        final OfferingType oldOfferingType, final OfferingType newOfferingType, final Status oldStatus,
        final Status newStatus, final CancellationPolicy oldCancellationPolicy,
        final CancellationPolicy newCancellationPolicy, final UsageType oldUsageType, final UsageType newUsageType,
        final String oldOfferingDetailId, final String newOfferingDetailId, final String oldProductLineId,
        final String newProductLineId, final SupportLevel oldSupportLevel, final SupportLevel newSupportLevel,
        final EnvironmentVariables environmentVariables, final PackagingType oldPackagingType,
        final PackagingType newPackagingType, final List<AssertionError> assertionErrorList) {

        commonAssertionsForBasicAndSubscriptionOfferingInfo(descriptionPropertyValues, oldSubscriptionPlanName,
            newSubscriptionPlanName, oldExternalKey, newExternalKey, oldOfferingType, newOfferingType, oldStatus,
            newStatus, oldUsageType, newUsageType, oldOfferingDetailId, newOfferingDetailId, oldProductLineId,
            newProductLineId, environmentVariables, assertionErrorList);

        // Cancellation Policy
        if (newCancellationPolicy != null) {
            final List<String> cancellationPolicyValues =
                descriptionPropertyValues.get(PelicanConstants.CANCELLATION_POLICY_FIELD);
            if (oldCancellationPolicy == null) {
                AssertCollector.assertThat("Invalid old cancellation policy value in audit log report",
                    cancellationPolicyValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old cancellation policy value in audit log report",
                    cancellationPolicyValues.get(0), equalTo(oldCancellationPolicy.toString()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new cancellation policy value in audit log report",
                cancellationPolicyValues.get(1), equalTo(newCancellationPolicy.toString()), assertionErrorList);
        }

        // Support Level
        if (newSupportLevel != null) {
            final List<String> supportLevelValues = descriptionPropertyValues.get(PelicanConstants.SUPPORT_LEVEL_FIELD);
            if (oldSupportLevel == null) {
                AssertCollector.assertThat("Invalid old support level value in audit log report",
                    supportLevelValues.get(0), equalTo(PelicanConstants.NONE), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old support level value in audit log report",
                    supportLevelValues.get(0), equalTo(oldSupportLevel.toString()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new support level value in audit log report", supportLevelValues.get(1),
                equalTo(newSupportLevel.getDisplayName()), assertionErrorList);
        }

        // Support Level
        if (newPackagingType != null) {
            final List<String> packagingTypeValues =
                descriptionPropertyValues.get(PelicanConstants.PACKAGING_TYPE_FIELD);
            if (oldPackagingType == null) {
                AssertCollector.assertThat("Invalid old packaging type value in audit log report",
                    packagingTypeValues.get(0), equalTo(PelicanConstants.NONE), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old packaging type value in audit log report",
                    packagingTypeValues.get(0), equalTo(oldPackagingType.getDisplayName()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new packaging type value in audit log report",
                packagingTypeValues.get(1), equalTo(newPackagingType.getDisplayName()), assertionErrorList);
        }
    }

    /**
     * This method does assertions on feature entitlement
     *
     * @param descriptionPropertyValues
     * @param oldFeatureId
     * @param newFeatureId
     * @param oldLicensingModel
     * @param oldCoreProducts
     * @param newCoreProducts
     * @param assertionErrorList
     */
    public static void assertionsForFeatureEntitlementInAuditLogReportDescription(
        final Map<String, List<String>> descriptionPropertyValues, final String oldFeatureId, final String newFeatureId,
        final String oldLicensingModel, final String newLicensingModel, final String oldCoreProducts,
        final String newCoreProducts, final String oldAssignableValue, final String newAssignableValue,
        final String oldEOSDate, final String newEOSDate, final String oldEOLImmeDate, final String newEOLImmeDate,
        final String oldEOLRenewalDate, final String newEOLRenewalDate, final EnvironmentVariables environmentVariables,
        final List<AssertionError> assertionErrorList) {

        // Get new Feature name
        final String newFeatureName = DbUtils
            .selectQuery("select NAME from ITEM where id = " + newFeatureId, "NAME", environmentVariables).get(0);
        // Feature
        final List<String> featureValues = descriptionPropertyValues.get(PelicanConstants.FEATURE_FIELD);
        if (oldFeatureId == null) {
            AssertCollector.assertThat("Invalid old feature value in audit log report", featureValues.get(0),
                nullValue(), assertionErrorList);
        } else {
            // Get new Feature name
            final String oldFeatureName = DbUtils
                .selectQuery("select NAME from ITEM where id = " + oldFeatureId, "NAME", environmentVariables).get(0);
            AssertCollector.assertThat("Invalid old feature value in audit log report", featureValues.get(0),
                equalTo(oldFeatureName + " (" + oldFeatureId + ")"), assertionErrorList);
        }
        AssertCollector.assertThat("Invalid new feature value in audit log report", featureValues.get(1),
            equalTo(newFeatureName + " (" + newFeatureId + ")"), assertionErrorList);

        // Licensing Model
        final List<String> licensingModelValues = descriptionPropertyValues.get(PelicanConstants.LICENSING_MODEL_FIELD);
        if (oldLicensingModel == null) {
            AssertCollector.assertThat("Invalid old licensing model value in audit log report",
                licensingModelValues.get(0), nullValue(), assertionErrorList);
        } else {
            AssertCollector.assertThat("Invalid old licensing model value in audit log report",
                licensingModelValues.get(0), equalTo(oldLicensingModel), assertionErrorList);
        }
        AssertCollector.assertThat("Invalid new licensing model value in audit log report", licensingModelValues.get(1),
            equalTo(newLicensingModel), assertionErrorList);

        // Core Products
        if (!(oldCoreProducts == null && newCoreProducts == null)) {
            final List<String> coreProductsValues = descriptionPropertyValues.get(PelicanConstants.CORE_PRODUCTS_FIELD);

            if (oldCoreProducts == null) {
                AssertCollector.assertThat("Invalid old core products value in audit log report",
                    coreProductsValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old core products value in audit log report",
                    validateCoreProducts(oldCoreProducts, coreProductsValues), is(0), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new core products value in audit log report",
                validateCoreProducts(newCoreProducts, coreProductsValues), is(0), assertionErrorList);
        }
        // Grant type
        final List<String> grantTypeValues = descriptionPropertyValues.get(PelicanConstants.GRANT_TYPE_FIELD);
        AssertCollector.assertThat("Invalid old grant type value in audit log report", grantTypeValues.get(0),
            nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new grant type value in audit log report", grantTypeValues.get(1),
            equalTo(PelicanConstants.ITEM), assertionErrorList);

        // Entitlement Type
        final List<String> entitlementTypeValues =
            descriptionPropertyValues.get(PelicanConstants.ENTITLEMENT_TYPE_FIELD);
        AssertCollector.assertThat("Invalid old entitlement type value in audit log report",
            entitlementTypeValues.get(0), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new entitlement type value in audit log report",
            entitlementTypeValues.get(1), equalTo(PelicanConstants.ONE_TIME_ENTITLEMENTS), assertionErrorList);

        validateRemoveFeatureDates(oldAssignableValue, newAssignableValue, "ASSIGNABLE",
            descriptionPropertyValues.get(PelicanConstants.AUDIT_ASSIGNABLE_COLUMN_NAME), assertionErrorList);
        validateRemoveFeatureDates(oldEOSDate, newEOSDate, "EOS",
            descriptionPropertyValues.get(PelicanConstants.AUDIT_EOS_DATE_COLUMN_NAME), assertionErrorList);
        validateRemoveFeatureDates(oldEOLImmeDate, newEOLImmeDate, "EOL Immediate",
            descriptionPropertyValues.get(PelicanConstants.AUDIT_EOL_IMMEDIATE_DATE_COLUMN_NAME), assertionErrorList);
        validateRemoveFeatureDates(oldEOLRenewalDate, newEOLRenewalDate, "EOL Renewal",
            descriptionPropertyValues.get(PelicanConstants.AUDIT_EOL_RENEWAL_DATE_COLUMN_NAME), assertionErrorList);

    }

    /**
     * Validate EOS, EOL Immediate, EOL Renewal and Assignable column values
     *
     * @param oldDate
     * @param newDate
     * @param dateType
     * @param dateColumnValues
     * @param assertionErrorList
     */
    private static void validateRemoveFeatureDates(final String oldDate, final String newDate, final String dateType,
        final List<String> dateColumnValues, final List<AssertionError> assertionErrorList) {
        if (!(oldDate == null && newDate == null)) {
            if (oldDate == null) {
                AssertCollector.assertThat("Invalid " + dateType + " Renewal value in audit log report",
                    dateColumnValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old " + dateType + " Renewal value in audit log report",
                    dateColumnValues.get(0), equalTo(oldDate), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new " + dateType + " Renewal value in audit log report",
                dateColumnValues.get(1), equalTo(DateTimeUtils.changeDateFormat(newDate,
                    PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.AUDIT_LOG_DATE_FORMAT)),
                assertionErrorList);
        }
    }

    /**
     * Core products assertions are done in a different way than others. Since core products come as comma separated in
     * square brackets, have to do string manipulations
     *
     * @param newCoreProducts
     * @return
     */
    private static int validateCoreProducts(final String newCoreProducts, final List<String> coreProductsValues) {

        final String[] expectedCoreProducts = newCoreProducts.replace("[", "").replace("]", "").split(",");
        final Set<String> coreProductSets = new HashSet<>();
        for (final String coreProduct : expectedCoreProducts) {
            coreProductSets.add(coreProduct.trim());
        }

        final String[] actualCoreProducts = coreProductsValues.get(1).replace("[", "").replace("]", "").split(",");
        for (final String coreProduct : actualCoreProducts) {
            coreProductSets.remove(coreProduct.trim());
        }
        return coreProductSets.size();
    }

    /**
     * This method does assertions on currency amount entitlement
     *
     * @param descriptionPropertyValues
     * @param oldAmount
     * @param newAmount
     * @param oldCurrency
     * @param assertionErrorList
     */
    public static void assertionsForCurrencyAmountEntitlementInAuditLogReportDescription(
        final Map<String, List<String>> descriptionPropertyValues, final String oldAmount, final String newAmount,
        final String oldCurrency, final String newCurrency, final List<AssertionError> assertionErrorList) {

        // Amount
        final List<String> amountValues = descriptionPropertyValues.get(PelicanConstants.AMOUNT_FIELD);
        if (oldAmount == null) {
            AssertCollector.assertThat("Invalid old amount value in audit log report", amountValues.get(0), nullValue(),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Invalid old amount value in audit log report", amountValues.get(0),
                equalTo(oldAmount), assertionErrorList);
        }
        AssertCollector.assertThat("Invalid new amount value in audit log report", amountValues.get(1),
            equalTo(newAmount), assertionErrorList);

        // Currency
        final List<String> currencyValues = descriptionPropertyValues.get(PelicanConstants.CURRENCY_FIELD);
        if (oldCurrency == null) {
            AssertCollector.assertThat("Invalid old currency value in audit log report", currencyValues.get(0),
                nullValue(), assertionErrorList);
        } else {
            AssertCollector.assertThat("Invalid old currency value in audit log report", currencyValues.get(0),
                equalTo(oldCurrency), assertionErrorList);
        }
        AssertCollector.assertThat("Invalid new currency value in audit log report", currencyValues.get(1),
            equalTo(newCurrency), assertionErrorList);

        // Grant type
        final List<String> grantTypeValues = descriptionPropertyValues.get(PelicanConstants.GRANT_TYPE_FIELD);
        AssertCollector.assertThat("Invalid old grant type value in audit log report", grantTypeValues.get(0),
            nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new grant type value in audit log report", grantTypeValues.get(1),
            equalTo(PelicanConstants.CURRENCY_ENTITLEMENT), assertionErrorList);

        // Entitlement Type
        final List<String> entitlementTypeValues =
            descriptionPropertyValues.get(PelicanConstants.ENTITLEMENT_TYPE_FIELD);
        AssertCollector.assertThat("Invalid old entitlement type value in audit log report",
            entitlementTypeValues.get(0), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new entitlement type value in audit log report",
            entitlementTypeValues.get(1), equalTo(PelicanConstants.ONE_TIME_ENTITLEMENTS), assertionErrorList);
    }

    /**
     * This method does assertions for Subscription Offers in Audit Log Report
     *
     * @param descriptionPropertyValues
     * @param oldOfferName
     * @param newOfferName
     * @param oldOfferExternalKey
     * @param newOfferExternalKey
     * @param oldStatus
     * @param newStatus
     * @param oldBillingFrequency
     * @param newBillingFrequency
     * @param assertionErrorList
     */
    public static void assertionsForSubscriptionOfferInAuditLogReportDescription(
        final Map<String, List<String>> descriptionPropertyValues, final String oldOfferName, final String newOfferName,
        final String oldOfferExternalKey, final String newOfferExternalKey, final Status oldStatus,
        final Status newStatus, final String oldBillingFrequency, final String newBillingFrequency,
        final List<AssertionError> assertionErrorList) {

        // Subscription Offer Name
        if (newOfferName != null) {
            final List<String> nameValues = descriptionPropertyValues.get(PelicanConstants.NAME_FIELD);
            if (oldOfferName == null) {
                AssertCollector.assertThat("Invalid old subscription offer name value in audit log report",
                    nameValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old subscription offer name value in audit log report",
                    nameValues.get(0), equalTo(oldOfferName), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new subscription offer name value in audit log report",
                nameValues.get(1), equalTo(newOfferName), assertionErrorList);
        }

        // Subscription Offer External Key
        if (newOfferExternalKey != null) {
            final List<String> externalKeyValues = descriptionPropertyValues.get(PelicanConstants.EXTERNAL_KEY_FIELD);
            if (oldOfferExternalKey == null) {
                AssertCollector.assertThat("Invalid old subscription offer external key value in audit log report",
                    externalKeyValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old subscription offer external key value in audit log report",
                    externalKeyValues.get(0), equalTo(oldOfferExternalKey), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new subscription offer external key value in audit log report",
                externalKeyValues.get(1), equalTo(newOfferExternalKey), assertionErrorList);
        }

        // Status
        if (newStatus != null) {
            final List<String> statusValues = descriptionPropertyValues.get(PelicanConstants.STATUS_FIELD);
            if (oldStatus == null) {
                AssertCollector.assertThat("Invalid old status value in audit log report", statusValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old status value in audit log report", statusValues.get(0),
                    equalTo(oldStatus.toString()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new status value in audit log report", statusValues.get(1),
                equalTo(newStatus.toString()), assertionErrorList);
        }

        // Billing Frequency
        if (newBillingFrequency != null) {
            final List<String> billingFrequencyValues =
                descriptionPropertyValues.get(PelicanConstants.BILLING_FREQUENCY_FIELD);
            if (oldBillingFrequency == null) {
                AssertCollector.assertThat("Invalid old billing frequency value in audit log report",
                    billingFrequencyValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old billing frequency value in audit log report",
                    billingFrequencyValues.get(0), equalTo(oldBillingFrequency), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new billing frequency value in audit log report",
                billingFrequencyValues.get(1), equalTo(newBillingFrequency), assertionErrorList);
        }
    }

    /**
     * This method does assertions for Subscription Price
     *
     * @param descriptionPropertyValues
     * @param oldCurrency
     * @param newCurrency
     * @param oldAmount
     * @param newAmount
     * @param oldPriceList
     * @param newPriceList
     * @param oldEffectiveStartDate
     * @param newEffectiveStartDate
     * @param oldEffectiveEndDate
     * @param newEffectiveEndDate
     * @param assertionErrorList
     */
    public static void assertionsForSubscriptionPriceInAuditLogReportDescription(
        final Map<String, List<String>> descriptionPropertyValues, final String oldCurrency, final String newCurrency,
        final String oldAmount, final String newAmount, final String oldPriceList, final String newPriceList,
        final String oldEffectiveStartDate, final String newEffectiveStartDate, final String oldEffectiveEndDate,
        final String newEffectiveEndDate, final List<AssertionError> assertionErrorList) {

        // Currency
        if (newCurrency != null) {
            final List<String> currencyValues = descriptionPropertyValues.get(PelicanConstants.CURRENCY_FIELD);
            if (oldCurrency == null) {
                AssertCollector.assertThat("Invalid old currency value in audit log report", currencyValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old currency value in audit log report", currencyValues.get(0),
                    equalTo(oldCurrency), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new currency value in audit log report", currencyValues.get(1),
                equalTo(newCurrency), assertionErrorList);
        }

        // Amount
        if (newAmount != null) {
            final List<String> amountValues = descriptionPropertyValues.get(PelicanConstants.AMOUNT_FIELD);
            if (oldAmount == null) {
                AssertCollector.assertThat("Invalid old amount value in audit log report", amountValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old amount value in audit log report", amountValues.get(0),
                    equalTo(oldAmount), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new amount value in audit log report", amountValues.get(1),
                equalTo(newAmount), assertionErrorList);
        }

        // Price List
        if (newPriceList != null) {
            final List<String> priceListValues = descriptionPropertyValues.get(PelicanConstants.PRICE_LIST_FIELD);
            if (oldPriceList == null) {
                AssertCollector.assertThat("Invalid old price list value in audit log report", priceListValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old price list value in audit log report", priceListValues.get(0),
                    equalTo(oldPriceList), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new price list value in audit log report", priceListValues.get(1),
                equalTo(newPriceList), assertionErrorList);
        }

        // Effective Start Date
        if (newEffectiveStartDate != null) {
            final List<String> effectiveStartDateValues =
                descriptionPropertyValues.get(PelicanConstants.EFFECTIVE_START_DATE_FIELD);
            if (oldEffectiveStartDate == null) {
                AssertCollector.assertThat("Invalid old effective start date value in audit log report",
                    effectiveStartDateValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old effective start date value in audit log report",
                    effectiveStartDateValues.get(0), equalTo(oldEffectiveStartDate), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new effective start date value in audit log report",
                effectiveStartDateValues.get(1), equalTo(newEffectiveStartDate), assertionErrorList);
        }

        // Effective End Date
        if (newEffectiveEndDate != null) {
            final List<String> effectiveEndDateValues =
                descriptionPropertyValues.get(PelicanConstants.EFFECTIVE_END_DATE_FIELD);
            if (oldEffectiveEndDate == null) {
                AssertCollector.assertThat("Invalid old effective end date value in audit log report",
                    effectiveEndDateValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old effective end date value in audit log report",
                    effectiveEndDateValues.get(0), equalTo(oldEffectiveEndDate), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new effective end date value in audit log report",
                effectiveEndDateValues.get(1), equalTo(newEffectiveEndDate), assertionErrorList);
        }
    }

    /**
     * This method does assertion on descriptor definition and value of Subscription Plan and Subscription Offer
     *
     * @param descriptionPropertyValues
     * @param descriptorId
     * @param descriptorOldValue
     * @param descriptorNewValue
     * @param fieldName
     * @param action
     * @param assertionErrorList
     */
    public static void assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
        final Map<String, List<String>> descriptionPropertyValues, final String descriptorId,
        final String descriptorOldValue, final String descriptorNewValue, final String fieldName, final Action action,
        final List<AssertionError> assertionErrorList) {

        if (Action.CREATE == action) {
            // Descriptor Definition
            final List<String> descriptorIdValues = descriptionPropertyValues.get(PelicanConstants.DEFINITION);
            AssertCollector.assertThat("Invalid old descriptor definition value in audit log report",
                descriptorIdValues.get(0), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new descriptor definition value in audit log report",
                descriptorIdValues.get(1), equalTo(fieldName + " (" + descriptorId + ")"), assertionErrorList);

            // Descriptor Value
            final List<String> descriptorValueValues = descriptionPropertyValues.get(PelicanConstants.VALUE_FIELD);
            AssertCollector.assertThat("Invalid old descriptor value value in audit log report",
                descriptorValueValues.get(0), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new descriptor value value in audit log report",
                descriptorValueValues.get(1), equalTo(descriptorNewValue), assertionErrorList);
        } else {
            // Descriptor Value
            final List<String> descriptorValue1Values = descriptionPropertyValues.get(fieldName);
            AssertCollector.assertThat("Invalid old descriptor value in audit log report",
                descriptorValue1Values.get(0), equalTo(descriptorOldValue), assertionErrorList);
            AssertCollector.assertThat("Invalid new descriptor value in audit log report",
                descriptorValue1Values.get(1), equalTo(descriptorNewValue), assertionErrorList);
        }

    }

    public static void assertionsForBasicOfferingInAuditLogReport(
        final Map<String, List<String>> descriptionPropertyValues, final String oldSubscriptionPlanName,
        final String newSubscriptionPlanName, final String oldExternalKey, final String newExternalKey,
        final OfferingType oldOfferingType, final OfferingType newOfferingType, final Status oldStatus,
        final Status newStatus, final UsageType oldUsageType, final UsageType newUsageType,
        final String oldOfferingDetailId, final String newOfferingDetailId, final String oldProductLineId,
        final String newProductLineId, final MediaType oldMediaType, final MediaType newMediaType,
        final EnvironmentVariables environmentVariables, final List<AssertionError> assertionErrorList) {

        commonAssertionsForBasicAndSubscriptionOfferingInfo(descriptionPropertyValues, oldSubscriptionPlanName,
            newSubscriptionPlanName, oldExternalKey, newExternalKey, oldOfferingType, newOfferingType, oldStatus,
            newStatus, oldUsageType, newUsageType, oldOfferingDetailId, newOfferingDetailId, oldProductLineId,
            newProductLineId, environmentVariables, assertionErrorList);

        // Media Type
        if (newMediaType != null) {
            final List<String> mediaTypeValues = descriptionPropertyValues.get(PelicanConstants.MEDIA_TYPE_FIELD);
            if (oldSubscriptionPlanName == null) {
                AssertCollector.assertThat("Invalid old media type value in audit log report", mediaTypeValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old media type value in audit log report", mediaTypeValues.get(0),
                    equalTo(oldMediaType.getDisplayValue()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new media type value in audit log report", mediaTypeValues.get(1),
                equalTo(newMediaType.getDisplayValue()), assertionErrorList);
        }

    }

    private static void commonAssertionsForBasicAndSubscriptionOfferingInfo(
        final Map<String, List<String>> descriptionPropertyValues, final String oldSubscriptionPlanName,
        final String newSubscriptionPlanName, final String oldExternalKey, final String newExternalKey,
        final OfferingType oldOfferingType, final OfferingType newOfferingType, final Status oldStatus,
        final Status newStatus, final UsageType oldUsageType, final UsageType newUsageType,
        final String oldOfferingDetailId, final String newOfferingDetailId, final String oldProductLineId,
        final String newProductLineId, final EnvironmentVariables environmentVariables,
        final List<AssertionError> assertionErrorList) {

        // Name
        if (newSubscriptionPlanName != null) {
            final List<String> nameValues = descriptionPropertyValues.get(PelicanConstants.NAME_FIELD);
            if (oldSubscriptionPlanName == null) {
                AssertCollector.assertThat("Invalid old name value in audit log report", nameValues.get(0), nullValue(),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old name value in audit log report", nameValues.get(0),
                    equalTo(oldSubscriptionPlanName), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new name value in audit log report", nameValues.get(1),
                equalTo(newSubscriptionPlanName), assertionErrorList);
        }

        // External Key
        if (newExternalKey != null) {
            final List<String> externalKeyValues = descriptionPropertyValues.get(PelicanConstants.EXTERNAL_KEY_FIELD);
            if (oldExternalKey == null) {
                AssertCollector.assertThat("Invalid old external key value in audit log report",
                    externalKeyValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old external key value in audit log report",
                    externalKeyValues.get(0), equalTo(oldExternalKey), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new external key value in audit log report", externalKeyValues.get(1),
                equalTo(newExternalKey), assertionErrorList);
        }

        // Offering Type
        if (newOfferingType != null) {
            final List<String> offeringTypeValues = descriptionPropertyValues.get(PelicanConstants.OFFERING_TYPE_FIELD);
            if (oldOfferingType == null) {
                AssertCollector.assertThat("Invalid old offering type value in audit log report",
                    offeringTypeValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old offering type value in audit log report",
                    offeringTypeValues.get(0), equalTo(oldOfferingType.toString()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new offering type value in audit log report", offeringTypeValues.get(1),
                equalTo(newOfferingType.toString()), assertionErrorList);
        }

        // Status
        if (newStatus != null) {
            final List<String> statusValues = descriptionPropertyValues.get(PelicanConstants.STATUS_FIELD);
            if (oldStatus == null) {
                AssertCollector.assertThat("Invalid old status value in audit log report", statusValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old status value in audit log report", statusValues.get(0),
                    equalTo(oldStatus.toString()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new status value in audit log report", statusValues.get(1),
                equalTo(newStatus.toString()), assertionErrorList);
        }

        // Usage Type
        if (newUsageType != null) {
            final List<String> usageTypeValues = descriptionPropertyValues.get(PelicanConstants.USAGE_TYPE_FIELD);
            if (oldUsageType == null) {
                AssertCollector.assertThat("Invalid old usage type value in audit log report", usageTypeValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old usage type value in audit log report", usageTypeValues.get(0),
                    equalTo(oldUsageType.getDisplayName()), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new usage type value in audit log report", usageTypeValues.get(1),
                equalTo(newUsageType.getDisplayName()), assertionErrorList);
        }

        if (newOfferingDetailId != null) {
            // Get Offering detail name by querying DB
            final String newOfferingDetailName =
                DbUtils.selectQuery("select NAME from offering_detail where id = " + newOfferingDetailId, "NAME",
                    environmentVariables).get(0);

            // Offering Detail Id
            final List<String> offeringDetailValues =
                descriptionPropertyValues.get(PelicanConstants.OFFERING_DETAIL_FIELD);
            if (oldOfferingDetailId == null) {
                AssertCollector.assertThat("Invalid old offering detail value in audit log report",
                    offeringDetailValues.get(0), nullValue(), assertionErrorList);
            } else {
                // Get Offering detail name by querying DB
                final String oldOfferingDetailName =
                    DbUtils.selectQuery("select NAME from offering_detail where id = " + oldOfferingDetailId, "NAME",
                        environmentVariables).get(0);
                AssertCollector.assertThat("Invalid old offering detail value in audit log report",
                    offeringDetailValues.get(0), equalTo(oldOfferingDetailName + " (" + oldOfferingDetailId + ")"),
                    assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new offering detail value in audit log report",
                offeringDetailValues.get(1), equalTo(newOfferingDetailName + " (" + newOfferingDetailId + ")"),
                assertionErrorList);
        }

        if (newProductLineId != null) {
            // Get Product Line name from DB
            final String newProductLineName =
                DbUtils.selectQuery("select NAME from product_line where id = " + newProductLineId, "NAME",
                    environmentVariables).get(0);

            // ProductLine
            final List<String> productLineValues = descriptionPropertyValues.get(PelicanConstants.PRODUCT_LINE_FIELD);
            if (oldProductLineId == null) {
                AssertCollector.assertThat("Invalid old product line value in audit log report",
                    productLineValues.get(0), nullValue(), assertionErrorList);
            } else {
                // Get Product Line name from DB
                final String oldProductLineName =
                    DbUtils.selectQuery("select NAME from product_line where id = " + oldProductLineId, "NAME",
                        environmentVariables).get(0);
                AssertCollector.assertThat("Invalid old product line value in audit log report",
                    productLineValues.get(0), equalTo(oldProductLineName + " (" + oldProductLineId + ")"),
                    assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new product line value in audit log report", productLineValues.get(1),
                equalTo(newProductLineName + " (" + newProductLineId + ")"), assertionErrorList);
        }
    }

}
