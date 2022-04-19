package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.PriceList;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PriceListGrid extends GenericGrid {

    public PriceListGrid(final String parentCssSelector, final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(parentCssSelector, driver, environmentVariables);
    }

    /**
     * Get price list by row. Start at row #0
     *
     * @return PriceList
     */
    public PriceList getPriceList(final int row) {

        final int totalRows = super.getTotalRows();
        final List<WebElement> totalElements =
            getDriver().findElements(By.cssSelector(parentElementSelector + ".results > tbody > tr"));
        if (row > totalRows) {
            throw new RuntimeException("Requested row, " + row + ", is greater than the total rows of " + totalRows);
        }

        final List<WebElement> fields = totalElements.get(row).findElements(By.tagName("td"));
        if (fields.size() != 4) {
            throw new RuntimeException("Price list grid should have 4 columns, but found " + fields.size());
        }

        final PriceList priceList = new PriceList();
        priceList.setId(fields.get(0).getText());
        priceList.setCurrency(Currency.getByValue(fields.get(1).getText()));
        priceList.setName(fields.get(2).getText());
        priceList.setExternalKey(fields.get(3).getText());
        return priceList;

    }
}
