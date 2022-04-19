package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class to add a role in Admin Tool
 *
 * @author vineel
 */
public class AddRolePage extends GenericDetails {

    public AddRolePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-description")
    private WebElement roleDescriptionInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddRolePage.class.getSimpleName());

    /**
     * This is the method which will navigate to add role page and add a role in a admin tool
     */
    public RoleDetailPage addRole(final String roleName, final String description) {

        navigateToAddRole();
        setRoleName(roleName);
        setRoleDescription(description);
        submit(TimeConstants.ONE_SEC);

        return super.getPage(RoleDetailPage.class);
    }

    /**
     * This is a method to navigate to Add Role Page
     */
    private void navigateToAddRole() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.ROLE.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * This is a method to set the role name in the Admin Tool
     */
    private void setRoleName(final String name) {
        getActions().setText(nameInput, name);
        LOGGER.info("Set role name to " + name);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * This is a method to set the role description
     */
    private void setRoleDescription(final String roleDescription) {
        getActions().setText(roleDescriptionInput, roleDescription);
        LOGGER.info("Set role description to " + roleDescription);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

}
