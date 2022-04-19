package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents edit subscription page
 *
 * @author Shweta Hegde
 */
public class EditSubscriptionPage extends GenericDetails {

    public EditSubscriptionPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-nextBillingDate")
    private WebElement nextBillingDateInput;

    @FindBy(id = "input-expirationDate")
    private WebElement expirationDateInput;

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "input-note")
    private WebElement notesInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditSubscriptionPage.class.getSimpleName());

    /**
     * This method is used to edit a subscription, either next billing date, expiration date or status.
     *
     * @param nextBillingDate
     * @param expirationDate
     * @param status
     * @param notes
     * @return SubscriptionDetailPage
     */
    public SubscriptionDetailPage editASubscription(final String nextBillingDate, final String expirationDate,
        final String status, final String notes) {

        setNextBillingDate(nextBillingDate);
        setExpirationDate(expirationDate);
        setStatus(status);
        setNotes(notes);
        submit(TimeConstants.ONE_SEC);
        // This code is added to make easier debugging of test cases related to Edit subscription
        try {
            final String error = getError();
            if (!error.isEmpty()) {
                LOGGER.error("Error occurred in edit subscription: " + error);
            }
        } catch (final Exception e) {
            LOGGER.info("Successfully edited the subscription");
        }
        return getPage(SubscriptionDetailPage.class);
    }

    private void setNextBillingDate(final String nextBillingDate) {
        if (nextBillingDate != null) {
            nextBillingDateInput.clear();
            nextBillingDateInput.sendKeys(nextBillingDate);
            LOGGER.info("Next Billing Date is changed to " + nextBillingDate);
        }
    }

    private void setExpirationDate(final String expirationDate) {
        if (expirationDate != null) {
            expirationDateInput.clear();
            expirationDateInput.sendKeys(expirationDate);
            LOGGER.info("Expiration Date is changed to " + expirationDate);
        }

    }

    private void setStatus(final String status) {
        if (status != null) {
            final Select select = new Select(statusSelect);
            select.selectByVisibleText(status);
            LOGGER.info("Status of the subscription is changed to " + status);
        }
    }

    private void setNotes(final String notes) {
        if (notes != null) {
            getActions().setText(notesInput, notes);
        }
    }
}
