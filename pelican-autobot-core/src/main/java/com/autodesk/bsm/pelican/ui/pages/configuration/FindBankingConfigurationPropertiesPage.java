package com.autodesk.bsm.pelican.ui.pages.configuration;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is reserved for finding banking configuration property
 *
 * @author Shweta Hegde
 */
public class FindBankingConfigurationPropertiesPage extends GenericDetails {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(FindBankingConfigurationPropertiesPage.class.getSimpleName());

    @FindBy(id = "component")
    private WebElement componentSelect;

    @FindBy(xpath = "//*[@id=\"subnav\"]/ul/li[4]/span")
    private WebElement configurationXpath;

    @FindBy(xpath = "//*[@id=\"subnav\"]/ul/li[3]/span")
    private WebElement alternativeConfigurationXpath;

    @FindBy(id = "subnav-link-bankingConfig-find")
    private WebElement findBankingProperties;

    public FindBankingConfigurationPropertiesPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method selects the component in the dropdown of Banking Configuration Properties Page
     */
    public BankingConfigurationPropertiesPage selectComponent(final String componentToSelect) {

        navigateToFindBankingConfiguration();
        final Select componentDropdown = new Select(componentSelect);
        componentDropdown.selectByVisibleText(componentToSelect);
        clickOnShowHive();
        LOGGER.info("Selected the component : " + componentToSelect);
        return super.getPage(BankingConfigurationPropertiesPage.class);
    }

    /**
     * This method to click on "Show Hive" button
     */
    private void clickOnShowHive() {
        submit();
    }

    /**
     * Navigate to Banking Properties page in Admin Tools Page
     */
    private void navigateToFindBankingConfiguration() {

        navigateToApplications();
        final String config = "Configuration";
        if (configurationXpath.getText().equalsIgnoreCase(config)) {
            configurationXpath.click();
        } else if (alternativeConfigurationXpath.getText().equalsIgnoreCase(config)) {
            alternativeConfigurationXpath.click();
        }

        findBankingProperties.click();
    }

    /**
     * Navigate to the Applications page in Admin Tool page
     */
    private void navigateToApplications() {

        final String applicationsUrl = "applications";
        final String url = getEnvironment().getAdminUrl() + "/" + applicationsUrl;
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
     * Navigate to Show Hives page in Admin Tools
     */
    public BankingConfigurationPropertiesPage showHives() {

        navigateToFindBankingConfiguration();
        clickOnShowHive();
        return super.getPage(BankingConfigurationPropertiesPage.class);
    }

}
