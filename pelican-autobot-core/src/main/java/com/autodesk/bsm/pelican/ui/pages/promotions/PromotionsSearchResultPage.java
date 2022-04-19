package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PromotionsSearchResultPage extends GenericGrid {

    public PromotionsSearchResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    public PromotionDetailsPage selectResultRow(final int row) {

        final List<WebElement> totalElements =
            getDriver().findElements(By.cssSelector(parentElementSelector + ".results > tbody > tr > td:nth-child(1)"));
        if (row > totalElements.size()) {
            throw new RuntimeException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }
        if (row < 1) {
            totalElements.get(row).click();
        } else {
            totalElements.get(row - 1).click();
        }
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(PromotionDetailsPage.class);
    }
}
