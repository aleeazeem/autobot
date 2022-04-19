package com.autodesk.bsm.pelican.ui.pages.configuration;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureFlagSetPage extends GenericDetails {

    public FeatureFlagSetPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "value")
    private WebElement valueSelect;

    @FindBy(className = "value")
    private WebElement featureFlagValue;

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFlagSetPage.class.getSimpleName());

    /**
     * This method edits the value of feature flag Method returns true if the feature flag is changed, else returns
     * false.
     *
     * @param value
     * @return boolean
     */
    public boolean editValue(final String value) {

        // Checks if argument value matches with the existing feature flag value, if not change it.
        if (!valueSelect.getAttribute("value").equalsIgnoreCase(value)) {
            getActions().select(valueSelect, value);

            final String featureFlagName = featureFlagValue.getText();
            submit(TimeConstants.TWO_SEC);
            LOGGER.info("Changed the " + featureFlagName + " feature flag value to : " + value);
            return true;
            // If feature flag value is as expected, then do click on "Submit" and do nothing else.
        } else {
            submit(TimeConstants.ONE_SEC);
        }
        return false;
    }

    /**
     * Method to get selected feature value.
     *
     * @return
     */
    public boolean getSelectedFeatureFlagValue() {
        final Select select = new Select(getDriver().findElement(By.id("value")));
        final String selectedValue = select.getFirstSelectedOption().getText();
        return selectedValue.equals(PelicanConstants.TRUE);
    }
}
