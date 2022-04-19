package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is a page object of Product Line Result Page
 *
 * @author Shweta Hegde
 */
public class ProductLineSearchResultPage extends GenericGrid {

    public ProductLineSearchResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = ".none-found")
    private WebElement noneFound;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLineSearchResultPage.class.getSimpleName());

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
     * This method return the existence of non found text
     *
     * @return none found existence
     */
    public boolean isNoneFoundPresent() {
        boolean isNoneFoundPresent = false;
        try {
            if (noneFound.isDisplayed()) {
                isNoneFoundPresent = true;
                LOGGER.info("none found exist");
            }
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("none found doesn't exist");
        }
        return isNoneFoundPresent;
    }

    /**
     * This method returns the column values of Active in the search results page
     *
     * @return List<String>
     */

    public List<String> getColumnValuesOfActive() {
        return (getColumnValues("Active"));
    }
}
