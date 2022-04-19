package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_BASIC_OFFERING;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_DESCRIPTOR;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_FEATURE;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_FEATURES;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_SUBSCRIPTION_OFFER;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_SUBSCRIPTION_PLAN;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.ENTITY_SUBSCRIPTION_PRICE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.hamcrest.core.Every;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Audit log report tests. Accessible in admin tool under Reports -> Audit Log Reports.
 *
 * @author t_joshv
 */
public class ViewAuditLogReportTest extends SeleniumWebdriver {

    private AuditLogReportPage auditLogReportPage;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private boolean featureFlagValue;

    /* Fields to hide in the Audit log report */
    private static final String[] SUBSCRIPTION_PLAN_HIDE_FIELDS =
        { "id", "appFamilyId", "created", "lastModified", "createdById", "lastModifiedById", "oneTimeEntitlements" };
    private static final String[] SUBSCRIPTION_OFFER_HIDE_FIELDS =
        { "id", "appFamilyId", "created", "planId", "order", "billingOption.billingDate" };
    private static final String[] SUBSCRIPTION_PRICE_HIDE_FIELDS = { "id", "offeringId", "offerId" };
    private static final String[] SUBSCRIPTION_ENTITLEMENT_HIDE_FIELDS = { "id", "relatedId" };
    private static final String[] SUBSCRIPTION_ENTITLEMENT_CUSTOM_HIDE_FIELD = { "amount.currencyID" };
    private static final String[] BASIC_OFFERING_HIDE_FIELDS =
        { "id", "created", "createdById", "lastModified", "lastModifiedById", "appFamilyId" };
    private static final String[] DESCRIPTORS_HIDE_FIELDS =
        { "id", "timestamp.created", "timestamp.lastModified", "appFamilyId", "entityId" };
    private static final String[] FEATURES_HIDE_FIELDS =
        { "id", "timestamp.created", "timestamp.lastModified", "appId" };
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewAuditLogReportTest.class.getSimpleName());

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        adminToolUserId = getEnvironmentVariables().getUserId();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        featureFlagValue =
            bankingConfigurationPropertiesPage.getFeatureFlag(PelicanConstants.SHOW_ES_LOGGING_DELAY_ON_REPORT);
    }

    /*
     * Verify all headers of columns in Report.
     *
     * @result report should show 7 columns and name of the headers of each column should be Parent Entity ID, Entity
     * Type, Entity Id, Change Date, User, Action, Description
     */
    @Test
    public void testAuditLogReportHeaders() {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(null, null, null, null, null, false);

        final List<String> auditLogReportResultPageColumnHeaders = auditLogReportResultPage.getColumnHeaders();

        AssertCollector.assertThat("Incorrect Header 1", auditLogReportResultPageColumnHeaders.get(0),
            equalTo(PelicanConstants.ENTITY_TYPE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", auditLogReportResultPageColumnHeaders.get(1),
            equalTo(PelicanConstants.OBJECT_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", auditLogReportResultPageColumnHeaders.get(2),
            equalTo(PelicanConstants.PARENT_OBJECT_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", auditLogReportResultPageColumnHeaders.get(3),
            equalTo(PelicanConstants.CHANGE_DATE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", auditLogReportResultPageColumnHeaders.get(4),
            equalTo(PelicanConstants.USER_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", auditLogReportResultPageColumnHeaders.get(5),
            equalTo(PelicanConstants.ACTION_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", auditLogReportResultPageColumnHeaders.get(6),
            equalTo(PelicanConstants.DESCRIPTION_FIELD), assertionErrorList);
        if (featureFlagValue) {
            AssertCollector.assertThat("Incorrect Header 8", auditLogReportResultPageColumnHeaders.get(7),
                equalTo(PelicanConstants.ES_LOGGING_DELAY_FIELD), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that report returns correct data with selected filters.
     */
    @Test(dataProvider = "dataForAuditLogReport")
    public void testAuditLogReportData(String changeDateFrom, String changeDateTo, final String entityType,
        final String objectId, final String userId) {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(changeDateFrom, changeDateTo, entityType, objectId, userId, false);

        final int totalRecordsInReport = auditLogReportResultPage.getTotalItems();
        LOGGER.info("Total Audit Log Report Records:" + totalRecordsInReport);

        // check if report is empty or not
        if (totalRecordsInReport > 0) {
            // get index of selected row
            final int selectedRowIndex = auditLogReportResultPage.selectRowRandomlyFromFirstPage(totalRecordsInReport);

            // validation on entity Type column
            if (entityType != null) {
                final List<String> entityTypeColumnList = auditLogReportResultPage.getValuesFromEntityTypeColumn();

                LOGGER
                    .info("Validating entity Type in entity type column:" + entityTypeColumnList.get(selectedRowIndex));

                AssertCollector.assertTrue("Entity type is not present in column values",
                    entityTypeColumnList.contains(entityType), assertionErrorList);
                for (int index = 0; index < entityTypeColumnList.size(); index++) {
                    final String entityTypeValue = entityTypeColumnList.get(index);
                    // EntityType -> SubscriptionPlan
                    if (entityTypeValue.equalsIgnoreCase(ENTITY_SUBSCRIPTION_PLAN)) {
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            SUBSCRIPTION_PLAN_HIDE_FIELDS, assertionErrorList);
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_SUBSCRIPTION_OFFER)) {
                        // EntityType -> SubscriptionOffer
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            SUBSCRIPTION_OFFER_HIDE_FIELDS, assertionErrorList);
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_SUBSCRIPTION_PRICE)) {
                        // EntityType -> SubscriptionPrice
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            SUBSCRIPTION_PRICE_HIDE_FIELDS, assertionErrorList);
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_SUBSCRIPTION_ENTITLEMENT)) {
                        // EntityType -> SubscriptionEntitlement
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            SUBSCRIPTION_ENTITLEMENT_HIDE_FIELDS, assertionErrorList);
                        // amount.currencyID (don't show if its type ITEM, if its Cloud Credits then show it)
                        if (entityType.equalsIgnoreCase(ENTITY_FEATURE)) {
                            auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                                SUBSCRIPTION_ENTITLEMENT_CUSTOM_HIDE_FIELD, assertionErrorList);
                        }
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_FEATURES)) {
                        // EntityType -> Features
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index, FEATURES_HIDE_FIELDS,
                            assertionErrorList);
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_BASIC_OFFERING)) {
                        // EntityType -> BasicOffering
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            BASIC_OFFERING_HIDE_FIELDS, assertionErrorList);
                    } else if (entityTypeValue.equalsIgnoreCase(ENTITY_DESCRIPTOR)) {
                        // EntityType -> Descriptors
                        auditLogReportHelper.validateHiddenField(auditLogReportResultPage, index,
                            DESCRIPTORS_HIDE_FIELDS, assertionErrorList);
                    }
                }
            }

            // validation on user id column
            if (userId != null) {
                final List<String> userIdList = auditLogReportResultPage.getUserIdFromUserColumn();
                LOGGER.info("Validating user id in user column:" + userIdList.get(selectedRowIndex));

                AssertCollector.assertThat("User id in user column is not correct", userIdList.get(selectedRowIndex),
                    equalTo(userId), assertionErrorList);

            }

            // If No From and To Date Filter specified,
            // Report should return results for last 24 hours by default.
            if (changeDateFrom == null) {
                changeDateFrom = DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -90);
            }

            if (changeDateTo == null) {
                changeDateTo = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
            }

            // validation on change date from
            if (changeDateFrom != null) {
                LOGGER.info("Validating change date from");
                AssertCollector.assertThat("All records should be after " + changeDateFrom + " date",
                    DateTimeUtils.convertStringListToDateList(auditLogReportResultPage.getValuesFromChangeDateColumn(),
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(greaterThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(changeDateFrom, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);

            }

            // validation on change date to
            if (changeDateTo != null) {
                LOGGER.info("Validating change date to");
                AssertCollector.assertThat(
                    "Creation end date for all records should be less than " + changeDateTo + " date",
                    DateTimeUtils.convertStringListToDateList(auditLogReportResultPage.getValuesFromChangeDateColumn(),
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(changeDateTo, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);

            }

        } else {
            // fail the test if no data is found with the selected filters. If
            // test fails due to this reason, update
            // data provider such that it returns data.
            Assert.fail("Report does not have any data to run the test.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Audit Log Report Pagination works fine.
     */
    @Test
    public void testAuditLogReportPagination() {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(null, null, null, null, null, false);

        LOGGER
            .info("Total Page Count:" + auditLogReportResultPage.getTotalPageCount(AuditLogReportResultPage.PAGE_SIZE));

        AssertCollector.assertThat("Wrong current page index", auditLogReportResultPage.getCurrentPageIndex(),
            equalTo(1), assertionErrorList);

        if (auditLogReportResultPage.getTotalPageCount(AuditLogReportResultPage.PAGE_SIZE) > 1) {
            auditLogReportResultPage.navigateToNextPage();
            Util.waitInSeconds(TimeConstants.TWO_SEC);

            LOGGER.info("Current Page Index:" + auditLogReportResultPage.getCurrentPageIndex());

            AssertCollector.assertThat("Wrong current page index", auditLogReportResultPage.getCurrentPageIndex(),
                equalTo(2), assertionErrorList);

            auditLogReportResultPage.navigateToPrevPage();
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            LOGGER.info("Current Page Index:" + auditLogReportResultPage.getCurrentPageIndex());

            AssertCollector.assertThat("Wrong current page index", auditLogReportResultPage.getCurrentPageIndex(),
                equalTo(1), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void verifyInvalidDateRangeErrorMessage() {
        auditLogReportPage.generateReport(DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1),
            DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH), null, null, null, false);

        AssertCollector.assertThat("Error message is not correct", auditLogReportPage.getError(),
            equalTo(PelicanErrorConstants.END_DATE_BEFORE_START_DATE_ERROR_MEESAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void verifyDateRangeDoesNotExceedNintyDaysErrorMessage() {
        auditLogReportPage.generateReport(
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -95),
            DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH), null, null, null, false);

        AssertCollector.assertThat("Error message is not correct", auditLogReportPage.getError(),
            equalTo(PelicanErrorConstants.DATE_RANGE_NINTYDAYS_ERROR_MEESAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Entity is linked to its Parent Entity.
     */
    @Test(dataProvider = "dataForAuditLogReport")
    public void testEntityIsLinkedToParentEntity(final String changeDateFrom, final String changeDateTo,
        final String entityType, final String objectId, final String userId) {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(changeDateFrom, changeDateTo, entityType, objectId, userId, false);

        final int totalRecordsInReport = auditLogReportResultPage.getTotalItems();
        LOGGER.info("Total Audit Log Report Records:" + totalRecordsInReport);

        if (totalRecordsInReport > 0) {

            // Get index of randomly selected row
            final int selectedRowIndex = auditLogReportResultPage.selectRowRandomlyFromFirstPage(totalRecordsInReport);

            // Get Entity Type Value
            final String entityTypeValue =
                (entityType == null) ? PelicanConstants.ENTITY_SUBSCRIPTION_PLAN : entityType;

            // Get Parent Entity ID Value
            final String parentObjectIdValue =
                auditLogReportResultPage.getIdValuesFromParentObjectColumn().get(selectedRowIndex);

            final String objectIdValue = parentObjectIdValue.equals("-")
                ? auditLogReportResultPage.getIdValuesFromObjectColumn().get(selectedRowIndex)
                : parentObjectIdValue;

            // Get href value linked to entityId
            final String objectIdLinkValue = auditLogReportResultPage.getObjectIdLinkValue(selectedRowIndex);
            LOGGER.info("Actual Entity ID Link Value :" + objectIdLinkValue);

            // Generate expected href value
            final String expectedObjectIdLinkValue =
                auditLogReportResultPage.generateParentLink(entityTypeValue, objectIdValue);
            LOGGER.info("Expected Object Id Link Value :" + expectedObjectIdLinkValue);

            AssertCollector.assertThat("Object Id is not linked to its Root Ancestor Entity",
                auditLogReportResultPage.getObjectIdLinkValue(selectedRowIndex), equalTo(expectedObjectIdLinkValue),
                assertionErrorList);

            AssertCollector.assertAll(assertionErrorList);
        }
    }

    /**
     * Verify that Description Column has field names as per entity detail page naming convention.
     */
    @Test(dataProvider = "dataForAuditLogReport")
    public void testToVerifyFieldNameAsPerEntityDetailPage(final String changeDateFrom, final String changeDateTo,
        final String entityType, final String entityId, final String userId) {

        final AuditLogReportResultPage auditLogReportResultPage =
            auditLogReportPage.generateReport(changeDateFrom, changeDateTo, entityType, entityId, userId, false);

        final int totalRecordsInReport = auditLogReportResultPage.getTotalItems();
        LOGGER.info("Total Audit Log Report Records:" + totalRecordsInReport);

        if (totalRecordsInReport > 0) {

            // Get index of randomly selected row
            final int selectedRowIndex = auditLogReportResultPage.selectRowRandomlyFromFirstPage(totalRecordsInReport);

            LOGGER.info("Selected Row index:" + selectedRowIndex);
            final String columnEntityType =
                auditLogReportResultPage.getValuesFromEntityTypeColumn().get(selectedRowIndex);
            final String description = auditLogReportResultPage.getValuesFromDescriptionColumn().get(selectedRowIndex);
            AssertCollector.assertThat("Description values is not present column values", description, notNullValue(),
                assertionErrorList);
            LOGGER.info("Description Column value:" + description);

            if (auditLogReportHelper.isDescriptionHasValues(description, columnEntityType)) {
                final HashMap<String, List<String>> descriptionPropertyValues =
                    auditLogReportResultPage.parseDescription(description);
                Set<String> descriptionFields = descriptionPropertyValues.keySet();
                final Set<String> propertyFields = AuditLogReportHelper.PropertyFieldName.get(columnEntityType);

                // Remove value with .xlsx.
                descriptionFields = AuditLogReportHelper.removeXlsxValueFromDescriptionFields(descriptionFields);

                LOGGER.info("DescriptionFields " + descriptionFields.toString());
                LOGGER.info("PropertyFields " + propertyFields.toString());
                AssertCollector.assertTrue("Field names for " + columnEntityType + " are not as expected.",
                    propertyFields.containsAll(descriptionFields), assertionErrorList);

            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider with different filters.
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForAuditLogReport")
    public Object[][] dataForAuditLogReport() {
        return new Object[][] { { null, null, null, null, null },
                { DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -5),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH), null, null,
                        adminToolUserId },
                { DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -7),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                        PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, null, adminToolUserId },
                { DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -10),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                        PelicanConstants.ENTITY_BASIC_OFFERING, null, adminToolUserId },
                { DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, -15),
                        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                        PelicanConstants.ENTITY_FEATURE, null, adminToolUserId },

        };
    }

}
