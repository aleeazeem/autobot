package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents edit user page
 *
 * @author t_mohag
 */
public class EditUserPage extends GenericDetails {

    public EditUserPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//*[@id='field-id']/div")
    private WebElement id;

    @FindBy(css = "#form-editUser > div.buttons > span.button.cancel-btn > a")
    private WebElement cancelButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditUserPage.class.getSimpleName());

    /**
     * set any name for User
     */
    public void setName(final String name) {
        LOGGER.info("Set name to '" + name + "'");
        getActions().setText(nameInput, name);
    }

    /**
     * Click on the save button of the edit user page
     *
     * @return UserDetailsPage
     */
    public UserDetailsPage clickOnSave() {
        submit(TimeConstants.ONE_SEC);
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * Click on the cancel button of the user edit page
     */
    public void clickOnCancel() {
        cancelButton.click();
    }
}
