package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.ui.entities.CountryPriceList;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CountryGrid extends GenericGrid {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryGrid.class.getSimpleName());

    public CountryGrid(final String parentCssSelector, final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(parentCssSelector, driver, environmentVariables);
    }

    /**
     * Get price list by row. Start at row #0
     *
     * @return PriceList
     */
    public CountryPriceList getAssignedCountry(final int rowNumber) {

        final List<WebElement> row = getRow(rowNumber);

        final CountryPriceList assignedCountry = new CountryPriceList();
        assignedCountry.setId(row.get(0).getText());
        assignedCountry.setAssignedCountry(Country.getByDescription(row.get(1).getText()));
        assignedCountry.setAssignedPriceList(row.get(2).getText());
        assignedCountry.setCurrency(Currency.getByValue(row.get(3).getText()));
        return assignedCountry;
    }

    public void deleteAssignedCountry(final int rowNumber) {
        final List<WebElement> row = getRow(rowNumber);
        LOGGER.info("Delete row #" + rowNumber);
        final List<WebElement> linkCellElement = findChildElements(row.get(4), By.tagName("a"));
        linkCellElement.get(0).click();
        final ConfirmationPopup popup = getPage(ConfirmationPopup.class);
        popup.confirm();

        // Wait
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    private List<WebElement> getRow(final int row) {
        final List<WebElement> totalElements =
            getDriver().findElements(By.cssSelector(parentElementSelector + ".results > tbody > tr"));
        if (row > totalElements.size()) {
            throw new RuntimeException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }

        final List<WebElement> fields = totalElements.get(row).findElements(By.tagName("td"));
        if (fields.size() != 5) {
            throw new RuntimeException("Countries grid should have 5 columns, but found " + fields.size());
        }

        return fields;
    }
}
