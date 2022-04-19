package com.autodesk.bsm.pelican.util;

import com.autodesk.platform.cse.Event;
import com.autodesk.platform.cse.Service;
import com.autodesk.platform.eventing.Read;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author mandas
 */
public class ChangeNotificationConsumer {
    private static final List<ChangeNotificationMessage> notifications = new LinkedList<>();
    private static final List<ChangeNotificationMessage> headers = new LinkedList<>();
    private String serviceUrl;
    private String channel;
    private String access;
    private boolean isRunning;
    private Read pulling;
    private Service sdk;
    private static int eventCounter = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeNotificationConsumer.class.getSimpleName());

    public ChangeNotificationConsumer(final String serviceUrl, final String channel, final String access) {
        this.serviceUrl = serviceUrl;
        this.channel = channel;
        this.access = access;
    }

    /**
     * Start the CSE service and the listener
     *
     * @return the status of the start request
     */
    public boolean start(final String testCase, final String search) {
        if (sdk == null || pulling == null) {
            sdk = com.autodesk.platform.cse.Factory.factory(this.serviceUrl);
            pulling = com.autodesk.platform.eventing.Factory.factory(sdk, new Reader(this.access), this.channel, "*",
                100, "head");
        }
        return true;
    }

    /**
     * Wait for specific timeout
     *
     * @param timeout timeout in ms, before checking messages
     */
    public void waitForEvents(final long timeout) {
        try {
            Thread.sleep(timeout);
            LOGGER.debug("Debug comment: Done waiting to collect Events");
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the notifications collected so far
     *
     * @return the list of notifications
     */
    public List<ChangeNotificationMessage> getNotifications() {
        return notifications;
    }

    /**
     * Clears the notifications which are collected so far
     */
    public void clearNotificationsList() {
        notifications.clear();
    }

    /**
     * Check if the listener is still running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Terminate the listener
     *
     * @return the status of termination
     */
    public boolean terminate() {
        pulling.terminate();
        sdk.shutdown();
        isRunning = false;
        return true;
    }

    /**
     * Reader class template
     *
     * @author zaheer
     */
    static class Reader implements Read.Sink {
        String token;

        Reader(final String token) {
            this.token = token;
        }

        @Override
        public String require_access_token() {
            return new ChangeNotificationAuthClient().getAuthToken();
        }

        @Override
        public boolean received(final List<Event> events, final String checkpoint) {

            for (final Event event : events) {
                LOGGER.debug("Received Event " + eventCounter + " :");
                LOGGER.debug("Event Header: " + event.headers().toString());
                LOGGER.debug("Event Body: " + event.toString() + "\n");
                notifications.add(new ChangeNotificationMessage(event.toString(), event.headers().toString()));

                eventCounter++;
            }
            return true;
        }
    }
}
