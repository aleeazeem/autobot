package com.autodesk.bsm.pelican.util;

import com.autodesk.platform.cse.v2.config.DefaultCSEConfigProviderChain;
import com.autodesk.platform.cse.v2.eventing.Event;
import com.autodesk.platform.cse.v2.eventing.impl.DefaultFailedEventProcessor;
import com.autodesk.platform.cse.v2.eventing.impl.Writer;
import com.autodesk.platform.cse.v2.util.Commons;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/********************************************************************************************
 * Encapsulate logic to produce events continuously, as well as all applicable configuration
 *********************************************************************************************/
public class Producer implements Callable {

    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private static final int EVENTS_PER_BATCH = 100;

    private Writer writer;
    private DefaultFailedEventProcessor failedEventProcessor = new DefaultFailedEventProcessor();

    public Producer() {
        // Initialize a Producer object
        this.writer = new Writer.Builder()
            // Load Parameters from the *cse_kinesis_config.properties* file
            .configProvider(DefaultCSEConfigProviderChain.getInstance())
            // Must provide a failed event processor when using AsyncPublish API
            .failedEventProcessor(this.failedEventProcessor)
            // ==========================================
            // OPTIONAL performance tuning parameters (defaults are adequate for 90% of use cases)
            // How many threads will be used to service Async Publish requests in parallel
            .setThreadCount(16)
            // How long a record will be kept alive in the queue since a Publish request is made, in milliseconds
            .setRecordTTL(30000)
            // Max queue size for pending Publish requests, in bytes
            .setMaxPendingBytes(32 * 1024 * 1024)
            // SDK splits larger payloads into batches and publishes them into stream separately;
            // this param determines whether failed batches will be retried
            .setRetryFailed(true)
            // HTTP timeout for AWS Publish-to-stream requests
            .setRequestTimeout(15000)
            // ==========================================
            //
            .build();
    }

    @Override
    public Object call() throws Exception {
        long counter = 1L;
        long failedCounter = 0L;
        long exceptionCounter = 0L;

        while (true) {

            // (1) Generate a batch of events (larger batches result in better throughput)
            final List<Event> events = new ArrayList<>();
            for (int i = 0; i < EVENTS_PER_BATCH; i++) {
                // (a) An event may contain any number of arbitrary headers
                final Map<String, Object> headers = new HashMap<>();
                headers.put("your_custom_header_could_be_anything", "YourCustomHeaderValue-" + counter);
                // CATEGORY is a "magic" header to be used by Consumer filtering
                headers.put(Commons.CATEGORY, "my-polyglot_example_java-oh_yeah");

                // (b) The raw data payload-- can contain any data up to a limit
                final byte[] payload =
                    ("any_data_whatsoever_can_go_here_as_long_as_it_does_not_exceed_250_kilobytes_" + counter)
                        .getBytes();

                // (c) Build an event object
                final Event e = new Event(headers, payload);
                events.add(e);

                counter++;
            }
            // ======================================================

            // (2) Publish the event batch just generated
            // NOTE: here we are using the Asynchronous API that does NOT guarantee delivery order
            // You may use any other of the 3 remaining Publisher APIs to meet your particular needs
            try {
                writer.publishAsync(events);
            } catch (final Exception e) { // always check for exceptions: things can always go wrong when comms are
                                          // involved
                exceptionCounter++;
                log.error("PRODUCER: Unexpected Exception Thrown on Produce: " + ExceptionUtils.getFullStackTrace(e));
            }
            // ======================================================

            // (3) See if Publisher asynchronously informed us of any event send failures-- that is a NORMAL occurrence
            // (NOTE1: there is a single buffer of failed events that can only be retrieved once)
            // (NOTE2: with SYNC Publish APIs the failed events are returned by those APIs)
            if (failedEventProcessor.getFailureCnt() > 0) {

                // (4) If any events were failed to be sent, send them again
                final List<Event> failedEvents = failedEventProcessor.getFailures();
                failedCounter += failedEvents.size();

                try {
                    writer.publishAsync(failedEvents);
                } catch (final Exception e) {
                    // always check for exceptions: things can always go wrong when comms are involved
                    exceptionCounter++;
                    log.error("PRODUCER: Unexpected Exception Thrown when retrying failed events: "
                        + ExceptionUtils.getFullStackTrace(e));
                } finally {
                    failedEventProcessor.resetOutstandingFailures();// forget all the failed events
                }
            }
            // ========================================================
            // (5) Writer capacity is limited due to Publisher internal buffer size,record TTL as well as AWS Throttling
            // The Producing application should pace itself according to Publisher settings and Channel Capacity
            Thread.sleep(1000);

            log.info("\r\n==============================================================");
            log.info("PRODUCER Events Attempted to be Sent: " + counter);
            log.info("PRODUCER Events Failed to be Sent: " + failedCounter);
            log.info("PRODUCER Unexpected Exceptions: " + exceptionCounter);
            log.info("PRODUCER Total MB Sent: " + writer.getTotalBytesSent());
            log.info("=================================================================");
        }
    }

    public boolean terminate() {
        writer.terminate();
        return true;
    }
}
