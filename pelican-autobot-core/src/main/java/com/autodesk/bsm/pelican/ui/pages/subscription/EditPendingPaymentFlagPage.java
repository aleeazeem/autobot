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
 * This is the page object for Subscription - Edit Pending Payment Flag Page in Pelican admin tool.
 *
 * @author mandas
 */

public class EditPendingPaymentFlagPage extends GenericDetails {

    public EditPendingPaymentFlagPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-pendingPayment")
    private WebElement pendingPaymentCheckBox;

    @FindBy(id = "input-note")
    private WebElement inputNoteTextField;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPendingPaymentFlagPage.class.getSimpleName());

    /**
     * Method returns boolean value based on pendingPaymentCheckBox presence
     */
    public Boolean getPendingPaymentFlagStatus() {
        return isElementPresent(pendingPaymentCheckBox);
    }

    /**
     * Method to Check the Pending Payment Flag and Input text in Notes text area for the Edit Pending Payment
     */
    public SubscriptionDetailPage checkAndEnterNotesInPendingPaymentTextAreaAndSubmit(final String text) {
        pendingPaymentCheckBox.click();
        LOGGER.info("Entered EBSO Notes for Edit pendingpayment flag: " + text);
        inputNoteTextField.sendKeys(text);
        submit(TimeConstants.ONE_SEC);
        return getPage(SubscriptionDetailPage.class);
    }

}
