package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Promotion Use Reports it comes under Promotion Report in main tab of Reports in Admin Tool
 *
 * @author Muhammad
 */
public class PromotionUseReportPage extends GenericDetails {

    public PromotionUseReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-promotionId")
    private WebElement promotionIdInput;

    @FindBy(id = "input-promotionCode")
    private WebElement promotionCodeInput;

    @FindBy(id = "storeWide")
    private WebElement storeWideSelect;

    @FindBy(id = "promotionType")
    private WebElement promotionTypeSelect;

    @FindBy(name = "includeNew")
    private WebElement newCheckbox;

    @FindBy(name = "includeActive")
    private WebElement activeCheckbox;

    @FindBy(name = "includeCanceled")
    private WebElement canceledCheckbox;

    @FindBy(name = "includeExpired")
    private WebElement expiredCheckbox;

    @FindBy(id = "input-createdAfter")
    private WebElement createdAfterInput;

    @FindBy(id = "input-createdBefore")
    private WebElement createdBeforeInput;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    @FindBy(css = ".errors")
    private WebElement errorMessageText;

    @FindBy(className = "none-found")
    private WebElement nonefound;

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionUseReportPage.class.getSimpleName());

    /**
     * Determine if we're on the Promotion Use Report page by getting the current url as well as the header
     *
     * @return true if we're on the page. Otherwise false
     */
    public boolean isPageValid() {
        boolean pageDisplayed = false;

        if (getDriver().getCurrentUrl().equalsIgnoreCase(getPromottionUseReportUrl())) {
            try {
                reportContainer.isDisplayed();
                pageDisplayed = true;
            } catch (final NoSuchElementException e) {
                LOGGER.info("Headers are not Displayed");
            }
        }
        return pageDisplayed;
    }

    public boolean isNoneFound() {
        boolean noneFound = false;
        try {
            noneFound = nonefound.isDisplayed();
            LOGGER.info("None Found Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("None Found Doesn't Exist");
        }
        return noneFound;
    }

    /**
     * Method to set promotion id
     */
    private void setPromotionId(final String promoId) {
        LOGGER.info("Set Promotion Id '" + promoId + "'");
        getActions().setText(promotionIdInput, promoId);
    }

    /**
     * Method to set promotion code
     */
    private void setPromotionCode(final String promoCode) {
        LOGGER.info("Set Promotion code '" + promoCode + "'");
        getActions().setText(promotionCodeInput, promoCode);
    }

    /**
     * Method to select storeWide
     */
    private void selectStoreWide(final String storeWide) {
        LOGGER.info("Select Store Wide '" + storeWide + "'");
        getActions().select(storeWideSelect, storeWide);
    }

    /**
     * Method to select promotionType
     */
    private void selectPromoType(final String promoType) {
        LOGGER.info("Set Promotion code '" + promoType + "'");
        getActions().select(promotionTypeSelect, promoType);
    }

    /**
     * Method to select promoState
     */
    private void checkStatus(final Status status) {
        LOGGER.info("Set Promotion State '" + status + "'");
        if (status.equals(Status.NEW)) {
            getActions().check(newCheckbox);
        }
        if (status.equals(Status.ACTIVE)) {
            getActions().check(activeCheckbox);
        }
        if (status.equals(Status.CANCELLED)) {
            getActions().check(canceledCheckbox);
        }
        if (status.equals(Status.EXPIRED)) {
            getActions().check(expiredCheckbox);
        }
    }

    /**
     * method uncheck Active status option
     */
    public void uncheckActiveStatus() {
        if (activeCheckbox.isSelected()) {
            activeCheckbox.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Active* is already unchecked.");
        }
    }

    /**
     * Method to set created before date
     */
    private void setOrderBeforeDate(final String beforeDate) {
        LOGGER.info("Order before date '" + beforeDate + "'");
        getActions().setText(createdBeforeInput, beforeDate);
    }

    /**
     * Method to set created after date
     */
    private void setOrderAfterDate(final String afterDate) {
        LOGGER.info("Order after date '" + afterDate + "'");
        getActions().setText(createdAfterInput, afterDate);
    }

    public void navigateToPage() {
        final String url = getPromottionUseReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
    }

    /**
     * method to return error message
     *
     * @return errorMessage
     */
    public String getStatusErrorMessage() {
        String statusErrorMessage = null;
        try {
            statusErrorMessage = errorMessageText.getText();
            LOGGER.info("Error message is '" + statusErrorMessage);
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Error for selecting a status is not generated");
        }
        return statusErrorMessage;
    }

    /**
     * This method navigates to report after selectig a filters to generate desired report.
     */
    public void getReportWithCombination(final String promoId, final String promoCode, final String storeWide,
        final String promoType, final Status promoState, final String dateAfter, final String dateBefore,
        final String action) {
        navigateToPage();
        uncheckActiveStatus();
        if (promoId != null) {
            setPromotionId(promoId);
        }
        if (promoCode != null) {
            setPromotionCode(promoCode);
        }
        if (storeWide != null) {
            selectStoreWide(storeWide);
        }
        if (promoType != null) {
            selectPromoType(promoType);
        }
        if (promoState != null) {
            checkStatus(promoState);
        }
        if (dateAfter != null) {
            setOrderAfterDate(dateAfter);
        }
        if (dateBefore != null) {
            setOrderBeforeDate(dateBefore);
        }
        if (action != null) {
            selectViewDownloadAction(action);
        }

        submit(TimeConstants.ONE_SEC);
    }

    /**
     * method to navigate to orders where the promo has been redeemed from promotion Detail Page
     */
    public void navigateToPromoRedeemedRelatedActions(final String id) {
        final String url = getEnvironment().getAdminUrl() + "/reports/order" + AdminPages.PROMOTION_USE_REPORT
            + "?promotionId=" + id + "&action=0";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to get Promotion Use Report page url.
     *
     * @return string (page url)
     */
    private String getPromottionUseReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.PROMOTION_USE_REPORT.getForm();
    }
}
