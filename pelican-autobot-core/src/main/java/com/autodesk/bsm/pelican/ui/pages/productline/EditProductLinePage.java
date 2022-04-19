package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for Edit Product Line Page
 *
 * @author Shweta Hegde TODO : Add details when edit related testcases are added
 */
public class EditProductLinePage extends GenericDetails {

    public EditProductLinePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EditProductLinePage.class.getSimpleName());

}
