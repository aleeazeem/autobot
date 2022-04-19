package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleAssignmentResultPage extends GenericGrid {

    public RoleAssignmentResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAssignmentResultPage.class.getSimpleName());

    /**
     * This methods navigate to the page of Show Role Assignement Page and select the roles which are passed as a
     * parameters.
     *
     * @return RoleAssignmentDetailPage
     */
    public RoleAssignmentDetailPage selectRole(final String id, final String selectRole) {

        // Navigate to Role Assignment
        navigateToRoleAssignment(id);

        // Click on Role
        final WebElement roleLink = getDriver().findElement(By.linkText(selectRole));
        roleLink.click();

        return super.getPage(RoleAssignmentDetailPage.class);
    }

    /**
     * Navigate to Role Assignment Page.
     */
    private void navigateToRoleAssignment(final String id) {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.ROLE_ASSIGNMENT.getForm() + "/find?namedPartyId=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
