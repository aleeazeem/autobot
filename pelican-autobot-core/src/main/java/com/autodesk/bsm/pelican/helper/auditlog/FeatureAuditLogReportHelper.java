package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.util.AssertCollector;

import java.util.List;
import java.util.Map;

/**
 * This class is a helper class for Description column of Audit Log Report for Features
 *
 * @author Shweta Hegde
 */
public class FeatureAuditLogReportHelper {

    /**
     * This method accepts old and new values and validates them in Audit Log Report
     *
     * @param descriptionPropertyValues
     * @param oldFeatureName
     * @param newFeatureName
     * @param oldExternalKey
     * @param newExternalKey
     * @param oldFeatureType
     * @param newFeatureType
     * @param assertionErrorList
     * @param old status
     * @param new status
     */
    public static void assertionsForFeatureInAuditLogReport(final Map<String, List<String>> descriptionPropertyValues,
        final String oldFeatureName, final String newFeatureName, final String oldExternalKey,
        final String newExternalKey, final String oldFeatureType, final String newFeatureType, final String oldStatus,
        final String newStatus, final List<AssertionError> assertionErrorList) {

        // Name
        if (newFeatureName != null) {
            final List<String> nameValues = descriptionPropertyValues.get(PelicanConstants.NAME_FIELD);
            if (oldFeatureName == null) {
                AssertCollector.assertThat("Invalid old feature name value in audit log report", nameValues.get(0),
                    nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old feature name value in audit log report", nameValues.get(0),
                    equalTo(oldFeatureName), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new feature name value in audit log report", nameValues.get(1),
                equalTo(newFeatureName), assertionErrorList);
        }

        // External Key
        if (newExternalKey != null) {
            final List<String> externalKeyValues = descriptionPropertyValues.get(PelicanConstants.EXTERNAL_KEY_FIELD);
            if (oldExternalKey == null) {
                AssertCollector.assertThat("Invalid old feature external key value in audit log report",
                    externalKeyValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old feature external key value in audit log report",
                    externalKeyValues.get(0), equalTo(oldExternalKey), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new feature external key value in audit log report",
                externalKeyValues.get(1), equalTo(newExternalKey), assertionErrorList);
        }

        // Feature Type
        if (newFeatureType != null) {
            final List<String> featureTypeValues = descriptionPropertyValues.get(PelicanConstants.FEATURE_TYPE_FIELD);
            if (oldFeatureType == null) {
                AssertCollector.assertThat("Invalid old feature type value in audit log report",
                    featureTypeValues.get(0), nullValue(), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid old feature type value in audit log report",
                    featureTypeValues.get(0), equalTo(oldFeatureType), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new feature type value in audit log report", featureTypeValues.get(1),
                equalTo(newFeatureType), assertionErrorList);
        }

        // Status
        if (newStatus != null) {
            final List<String> statusValues = descriptionPropertyValues.get(PelicanConstants.IS_ACTIVE);
            if (oldStatus == null) {
                AssertCollector.assertThat("Incorrect old status for the feature", statusValues.get(0), nullValue(),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect old status for the feature", statusValues.get(0),
                    equalTo(oldStatus), assertionErrorList);
            }
            AssertCollector.assertThat("Invalid new feature status value in audit log report", statusValues.get(1),
                equalTo(newStatus), assertionErrorList);
        }
    }
}
