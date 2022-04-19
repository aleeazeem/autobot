package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a class which will do the audit log validations on different actions such as Create/Edit/Delete of Roles
 *
 * @author vineel
 */
public abstract class RoleAuditLogHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAuditLogHelper.class.getSimpleName());

    /**
     * This is a method to validate the audit log entry for role creation/edit/deletion
     *
     * @param assertionErrorList
     */
    public static void validateRoleData(final String roleId, final String oldRoleName, final String oldRoleDescription,
        final HashMap<String, String> oldPropertiesMap, final String oldPermission, final String newRoleName,
        final String newRoleDescription, final HashMap<String, String> newPropertiesMap, final String newPermission,
        final Action action, final String userId, final List<AssertionError> assertionErrorList) {

        String newPropertiesValue = null;
        if (newPropertiesMap != null && Action.UPDATE == action) {
            newPropertiesValue = PelicanConstants.PROPERTIES_VALUE + newPropertiesMap.keySet().toArray()[0] + "="
                + newPropertiesMap.get(newPropertiesMap.keySet().toArray()[0]) + "&"
                + newPropertiesMap.keySet().toArray()[1] + "="
                + newPropertiesMap.get(newPropertiesMap.keySet().toArray()[1]);
        } else if (oldPropertiesMap != null && Action.DELETE == action) {
            newPropertiesValue = PelicanConstants.PROPERTIES_VALUE + oldPropertiesMap.keySet().toArray()[0] + "="
                + oldPropertiesMap.get(oldPropertiesMap.keySet().toArray()[0]) + "&"
                + oldPropertiesMap.keySet().toArray()[1] + "="
                + oldPropertiesMap.get(oldPropertiesMap.keySet().toArray()[1]);
        }

        Util.waitInSeconds(TimeConstants.THREE_SEC);

        List<Map<String, AttributeValue>> items;
        if (Action.CREATE == action) {
            items = DynamoDBUtil.role(roleId);
        } else {
            items = DynamoDBUtil.role(roleId);
        }
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);
        AssertCollector.assertThat("Audit log data for Roles is not found", items.size(), greaterThanOrEqualTo(1),
            assertionErrorList);
        boolean auditDataFound = false;
        final AuditLogEntry auditLogEntry = auditLogEntries.get(0);
        LOGGER.info("Audit Log Entry:" + auditLogEntry.toString());
        AssertCollector.assertThat("Invalid audit id value", auditLogEntry.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid audit date value", auditLogEntry.getDate(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid audit timestamp value", auditLogEntry.getTimestamp(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid user id value", auditLogEntry.getUserId(), equalTo(userId),
            assertionErrorList);
        AssertCollector.assertThat("Invalid ip address value", auditLogEntry.getIpAddress(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid entity type value", auditLogEntry.getEntityType(),
            equalTo(PelicanConstants.ROLE), assertionErrorList);
        AssertCollector.assertThat("Invalid entity id value", auditLogEntry.getEntityId(), equalTo(roleId),
            assertionErrorList);
        final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
        if (Action.CREATE == action && Action.CREATE.toString().equals(auditLogEntry.getAction())
            && oldPropertiesMap == null) {
            AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                equalTo(roleId), assertionErrorList);
            AssertCollector.assertThat("Invalid old role name value",
                auditData.get(PelicanConstants.NAME).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new role name value",
                auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newRoleName), assertionErrorList);
            AssertCollector.assertThat("Invalid old role description value",
                auditData.get(PelicanConstants.DESCRIPTION).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new role description value",
                auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), equalTo(newRoleDescription),
                assertionErrorList);
            AssertCollector.assertThat("Invalid old role properties value",
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid new role properties value",
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), equalTo(PelicanConstants.PROPERTIES_VALUE),
                assertionErrorList);
            auditDataFound = true;
        } else if (Action.UPDATE == action && Action.UPDATE.toString().equals(auditLogEntry.getAction())) {
            // assertion on name
            if (auditData.get(PelicanConstants.NAME) != null && oldRoleName != null && newRoleName != null) {
                AssertCollector.assertThat("Invalid old name", auditData.get(PelicanConstants.NAME).getOldValue(),
                    equalTo(oldRoleName), assertionErrorList);
                AssertCollector.assertThat("Invalid new name", auditData.get(PelicanConstants.NAME).getNewValue(),
                    equalTo(newRoleName), assertionErrorList);
            }
            // assertion on external key
            if (oldRoleDescription != null && newRoleDescription != null) {
                AssertCollector.assertThat("Invalid old role description",
                    auditData.get(PelicanConstants.DESCRIPTION).getOldValue(), equalTo(oldRoleDescription),
                    assertionErrorList);
                AssertCollector.assertThat("Invalid new role description",
                    auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), equalTo(newRoleDescription),
                    assertionErrorList);
            }
            // assertion on permissions
            if (auditData.get(PelicanConstants.PERMISSIONS) != null) {

                if (oldPermission == null && newPermission != null) {
                    AssertCollector.assertThat("Incorrect Properties old value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect Properties new value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getNewValue(), equalTo(newPermission),
                        assertionErrorList);
                } else if (oldPermission != null && newPermission != null) {
                    AssertCollector.assertThat("Incorrect Properties old value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getOldValue(), equalTo(oldPermission),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect Properties new value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getNewValue(), equalTo(newPermission),
                        assertionErrorList);
                } else if (oldPermission != null && newPermission == null) {
                    AssertCollector.assertThat("Incorrect Properties old value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getOldValue(), equalTo(oldPermission),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect Properties new value for Role : " + roleId,
                        auditData.get(PelicanConstants.PERMISSIONS).getNewValue(), nullValue(), assertionErrorList);
                }
            }
            if (oldPropertiesMap != null && newPropertiesMap != null) {
                AssertCollector.assertThat("Invalid role old properties value",
                    auditData.get(PelicanConstants.PROPERTIES).getOldValue(),
                    equalTo(PelicanConstants.PROPERTIES_VALUE), assertionErrorList);
                AssertCollector.assertThat("Invalid new role properties value",
                    auditData.get(PelicanConstants.PROPERTIES).getNewValue(), equalTo(newPropertiesValue),
                    assertionErrorList);
            }
            auditDataFound = true;
        } else if (Action.DELETE == action && Action.DELETE.toString().equals(auditLogEntry.getAction())
            && oldPropertiesMap != null) {
            AssertCollector.assertThat("Invalid old id value", auditData.get(PelicanConstants.ID).getOldValue(),
                equalTo(roleId), assertionErrorList);
            AssertCollector.assertThat("Invalid new id value", auditData.get(PelicanConstants.ID).getNewValue(),
                nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid old role name value",
                auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldRoleName), assertionErrorList);
            AssertCollector.assertThat("Invalid new role name value",
                auditData.get(PelicanConstants.NAME).getNewValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid old role description value",
                auditData.get(PelicanConstants.DESCRIPTION).getOldValue(), equalTo(oldRoleDescription),
                assertionErrorList);
            AssertCollector.assertThat("Invalid new role description value",
                auditData.get(PelicanConstants.DESCRIPTION).getNewValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid old role properties value",
                auditData.get(PelicanConstants.PROPERTIES).getOldValue(), equalTo(newPropertiesValue),
                assertionErrorList);
            AssertCollector.assertThat("Invalid new role properties value",
                auditData.get(PelicanConstants.PROPERTIES).getNewValue(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect permission old value for Role : " + roleId,
                auditData.get(PelicanConstants.PERMISSIONS).getOldValue(), equalTo(oldPermission), assertionErrorList);
            AssertCollector.assertThat("Incorrect permission new value for Role : " + roleId,
                auditData.get(PelicanConstants.PERMISSIONS).getNewValue(), nullValue(), assertionErrorList);

            auditDataFound = true;
        }

        AssertCollector.assertThat(action.toString() + " Role audit data not found", auditDataFound, equalTo(true),
            assertionErrorList);
        items.clear();
    }
}
