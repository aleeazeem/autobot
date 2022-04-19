package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object represent the Upload Subscription Page. Access via Subscriptions | Subscription | Upload
 *
 * @author t_mohag
 */
public class UploadSubscriptionsPage extends GenericDetails {

    public UploadSubscriptionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // select Application family
    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    // find Error messages
    @FindBy(className = "error-message")
    private WebElement errorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionsPage.class.getSimpleName());

    // Method to select Application family
    public void selectApplicationFamily(final String appFamilyName) {
        LOGGER.info("Select application family to '" + appFamilyName + "'");
        getActions().select(appFamilyIdSelect, appFamilyName);
    }

    /**
     * Method to get error messages
     */
    public String getH3ErrorMessage() {
        final String errorMessage = this.errorMessage.getText();
        LOGGER.info("Error Message is '" + errorMessage + "'");
        return errorMessage;
    }

    /**
     * Method to navigate to Subscription upload page
     */
    public void navigateToSubscriptionUploadPage() {
        final String subscriptionImportForm = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm()
            + "/" + AdminPages.IMPORT_FORM.getForm();
        if (!isPageValid(subscriptionImportForm)) {
            getDriver().get(subscriptionImportForm);
        }
    }

}
