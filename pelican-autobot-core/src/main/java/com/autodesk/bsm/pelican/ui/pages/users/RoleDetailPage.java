package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

/**
 * This is a page class for the role detail page in the admin tool
 *
 * @author vineel
 */
public class RoleDetailPage extends GenericDetails {

    public RoleDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='delete-action-form']//*[@type='submit']")
    private WebElement nameInput;

    @FindBy(xpath = ".//*[@name='Edit']")
    private WebElement editButton;

    @FindBy(xpath = ".//*[@name='Delete']")
    private WebElement deleteButton;

    @FindBy(xpath = ".//*[@class='n field']//*[@class='input']//*[@name='k0']")
    private WebElement firstPropertyNameInput;

    @FindBy(xpath = ".//*[@class='n field']//*[@class='input']//*[@name='k1']")
    private WebElement secondPropertyNameInput;

    @FindBy(xpath = ".//*[@class='v field']//*[@class='input']//*[@name='v0']")
    private WebElement firstPropertyValueInput;

    @FindBy(xpath = ".//*[@class='v field']//*[@class='input']//*[@name='v1']")
    private WebElement secondPropertyValueInput;

    @FindBy(xpath = ".//*[@id='updatepropertiessubmit']")
    private WebElement updateButton;

    @FindBy(xpath = ".//*[@class='revert-button']")
    private WebElement revertButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(id = "cancel-btn")
    private WebElement cancelButton;

    @FindBy(xpath = "//*[@id='related']/ul/li[1]/a")
    private WebElement findRoleAssignments;

    @FindBy(xpath = "//*[@id='related']/ul/li[2]/a")
    private WebElement setPermissions;

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleDetailPage.class.getSimpleName());

    /**
     * This is the method to click on the edit button on the role detail page
     */
    public EditRolePage clickOnEditButton() {
        editButton.click();

        return super.getPage(EditRolePage.class);
    }

    /**
     * This is the method to update the properties for a role
     *
     * @return RoleDetailPage
     */
    public RoleDetailPage updateProperties(final HashMap<String, String> propertiesMap) {

        getActions().setText(firstPropertyNameInput, propertiesMap.keySet().toArray()[0].toString());
        getActions().setText(firstPropertyValueInput,
            propertiesMap.get(propertiesMap.keySet().toArray()[0].toString()));
        getActions().setText(secondPropertyNameInput, propertiesMap.keySet().toArray()[1].toString());
        getActions().setText(secondPropertyValueInput,
            propertiesMap.get(propertiesMap.keySet().toArray()[1].toString()));
        clickOnUpdatePropertiesButton();

        return super.getPage(RoleDetailPage.class);
    }

    /**
     * This method will delete the created role in the admin tool
     */
    public DeleteRolePage deleteRole() {
        clickOnDeleteButton();
        final String mainWindow = getDriver().getWindowHandle();
        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        clickOnConfirmButton();
        getDriver().switchTo().window(mainWindow);

        return super.getPage(DeleteRolePage.class);
    }

    /**
     * This method returns id of role
     *
     * @return id
     */
    public String getRoleId() {
        final String id = getValueByField("ID");
        LOGGER.info("Role Id : " + id);
        return id;
    }

    /**
     * This method returns name of role
     *
     * @return name
     */
    public String getRoleName() {
        final String name = getValueByField("Name");
        LOGGER.info("Role name : " + name);
        return name;
    }

    /**
     * This method returns description of role
     *
     * @return description
     */
    public String getRoleDescription() {
        final String description = getValueByField("Description");
        LOGGER.info("Role Description : " + description);
        return description;
    }

    /**
     * This is a method to click on update button under properties on the role detail page
     */
    private void clickOnUpdatePropertiesButton() {
        updateButton.click();
    }

    /**
     * This method will click on the confirm button in the popup
     */
    private void clickOnConfirmButton() {
        confirmButton.click();
    }

    /**
     * This is a method which will click on the 'Set Permission' link on the Role detail page
     *
     * @return SetRolePermissionsPage
     */
    public SetRolePermissionsPage clickOnSetPermissionLink() {
        setPermissions.click();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return super.getPage(SetRolePermissionsPage.class);
    }

}
