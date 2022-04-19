package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the Find Store Type Page
 *
 * @author vineel
 */
public class FindStoreTypePage extends GenericDetails {

    public FindStoreTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = ".form-group-labels > h3:nth-child(1)")
    private WebElement findByIdLink;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByExtKeyLink;

    @FindBy(css = ".errors")
    private WebElement errorHeading;

    @FindBy(css = ".error-message")
    private WebElement error;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindStoreTypePage.class.getSimpleName());

    /**
     * Navigate to find by id page to search for Store type by Id
     *
     * @param id Storetype id
     * @return StoreTypeDetailPage
     */
    public StoreTypeDetailsPage findByValidId(final String id) {

        navigateToFindForm();
        findByIdLink.click();
        setId(id);
        submit(0);

        return super.getPage(StoreTypeDetailsPage.class);
    }

    /**
     * Navigate to find by id page to search for Store type by Id
     *
     * @param id Storetype id
     * @return StoreTypeSearchResultsPage
     */
    public StoreTypeSearchResultsPage findByInvalidId(final String id) {

        navigateToFindForm();
        findByIdLink.click();
        setId(id);
        submit(0);

        return super.getPage(StoreTypeSearchResultsPage.class);
    }

    /**
     * Navigate to find by ext key page to search for Store by Ext Key
     *
     * @param extKey StoreType extKey
     * @return StoreTypeDetailsPage
     */
    public StoreTypeDetailsPage findByValidExtKey(final String extKey) {

        navigateToFindForm();
        findByExtKeyLink.click();
        setExternalKey(extKey);
        submit(1);

        return super.getPage(StoreTypeDetailsPage.class);
    }

    /**
     * Navigate to find by ext key page to search for Store type by ext key
     *
     * @param ext key Storetype ext key
     * @return StoreTypeSearchResultsPage
     */
    public StoreTypeSearchResultsPage findByInvalidExtKey(final String extKey) {

        navigateToFindForm();
        findByExtKeyLink.click();
        setExternalKey(extKey);
        submit(1);

        return super.getPage(StoreTypeSearchResultsPage.class);
    }

    /**
     * This is the method which will return the error on find store type
     */
    public String getErrorMessageOnFindPage() {
        return error.getText();
    }

    /**
     * This method will navigate to the find store type page
     */
    private void navigateToFindForm() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.STORE_TYPE.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);

    }
}
