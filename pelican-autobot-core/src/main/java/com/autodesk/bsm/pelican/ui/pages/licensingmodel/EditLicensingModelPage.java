package com.autodesk.bsm.pelican.ui.pages.licensingmodel;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * this class represents POM for Edit functionality of Licensing Model
 *
 * @author mandas
 */
public class EditLicensingModelPage extends GenericDetails {

    public EditLicensingModelPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Check box to select SubscriptionLifecycle option
    @FindBy(id = "input-tiedToSubscriptionLifecycle")
    private WebElement subscriptionLifecycleCheckBox;

    // Check box to select FiniteTime option
    @FindBy(id = "input-forFiniteTime")
    private WebElement finiteTimeCheckBox;

    /**
     * Navigate to Edit Licensing Model page and add a new License Model.
     *
     * @param name - String
     * @param description - String
     * @param externalKey - String
     * @param lifeCycle - Boolean
     * @param finiteTime - Boolean
     * @return LicensingModelDetailPage
     */
    public LicensingModelDetailPage editLicencingModel(final String name, final String description,
        final String externalKey, final Boolean lifeCycle, final Boolean finiteTime) {
        fillLicenseModelData(name, description, externalKey, lifeCycle, finiteTime);
        return super.getPage(LicensingModelDetailPage.class);
    }

    /**
     * Method to fill the Licensing form
     *
     * @param name- String
     * @param description- String
     * @param externalKey- String
     * @param lifeCycle - This is checked if param is true
     * @param finiteTime - This is checked if param is true
     */
    private void fillLicenseModelData(final String name, final String description, final String externalKey,
        final Boolean lifeCycle, final Boolean finiteTime) {
        selectApplicationFamily(PelicanConstants.APPLICATION_FAMILY_NAME);
        setName(name);
        setExternalKey(externalKey);
        setDescription(description);
        if (lifeCycle) {
            actionOnCheckBox(subscriptionLifecycleCheckBox, PelicanConstants.CHECKBOX_CHECK);
        }
        if (finiteTime) {
            actionOnCheckBox(finiteTimeCheckBox, PelicanConstants.CHECKBOX_CHECK);
        }
        submit();
    }

}
