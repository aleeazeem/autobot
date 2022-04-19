package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This is a page class for the role search results in the admin tool
 *
 * @author vineel
 */
public class RoleSearchResultsPage extends GenericDetails {

    public RoleSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@class='results normal']//tbody//td")
    private WebElement roleSearchResult;

    /**
     * This method will return the text from a role search result
     */
    public String getRoleSearchResultsText() {

        return roleSearchResult.getText();
    }

    /**
     * This method will click on the role search result and navigate to the role detail page
     */
    public RoleDetailPage viewFoundRole(final GenericGrid genericGrid) {

        genericGrid.selectResultRow(1);

        return super.getPage(RoleDetailPage.class);

    }

}
