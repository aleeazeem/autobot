package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the Order Promotion Use Report which can be seen under Reports --> Promotion Reports.
 *
 * @author t_joshv
 */
public class OrderPromotionUseReportPage extends GenericDetails {

    public OrderPromotionUseReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-promotionId")
    private WebElement promotionIdInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPromotionUseReportPage.class.getSimpleName());

    /**
     * This is a method which will navigate to the Order Promotion Use Report Page
     */
    private void navigateToReportPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm() + "/"
            + AdminPages.ORDER_PROMOTION_USE_REPORT.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);

    }

    /**
     * This is the method to set the promotion id in the report
     */
    private void setPromotionId(final String promotionId) {
        getActions().setText(promotionIdInput, promotionId);
        LOGGER.info("Promotion Id is set:" + promotionId);
    }

    /**
     * This is a method which will select the view action on the report and generate the report
     */
    public void viewOrDownloadReport(final String promotionId, final String action) {
        navigateToReportPage();
        setPromotionId(promotionId);
        selectViewDownloadAction(action);
        submit(TimeConstants.ONE_SEC);
    }
}
