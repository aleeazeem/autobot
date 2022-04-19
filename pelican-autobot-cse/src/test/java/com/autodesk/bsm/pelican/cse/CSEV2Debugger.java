package com.autodesk.bsm.pelican.cse;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.Executors;

public class CSEV2Debugger {

    private List<ChangeNotificationMessage> eventsList;
    private Consumer consumerV2 = new Consumer();
    // private Producer producerV2 = new Producer();
    private static final Logger LOGGER = LoggerFactory.getLogger(CSEConsumerDebugger.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        Executors.newSingleThreadExecutor().submit(consumerV2);
        // Executors.newSingleThreadExecutor().submit(producerV2);
        // producerV2.call();
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");

    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        // producerV2.terminate();
        consumerV2.terminate();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$ COMPLETED $$$$$$$$$$$");
    }

    @Test(enabled = true)
    public void testDebugConsumer() {

        consumerV2.waitForEvents(120000);
        eventsList = consumerV2.getNotifications();
        LOGGER.info("Total events received in eventsList: " + eventsList.size());
        for (final ChangeNotificationMessage message : eventsList) {
            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
            CSEHelper.helperToPrintEventData(cseEvent);
        }
    }
}
