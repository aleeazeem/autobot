package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;

/**
 * This class serves as a common class for Dynamo DB query of API Secret Credential and assertion
 *
 * @author Vaibhavi
 */
public class ApiSecretCredentialAuditLogHelper {

    /**
     * This is a method to validate the audit log entry for ApiSecretCredential creation/edit/deletion
     *
     * @param assertionErrorList
     */
    public static boolean helperToValidateDynamoDbForApiSecretCredential(final String apiSecretCredentialId,
        final String oldOwnerId, final String newOwnerid, String oldId, final Action action, final String userId,
        final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.apiSecretCredential(apiSecretCredentialId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(action.toString())) {

                String newId = apiSecretCredentialId;
                if (action == Action.CREATE) {
                    oldId = null;
                    newId = apiSecretCredentialId;
                }
                if (action == Action.UPDATE) {
                    oldId = null;
                    newId = null;
                }
                if (action == Action.DELETE) {
                    oldId = apiSecretCredentialId;
                    newId = null;
                }

                AssertCollector.assertThat("Invalid audit id value", auditLogEntry.getId(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid audit date value", auditLogEntry.getDate(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid audit timestamp value", auditLogEntry.getTimestamp(),
                    notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect user id", auditLogEntry.getUserId(), equalTo(userId),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect IP", auditLogEntry.getIpAddress(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid entity type value", auditLogEntry.getEntityType(),
                    equalTo(PelicanConstants.API_SECRET_CREDENTIAL), assertionErrorList);
                AssertCollector.assertThat("Invalid entity id value", auditLogEntry.getEntityId(),
                    equalTo(apiSecretCredentialId), assertionErrorList);

                commonAssertionsCreateUpdateApiSecretCredential(auditData, auditLogEntry.getEntityId(), oldOwnerId,
                    newOwnerid, oldId, newId, assertionErrorList);
            }

            return true;
        }

        return false;

    }

    private static void commonAssertionsCreateUpdateApiSecretCredential(final Map<String, ChangeDetails> auditData,
        final String apiSecretCredentialId, final String oldOwnerId, final String newOwnerId, final String oldId,
        final String newId, final List<AssertionError> assertionErrorList) {

        // ApiSecretCredential Creation Scenario
        if (oldId == null && newId != null) {

            AssertCollector.assertThat("Incorrect id old value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for ApiSecretCredential: " + apiSecretCredentialId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(apiSecretCredentialId), assertionErrorList);

            AssertCollector.assertThat(
                "Incorrect Owner Id old value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.OWNER_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Owner Id new value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.OWNER_ID).getNewValue(), equalTo(newOwnerId), assertionErrorList);

        }

        // ApiSecretCredential Delete Scenario
        if (oldId != null && newId == null) {
            AssertCollector.assertThat("Incorrect id old value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.ID).getOldValue(), equalTo(apiSecretCredentialId), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for ApiSecretCredential: " + apiSecretCredentialId,
                auditData.get(PelicanConstants.ID).getNewValue(), nullValue(), assertionErrorList);

            AssertCollector.assertThat(
                "Incorrect Owner Id old value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.OWNER_ID).getOldValue(), equalTo(oldOwnerId), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Owner Id new value for ApiSecretCredential : " + apiSecretCredentialId,
                auditData.get(PelicanConstants.OWNER_ID).getNewValue(), nullValue(), assertionErrorList);
        }

    }

}
