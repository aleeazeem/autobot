package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.CseECNotification;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.ChangeNotificationProducer;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test Case : CSE notifications being published in CSE channel for different types as following 1. changeNotifications
 * 2. subscriptionChangeNotification
 *
 * @author t_mohag
 */
public class SubscriptionECStatusTest extends SeleniumWebdriver {

    private ChangeNotificationConsumer personMasterConsumer = null;
    private ChangeNotificationProducer personMasterProducer = null;
    private String personMasterNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private String ecUser;
    private Long timeOut = 7000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionECStatusTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanPlatform resource = pelicanResource.platform();

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        personMasterNotificationChannel = getEnvironmentVariables().getPersonMasterNotificationChannel();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String AUTH_URL = getEnvironmentVariables().getAuthUrl();
        ecUser = getEnvironmentVariables().getUserExternalKey();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(AUTH_URL);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String ACCESS_TOKEN = authClient.getAuthToken();

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource);
        // Initialize Consumer
        personMasterConsumer = cseHelper.initializeConsumer(brokerUrl, personMasterNotificationChannel, ACCESS_TOKEN);
        personMasterProducer = cseHelper.initializeProducer(brokerUrl, personMasterNotificationChannel, authClient);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateConsumer(personMasterConsumer, personMasterNotificationChannel, eventsList);
        personMasterProducer.terminate();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
        personMasterConsumer.clearNotificationsList();
    }

    /*
     * Test case to change EC status to Review
     */
    @Test
    public void verifyReviewECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.REVIEW.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Accept
     */
    @Test
    public void verifyAcceptECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.ACCEPT.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));
        LOGGER.info("Constructed EC Notification Message: " + ecNotification);

        final String ecAcceptNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecAcceptNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        LOGGER.info("Person Master Consumer Events List Size: " + eventsList.size());
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found in Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    // @Test( threadPoolSize =1, invocationCount =
    // 100, timeOut = 10000)
    public void verifyLoadTestConsumerApp() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.ACCEPT.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecAcceptNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecAcceptNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Block
     */
    @Test
    public void verifyBlockECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.BLOCK.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecBlockNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecBlockNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Hard Block
     */
    @Test
    public void verifyHardBlockECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.HARDBLOCK.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecHardBlockNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecHardBlockNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Unverified
     */
    @Test
    public void verifyUnverifiedECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.UNVERIFIED.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecUnverifiedNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecUnverifiedNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Reopen
     */
    @Test
    public void verifyReopenECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.REOPEN.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecReopenNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReopenNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid state
     */
    @Test
    public void verifyInvalidValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus("DUMMY");
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecInvalidValueNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidValueNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is not found by Consumer", messageFound, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void verifyInvalidFormatMissingFieldECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidNotification =
            "{\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"]," + "\"SummaryECStatus\":\""
                + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\"" + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            LOGGER.info("Print:  " + message.getData());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void verifyInvalidFormatInvalidSyntaxECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidNotification =
            "\"oxygen_id\":[\"7MYJKXZLG8BN\"],\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"],"
                + "\"SummaryECStatus\":\"" + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\""
                + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            LOGGER.info("Print:  " + message.getData());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Null Value
     */
    @Test
    public void verifyNullValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = {};
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(null);
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecNullValueNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecNullValueNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is found in Consumer", messageFound, equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to EmptyValue
     */
    @Test
    public void verifyEmptyValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        Boolean messageFound = false;
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = {};
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus("DUMMY");
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecEmptyValueNotification = CSEHelper.buildMessage(ecNotification);

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecEmptyValueNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            final CseECNotification ecConsumerNotification =
                CSEHelper.getDataFromChangeNotificationMessage(message.getData());
            if (CSEHelper.comparePOJO(ecConsumerNotification, ecNotification)) {
                LOGGER.info("Printing relevant Notification from Consumer:  " + message.getData());
                messageFound = true;
            }
        }
        AssertCollector.assertThat("Published message is found in Consumer", messageFound, equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void verifyInvalidFormatECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormat(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidFormatNotification = "{\"oxy_id\":[\"" + ecUser
            + "\"],\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"]," + "\"SummaryECStatus\":\""
            + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\"" + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidFormatNotification));

        personMasterConsumer.waitForEvents(timeOut);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        for (final ChangeNotificationMessage message : eventsList) {
            LOGGER.info("Print:  " + message.getData());
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
