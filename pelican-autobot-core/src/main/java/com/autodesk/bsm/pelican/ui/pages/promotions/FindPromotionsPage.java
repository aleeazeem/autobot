package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for Find Promotions It includes methods for 1. Find Promotion By Id 2. Find Promotion By Code 3.
 * Find by Advanced Find
 *
 * @author - Shweta Hegde
 */
public class FindPromotionsPage extends GenericDetails {

    public FindPromotionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//span[text()='Advanced Find']")
    private WebElement advancedFindTab;

    @FindBy(id = "input-code")
    private WebElement codeInput;

    @FindBy(id = "subtype")
    private WebElement promotionTypeSelect;

    @FindBy(id = "input-storeId")
    private WebElement storeIdInput;

    @FindBy(className = "trigger")
    private WebElement storeSearchIcon;

    @FindBy(id = "state")
    private WebElement stateSelect;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByCodeTab;

    @FindBy(id = "storewide")
    private WebElement storeWideSelect;

    @FindBy(id = "bundled")
    private WebElement bundledSelect;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindPromotionsPage.class.getSimpleName());

    /**
     * Method to navigate to Find Promotions
     */
    private void navigateToFindPromotion() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PROMOTION.getForm() + "/findForm";
        LOGGER.info("Navigate to '" + url + "'");
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

    /**
     * This is a method to find promotions by id, without giving any id
     *
     * @return GenericGrid
     */
    public PromotionsSearchResultPage findById() {

        navigateToFindPromotion();
        Wait.pageLoads(driver);
        submit();
        return super.getPage(PromotionsSearchResultPage.class);
    }

    /**
     * This method takes id and provides promotion details
     *
     * @return PromotionDetailsPage
     */
    public PromotionDetailsPage findById(final String id) {
        navigateToFindPromotion();
        setId(id);
        submit();

        return super.getPage(PromotionDetailsPage.class);
    }

    /**
     * This method returns grid of promotions depending on Promo Code
     *
     * @return GenericGrid
     */
    public PromotionsSearchResultPage findByCode(final String code) {

        navigateToFindPromotion();
        findByCodeTab.click();
        getActions().setText(codeInput, code);
        submit(1);
        return super.getPage(PromotionsSearchResultPage.class);
    }

    public PromotionDetailsPage selectResultRowWithFindByCode(final int row, final String code) {

        final GenericGrid genericGrid = findByCode(code);
        genericGrid.selectResultRow(row);
        return super.getPage(PromotionDetailsPage.class);
    }

    /**
     * This method takes arguments on status, promotion type and store id and returns grid of promotions
     *
     * @return GenericGrid
     */
    public PromotionsSearchResultPage findByAdvancedFind(final PromotionType promotionType, final String storeId,
        final Status status, final String storeWide, final String bundled) {

        navigateToFindPromotion();
        advancedFindTab.click();
        if (promotionType != null) {
            selectPromotionType(promotionType);
        }
        if (storeId != null) {
            setStore(storeId);
        }
        if (status != null) {
            selectStatus(status);
        }
        if (storeWide != null) {
            selectStoreWide(storeWide);
        }
        if (bundled != null) {
            selectBundled(bundled);
        }
        submit(2);
        return super.getPage(PromotionsSearchResultPage.class);
    }

    /**
     * This method selects a row in the result grid and returns promotion details page
     *
     * @return PromotionDetailsPage
     */
    public PromotionDetailsPage selectResultRowWithAdvancedFind(final int row, final PromotionType promotionType,
        final String storeId, final Status status, final String storeWide, final String bundled) {

        final GenericGrid genericGrid = findByAdvancedFind(promotionType, storeId, status, storeWide, bundled);
        genericGrid.selectResultRow(row);
        return super.getPage(PromotionDetailsPage.class);
    }

    /**
     * This is a method to select promotion type
     */
    private void selectPromotionType(final PromotionType promotionType) {

        getActions().select(promotionTypeSelect, promotionType.getDisplayName());
    }

    /**
     * This is a method to set store
     */
    private void setStore(final String storeId) {

        getActions().setText(storeIdInput, storeId);
    }

    /**
     * This is a method to select status
     */
    private void selectStatus(final Status status) {

        getActions().select(stateSelect, status.toString());
    }

    /**
     * This is a method to select storewide
     */
    private void selectStoreWide(final String storewide) {

        getActions().select(storeWideSelect, storewide);
    }

    /**
     * This is a method to select bundled
     */
    private void selectBundled(final String bundledValue) {

        getActions().select(bundledSelect, bundledValue);
    }
}
