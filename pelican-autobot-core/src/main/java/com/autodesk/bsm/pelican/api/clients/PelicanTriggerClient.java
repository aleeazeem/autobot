package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

/**
 * Access to Pelican's trigger api
 *
 * @author yin
 */
public class PelicanTriggerClient {

    private JobsClient jobsResource;
    private JobStatusesClient jobStatusesResource;

    public PelicanTriggerClient(final EnvironmentVariables environmentVariables) {

        jobsResource = new JobsClient(environmentVariables.getTriggerUrl(), environmentVariables);
        jobStatusesResource = new JobStatusesClient(environmentVariables.getTriggerUrl());

    }

    public JobsClient jobs() {
        return jobsResource;
    }

    public JobStatusesClient jobStatuses() {
        return jobStatusesResource;
    }
}
