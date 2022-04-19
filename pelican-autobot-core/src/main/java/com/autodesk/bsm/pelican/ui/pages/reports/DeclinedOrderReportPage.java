package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Declined Order Reports it cpmes under Order Report in main tab of Reports in Admin Tool
 *
 * @author Muhammad
 */
public class DeclinedOrderReportPage extends BasicOfferingsReportPage {

    public DeclinedOrderReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-purchasedAfter")
    private WebElement orderDatePurchasedAfterInput;

    @FindBy(id = "input-purchasedBefore")
    private WebElement orderDatePurchasedBeforeInput;

    @FindBy(name = "declined")
    private WebElement declinedCheckBox;

    @FindBy(name = "submitted")
    private WebElement submittedCheckBox;

    @FindBy(name = "cancelled")
    private WebElement chargedBackCheckBox;

    @FindBy(name = "includePendingTimeFrame")
    private WebElement includePendingTimeFrameCheckBox;

    @FindBy(name = "pendingDuration")
    private WebElement pendingDurationInput;

    @FindBy(id = "action")
    private WebElement viewOrDownloadSelection;

    @FindBy(css = ".error-message")
    private WebElement errorMessageText;

    @FindBy(className = "submit")
    private WebElement generateReportButton;

    @FindBy(className = "none-found")
    private WebElement noResults;

    @FindBy(id = "declineReason")
    private WebElement declineReasonSelect;

    @FindBy(id = "action")
    private WebElement actionToSelect;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeclinedOrderReportPage.class.getSimpleName());

    /**
     * Method to set order before date
     */
    public void setOrderBeforeDate(final String orderBeforeDate) {
        LOGGER.info("Order before date '" + orderBeforeDate + "'");
        getActions().setText(orderDatePurchasedBeforeInput, orderBeforeDate);
    }

    /**
     * Method to set order after date
     */
    public void setOrderAfterDate(final String orderAfterDate) {
        LOGGER.info("Order before date '" + orderAfterDate + "'");
        getActions().setText(orderDatePurchasedAfterInput, orderAfterDate);
    }

    /**
     * method check include declined orders option
     */
    public void declinedOrdersCheck() {
        if (declinedCheckBox.getAttribute("checked") == null) {
            declinedCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Declined Orders* is already checked.");
        }
    }

    /**
     * method uncheck include declined orders option
     */
    public void declinedOrdersUncheck() {
        if (declinedCheckBox.getAttribute("checked") != null) {
            declinedCheckBox.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Declined Orders* is already unchecked.");
        }
    }

    /**
     * method check include submitted orders option
     */
    public void submittedOrdersCheck() {
        if (submittedCheckBox.getAttribute("checked") == null) {
            submittedCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Submitted Orders* is already checked.");
        }
    }

    /**
     * method uncheck include submitted orders option
     */
    public void submittedOrdersUncheck() {
        if (submittedCheckBox.getAttribute("checked") != null) {
            submittedCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Submitted Orders* is already unchecked.");
        }
    }

    /**
     * method check include pending orders option
     */
    public void pendingStatusOrdersCheck() {
        if (includePendingTimeFrameCheckBox.getAttribute("checked") == null) {
            includePendingTimeFrameCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Pending Status* is already checked.");
        }
    }

    /**
     * method uncheck include pending orders option
     */
    public void pendingStatusOrdersUncheck() {
        if (includePendingTimeFrameCheckBox.getAttribute("checked") != null) {
            includePendingTimeFrameCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Pending Status* is already unchecked.");
        }
    }

    /**
     * method check include chargedBack orders option
     */
    public void chargedBackStatusOrdersCheck() {
        if (chargedBackCheckBox.getAttribute("checked") == null) {
            chargedBackCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Charged Back Status* is already checked.");
        }
    }

    /**
     * method uncheck include chargedBack orders option
     */
    public void chargedBackOrdersUncheck() {
        if (chargedBackCheckBox.getAttribute("checked") != null) {
            chargedBackCheckBox.click();
        } else {
            LOGGER.info("Checkbox *Include Charged Back Status* is already unchecked.");
        }
    }

    /**
     * method to select decline reason from dropdown
     */
    public void selectDeclineReason(final String declineReason) {
        getActions().select(declineReasonSelect, declineReason);
        LOGGER.info("Decline Reason is set to:" + declineReason);
    }

    /**
     * method to select Action from Drop down
     */
    public void selectAction(final String action) {
        getActions().select(actionToSelect, action);
        LOGGER.info("Action is selected:" + action);
    }

    /**
     * Method to get Declined Order Report page url.
     *
     * @return string (page url)
     */
    private String getDeclinedOrderReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.DECLINED_ORDER_REPORT.getForm();
    }

    public void navigateToPage() {
        final String url = getDeclinedOrderReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    public String getCheckBoxErrorMessages() {
        final String errorMessage = errorMessageText.getText();
        LOGGER.info("Error message is '" + errorMessage);
        return errorMessage;
    }

    public String getNotFound() {
        final String message = noResults.getText();
        LOGGER.info("Error message is '" + message);
        return message;
    }

    public void clickGenerateReportButton() {
        getActions().click(this.generateReportButton);
    }

    /**
     * @return the presence of drop down of decline reason select
     */
    public boolean isSelectDeclineReasonFilterPresent() {
        boolean filterSelectDeclineReason = false;
        try {
            filterSelectDeclineReason = declineReasonSelect.isDisplayed();
            LOGGER.info("DropDown of select Decline Reason Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("DropDown of select Decline Reason Exist");
        }
        return filterSelectDeclineReason;
    }
}
