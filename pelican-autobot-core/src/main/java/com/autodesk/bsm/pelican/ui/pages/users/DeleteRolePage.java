package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a role for deleting a role in admin tool
 *
 * @author vineel
 */
public class DeleteRolePage extends GenericDetails {

    public DeleteRolePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='bd']//h1")
    private WebElement pageHeader;

    @FindBy(xpath = ".//*[@class='props']//tbody//tr[1]//td")
    private WebElement roleIdValue;

    @FindBy(xpath = ".//*[@class='props']//tbody//tr[2]//td")
    private WebElement roleNameValue;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRolePage.class.getSimpleName());

    /**
     * This is a method to return a text from the Delete role Page Header
     *
     * @return String
     */
    public String getPageHeaderText() {
        return pageHeader.getText();
    }

    /**
     * This method will return the role id that got deleted
     *
     * @return String - roleId
     */
    public String getRoleId() {
        final String roleId = roleIdValue.getText();
        LOGGER.info("Role Id : " + roleId);
        return roleId;
    }

    /**
     * This method will return the role name that got deleted
     *
     * @return String - roleName
     */
    public String getRoleName() {
        final String roleName = roleNameValue.getText();
        LOGGER.info("Role Name : " + roleName);
        return roleName;
    }

}
