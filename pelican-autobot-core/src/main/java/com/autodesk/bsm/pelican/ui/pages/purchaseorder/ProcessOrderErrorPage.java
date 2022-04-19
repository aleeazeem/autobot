package com.autodesk.bsm.pelican.ui.pages.purchaseorder;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

/**
 * This class is for Process PO error page. Error message methods are derived from GenericDetails.
 *
 * @author Shweta Hegde
 */
public class ProcessOrderErrorPage extends GenericDetails {

    public ProcessOrderErrorPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

}
