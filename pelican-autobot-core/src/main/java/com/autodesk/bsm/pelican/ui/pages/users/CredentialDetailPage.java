package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents the Credential Detail Page This shows password/secret details.
 *
 * @author Shweta Hegde
 */
public class CredentialDetailPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialDetailPage.class.getSimpleName());

    public CredentialDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method returns the id of the credential
     *
     * @return id
     */
    public String getId() {

        final String id = getValueByField("ID");
        LOGGER.info("Credential id : " + id);
        return id;
    }

    /**
     * This method returns the credential type either password or secret
     *
     * @return type
     */
    public String getType() {

        final String type = getValueByField("Type");
        LOGGER.info("Credential type : " + type);
        return type;
    }

    /**
     * This method returns the created date of the password
     *
     * @return created
     */
    public String getCreated() {

        final String created = getValueByField("Created");
        LOGGER.info("Created : " + created);
        return created;
    }
}
