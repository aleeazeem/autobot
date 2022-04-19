package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Extend PromotionPage.
 *
 * @author jains
 */
public class ExtendPromotionPage extends GenericGrid {

    public ExtendPromotionPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//*[@id='bd']/h1")
    private WebElement pageHeader;

    @FindBy(id = "input-maxUses")
    private WebElement maxNumberOfUsesInput;

    @FindBy(id = "input-maxUsesPerUser")
    private WebElement maxUsesPerUserInput;

    @FindBy(id = "input-expirationDate")
    private WebElement effectiveEndDate;

    @FindBy(xpath = ".//*[text()[contains(.,'Cancel')]]")
    private WebElement cancelButton;

    @FindBy(css = ".error-message")
    private WebElement expirationDateError;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendPromotionPage.class.getSimpleName());

    public void setMaxNumberOfUses(final String maxNumberOfUses) {
        getActions().setText(maxNumberOfUsesInput, maxNumberOfUses);
    }

    public String getMaxNumberOfUses() {
        return maxNumberOfUsesInput.getText();
    }

    public void setMaxUsesPerUser(final String maxUsesPerUser) {
        getActions().setText(maxUsesPerUserInput, maxUsesPerUser);
    }

    public String getMaxUsesPerUser() {
        return maxUsesPerUserInput.getText();
    }

    public void extendActivePromotion() {
        LOGGER.info("Extend active promotion");
        submit();
    }

    public void setEffectiveEndDate(final String date) {
        getActions().setText(effectiveEndDate, date);
    }

    public String getEffectiveEndDate() {
        return effectiveEndDate.getText();
    }

    public String getPageHeader() {
        return this.pageHeader.getText();
    }

    public void cancelExtendActivePromotion() {
        this.cancelButton.click();
    }

    public String getExpirationDateError() {
        return this.expirationDateError.getText();
    }

}
