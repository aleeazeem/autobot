package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

public class StoreSearchResultsPage extends GenericGrid {

    public StoreSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to return total store count.
     *
     * @return total store count.
     */
    public int getTotalItemsInStoreSearchResultsPage() {
        return (getTotalItems());
    }

}
