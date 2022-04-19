package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.JobStatusesReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This is a test class which will test the job statuses report in admin tool
 *
 * @author vineel
 */
public class JobStatusesReportTest extends SeleniumWebdriver {

    private JobStatusesReportPage jobStatusesReport;
    private JobsClient jobsResource;
    private PurchaseOrderUtils purchaseOrderUtils;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        jobStatusesReport = adminToolPage.getPage(JobStatusesReportPage.class);

        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /*
     * This is a test method which will test the job statuses report headers in admin tool
     */
    @Test
    public void testJobStatusesReportHeaders() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();
        AssertCollector.assertThat("Title of the Page is not Correct", jobStatusesReportGrid.getDriver().getTitle(),
            equalTo("Pelican - Job Status Report"), assertionErrorList);
        AssertCollector.assertThat("Total number of columns are not correct",
            jobStatusesReportGrid.getColumnHeaders().size(), equalTo(5), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", jobStatusesReportGrid.getColumnHeaders().get(0),
            equalTo("Job GUID"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", jobStatusesReportGrid.getColumnHeaders().get(1),
            equalTo("Category"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", jobStatusesReportGrid.getColumnHeaders().get(2),
            equalTo("State"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", jobStatusesReportGrid.getColumnHeaders().get(3),
            equalTo("Created Date"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", jobStatusesReportGrid.getColumnHeaders().get(4),
            equalTo("Last Modified Date"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the job statuses report without parameters
     */
    @Test
    public void testJobStatusesReportWithoutParameters() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();
        final List<String> jobGuidList = jobStatusesReportGrid.getColumnValues("Job GUID");
        if (jobGuidList.size() > 0) {
            for (final String guid : jobGuidList) {
                AssertCollector.assertThat("Incorrect job guid value of a report", guid, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> jobCategoryList = jobStatusesReportGrid.getColumnValues("Category");
        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> jobStateList = jobStatusesReportGrid.getColumnValues("State");
        if (jobStateList.size() > 0) {
            for (final String jobState : jobStateList) {
                AssertCollector.assertThat("Incorrect job state value of a report", jobState, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> startDateList = jobStatusesReportGrid.getColumnValues("Created Date");
        if (startDateList.size() > 0) {
            for (final String startDate : startDateList) {
                AssertCollector.assertThat("Incorrect created start date value of a report", startDate, notNullValue(),
                    assertionErrorList);
            }
        }

        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory, notNullValue(),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the job statuses report with Job Category
     */
    @Test
    public void testJobStatusesReportWithJobCategory() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory("SUBSCRIPTION_RENEWALS");
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();

        final List<String> jobGuidList = jobStatusesReportGrid.getColumnValues("Job GUID");
        if (jobGuidList.size() > 0) {
            for (final String guid : jobGuidList) {
                AssertCollector.assertThat("Incorrect job guid value of a report", guid, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> jobCategoryList = jobStatusesReportGrid.getColumnValues("Category");
        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory,
                    equalTo("SUBSCRIPTION_RENEWALS"), assertionErrorList);
            }
        }

        final List<String> jobStateList = jobStatusesReportGrid.getColumnValues("State");
        if (jobStateList.size() > 0) {
            for (final String jobState : jobStateList) {
                AssertCollector.assertThat("Incorrect job state value of a report", jobState, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> startDateList = jobStatusesReportGrid.getColumnValues("Created Date");
        if (startDateList.size() > 0) {
            for (final String startDate : startDateList) {
                AssertCollector.assertThat("Incorrect created start date value of a report", startDate, notNullValue(),
                    assertionErrorList);
            }
        }

        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory, notNullValue(),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the job statuses report with Job Category and job state
     */
    @Test(dataProvider = "JobState")
    public void testJobStatusesReportWithJobState(final String state) {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory("SUBSCRIPTION_RENEWALS");
        jobStatusesReport.selectJobState(state);
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();

        final List<String> jobGuidList = jobStatusesReportGrid.getColumnValues("Job GUID");
        if (jobGuidList.size() > 0) {
            for (final String guid : jobGuidList) {
                AssertCollector.assertThat("Incorrect job guid value of a report", guid, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> jobCategoryList = jobStatusesReportGrid.getColumnValues("Category");
        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory,
                    equalTo("SUBSCRIPTION_RENEWALS"), assertionErrorList);
            }
        }

        final List<String> jobStateList = jobStatusesReportGrid.getColumnValues("State");
        if (jobStateList.size() > 0) {
            for (final String jobState : jobStateList) {
                AssertCollector.assertThat("Incorrect job state value of a report", jobState, equalTo(state),
                    assertionErrorList);
            }
        }

        final List<String> startDateList = jobStatusesReportGrid.getColumnValues("Created Date");
        if (startDateList.size() > 0) {
            for (final String startDate : startDateList) {
                AssertCollector.assertThat("Incorrect created start date value of a report", startDate, notNullValue(),
                    assertionErrorList);
            }
        }

        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory, notNullValue(),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the job statuses report with Job Category, Job State, Created Start Date
     * and End date
     */
    @Test
    public void testJobStatusesReportWithStartAndEndDates() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory("SUBSCRIPTION_RENEWALS");
        jobStatusesReport.selectJobState("COMPLETE_WITH_FAILURES");
        jobStatusesReport.fillStartDate("01/01/2015");
        jobStatusesReport.fillEndDate("01/01/2026");
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();

        final List<String> jobGuidList = jobStatusesReportGrid.getColumnValues("Job GUID");
        if (jobGuidList.size() > 0) {
            for (final String guid : jobGuidList) {
                AssertCollector.assertThat("Incorrect job guid value of a report", guid, notNullValue(),
                    assertionErrorList);
            }
        }

        final List<String> jobCategoryList = jobStatusesReportGrid.getColumnValues("Category");
        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory,
                    equalTo("SUBSCRIPTION_RENEWALS"), assertionErrorList);
            }
        }

        final List<String> jobStateList = jobStatusesReportGrid.getColumnValues("State");
        if (jobStateList.size() > 0) {
            for (final String jobState : jobStateList) {
                AssertCollector.assertThat("Incorrect job state value of a report", jobState,
                    equalTo("COMPLETE_WITH_FAILURES"), assertionErrorList);
            }
        }

        final List<String> startDateList = jobStatusesReportGrid.getColumnValues("Created Date");
        if (startDateList.size() > 0) {
            for (final String startDate : startDateList) {
                AssertCollector.assertThat("Incorrect created start date value of a report", startDate, notNullValue(),
                    assertionErrorList);
            }
        }

        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory, notNullValue(),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the error message with invalid start date and end date
     */
    @Test
    public void testJobStatusesReportErrorMessageWithInvalidStartDateAndEndDate() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory("SUBSCRIPTION_RENEWALS");
        jobStatusesReport.selectJobState("COMPLETE_WITH_FAILURES");
        jobStatusesReport.fillStartDate("abcd");
        jobStatusesReport.fillEndDate("abcd");
        jobStatusesReport.submit(TimeConstants.ONE_SEC);
        final String errorMessage = jobStatusesReport.getErrorMessageForField();
        AssertCollector.assertThat("Incorrect error message of the date field", errorMessage,
            equalTo("Start Date must be in 'MM/dd/yyyy' format."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the error message with start date filled and end
     */
    @Test
    public void testJobStatusesReportErrorMessageWithStartDateFilledAndEndDateEmpty() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory("SUBSCRIPTION_RENEWALS");
        jobStatusesReport.selectJobState("COMPLETE_WITH_FAILURES");
        jobStatusesReport.fillStartDate("01/01/2015");
        jobStatusesReport.submit(TimeConstants.ONE_SEC);
        final String errorMessage = jobStatusesReport.getErrorMessageForField();
        AssertCollector.assertThat("Incorrect error message of the date field", errorMessage,
            equalTo("End Date cannot be blank."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate Pending Orders SFDC case Job Status Report.
     */
    @Test
    public void testSuccessPendingOrderSFDCCaseJobStatusReport() {

        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), getBuyerUser(), 1);

        final String purchaseOrderId = purchaseOrder.getId();

        // process the purchase order to 'Pending' state
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);

        // Database query to change last modified date in past.
        purchaseOrderUtils.updateTransactionDate(purchaseOrderId,
            DateTimeUtils.getCurrentTimeMinusSpecifiedHours(PelicanConstants.DB_DATE_FORMAT, 15));

        // Run Pending PO SFDC job
        jobsResource.pendingPurchaseOrder();

        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory(JobCategory.MONITORING_PENDING_PURCHASE_ORDER_JOB.toString());
        jobStatusesReport.selectJobState(Status.COMPLETE.getDisplayName());
        jobStatusesReport.fillStartDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        jobStatusesReport.fillEndDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        final GenericGrid jobStatusReportGrid = jobStatusesReport.clickOnSubmit();
        final List<String> jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        if (jobGuidList.size() > 0) {
            for (final String jobGuid : jobGuidList) {
                AssertCollector.assertThat("Job GUID should not be Null", jobGuid, notNullValue(), assertionErrorList);
            }
            final List<String> categoryList = jobStatusReportGrid.getColumnValues(Status.COMPLETE.getDisplayName());
            for (final String category : categoryList) {
                AssertCollector.assertThat("Incorrect Category", category,
                    equalTo(JobCategory.MONITORING_PENDING_PURCHASE_ORDER_JOB.toString()), assertionErrorList);
            }
            final List<String> stateList = jobStatusReportGrid.getColumnValues(PelicanConstants.STATE);
            for (final String state : stateList) {
                AssertCollector.assertThat("Incorrect Job State", state, equalTo(Status.COMPLETE.toString()),
                    assertionErrorList);
            }
            final List<String> createDateList = jobStatusReportGrid.getColumnValues(PelicanConstants.CREATED_DATE);
            for (final String createDate : createDateList) {
                AssertCollector.assertThat("Incorrect Job Created Date", createDate.split(" ")[0],
                    equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
            }
            final List<String> lastModifiedDateList =
                jobStatusReportGrid.getColumnValues(PelicanConstants.LAST_MODIFIED_DATE_FIELD);

            for (final String lastModifiedDate : lastModifiedDateList) {
                AssertCollector.assertThat("Incorrect Jobs Last Modified Date", lastModifiedDate.split(" ")[0],
                    equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
            }
        } else {
            AssertCollector.assertTrue("Report has no Data to validate.", jobGuidList.size() > 0, assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider which provides the different job states for the job status report in the admin tool
     *
     * @return two dimensional object array containing job statuses
     */
    @DataProvider(name = "JobState")
    public Object[][] getDifferentJobStates() {
        return new Object[][] { { "IN_PROGRESS" }, { "COMPLETE" }, { "COMPLETE_WITH_FAILURES" } };
    }

}
