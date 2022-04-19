package com.autodesk.bsm.pelican.ui.pages.licensingmodel;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is POM for Find Licensing Model
 *
 * @author mandas
 */
public class FindLicensingModelPage extends GenericDetails {

    public FindLicensingModelPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FindLicensingModelPage.class.getSimpleName());

    /**
     * Perform find Licensing Model without ID, default find.
     *
     * @return LicensingModelSearchResultsPage
     */
    public LicensingModelSearchResultsPage findByEmptyId() {

        navigateToFindForm();
        setId(PelicanConstants.EMPTY_STRING);
        submit();

        return super.getPage(LicensingModelSearchResultsPage.class);
    }

    /**
     * Navigate to find by id page to search for Licensing Model by Id.
     *
     * @param id -> Licensing Model id
     * @return LicensingModelDetailPage
     */
    public LicensingModelDetailPage findById(final String id) {

        navigateToFindForm();
        setId(id);
        submit(0);

        return super.getPage(LicensingModelDetailPage.class);
    }

    /**
     * Navigate to find by External key page to search for Licensing Model.
     *
     * @param externalKey -> Licensing Model externalKey
     * @return LicensingModelDetailPage
     */
    public LicensingModelDetailPage findByExternalKey(final String externalKey) {

        navigateToFindForm();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        submit(1);

        return super.getPage(LicensingModelDetailPage.class);
    }

    /**
     * This method will navigate to the find LicensingModel page.
     */
    private void navigateToFindForm() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.LICENSING_MODEL.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
