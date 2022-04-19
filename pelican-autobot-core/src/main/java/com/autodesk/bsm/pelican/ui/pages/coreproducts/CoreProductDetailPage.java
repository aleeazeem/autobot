package com.autodesk.bsm.pelican.ui.pages.coreproducts;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is POM representation of Core Product detail page
 *
 * @author mandas
 */
public class CoreProductDetailPage extends GenericDetails {

    public CoreProductDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreProductDetailPage.class.getSimpleName());

    /**
     * This method returns id of a Core Product
     *
     * @return id
     */
    public String getId() {

        final String id = getValueByField("ID");
        LOGGER.info("Core Product id : " + id);
        return id;
    }

    /**
     * This method returns external key of a Core Product
     *
     * @return externalKey
     */
    public String getExternalKey() {

        final String externalKey = getValueByField("External Key");
        LOGGER.info("Core Product external key : " + externalKey);
        return externalKey;
    }

    /**
     * This method clicks on edit button
     *
     * @return EditCoreProductPage
     */
    public EditCoreProductPage clickOnEdit() {

        editButton.click();
        LOGGER.info("Click on 'Edit' to edit a Core Product page");
        return super.getPage(EditCoreProductPage.class);
    }

}
