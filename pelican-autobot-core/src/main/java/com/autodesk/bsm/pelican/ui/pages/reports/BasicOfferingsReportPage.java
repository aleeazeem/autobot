package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Page object for Admin Tool's Basic Offering Report - on the main tab
 *
 * @author Kishor
 */
public class BasicOfferingsReportPage extends GenericDetails {

    public BasicOfferingsReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Status new check box
    @FindBy(name = "includeNew")
    private WebElement statusNewCheckbox;

    // Status active check box
    @FindBy(name = "includeActive")
    private WebElement statusActiveCheckbox;

    // Status active check box
    @FindBy(name = "includeCanceled")
    private WebElement statusCanceledCheckbox;

    // find offerings button
    @FindBy(className = "submit")
    private WebElement submitButton;

    @FindBy(css = ".intro .errors")
    private WebElement errorText;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    @FindBy(css = "#bd > div:last-child")
    private WebElement reportData;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicOfferingsReportPage.class.getSimpleName());

    // method to activate the Active check box
    public void activateNewCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusNewCheckbox.getAttribute("checked") == null) {
            statusNewCheckbox.click();

        } else {
            LOGGER.info("Checkbox *New* is already checked.");
        }
    }

    // method to deActivate the New check box
    public void deactivateNewCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusNewCheckbox.getAttribute("checked") != null) {
            statusNewCheckbox.click();

        } else {
            LOGGER.info("Checkbox *New* is already unchecked.");
        }
    }

    // method to activate the Active check box
    public void activateActiveCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusActiveCheckbox.getAttribute("checked") == null) {
            statusActiveCheckbox.click();

        } else {
            LOGGER.info("Checkbox *Active* is already checked.");
        }
    }

    // method to deActivate the Active check box
    public void deactivateActiveCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusActiveCheckbox.getAttribute("checked") != null) {
            statusActiveCheckbox.click();

        } else {
            LOGGER.info("Checkbox *Active* is already unchecked.");
        }
    }

    // method to Activate the Canceled check box
    public void activateCanceledCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusCanceledCheckbox.getAttribute("checked") == null) {
            statusCanceledCheckbox.click();

        } else {
            LOGGER.info("Checkbox *Canceled* is already checked.");
        }
    }

    // method to Activate the Canceled check box
    public void deactivateCanceledCheckbox() {
        if (!isPageValid()) {
            navigateToPage();
        }
        if (statusCanceledCheckbox.getAttribute("checked") != null) {
            statusCanceledCheckbox.click();

        } else {
            LOGGER.info("Checkbox *Canceled* is already unchecked.");
        }
    }

    public String getH3ErrorMessage() {
        final String message = errorText.getText();
        LOGGER.info("Error message is '" + message);
        return message;
    }

    /**
     * Assume the first line is the data
     */
    public String getReportHeader() {

        String header = null;
        final String data = reportData.getText();

        final BufferedReader rdr = new BufferedReader(new StringReader(data));
        try {
            header = rdr.readLine();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
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
     * Data is everything after the first line
     */
    public List<String> getReportData() {
        final List<String> lines = new ArrayList<>();

        final String data = reportData.getText();
        final BufferedReader rdr = new BufferedReader(new StringReader(data));

        // skip the header
        String line;
        try {
            line = rdr.readLine();
            boolean done = false;

            while (!done) {
                line = rdr.readLine();
                if (line == null) {
                    done = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * Find the data from the Order Date column
     */
    public List<String> getReportValues(final String columnName) {
        final List<String> orderDates = new ArrayList<>();

        // get column index for order date
        int columnIndex = -1;
        final String header = getReportHeader();
        final String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex < 0) {
            throw new RuntimeException(
                "Unable to find header '" + columnName + "' in Basic Offering Report\n" + header);
        }

        // get values
        final List<String> lines = getReportData();
        for (final String line : lines) {
            final String[] rowData = line.split(",");
            orderDates.add(rowData[columnIndex]);
        }
        return orderDates;
    }

    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.BASIC_OFFERING_REPORT.getForm();
    }

    public void navigateToPage() {
        final String url = getUrl();
        getDriver().get(url);
        LOGGER.info("Navigating to '" + url + "'");
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        LOGGER.info("Page title after navigating: " + getTitle());
    }

    public void selectStoreFieldsInGenerateOfferingReport() {
        selectStoreType("AUTO_OFFERING_STORE_TYPE");
        selectStore("AUTO_OFFERING_REPORT_STORE");
    }

    /**
     * Determine if we're on the Basic Offering Report page by getting the current url as well as the header = "Generate
     * Basic Offering Report"
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
                // ignore
            }
        }
        return pageDisplayed;
    }

    public void uploadBasicOffering() {
        final String uploadUrl = getEnvironment().getAdminUrl() + "/offering/importForm";
        final WebDriver driver = getDriver();
        driver.get(uploadUrl);
        final String fileName = System.getProperty("user.dir") + "/src/pelican/testdata/basic_offering_upload.csv";
        LOGGER.info("FileName : " + fileName);
        driver.findElement(By.id("input-file")).sendKeys(fileName);
        driver.findElement(By.className("submit")).click();

        LOGGER.info("Basic Offering Imported");
        // navigate back to basic offering reports page
        if (!isPageValid()) {
            navigateToPage();
        }

    }
}
