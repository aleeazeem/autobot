package com.autodesk.bsm.pelican.cse;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.OfferingEntitlement;
import com.autodesk.bsm.pelican.api.pojos.json.CseECNotification;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.SalesChannel;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.ChangeNotificationProducer;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class CSEHelper {

    private static final String SUBSCRIPTIONS = "subscriptions";
    private static final String SUBSCRIPTION = "subscription";
    private static final String USERS = "users";
    private boolean createChangeNotificationFound;
    private boolean createSubscriptionChangeNotificationFound;
    private boolean updateChangeNotificationFound;
    private boolean updateSubscriptionChangeNotificationFound;
    private PelicanPlatform resource;
    private EnvironmentVariables environmentVariables;
    private static final int ENTITLEMENT_NOT_FOUND = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSEHelper.class.getSimpleName());

    public CSEHelper(final PelicanPlatform resource) {
        this.resource = resource;
    }

    public CSEHelper(final PelicanPlatform resource, final EnvironmentVariables environmentVariables) {
        this.resource = resource;
        this.environmentVariables = environmentVariables;
    }

    /**
     * Helper method to initialize the consumer
     */
    public ChangeNotificationConsumer initializeConsumer(final String BROKER_URL, final String NOTIFICATION_CHANNEL,
        final String ACCESS_TOKEN) {
        // Initialize the consumer
        final ChangeNotificationConsumer consumer =
            new ChangeNotificationConsumer(BROKER_URL, NOTIFICATION_CHANNEL, ACCESS_TOKEN);
        consumer.start(null, null);
        LOGGER.info("$$$$$$$$$$$$$$$$$$ STARTED " + NOTIFICATION_CHANNEL + " EVENTS $$$$$$$$$$$$$$$$$$$$");
        return consumer;
    }

    /**
     * Helper method to initialize the Producer
     */
    public ChangeNotificationProducer initializeProducer(final String BROKER_URL, final String NOTIFICATION_CHANNEL,
        final ChangeNotificationAuthClient authClient) {
        // Initialize the Producer
        return new ChangeNotificationProducer(BROKER_URL, NOTIFICATION_CHANNEL, false, authClient);
    }

    /**
     * Helper method to terminate the consumer
     */
    public void terminateConsumer(final ChangeNotificationConsumer consumer, final String NOTIFICATION_CHANNEL,
        final List<ChangeNotificationMessage> eventsList) {
        consumer.terminate();

        LOGGER.info("$$$$$$$$$$$$$$$$$$ " + NOTIFICATION_CHANNEL + " CONSUMER TERMINATED $$$$$$$$$$$$$$$$$$$$");
        LOGGER.info(
            "$$$$$$$$$$$$$$ Total Events captured by " + NOTIFICATION_CHANNEL + " Consumer: " + eventsList.size());
    }

    /**
     * Helper method to Terminate the Producer plug
     */
    public void terminateProducer(final ChangeNotificationProducer producer, final String NOTIFICATION_CHANNEL) {
        producer.terminate();
        LOGGER.info("$$$$$$$$$$$$$$$$$$ " + NOTIFICATION_CHANNEL + " PRODUCER TERMINATED $$$$$$$$$$$$$$$$$$$$");
    }

    /**
     * Build ECNotification message of type Json string
     *
     * @return Json String
     */
    public static String buildMessage(final CseECNotification ecNotification) {

        return toJSON(ecNotification);
    }

    /**
     * convert java object to JSON format, and returned as JSON formatted string
     *
     * @return String
     */
    private static String toJSON(final CseECNotification ecNotification) {

        final Gson gson = new Gson();
        final String json = gson.toJson(ecNotification);
        LOGGER.info("Json String for ec notification: " + json);
        return json;
    }

    /**
     * Generic Method to compare 2 Pojos
     *
     * @return Boolean
     */
    public static boolean comparePOJO(final Object obj1, final Object obj2) {
        return new Gson().toJson(obj1).equals(new Gson().toJson(obj2));
    }

    /**
     * Get Data part of the message from Json
     *
     * @return CseECNotification object
     */
    public static CseECNotification getDataFromChangeNotificationMessage(final String jsonString) {

        final CseECNotification ecNjsonObjectotification = new CseECNotification();
        final String summaryECStatus = "SummaryECStatus";
        final String oxygenId = "oxygen_Id";
        final String summaryECUpdateTimetamp = "SummaryECUpdateTimestamp";

        final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        LOGGER.info("Json Object: " + jsonObject);
        if (jsonObject != null) {
            if (jsonObject.has(summaryECStatus)) {
                if (!(jsonObject.get(summaryECStatus).isJsonNull())) {
                    ecNjsonObjectotification.setECStatus(jsonObject.get(summaryECStatus).getAsString());
                }
            }
            if (jsonObject.has(oxygenId)) {
                final int size = jsonObject.getAsJsonArray(oxygenId).size();
                LOGGER.info("Json Array with oxygenId size: " + size);
                final String[] stringArray = new String[size];
                String oxygenIdValue;
                for (int count = 0; count < size; count++) {
                    oxygenIdValue = jsonObject.getAsJsonArray(oxygenId).get(count).getAsString();
                    LOGGER.info("Oxygen Id String: " + oxygenIdValue);
                    stringArray[count] = jsonObject.getAsJsonArray(oxygenId).get(count).getAsString();
                    LOGGER.info("String Array:" + stringArray);
                    ecNjsonObjectotification.setEcOxygenIds(stringArray);
                    if (jsonObject.has(summaryECUpdateTimetamp)) {
                        LOGGER
                            .info("Json Object Summary EC Update Timestamp " + jsonObject.get(summaryECUpdateTimetamp));
                        if (!(jsonObject.get(summaryECUpdateTimetamp).isJsonNull())) {
                            ecNjsonObjectotification
                                .setECUpdateTimeStamp(jsonObject.get(summaryECUpdateTimetamp).getAsString());
                        }
                    }
                }
            }
        } else {
            LOGGER.error("Json Object is null");
        }

        return ecNjsonObjectotification;
    }

    /**
     * Helper method to place a purchase order for a specific priceId
     *
     * @return subscriptionId
     */
    public String getPurchaseOrderAndSubscription(final PurchaseOrderUtils purchaseOrderUtils, final String priceId,
        final BuyerUser buyerUser) {
        // Submit a purchase order with Credit card and process it to charged
        final int quantity = 1;
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, quantity);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        return purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
    }

    /**
     * Helper method to print the CSE event data (for both types changeNotifications and subscriptionChangeNotification
     * based on the change type)
     */
    public static void helperToPrintEventData(final ChangeNotifications cseEvent) {
        LOGGER.info("\n" + "########### Get Data id: " + cseEvent.getData().getId() + "\n"
            + "########### Get Data type: " + cseEvent.getData().getType() + "\n" + "########### Get Meta version: "
            + cseEvent.getMeta().getVersion() + "\n" + "########### Get jsonapi Version: "
            + cseEvent.getJsonapi().getVersion() + "\n" + "########### Get Attribute PublishDate: "
            + cseEvent.getData().getAttributes().getPublishDate() + "\n");

        if ((cseEvent.getData().getType()).equals("changeNotifications")) {
            LOGGER.info(
                "########### Get Attribute ChangeType: " + cseEvent.getData().getAttributes().getChangeType() + "\n");
        } else if ((cseEvent.getData().getType()).equals("subscriptionChangeNotification")) {
            // if it's subscriptionChangeNotification
            LOGGER.info(
                "########### Get Relationships User Link Data attributes: " + cseEvent.getData().getRelationships()
                    .getChangeNotificationUser().getLink().getData().getAttributes().getExternalKey() + "\n");
        }

        if (cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering() == null) {
            LOGGER.info("\n" + "########### Get Attribute Subject: " + cseEvent.getData().getAttributes().getSubject()
                + "\n" + "########### Get Relationships User Link Related: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getRelated() + "\n"
                + "########### Get Relationships User Link Data Type: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getType() + "\n"
                + "########### Get Relationships User Link Data id: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getId() + "\n"
                + "########### Get Relationships App Family Link Data type: "
                + cseEvent.getData().getRelationships().getChangeNotificationApplicationFamily().getLink().getData()
                    .getType()
                + "\n" + "########### Get Relationships App Family Link Data id: " + cseEvent.getData()
                    .getRelationships().getChangeNotificationApplicationFamily().getLink().getData().getId()
                + "\n");
        }
        if (cseEvent.getData().getRelationships().getChangeNotificationSubscription() != null) {
            LOGGER.info("\n" + "########### Get Relationships Subscription Link Related: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getRelated() + "\n"
                + "########### Get Relationships Subscription Link Data Type: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getType() + "\n"
                + "########### Get Relationships Subscription Link Data id: "
                + cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getId() + "\n");
        }
        if (cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder() != null) {
            LOGGER.info("\n" + "########### Get Relationships PurchaseOrder Link Related: "
                + cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getRelated()
                + "\n" + "########### Get Relationships PurchaseOrder Link Data Type: "
                + cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData()
                    .getType()
                + "\n" + "########### Get Relationships PurchaseOrder Link Data id: "
                + cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData().getId()
                + "\n");
        }

        if (cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering() != null) {
            LOGGER
                .info("\n" + "########### Get Relationships Subscription Offering Link Related: "
                    + cseEvent
                        .getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getRelated()
                    + "\n" + "########### Get Relationships Subscription Offering Link Data Type: "
                    + cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getType()
                    + "\n" + "########### Get Relationships Subscription Offering Link Data id: "
                    + cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getId()
                    + "\n" + "########### Get Relationships Subscription Offering features Data id: "
                    + cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures()
                    + "\n" + "########### Get Relationships Subscription Offering attributes Data id: "
                    + cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getAttributes()
                    + "\n");
            if (cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.NONE.toLowerCase())) {
                LOGGER.info("########### Get Relationships Offering Features: " + cseEvent.getData().getRelationships()
                    .getChangeNotificationSubscriptionOffering().getLink().getData().getFeatures() + "\n");
            }
        }

    }

    /**
     * Helper method for purchase order header related CSE assertions.
     *
     * @param changeNotificationHeader
     * @param orderState
     * @param assertionErrorList
     */
    private void helperForPurchaseOrderHeaderRelatedAssertions(final ChangeNotificationsHeader changeNotificationHeader,
        final OrderState orderState, final String orderType, final List<AssertionError> assertionErrorList) {

        if (changeNotificationHeader != null) {

            AssertCollector.assertThat("Incorrect change notification pelican context header for purchase order",
                changeNotificationHeader.getPelicanContext(),
                equalTo("purchaseorder-" + orderState.toString().toLowerCase()), assertionErrorList);
            AssertCollector.assertThat("Incorrect change notification Category for purchase order",
                changeNotificationHeader.getcategory(),
                equalTo("pelican-change.notifications-purchaseorder-" + orderType), assertionErrorList);

        } else {
            Assert.fail("Change notification header for purchase order is not found.");
        }
    }

    /**
     * Helper method for purchase order related CSE assertions
     *
     * @param cseEvent
     * @param purchaseOrderId
     * @param changeType
     * @param assertionErrorList
     */
    private void helperForPurchaseOrderRelatedAssertions(final ChangeNotifications cseEvent,
        final String purchaseOrderId, final String changeType, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect consumer type", cseEvent.getData().getType(),
            equalTo(PelicanConstants.CHANGE_NOTIFICATIONS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Attribute Change Type",
            cseEvent.getData().getAttributes().getChangeType(), equalTo(changeType), assertionErrorList);
        AssertCollector.assertThat("Incorrect attribute subject", cseEvent.getData().getAttributes().getSubject(),
            equalTo(PelicanConstants.CSE_PURCHASE_ORDER), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchaseOrder link data type",
            cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData().getType(),
            equalTo(PelicanConstants.CSE_PURCHASE_ORDERS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchaseOrder link data id",
            cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData().getId(),
            equalTo(purchaseOrderId), assertionErrorList);
    }

    /**
     * Helper method for Subscription Offering header related CSE assertions.
     *
     * @param changeNotificationHeader
     * @param changeType
     * @param hasContext
     * @param assertionErrorList
     */
    private void helperForSubscriptionOfferingHeaderRelatedAssertions(
        final ChangeNotificationsHeader changeNotificationHeader, final String changeType, final Boolean hasContext,
        final List<AssertionError> assertionErrorList) {

        if (changeNotificationHeader != null) {

            if (changeType.equalsIgnoreCase(PelicanConstants.UPDATED) && hasContext) {

                AssertCollector.assertThat("Incorrect change notification Category for Subscription Offering",
                    changeNotificationHeader.getcategory(),
                    equalTo(PelicanConstants.OFFERING_CHANGE_NOTIFICATION_CATEGORY_HEADER), assertionErrorList);

                if (changeNotificationHeader.getPelicanContext() != null) {
                    AssertCollector.assertThat(
                        "Incorrect change notification pelican context header for Subscription Offering for Features "
                            + "is missing in change notification header",
                        changeNotificationHeader.getPelicanContext(),
                        equalTo(PelicanConstants.CSE_PELICAN_CONTEXT_ADD_FEATURE), assertionErrorList);
                } else {
                    Assert.fail("Pelican context header for Subscription Offering change notification for Features "
                        + "is missing in the message header");
                }
            } else if (changeType.equalsIgnoreCase(PelicanConstants.CREATED)) {
                AssertCollector.assertThat("Incorrect change notification Category for Subscription Offering",
                    changeNotificationHeader.getcategory(),
                    equalTo(PelicanConstants.CHANGE_NOTIFICATION_CATEGORY_HEADER), assertionErrorList);
            }

        } else {
            Assert.fail("Change notification header for Subscription Offering is not found.");
        }
    }

    /**
     * Helper method for Subscription Offering related CSE assertions
     *
     * @param cseEvent
     * @param subscriptionOfferingId
     * @param changeType
     * @param assertionErrorList
     */
    private void helperForSubscriptionOfferingRelatedAssertions(final ChangeNotifications cseEvent,
        final String subscriptionOfferingId, final String changeType, final String externalKey,
        final Boolean isAddFeatures, final Item item, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect consumer type", cseEvent.getData().getType(),
            equalTo(PelicanConstants.CHANGE_NOTIFICATIONS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Attribute Change Type",
            cseEvent.getData().getAttributes().getChangeType(), equalTo(changeType), assertionErrorList);
        AssertCollector.assertThat("Incorrect attribute subject", cseEvent.getData().getAttributes().getSubject(),
            equalTo(PelicanConstants.SUBSCRIPTION_OFFERING), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect subscriptionOffering link data type", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getType(),
            equalTo(PelicanConstants.SUBSCRIPTION_OFFERINGS), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect subscriptionOffering link data id", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getId(),
            equalTo(subscriptionOfferingId), assertionErrorList);
        if (isAddFeatures) {
            AssertCollector.assertThat(
                "Incorrect externalKey in subscriptionOffering data attribute", cseEvent.getData().getRelationships()
                    .getChangeNotificationSubscriptionOffering().getLink().getData().getAttributes().getExternalKey(),
                equalTo(externalKey), assertionErrorList);
            AssertCollector
                .assertThat("Incorrect Feature externalKey in subscriptionOffering data",
                    cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures().get(0).getExternalKey(),
                    equalTo(item.getExternalKey()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Feature ChangeType in subscriptionOffering data",
                cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
                    .getFeatures().get(0).getChangeType(),
                equalTo(PelicanConstants.CSE_ADD_FEATURE_CHANGE_TYPE), assertionErrorList);
        }
    }

    /**
     * This is a method which will validate whether the change notifications are found for a specific Subscription
     * Offering.
     *
     * @param eventsList
     * @param changeType
     * @param user
     * @param assertionErrorList
     * @param purchaseOrderId
     * @param orderState
     * @return boolean - true or false depending on whether change notification was found or not.
     */
    public boolean helperToFindChangeNotificationWithStateForSubscriptionOffering(
        final List<ChangeNotificationMessage> eventsList, final String subscriptionOfferingId, final String changeType,
        final User user, final String externalKey, final Boolean isAddFeatures, final Item item,
        final List<AssertionError> assertionErrorList) {
        Boolean notificationFound = false;
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        ChangeNotifications cseEvent;

        for (final ChangeNotificationMessage message : eventsList) {
            if (message.getData().contains(PelicanConstants.SUBSCRIPTION_OFFERING)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                    .getData().getId().equals(subscriptionOfferingId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(changeType)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    final ChangeNotificationsHeader changeNotificationHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());

                    if (cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures() != null) {
                        // Assert on Header.
                        helperForSubscriptionOfferingHeaderRelatedAssertions(changeNotificationHeader, changeType,
                            isAddFeatures, assertionErrorList);
                        // Assert the data
                        helperForSubscriptionOfferingRelatedAssertions(cseEvent, subscriptionOfferingId, changeType,
                            externalKey, isAddFeatures, item, assertionErrorList);
                    } else {
                        // Assert on Header.
                        helperForSubscriptionOfferingHeaderRelatedAssertions(changeNotificationHeader, changeType,
                            false, assertionErrorList);
                        // Assert the data
                        helperForSubscriptionOfferingRelatedAssertions(cseEvent, subscriptionOfferingId, changeType,
                            externalKey, false, null, assertionErrorList);
                    }
                    helperToPrintEventData(cseEvent);

                    helperForCommonAssertions(cseEvent, user, "", assertionErrorList);
                    notificationFound = true;
                }
            }
        }
        return notificationFound;
    }

    /**
     * Helper method for subscription related assertions for a CSE event with type as changeNotifications and
     * subscriptionChangeNotification
     *
     * @param cseEvent
     * @param subscriptionOffering
     * @param message - message will make the difference in between assert statement for change notification and
     * @param assertionErrorList
     */
    private void helperForAssertionsOnSubscriptionEntity(final ChangeNotifications cseEvent,
        final String subscriptionId, final SubscriptionOffering subscriptionOffering, final String message,
        final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect attribute subject " + message,
            cseEvent.getData().getAttributes().getSubject(), equalTo(SUBSCRIPTION), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription link data type " + message,
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData().getType(),
            equalTo(SUBSCRIPTIONS), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect subscription offering id " + message, cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getId(),
            equalTo(subscriptionOffering.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription offering product line " + message,
            cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
                .getAttributes().getProductLine(),
            equalTo(subscriptionOffering.getJProductLine().getCode()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscritpion Id ",
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData().getId(),
            equalTo(subscriptionId), assertionErrorList);
    }

    /**
     * Helper method for all common assertions for a CSE event
     *
     * @param assertionErrorList
     *
     * @param cseEvent.
     * @param user.
     * @param message.
     */
    private void helperForCommonAssertions(final ChangeNotifications cseEvent, final User user, final String message,
        final List<AssertionError> assertionErrorList) {

        if (!(cseEvent.getData().getAttributes().getSubject()
            .equalsIgnoreCase(PelicanConstants.SUBSCRIPTION_OFFERING))) {
            AssertCollector.assertThat("Incorrect user link data type" + message,
                cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getType(),
                equalTo(USERS), assertionErrorList);
            AssertCollector.assertThat("Incorrect user link data id" + message,
                cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getId(),
                equalTo(user.getId()), assertionErrorList);
        }
        if (!(cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(PelicanConstants.CSE_PURCHASE_ORDER))
            && !(cseEvent.getData().getAttributes().getSubject()
                .equalsIgnoreCase(PelicanConstants.SUBSCRIPTION_OFFERING))) {
            AssertCollector.assertThat(
                "Incorrect user external key", cseEvent.getData().getRelationships().getChangeNotificationUser()
                    .getLink().getData().getAttributes().getExternalKey(),
                equalTo(user.getExternalKey()), assertionErrorList);
        }
        AssertCollector.assertThat(
            "Incorrect application family link data type" + message, cseEvent.getData().getRelationships()
                .getChangeNotificationApplicationFamily().getLink().getData().getType(),
            equalTo("applicationFamilies"), assertionErrorList);
        if (user.getApplicationFamily() == null
            || user.getApplicationFamily().equalsIgnoreCase(environmentVariables.getApplicationFamily())) {
            AssertCollector.assertThat(
                "Incorrect application family link data id" + message, cseEvent.getData().getRelationships()
                    .getChangeNotificationApplicationFamily().getLink().getData().getId(),
                equalTo(environmentVariables.getAppFamilyId()), assertionErrorList);
        } else if (user.getApplicationFamily().equalsIgnoreCase(environmentVariables.getOtherAppFamily())) {
            AssertCollector.assertThat(
                "Incorrect application family link data id" + message, cseEvent.getData().getRelationships()
                    .getChangeNotificationApplicationFamily().getLink().getData().getId(),
                equalTo(environmentVariables.getOtherAppFamilyId()), assertionErrorList);
        }
    }

    /**
     * This is the main assertion method. it internally calls other assertion methods.
     *
     * @param cseEvent
     * @param user
     * @param subscriptionOffering
     * @param isSubscriptionChangeNotification
     * @param assertionErrorList
     */
    private void helperForAllAssertions(final ChangeNotifications cseEvent, final String subscriptionId,
        final User user, final SubscriptionOffering subscriptionOffering,
        final boolean isSubscriptionChangeNotification, final List<AssertionError> assertionErrorList) {

        String message = "";

        if (!isSubscriptionChangeNotification) {
            helperToPrintEventData(cseEvent);
            helperForAssertionsOnSubscriptionEntity(cseEvent, subscriptionId, subscriptionOffering, message,
                assertionErrorList);
            helperForCommonAssertions(cseEvent, user, message, assertionErrorList);
        } else {
            message = "in Subscription Change Notification";
            helperToPrintEventData(cseEvent);
            helperForAssertionsOnSubscriptionEntity(cseEvent, subscriptionId, subscriptionOffering, message,
                assertionErrorList);
            helperForCommonAssertions(cseEvent, user, message, assertionErrorList);
        }
    }

    /**
     * Method to verify changeNotifications and subscriptionChangeNotification for subscription. if isBicSubscription is
     * true then method will check changeNotification and subscriptionChangeNotification. if isBicSubscription is false
     * then method will check changeNotification
     *
     * @param foundNotifications
     * @param validate
     * @param subscriptionId
     * @param isBicSubscription
     * @param assertionErrorList
     */
    public void assertionToValidateChangeAndSubscriptionChangeNotificationFound(final List<Boolean> foundNotifications,
        final String validate, final String subscriptionId, final boolean isBicSubscription,
        final List<AssertionError> assertionErrorList) {

        if (validate.equals(PelicanConstants.CREATED)) {
            if (isBicSubscription) {
                AssertCollector.assertTrue("Subscription change notification is not found for 'create'"
                    + " for subscription id : " + subscriptionId, foundNotifications.get(0), assertionErrorList);
            }
            AssertCollector.assertTrue(
                "Change notification is not found for 'create'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(2), assertionErrorList);
        } else if (validate.equals(PelicanConstants.UPDATED)) {
            if (isBicSubscription) {
                AssertCollector.assertTrue("Subscription change notification is not found for 'update'"
                    + " for subscription id : " + subscriptionId, foundNotifications.get(1), assertionErrorList);
            }
            AssertCollector.assertTrue(
                "Change notification is not found for 'update'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(3), assertionErrorList);
        } else if (validate.equals(PelicanConstants.CREATE_AND_UPDATE)) {
            if (isBicSubscription) {
                AssertCollector.assertTrue("Subscription change notification is not found for 'create'"
                    + " for subscription id : " + subscriptionId, foundNotifications.get(0), assertionErrorList);
                AssertCollector.assertTrue("Subscription change notification is not found for 'update'"
                    + " for subscription id : " + subscriptionId, foundNotifications.get(1), assertionErrorList);
            }
            AssertCollector.assertTrue(
                "Change notification is not found for 'create'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(2), assertionErrorList);
            AssertCollector.assertTrue(
                "Change notification is not found for 'update'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(3), assertionErrorList);
        } else {
            Assert.fail("Invalid input to validate");
        }
    }

    public void assertionToValidateChangeAndSubscriptionChangeNotificationNotFoundForGDPRDeleteUser(
        final List<Boolean> foundNotifications, final String subscriptionId,
        final List<AssertionError> assertionErrorList) {
        AssertCollector.assertFalse(
            "Subscription change notification found for 'create'" + " for subscription id : " + subscriptionId,
            foundNotifications.get(0), assertionErrorList);
        AssertCollector.assertFalse(
            "Subscription change notification found for 'update'" + " for subscription id : " + subscriptionId,
            foundNotifications.get(1), assertionErrorList);
        AssertCollector.assertFalse(
            "Change notification found for 'create'" + " for subscription id : " + subscriptionId,
            foundNotifications.get(2), assertionErrorList);
        AssertCollector.assertFalse(
            "Change notification found for 'update'" + " for subscription id : " + subscriptionId,
            foundNotifications.get(3), assertionErrorList);
    }

    /**
     * helper method to find notification(changeNotification and subscriptionChangeNotification) from event list
     * depending upon the provided parameters
     *
     * @param events
     * @param subscriptionId
     * @param user
     * @param subscriptionOffering
     * @param isBicSubscription
     * @param assertionErrorList
     * @return
     */
    public List<Boolean> helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(
        final List<ChangeNotificationMessage> events, final String subscriptionId, final User user,
        final SubscriptionOffering subscriptionOffering, final boolean isBicSubscription,
        final List<AssertionError> assertionErrorList) {

        createChangeNotificationFound = false;
        createSubscriptionChangeNotificationFound = false;
        updateChangeNotificationFound = false;
        updateSubscriptionChangeNotificationFound = false;

        final List<Boolean> foundChangeAndSubscriptionChangeNotificationsList = new ArrayList<>();
        ChangeNotifications cseEvent;
        int numberOfEventsForSubscription = 0;

        for (int i = 0; i < events.size(); i++) {
            final ChangeNotificationMessage message = events.get(i);

            if (message.getData().contains(SUBSCRIPTIONS)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());

                if (isBicSubscription
                    && cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                        .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.CREATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, true,
                        assertionErrorList);
                    createSubscriptionChangeNotificationFound = true;
                    LOGGER.info("$$$$$Subscription Change Notification found for CREATE for subscription id: "
                        + subscriptionId);
                    LOGGER.info("Event number:" + i);
                    numberOfEventsForSubscription++;
                } else if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.CREATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, false,
                        assertionErrorList);
                    createChangeNotificationFound = true;
                    LOGGER.info("$$$$$Change Notification found for CREATE for subscription id: " + subscriptionId);
                    LOGGER.info("Event number:" + i);
                    numberOfEventsForSubscription++;
                } else if (isBicSubscription
                    && cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                        .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.UPDATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, true,
                        assertionErrorList);
                    updateSubscriptionChangeNotificationFound = true;
                    LOGGER.info("$$$$$Subscription Change Notification found for UPDATE for subscription id: "
                        + subscriptionId);
                    LOGGER.info("Event number:" + i);
                    numberOfEventsForSubscription++;
                } else if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.UPDATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, false,
                        assertionErrorList);
                    updateChangeNotificationFound = true;
                    LOGGER.info("$$$$$Change Notification found for UPDATE for subscription id: " + subscriptionId);
                    LOGGER.info("Event number:" + i);
                    numberOfEventsForSubscription++;
                }
            }
        }
        foundChangeAndSubscriptionChangeNotificationsList.add(createSubscriptionChangeNotificationFound);
        foundChangeAndSubscriptionChangeNotificationsList.add(updateSubscriptionChangeNotificationFound);
        foundChangeAndSubscriptionChangeNotificationsList.add(createChangeNotificationFound);
        foundChangeAndSubscriptionChangeNotificationsList.add(updateChangeNotificationFound);
        LOGGER.info("Completed loop in helperToFindChangeNotificationInCSEEvent");
        LOGGER.info("Number of total events found for subscription:" + numberOfEventsForSubscription);

        return foundChangeAndSubscriptionChangeNotificationsList;

    }

    /**
     * This method find all cse events for given subscription.
     *
     * @param events
     * @param subscriptionId
     * @return list of changeNotification
     */
    public List<ChangeNotifications> helperToFindChangeNotificationsForSubscription(
        final List<ChangeNotificationMessage> events, final String subscriptionId) {

        List<ChangeNotifications> eventsFound = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            final ChangeNotificationMessage message = events.get(i);

            if (message.getData().contains(SUBSCRIPTIONS)) {
                ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());

                if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {
                    createChangeNotificationFound = true;
                    LOGGER
                        .info("Change Notification " + cseEvent.getData().getAttributes().getChangeType().toUpperCase()
                            + " found for subscription id: " + subscriptionId);
                    eventsFound.add(cseEvent);
                }
            }
        }
        return eventsFound;

    }

    /**
     * This method validates the whole payload which gets published on cse channel.
     *
     * @param cseEvent
     * @param subscription
     * @param user
     * @param subscriptionOffering
     * @param changeType
     * @param assertionErrorList
     * @param saleschannel
     */
    public void helperToValidateChangeNotificationForSubscription(ChangeNotifications cseEvent,
        JSubscription subscription, User user, SubscriptionOffering subscriptionOffering, final String changeType,
        SalesChannel saleschannel, final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect change type", cseEvent.getData().getType(),
            equalTo(PelicanConstants.CHANGE_NOTIFICATIONS), assertionErrorList);

        // validate attributes
        AssertCollector.assertThat("Change type is not correct", cseEvent.getData().getAttributes().getChangeType(),
            equalTo(changeType), assertionErrorList);
        AssertCollector.assertThat("Subject is not correct", cseEvent.getData().getAttributes().getSubject(),
            equalTo("subscription"), assertionErrorList);

        // validate relationships
        AssertCollector.assertThat("Related of subscription offering is not correct",
            cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getRelated(),
            equalTo(environmentVariables.getV2ApiUrl() + "/subscriptionPlan" + "/" + subscriptionOffering.getId()),
            assertionErrorList);
        AssertCollector.assertThat(
            "Type of subscription offering is not correct", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getType(),
            equalTo("subscriptionOfferings"), assertionErrorList);
        AssertCollector.assertThat(
            "Id of subscription offering is not correct", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getId(),
            equalTo(subscriptionOffering.getId()), assertionErrorList);
        AssertCollector.assertThat("Product line of subscription offering is not correct",
            cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
                .getAttributes().getProductLine(),
            equalTo(subscriptionOffering.getJProductLine().getName()), assertionErrorList);
        AssertCollector.assertThat("Offering type of subscription offering is not correct",
            cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
                .getAttributes().getOfferingType(),
            equalTo(subscriptionOffering.getOfferingType().toString()), assertionErrorList);
        AssertCollector.assertThat("Usage type of subscription offering is not correct",
            cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
                .getAttributes().getUsageType(),
            equalTo(subscriptionOffering.getUsageType().getUploadName()), assertionErrorList);

        // validation on subscription
        AssertCollector.assertThat("Related is not correct for subscription",
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getRelated(),
            equalTo(environmentVariables.getSubscriptionServiceUrl() + "v3/" + subscription.getData().getId()),
            assertionErrorList);
        AssertCollector.assertThat("Type is not correct for subscription",
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData().getType(),
            equalTo("subscriptions"), assertionErrorList);
        AssertCollector.assertThat("Id is not correct for subscription",
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData().getId(),
            equalTo(subscription.getData().getId()), assertionErrorList);
        AssertCollector.assertThat(
            "Sales channel is not correct for subscription", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscription().getLink().getData().getAttributes().getSalesChannel(),
            equalTo(saleschannel.toString()), assertionErrorList);
        AssertCollector.assertThat("Aggrement number is not correct for subscription",
            cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                .getAttributes().getAggrementNumber(),
            equalTo(subscription.getData().getAggrementNumber()), assertionErrorList);

        // validations on user
        AssertCollector.assertThat("Related is not correct for user",
            cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getRelated(),
            equalTo(environmentVariables.getV2ApiUrl() + "/user" + "/" + user.getId()), assertionErrorList);
        AssertCollector.assertThat("Type is not correct for user",
            cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getType(),
            equalTo("users"), assertionErrorList);
        AssertCollector.assertThat("Id is not correct for user",
            cseEvent.getData().getRelationships().getChangeNotificationUser().getLink().getData().getId(),
            equalTo(user.getId()), assertionErrorList);
        AssertCollector.assertThat(
            "User external key is not correct for user", cseEvent.getData().getRelationships()
                .getChangeNotificationUser().getLink().getData().getAttributes().getExternalKey(),
            equalTo(user.getExternalKey()), assertionErrorList);

        // validations on applicationFamily
        AssertCollector.assertThat(
            "Application families is not correct", cseEvent.getData().getRelationships()
                .getChangeNotificationApplicationFamily().getLink().getData().getType(),
            equalTo("applicationFamilies"), assertionErrorList);
        AssertCollector.assertThat("Application family id is not correct",
            cseEvent.getData().getRelationships().getChangeNotificationApplicationFamily().getLink().getData().getId(),
            equalTo(environmentVariables.getAppFamilyId()), assertionErrorList);

        AssertCollector.assertThat("Meta version is not correct", cseEvent.getMeta().getVersion(), equalTo("1.0"),
            assertionErrorList);
        AssertCollector.assertThat("Json version is not correct", cseEvent.getJsonapi().getVersion(), equalTo("1.0"),
            assertionErrorList);

    }

    /**
     * This is a method to return change notification header object for subscription.
     *
     * @param events - List of events
     * @param subscriptionId - subscription id
     * @param String - notificationType
     * @param String - action
     * @return ChangeNotificationsHeader.
     */
    private ChangeNotificationsHeader getChangeAndSubscriptionChangeNotificationHeaderMessageForSubscription(
        final List<ChangeNotificationMessage> events, final String subscriptionId, final String notificationType,
        final String action) {

        LOGGER.info("Events size in helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent: " + events.size());

        ChangeNotifications cseEvent;
        ChangeNotificationsHeader changeNotificationsHeader = null;

        if (!(notificationType.equals(PelicanConstants.CHANGE_NOTIFICATIONS)
            || notificationType.equals(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION))) {
            Assert.fail("Please pass a valid value for notificationType. Valid values are: "
                + PelicanConstants.CHANGE_NOTIFICATIONS + ", " + PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION);
        }

        if (!(action.equals(PelicanConstants.CREATED) || action.equals(PelicanConstants.UPDATED))) {
            Assert.fail("Please pass a valid value for action. Valid values are: " + PelicanConstants.CREATED + ", "
                + PelicanConstants.UPDATED);
        }

        for (final ChangeNotificationMessage message : events) {
            if (message.getData().contains(SUBSCRIPTIONS)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());

                if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId) && cseEvent.getData().getAttributes().getChangeType().equals(action)
                    && cseEvent.getData().getType().equals(notificationType)) {
                    changeNotificationsHeader = CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                }
            }
        }
        LOGGER.info("Completed loop in returnChangeNotificationHeaderMessage");

        return changeNotificationsHeader;
    }

    public void assertionToValidateChangeNotificationFound(final List<Boolean> foundNotifications,
        final String validate, final String subscriptionId, final List<AssertionError> assertionErrorList) {

        if (validate.equals(PelicanConstants.CREATED)) {

            AssertCollector.assertThat("Subscription change notification should not be found for 'create'"
                + " for subscription id : " + subscriptionId, foundNotifications.get(0), equalTo(false),
                assertionErrorList);
            AssertCollector.assertTrue(
                "Change notification is not found for 'create'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(2), assertionErrorList);
        } else if (validate.equals(PelicanConstants.UPDATED)) {

            AssertCollector.assertThat("Subscription change notification should not be found for 'update'"
                + " for subscription id : " + subscriptionId, foundNotifications.get(1), equalTo(false),
                assertionErrorList);
            AssertCollector.assertTrue(
                "Change notification is not found for 'update'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(3), assertionErrorList);
        } else if (validate.equals(PelicanConstants.CREATE_AND_UPDATE)) {
            AssertCollector.assertThat("Subscription change notification should not be found for 'create'"
                + " for subscription id : " + subscriptionId, foundNotifications.get(0), equalTo(false),
                assertionErrorList);
            AssertCollector.assertThat("Subscription change notification should not be found for 'update'"
                + " for subscription id : " + subscriptionId, foundNotifications.get(1), equalTo(false),
                assertionErrorList);
            AssertCollector.assertTrue(
                "Change notification is not found for 'create'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(2), assertionErrorList);
            AssertCollector.assertTrue(
                "Change notification is not found for 'update'" + " for subscription id : " + subscriptionId,
                foundNotifications.get(3), assertionErrorList);
        } else {
            LOGGER.info("Invalid input to validate");
        }
    }

    /**
     * This is a method which will validate whether the change notifications are found for a specific purchase order.
     *
     * @param eventsList
     * @param purchaseOrderId
     * @param changeType
     * @param orderState
     * @param user
     * @param isGdprDelete
     * @param assertionErrorList
     * @return boolean - true or false depending on whether change notification was found or not.
     */
    public boolean helperToFindChangeNotificationWithStateForPurchaseOrder(
        final List<ChangeNotificationMessage> eventsList, final String purchaseOrderId, final String changeType,
        final OrderState orderState, final User user, final boolean isGdprDelete,
        final List<AssertionError> assertionErrorList) {
        Boolean notificationFound = false;
        if (!isGdprDelete) {
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        }
        ChangeNotifications cseEvent;

        for (final ChangeNotificationMessage message : eventsList) {
            if (message.getData().contains(PelicanConstants.PURCHASE_ORDERS)
                && message.getHeader().contains(("purchaseorder-" + orderState.toString()).toLowerCase())) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData()
                    .getId().equals(purchaseOrderId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(changeType)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    final ChangeNotificationsHeader changeNotificationHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());

                    // Assert on Header.
                    helperForPurchaseOrderHeaderRelatedAssertions(changeNotificationHeader, orderState, changeType,
                        assertionErrorList);
                    helperToPrintEventData(cseEvent);

                    // Assert the data
                    helperForPurchaseOrderRelatedAssertions(cseEvent, purchaseOrderId, changeType, assertionErrorList);
                    helperForCommonAssertions(cseEvent, user, "", assertionErrorList);
                    notificationFound = true;
                }
            }
        }
        return notificationFound;
    }

    /**
     * Helper Method to find change notification for Purchase order for Invoice and Fulfillment where under header
     * context type should not be present.
     *
     * @param eventsList
     * @param purchaseOrderId
     * @param changeType
     * @param isGdprDelete
     * @param assertionErrorList
     * @return changeNotificationFound
     */
    public boolean helperToFindChangeNotificationWithoutContextForPurchaseOrder(
        final List<ChangeNotificationMessage> eventsList, final String purchaseOrderId, final String changeType,
        final boolean isGdprDelete, final List<AssertionError> assertionErrorList) {
        boolean changeNotificationFound = false;
        if (!isGdprDelete) {
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        }
        ChangeNotifications cseEvent;

        for (final ChangeNotificationMessage message : eventsList) {
            if (message.getData().contains(PelicanConstants.PURCHASE_ORDERS) && message.getHeader()
                .contains(("pelican-change.notifications-purchaseorder-" + changeType).toLowerCase())) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink().getData()
                    .getId().equals(purchaseOrderId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(changeType)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    final ChangeNotificationsHeader changeNotificationHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());

                    // Assert on Header.
                    if (changeNotificationHeader.getPelicanContext() == null) {
                        AssertCollector.assertThat("Incorrect change notification Category for purchase order",
                            changeNotificationHeader.getcategory(),
                            equalTo("pelican-change.notifications-purchaseorder-" + changeType), assertionErrorList);
                        AssertCollector.assertThat("Context Type should be not present for purchase order",
                            changeNotificationHeader.getPelicanContext(), is(nullValue()), assertionErrorList);
                        LOGGER.info("Interested Event for Purchase Order :" + purchaseOrderId + "Found.");
                        helperToPrintEventData(cseEvent);
                        changeNotificationFound = true;
                    }
                }
            }
        }
        return changeNotificationFound;

    }

    public List<Boolean> helperToFindChangeNotificationInCSEEvent(final List<ChangeNotificationMessage> events,
        final String subscriptionId, final User user, final SubscriptionOffering subscriptionOffering,
        final List<AssertionError> assertionErrorList) {

        createChangeNotificationFound = false;
        createSubscriptionChangeNotificationFound = false;
        updateChangeNotificationFound = false;
        updateSubscriptionChangeNotificationFound = false;

        final List<Boolean> foundChangeNotificationsList = new ArrayList<>();

        ChangeNotifications cseEvent;

        for (final ChangeNotificationMessage message : events) {
            if (message.getData().contains(SUBSCRIPTIONS)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.CREATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, true,
                        assertionErrorList);
                    createSubscriptionChangeNotificationFound = true;
                } else if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.CREATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, false,
                        assertionErrorList);
                    createChangeNotificationFound = true;
                } else if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.UPDATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION)) {
                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, true,
                        assertionErrorList);
                    updateSubscriptionChangeNotificationFound = true;
                } else if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.UPDATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {

                    helperForAllAssertions(cseEvent, subscriptionId, user, subscriptionOffering, false,
                        assertionErrorList);
                    updateChangeNotificationFound = true;
                }
            }
        }
        foundChangeNotificationsList.add(createSubscriptionChangeNotificationFound);
        foundChangeNotificationsList.add(updateSubscriptionChangeNotificationFound);
        foundChangeNotificationsList.add(createChangeNotificationFound);
        foundChangeNotificationsList.add(updateChangeNotificationFound);
        LOGGER.info("Completed loop in helperToFindChangeNotificationInCSEEvent");

        return foundChangeNotificationsList;
    }

    /**
     * This method is to validate all the CSE Republish CN Messages to check if the header has the republish flag and
     * return true if all pass the validation
     *
     * @return boolean
     */
    public Boolean validateRepublishChangeNotificationHeader(final List<ChangeNotificationMessage> messages,
        final String entity, final List<String> ids) {
        Boolean republishValidation = false;
        for (final ChangeNotificationMessage message : messages) {

            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
            if (entity.equalsIgnoreCase((RepublishChangeNotificationEntity.PURCHASEORDER.getEntityCSEName()))
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse PURCHASEORDER Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag().equalsIgnoreCase("true"))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityCSEName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering()
                    .getLink().getData().getId())) {
                    LOGGER.info("Parse SUBSCRIPTION_PLAN Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag().equalsIgnoreCase("true"))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityCSEName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse SUBSCRIPTION Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag().equalsIgnoreCase("true"))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.AUM_SUBSCRIPTION.getEntityName())
                && cseEvent.getData().getAttributes().getSubject()
                    .equalsIgnoreCase(RepublishChangeNotificationEntity.AUM_SUBSCRIPTION.getEntityCSEName())) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse AUM SUBSCRIPTION Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        republishValidation = (cseEventHeader.getRepublishFlag()
                            .equalsIgnoreCase(StringUtils.lowerCase(PelicanConstants.TRUE))
                            && cseEventHeader.getcategory().equals("subscription-change.notifications"));
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.BASIC_OFFERING.getEntityCSEName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationBasicOffering().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse BASIC_OFFERING Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag()
                            .equalsIgnoreCase(StringUtils.lowerCase(PelicanConstants.TRUE)))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.STORE.getEntityCSEName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(
                    cseEvent.getData().getRelationships().getChangeNotificationStore().getLink().getData().getId())) {
                    LOGGER.info("Parse STORE Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag()
                            .equalsIgnoreCase(StringUtils.lowerCase(PelicanConstants.TRUE)))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityCSEName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {

                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationEntitlement().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse ENTITLEMENT Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishFlag()
                            .equalsIgnoreCase(StringUtils.lowerCase(PelicanConstants.TRUE)))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            }
        }
        return republishValidation;
    }

    /**
     * This method is to validate all the CSE Republish CN Messages to check if the header has the right requester value
     *
     * @return boolean
     */
    public Boolean validateRepublishChangeNotificationHeader(final List<ChangeNotificationMessage> messages,
        final String entity, final List<String> ids, final String requester) {

        Boolean republishValidation = false;
        for (final ChangeNotificationMessage message : messages) {

            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
            if (entity.equalsIgnoreCase((RepublishChangeNotificationEntity.PURCHASEORDER.getEntityCSEName()))
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationPurchaseOrder().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse PURCHASEORDER Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering()
                    .getLink().getData().getId())) {
                    LOGGER.info("Parse SUBSCRIPTION_PLAN Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse SUBSCRIPTION Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.BASIC_OFFERING.getEntityName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationBasicOffering().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse BASIC_OFFERING Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.STORE.getEntityName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(
                    cseEvent.getData().getRelationships().getChangeNotificationStore().getLink().getData().getId())) {
                    LOGGER.info("Parse STORE Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            } else if (entity.equalsIgnoreCase(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName())
                && cseEvent.getData().getAttributes().getSubject().equalsIgnoreCase(entity)) {
                if (ids.contains(cseEvent.getData().getRelationships().getChangeNotificationEntitlement().getLink()
                    .getData().getId())) {
                    LOGGER.info("Parse ENTITLEMENT Header: " + message.getHeader());
                    final ChangeNotificationsHeader cseEventHeader =
                        CSEClient.parseCSENotificationHeaderResponse(message.getHeader());
                    try {
                        if (!(cseEventHeader.getRepublishRequester().equalsIgnoreCase(requester))) {
                            return false;
                        } else {
                            republishValidation = true;
                        }
                    } catch (final Exception e) {
                        return false;
                    }
                }
            }
        }
        return republishValidation;
    }

    /**
     * Method to validate change notification header for a subscription for sherpa migration.
     *
     * @param events
     * @param subscriptionId
     * @param action
     * @param assertionErrorList
     */
    public void assertionToValidateChangeNotificationHeaderForSubscriptionForSherpaMigration(
        final List<ChangeNotificationMessage> events, final String subscriptionId, final String action,
        final List<AssertionError> assertionErrorList) {
        final ChangeNotificationsHeader changeNotificationsHeaderMessage =
            getChangeAndSubscriptionChangeNotificationHeaderMessageForSubscription(events, subscriptionId,
                PelicanConstants.CHANGE_NOTIFICATIONS, action);

        if (changeNotificationsHeaderMessage != null) {
            AssertCollector.assertThat("Incorrect change notification priority header for sherpa migration",
                changeNotificationsHeaderMessage.getPelicanPriority(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_PRIORITY_HEADER), assertionErrorList);
            AssertCollector.assertThat("Incorrect change notification pelican context header for sherpa migration",
                changeNotificationsHeaderMessage.getPelicanContext(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_CONTEXT_HEADER), assertionErrorList);
            AssertCollector.assertThat("Incorrect change notification category for sherpa migration",
                changeNotificationsHeaderMessage.getcategory(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_CATEGORY_HEADER), assertionErrorList);
        } else {
            Assert.fail("Change notification header for sherpa migration is not found.");
        }
    }

    /**
     * Method to validate subscription change notification header for a subscription for sherpa migration.
     *
     * @param events
     * @param subscriptionId
     * @param action
     * @param assertionErrorList
     */
    public void assertionToValidateSubscriptionChangeNotificationHeaderForSherpaMigration(
        final List<ChangeNotificationMessage> events, final String subscriptionId, final String action,
        final List<AssertionError> assertionErrorList) {
        final ChangeNotificationsHeader changeNotificationsHeaderMessage =
            getChangeAndSubscriptionChangeNotificationHeaderMessageForSubscription(events, subscriptionId,
                PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION, action);

        if (changeNotificationsHeaderMessage != null) {
            AssertCollector.assertThat("Incorrect subscription change notification priority header",
                changeNotificationsHeaderMessage.getPelicanPriority(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_PRIORITY_HEADER), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription change notification pelican context header",
                changeNotificationsHeaderMessage.getPelicanContext(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_CONTEXT_HEADER), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription change notification category",
                changeNotificationsHeaderMessage.getcategory(),
                equalTo(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION_CATEGORY_HEADER), assertionErrorList);
        } else {
            Assert.fail("Subscription Change notification header for sherpa migration is not found.");
        }

    }

    /**
     * Method to validate subscription change notification header for all subscriptions EXCEPT sherpa migration.
     *
     * @param events
     * @param subscriptionId
     * @param action
     * @param assertionErrorList
     */
    public void assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(
        final List<ChangeNotificationMessage> events, final String subscriptionId, final String action,
        final List<AssertionError> assertionErrorList) {
        final ChangeNotificationsHeader changeNotificationsHeaderMessage =
            getChangeAndSubscriptionChangeNotificationHeaderMessageForSubscription(events, subscriptionId,
                PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION, action);

        if (changeNotificationsHeaderMessage != null) {
            AssertCollector.assertThat("Incorrect subscription change notification priority header",
                changeNotificationsHeaderMessage.getPelicanPriority(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription change notification pelican context header",
                changeNotificationsHeaderMessage.getPelicanContext(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription change notification category",
                changeNotificationsHeaderMessage.getcategory(),
                equalTo(PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATION_CATEGORY_HEADER), assertionErrorList);
        } else {
            Assert.fail("Subscription Change notification header for non sherpa migration is not found.");
        }

    }

    /**
     * Method to find change notification for item instances.
     *
     * @param eventsList
     * @param itemInstanceId
     * @param action
     * @param assertionErrorList
     * @return boolean
     */
    public boolean assertionToValidateChangeNotificationForItemInstances(
        final List<ChangeNotificationMessage> eventsList, final String itemInstanceId, final String action,
        final List<AssertionError> assertionErrorList) {
        Boolean notificationFound = false;
        AssertCollector.assertThat("Empty events list ", eventsList.size(), greaterThan(0), assertionErrorList);
        ChangeNotifications cseEvent;

        for (final ChangeNotificationMessage message : eventsList) {
            if (message.getData().contains(PelicanConstants.ENTITLEMENT)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationEntitlement().getLink().getData().getId()
                    .equals(itemInstanceId) && cseEvent.getData().getAttributes().getChangeType().equals(action)) {
                    helperToPrintEventData(cseEvent);
                    // Assert the data
                    notificationFound = true;
                    LOGGER.info("Notification found for item instances for " + action + "action: " + notificationFound);
                }
            }
        }
        return notificationFound;
    }

    /**
     * Method to validation CSE messages for remove date
     *
     * @param eventsList
     * @param planId
     * @param planName
     * @param entitlementId
     * @param item
     *
     * @return boolean
     */
    public boolean helperToFindChangeNotificationWithEntitlementEndDate(
        final List<ChangeNotificationMessage> eventsList, final String planId, final String planExtKey,
        final ArrayList<OfferingEntitlement> entitlementDetails, final String date,
        final List<AssertionError> assertionErrorList) {

        boolean isNotificationFound = false;

        for (final ChangeNotificationMessage message : eventsList) {
            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());

            if (message.getData().contains(PelicanConstants.SUBSCRIPTION_OFFERING) && message.getData().contains(planId)
                && cseEvent.getData().getAttributes().getChangeType().equals(PelicanConstants.NONE.toLowerCase())) {

                final ChangeNotificationsHeader changeNotificationHeader =
                    CSEClient.parseCSENotificationHeaderResponse(message.getHeader());

                helperToPrintEventData(cseEvent);

                // Assert on Header.
                helperForRemoveFeatureHeaderAssertions(changeNotificationHeader, assertionErrorList);

                isNotificationFound = helperForRemoveEntitlementRelatedAssertions(cseEvent, planId, planExtKey,
                    entitlementDetails, date, assertionErrorList);

                LOGGER.info("Change Notification for Entitlement remove date result: " + isNotificationFound);
            }
        }

        return isNotificationFound;

    }

    /**
     * Helper method for REMOVE entitlements header related CSE assertions.
     *
     * @param changeNotificationHeader
     * @param assertionErrorList
     */
    private void helperForRemoveFeatureHeaderAssertions(final ChangeNotificationsHeader changeNotificationHeader,
        final List<AssertionError> assertionErrorList) {

        if (changeNotificationHeader != null) {

            AssertCollector.assertThat(
                "Incorrect change notification pelican context header for REMOVE entitlement cse event",
                changeNotificationHeader.getPelicanContext(),
                equalTo(PelicanConstants.CSE_HEADER_REMOVE_ENTITLEMENTS_CONTEXT), assertionErrorList);
            AssertCollector.assertThat("Incorrect change notification Category for REMOVE entitlement cse event",
                changeNotificationHeader.getcategory(),
                equalTo(PelicanConstants.CSE_HEADER_REMOVE_ENTITLEMENTS_CATEGORY), assertionErrorList);
            AssertCollector.assertThat("Incorrect change notification priority for REMOVE entitlement cse event",
                changeNotificationHeader.getPelicanPriority(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATION_PRIORITY_HEADER), assertionErrorList);
        } else {
            Assert.fail("Change notification header for REMOVE entitlement is not found.");
        }
    }

    /**
     * Helper method for Subscription Offering - Remove entitlement related CSE assertions
     *
     * @param cseEvent
     * @param planId
     * @param planExtKey
     * @param items
     * @param date
     * @param assertionErrorList
     *
     *        return boolean
     */
    private boolean helperForRemoveEntitlementRelatedAssertions(final ChangeNotifications cseEvent, final String planId,
        final String planExtKey, final ArrayList<OfferingEntitlement> entitlementDetails, final String date,
        final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect consumer type", cseEvent.getData().getType(),
            equalTo(PelicanConstants.CHANGE_NOTIFICATIONS), assertionErrorList);
        AssertCollector.assertTrue("Incorrect Attribute Change Type",
            cseEvent.getData().getAttributes().getPublishDate().contains(date), assertionErrorList);
        AssertCollector.assertThat("Incorrect Attribute Change Type",
            cseEvent.getData().getAttributes().getChangeType(), equalTo(PelicanConstants.NONE.toLowerCase()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect attribute subject", cseEvent.getData().getAttributes().getSubject(),
            equalTo(PelicanConstants.SUBSCRIPTION_OFFERING), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Offering link data type", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getType(),
            equalTo(PelicanConstants.SUBSCRIPTION_OFFERINGS), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Offering link data id", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getId(),
            equalTo(planId), assertionErrorList);
        AssertCollector.assertTrue("Incorrect Offering ID", cseEvent.getData().getRelationships()
            .getChangeNotificationSubscriptionOffering().getLink().getRelated().contains(planId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect Offering External key", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getAttributes().getExternalKey(),
            equalTo(planExtKey), assertionErrorList);
        AssertCollector
            .assertThat(
                "Incorrect App family Type", cseEvent.getData().getRelationships()
                    .getChangeNotificationApplicationFamily().getLink().getData().getType(),
                equalTo(PelicanConstants.CSE_APP_FAMILY), assertionErrorList);
        AssertCollector.assertThat("Incorrect App family ID",
            cseEvent.getData().getRelationships().getChangeNotificationApplicationFamily().getLink().getData().getId(),
            equalTo(environmentVariables.getAppFamilyId()), assertionErrorList);
        if (cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink().getData()
            .getFeatures() != null) {
            helperForFeatureObjectInRemoveEntitlements(cseEvent, entitlementDetails, assertionErrorList);
        } else {
            LOGGER.info("Features list is empty in Remove Feature CSE notification for plan id: " + planId);
            return false;
        }

        return true;

    }

    /**
     * Assert Feature Object inside relationship -> date
     *
     * @param cseEvent
     * @param items
     * @param date
     * @param assertionErrorList
     */
    private void helperForFeatureObjectInRemoveEntitlements(final ChangeNotifications cseEvent,
        final ArrayList<OfferingEntitlement> entitlementDetails, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat(
            "Error: Incorrect count of features", cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getFeatures().size(),
            equalTo(entitlementDetails.size()), assertionErrorList);
        for (int i = 0; i < entitlementDetails.size(); i++) {

            final int index = helperToReturnItemIndex(entitlementDetails, cseEvent.getData().getRelationships()
                .getChangeNotificationSubscriptionOffering().getLink().getData().getFeatures().get(i).getExternalKey());

            if (index > ENTITLEMENT_NOT_FOUND) {
                AssertCollector.assertThat("Error: Incorrect features externalKey",
                    cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures().get(i).getExternalKey(),
                    equalTo(entitlementDetails.get(index).getItemExternalKey()), assertionErrorList);
                AssertCollector.assertThat("Error: Incorrect features change type",
                    cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures().get(i).getChangeType(),
                    equalTo(PelicanConstants.CSE_REMOVE_ENTITLEMENT_CHANGE_TYPE), assertionErrorList);
                AssertCollector.assertThat("Error: Incorrect features Type(CPR/CSR): ",
                    cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures().get(i).getType(),
                    equalTo(entitlementDetails.get(index).getType()), assertionErrorList);

                if (entitlementDetails.get(index).getIsEos()) {
                    AssertCollector.assertThat("Error: Incorrect EOS end date: ",
                        cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                            .getData().getFeatures().get(i).getEosDate(),
                        equalTo(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH)
                            + " 00:00:00 UTC"),
                        assertionErrorList);
                }

                if (entitlementDetails.get(index).getIsEolImme()) {
                    AssertCollector.assertThat("Error: Incorrect EOL Immediate end date: ",
                        cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                            .getData().getFeatures().get(i).getEolImmediateDate(),
                        equalTo(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH)
                            + " 00:00:00 UTC"),
                        assertionErrorList);
                }

                if (entitlementDetails.get(index).getIsEolRenewal()) {
                    AssertCollector.assertThat("Error: Incorrect EOL Renewal end date: ",
                        cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                            .getData().getFeatures().get(i).getEolRenewalDate(),
                        equalTo(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH)
                            + " 00:00:00 UTC"),
                        assertionErrorList);
                }

            } else {
                Assert.fail("Unexpected Feature with external Key ("
                    + cseEvent.getData().getRelationships().getChangeNotificationSubscriptionOffering().getLink()
                        .getData().getFeatures().get(i).getExternalKey()
                    + "), found in CSE event ");
            }

        }

    }

    /**
     * helper to find index of the item in the ArrayList
     *
     * @param items
     * @param findItemExternalKey
     * @return
     */
    private int helperToReturnItemIndex(final ArrayList<OfferingEntitlement> entitlementDetails,
        final String findItemExternalKey) {
        for (int i = 0; i < entitlementDetails.size(); i++) {
            if (entitlementDetails.get(i).getItemExternalKey().equals(findItemExternalKey)) {
                return i;
            }
        }
        return ENTITLEMENT_NOT_FOUND;

    }
}
