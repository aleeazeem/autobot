package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Page object for Admin Tool's Offerings with Active Promotion Report
 *
 * @author Sunitha
 */
public class OfferingsActivePromotionReportPage extends GenericDetails {
    public OfferingsActivePromotionReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "action")
    private WebElement viewOrDownloadSelect;

    @FindBy(className = "submit")
    private WebElement submitButton;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    @FindBy(css = "#bd > div:last-child")
    private WebElement reportData;

    @FindBy(xpath = "(//table/tbody/tr)[1]/td[1]")
    private WebElement firstActivePromotion;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(OfferingsActivePromotionReportPage.class.getSimpleName());

    /**
     * Method to select view or download dropdwon.
     */
    public void selectviewDownload(final String viewDownload) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Select view Download to '" + viewDownload + "'");
        getActions().select(viewOrDownloadSelect, viewDownload);
    }

    /**
     * Method to click Generate report button.
     */
    public void submit(final long time) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Click Submit button.");
        submitButton.click();
    }

    /**
     * Method to navigate to the page.
     */
    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Determine if we're on the Offerings with Active Promotion Report page by getting the current url as well as the
     * header.
     *
     * @return true if we're on the page. Otherwise false
     */
    public boolean isPageValid() {
        boolean pageDisplayed = false;

        if (getDriver().getCurrentUrl().equalsIgnoreCase(getUrl())) {
            try {
                reportContainer.isDisplayed();
                pageDisplayed = true;
            } catch (final NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return pageDisplayed;
    }

    /**
     * Assume the first line is the data.
     *
     * @return Report Header
     */
    public String getReportHeader() {

        String header = null;
        final String data = reportData.getText();
        final BufferedReader rdr = new BufferedReader(new StringReader(data));
        try {
            header = rdr.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rdr.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return header;
    }

    /**
     * Method to click on first active promotion report.
     */
    public void clickOnFirstActivePromotion() {
        LOGGER.info("Click on first Active promotion");
        getActions().click(firstActivePromotion);
    }

    /**
     * Method to get URL.
     */
    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.OFFERINGS_PROMOTION_REPORT.getForm();
    }

}
