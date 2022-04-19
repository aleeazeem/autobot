package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for reduce seats Page in Pelican admin tool This page can be viewed in pelican Admin under
 * Subscriptions tab / Subscription > reduce seats link. Reduce seats link is only visible for active and delinquent
 * subscriptions, subscriptions which has quantity more than 1 and renewal date of subscription should not be set to
 * today's date
 *
 * @author Muhammad
 */
public class ReduceSeatsPage extends GenericDetails {

    public ReduceSeatsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-qtyToReduce")
    private WebElement quantityToReduceInput;

    @FindBy(id = "input-note")
    private WebElement noteInput;

    @FindBy(id = "form-reduceSeatsForm")
    private WebElement currentQuantityToReduceSeats;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReduceSeatsPage.class.getSimpleName());

    /**
     * This method sets number of seats.
     *
     * @param seats
     */
    private void setQuantityToReduce(final String seats) {
        if (seats != null) {
            getActions().setText(quantityToReduceInput, seats);
            LOGGER.info("Seats are set to: " + seats);
        }
    }

    /**
     * This method sets notes.
     *
     * @param note
     */
    private void setNote(final String note) {
        if (note != null) {
            getActions().setText(noteInput, note);
            LOGGER.info("Note is set to: " + note);
        }
    }

    /**
     * This method clicks on reduce seats button.
     */
    private void clickOnReduceSeatsButton() {
        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Submit button has been clicked");
    }

    /**
     * Method to add seats, note and click on submit button
     *
     * @param seats
     * @param note
     * @return subscription details page class
     */
    public SubscriptionDetailPage clickOnReduceSeats(final String seats, final String note) {
        setQuantityToReduce(seats);
        setNote(note);
        clickOnReduceSeatsButton();
        return getPage(SubscriptionDetailPage.class);
    }

    /**
     * This method clicks on cancel button.
     *
     * @return subscription details page class
     */
    public SubscriptionDetailPage clickOnCancelButton() {
        cancel();
        LOGGER.info("Cancel button has been clicked");
        return getPage(SubscriptionDetailPage.class);
    }

    /**
     * This method gets the current value of quantity to reduce seats.
     *
     * @return string
     */
    public String getCurrentQuantityToReduceSeats() {
        final String currentValueOfQuantityToReduceSeats = currentQuantityToReduceSeats.getText();
        LOGGER.info("Current value of quantity to reduce seats is: " + currentValueOfQuantityToReduceSeats);
        return currentValueOfQuantityToReduceSeats;
    }
}
