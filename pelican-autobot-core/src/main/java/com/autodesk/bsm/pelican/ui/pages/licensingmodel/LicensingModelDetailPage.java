package com.autodesk.bsm.pelican.ui.pages.licensingmodel;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is POM representation of Licensing Model detail page
 *
 * @author mandas
 */
public class LicensingModelDetailPage extends GenericDetails {

    public LicensingModelDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LicensingModelDetailPage.class.getSimpleName());

    /**
     * This method returns id of a License Model
     *
     * @return id
     */
    public String getId() {

        final String id = getValueByField("ID");
        LOGGER.info("License Model id : " + id);
        return id;
    }

    /**
     * This method returns name of a License Model
     *
     * @return Name
     */
    public String getName() {

        final String name = getValueByField("Name");
        LOGGER.info("License Model Name : " + name);
        return name;
    }

    /**
     * This method returns external key of a License Model
     *
     * @return externalKey
     */
    public String getExternalKey() {

        final String externalKey = getValueByField("External Key");
        LOGGER.info("License Model external key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns description of a License Model
     *
     * @return Description
     */
    public String getDescription() {

        final String description = getValueByField("Description");
        LOGGER.info("License Model Description : " + description);
        return description;
    }

    /**
     * This method returns "Tied to Subscription Lifecycle" of a License Model
     *
     * @return subscriptionLifecycle
     */
    public String getSubscriptionLifecycle() {

        final String subscriptionLifecycle = getValueByField("Tied to Subscription Lifecycle");
        LOGGER.info("License Model - Tied to Subscription Lifecycle : " + subscriptionLifecycle);
        return subscriptionLifecycle;
    }

    /**
     * This method returns "For Finite Time" of a License Model
     *
     * @return finiteTime
     */
    public String getForFiniteTime() {

        final String finiteTime = getValueByField("For Finite Time");
        LOGGER.info("License Model - Finite Time : " + finiteTime);
        return finiteTime;
    }

    /**
     * This method clicks on edit button
     *
     * @return AddEditLicensingModelPage
     */
    public EditLicensingModelPage clickOnEdit() {

        editButton.click();
        LOGGER.info("Click on 'Edit' to edit Licensing Model");
        return super.getPage(EditLicensingModelPage.class);
    }

}
