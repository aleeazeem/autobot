package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for Find Product Line Page
 *
 * @author Shweta Hegde
 */
public class FindProductLinePage extends GenericDetails {

    public FindProductLinePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FindProductLinePage.class.getSimpleName());

    private void navigateToFindPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PRODUCT_LINE.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * This method returns search result page when no id is provided
     *
     * @return ProductLineSearchResultPage
     */
    public ProductLineSearchResultPage findByValidId() {
        navigateToFindPage();
        submit(0);
        return super.getPage(ProductLineSearchResultPage.class);
    }

    /**
     * Navigate to find by id page to search for Product Line by id
     *
     * @param id
     * @return ProductLineDetailsPage
     */
    public ProductLineDetailsPage findByValidId(final String id) {
        navigateToFindPage();
        setId(id);
        submit(0);
        return super.getPage(ProductLineDetailsPage.class);
    }

    /**
     * Navigate to find by ext key page to search for ProductLine by Ext Key
     *
     * @param externalKey
     * @return ProductLineDetailsPage
     */
    public ProductLineDetailsPage findByValidExternalKey(final String externalKey) {
        navigateToFindPage();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        submit(1);
        return super.getPage(ProductLineDetailsPage.class);
    }

    /**
     * This method tries to find product line by invalid id but error is thrown or none found
     *
     * @param id
     * @return ProductLineSearchResultPage
     */
    public ProductLineSearchResultPage findByInValidId(final String id) {
        navigateToFindPage();
        setId(id);
        submit(0);
        return super.getPage(ProductLineSearchResultPage.class);
    }

    /**
     * Navigate to find product line by external key page
     *
     * @param externalKey
     * @return productLineSearchResultPage
     */
    public ProductLineSearchResultPage findByNonExistingExternalKey(final String externalKey) {
        navigateToFindPage();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        submit(1);
        return super.getPage(ProductLineSearchResultPage.class);
    }

    /**
     * This method tries to find product line by invalid id
     *
     * @param externalKey
     * @return ProductLineSearchResultPage
     */
    public ProductLineSearchResultPage findByInValidExternalKey(final String externalKey) {
        navigateToFindPage();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        submit(1);
        return super.getPage(ProductLineSearchResultPage.class);
    }

    /**
     * This is the method which will return the error on find store type
     *
     * @return error message
     */
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
