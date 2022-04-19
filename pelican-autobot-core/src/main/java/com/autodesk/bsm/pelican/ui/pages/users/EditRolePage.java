package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This is a page class for editing a role in admin tool
 *
 * @author vineel
 */
public class EditRolePage extends GenericDetails {

    public EditRolePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-description")
    private WebElement roleDescriptionInput;

    @FindBy(xpath = ".//*[@class='submit']")
    private WebElement updateButton;

    /**
     * This method will edit the role name and description
     */
    public void editRole(final String name, final String description) {
        editRoleName(name);
        editRoleDescription(description);
        clickOnUpdateButton();
    }

    /**
     * This is the method to edit the role name
     */
    private void editRoleName(final String name) {
        nameInput.clear();
        getActions().setText(nameInput, name);
    }

    /**
     * This is the method to edit the role description
     */
    private void editRoleDescription(final String description) {
        roleDescriptionInput.clear();
        getActions().setText(roleDescriptionInput, description);
    }

    /**
     * This method will click on the update button
     *
     * @return RoleDetailPage
     */
    private RoleDetailPage clickOnUpdateButton() {
        updateButton.click();

        return super.getPage(RoleDetailPage.class);
    }

}
