package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents Role assignment complete page This page is returned when role/roles are assigned to a user
 *
 * @author Shweta Hegde
 */
public class RoleAssignmentCompletePage extends GenericDetails {

    public RoleAssignmentCompletePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAssignmentCompletePage.class.getSimpleName());

    /**
     * This method returns named party of roles assigned
     *
     * @return named party
     */
    public String getNamedParty() {

        final String namedParty = getValueByField("Named Party");
        LOGGER.info("Named Party : " + namedParty);
        return namedParty;
    }

    /**
     * This method returns the application value for the roles assigned
     *
     * @return application value.
     */
    public String getApplication() {

        final String application = getValueByField("Application");
        LOGGER.info("Application : " + application);
        return application;
    }

    /**
     * This method returns roles assigned to the user
     */
    public String getRoles() {

        final String roles = getValueByField("Roles");
        LOGGER.info("Roles : " + roles);
        return roles;
    }
}
