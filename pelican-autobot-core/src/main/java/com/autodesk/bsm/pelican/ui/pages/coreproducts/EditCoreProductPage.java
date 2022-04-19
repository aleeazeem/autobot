package com.autodesk.bsm.pelican.ui.pages.coreproducts;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

/**
 * this class represents POM for Edit functionality of Core Product
 *
 * @author mandas
 */
public class EditCoreProductPage extends GenericDetails {

    public EditCoreProductPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * Navigate to Edit Core Product page and edit a Core Product.
     *
     * @param externalKey - String
     * @return CoreProductDetailPage
     */
    public CoreProductDetailPage editCoreProduct(final String externalKey) {
        setExternalKey(externalKey);
        submit();
        return super.getPage(CoreProductDetailPage.class);
    }
}
