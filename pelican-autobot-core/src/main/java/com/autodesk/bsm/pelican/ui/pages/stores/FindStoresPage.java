package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the Find Stores Page which can be accessed from Stores --> Stores --> Find.
 *
 * @author t_joshv
 */
public class FindStoresPage extends GenericDetails {

    public FindStoresPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FindStoresPage.class.getSimpleName());

    @FindBy(css = ".form-group-labels > h3:nth-child(1)")
    private WebElement findByIdLink;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByExtKeyLink;

    /**
     * Navigate to find by id page to search for Store type by Id.
     *
     * @param id Store id
     * @return StoreDetailPage
     */
    public StoreDetailPage findById(final String id) {

        navigateToFindForm();
        setId(id);
        submit(0);

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * Navigate to find by external key page to search for Store by External Key.
     *
     * @param extKey Store extKey
     * @return StoreDetailPage
     */
    public StoreDetailPage findByValidExtKey(final String extKey) {

        navigateToFindForm();
        findByExtKeyLink.click();
        setExternalKey(extKey);
        submit(1);

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * Navigate to find by id page to search for Store by Id
     *
     * @param
     * @return StoreSearchResultsPage
     */
    public StoreSearchResultsPage findByIdDefaultSearch() {

        navigateToFindForm();
        findByIdLink.click();
        submit(0);

        return super.getPage(StoreSearchResultsPage.class);
    }

    /**
     * This method will navigate to the find stores page.
     */
    private void navigateToFindForm() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.STORE.getForm() + "/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
