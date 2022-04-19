package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for Audit Log Reports page.
 *
 * @author t_joshv
 */
public class AuditLogReportPage extends GenericDetails {

    public AuditLogReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogReportPage.class.getSimpleName());

    @FindBy(id = "input-startDate")
    private WebElement changeDateFromInput;

    @FindBy(id = "input-endDate")
    private WebElement changeDateToInput;

    @FindBy(id = "entityType")
    private WebElement entityTypeSelect;

    @FindBy(id = "input-entityId")
    private WebElement objectIdInput;

    @FindBy(id = "input-userId")
    private WebElement userIdInput;

    /**
     * Method to set change date from.
     *
     * @param changeDateFrom : Start Date for Audit log
     */
    private void setChangeDateFrom(final String changeDateFrom) {
        getActions().setText(changeDateFromInput, changeDateFrom);
        LOGGER.info("Set Change Start date: " + changeDateFrom);
    }

    /**
     * Method to set change date to.
     *
     * @param changeDateTo : End Date for Audit log
     */
    private void setChangeDateTo(final String changeDateTo) {
        getActions().setText(changeDateToInput, changeDateTo);
        LOGGER.info("Set Change End date: " + changeDateTo);
    }

    /**
     * Method to set entity type.
     *
     * @param entityType : Set Entity type from Subscription plan/ Feature/ Basic Offering
     */
    private void setEntityType(final String entityType) {
        getActions().selectByValue(entityTypeSelect, entityType);
        LOGGER.info("Set entity type: " + entityType);
    }

    /**
     * Method to set object id.
     *
     * @param objectId : Entity Id of Subscription plan/ Feature/ Basic Offering
     */
    private void setObjectId(final String objectId) {
        getActions().setText(objectIdInput, objectId);
        LOGGER.info("Set entity Id: " + objectId);
    }

    /**
     * Method to set userId.
     *
     * @param userId : User Id
     */
    private void setUserId(final String userId) {
        getActions().setText(userIdInput, userId);
        LOGGER.info("Set user id: " + userId);
    }

    /**
     * Method to navigate to audit log report page.
     */
    private void navigateToAuditLogReportsPage() {
        final String url = getAuditLogReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

    /**
     * Method to generate Report according to input value.
     *
     * @param changeDateFrom : Start Date for Audit log
     * @param changeDateTo : End Date for Audit log
     * @param entityType : set entity type
     * @param objectId : set entity id
     * @param userId : set userId
     * @return AuditLogReportResultPage
     */
    public AuditLogReportResultPage generateReport(final String changeDateFrom, final String changeDateTo,
        final String entityType, final String objectId, final String userId, final boolean isDownload) {

        navigateToAuditLogReportsPage();
        Util.waitInSeconds(TimeConstants.ONE_SEC);

        if (changeDateFrom != null) {
            setChangeDateFrom(changeDateFrom);
        }
        if (changeDateTo != null) {
            setChangeDateTo(changeDateTo);
        }
        if (userId != null) {
            setUserId(userId);
        }
        if (entityType != null) {
            setEntityType(entityType);
        }
        if (objectId != null) {
            setObjectId(objectId);
        }

        if (isDownload) {
            selectViewDownloadAction(PelicanConstants.DOWNLOAD);
        }

        LOGGER.info("Generating AuditLogReport using parameters.");
        submit(TimeConstants.ONE_SEC);

        // TODO : remove below line after pagination issue get fixed.
        final AuditLogReportResultPage auditLogReportResultPage = super.getPage(AuditLogReportResultPage.class);
        auditLogReportResultPage.navigateToFirstPage();

        return auditLogReportResultPage;
    }

    /**
     * Method to get url of Audit log report page.
     *
     * @return String (page url)
     */
    private String getAuditLogReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.AUDIT_LOG_REPORT.getForm();
    }

}
