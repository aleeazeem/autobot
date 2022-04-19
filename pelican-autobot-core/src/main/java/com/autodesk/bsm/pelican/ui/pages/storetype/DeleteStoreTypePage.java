package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the Delete Store Type Page
 *
 * @author vineel
 */

public class DeleteStoreTypePage extends GenericDetails {

    public DeleteStoreTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteStoreTypePage.class.getSimpleName());

    /**
     * This method returns application family of a store type
     *
     * @return application family name
     */
    public String getApplicationFamily() {
        final String appFamily = getValueByField("Application Family");
        LOGGER.info("Application Family name : " + appFamily);
        return appFamily;
    }

    /**
     * This method returns id of a store type
     *
     * @return id of a store type
     */
    public String getId() {
        final String id = getValueByField("ID");
        LOGGER.info("ID : " + id);
        return id;
    }

    /**
     * This method returns external key of a store type
     *
     * @return external key of a store type
     */
    public String getExternalKey() {
        final String externalKey = getValueByField("External Key");
        LOGGER.info("External Key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns name of a store type
     *
     * @return Name of a store type
     */
    public String getName() {
        final String name = getValueByField("Name");
        LOGGER.info("Name : " + name);
        return name;
    }

    public String getCreated() {
        final String created = getValueByField("Created");
        LOGGER.info("Created :" + created);
        return created;
    }

}
