package com.autodesk.bsm.pelican.api.triggers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobStatusesClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusesData;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.http.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This is a test class which will test the get job statuses api in triggers
 *
 * @author vineel
 */

public class GetJobStatusesTest extends BaseTestData {
    private PelicanTriggerClient triggerResource;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
    }

    /**
     * Test method to test the triggers get job statuses api without any parameters
     *
     * @result Non null value job status id
     */
    @Test
    public void getJobStatusesWithoutParameters() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        for (final JsonApiJobStatus jobStatus : jobStatusesList) {
            AssertCollector.assertThat("Incorrect response of the get job statuses api", jobStatus.getId(),
                notNullValue(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get job status api with job category
     *
     * @result Job Category should be Subscription Renewals
     */
    @Test
    public void getJobStatusesWithJobCategory() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response =
            jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS", null, null, null);
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        for (final JsonApiJobStatus jobStatus : jobStatusesList) {
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobCategory().toString(), equalTo(JobCategory.SUBSCRIPTION_RENEWALS.toString()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the get job statuses api with job category and job state as parameters
     *
     * @result Job State should be Failed
     */
    @Test
    public void getJobStatusesWithJobstate() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response =
            jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS", Status.FAILED.toString(), null, null);
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        for (final JsonApiJobStatus jobStatus : jobStatusesList) {
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobCategory().toString(), equalTo(JobCategory.SUBSCRIPTION_RENEWALS.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobState().toString(), equalTo(Status.FAILED.toString()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method which test the get job statuses api with created start date
     *
     * @result created start date should not be null value
     */
    @Test
    public void getJobStatusesWithCreatedStartDate() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS",
            Status.FAILED.toString(), "03%2F22%2F2016+14%3A14%3A30", null);
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        for (final JsonApiJobStatus jobStatus : jobStatusesList) {
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobCategory().toString(), equalTo(JobCategory.SUBSCRIPTION_RENEWALS.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobState().toString(), equalTo(Status.FAILED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect response of the get job statuses api", jobStatus.getCreated(),
                notNullValue(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method which will test the get job statuses api with created end date
     *
     * @result created end date should not be null
     */
    @Test
    public void getJobStatusesWithCreatedEndDate() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS",
            Status.FAILED.toString(), "03%2F22%2F2015+14%3A14%3A30", "03%2F22%2F2016+14%3A14%3A30");
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        for (final JsonApiJobStatus jobStatus : jobStatusesList) {
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobCategory().toString(), equalTo(JobCategory.SUBSCRIPTION_RENEWALS.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect response of the get job statuses api",
                jobStatus.getJobState().toString(), equalTo(Status.FAILED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect response of the get job statuses api", jobStatus.getCreated(),
                notNullValue(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method which test the get job statuses api with invalid job category
     *
     * @result response should be Invalid Job Category
     */
    @Test
    public void getJobStatusesWithInvalidJobCategory() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses("abcd", null, null, null);
        final List<Errors> errors = response.getErrors();
        AssertCollector.assertThat("Incorrect job category response of the get job statuses api",
            errors.get(0).getTitle(), equalTo("Invalid Job Category"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * A test method which will test the get job statuses api with invalid job state
     *
     * @result @result response should be Invalid Job State
     */
    @Test
    public void getJobStatusesWithInvalidJobState() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response =
            jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS", "abcd", null, null);
        final List<Errors> errors = response.getErrors();
        AssertCollector.assertThat("Incorrect job state response of the get job statuses api", errors.get(0).getTitle(),
            equalTo("Invalid Job State"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * A test method which will test the get job statuses api with invalid created start date
     */
    @Test
    public void getJobStatusesWithInvalidCreatedDate() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response =
            jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS", Status.FAILED.toString(), "abcd", null);
        final List<Errors> errors = response.getErrors();
        AssertCollector.assertThat("Incorrect created start date response of the get job statuses api",
            errors.get(0).getTitle(),
            equalTo("Invalid createdDateStart. Date must be in 'MM/dd/yyyy HH:mm:ss' format."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * A test method which will test the get job statuses api with invalid end date
     */
    @Test
    public void getJobStatusesWithInvalidEndDate() throws ParseException {

        // Run the Get Job Statuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses("SUBSCRIPTION_RENEWALS",
            Status.FAILED.toString(), "03%2F22%2F2015+14%3A14%3A30", "abcd");
        final List<Errors> errors = response.getErrors();
        AssertCollector.assertThat("Incorrect created end date response of the get job statuses api",
            errors.get(0).getTitle(), equalTo("Invalid createdDateEnd. Date must be in 'MM/dd/yyyy HH:mm:ss' format."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
