package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object class for Cancel Subscription Page/pop up. This class should contain only controller, webelements which
 * are present in this page
 *
 * @author Shweta Hegde
 */
public class CancelSubscriptionPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelSubscriptionPage.class.getSimpleName());

    @FindBy(id = "cancel-later-radio")
    private WebElement cancelAtTheEndOfBillingRadioButton;

    @FindBy(id = "cancel-now-radio")
    private WebElement cancelImmediatelyRadioButton;

    @FindBy(id = "cancel-gdpr-radio")
    private WebElement cancelGdprRadioButton;

    @FindBy(id = "cancel-notes")
    private WebElement cancelNoteText;

    public CancelSubscriptionPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method selects the cancellation policy and cancels a subscription accordingly
     *
     * @param cancellationPolicy
     * @param notes
     * @return SubscriptionDetailPage
     */
    public SubscriptionDetailPage cancelASubscription(final CancellationPolicy cancellationPolicy, final String notes) {

        Util.waitInSeconds(TimeConstants.THREE_SEC);
        // Saving the parent window handle before switching to the new window
        final String parentWindowHandle = getDriver().getWindowHandle();
        final int numberOfHandles = getDriver().getWindowHandles().size();

        String handle = null;
        if (numberOfHandles == 1) {
            handle = getDriver().getWindowHandles().toArray()[0].toString();

        } else if (numberOfHandles == 2) {
            handle = getDriver().getWindowHandles().toArray()[1].toString();
        }

        getDriver().switchTo().window(handle);

        // Choose cancellation type
        if (cancellationPolicy == CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD) {
            getActions().click(cancelAtTheEndOfBillingRadioButton);
        } else if (cancellationPolicy == CancellationPolicy.IMMEDIATE_NO_REFUND) {
            getActions().click(cancelImmediatelyRadioButton);
        } else if (cancellationPolicy == CancellationPolicy.GDPR_CANCEL_IMMEDIATELY) {
            getActions().click(cancelGdprRadioButton);
        }

        // Set Cancel note if present
        getActions().setText(cancelNoteText, notes);

        // Confirm the cancellation
        getActions().click(confirmButton);
        LOGGER.info("Cancelling a subscription with option : {}", cancellationPolicy.getDisplayName());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        getDriver().switchTo().window(parentWindowHandle);

        return super.getPage(SubscriptionDetailPage.class);
    }

    /**
     * This method returns boolean depending on cancel radio visibility.
     *
     * @return boolean
     * @param cancellationPolicy
     */
    public boolean isRequiredCancelOptionDisplayed(final CancellationPolicy cancellationPolicy) {

        final int numberOfHandles = getDriver().getWindowHandles().size();

        String handle = null;
        if (numberOfHandles == 1) {
            handle = getDriver().getWindowHandles().toArray()[0].toString();

        } else if (numberOfHandles == 2) {
            handle = getDriver().getWindowHandles().toArray()[1].toString();
        }

        getDriver().switchTo().window(handle);

        WebElement requiredWebElement = null;
        switch (cancellationPolicy) {
            case IMMEDIATE_NO_REFUND:
                requiredWebElement = cancelImmediatelyRadioButton;
                break;
            case GDPR_CANCEL_IMMEDIATELY:
                requiredWebElement = cancelGdprRadioButton;
                break;
            default:
                requiredWebElement = cancelAtTheEndOfBillingRadioButton;
                break;
        }

        boolean isFound;
        try {
            isFound = requiredWebElement.isDisplayed();
        } catch (final NoSuchElementException e) {
            isFound = false;
        }
        return isFound;
    }
}
