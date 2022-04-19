package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the Edit Price List Page
 *
 * @author t_joshv
 */
public class EditPriceListPage extends GenericDetails {

    public EditPriceListPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPriceListPage.class.getSimpleName());

    /**
     * Edit Store.
     *
     * @param name
     * @param externalKey
     */
    public void editStore(final String name, final String externalKey) {
        setName(name);
        setExternalKey(externalKey);
    }

    /**
     * This method will click on save changes after editing the Price List in admin tool.
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnSubmit() {
        LOGGER.info("Click on Submit Button");
        submit();

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * This method will click on cancel button for editing the Price List in admin tool.
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnCancel() {
        LOGGER.info("Click on Cancel Button");
        cancel();

        return super.getPage(StoreDetailPage.class);
    }
}
