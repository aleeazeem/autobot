package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is a page class for upload status
 *
 * @author Muhammad
 */
public class UploadProductLineStatusPage extends GenericGrid {

    public UploadProductLineStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadProductLineStatusPage.class.getSimpleName());

    /**
     * method to get column values of status
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfStatus() {
        LOGGER.info("Returning all values of column Status");
        return getColumnValues(PelicanConstants.STATUS_FIELD);
    }

    /**
     * method to get column values of errors
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfErrors() {
        LOGGER.info("Returning all values of column Errors");
        return getColumnValues(PelicanConstants.ERRORS_FIELD);
    }
}
