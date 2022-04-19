package com.autodesk.bsm.pelican.api.triggers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobStatusesClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusesData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiWip;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiWipData;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.http.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This is a test class which will test the get WIP Api in triggers
 *
 * @author vineel
 */

public class GetWIPTest extends BaseTestData {

    private PelicanTriggerClient triggerResource;
    private String jobStatusId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
    }

    /**
     * Test method to test the triggers get WIP api without any parameters
     *
     * @result Non-null value wip id
     */
    @Test
    public void getWipWithoutParameters() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse = jobsStatusesResource.getWipsById(jobStatusId);
        final List<JsonApiWip> wipList = wipDataResponse.getWips();
        if (wipList.size() > 0) {
            for (final JsonApiWip wip : wipList) {
                AssertCollector.assertThat("Incorrect api response of the get wip api", wip.getId(), notNullValue(),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with WIP State
     *
     * @result Wip State should be failed
     */
    @Test
    public void getWIPWithWipState() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse =
            jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.toString(), null, null);
        final List<JsonApiWip> wipList = wipDataResponse.getWips();
        if (wipList.size() > 0) {
            for (final JsonApiWip wip : wipList) {
                AssertCollector.assertThat("Incorrect api response of the get wip api", wip.getId(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect api response for the get wips api", wip.getWipState().toString(),
                    equalTo(Status.FAILED.toString()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with WIP State, object type
     *
     * @result Object type should be subscription migration
     */
    @Test
    public void getWIPWithObjectType() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse =
            jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.toString(), "SUBSCRIPTION_DR_MIGRATION", null);
        final List<JsonApiWip> wipList = wipDataResponse.getWips();
        if (wipList.size() > 0) {
            for (final JsonApiWip wip : wipList) {
                AssertCollector.assertThat("Incorrect api response of the get wip api", wip.getId(), notNullValue(),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect api response for the get wips api", wip.getWipState().toString(),
                    equalTo(Status.FAILED.toString()), assertionErrorList);
                AssertCollector.assertThat("Incorrect api response for the get wips api", wip.getObjectType(),
                    equalTo("subscription migration"), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with WIP State, object type and object id
     *
     * @result Non-null object id
     */
    @Test
    public void getWIPWithObjectId() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse =
            jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.toString(), null, null);
        final List<JsonApiWip> wipList = wipDataResponse.getWips();
        if (wipList.size() > 0) {
            final String objectId = wipList.get(0).getObjectId();
            final JsonApiWipData wipNewDataResponse =
                jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.toString(), null, objectId);
            AssertCollector.assertThat("Incorrect api response of the get wip api",
                wipNewDataResponse.getWips().get(0).getId(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect api response for the get wips api",
                wipNewDataResponse.getWips().get(0).getObjectId(), equalTo(objectId), assertionErrorList);
            AssertCollector.assertThat("Incorrect api response for the get wips api",
                wipNewDataResponse.getWips().get(0).getWipState().toString(), equalTo(Status.FAILED.toString()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with Invalid WIP State
     *
     * @result Error title should be Invalid Wip State
     */
    @Test
    public void getWIPWithInvalidWipState() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse = jobsStatusesResource.getWipsById(jobStatusId, "abcd", null, null);
        final List<Errors> errorsList = wipDataResponse.getErrors();
        if (errorsList.size() > 0) {
            for (final Errors error : errorsList) {
                AssertCollector.assertThat("Incorrect wip state api response of the get wip api", error.getTitle(),
                    equalTo("Invalid Wip State"), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with Invalid Object Type
     *
     * @result Error title should be Invalid Object Type
     */
    @Test
    public void getWIPWithInvalidObjectType() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        // Run the Get WIP Api
        final JsonApiWipData wipDataResponse =
            jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.getDisplayName(), "abcd", null);
        final List<Errors> errorsList = wipDataResponse.getErrors();
        if (errorsList.size() > 0) {
            for (final Errors error : errorsList) {
                AssertCollector.assertThat("Incorrect wip object type api response of the get wip api",
                    error.getTitle(), equalTo("Invalid Object Type"), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to test the triggers get WIP Api with Invalid Object Id.
     *
     * @result Response should be empty with invalid object id.
     */
    @Test
    public void getWIPWithInvalidObjectId() throws ParseException {

        // Run the Get JobStatuses Api
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        final JsonApiJobStatusesData response = jobsStatusesResource.getJobStatuses();
        final List<JsonApiJobStatus> jobStatusesList = response.getData();
        jobStatusId = jobStatusesList.get(0).getId();

        final JsonApiWipData wipDataResponse =
            jobsStatusesResource.getWipsById(jobStatusId, Status.FAILED.getDisplayName(), null, "abcd");
        final List<JsonApiWip> wipList = wipDataResponse.getWips();

        AssertCollector.assertThat("There should not be any wip.", wipList.size(), equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
