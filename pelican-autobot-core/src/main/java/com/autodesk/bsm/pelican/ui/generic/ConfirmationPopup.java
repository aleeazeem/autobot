package com.autodesk.bsm.pelican.ui.generic;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmationPopup extends GenericDetails {

    public ConfirmationPopup(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(className = "popup-dialog")
    private WebElement popupContainer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationPopup.class.getSimpleName());

    /**
     * Remove anything in the span
     */
    public String getMessage() {
        final String message = popupContainer.getText();
        return message.split("\n")[0];
    }

    public void confirm() {
        LOGGER.info("Click confirm");
        getActions().click(confirmButton);
    }

    public void cancel() {
        LOGGER.info("Click cancel");
        getActions().click(cancelPopUpButton);

    }

    /**
     * Method to check if popup container exists
     *
     * @return true or false based on condition
     */
    public Boolean isPopUpContainerExists() {
        return doesWebElementExist(popupContainer);
    }
}
