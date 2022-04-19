package com.autodesk.bsm.pelican.ui.pages.applications;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is used to edit Application Family details
 *
 * @author Shweta Hegde
 */
public class ApplicationFamilyEditPage extends GenericDetails {

    public ApplicationFamilyEditPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-allowLocalAuth")
    private WebElement allowLocalAuthenticationCheckBox;

    @FindBy(className = "button")
    private WebElement cancelButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFamilyEditPage.class.getSimpleName());

    /**
     * This methods select 'allow local authentication' checkbox, if not selected
     */
    public void enableLocalAuthentication() {

        if (!allowLocalAuthenticationCheckBox.isSelected()) {
            allowLocalAuthenticationCheckBox.click();
            LOGGER.info("Selected the allow local authentication checkbox");
        }
    }

    /**
     * This methods deselect 'allow local authentication' checkbox, if selected
     */
    public void disableLocalAuthentication() {

        if (allowLocalAuthenticationCheckBox.isSelected()) {
            allowLocalAuthenticationCheckBox.click();
            LOGGER.info("Deselected the allow local authentication checkbox");
        }
    }

    /**
     * This method saves the edited application family
     */
    public void updateApplicationFamily() {

        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Updated the application family details");
    }
}
