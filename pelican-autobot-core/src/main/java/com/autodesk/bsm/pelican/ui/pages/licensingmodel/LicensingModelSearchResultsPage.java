package com.autodesk.bsm.pelican.ui.pages.licensingmodel;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

/**
 * This class is POM representation of Licensing Model Search Results Page
 *
 * @author mandas
 *
 */
public class LicensingModelSearchResultsPage extends GenericGrid {

    public LicensingModelSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    public GenericGrid getLicensingModelSearchResults() {
        return super.getPage(GenericGrid.class);
    }

}
