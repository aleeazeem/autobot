package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the page object for User Access Report page
 *
 * @author jains
 */
public class UserAccessReportPage extends GenericDetails {

    public UserAccessReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccessReportPage.class.getSimpleName());

    @FindBy(name = "userId")
    private WebElement userIdInput;

    @FindBy(id = "input-createdAfter")
    private WebElement startCreationDateInput;

    @FindBy(id = "input-createdBefore")
    private WebElement endCreationDateInput;

    @FindBy(id = "roleId")
    private WebElement roleSelect;

    @FindBy(className = "error-message")
    private WebElement errorText;

    @FindBy(css = ".errors")
    private WebElement errorMessage;

    @FindBy(id = "action")
    private WebElement actionSelect;

    /**
     * Method to set userId
     */
    public void setUserId(final String userId) {
        getActions().setText(userIdInput, userId);
        LOGGER.info("Set user id: " + userId);
    }

    /**
     * Method to set Creation start date
     */
    private void setCreationStartDate(final String creationStartDate) {
        getActions().setText(startCreationDateInput, creationStartDate);
        LOGGER.info("Set creation start date: " + creationStartDate);
    }

    /**
     * Method to set Creation end date
     */
    private void setCreationEndDate(final String creationEndDate) {
        getActions().setText(endCreationDateInput, creationEndDate);
        LOGGER.info("Set creation end date: " + creationEndDate);
    }

    /**
     * Method to selectRole
     */
    private void selectRole(final List<String> roleList) {
        for (final String strRole : roleList) {
            getActions().select(roleSelect, strRole);
            LOGGER.info("Selected role: " + strRole);
        }
    }

    /**
     * This method selects View/Download action on the report
     */
    private void selectAction(final String action) {
        LOGGER.info("Selecting action: " + action);
        getActions().select(actionSelect, action);
    }

    /**
     * Method to get url of access review report page.
     *
     * @return String (page url)
     */
    private String getAccessReviewReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.USER_ACCESS_REPORT.getForm();
    }

    /**
     * Method to navigate to user access report page
     */
    public void navigateToUserAccessReportPage() {
        final String url = getAccessReviewReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to generate report with selected filters
     */
    public UserAccessReportResultPage getReportWithSelectedFilters(final String applicationFamily, final String userId,
        final String creationStartDate, final String creationEndDate, final List<String> roleList,
        final String action) {
        navigateToUserAccessReportPage();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        if (applicationFamily != null) {
            selectApplicationFamily(applicationFamily);
        }
        if (userId != null) {
            setUserId(userId);
        }
        if (creationStartDate != null) {
            setCreationStartDate(creationStartDate);
        }
        if (creationEndDate != null) {
            setCreationEndDate(creationEndDate);
        }
        if (roleList != null) {
            selectRole(roleList);
        }
        selectAction(action);
        submit(TimeConstants.ONE_SEC);

        return super.getPage(UserAccessReportResultPage.class);
    }

    /**
     * Method to return error message on Date
     *
     * @return String (errorMessage)
     */

    public String getH3ErrorMessage() {
        String error = null;
        try {
            error = errorMessage.getText();
            LOGGER.info("Error is generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Error is not generated");
        }
        return error;
    }

    /**
     * Method to get role names
     */
    public List<String> getRoleNameList() {
        final List<String> roleList = new ArrayList<>();
        final Select selectRole = new Select(roleSelect);
        final List<WebElement> optionList = selectRole.getOptions();
        for (final WebElement option : optionList) {
            roleList.add(option.getText());
        }
        return roleList;
    }

}
