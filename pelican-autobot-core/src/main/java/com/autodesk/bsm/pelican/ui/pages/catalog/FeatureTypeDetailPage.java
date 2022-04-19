package com.autodesk.bsm.pelican.ui.pages.catalog;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page Object for the Feature Type Detail Page which comes on adding a feature type
 *
 * @author vineel
 */
public class FeatureTypeDetailPage extends GenericDetails {

    public FeatureTypeDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='related']//ul//li[1]//a")
    private WebElement showAllFeaturesLink;

    /**
     * This is a method which will return the id of the feature type from the feature type detail page
     *
     * @return FeatureTypeId
     */
    public String getId() {
        return getValueByField("ID");
    }

    /**
     * This is a method which will return the name of the feature type from the feature type detail page
     *
     * @return FeatureTypeName
     */
    public String getName() {
        return getValueByField("Name");
    }

    /**
     * This is a method which will return the external key of the feature type from the feature type detail page
     *
     * @return FeatureTypeExternalKey
     */
    public String getExternalKey() {
        return getValueByField("External Key");
    }

    /**
     * This is a method which will click on the show features link on the feature type detail page
     */
    public void clickOnShowAllFeaturesLink() {
        showAllFeaturesLink.click();
    }
}
