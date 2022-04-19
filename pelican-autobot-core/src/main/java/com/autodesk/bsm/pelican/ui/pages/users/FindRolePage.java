package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for finding a role in admin tool
 *
 * @author vineel
 */
public class FindRolePage extends GenericDetails {

    public FindRolePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-name")
    private WebElement roleNameInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindRolePage.class.getSimpleName());

    /**
     * This method will find a role by role name
     *
     * @return GenericGrid
     */
    public GenericGrid findRoleByName(final String roleName) {

        navigateToFindRolePage();
        LOGGER.info("Finding a role by role name: " + roleName);
        getActions().setText(roleNameInput, roleName);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        submit(TimeConstants.ONE_SEC);

        return super.getPage(GenericGrid.class);

    }

    /**
     * This method will navigate to the find role page in the admin tool
     */
    private void navigateToFindRolePage() {

        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.ROLE.getForm() + "/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);

    }

}
