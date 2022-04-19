package com.autodesk.bsm.pelican.ui.events;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.EventJobStatus;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.events.EventJobStatusDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.events.EventJobsSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.events.FindEventJobsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * This Class tests the functionality of find events. This can be search in admin tool under Application tab --> events
 * --> Find event jobs
 *
 * @author Muhammad
 */
public class FindEventJobsTest extends SeleniumWebdriver {

    private FindEventJobsPage findEventJobsPage;
    private static final String START_DATE = DateTimeUtils.getNowMinusDays(5) + " 00:00:00";
    private static final String END_DATE = DateTimeUtils.getCurrentDate() + " 24:60:60";
    private EventJobStatusDetailsPage eventJobStatusPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericGrid.class.getSimpleName());
    private static final String BATCH_SIZE = "100";

    /**
     *
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findEventJobsPage = adminToolPage.getPage(FindEventJobsPage.class);
        eventJobStatusPage = adminToolPage.getPage(EventJobStatusDetailsPage.class);
    }

    /**
     * This methods test the search page with selected filters.
     *
     * @param category
     * @param jobStatus
     * @param createdStartDate
     * @param createdEndDate
     */
    @Test(dataProvider = "testDataForFindEventsJob")
    public void testAdvancedFindSearch(final JobCategory category, final EventJobStatus jobStatus,
        final String createdStartDate, final String createdEndDate) {
        findEventJobsPage.findJobsThroughAdvancedFind(category, jobStatus, createdStartDate, createdEndDate);
        final EventJobsSearchResultsPage eventJobsSearchPage = findEventJobsPage.clickOnFindEventJobsButton();
        if (eventJobsSearchPage.getTotalRows() > 1) {
            AssertCollector.assertThat("Column values of category are not correct",
                eventJobsSearchPage.getColumnValuesOfCategory(), everyItem(equalTo(category.getJobCategory())),
                assertionErrorList);
            if (jobStatus != null) {
                AssertCollector.assertThat("Column values of status are other then" + jobStatus.getName(),
                    eventJobsSearchPage.getColumnValuesOfStatus(), everyItem(equalTo(jobStatus.getName())),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Column values of status are not correct",
                    eventJobsSearchPage.getColumnValuesOfStatus(),
                    everyItem(isOneOf(EventJobStatus.COMPLETED.getName(), EventJobStatus.STARTED.getName(),
                        EventJobStatus.STARTING.getName(), EventJobStatus.STOPPED.getName(),
                        EventJobStatus.STOPPING.getName(), EventJobStatus.FAILED.getName(),
                        EventJobStatus.ABANDONED.getName(), EventJobStatus.UNKNOWN.getName())),
                    assertionErrorList);
            }
            if (createdStartDate != null) {
                if (createdEndDate != null) {
                    AssertCollector.assertThat("Column values of created are less than start date",
                        eventJobsSearchPage.getColumnValuesOfCreatedDate(),
                        everyItem(greaterThanOrEqualTo(createdStartDate)), assertionErrorList);
                    AssertCollector.assertThat("Column values of created are greater than end date",
                        eventJobsSearchPage.getColumnValuesOfCreatedDate(),
                        everyItem(lessThanOrEqualTo(createdEndDate)), assertionErrorList);
                }
            }
        } else {
            LOGGER.info("No results found with selected filters");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test the data of search results with data on details page of event job status page.
     *
     * @param category
     */
    @Test(dataProvider = "dataToSelectCategory")
    public void testDataInSearchResults(final JobCategory category) {
        findEventJobsPage.findJobsThroughAdvancedFind(category, null, null, null);

        final EventJobsSearchResultsPage eventJobsSearchPage = findEventJobsPage.clickOnFindEventJobsButton();
        final int selectedIndex =
            eventJobsSearchPage.selectRowRandomlyFromFirstPage(eventJobsSearchPage.getTotalItems());
        final String jobIdOnSearchedPage = eventJobsSearchPage.getColumnValuesOfJobId().get(selectedIndex);
        final String categoryOnSearchedPage = eventJobsSearchPage.getColumnValuesOfCategory().get(selectedIndex);
        final String statusOnSearchedPage = eventJobsSearchPage.getColumnValuesOfStatus().get(selectedIndex);
        final String createdDateOnSearchedPage = eventJobsSearchPage.getColumnValuesOfCreatedDate().get(selectedIndex);
        final String lastModifiedDateOnSearchedPage =

            eventJobsSearchPage.getColumnValuesOfLastModifiedDate().get(selectedIndex);
        eventJobsSearchPage.selectResultRow(selectedIndex + 1);
        AssertCollector.assertThat("Id on details page is not same as shown in search page", eventJobStatusPage.getId(),
            equalTo(jobIdOnSearchedPage), assertionErrorList);
        AssertCollector.assertThat("Category on details page is not same as shown in search page",
            eventJobStatusPage.getCategory(), equalTo(categoryOnSearchedPage), assertionErrorList);
        AssertCollector.assertThat("Status on details page is not same as shown in search page",
            eventJobStatusPage.getStatus(), equalTo(statusOnSearchedPage), assertionErrorList);
        AssertCollector.assertThat("Created on details page is not same as shown in search page",
            eventJobStatusPage.getCreated(), equalTo(createdDateOnSearchedPage), assertionErrorList);
        AssertCollector.assertThat("LastModified on details page is not same as shown in search page",
            eventJobStatusPage.getLastModified(), equalTo(lastModifiedDateOnSearchedPage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the functionality of find job by id.
     */
    @Test
    public void testFindEventsJobById() {

        final String sqlQuery = "select* from batch_step_execution bse join batch_job_execution bje on "
            + "bse.JOB_EXECUTION_ID = bje.JOB_EXECUTION_ID "
            + "join batch_job_instance bji on bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
            + "order by bse.JOB_EXECUTION_ID desc limit 1";
        final List<Map<String, String>> resultList =
            DbUtils.selectQueryFromWorkerDb(sqlQuery, getEnvironmentVariables());
        final String jobId = resultList.get(0).get("JOB_EXECUTION_ID");

        final String created = (DateTimeUtils.changeDateFormat((resultList.get(0).get("START_TIME")),
            PelicanConstants.DATE_FORMAT_FROM_WORKERS_TABLE, PelicanConstants.DB_DATE_FORMAT));
        LOGGER.info(created);
        final String lastModified = (DateTimeUtils.changeDateFormat((resultList.get(0).get("LAST_UPDATED")),
            PelicanConstants.DATE_FORMAT_FROM_WORKERS_TABLE, PelicanConstants.DB_DATE_FORMAT));
        final String status = resultList.get(0).get(PelicanConstants.STATUS.toUpperCase());
        final String processedRecordsCount = resultList.get(0).get("READ_COUNT");
        final String category = resultList.get(0).get("JOB_NAME");
        final String rollBackRecordsCount = resultList.get(0).get("ROLLBACK_COUNT");
        final String stepCount = resultList.get(0).get("COMMIT_COUNT");
        final String skippedRecordsCount = resultList.get(0).get("WRITE_SKIP_COUNT");

        eventJobStatusPage = findEventJobsPage.findEventsByJobId(jobId);
        AssertCollector.assertThat("Title of the page is not correct", eventJobStatusPage.getTitle(),
            equalTo("Event Job Status"), assertionErrorList);
        AssertCollector.assertThat("Id is not correct on event job status page", eventJobStatusPage.getId(),
            equalTo(jobId), assertionErrorList);
        AssertCollector.assertThat("Created is not correct on event job status page", eventJobStatusPage.getCreated(),
            equalTo(created), assertionErrorList);
        AssertCollector.assertThat("LastModified is not correct on event job status page",
            eventJobStatusPage.getLastModified(), equalTo(lastModified), assertionErrorList);
        AssertCollector.assertThat("Category is not correct on event job status page", eventJobStatusPage.getCategory(),
            equalTo(category), assertionErrorList);
        AssertCollector.assertThat("Status is not correct on event job status page", eventJobStatusPage.getStatus(),
            equalTo(status), assertionErrorList);
        AssertCollector.assertThat("Batch size is not correct on event job status page",
            eventJobStatusPage.getBatchSize(), equalTo(BATCH_SIZE), assertionErrorList);
        AssertCollector.assertThat("Step count is not correct on event job status page",
            eventJobStatusPage.getStepCount(), equalTo(stepCount), assertionErrorList);
        AssertCollector.assertThat("Processed records count is not correct on event job status page",
            eventJobStatusPage.getProcessedRecordsCount(), equalTo(processedRecordsCount), assertionErrorList);
        AssertCollector.assertThat("Roll back  records count is not correct on event job status page",
            eventJobStatusPage.getRollbackRecordsCount(), equalTo(rollBackRecordsCount), assertionErrorList);
        AssertCollector.assertThat("Skipped records count is not correct on event job status page",
            eventJobStatusPage.getSkippedRecordsCount(), equalTo(skippedRecordsCount), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the errors of find job by id.
     */
    @Test(dataProvider = "invalidIds")
    public void testFindEventsJobByInvalidId(final String invalidJobId) {
        findEventJobsPage = findEventJobsPage.findEventsByJobIdForErrors(invalidJobId);
        AssertCollector.assertThat("Title of the page is not correct", findEventJobsPage.getTitle(),
            equalTo("Find Event Jobs"), assertionErrorList);
        AssertCollector.assertThat("Main error message is not generated", findEventJobsPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        if (invalidJobId.equals(" ")) {
            AssertCollector.assertThat("Required message is not generated", findEventJobsPage.getErrorMessageForField(),
                equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        } else {
            AssertCollector.assertThat("Must be a number message is not generated",
                findEventJobsPage.getErrorMessageForField(), equalTo(PelicanErrorConstants.NUMBER_ERROR_MESSAGE),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data required to select filters to get search page of events job
     *
     * @return Object[][]
     */
    @DataProvider(name = "testDataForFindEventsJob")
    private Object[][] getTestDataForFindEventJobsAdvancedFind() {
        return new Object[][] {

                { JobCategory.SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.COMPLETED, START_DATE,
                        END_DATE },
                { JobCategory.PURCHASE_ORDER_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.FAILED, null, null },

                { JobCategory.ENITITLEMENT_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.STOPPED, null, null },
                { JobCategory.SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.COMPLETED, null,
                        null },
                { JobCategory.BASIC_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.COMPLETED, null, null },
                { JobCategory.COM_SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB, EventJobStatus.FAILED, null, null },
                { JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB, EventJobStatus.STOPPED, null, null },
                { JobCategory.PURCHASE_ORDER_CHANGENOTIFICATION_REPUBLISH_JOB, EventJobStatus.COMPLETED, null, null },
                { JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB, null, null, null },
                { JobCategory.SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB, null, START_DATE, END_DATE },
                { JobCategory.BASIC_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB, EventJobStatus.COMPLETED, null, null },
                { JobCategory.STORE_CHANGENOTIFICATION_REPUBLISH_JOB, EventJobStatus.FAILED, START_DATE, END_DATE },
                { JobCategory.AUM_SUBSCRIPTION_CHANGE_NOTIFICATION_REPUBLISH_JOB, EventJobStatus.COMPLETED, null,
                        null },
                { JobCategory.CHANGE_NOTIFICATION_RECOVERY_JOB, EventJobStatus.COMPLETED, null, null } };
    }

    /**
     * Data required to select filter of category
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataToSelectCategory")
    private Object[][] getDataToSelectCategory() {
        return new Object[][] { { JobCategory.SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.PURCHASE_ORDER_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.ENITITLEMENT_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.BASIC_OFFERING_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.COM_SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB },
                { JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.PURCHASE_ORDER_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.BASIC_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.STORE_CHANGENOTIFICATION_REPUBLISH_JOB },
                { JobCategory.AUM_SUBSCRIPTION_CHANGE_NOTIFICATION_REPUBLISH_JOB },
                { JobCategory.CHANGE_NOTIFICATION_RECOVERY_JOB } };

    }

    /**
     * Data required for invalid id to find a job
     *
     * @return Object[][]
     */
    @DataProvider(name = "invalidIds")
    private Object[][] getTestDataForInvalidIds() {
        return new Object[][] { { " " }, { "abcf" }, { "}" } };
    }
}
