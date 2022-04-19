package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.UserStatus;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object Pattern represents AddUser through AdminTool under User.
 *
 * @author Muhammad Azeem
 */
public class AddUserPage extends GenericGrid {

    public AddUserPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(id = "state")
    private WebElement statusSelect;

    @FindBy(css = ".errors")
    private WebElement allErrorMessages;

    @FindBy(css = ".error-message")
    private WebElement requiredErrorMessage;

    @FindBy(id = "input-nameOrId")
    private WebElement idOrNameInput;

    @FindBy(id = "input-username")
    private WebElement userNameInput;

    @FindBy(id = "input-email")
    private WebElement emailInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddUserPage.class.getSimpleName());

    /**
     * set any name for User
     */
    protected void setName(final String name) {
        LOGGER.info("Set name to '" + name + "'");
        getActions().setText(nameInput, name);
    }

    /**
     * set any status for User
     */
    private void selectStatus(final String status) {
        LOGGER.info("Set status to '" + status + "'");
        getActions().select(statusSelect, status);
    }

    /**
     * get required error message
     *
     * @return errorMessage
     */
    public String getRequiredErrorMessage() {
        final String requiredErrorMessage = this.requiredErrorMessage.getText();
        LOGGER.info("'" + requiredErrorMessage + "'");
        return requiredErrorMessage;
    }

    /**
     * get main error message - If any error is generated than the main Error on top of the fields will be generated
     *
     * @return errorMessage
     */
    public String getAllErrorMessage() {
        final String errorMessage = this.allErrorMessages.getText();
        LOGGER.info("'" + errorMessage + "'");
        return errorMessage;
    }

    private void navigateToAddUser() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.USER.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Click on the save button of the user which is inherited from Generic Page This method is written so that any test
     * class can use this
     *
     * @return UserDetailsPage
     */
    public UserDetailsPage clickOnSave() {

        submit(TimeConstants.ONE_SEC);
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * This method adds new user
     */
    public void addUser(final String name, final String externalKey, final UserStatus userState) {

        navigateToAddUser();
        LOGGER.info("Add User Details");
        setName(name);
        setExternalKey(externalKey);
        selectStatus(userState.toString());
    }

    /**
     * Click on the save button of the user which is inherited from Generic Page This method is written so that any test
     * class can use this
     *
     * @return AddUserPage
     */
    public AddUserPage clickOnSaveForError() {

        submit(TimeConstants.ONE_SEC);
        return super.getPage(AddUserPage.class);
    }

    /**
     * Click on the cancel button of the user which is inherited from Generic Page This method is written so that any
     * test class can use this
     */
    public void clickOnCancel() {

        cancel();
    }
}
