package com.autodesk.bsm.pelican.ui.pages.reports;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the page object for Audit log Report result page.
 *
 * @author t_joshv
 */
public class AuditLogReportResultPage extends GenericGrid {

    /**
     * Paging Size.
     */
    public static final int PAGE_SIZE = 20;

    /**
     * Description spliter regex.
     */
    private static final String DESCRIPTION_SPLITTER = "\\.\n";

    /**
     * Property regex."<b>(.+?)</b>";
     */

    private static final String PROPERTY_REGEX = "([\\w\\.\\s]+) (set|changed|was)";

    /**
     * Values regex.
     */
    private static final String VALUE_REGEX = "\"([^\"]*)\"";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogReportResultPage.class.getSimpleName());

    /**
     * Find all href linked to object id.
     */
    @FindAll(@FindBy(xpath = ".//*[@id='find-results']/div[3]/table/tbody/tr/td[2]/a"))
    private List<WebElement> objectIdLinks;

    public AuditLogReportResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to get all values in Parent Object column as list.
     *
     * @return List of Parent Object.
     */
    private List<String> getValuesFromParentObjectColumn() {
        return getColumnValues(PelicanConstants.PARENT_OBJECT_FIELD);
    }

    /**
     * Method to get all values in Parent Object Id column as list.
     *
     * @return List of Parent Object Id.
     */
    public List<String> getIdValuesFromParentObjectColumn() {
        final List<String> parentObjectIdValues = new ArrayList<>();
        final List<String> columnValues = getValuesFromParentObjectColumn();

        for (final String value : columnValues) {
            if (value != null) {
                final String columnValue = value.contains("(") && value.contains(")")
                    ? value.substring(value.indexOf("(") + 1, value.indexOf(")"))
                    : value;

                parentObjectIdValues.add(columnValue);
            }
        }

        return parentObjectIdValues;
    }

    /**
     * Method to get all values in Entity Type column as list.
     *
     * @return List of Entity Type
     */
    public List<String> getValuesFromEntityTypeColumn() {
        return getColumnValues(PelicanConstants.ENTITY_TYPE_FIELD);
    }

    /**
     * Method to get all values in Object Id column as list.
     *
     * @return List of Object.
     */
    public List<String> getValuesFromObjectColumn() {
        return getColumnValues(PelicanConstants.OBJECT_FIELD);
    }

    /**
     * Method to get all values in Object Id column as list.
     *
     * @return List of Object Id.
     */
    public List<String> getIdValuesFromObjectColumn() {
        final List<String> objectIdValues = new ArrayList<>();
        final List<String> columnValues = getValuesFromObjectColumn();

        for (final String value : columnValues) {
            if (value != null) {
                final String columnValue = value.contains("(") && value.contains(")")
                    ? value.substring(value.indexOf("(") + 1, value.indexOf(")"))
                    : value;

                objectIdValues.add(columnValue);
            }
        }

        return objectIdValues;
    }

    /**
     * Method to get all values in change date column as list.
     *
     * @return List of change date.
     */
    public List<String> getValuesFromChangeDateColumn() {
        return getColumnValues(PelicanConstants.CHANGE_DATE_FIELD);
    }

    /**
     * Method to get all values in User column as list.
     *
     * @return List of User.
     */
    private List<String> getValuesFromUserColumn() {
        return getColumnValues(PelicanConstants.USER_FIELD);
    }

    /**
     * Method to get user id values in User column as list.
     *
     * @return List of User Ids.
     */
    public List<String> getUserIdFromUserColumn() {
        final List<String> userIdValues = new ArrayList<>();
        final List<String> columnValues = getValuesFromUserColumn();

        for (final String value : columnValues) {
            if (value != null) {
                final String columnValue = value.contains("(") && value.contains(")")
                    ? value.substring(value.indexOf("(") + 1, value.indexOf(")"))
                    : value;
                userIdValues.add(columnValue);
            }
        }

        return userIdValues;
    }

    /**
     * Method to get all values in Action column as list.
     *
     * @return List of Action.
     */
    public List<String> getValuesFromActionColumn() {
        return getColumnValues(PelicanConstants.ACTION_FIELD);
    }

    /**
     * Method to get all values in Description column as list.
     *
     * @return List of Description.
     */
    public List<String> getValuesFromDescriptionColumn() {
        return getColumnValues(PelicanConstants.DESCRIPTION_FIELD);
    }

    /**
     * Method return Href value for objectID
     *
     * @param rowIndex
     * @return href value
     */
    public String getObjectIdLinkValue(final int rowIndex) {
        return objectIdLinks.get(rowIndex).getAttribute("href");
    }

    /**
     * Search Description column values from grid based on criteria.
     *
     * @param parentObjectId parent entity id.
     * @param entityType entity type.
     * @param objectId entitiy id.
     * @param action action.
     * @return description column values.
     */
    public int getDescriptionRowIndex(final String parentObjectId, final String entityType, final String objectId,
        final String action) {

        LOGGER.info(String.format("Searching Description using following criteria:%s %s %s %s", entityType,
            parentObjectId, objectId, action));

        final int rowIndex = -1;
        final List<String> parentObjectIdColumnValues = getIdValuesFromParentObjectColumn();
        final List<String> entityTypeColumnValues = getValuesFromEntityTypeColumn();
        final List<String> objectIdColumnValues = getIdValuesFromObjectColumn();
        final List<String> actionColumnValues = getValuesFromActionColumn();

        LOGGER.info("Search Description, Total Rows:" + getTotalItems());

        for (int i = 0; i < getTotalItems(); i++) {

            if (parentObjectId == null) {
                if (objectIdColumnValues.get(i).equals(objectId)
                    && entityTypeColumnValues.get(i).trim().equals(entityType)
                    && actionColumnValues.get(i).trim().equals(action)) {

                    LOGGER.info("Description Row Index Found:" + i);

                    return i;
                }
            } else {

                if (parentObjectIdColumnValues.get(i).equals(parentObjectId)
                    && objectIdColumnValues.get(i).equals(objectId)
                    && actionColumnValues.get(i).trim().equals(action)) {

                    LOGGER.info("Description Row Index Found:" + i);

                    return i;
                }

            }

        }
        return rowIndex;

    }

    /**
     * Search Description column values from grid based on criteria.
     *
     * @param parentObjectId parent entity id.
     * @param entityType entity type.
     * @param objectId entity id.
     * @param action action.
     * @return description column values.
     */
    public String searchDescription(final String parentObjectId, final String entityType, final String objectId,
        final String action) {

        final int rowIndex = getDescriptionRowIndex(parentObjectId, entityType, objectId, action);
        if (rowIndex != -1) {
            LOGGER.info("Search Description, Found:" + getValuesFromDescriptionColumn().get(rowIndex));
            return getValuesFromDescriptionColumn().get(rowIndex);
        }

        return null;

    }

    /**
     * Parse description column string in property -> Values hashmap.
     *
     * @param description string.
     * @return Description property -> Values map.
     *         <p>
     *         Description : property : String , Values<String>
     *         <p>
     *         Example : Property : currencyId Values(0) : oldValue Values(1) : newValue
     */
    public HashMap<String, List<String>> parseDescription(final String description) {

        final Pattern propertyPattern = Pattern.compile(PROPERTY_REGEX);
        final Pattern valuePattern = Pattern.compile(VALUE_REGEX);
        final HashMap<String, List<String>> descriptionPropertyValues = new HashMap<>();

        // Split the Description on "\n"
        final String[] descArray = description.split(DESCRIPTION_SPLITTER);

        // Loop through each line.
        for (final String descVal : descArray) {

            String desc = descVal.replaceAll("(\r\n|\n)", "");
            LOGGER.info("Parse Description, raw:" + desc);

            final String[] descSplit = desc.split("changed");

            if (descSplit.length > 1) {
                desc = descSplit[0].split("\\(")[0] + "changed" + descSplit[1];
            }

            // Look for Property Name in description string.
            final Matcher propertyMatcher = propertyPattern.matcher(desc);

            // look for Property Values in description string.
            final Matcher valueMatcher = valuePattern.matcher(desc);

            if (propertyMatcher.find()) {

                final String propertyName = propertyMatcher.group(1).trim();
                LOGGER.info("Parse Description, Property:" + propertyName);
                final List<String> propertyValues = new ArrayList<>();

                while (valueMatcher.find()) {
                    LOGGER.info("Parse Description, Values:" + valueMatcher.group(1));
                    propertyValues.add(valueMatcher.group(1).trim());
                }

                if (propertyValues.size() == 1) {
                    propertyValues.add(0, null);
                }

                descriptionPropertyValues.put(propertyName, propertyValues);
            }
        }

        return descriptionPropertyValues;
    }

    /**
     * Method to build Link for Parent Entity.
     *
     * @param entityType
     * @param parentObjectId
     * @return
     */
    public String generateParentLink(final String entityType, final String parentObjectId) {

        final String suffixUrl = "/show?id=" + parentObjectId;

        switch (entityType) {
            case PelicanConstants.ENTITY_SUBSCRIPTION_PLAN:
                return getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION_PLAN.getForm() + suffixUrl;
            case PelicanConstants.ENTITY_BASIC_OFFERING:
                return getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + suffixUrl;
            case PelicanConstants.ENTITY_FEATURE:
                return getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE.getForm() + suffixUrl;
            default:
                return null;
        }
    }

    /**
     * Method to validate audit report for remove of feature from subscription plan.
     *
     * @param auditLogReportResultPage
     * @param subPlan
     * @param feature
     * @param action
     * @param errors TODO
     * @return Boolean
     */
    public Boolean validateFeatureRemoveFromAuditLog(final AuditLogReportResultPage auditLogReportResultPage,
        final String subPlan, final String feature, final String action,
        final List<AssertionError> assertionErrorList) {

        Boolean foundAuditEntry = false;
        LOGGER.info("total rows: " + auditLogReportResultPage.getTotalItems());
        for (int i = 0; i < auditLogReportResultPage.getTotalItems(); i++) {
            if (auditLogReportResultPage.getColumnValues(PelicanConstants.OBJECT_FIELD, 1).get(i).equals(feature)) {
                AssertCollector.assertThat("Action is not Delete",
                    auditLogReportResultPage.getColumnValues(PelicanConstants.ACTION_FIELD).get(i),
                    equalTo(Action.DELETE.getDisplayName()), assertionErrorList);
                AssertCollector.assertThat("Parent Object is not right",
                    auditLogReportResultPage.getColumnValues(PelicanConstants.PARENT_OBJECT_FIELD).get(i),
                    equalTo(subPlan), assertionErrorList);
                foundAuditEntry = true;
            }
        }

        return foundAuditEntry;
    }
}
