package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * This is the page class for the Store Type module Search Results Page
 *
 * @author vineel
 */
public class StoreTypeSearchResultsPage extends GenericGrid {

    public StoreTypeSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method returns the column values of Id header in the search results page
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfId() {
        return (getColumnValues("ID"));
    }

    /**
     * This method returns the column values of External Key header in the search results page
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfExternalKey() {
        return (getColumnValues("External Key"));
    }

    /**
     * This method returns the column values of Name header in the search results page
     *
     * @return List<String>
     */

    public List<String> getColumnValuesOfName() {
        return (getColumnValues("Name"));
    }

    /**
     * This method returns the column values of Application Family header in the search results page
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfApplicationFamily() {
        return (getColumnValues("Application Family"));
    }

    /**
     * This method returns the column header values in the search results page
     *
     * @return List<String>
     */
    public List<String> getColumnHeadersInStoreTypeSearchResultsPage() {
        return (getColumnHeaders());
    }

    /**
     * This method will click on the desired row in the store type search results page
     *
     * @return StoreTypeDetailsPage Object
     */
    public StoreTypeDetailsPage selectResultRowInStoreTypeSearchResultsPage(final int row) {
        selectResultRow(row);

        return super.getPage(StoreTypeDetailsPage.class);
    }

    /**
     * This method will return the Id value in the store type search results page
     *
     * @return Id value as String
     */
    public String getIdValue() {
        return (getValueByField("ID"));
    }

    public int getTotalItemsInStoreTypeSearchResultsPage() {
        return (getTotalItems());
    }

}
