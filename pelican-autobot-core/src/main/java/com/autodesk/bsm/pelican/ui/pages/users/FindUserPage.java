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

public class FindUserPage extends GenericDetails {

    public FindUserPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByExternalKey;

    @FindBy(css = ".form-group-labels > h3:nth-child(3)")
    private WebElement advancedFind;

    @FindBy(id = "input-nameOrId")
    private WebElement idOrNameInput;

    @FindBy(id = "input-username")
    private WebElement userNameInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindUserPage.class.getSimpleName());

    private void navigateToFindUser() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.USER.getForm() + "/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Navigate to find by Id or Name
     *
     * @param idOrName - ID or Name
     * @return user page
     */
    public UserDetailsPage getByName(final String name) {
        navigateToFindUser();
        getActions().setText(idOrNameInput, name);
        submit(TimeConstants.ONE_SEC);
        final GenericGrid userGrid = super.getPage(GenericGrid.class);
        userGrid.selectResultRow(1);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        LOGGER.info("Page title: " + getTitle());
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * Navigate to find by Id
     *
     * @param idOrName - ID
     * @return user page
     */
    public UserDetailsPage getById(final String id) {
        navigateToFindUser();
        getActions().setText(idOrNameInput, id);
        submit(TimeConstants.ONE_SEC);
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * Navigate to find by external Key
     *
     * @return user page
     */
    public UserDetailsPage getByExternalKey(final String externalKey) {
        navigateToFindUser();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        findByExternalKey.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        setExternalKey(externalKey);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        submit(1);
        return super.getPage(UserDetailsPage.class);
    }
}
