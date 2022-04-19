package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page class of Product line details Getter methods for all info and edit button
 *
 * @author Shweta Hegde
 */
public class ProductLineDetailsPage extends GenericDetails {

    public ProductLineDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLineDetailsPage.class.getSimpleName());

    /**
     * This method returns id of a product line
     *
     * @return id
     */
    public String getId() {

        final String id = getValueByField("ID");
        LOGGER.info("User id : " + id);
        return id;
    }

    /**
     * This method returns name of a product line
     *
     * @return Name
     */
    public String getName() {

        final String name = getValueByField("Name");
        LOGGER.info("Product Line Name : " + name);
        return name;
    }

    /**
     * This method returns external key of a product line
     *
     * @return externalKey
     */
    public String getExternalKey() {

        final String externalKey = getValueByField("External Key");
        LOGGER.info("Product Line external key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns Active Status of a product line
     *
     * @return Active Status
     */
    public String getActiveStatus() {

        final String activeStatus = getValueByField(PelicanConstants.ACTIVE_STATUS_FIELD);
        LOGGER.info("Product Line Active Status : " + activeStatus);
        return activeStatus;
    }

    /**
     * This method clicks on edit button
     *
     * @return EditProductLinePage
     */
    public EditProductLinePage clickOnEdit() {

        editButton.click();
        LOGGER.info("Click on 'Edit' to edit productline");
        return super.getPage(EditProductLinePage.class);
    }
}
