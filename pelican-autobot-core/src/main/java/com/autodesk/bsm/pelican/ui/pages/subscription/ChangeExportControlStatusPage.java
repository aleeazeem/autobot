package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for Subscription Detail Page in Pelican admin tool. This page can be viewed in AT from
 * Subscriptions tab / User >Find Subscriptions-->subscription search results --> subscription detail
 *
 * @author Muhammad
 */
public class ChangeExportControlStatusPage extends GenericDetails {

    public ChangeExportControlStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "exportControlStatus")
    private WebElement exportControlStatusSelect;

    @FindBy(id = "input-note")
    private WebElement noteInput;

    @FindBy(xpath = ".//*[@id='form-changeECStatusForm']/div[1]/div")
    private WebElement currentEcStatus;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeExportControlStatusPage.class.getSimpleName());

    /**
     * Method to change ec status and add notes
     */
    public void helperToChangeEcStatus(final ECStatus ecStatus, final String text) {
        getActions().select(exportControlStatusSelect, ecStatus.getName());
        LOGGER.info("EC status is set to: " + ecStatus);
        getActions().setText(noteInput, text);
        LOGGER.info("Notes are entered");
    }

    public String getCurrentEcStatus() {
        return currentEcStatus.getText();
    }
}
