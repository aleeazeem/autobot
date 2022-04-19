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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class serves as a common class for Dynamo DB query of Role Assignment and assertion
 *
 * @author Vaibhavi
 */
public class RoleAssignmentAuditLogHelper {

    public static boolean helperToValidateDynamoDbForRoleAssignment(final String roleAssignmentId,
        final String oldAppFamilyId, final String newAppFamilyId, final String oldNamedPartyId,
        final String newNamedPartyId, final String oldRoleId, final String newRoleId,
        final HashMap<String, String> oldPropertiesMap, final HashMap<String, String> newPropertiesMap, String oldId,
        final Action action, final String userId, final List<AssertionError> assertionErrorList) {

        String oldProperties = null;
        String newProperties = null;
        String propertiesValue = null;

        if (newPropertiesMap != null && Action.UPDATE == action) {
            propertiesValue = PelicanConstants.PROPERTIES_VALUE + newPropertiesMap.keySet().toArray()[0] + "="
                + newPropertiesMap.get(newPropertiesMap.keySet().toArray()[0]) + "&"
                + newPropertiesMap.keySet().toArray()[1] + "="
                + newPropertiesMap.get(newPropertiesMap.keySet().toArray()[1]);
        } else if (oldPropertiesMap != null && Action.DELETE == action) {
            propertiesValue = PelicanConstants.PROPERTIES_VALUE + oldPropertiesMap.keySet().toArray()[0] + "="
                + oldPropertiesMap.get(oldPropertiesMap.keySet().toArray()[0]) + "&"
                + oldPropertiesMap.keySet().toArray()[1] + "="
                + oldPropertiesMap.get(oldPropertiesMap.keySet().toArray()[1]);
        }

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.roleAssignment(roleAssignmentId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(action.toString())) {

                String newId = roleAssignmentId;
                if (action == Action.CREATE) {
                    oldId = null;
                    newId = roleAssignmentId;
                    newProperties = PelicanConstants.PROPERTIES_VALUE;
                }
                if (action == Action.UPDATE) {
                    oldId = null;
                    newId = null;
                    oldProperties = PelicanConstants.PROPERTIES_VALUE;
                    newProperties = propertiesValue;
                }
                if (action == Action.DELETE) {
                    oldId = roleAssignmentId;
                    newId = null;
                    oldProperties = propertiesValue;
                    newProperties = null;
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
                    equalTo(PelicanConstants.ROLE_ASSIGNMENT), assertionErrorList);
                AssertCollector.assertThat("Invalid entity id value", auditLogEntry.getEntityId(),
                    equalTo(roleAssignmentId), assertionErrorList);

                commonAssertionsCreateUpdateDeleteRoleAssignment(auditData, auditLogEntry.getEntityId(), oldAppFamilyId,
                    newAppFamilyId, oldNamedPartyId, newNamedPartyId, oldProperties, newProperties, oldId, newId,
                    assertionErrorList);

            }
            return true;
        }

        return false;
    }

    private static void commonAssertionsCreateUpdateDeleteRoleAssignment(final Map<String, ChangeDetails> auditData,
        final String roleAssignmentId, final String oldAppFamilyId, final String newAppFamilyId,
        final String oldNamedPartyId, final String newNamedPartyId, final String oldProperties,
        final String newProperties, final String oldId, final String newId,
        final List<AssertionError> assertionErrorList) {

        // Role Assignment Creation Scenario
        if (oldId == null && newId != null) {

            AssertCollector.assertThat("Incorrect id old value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for Role Assignment: " + roleAssignmentId,
                auditData.get(PelicanConstants.ID).getNewValue(), equalTo(roleAssignmentId), assertionErrorList);

            AssertCollector.assertThat("Incorrect App Family Id old value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect App Family Id new value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getNewValue(), equalTo(newAppFamilyId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Named Party Id old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.NAMED_PARTY_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect Named Party Id new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.NAMED_PARTY_ID).getNewValue(), equalTo(newNamedPartyId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect RoleId old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.ROLE_ID).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect RoleId new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.ROLE_ID).getNewValue(), notNullValue(), assertionErrorList);

            AssertCollector.assertThat("Incorrect properties old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect properties new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), equalTo(newProperties), assertionErrorList);
        }

        // Role Update Scenario (no ID will be available in audit log)
        if (oldId == null && newId == null) {

            // assertion on properties
            if (auditData.get(PelicanConstants.PROPERTIES) != null && oldProperties != null && newProperties != null) {

                AssertCollector.assertThat("Incorrect Properties old value for Role : " + roleAssignmentId,
                    auditData.get(PelicanConstants.PROPERTIES).getOldValue(), equalTo(oldProperties),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect Properties new value for Role : " + roleAssignmentId,
                    auditData.get(PelicanConstants.PROPERTIES).getNewValue(), equalTo(newProperties),
                    assertionErrorList);
            }

        }

        // Role Delete Scenario
        if (oldId != null && newId == null) {

            AssertCollector.assertThat("Incorrect id old value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.ID).getOldValue(), equalTo(roleAssignmentId), assertionErrorList);
            AssertCollector.assertThat("Incorrect id new value for Role Assignment: " + roleAssignmentId,
                auditData.get(PelicanConstants.ID).getNewValue(), nullValue(), assertionErrorList);

            AssertCollector.assertThat("Incorrect App Family Id old value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getOldValue(), equalTo(oldAppFamilyId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect App Family Id new value for Role Assignment : " + roleAssignmentId,
                auditData.get(PelicanConstants.APP_FAMILY_ID).getNewValue(), nullValue(), assertionErrorList);

            AssertCollector.assertThat("Incorrect Named Party Id old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.NAMED_PARTY_ID).getOldValue(), equalTo(oldNamedPartyId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Named Party Id new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.NAMED_PARTY_ID).getNewValue(), nullValue(), assertionErrorList);

            AssertCollector.assertThat("Incorrect RoleId old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.ROLE_ID).getOldValue(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect RoleId new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.ROLE_ID).getNewValue(), nullValue(), assertionErrorList);

            AssertCollector.assertThat("Incorrect properties old value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), equalTo(oldProperties), assertionErrorList);
            AssertCollector.assertThat("Incorrect properties new value for Role : " + roleAssignmentId,
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), nullValue(), assertionErrorList);

        }

    }

}
