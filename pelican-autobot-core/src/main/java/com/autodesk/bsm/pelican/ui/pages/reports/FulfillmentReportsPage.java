package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Admin Tool's Fulfillment Report on the main tab navigate to
 *
 * @author Sunitha
 */

public class FulfillmentReportsPage extends GenericDetails {
    public FulfillmentReportsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Order Date Purchased After Input
    @FindBy(id = "input-purchasedAfter")
    private WebElement orderDatePurchasedAfterInput;

    // Order Date Purchase Before Input
    @FindBy(id = "input-purchasedBefore")
    private WebElement orderDatePurchasedBeforeInput;

    // Options Include successful fulfillment groups check box
    @FindBy(name = "includeSuccessful")
    private WebElement includeSuccessfulCheckbox;

    // Options Include failed fulfillment groups check box
    @FindBy(name = "includeFailed")
    private WebElement includeFailedCheckbox;

    // Options Include pending fulfillment groups older than check box
    @FindBy(name = "includePending")
    private WebElement includePendingCheckbox;

    // Pending Duration Input
    @FindBy(name = "pendingDuration")
    private WebElement pendingDurationInput;

    // Pending Units Drop down
    @FindBy(name = "pendingUnits")
    private WebElement pendingUnitsSelect;

    // Generate Report Submit button
    @FindBy(className = "submit")
    private WebElement generateReportSubmitButton;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentReportsPage.class.getSimpleName());

    // method to activate the Options Include Successful Fulfillment Groups
    // check box
    public void activateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includeSuccessfulCheckbox.getAttribute("checked") == null) {
            includeSuccessfulCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already checked.");
        }
    }

    // method to deActivate the Options Include Successful Fulfillment Groups
    // Check box
    public void deactivateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includeSuccessfulCheckbox.getAttribute("checked") != null) {
            includeSuccessfulCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already unchecked.");
        }

    }

    // method to activate the Options Include Failed Fulfillment Groups check
    // box
    public void activateOptionsIncludeFailedFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includeFailedCheckbox.getAttribute("checked") == null) {
            includeFailedCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already checked.");
        }
    }

    // method to deActivate the Options Include Failed Fulfillment Groups
    // Checkbox
    public void deactivateOptionsIncludeFailedFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includeFailedCheckbox.getAttribute("checked") != null) {
            includeFailedCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already unchecked.");
        }

    }

    // method to activate the Options Include Pending Fulfillment Groups check
    // box
    public void activateOptionsIncludePendingFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includePendingCheckbox.getAttribute("checked") == null) {
            includePendingCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already checked.");
        }
    }

    // method to deActivate the Options Include Pending Fulfillment Groups
    // Checkbox
    public void deactivateOptionsIncludePendingFulfillmentGroupsCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (includePendingCheckbox.getAttribute("checked") != null) {
            includePendingCheckbox.click();
        } else {
            LOGGER.info("Checkbox *OptionsIncludeSuccessfulFulfillmentGroupsCheckbox* is already unchecked.");
        }

    }

    /**
     * Method to set order after date
     */
    public void setOrderAfterDate(final String orderAfterDate) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Order after date '" + orderAfterDate + "'");
        getActions().setText(orderDatePurchasedAfterInput, orderAfterDate);
    }

    /**
     * Method to set order before date
     */
    public void setOrderBeforeDate(final String orderBeforeDate) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Order before date '" + orderBeforeDate + "'");
        getActions().setText(orderDatePurchasedBeforeInput, orderBeforeDate);
    }

    /**
     * Method to set Pending duration value.
     */
    public void setPendingDuration(final String pendingDuration) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Pending duration '" + pendingDuration + "'");
        getActions().setText(pendingDurationInput, pendingDuration);
    }

    /**
     * Method to select Pending units value.
     */
    public void selectPendingUnits(final String pendingUnits) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Select Pending Units '" + pendingUnits + "'");
        getActions().select(pendingUnitsSelect, pendingUnits);
    }

    /**
     * Determine if we're on the Fulfillment Report page by getting the current url as well as the header =
     * "GenerateFulfillment Report"
     *
     * @return true if we're on the page. Otherwise false
     */
    public boolean isPageValid() {
        boolean pageDisplayed = false;

        if (getDriver().getCurrentUrl().equalsIgnoreCase(getUrl())) {
            try {
                reportContainer.isDisplayed();
                pageDisplayed = true;
            } catch (final NoSuchElementException e) {
                // ignore
            }
        }
        return pageDisplayed;
    }

    /**
     * Method to get Fulfillment Report page url.
     *
     * @return string (page url)
     */
    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.FULFILLMENT_REPORT.getForm();
    }

    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    public void clickGenerateReport() {
        getActions().click(this.generateReportSubmitButton);
    }
}
