package com.autodesk.bsm.pelican.ui.pages.apidoc;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API Doc page for platform
 *
 * @author mandas
 */
public class APIDocPage extends GenericDetails {

    @FindBy(xpath = "/html/body/h2[1]")
    private WebElement platformVersion;

    private static final Logger LOGGER = LoggerFactory.getLogger(APIDocPage.class.getSimpleName());

    public APIDocPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        PageFactory.initElements(driver, this); // init the page fields
    }

    /**
     * This method returns the Platform version from API Doc page
     *
     * @return String
     */
    public String getPlatformVersion(final String host) {
        getDriver().get(getPlatformUrl(host));
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return platformVersion.getText();
    }

    /**
     * Generic method for ALL logIn in Admin Tool
     */
    private String getPlatformUrl(final String host) {
        String url = null;
        if (host.equalsIgnoreCase(PelicanConstants.PLATFORM)) {
            url = getEnvironment().getAdminUrl().split("/admin")[0] + PelicanConstants.API_DOC_PARAMS;
        } else if (host.equalsIgnoreCase(PelicanConstants.TRIGGERS)) {
            url = getEnvironment().getTriggerUrl().split("/triggers-workers")[0] + PelicanConstants.API_DOC_PARAMS;
        }
        LOGGER.info("Platform URL: " + url);
        return url;
    }

}
