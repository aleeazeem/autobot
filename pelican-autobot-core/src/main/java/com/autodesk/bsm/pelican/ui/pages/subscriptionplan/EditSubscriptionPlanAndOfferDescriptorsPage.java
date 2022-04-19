package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This page represents edit Localized/Non-Localized descriptors page. This is usually accessed from the subscription
 * plan page.
 *
 * @author t_mohag
 */
public class EditSubscriptionPlanAndOfferDescriptorsPage extends SubscriptionPlanGenericPage {

    @FindBy(xpath = "//input[contains(@name,'ipp')]")
    private WebElement ippDescriptors;

    @FindBy(xpath = "//input[contains(@name,'estore.productName1')]")
    private WebElement estoreDescriptors;

    @FindBy(className = "submit")
    private WebElement updateDescriptorsButton;

    public EditSubscriptionPlanAndOfferDescriptorsPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * Method to edit Non Localized IPP Descriptor value
     */
    public void editNonLocalizedIPPDescriptorValue(final String descriptorValue) {
        Wait.elementVisibile(driver, ippDescriptors);
        ippDescriptors.clear();
        ippDescriptors.sendKeys(descriptorValue);
    }

    /**
     * Method to edit Non Localized Estore Descriptor value
     */
    public void editNonLocalizedEstoreDescriptorValue(final String descriptorValue) {
        Wait.elementVisibile(driver, estoreDescriptors);
        estoreDescriptors.clear();
        estoreDescriptors.sendKeys(descriptorValue);
    }

    /**
     * Method to edit Localized/Non-Localized Descriptor value
     */
    public void editDescriptorValue(final String groupName, final String descriptorsName,
        final String descriptorValue) {
        final String id = "input-" + groupName + "." + descriptorsName;
        final WebElement element = getDriver().findElement(By.id(id));
        Wait.elementDisplayed(driver, element);
        element.clear();
        element.sendKeys(descriptorValue);
    }

    /**
     * Method to click on update descriptors button
     */
    public void clickOnUpdateDescriptorsButton() {
        getActions().click(updateDescriptorsButton);
        Wait.pageLoads(getDriver());
    }
}
