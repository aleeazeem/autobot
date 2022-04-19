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
 * Page class for Reprovision New User Credits. This page can be navigated thrrough Applications -> Credits ->
 * Reprovision.
 *
 * @author jains
 *
 */
public class ReprovisionNewUserCreditsPage extends GenericDetails {

    public ReprovisionNewUserCreditsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-subIdList")
    private WebElement subscriptionIdListInput;

    @FindBy(id = "banner")
    private WebElement banner;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReprovisionNewUserCreditsPage.class.getSimpleName());

    /**
     * Method to return the info message on how many messages are sent to RabbitMQ.
     *
     * @return String
     */
    public String getInfoMessage() {
        return banner.getText();
    }

    /**
     * Method to reprovision new user credit for given subscription.
     *
     * @param subscriptionIdList - comma separated list of subscription
     * @return ReprovisionNewUserCreditsPage
     */
    public ReprovisionNewUserCreditsPage reprovisionNewUserCredits(final String subscriptionIdList) {
        navigateToReprovisionCreditPage();
        setSubscriptionIdList(subscriptionIdList);
        submit(TimeConstants.THREE_SEC);
        return getPage(ReprovisionNewUserCreditsPage.class);
    }

    /**
     * Method to set SubscriptionIdList.
     *
     * @param subscription
     */
    private void setSubscriptionIdList(final String subscription) {
        getActions().setText(subscriptionIdListInput, subscription);
        LOGGER.info("subscription " + subscription);
    }

    /**
     * Method to navigate to Reprovision New User Credits page.
     */
    public void navigateToReprovisionCreditPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + "event/reprovisionNewUserCreditsForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
