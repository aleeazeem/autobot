package com.autodesk.bsm.pelican.ui.cart.renewal;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.goose.cart.pages.ShoppingCart;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.CartConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test class for renewal purchase order.
 *
 * @author jains
 *
 */
public class RenewalPurchaseOrderDemoFamilyTest extends SeleniumWebdriver {

    private JobsClient jobsResource;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private User user;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RenewalPurchaseOrderDemoFamilyTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolDemoUser = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolDemoUser.login(getEnvironmentVariables().getOtherAppFamily(), getEnvironmentVariables().getUserName(),
            getEnvironmentVariables().getPassword());
        findSubscriptionsPage = adminToolDemoUser.getPage(FindSubscriptionsPage.class);

        final PelicanPlatform resource =
            new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getOtherAppFamily());
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanTriggerClient triggersResource = pelicanResource.trigger();
        jobsResource = triggersResource.jobs();
        findPurchaseOrdersPage = adminToolDemoUser.getPage(FindPurchaseOrdersPage.class);
        user = new User();
        user.setExternalKey(getEnvironmentVariables().getCartUserExternalKey());
        user.setId(getEnvironmentVariables().getCartUserId());
        user.setName(getEnvironmentVariables().getCartUserName());
        user.setApplicationFamily(getEnvironmentVariables().getOtherAppFamily());
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String broker = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        final String notificationConsKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationConsSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationConsKey, notificationConsSecret);
        final String accessToken = authClient.getAuthToken();

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(broker, pelicanEventsNotificationChannel, accessToken);

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result - ITestResult would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
        pelicanEventsConsumer.clearNotificationsList();
    }

    /**
     * This method verifies that bic renewal order is generated with correct subscription quantity after reducing seats.
     *
     */
    @Test
    public void testBICRenewalWithReduceSeats() {
        final String initialQuantity = "5";
        final int quantityToReduce = 2;
        final String renewalQuantity = String.valueOf(Integer.parseInt(initialQuantity) - quantityToReduce);
        final ShoppingCart cartPages = new ShoppingCart(getDriver(), getEnvironmentVariables());
        cartPages.loadShoppingCart(getEnvironmentVariables(), CartConstants.STOREKEY, "BIC");
        cartPages.setQuantity(initialQuantity);
        cartPages.checkoutShoppingCart();
        cartPages.cartLogin(getEnvironmentVariables().getCartUserName(), getEnvironmentVariables().getCartPassword());
        cartPages.choosePaymentTypeAndReviewOrder("CreditCard");
        cartPages.submitPaypalOrderSummary(getEnvironmentVariables().getCartPaypalEmailAddress(),
            getEnvironmentVariables().getCartPaypalPassword());
        final String purchaseOrderId = cartPages.getPurchaseOrderID();
        // Get subscription id
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        final String subscriptionId = purchaseOrderDetailPage.getSubscriptionIdLinkedToLineItem(1);
        // Update subscription set QTY_TO_REDUCE
        final String updateSQL =
            "Update subscription set QTY_TO_REDUCE = " + quantityToReduce + " where id = " + subscriptionId;
        DbUtils.updateQuery(updateSQL, getEnvironmentVariables());

        // Edit subscription status, billing date and expiration date before running renewal job for subscription
        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowMinusDays(0), null, null, null);
        // get current time before running renewal job and convert to date for date comparison
        // Wait is added for purchase order to be in charge state
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String timeStampBeforeRenewalStrFormat = DateTimeUtils.getNowAsUTC(PelicanConstants.DB_DATE_FORMAT);
        final Date timeStampBeforeRenewalDateFormat =
            DateTimeUtils.convertStringToDate(timeStampBeforeRenewalStrFormat, PelicanConstants.DB_DATE_FORMAT);
        // Run renewal job
        final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(subscriptionId);
        jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());
        eventsList.clear();
        eventsList = pelicanEventsConsumer.getNotifications();
        Util.waitInSeconds(TimeConstants.TWO_MINS);
        pelicanEventsConsumer.waitForEvents(10000);
        // Refresh page to get renewal subscription activity
        getDriver().navigate().refresh();

        // verify change notification and AUM notification for subscription update.
        final SubscriptionOffering subscriptionOffering = new SubscriptionOffering();
        subscriptionOffering.setId(CartConstants.STG_BIC_MAYALT_ADVANCED_OFFERING_ID);
        final JProductLine productLine = new JProductLine();
        productLine.setCode(CartConstants.STG_BIC_MAYALT_ADVANCED_PRODUCT_LINE);
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, subscriptionId, user,
                subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionId, true, assertionErrorList);

        // get subscription activity to check if subscription is renewed
        final SubscriptionActivity subscriptionActivity = subscriptionDetailPage.getLastSubscriptionActivity();
        if (subscriptionActivity.getActivity().equals(PelicanConstants.EDIT)) {
            Assert.fail("Subscription was not renewed: " + subscriptionId);
        }
        final Date timeStampAfterRenewalDateFormat =
            DateTimeUtils.convertStringToDate(subscriptionActivity.getDate(), PelicanConstants.DB_DATE_FORMAT);
        AssertCollector.assertTrue(
            "Date time stamp for renewal order should be after original order for subscription id:" + subscriptionId,
            timeStampAfterRenewalDateFormat.after(timeStampBeforeRenewalDateFormat), assertionErrorList);
        AssertCollector.assertThat(
            "Quantity To Reduce on subscription detail page is not correct after renewal for subscription id:"
                + subscriptionId,
            subscriptionDetailPage.getQuantityToReduce(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat(
            "Quantity on subscription detail page is not correct after renewal for subscription id:" + subscriptionId,
            subscriptionDetailPage.getQuantity(), equalTo(Integer.parseInt(renewalQuantity)), assertionErrorList);
        // Get the renewal purchase order and verify renewal purchase order details
        String renewalPurchaseOrderId;
        renewalPurchaseOrderId = subscriptionActivity.getPurchaseOrder();
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderId);
        final PurchaseOrderDetailPage renewedPurchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertThat(
            "Quantity on renewal purchase order detail page is not correct for purchase order: "
                + renewalPurchaseOrderId,
            renewedPurchaseOrderDetailPage.getLineItemsQuantity(1), equalTo(renewalQuantity), assertionErrorList);
        // Set the subscription data so that it will be picked up when expiration job runs next time.
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        final String expirationDate = DateTimeUtils.getNowMinusDays(0);
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowMinusDays(1), expirationDate,
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);
        AssertCollector.assertAll(assertionErrorList);
    }
}
