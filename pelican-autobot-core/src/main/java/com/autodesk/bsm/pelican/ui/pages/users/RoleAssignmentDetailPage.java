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

/***
 * Page object Pattern represents Role Assignment Detail Page through AdminTool under User.
 *
 * @author Vaibhavi
 */
public class RoleAssignmentDetailPage extends GenericDetails {

    public RoleAssignmentDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAssignmentDetailPage.class.getSimpleName());

    @FindBy(xpath = ".//*[@id='updatepropertiessubmit']")
    private WebElement updateButton;

    @FindBy(xpath = ".//*[@name='Delete']")
    private WebElement deleteButton;

    @FindBy(xpath = ".//*[@class='revert-button']")
    private WebElement revertButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(xpath = ".//*[@class='n field']//*[@class='input']//*[@name='k0']")
    private WebElement firstPropertyNameInput;

    @FindBy(xpath = ".//*[@class='n field']//*[@class='input']//*[@name='k1']")
    private WebElement secondPropertyNameInput;

    @FindBy(xpath = ".//*[@class='v field']//*[@class='input']//*[@name='v0']")
    private WebElement firstPropertyValueInput;

    @FindBy(xpath = ".//*[@class='v field']//*[@class='input']//*[@name='v1']")
    private WebElement secondPropertyValueInput;

    /**
     * This method returns id of a Role Assignment
     *
     * @return id
     */
    public String getRoleAssignmentId() {

        final String id = getValueByField("ID");
        LOGGER.info("Role Assignment id : " + id);
        return id;
    }

    /**
     * This method returns Name of a Role
     *
     * @return Role Name
     */
    public String getRoleName() {

        final String roleName = getValueByField("Role");
        LOGGER.info("Role Name : " + roleName);
        return roleName;
    }

    /**
     * This method returns Named Party
     *
     * @return Named party
     */
    public String getNamedParty() {

        final String getNamedParty = getValueByField("Named Party");
        LOGGER.info("Named Party : " + getNamedParty);
        return getNamedParty;
    }

    /**
     * This is the method to update the properties for a role
     *
     * @return RoleAssignmentDetailPage
     */
    public RoleAssignmentDetailPage updateProperties(final HashMap<String, String> propertiesMap) {

        getActions().setText(firstPropertyNameInput, propertiesMap.keySet().toArray()[0].toString());
        getActions().setText(firstPropertyValueInput,
            propertiesMap.get(propertiesMap.keySet().toArray()[0].toString()));
        getActions().setText(secondPropertyNameInput, propertiesMap.keySet().toArray()[1].toString());
        getActions().setText(secondPropertyValueInput,
            propertiesMap.get(propertiesMap.keySet().toArray()[1].toString()));
        clickOnUpdatePropertiesButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(RoleAssignmentDetailPage.class);
    }

    /**
     * Click on Delete link
     *
     * @return RoleAssignmentDeletePage
     */
    public RoleAssignmentDeletePage deleteRole() {
        LOGGER.info("Delete Role");
        clickOnDeleteButton();
        final String mainWindow = getDriver().getWindowHandle();
        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        clickOnConfirmButton();
        getDriver().switchTo().window(mainWindow);

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(RoleAssignmentDeletePage.class);
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

}
