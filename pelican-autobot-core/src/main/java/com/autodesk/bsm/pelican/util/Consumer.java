package com.autodesk.bsm.pelican.util;

import com.autodesk.platform.cse.v2.config.DefaultCSEConfigProviderChain;
import com.autodesk.platform.cse.v2.eventing.Event;
import com.autodesk.platform.cse.v2.eventing.Read;
import com.autodesk.platform.cse.v2.eventing.impl.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/********************************************************************************************
 * Encapsulate logic to consume events continuously, as well as all applicable configuration
 *********************************************************************************************/
public class Consumer implements Callable {

    private static final List<ChangeNotificationMessage> notifications = new LinkedList<>();
    private static int eventCounter = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private List<Event> receivedEventsBuffer = new ArrayList<>();
    private Reader reader;

    public Consumer() {

        // Unused code, will used in 2nd story
        // final String propertyFilePath = Util.getTestRootDir()
        // + String.format("/src/test/resources/environments/auto_env_wildflystg.properties");
        // LOGGER.info(propertyFilePath);
        // ClasspathPropertiesFileConfigProvider classPathProvider =
        // new ClasspathPropertiesFileConfigProvider(
        // "/src/test/resources/environments/auto_env_wildflystg.properties");
        // CSEConfigProvider[] providers =
        // new CSEConfigProvider[]{new EnvironmentVariableConfigProvider(), new SystemPropertiesConfigProvider(),
        // new ClasspathPropertiesFileConfigProvider(
        // "/src/test/resources/environments/auto_env_wildflystg.properties")};
        // CSEConfigProviderChain configChain = new CSEConfigProviderChain(providers);

        this.reader = new Reader.Builder()
            // configProvider((configChain)). //Load Parameters from the *cse_kinesis_config.properties* file
            .configProvider((DefaultCSEConfigProviderChain.getInstance())).callback(new NewEventsCallback()) // Hook to
                                                                                                             // be
                                                                                                             // invoked
                                                                                                             // upon new
                                                                                                             // event
                                                                                                             // arrival
            //
            // ===============================
            // OPTIONAL performance tuning parameters (defaults are adequate for 90% of use cases)
            // If true, consumer will pull channel events as fast as it can
            // (NOTE: suitable only for apps that own stream consumption; otherwise, throttling will be increased)
            .withMinimalLatencyMode(false)
            // Under the hood consumption is based on polling. This is how many event batches will be pulled per
            // (NOTE: only increase if certain that published event batches on the channel are ALWAYS small
            // If Max Batch Size * This Parameter > 2MB, AWS throttling will DRAMATICALLY degrade performance)
            .batch(8)
            // ===============================
            //
            .build();

        // LOGGER.info(String.format("CONSUMER appName= %s, workerName= %s, streamName= %s, category= %s",
        // DefaultCSEConfigProviderChain.getInstance().getConfig().getAppName(),
        // DefaultCSEConfigProviderChain.getInstance().getConfig().getConsumerWorkerName(),
        // DefaultCSEConfigProviderChain.getInstance().getConfig().getStreamName(),
        // DefaultCSEConfigProviderChain.getInstance().getConfig().getCategory()));

    }

    @Override
    public Object call() throws Exception {
        long receivedEventsCount = 0L;
        Event lastReceivedEvent = new Event("None", "None");
        while (true) {
            // (1) Check new events that were fed to us via Callback, then clear buffer
            synchronized (receivedEventsBuffer) {
                receivedEventsCount += receivedEventsBuffer.size();
                if (receivedEventsBuffer.size() > 0) {
                    lastReceivedEvent = receivedEventsBuffer.get(receivedEventsBuffer.size() - 1);
                }
                receivedEventsBuffer.clear();
            }

            LOGGER.debug("last_received_event" + lastReceivedEvent);
            // There is no reason to keep checking the onReceive buffer as the SDK Reader gets new events
            Thread.sleep(1000);
        }
    }

    /**
     * Callback interface-- this is how the Reader portion of the SDK will communicate new events to Customer; NOTE:
     * BufferingCallableReader class encapsulates this callback logic and provides direct event retrieval methods You
     * may find it more straightforward to use that instead of following this Callback approach
     */
    private class NewEventsCallback implements Read.Handler {

        /**
         * @param list New events onReceive
         * @param checkpoint checkpoint token pointing to the event next to last one in
         * @param aLongCheckpoint same as long
         * @return receiving event status, true for success and false for failure
         */
        @Override
        public boolean onReceive(final List<Event> events, final String checkpoint, final Long longCheckpoint) {
            for (final Event event : events) {
                LOGGER.debug("Received Event " + eventCounter + " :");
                LOGGER.debug(event.toString());
                LOGGER.debug("Event Header: " + event.headers().toString() + "\n\n");
                notifications.add(new ChangeNotificationMessage(event.toString(), event.headers().toString()));

                eventCounter++;
            }
            synchronized (receivedEventsBuffer) {
                receivedEventsBuffer.addAll(events);
                return true;
            }
        }
    }

    public boolean terminate() {
        reader.terminate();
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

}
