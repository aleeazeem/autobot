package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Objects represents Events Jobs page.
 *
 * @author mandas
 */
public class RepublishJobsPage extends GenericDetails {

    public RepublishJobsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//button[text()='Force Fail']")
    private WebElement forceFailButton;

    @FindBy(xpath = "//button[text()='Abort']")
    private WebElement abortButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(RepublishJobsPage.class.getSimpleName());

    /**
     * Navigate to the Events Jobs page in Admin Tool page
     */
    public void navigateToEventsJobPage(final String jobId) {

        final String URL = "event/job/show?id=";
        final String url = getEnvironment().getAdminUrl() + "/" + URL + jobId;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Click on Abort Button from Event Job status page
     */
    public void clickAbort() {
        abortButton.submit();
        LOGGER.info("Clicked on Abort Button");
    }

    /**
     * Click on ForceFail Button from Event Job status page
     */
    public void clickForceFail() {
        forceFailButton.submit();
        LOGGER.info("Clicked on ForceFail Button");
    }

    /**
     * Get ID value from Event Job status page
     *
     * @return String
     */
    public String getId() {
        return getValueByField(PelicanConstants.ID.toUpperCase());
    }

    /**
     * Get Category value from Event Job status page
     *
     * @return String
     */
    public String getCategory() {
        getDriver().navigate().refresh();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return getValueByField(PelicanConstants.CATEGORY);
    }

    /**
     * Get Status value from Event Job status page
     *
     * @return String
     */
    public String getStatus() {
        getDriver().navigate().refresh();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return getValueByField(PelicanConstants.STATUS_FIELD);
    }

    /**
     * Get Processed Events count value from Event Job status page
     *
     * @return String
     */
    public String getProcessedEventsCount() {
        getDriver().navigate().refresh();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return getValueByField(PelicanConstants.PROCESSED_RECORDS_COUNT);
    }

    /**
     * Get Step Count value from Event Job status page
     *
     * @return String
     */
    public String getStepCount() {
        getDriver().navigate().refresh();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return getValueByField(PelicanConstants.STEP_COUNT);
    }

}
