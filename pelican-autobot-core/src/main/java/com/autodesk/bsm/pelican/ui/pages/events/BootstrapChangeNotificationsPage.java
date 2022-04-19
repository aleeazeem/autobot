package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for the bootstrap change notifications page in the admin tool
 *
 * @author yerragv
 */
public class BootstrapChangeNotificationsPage extends GenericDetails {

    @FindBy(id = "entityType")
    private WebElement entityTypeSelect;

    @FindBy(id = "changeNotificationType")
    private WebElement changeNotificationTypeElement;

    @FindBy(id = "filterType")
    private WebElement filterTypeSelect;

    @FindBy(id = "requester")
    private WebElement requesterTypeSelect;

    @FindBy(id = "channel")
    private WebElement channelTypeSelect;

    @FindBy(id = "input-firstId")
    private WebElement inputFirstId;

    @FindBy(id = "input-lastId")
    private WebElement inputLastId;

    @FindBy(id = "input-startDate")
    private WebElement inputStartDate;

    @FindBy(id = "input-endDate")
    private WebElement inputEndDate;

    @FindBy(id = "startHour")
    private WebElement startHourSelect;

    @FindBy(id = "endHour")
    private WebElement endHourSelect;

    @FindBy(id = "startMinute")
    private WebElement startMinuteSelect;

    @FindBy(id = "endMinute")
    private WebElement endMinuteSelect;

    @FindBy(id = "startSecond")
    private WebElement startSecondSelect;

    @FindBy(id = "endSecond")
    private WebElement endSecondSelect;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(BootstrapChangeNotificationsPage.class.getSimpleName());

    public BootstrapChangeNotificationsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * This is a method to navigate to the bootstrap change notifications page
     */
    private void navigateToBootstrapChangeNotificationsPage() {

        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.EVENT.getForm() + "/"
            + AdminPages.BOOTSTRAP_FORM.getForm();
        LOGGER.info("Navigating to " + url);

        getDriver().get(url);
    }

    /**
     * This is a method to publish bootstrap entities
     *
     * @param entity
     * @param filter
     * @param requester
     * @param channel
     * @param startId
     * @param endId
     * @param startDate
     * @param endDate
     * @param changeNotificationType TODO
     * @return BootstrapEventStatusPage
     */
    public BootstrapEventStatusPage publishBootStrapEntities(final String entity, final String filter,
        final String requester, final String channel, final String startId, final String endId, final String startDate,
        final String endDate, final String changeNotificationType) {

        navigateToBootstrapChangeNotificationsPage();

        if (entity != null) {
            selectEntity(entity);
        }

        if (StringUtils.isNotEmpty(changeNotificationType)) {
            selectChangeNotificationType(changeNotificationType);
        }

        if (filter != null && filter.equalsIgnoreCase(PelicanConstants.ID_RANGE)) {
            selectFilterType(filter);
            setIdRange(startId, endId);
        }

        if (filter != null && filter.equalsIgnoreCase(PelicanConstants.DATE_RANGE)) {
            selectFilterType(filter);
            setDateRange(startDate, endDate);
        }

        if (requester != null) {
            selectRequester(requester);
        }

        if (channel != null) {
            selectChannel(channel);
        }
        submit(TimeConstants.ONE_SEC);

        return super.getPage(BootstrapEventStatusPage.class);
    }

    /**
     * This is a method to select entity type on the page
     *
     * @param entity
     */
    private void selectEntity(final String entity) {

        getActions().select(entityTypeSelect, entity);
        LOGGER.info("Selecting the entity type as " + entity);

    }

    private void selectChangeNotificationType(final String changeNotificationType) {
        getActions().select(changeNotificationTypeElement, changeNotificationType);
        LOGGER.info("Selecting the change notification type as " + changeNotificationType);
    }

    /**
     * This is a method to select the filter type
     *
     * @param filter
     */
    private void selectFilterType(final String filter) {

        getActions().select(filterTypeSelect, filter);
        LOGGER.info("Selecting the filter type as " + filter);

    }

    /**
     * This is a method to select the requester on the page
     *
     * @param requestor
     */
    private void selectRequester(final String requester) {

        getActions().select(requesterTypeSelect, requester);
        LOGGER.info("Selecting the requester type as " + requester);

    }

    /**
     * This is a method to select channel type
     *
     * @param channel
     */
    private void selectChannel(final String channel) {

        getActions().select(channelTypeSelect, channel);
        LOGGER.info("Selecting the channel type as " + channel);
    }

    /**
     * This is the method to set the start id range and end id range
     *
     * @param startIdRange
     * @param endIdRange
     */
    private void setIdRange(final String startIdRange, final String endIdRange) {

        getActions().setText(inputFirstId, startIdRange);
        LOGGER.info("Set the start id to " + startIdRange);
        getActions().setText(inputLastId, endIdRange);
        LOGGER.info("Set the end id to " + endIdRange);

    }

    /**
     * This is a method to set the start date range and end date range
     *
     * @param startDateRange
     * @param endDateRange
     */
    private void setDateRange(final String startDateRange, final String endDateRange) {

        getActions().setText(inputStartDate, startDateRange);
        LOGGER.info("Set the start id to " + startDateRange);
        getActions().setText(inputEndDate, endDateRange);
        LOGGER.info("Set the end id to " + endDateRange);

    }

}
