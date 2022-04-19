package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

public class FeatureSearchResultsPage extends GenericGrid {

    public FeatureSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    public GenericGrid getFeatureSearchResults() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * This is a method to get the column values for a specified column
     */
    public List<String> getColumnValuesOfActiveStatus() {
        return (getColumnValues(PelicanConstants.ACTIVE_FIELD));
    }

}
