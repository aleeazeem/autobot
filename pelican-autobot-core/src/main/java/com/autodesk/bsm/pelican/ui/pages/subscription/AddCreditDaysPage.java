package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object class for Credit Discounts page You can navigate to this from an Subscription Page Pelican
 * Admin> Subscriptions >Subscription >Add/Remove Credit days Web elements initiated may not be complete. Please add
 * relevant web elements if missing and required for your automation!
 *
 * @author Muhammad
 */
public class AddCreditDaysPage extends GenericDetails {

    public AddCreditDaysPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-creditDays")
    private WebElement creditDaysInput;

    @FindBy(id = "input-note")
    private WebElement creditNoteInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCreditDaysPage.class.getSimpleName());

    /*
     * Navigate to the url
     */
    public void addOrRemoveCreditDaysInSub(final String id, final String days) {
        navigateToAddDaysPage(id);
        setCreditDays(days);
    }

    public SubscriptionDetailPage addOrRemoveCreditDaysButton() {
        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Clicked on submit button");
        return super.getPage(SubscriptionDetailPage.class);
    }

    private void navigateToAddDaysPage(final String id) {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm()
            + "/showAddCreditDaysForm?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    private void setCreditDays(final String days) {
        getActions().setText(creditDaysInput, days);
    }

    private void setCreditNote(final String value) {
        LOGGER.info("Set credit note :" + value);
        getActions().setText(creditNoteInput, value);
    }

    /**
     * This method adds/removes credit days, notes and clicks on Add Credit Days and returns SubscriptionDetailPage
     *
     * @param subsId
     * @param creditDays
     * @param creditNote
     * @return SubscriptionDetailPage
     */
    public SubscriptionDetailPage addCreditDays(final String subsId, final int creditDays, final String creditNote) {
        navigateToAddDaysPage(subsId);
        setCreditDays(Integer.toString(creditDays));
        setCreditNote(creditNote);
        return addOrRemoveCreditDaysButton();
    }
}
