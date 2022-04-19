package com.autodesk.bsm.pelican.ui.pages.admintool;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

/**
 * This page is the home page of Admin tool
 *
 * @author Shweta Hegde
 */
public class HomePage extends GenericDetails {

    public HomePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method returns the url home page url
     *
     * @return String
     */
    public String getHomePageUrl(final EnvironmentVariables environmentVariables) {
        return environmentVariables.getAdminUrl() + "/";
    }
}
