package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for Audit Log Report asserts.
 *
 * @author t_joshv
 */
public class AuditLogReportHelper {

    private EnvironmentVariables environmentVariables;
    private AuditLogReportPage auditLogReportPage;
    private static final String GENERIC_UPDATE_DESCRIPTION = "Changes only on related entities.";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogReportHelper.class.getSimpleName());

    /**
     * HashMap for Entity/Object Type and its DB query
     */
    private static final Map<String, String> entitySqlQuery = new HashMap<String, String>() {

        private static final long serialVersionUID = 112981671726236692L;

        {
            put(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, "select NAME from offering where ID=%s");
            put(PelicanConstants.ENTITY_SUBSCRIPTION_OFFER, "select NAME from subscription_offer where ID=%s");
            put(PelicanConstants.ENTITY_SUBSCRIPTION_PRICE, "SELECT P.NAME FROM SUBSCRIPTION_PRICE AS SP "
                + "LEFT JOIN PRICE_LIST P ON P.ID = SP.PRICE_LIST_ID " + "WHERE SP.ID=%s");
            put(PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT, "SELECT CASE SE.CUR_ID WHEN 0 THEN IT.NAME "
                + "ELSE TC.CURRENCY_KEY END AS name, SE.ITEM_ID, IT.ID,SE.CUR_ID,TC.ID from subscription_entitlement "
                + "AS SE left join item AS IT ON SE.ITEM_ID = IT.ID  left join twofish_currency as tc "
                + "on SE.CUR_ID = TC.ID WHERE SE.ID =%s");
            put(PelicanConstants.ENTITY_BASIC_OFFERING, "select NAME from offering where ID=%s");
            put(PelicanConstants.ENTITY_FEATURE, "select NAME from item where ID=%s");
            put(PelicanConstants.ENTITY_DESCRIPTOR, "select dd.FIELD_NAME from descriptor_definition as dd "
                + "left join descriptor as d on dd.ID = d.DEFINITION_ID where d.ID = %s");
        }
    };

    /*
     * Fields to verify for Subscription Plan , Subscription Offer, Subscription Price, Entitlement, Descriptor, Basic
     * Offering and Feature Entities.
     */
    public static final Map<String, Set<String>> PropertyFieldName = new HashMap<String, Set<String>>() {

        private static final long serialVersionUID = -770475870577969047L;

        {
            put(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                new HashSet<>(Arrays.asList("Properties", "External Key", "Name", "Offering Detail", "Product Line",
                    "Offering Type", "Status", "Support Level", "Usage Type", "Is Module", "Cancellation Policy",
                    "Short Description", "Long Description", "Small Image URL", "Medium Image URL", "Large Image URL",
                    "Button Display Name", "Packaging Type", "Send Expiration Reminder Emails",
                    "lastFeatureCompositionChangedTime")));
            put(PelicanConstants.ENTITY_SUBSCRIPTION_OFFER, new HashSet<>(
                Arrays.asList("Name", "External Key", "Billing Frequency", "Billing Cycle Count", "Status")));
            put(PelicanConstants.ENTITY_SUBSCRIPTION_PRICE, new HashSet<>(
                Arrays.asList("Currency", "Amount", "Price List", "Effective Start Date", "Effective End Date")));
            put(PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT,
                new HashSet<>(Arrays.asList("Entitlement Type", "Grant Type", "Feature", "Currency", "Licensing Model",
                    "Core Products", "Amount", "assignable", "eosDate", "eolImmediateDate", "eolRenewalDate")));
            put(PelicanConstants.DESCRIPTOR,
                new HashSet<>(Arrays.asList("Definition", "Value", "Language", "Country", "AUTO_TEST_DESCRIPTOR")));
            put(PelicanConstants.ENTITY_BASIC_OFFERING,
                new HashSet<>(Arrays.asList("Properties", "External Key", "Name", "Offering Detail", "Status",
                    "Product Line", "Offering Type", "Usage Type", "Support Level", "Media Type", "Language Code")));
            put(PelicanConstants.ENTITY_FEATURE, new HashSet<>(
                Arrays.asList("Name", "Feature Type", "External Key", "isActive", "Properties", "itemDetailId")));

        }
    };

    public AuditLogReportHelper(final AdminToolPage adminToolPage) {
        LOGGER.info("Initializing Instance of Audit Log Report Helper.");
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        environmentVariables = new PelicanEnvironment().initializeEnvironmentVariables();
    }

    /**
     * Verify Audit Log Report Results Page.
     *
     * @param changeDateFrom - From Date
     * @param changeDateTo - To Date
     * @param entityType - Entity Type
     * @param parentObjectId - Parent Object Id
     * @param objectId - Entity Id
     * @param userId - User Id
     * @param action - Action
     * @param objectName
     * @param assertionErrorList
     */
    public HashMap<String, List<String>> verifyAuditLogReportResults(final String changeDateFrom,
        final String changeDateTo, final String entityType, final String parentObjectId, final String objectId,
        final String userId, final String action, final String objectName,
        final List<AssertionError> assertionErrorList) {

        HashMap<String, List<String>> descriptionPropertyValues = null;
        final String auditLogReportEntityId = parentObjectId == null ? objectId : parentObjectId;
        final AuditLogReportResultPage auditLogReportResultPage = auditLogReportPage.generateReport(changeDateFrom,
            changeDateTo, entityType, auditLogReportEntityId, userId, false);

        final int totalRecordsInReport = auditLogReportResultPage.getTotalItems();
        LOGGER.info("Total Audit Log Report Records:" + totalRecordsInReport);

        AssertCollector.assertThat("Audit Log Report results count is not correct ", totalRecordsInReport,
            greaterThanOrEqualTo(1), assertionErrorList);

        if (totalRecordsInReport > 0) {

            // validation on entity Type column
            if (entityType != null) {

                LOGGER.info("Validating Entity Type:" + entityType);

                final List<String> entityTypeColumnList = auditLogReportResultPage.getValuesFromEntityTypeColumn();

                AssertCollector.assertTrue("Entity type is not present in column values",
                    entityTypeColumnList.contains(entityType), assertionErrorList);

            }

            // validating on Parent entity Id column
            if (parentObjectId != null) {

                LOGGER.info("Validating Parent Object Id:" + parentObjectId);

                final List<String> parentObjectIdColumnList =
                    auditLogReportResultPage.getIdValuesFromParentObjectColumn();

                final List<String> objectIdColumnList = auditLogReportResultPage.getIdValuesFromObjectColumn();

                AssertCollector.assertTrue("Parent Object ID is not present in column values",
                    (parentObjectIdColumnList.contains(parentObjectId) || objectIdColumnList.contains(parentObjectId)),
                    assertionErrorList);

                // Assert for Parent Object Name
                String parentObjectName;
                String parentObjectIdFromColumn;
                // get Parent Object Value which has Name and ID
                final List<String> parentObjectColumnList = auditLogReportResultPage.getValuesFromObjectColumn();

                for (int i = 0; i < parentObjectColumnList.size(); i++) {
                    final String actionColumn = auditLogReportResultPage.getValuesFromActionColumn().get(i);
                    // get list of Parent Object Name
                    parentObjectName = parentObjectColumnList.get(i).split(" \\(")[0];
                    // get list of Parent Object ID
                    parentObjectIdFromColumn = parentObjectColumnList.get(i).split("\\(")[1].replace(")", "");
                    // get Object Name from DB to put validation on Parent Object Name
                    if (parentObjectId.equals(parentObjectIdFromColumn) && actionColumn.equals(action)) {
                        if (entitySqlQuery
                            .containsKey(auditLogReportResultPage.getValuesFromEntityTypeColumn().get(i))) {
                            final String parentEntityTypeSQLQuery = String.format(
                                entitySqlQuery.get(auditLogReportResultPage.getValuesFromEntityTypeColumn().get(i)),
                                parentObjectId);
                            final String parentObjectNameFromDB =
                                DbUtils.selectQuery(parentEntityTypeSQLQuery, "NAME", environmentVariables).get(0);
                            LOGGER.info("Parent Entity Name From DB:" + parentObjectNameFromDB);
                            LOGGER.info("Parent Entity Name From Audit Log Report:" + parentObjectName);
                            // Validation for Parent Object column field : it should as expected in audit log report as
                            // per DB.
                            AssertCollector.assertThat(
                                "Incorrect Entity name in parent object column in audit log report",
                                parentObjectName + " (" + parentObjectId + ")",
                                equalTo(parentObjectNameFromDB + " (" + parentObjectIdFromColumn + ")"),
                                assertionErrorList);
                        }
                    }
                    break;
                }
            }

            // validation on Object Id column
            if (objectId != null) {
                LOGGER.info("Validating Object Id:" + objectId);

                final List<String> objectIdColumnList = auditLogReportResultPage.getIdValuesFromObjectColumn();

                AssertCollector.assertTrue("Object ID is not present in column values",
                    objectIdColumnList.contains(objectId), assertionErrorList);

                // Assert for Object Name
                String objectNameFromAuditLogReport;
                String objectIdFromColumn;
                final List<String> objectColumnList = auditLogReportResultPage.getValuesFromObjectColumn();

                for (int i = 0; i < objectColumnList.size(); i++) {
                    objectNameFromAuditLogReport = objectColumnList.get(i).split(" \\(")[0];
                    objectIdFromColumn = objectColumnList.get(i).split("\\(")[1].replace(")", "");

                    if (objectId.equals(objectIdFromColumn)) {
                        final String objectType = auditLogReportResultPage.getValuesFromEntityTypeColumn().get(i);

                        if (entitySqlQuery
                            .containsKey(auditLogReportResultPage.getValuesFromEntityTypeColumn().get(i))) {
                            final String entityTypeSQLQuery = String.format(
                                entitySqlQuery.get(auditLogReportResultPage.getValuesFromEntityTypeColumn().get(i)),
                                objectId);
                            String columnName = PelicanConstants.NAME;
                            if (objectType.equals(PelicanConstants.ENTITY_DESCRIPTOR)) {
                                columnName = "FIELD_NAME";
                            }
                            if (action.equals(auditLogReportResultPage.getValuesFromActionColumn().get(i))) {
                                String objectNameFromDB = objectName;
                                if (objectName == null) {
                                    objectNameFromDB = DbUtils
                                        .selectQuery(entityTypeSQLQuery, columnName, environmentVariables).get(0);
                                }
                                LOGGER.info("Object Name From DB:" + objectNameFromDB);
                                LOGGER.info("Object Name From Audit Log Report:" + objectNameFromAuditLogReport);
                                // Validation for Object Column field, it should as expected in audit log report as per
                                // DB.
                                AssertCollector.assertThat("Incorrect Object name in Object column in audit log report",
                                    objectNameFromAuditLogReport + " (" + objectId + ")",
                                    equalTo(objectNameFromDB + " (" + objectIdFromColumn + ")"), assertionErrorList);
                            }
                        }
                    }
                }
            }
            // validating on Action column
            if (action != null) {

                LOGGER.info("Validating Action:" + action);

                final List<String> actionColumnList = auditLogReportResultPage.getValuesFromActionColumn();
                LOGGER.info("Action column length:" + actionColumnList.size());
                LOGGER.info("Action Column values:" + Arrays.toString(actionColumnList.toArray()));
                AssertCollector.assertTrue("Action: '" + action + "' is not present in column values",
                    actionColumnList.contains(action), assertionErrorList);

                // validating description column
                if (action.equals(Action.DELETE.getDisplayName())) {

                    final int descriptionRowIndex =
                        auditLogReportResultPage.getDescriptionRowIndex(parentObjectId, entityType, objectId, action);
                    final String description =
                        auditLogReportResultPage.searchDescription(parentObjectId, entityType, objectId, action);

                    LOGGER.info("Description Row index:" + descriptionRowIndex);
                    LOGGER.info("Description Column value:" + description);

                    AssertCollector.assertThat("Description values is not present column values", description,
                        notNullValue(), assertionErrorList);

                    if (description != null) {
                        final List<String> entityTypeColumnList =
                            auditLogReportResultPage.getValuesFromEntityTypeColumn();
                        final String expectedDescription = entityTypeColumnList.get(descriptionRowIndex) + " deleted.";

                        AssertCollector.assertThat("Wrong Description values for Delete Action", description,
                            equalTo(expectedDescription), assertionErrorList);
                    }

                } else {
                    final String description =
                        auditLogReportResultPage.searchDescription(parentObjectId, entityType, objectId, action);

                    AssertCollector.assertThat("Description values is not present column values", description,
                        notNullValue(), assertionErrorList);

                    if (description != null) {
                        descriptionPropertyValues = auditLogReportResultPage.parseDescription(description);
                    }
                }
            }
        }

        return descriptionPropertyValues;

    }

    public HashMap<String, List<String>> getAuditDescription(final String parentEntityType,
        final String childrenEntityType, final String parentObjectId, final String objectId, final String action) {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(null, null, parentEntityType, parentObjectId, null, false);
        HashMap<String, List<String>> descriptionPropertyValues = null;

        final String description =
            auditLogReportResultPage.searchDescription(parentObjectId, childrenEntityType, objectId, action);

        if (description != null) {
            descriptionPropertyValues = auditLogReportResultPage.parseDescription(description);
        }

        return descriptionPropertyValues;
    }

    /**
     * As we don't want to see all the fields in the Audit log reports, it is decided to hide some of the fields based
     * on the Entity type This method validates hidden fields in the description for the given set of entity.
     *
     * @param auditLogReportResultPage
     * @param index
     * @param fieldsToHide
     * @param assertionErrorList
     */
    public void validateHiddenField(final AuditLogReportResultPage auditLogReportResultPage, final int index,
        final String[] fieldsToHide, final List<AssertionError> assertionErrorList) {
        final String descriptionValue = auditLogReportResultPage.getValuesFromDescriptionColumn().get(index);
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportResultPage.parseDescription(descriptionValue);
        for (final String fieldToHide : fieldsToHide) {
            AssertCollector.assertThat(
                fieldToHide + " -> field is not hidden for Audit Report for Subscription Plan " + "related entities",
                descriptionPropertyValues.get(fieldToHide), equalTo(null), assertionErrorList);
        }
    }

    /**
     * Method to verify Description column has field and values or just generic statement for UPDATE and DELETE Action.
     *
     * @param descriptionValue
     * @param entityType
     * @return
     */
    public boolean isDescriptionHasValues(final String descriptionValue, final String entityType) {
        return !(descriptionValue != null && (descriptionValue.equals(GENERIC_UPDATE_DESCRIPTION)
            || descriptionValue.equals(entityType + " Deleted.")));
    }

    /**
     * Method to remove .xlsx value from the description fields of audit log report.
     *
     * @param descriptionFields
     * @return Set<String>
     */
    public static Set<String> removeXlsxValueFromDescriptionFields(final Set<String> descriptionFields) {
        if (descriptionFields.toString().contains(".xlsx")) {
            final Iterator<String> iterator = descriptionFields.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().endsWith(".xlsx")) {
                    iterator.remove();
                }
            }
        }
        return descriptionFields;
    }
}
