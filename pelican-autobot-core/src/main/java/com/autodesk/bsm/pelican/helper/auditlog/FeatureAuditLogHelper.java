package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;

public abstract class FeatureAuditLogHelper {

    public static void validateFeatureData(final String featureId, final String oldAppId, final String newAppId,
        final String oldFeatureTypeId, final String newFeatureTypeId, final String oldName, final String newName,
        final String oldExternalKey, final String newExternalKey, final String oldProperties,
        final String newProperties, final Action action, final String userId, final String fileName,
        final String oldStatus, final String newStatus, final List<AssertionError> assertionErrorList) {
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.feature(featureId);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit feature data not found", items.size(), greaterThanOrEqualTo(1),
            assertionErrorList);
        boolean auditDataFound = false;

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
            if (Action.CREATE == action && Action.CREATE.toString().equals(auditLogEntry.getAction())) {
                AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                    equalTo(featureId), assertionErrorList);
                AssertCollector.assertThat("Invalid old appId value",
                    auditData.get(PelicanConstants.APP_ID).getOldValue(), equalTo(oldAppId), assertionErrorList);
                AssertCollector.assertThat("Invalid new appId value",
                    auditData.get(PelicanConstants.APP_ID).getNewValue(), equalTo(newAppId), assertionErrorList);
                AssertCollector.assertThat("Invalid old featureTypeId value",
                    auditData.get(PelicanConstants.TYPE_ID).getOldValue(), equalTo(oldFeatureTypeId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new featureTypeId value",
                    auditData.get(PelicanConstants.TYPE_ID).getNewValue(), equalTo(newFeatureTypeId),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid old name", auditData.get(PelicanConstants.NAME).getOldValue(),
                    equalTo(oldName), assertionErrorList);
                AssertCollector.assertThat("Invalid new name", auditData.get(PelicanConstants.NAME).getNewValue(),
                    equalTo(newName), assertionErrorList);
                AssertCollector.assertThat("Invalid old external key",
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new external key",
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                    assertionErrorList);
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
                AssertCollector.assertThat("Incorrect old status value",
                    auditData.get(PelicanConstants.IS_ACTIVE).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect new status value",
                    auditData.get(PelicanConstants.IS_ACTIVE).getNewValue(), equalTo(newStatus), assertionErrorList);
                auditDataFound = true;
            } else if (Action.UPDATE == action && Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
                // assertion on name
                if (auditData.get(PelicanConstants.NAME) != null && oldName != null && newName != null) {
                    AssertCollector.assertThat("Invalid old name", auditData.get(PelicanConstants.NAME).getOldValue(),
                        equalTo(oldName), assertionErrorList);
                    AssertCollector.assertThat("Invalid new name", auditData.get(PelicanConstants.NAME).getNewValue(),
                        equalTo(newName), assertionErrorList);
                }
                // assertion on external key
                if (auditData.get(PelicanConstants.EXTERNAL_KEY) != null && oldExternalKey != null
                    && newExternalKey != null) {
                    AssertCollector.assertThat("Invalid old external key",
                        auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                        assertionErrorList);
                    AssertCollector.assertThat("Invalid new external key",
                        auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                        assertionErrorList);
                    // assertion on feature type id
                    if (auditData.get(PelicanConstants.TYPE_ID) != null && oldFeatureTypeId != null
                        && newFeatureTypeId != null) {
                        AssertCollector.assertThat("Invalid old type id",
                            auditData.get(PelicanConstants.TYPE_ID).getOldValue(), equalTo(oldFeatureTypeId),
                            assertionErrorList);
                        AssertCollector.assertThat("Invalid new type id",
                            auditData.get(PelicanConstants.TYPE_ID).getNewValue(), equalTo(newFeatureTypeId),
                            assertionErrorList);
                    }
                    AssertCollector.assertThat("Invalid lastModified timestamp old value",
                        auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getOldValue(), notNullValue(),
                        assertionErrorList);
                    AssertCollector.assertThat(
                        "lastModified timestamp new value is NOT greater than lastModified timestamp" + " old value",
                        auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getNewValue(),
                        greaterThan(auditData.get(PelicanConstants.TIMESTAMP_LAST_MODIFIED).getOldValue()),
                        assertionErrorList);
                    if (auditData.get(PelicanConstants.AUDIT_LOG_PROPERTIES) != null && oldProperties != null
                        && newProperties != null) {
                        AssertCollector.assertThat("Invalid properties old value",
                            auditData.get(PelicanConstants.AUDIT_LOG_PROPERTIES).getOldValue(), equalTo(oldProperties),
                            assertionErrorList);
                        AssertCollector.assertThat("Invalid properties new value",
                            auditData.get(PelicanConstants.AUDIT_LOG_PROPERTIES).getNewValue(), equalTo(newProperties),
                            assertionErrorList);
                    }
                    if (newStatus != null) {
                        AssertCollector.assertThat("Incorrect old status value",
                            auditData.get(PelicanConstants.IS_ACTIVE).getOldValue(), equalTo(oldStatus),
                            assertionErrorList);
                        AssertCollector.assertThat("Incorrect new status value",
                            auditData.get(PelicanConstants.IS_ACTIVE).getNewValue(), equalTo(newStatus),
                            assertionErrorList);
                    }
                }
                AssertCollector.assertThat("Incorrect user id", auditLogEntry.getUserId(), equalTo(userId),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect IP", auditLogEntry.getIpAddress(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect upload file name", auditLogEntry.getFileName(), equalTo(fileName),
                    assertionErrorList);
                auditDataFound = true;
            }

            AssertCollector.assertThat(action.toString() + " feature audit data not found", auditDataFound,
                equalTo(true), assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        }
    }

    /**
     * This method returns the property value in audit log format
     *
     * @return propertyName=propertyValue
     */
    public static String getPropertyValueFormatInAuditLog(final String propertyName, final String propertyValue) {
        return propertyName.concat("=").concat(propertyValue);
    }
}
