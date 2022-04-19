package com.autodesk.bsm.pelican.api.triggers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiHealthCheck;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This is a test class which will test triggers health check api
 *
 * @author Rohini
 */

public class GetHealthCheckTest extends BaseTestData {

    private PelicanPlatform resource;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetHealthCheckTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * Test method is to test the triggers health check api without any parameters
     */

    @Test
    public void getHealthCheckStatus() throws ParseException {

        final JsonApiHealthCheck jsonApiHealthCheck = resource.healthCheckStatusResource().getHealthCheckStatus();

        LOGGER.info("TempestDB status is: " + jsonApiHealthCheck.getData().getJsonApiHealthCheckAttributes()
            .getJsonApiHealthCheckDetails().getTempestDB());

        AssertCollector.assertThat("Incorrect JSON Api version", jsonApiHealthCheck.getJsonApi().getVersion(),
            equalTo("1.0"), assertionErrorList);
        AssertCollector.assertThat("Incorrect type", jsonApiHealthCheck.getData().getType(), equalTo("healthChecks"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Id", jsonApiHealthCheck.getData().getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Created date is not correct",
            jsonApiHealthCheck.getData().getJsonApiHealthCheckAttributes().getCreated(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("System is not working properly",
            jsonApiHealthCheck.getData().getJsonApiHealthCheckAttributes().getSystem(), equalTo("up"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect database status", jsonApiHealthCheck.getData()
            .getJsonApiHealthCheckAttributes().getJsonApiHealthCheckDetails().getDatabases(), equalTo("up"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect queues status",
            jsonApiHealthCheck.getData().getJsonApiHealthCheckAttributes().getJsonApiHealthCheckDetails().getQueues(),
            equalTo("up"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of DB", jsonApiHealthCheck.getData()
            .getJsonApiHealthCheckAttributes().getJsonApiHealthCheckDetails().getTempestDB(), equalTo("up"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
