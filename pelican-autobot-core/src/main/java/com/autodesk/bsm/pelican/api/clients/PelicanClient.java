package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

/**
 * Central access to rest
 *
 * @author yin
 */
public class PelicanClient {

    private final PelicanPlatform pelicanPlatform;
    private final PelicanTriggerClient trigger;

    public PelicanClient(final EnvironmentVariables environmentVariables) {
        pelicanPlatform = new PelicanPlatform(environmentVariables, environmentVariables.getAppFamily());
        trigger = new PelicanTriggerClient(environmentVariables);
    }

    public PelicanClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        pelicanPlatform = new PelicanPlatform(environmentVariables, appFamily);
        trigger = new PelicanTriggerClient(environmentVariables);
    }

    public PelicanPlatform platform() {
        return pelicanPlatform;
    }

    public PelicanTriggerClient trigger() {
        return trigger;
    }
}
