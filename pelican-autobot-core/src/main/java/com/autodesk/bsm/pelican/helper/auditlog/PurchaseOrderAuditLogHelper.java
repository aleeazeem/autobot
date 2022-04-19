package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;

/**
 * This class serves as a common class for Dynamo DB query of Purchase Order and assertion
 *
 * @author Vaibhavi
 */
public class PurchaseOrderAuditLogHelper {

    public static boolean helperToValidateDynamoDbForPurchaseOrder(final String purchaseOrderId, final Action action,
        final String userId, final List<AssertionError> assertionErrorList) {
        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.purchaseOrder(purchaseOrderId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {

            if (auditLogEntry.getAction().equals(action.toString())) {

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
                    equalTo(PelicanConstants.PURCHASE_ORDER), assertionErrorList);
                AssertCollector.assertThat("Invalid entity id value", auditLogEntry.getEntityId(),
                    equalTo(purchaseOrderId), assertionErrorList);

                return true;
            }
        }

        return false;
    }
}
