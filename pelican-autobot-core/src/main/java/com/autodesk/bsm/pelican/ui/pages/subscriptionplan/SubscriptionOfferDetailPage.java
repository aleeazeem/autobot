package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This page represents subscription offer detail page in admin tool
 *
 * @author t_mohag
 */

public class SubscriptionOfferDetailPage extends SubscriptionPlanGenericPage {

    public SubscriptionOfferDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//button[text()='Edit Localized Descriptors']")
    private WebElement editLocalizedDescriptors;

    @FindBy(xpath = "//button[text()='Edit Non-Localized Descriptors']")
    private WebElement editNonLocalizedDescriptors;

    /**
     * Method to click on Edit Localized Descriptors button
     *
     * @return EditSubscriptionPlanAndOfferDescriptorsPage
     */
    public EditSubscriptionPlanAndOfferDescriptorsPage clickOnEditLocalizedDescriptors() {

        try {
            getActions().click(editLocalizedDescriptors);
            Wait.pageLoads(driver);
        } catch (final UnhandledAlertException uae) {
            getDriver().switchTo().alert().dismiss();
        }
        return super.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
    }

    /**
     * Method to click on Edit NonLocalized Descriptors button
     *
     * @return EditSubscriptionPlanAndOfferDescriptorsPage
     */
    public EditSubscriptionPlanAndOfferDescriptorsPage clickOnEditNonLocalizedDescriptors() {
        try {
            getActions().click(editNonLocalizedDescriptors);
        } catch (final UnhandledAlertException uae) {
            getDriver().switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
        return super.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
    }
}
