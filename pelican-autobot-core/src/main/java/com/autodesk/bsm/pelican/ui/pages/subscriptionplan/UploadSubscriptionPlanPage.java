package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Page object for Admin Tool's Upload Subscription Plans - On Admin Tool's Main Tab navigate to Subscriptions -> Upload
 * Subscription Plans
 *
 * @author Sunitha
 */

public class UploadSubscriptionPlanPage extends SubscriptionPlanGenericPage {

    // select App family
    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    // find Error messages
    @FindBy(className = "error-message")
    private WebElement errorMessage;

    @FindBy(linkText = "View Errors")
    private List<WebElement> viewErrorsLink;

    @FindBy(xpath = "//*[@id=\"find-results\"]/div[2]/table/tbody/tr[1]/td[1]")
    private WebElement uploadJobId;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionPlanPage.class.getSimpleName());

    public UploadSubscriptionPlanPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Method to select App family
    public void selectApplicationFamily(final String appFamilyName) {
        LOGGER.info("Select application family to '" + appFamilyName + "'");
        getActions().select(appFamilyIdSelect, appFamilyName);
    }

    // Method to get Error messages
    public String getH3ErrorMessage() {
        final String errorMessage = this.errorMessage.getText();
        LOGGER.info("Error Message is '" + errorMessage + "'");
        return errorMessage;
    }

    /**
     * Method to navigate to Subscription Plans upload page
     */
    public void navigateToSubscriptionPlanUploadPage() {
        final String subscriptionPlanImportForm = getEnvironment().getAdminUrl() + "/subscriptionPlan/importForm";
        try {
            if (!isPageValid(subscriptionPlanImportForm)) {
                getDriver().get(subscriptionPlanImportForm);
            }
        } catch (final UnhandledAlertException e) {
            LOGGER.info("Found page error: " + e.getMessage());
            getDriver().switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

    /**
     * Get list of upload errors
     *
     * @return
     */
    public List<WebElement> getViewErrorsLink() {
        return viewErrorsLink;
    }

    /**
     * Get Upload Job ID
     *
     * @return
     */
    public String getUploadJobId() {
        LOGGER.info("ID: " + uploadJobId.getText());
        return uploadJobId.getText();

    }

    /**
     * This is a method which returns the error from the Upload
     *
     * @param rowIndex
     * @param columnIndex
     * @return Feature details - String
     */
    public String getUploadError(final String jobId) {
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final WebElement element = driver.findElement(By.id("errors_" + jobId));
        return element.getAttribute("value");

    }

}
