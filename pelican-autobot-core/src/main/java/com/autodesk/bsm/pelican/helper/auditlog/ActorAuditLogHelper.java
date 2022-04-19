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
 * This class serves as a common class for Dynamo DB query of Actor and assertion
 *
 * @author Vaibhavi
 */
public class ActorAuditLogHelper {

    /**
     * * This is a method to validate the audit log entry for actor creation/edit
     *
     * @param assertionErrorList
     */
    public static boolean helperToValidateDynamoDbForActor(final String actorId, final String oldAppFamilyId,
        final String newAppFamilyId, final String oldName, final String newName, final String oldUUID,
        final String newUUID, final String oldExternalKey, final String newExternalKey, final String oldId,
        final Action action, final String userId, final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.actor(actorId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(action.toString())) {

                String newId = actorId;
                if (action == Action.CREATE) {
                    newId = actorId;
                }
                if (action == Action.UPDATE) {
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
                    equalTo(PelicanConstants.ACTOR), assertionErrorList);
                AssertCollector.assertThat("Invalid entity id value", auditLogEntry.getEntityId(), equalTo(actorId),
                    assertionErrorList);

                commonAssertionsCreateUpdateActor(auditData, auditLogEntry.getEntityId(), newAppFamilyId, oldName,
                    newName, newUUID, oldExternalKey, newExternalKey, oldId, newId, assertionErrorList);

            }
            return true;
        }

        return false;
    }

    private static void commonAssertionsCreateUpdateActor(final Map<String, ChangeDetails> auditData,
        final String actorId, final String newAppFamilyId, final String oldName, final String newName,
        final String newUUID, final String oldExternalKey, final String newExternalKey, final String oldId,
        final String newId, final List<AssertionError> assertionErrorList) {

        // Actor Creation Scenario
        if (oldId == null && newId != null) {

            AssertCollector.assertThat("Incorrect id old value for Actor : " + actorId,
                auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for Actor: " + actorId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(actorId), assertionErrorList);

            AssertCollector.assertThat("Incorrect App Family Id old value for Actor : " + actorId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect App Family Id new value for Actor : " + actorId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getNewValue(), equalTo(newAppFamilyId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Name old value for Actor : " + actorId,
                auditData.get(PelicanConstants.NAME).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect Name new value for Actor : " + actorId,
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newName), assertionErrorList);

            AssertCollector.assertThat("Incorrect UUID old value for Actor : " + actorId,
                auditData.get(PelicanConstants.UUID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect UUID new value for Actor : " + actorId,
                auditData.get(PelicanConstants.UUID).getNewValue(), equalTo(newUUID), assertionErrorList);
        }

        // Actor Update Scenario
        if (oldId == null && newId == null) {

            // assertion on name
            if (auditData.get(PelicanConstants.NAME) != null && oldName != null && newName != null) {

                AssertCollector.assertThat("Incorrect Name old value for Actor : " + actorId,
                    auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldName), assertionErrorList);
                AssertCollector.assertThat("Incorrect Name new value for Actor : " + actorId,
                    auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newName), assertionErrorList);
            }

            // assertion on externalKey
            if (auditData.get(PelicanConstants.EXTERNAL_KEY) != null && oldExternalKey != null
                && newExternalKey != null) {

                AssertCollector.assertThat("Incorrect ExternalKey old value for Actor : " + actorId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect ExternalKey new value for Actor : " + actorId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                    assertionErrorList);
            }

        }

    }

}
