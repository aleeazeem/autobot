package com.autodesk.bsm.pelican.cse.entitlement;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.ItemInstanceClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstance;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.CancelSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to verify that CSE events are not sent for entitlements add, update for a user who is flagged with "GDPR
 * DELETED"
 *
 * @author Shweta Hegde
 */
public class EntitlementCseEventsForGdprDeleteTest extends SeleniumWebdriver {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(EntitlementCseEventsForGdprDeleteTest.class.getSimpleName());
    private FindSubscriptionsPage findSubscriptionsPage;
    private List<ChangeNotificationMessage> eventsList;
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanEventsConsumer;
    private String pelicanEventsNotificationChannel;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        cseHelper = new CSEHelper(resource);
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        final String pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        eventsList = new ArrayList<>();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * Test to verify that no CSE Event is sent for entitlements add, update & update expiration date for a GDPR deleted
     * user. Step1 : Create Meta Subscription. Step2 : GDPR Cancel subscription. Step3 : Update the user to GDPR
     * DELETED. Step4 : Add Item instance to the user and verify that no change notifications are sent. Step5 : Update
     * Item Instance's expiration date and verify that no change notifications are sent. Step6 : Update Item instance's
     * owner id to non-gdpr deleted user, and verify that no change notifications are sent.
     */
    @Test
    public void testNoCSEEventsPublishedForAddUpdateItemInstancesToGdprDeletedUser() {

        // Create a user for GDPR delete flag value 1
        final Map<String, String> userParams = new HashMap<>();
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(9);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User gdprDeleteUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        final BuyerUser gdprBuyerUser = new BuyerUser();
        gdprBuyerUser.setId(gdprDeleteUser.getId());
        gdprBuyerUser.setExternalKey(gdprDeleteUser.getExternalKey());
        gdprBuyerUser.setEmail("abc.pxrs@xyz.com");

        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            Payment.PaymentType.CREDIT_CARD, getMetaMonthlyUsPriceId(), gdprBuyerUser, 3);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Find the subscription in Admin Tool and cancel with GDPR option
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, "gdpr cancel");

        // Update the GDPR delete flag to 1
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, gdprDeleteUser.getId()),
            getEnvironmentVariables());

        // Add Item Type and Item
        final String itemTypeName = "ItemType-" + RandomStringUtils.randomAlphanumeric(8);
        final ItemType itemType = resource.itemType().addItemType(getEnvironmentVariables().getAppId(), itemTypeName);
        final String itemNameExternalKey = "Item-" + RandomStringUtils.randomAlphanumeric(8);

        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ItemParameter.NAME.getName(), itemNameExternalKey);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), getEnvironmentVariables().getAppId());
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), itemNameExternalKey);
        final Item item = resource.item().addItem(paramMap);

        // Add Item Instance to the GDPR Delete User
        Map<String, String> requestParam = new HashMap<>();
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.ITEM_ID.getName(), item.getId());
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.OWNER_ID.getName(), gdprDeleteUser.getId());
        final ItemInstance itemInstance = resource.itemInstance().add(requestParam);
        final String itemInstanceId = itemInstance.getId();
        LOGGER.info("Item Instance id : {}", itemInstanceId);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        // Verify that notifications are not sent
        final boolean isNotificationFoundForItemInstanceAdd =
            cseHelper.assertionToValidateChangeNotificationForItemInstances(eventsList, itemInstanceId,
                PelicanConstants.CREATED, assertionErrorList);
        AssertCollector.assertFalse("Change notification found for Adding an iteminstance with id:" + itemInstanceId,
            isNotificationFoundForItemInstanceAdd, assertionErrorList);

        // Update Expiration Date of the item instance
        requestParam = new HashMap<>();
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.ITEM_INSTANCE_ID.getName(), itemInstanceId);
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.EXPIRATION_DATE.getName(),
            DateTimeUtils.getNowPlusDays(PelicanConstants.DB_DATE_FORMAT, 5) + " " + PelicanConstants.UTC_TIME_ZONE);
        resource.itemInstance().updateExpirationDate(requestParam);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        // Verify that notifications are not sent
        final boolean isNotificationFoundForItemInstanceUpdateExpirationDateGdprUser =
            cseHelper.assertionToValidateChangeNotificationForItemInstances(eventsList, itemInstanceId,
                PelicanConstants.UPDATED, assertionErrorList);
        AssertCollector.assertFalse(
            "Change notification found for Updating expiration date of an iteminstance " + "with id:" + itemInstanceId,
            isNotificationFoundForItemInstanceUpdateExpirationDateGdprUser, assertionErrorList);

        // Update the item instance with non gdpr delete user
        requestParam = new HashMap<>();
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.ITEM_INSTANCE_ID.getName(), itemInstanceId);
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.OWNER_ID.getName(), getBuyerUser().getId());
        requestParam.put(ItemInstanceClient.ItemInstanceParameter.IP_ADDRESS.getName(), "10.101.101.10");
        resource.itemInstance().update(requestParam);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        // Verify that notifications are not sent
        final boolean isNotificationFoundForItemInstanceUpdate =
            cseHelper.assertionToValidateChangeNotificationForItemInstances(eventsList, itemInstanceId,
                PelicanConstants.UPDATED, assertionErrorList);
        AssertCollector.assertFalse("Change notification found for Updating an iteminstance with id:" + itemInstanceId,
            isNotificationFoundForItemInstanceUpdate, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
