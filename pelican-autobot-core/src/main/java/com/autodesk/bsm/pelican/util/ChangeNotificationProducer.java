package com.autodesk.bsm.pelican.util;

import com.autodesk.platform.cse.Event;
import com.autodesk.platform.cse.Service;
import com.autodesk.platform.eventing.Write;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * This class is used to publish events to CSE Channel
 *
 * @author mandas
 */
public class ChangeNotificationProducer {

    private static final String CSE_PUBLISH_COUNT_HEADER = "x-cse-publish-count";
    private static final String CSE_PUBLISH_COUNT_MSG = CSE_PUBLISH_COUNT_HEADER + ":";

    private static final int BUFFER_SIZE = 100;
    private static final long BUFFER_TIMEOUT_MS = 3000;
    private static final long PUBLISH_TIMEOUT_MS = 30 * 1000L;
    private static final String DEFAULT_CATEGORY_NAME = "OxygenId-with-PaymentProfile";

    private String changeNotificationBrokerUrl;
    private String changeNotificationChannel;
    private ChangeNotificationAuthClient authClient;
    private final Object lock = new Object();
    private volatile Write publishing;
    private boolean enableBuffer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeNotificationProducer.class.getSimpleName());

    public ChangeNotificationProducer(final String changeNotificationBrokerUrl, final String changeNotificationChannel,
        final boolean enableBuffer, final ChangeNotificationAuthClient authClient) {
        this.changeNotificationBrokerUrl = changeNotificationBrokerUrl;
        this.changeNotificationChannel = changeNotificationChannel;
        this.enableBuffer = enableBuffer;
        this.authClient = authClient;
    }

    /**
     * Sends change notification to the CSE server
     *
     * @param eventMsg Event message to publish
     * @return notification status
     */
    public boolean notifyChange(final ChangeNotificationMessage eventMsg) throws ListenerExecutionFailedException {
        return this.notifyChange(eventMsg, DEFAULT_CATEGORY_NAME, null);
    }

    /**
     * Sends change notifications to the CSE server
     *
     * @param eventMsgs Event messages to publish
     * @return notification status
     */
    public boolean notifyChangeList(final Collection<ChangeNotificationMessage> eventMsgs)
        throws ListenerExecutionFailedException {
        final Map<String, Object> eventHeaders = new HashMap<>();
        eventHeaders.put("category", DEFAULT_CATEGORY_NAME);
        final List<Event> events = new ArrayList<>();
        for (final ChangeNotificationMessage eventMsg : eventMsgs) {
            events.add(new Event(eventHeaders, eventMsg.getData().getBytes()));
        }
        return this.publishNotificationsToCse(events);
    }

    /**
     * Sends change notification to the CSE server
     *
     * @param eventMsg Event message to publish
     * @param category Category of the change notifications
     * @param headers Message headers for change notifications
     * @return notification status
     */
    private boolean notifyChange(final ChangeNotificationMessage eventMsg, final String category,
        final Map<String, ?> headers) throws ListenerExecutionFailedException {
        final Map<String, Object> eventHeaders = new HashMap<>();
        eventHeaders.put("category", category);
        if (headers != null) {
            eventHeaders.putAll(headers);
        }

        final List<Event> events = new ArrayList<>();
        final List<String> eventDataList = getChangeNotificationEvents(eventMsg.getData());
        if (!eventDataList.isEmpty()) {
            for (final String eventData : eventDataList) {
                events.add(new Event(eventHeaders, eventData.getBytes()));
            }
        } else {
            events.add(new Event(eventHeaders, eventMsg.getData().getBytes()));
        }
        return this.publishNotificationsToCse(events);
    }

    /**
     * Deserialize message payload to extract change notification events
     *
     * @param msgPayload Message payload
     * @return List of events in json format
     */
    private static List<String> getChangeNotificationEvents(final String msgPayload) {
        List<String> events = new ArrayList<>();
        try {
            final ChangeNotificationArrayMessage arrayMessage =
                new Gson().fromJson(msgPayload, ChangeNotificationArrayMessage.class);
            if (arrayMessage != null && arrayMessage.getData() != null) {
                final List<String> unformattedEvents = arrayMessage.getData();
                events = new ArrayList<>(unformattedEvents.size());
                for (final String unformattedEvent : unformattedEvents) {
                    final String formattedEventString = StringEscapeUtils.unescapeJava(unformattedEvent);
                    final JsonObject jsonObject = (JsonObject) new JsonParser().parse(formattedEventString);
                    if (!jsonObject.has("meta")) {
                        final JsonObject metaElement = new JsonObject();
                        metaElement.addProperty("version", "1.0");
                        jsonObject.add("meta", metaElement);
                    }
                    if (!jsonObject.has("jsonapi")) {
                        final JsonObject jsonApiElement = new JsonObject();
                        jsonApiElement.addProperty("version", "1.0");
                        jsonObject.add("jsonapi", jsonApiElement);
                    }
                    events.add(jsonObject.toString());
                }
            }
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error in parsing change notification message, no worries: " + e.getMessage());
        }
        return events;
    }

    /**
     * Sends change notification to the CSE server
     *
     * @param events CSE change events
     * @return notification status
     */
    private boolean publishNotificationsToCse(final List<Event> events) {
        try {
            final Write publishing = getPublishingSource();
            LOGGER.info("Change notification request with " + events.size() + " events, " + "publishing on url: "
                + changeNotificationBrokerUrl + " ,channel: " + changeNotificationChannel);
            final int publishedEventCount = publishing.publish_until(events, PUBLISH_TIMEOUT_MS);
            LOGGER.info("Number of published events:" + publishedEventCount);
            if (publishedEventCount != events.size()) {
                LOGGER.error("Unable to publish events to CSE server: " + "Published events count do not match");
                throw new ListenerExecutionFailedException(CSE_PUBLISH_COUNT_MSG + publishedEventCount, null);
            }
            return true;
        } catch (final ListenerExecutionFailedException e) {
            throw e;
        } catch (final TimeoutException e) {
            throw new ListenerExecutionFailedException("Timeout in notifying events to CSE server", e);
        } catch (final Exception e) {
            LOGGER.info("Error in notifying the changes ");
            throw new ListenerExecutionFailedException("Error in notifying events to CSE server", e);
        }
    }

    private Write getPublishingSource() {
        if (this.publishing == null) {
            synchronized (lock) {
                if (this.publishing == null) {
                    final Service service = com.autodesk.platform.cse.Factory.factory(changeNotificationBrokerUrl);

                    final Write publishing = com.autodesk.platform.eventing.Factory.factory(service, new Writer(),
                        this.changeNotificationChannel);

                    if (this.enableBuffer) {
                        // Set the watermark to the appropriate size to let the
                        // CSE SDK send
                        // messages in bulk
                        publishing.set_watermark(BUFFER_SIZE);
                        publishing.set_window(BUFFER_TIMEOUT_MS);
                    }
                    this.publishing = publishing;
                }
            }
        }

        return publishing;
    }

    /**
     * Terminate CSE Producer
     */
    public void terminate() {
        LOGGER.info("$$$$$$$$$$$$$$$$$$ TERMINATING PRODUCER $$$$$$$$$$$$$$$$$$$$");
        publishing.terminate();
    }

    /**
     * Publisher class to write events
     */
    class Writer implements Write.Sink {
        public String require_access_token() {
            return authClient.getAuthToken();
        }
    }

}
