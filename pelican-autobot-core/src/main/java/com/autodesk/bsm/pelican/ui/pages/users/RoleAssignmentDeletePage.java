package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is page for Role Assignment deleted page in admin tool.
 *
 * @author Vaibhavi
 */
public class RoleAssignmentDeletePage extends GenericDetails {

    @FindBy(xpath = ".//*[@class='props']//tbody//tr[1]//td")
    private WebElement roleAssignmentIdValue;

    @FindBy(xpath = ".//*[@class='props']//tbody//tr[2]//td")
    private WebElement roleNameValue;

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAssignmentDeletePage.class.getSimpleName());

    public RoleAssignmentDeletePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method will return the role assignment id that got deleted
     *
     * @return String - roleAssignmentIdValue
     */
    public String getRoleAssignmentId() {
        final String roleAssignmentId = roleAssignmentIdValue.getText();
        LOGGER.info("Role Assignment Id : " + roleAssignmentId);
        return roleAssignmentId;

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
