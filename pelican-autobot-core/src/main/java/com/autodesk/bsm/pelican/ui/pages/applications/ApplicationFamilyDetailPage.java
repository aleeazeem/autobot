package com.autodesk.bsm.pelican.ui.pages.applications;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is for application family details
 *
 * @author Shweta Hegde
 */
public class ApplicationFamilyDetailPage extends GenericDetails {

    public ApplicationFamilyDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "Edit")
    private WebElement editButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFamilyDetailPage.class.getSimpleName());

    private void navigateToApplicationFamilyDetail() {

        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.APPLICATION_FAMILY.getForm() + "/"
            + AdminPages.SHOW.getForm() + "?id=" + getEnvironment().getAppFamilyId();
        LOGGER.info("Navigating to " + url);
        getDriver().get(url);
    }

    /**
     * This method navigates to the application family and return the details
     *
     * @return GenericDetails
     */
    public GenericDetails getApplicationFamilyDetail() {

        navigateToApplicationFamilyDetail();
        return super.getPage(GenericDetails.class);
    }

    /**
     * This methods clicks 'edit' on the application family
     */
    public void clickEdit() {

        editButton.click();
        LOGGER.info("Application family is opened for editing");
    }
}
