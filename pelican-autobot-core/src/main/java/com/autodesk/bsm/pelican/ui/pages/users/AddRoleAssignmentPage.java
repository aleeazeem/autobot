package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AddRoleAssignmentPage extends GenericDetails {

    public AddRoleAssignmentPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "roleId")
    private WebElement roleId;

    /**
     * This method assigns a given role to the provided named party using the admin tool. This method accepts list of
     * roles
     */
    public void selectRole(final List<Role> roles) {

        for (final Role role : roles) {
            getActions().select(roleId, role.getValue());
        }
    }

    /**
     * This method clicks on Submit button and return RoleAssignmentCompletePage
     *
     * @return RoleAssignmentCompletePage
     */
    public RoleAssignmentCompletePage clickOnAssignRole() {
        submit();
        return super.getPage(RoleAssignmentCompletePage.class);
    }
}
