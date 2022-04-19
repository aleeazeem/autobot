package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.cse.Attributes;
import com.autodesk.bsm.pelican.cse.ChangeNotificationApplicationFamily;
import com.autodesk.bsm.pelican.cse.ChangeNotificationAttributes;
import com.autodesk.bsm.pelican.cse.ChangeNotificationData;
import com.autodesk.bsm.pelican.cse.ChangeNotificationJsonAPI;
import com.autodesk.bsm.pelican.cse.ChangeNotificationMeta;
import com.autodesk.bsm.pelican.cse.ChangeNotificationRelationship;
import com.autodesk.bsm.pelican.cse.ChangeNotificationRelationships;
import com.autodesk.bsm.pelican.cse.ChangeNotifications;
import com.autodesk.bsm.pelican.cse.ChangeNotificationsHeader;
import com.autodesk.bsm.pelican.cse.Data;
import com.autodesk.bsm.pelican.cse.Links;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSEClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSEClient.class.getSimpleName());

    /**
     * CSE ChangeNotification response has data, meta and app family
     *
     * @param String - CSE Notification event
     * @param String - CSE Action (PurchaseOrder/SubscriptionOffer/Subscription/...)
     * @return ChangeNotification pojo
     */
    public static ChangeNotifications parseCSENotificationResponse(final String event) {

        final ChangeNotifications cseEvent = new ChangeNotifications();
        ChangeNotificationData cseData = new ChangeNotificationData();
        ChangeNotificationMeta cseMeta = new ChangeNotificationMeta();
        ChangeNotificationJsonAPI cseJsonAPI = new ChangeNotificationJsonAPI();

        ChangeNotificationAttributes attributes = new ChangeNotificationAttributes();
        ChangeNotificationRelationships relationships = new ChangeNotificationRelationships();
        ChangeNotificationRelationship user = new ChangeNotificationRelationship();
        ChangeNotificationRelationship subscriptionOffering = new ChangeNotificationRelationship();
        ChangeNotificationRelationship subscriptionEventData = new ChangeNotificationRelationship();
        ChangeNotificationRelationship purchaseOrderEventData = new ChangeNotificationRelationship();
        ChangeNotificationRelationship entitlementEventData = new ChangeNotificationRelationship();
        ChangeNotificationRelationship storeEventData = new ChangeNotificationRelationship();
        ChangeNotificationRelationship basicOfferingEventData = new ChangeNotificationRelationship();
        ChangeNotificationRelationship subscriptionOfferingEventData = new ChangeNotificationRelationship();
        ChangeNotificationApplicationFamily appFamily = new ChangeNotificationApplicationFamily();
        Links purchaseOrderlink = new Links();
        Links subscriptionlink = new Links();
        Links entitlementlink = new Links();
        Links storelink = new Links();
        Links basicOfferinglink = new Links();
        Links subscriptionOfferinglink = new Links();
        Links userlink = new Links();
        Links applicationFamilylink = new Links();

        Data data = new Data();
        Attributes userDataAttributes = new Attributes();
        Attributes subscriptionOfferingDataAttributes = new Attributes();

        try {
            final Gson gson = new GsonBuilder().create();
            LOGGER.debug("\n" + "Event = " + event);

            JsonObject jsonObject;
            try {
                final String jsonStr = gson.fromJson(event, String.class);
                jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
            } catch (final JsonSyntaxException e) {
                jsonObject = new JsonParser().parse(event).getAsJsonObject();
            }

            // Parse data
            final JsonObject jsonData = jsonObject.getAsJsonObject("data");
            cseData = gson.fromJson(jsonData.toString(), ChangeNotificationData.class);
            cseEvent.setData(cseData);

            // Parse attributes from data
            final JsonObject jsonAttribute = jsonData.getAsJsonObject("attributes");
            attributes = gson.fromJson(jsonAttribute.toString(), ChangeNotificationAttributes.class);
            cseData.setAttributes(attributes);

            // Parse relationships from data
            final JsonObject jsonRelationship = jsonData.getAsJsonObject("relationships");
            relationships = gson.fromJson(jsonRelationship.toString(), ChangeNotificationRelationships.class);
            cseData.setRelationships(relationships);

            // Some Relationships may not have User JsonObject
            if (jsonRelationship.has("user")) {
                // Parse User from relationships
                final JsonObject jsonUser = jsonRelationship.getAsJsonObject("user");
                user = gson.fromJson(jsonUser.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationUser(user);

                // Parse Links from Action
                final JsonObject jsonUserLinks = jsonUser.getAsJsonObject("links");
                userlink = gson.fromJson(jsonUserLinks.toString(), Links.class);
                user.setLink(userlink);

                // Parse Data from Links
                final JsonObject jsonUserLinksData = jsonUserLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonUserLinksData.toString(), Data.class);
                userlink.setData(data);

                // Parse Data from attributes
                final JsonObject jsonUserDataAttributes = jsonUserLinksData.getAsJsonObject("attributes");
                if (jsonUserDataAttributes != null) {
                    userDataAttributes = gson.fromJson(jsonUserDataAttributes.toString(), Attributes.class);
                    data.setAttributes(userDataAttributes);
                }
            }

            // Some Relationships may not have subscription JsonObject
            if (jsonRelationship.has("subscription")) {
                // Parse Subscription from relationships
                final JsonObject jsonSubscription = jsonRelationship.getAsJsonObject("subscription");
                subscriptionEventData =
                    gson.fromJson(jsonSubscription.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationSubscription(subscriptionEventData);

                // Parse Links from Action
                final JsonObject jsonSubscriptionLinks = jsonSubscription.getAsJsonObject("links");
                subscriptionlink = gson.fromJson(jsonSubscriptionLinks.toString(), Links.class);
                subscriptionEventData.setLink(subscriptionlink);

                // Parse Data from Links
                final JsonObject jsonSubscriptionLinksData = jsonSubscriptionLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonSubscriptionLinksData.toString(), Data.class);
                subscriptionlink.setData(data);

                // set subscription offering
                if (jsonRelationship.has("subscriptionOffering")) {
                    // Parse subscriptionOffering from relationships
                    final JsonObject jsonSubscriptionOffering =
                        jsonRelationship.getAsJsonObject("subscriptionOffering");
                    subscriptionOffering =
                        gson.fromJson(jsonSubscriptionOffering.toString(), ChangeNotificationRelationship.class);
                    relationships.setChangeNotificationSubscriptionOffering(subscriptionOffering);

                    // Parse Links from Action
                    final JsonObject jsonSubscriptionOfferingLinks = jsonSubscriptionOffering.getAsJsonObject("links");
                    subscriptionOfferinglink = gson.fromJson(jsonSubscriptionOfferingLinks.toString(), Links.class);
                    subscriptionOffering.setLink(subscriptionOfferinglink);

                    // Parse Data from Links
                    final JsonObject jsonSubscriptionOfferingLinksData =
                        jsonSubscriptionOfferingLinks.getAsJsonObject("data");
                    data = gson.fromJson(jsonSubscriptionOfferingLinksData.toString(), Data.class);
                    subscriptionOfferinglink.setData(data);

                    // Parse Data from attributes
                    final JsonObject jsonSubscriptionOfferingDataAttributes =
                        jsonSubscriptionOfferingLinksData.getAsJsonObject("attributes");
                    if (jsonSubscriptionOfferingDataAttributes != null) {
                        subscriptionOfferingDataAttributes =
                            gson.fromJson(jsonSubscriptionOfferingDataAttributes.toString(), Attributes.class);
                        data.setAttributes(subscriptionOfferingDataAttributes);
                    }
                }
            } else if (jsonRelationship.has("purchaseOrder")) {
                // Parse purchaseOrder from relationships
                final JsonObject jsonPurchaseOrder = jsonRelationship.getAsJsonObject("purchaseOrder");
                purchaseOrderEventData =
                    gson.fromJson(jsonPurchaseOrder.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationPurchaseOrder(purchaseOrderEventData);

                // Parse Links from Action
                final JsonObject jsonPurchaseOrderLinks = jsonPurchaseOrder.getAsJsonObject("links");
                purchaseOrderlink = gson.fromJson(jsonPurchaseOrderLinks.toString(), Links.class);
                purchaseOrderEventData.setLink(purchaseOrderlink);

                // Parse Data from Links
                final JsonObject jsonPurchaseOrderLinksData = jsonPurchaseOrderLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonPurchaseOrderLinksData.toString(), Data.class);
                purchaseOrderlink.setData(data);
            } else if (jsonRelationship.has("entitlement")) {
                // Parse entitlement from relationships
                final JsonObject jsonEntitlement = jsonRelationship.getAsJsonObject("entitlement");
                entitlementEventData = gson.fromJson(jsonEntitlement.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationEntitlement(entitlementEventData);

                // Parse Links from Action
                final JsonObject jsonEntitlementLinks = jsonEntitlement.getAsJsonObject("links");
                entitlementlink = gson.fromJson(jsonEntitlementLinks.toString(), Links.class);
                entitlementEventData.setLink(entitlementlink);

                // Parse Data from Links
                final JsonObject jsonEntitlementLinksData = jsonEntitlementLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonEntitlementLinksData.toString(), Data.class);
                entitlementlink.setData(data);
            } else if (jsonRelationship.has("store")) {
                // Parse store from relationships
                final JsonObject jsonStore = jsonRelationship.getAsJsonObject("store");
                storeEventData = gson.fromJson(jsonStore.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationStore(storeEventData);

                // Parse Links from Action
                final JsonObject jsonStoreLinks = jsonStore.getAsJsonObject("links");
                storelink = gson.fromJson(jsonStoreLinks.toString(), Links.class);
                storeEventData.setLink(storelink);

                // Parse Data from Links
                final JsonObject jsonStoreLinksData = jsonStoreLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonStoreLinksData.toString(), Data.class);
                storelink.setData(data);
            } else if (jsonRelationship.has("basicOffering")) {
                // Parse basicoffering from relationships
                final JsonObject jsonBasicOffering = jsonRelationship.getAsJsonObject("basicOffering");
                basicOfferingEventData =
                    gson.fromJson(jsonBasicOffering.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationBasicOffering(basicOfferingEventData);

                // Parse Links from Action
                final JsonObject jsonBasicOfferingLinks = jsonBasicOffering.getAsJsonObject("links");
                basicOfferinglink = gson.fromJson(jsonBasicOfferingLinks.toString(), Links.class);
                basicOfferingEventData.setLink(basicOfferinglink);

                // Parse Data from Links
                final JsonObject jsonBasicOfferingLinksData = jsonBasicOfferingLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonBasicOfferingLinksData.toString(), Data.class);
                basicOfferinglink.setData(data);
            } else if (jsonRelationship.has("subscriptionOffering")) {
                // Parse SubscriptionOffering from relationships
                final JsonObject jsonSubscriptionOffering = jsonRelationship.getAsJsonObject("subscriptionOffering");
                subscriptionOfferingEventData =
                    gson.fromJson(jsonSubscriptionOffering.toString(), ChangeNotificationRelationship.class);
                relationships.setChangeNotificationSubscriptionOffering(subscriptionOfferingEventData);

                // Parse Links from Action
                final JsonObject jsonSubscriptionOfferingLinks = jsonSubscriptionOffering.getAsJsonObject("links");
                subscriptionOfferinglink = gson.fromJson(jsonSubscriptionOfferingLinks.toString(), Links.class);
                subscriptionOfferingEventData.setLink(subscriptionOfferinglink);

                // Parse Data from Links
                final JsonObject jsonSubscriptionOfferingLinksData =
                    jsonSubscriptionOfferingLinks.getAsJsonObject("data");
                data = gson.fromJson(jsonSubscriptionOfferingLinksData.toString(), Data.class);
                subscriptionOfferinglink.setData(data);
            }

            // Parse App Family from relationships
            final JsonObject jsonApplicationFamily = jsonRelationship.getAsJsonObject("applicationFamily");
            appFamily = gson.fromJson(jsonApplicationFamily.toString(), ChangeNotificationApplicationFamily.class);
            relationships.setChangeNotificationApplicationFamily(appFamily);

            // Parse Links from Action
            final JsonObject jsonAppfamilyLinks = jsonApplicationFamily.getAsJsonObject("links");
            applicationFamilylink = gson.fromJson(jsonAppfamilyLinks.toString(), Links.class);
            appFamily.setLink(applicationFamilylink);

            // Parse Data from Links
            final JsonObject jsonAppfamilyLinksData = jsonAppfamilyLinks.getAsJsonObject("data");
            data = gson.fromJson(jsonAppfamilyLinksData.toString(), Data.class);
            applicationFamilylink.setData(data);

            // Parse data
            final JsonObject jsonMeta = jsonObject.getAsJsonObject("meta");
            cseMeta = gson.fromJson(jsonMeta.toString(), ChangeNotificationMeta.class);
            cseEvent.setMeta(cseMeta);

            // Parse data
            final JsonObject jsonApi = jsonObject.getAsJsonObject("jsonapi");
            cseJsonAPI = gson.fromJson(jsonApi.toString(), ChangeNotificationJsonAPI.class);
            cseEvent.setJsonapi(cseJsonAPI);

        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return cseEvent;
    }

    /**
     * CSE ChangeNotification message has JSON Header
     *
     * @param String - CSE Notification event
     * @return ChangeNotificationsHeader pojo
     */
    public static ChangeNotificationsHeader parseCSENotificationHeaderResponse(String header) {
        ChangeNotificationsHeader cseHeader = new ChangeNotificationsHeader();
        header = header.replace("\"", "");
        header = header.replaceAll("=", "\":\"");
        header = header.replaceAll(", ", "\",\"");
        header = header.replace("{", "{\"");
        header = header.replace("}", "\"}");

        try {
            final Gson gson = new GsonBuilder().create();
            LOGGER.info("Header = " + header);

            JsonObject jsonObject;
            try {
                final String jsonStr = gson.fromJson(header, String.class);
                jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
            } catch (final JsonSyntaxException e) {
                jsonObject = new JsonParser().parse(header).getAsJsonObject();
            }

            cseHeader = gson.fromJson(jsonObject.toString(), ChangeNotificationsHeader.class);

            LOGGER.info("Republish Flag: {}", cseHeader.getRepublishFlag());

        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return cseHeader;
    }
}
