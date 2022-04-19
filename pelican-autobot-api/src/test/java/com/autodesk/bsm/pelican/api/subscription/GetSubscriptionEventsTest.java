package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.SubscriptionEventType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ChangeExportControlStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.Iterables;

import org.apache.http.ParseException;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This test class tests find subscription events api.
 *
 * @author Muhammad
 *
 */
public class GetSubscriptionEventsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private Object apiResponse;
    private JobsClient jobsResource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private JSubscriptionEvents subscriptionEvents;
    private AdminToolPage adminToolPage;
    private FindSubscriptionsPage findSubscriptionPage;
    private String subscriptionId;
    private static final String EVENT_TYPE = "eventTypes";
    private static final String CREATED_ON_OR_AFTER = "createdOnOrAfter";
    private static final String TYPE = "subscription-event";
    private SubscriptionDetailPage subscriptionDetailPage;
    private SubscriptionActivity chargeEventFields;
    private SubscriptionActivity startPhaseEventFields;
    private SubscriptionActivity addSeatsEventFields;
    private SubscriptionActivity refundEventFields;
    private SubscriptionActivity reduceSeatsEventFields;
    private SubscriptionActivity ecChangeEventFields;
    private SubscriptionActivity subscriptionMigrationEventFields;
    private SubscriptionActivity editEventFields;
    private SubscriptionActivity cancelEventFields;
    private SubscriptionActivity expirationReminderEventFields;
    private SubscriptionActivity restartASubscriptionEventFields;
    private int totalEventsOfASubscription;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        subscriptionId = createSubscriptionAndStartPhaseAndChargeEvents();
        addSeatsEventFields = createEventForAddSeats(subscriptionId);

        refundEventFields = createEventForRefund(addSeatsEventFields.getPurchaseOrder());
        reduceSeatsEventFields = createEventForReduceSeats(subscriptionId);
        ecChangeEventFields = createEcStatusChangeEvent();
        subscriptionMigrationEventFields = createEventForSubscriptionMigration();

        editEventFields = createEventForEditSubscription();
        cancelEventFields = createEventForCancelASubscription(subscriptionId);
        expirationReminderEventFields = createEventForExpireASubscription();
        restartASubscriptionEventFields = createEventForRestartASubscription(subscriptionId);
        subscriptionDetailPage.refreshPage();
        totalEventsOfASubscription = subscriptionDetailPage.getSubscriptionActivity().size();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result would contain the result of the test method.
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        apiResponse = null;
    }

    /**
     * This method tests find subscription events api returns all events in response which are associated with given
     * subscription in sequence.
     *
     * @throws IOException
     * @throws ParseException
     * @throws java.text.ParseException
     */
    @Test
    public void findSubscriptionEventsByIdTest() throws ParseException, IOException, java.text.ParseException {
        apiResponse = resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, null);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        subscriptionDetailPage.refreshPage();
        final int totalEventsOfASubscription = subscriptionDetailPage.getSubscriptionActivity().size();
        AssertCollector.assertThat("Total events are nto correct for subcription: " + subscriptionId,
            subscriptionEvents.getEventsData().size(), equalTo(totalEventsOfASubscription), assertionErrorList);
        for (int i = 0; i < subscriptionEvents.getEventsData().size(); i++) {
            AssertCollector.assertThat("Event type is null for subscription: " + subscriptionId,
                subscriptionEvents.getEventsData().get(i).getEventType(), is(notNullValue()), assertionErrorList);
            // starting with 3rd event because first two events (i.e start phase and charge have same time)
            if (i > 1) {
                final Date dateOfCurrentEvent = DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT,
                    subscriptionEvents.getEventsData().get(i).getCreatedDate().split(" UTC")[0]);
                final Date dateOfPreviousEvent = DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT,
                    subscriptionEvents.getEventsData().get(i - 1).getCreatedDate().split(" UTC")[0]);
                AssertCollector.assertThat("Sequence of events are not correct for subscription: " + subscriptionId,
                    dateOfCurrentEvent, greaterThan(dateOfPreviousEvent), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests find subscription events by event type.
     *
     * @param eventType
     * @param expectedEventData
     * @throws ParseException
     * @throws IOException
     */
    @Test(dataProvider = "subscriptionEventType")
    public void findSubscriptionEventsByIdAndEventTest(final String eventType,
        final SubscriptionActivity expectedEventData) throws ParseException, IOException {
        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(EVENT_TYPE, eventType);
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;

        for (final SubscriptionEventsData subscriptionEventData : subscriptionEvents.getEventsData()) {
            AssertCollector.assertThat(
                "Type of an event: '" + eventType + "' is not coorrect for subscription: " + subscriptionId,
                subscriptionEventData.getType(), equalTo(TYPE), assertionErrorList);
            AssertCollector.assertThat(
                "Event type: '" + eventType + "' is not coorrect for subscription: " + subscriptionId,
                subscriptionEventData.getEventType(), equalTo(eventType), assertionErrorList);
            AssertCollector.assertThat(
                "Id of an event: '" + eventType + "' is null for subscription: " + subscriptionId,
                subscriptionEventData.getId(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat(
                "Date of an event: '" + eventType + "' is not correct for subscription: " + subscriptionId,
                subscriptionEventData.getCreatedDate(), equalTo(expectedEventData.getDate()), assertionErrorList);
            String expectedRequesterName = null;
            if (expectedEventData.getRequestor() != null) {
                expectedRequesterName = expectedEventData.getRequestor().split(" ")[0];
            }
            if (expectedEventData.getGrant() != null) {
                AssertCollector.assertThat(
                    "Grant of an event: '" + eventType + "' is not correct for subscription: " + subscriptionId,
                    subscriptionEventData.getGrant(), equalTo(expectedEventData.getGrant()), assertionErrorList);
            }
            AssertCollector.assertThat(
                "Requester of an event: '" + eventType + "' is not correct for subscription: " + subscriptionId,
                subscriptionEventData.getRequesterName(), equalTo(expectedRequesterName), assertionErrorList);
            AssertCollector.assertThat(
                "Memo of an event: '" + eventType + "' is not correct for subscription: " + subscriptionId,
                subscriptionEventData.getMemo(), equalTo(expectedEventData.getMemo()), assertionErrorList);
            AssertCollector.assertThat(
                "PO id of an event: '" + eventType + "' is not correct for subscription: " + subscriptionId,
                subscriptionEventData.getPurchaseOrderId(), equalTo(expectedEventData.getPurchaseOrder()),
                assertionErrorList);
            if (eventType.equals(SubscriptionEventType.CANCEL.toString())) {
                AssertCollector.assertThat(
                    "Cancellation policy of an event: '" + eventType + "' is not correct for subscription: "
                        + subscriptionId,
                    subscriptionEventData.getCancllationPolicy(),
                    equalTo(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD.getDisplayName()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests find subscription events by createdOnOrAfter.
     *
     * @throws ParseException
     * @throws IOException
     * @throws java.text.ParseException
     */
    @Test
    public void findSubscriptionEventsByCreatedOnOrAfter()
        throws ParseException, IOException, java.text.ParseException {
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        final int totalSubscriptionActivity = subscriptionDetailPage.getNumberOfSubscriptionActivity();
        final String selectedDate = subscriptionDetailPage.getSubscriptionActivity(totalSubscriptionActivity - 4).get(0)
            .getDate().split(" ")[0];

        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(CREATED_ON_OR_AFTER, selectedDate.replaceAll("/", "-"));
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;

        final Date dateCreatedOnOrAfter =
            DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT, selectedDate + " 00:00:00");
        for (int i = 0; i < subscriptionEvents.getEventsData().size(); i++) {
            final Date actualDate = DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT,
                subscriptionEvents.getEventsData().get(i).getCreatedDate().split(" UTC")[0]);
            AssertCollector.assertThat("Sequence of events are not correct for subscription: " + subscriptionId,
                actualDate, greaterThan(dateCreatedOnOrAfter), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests find subscription events by multiple event types.
     *
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void findSubscriptionEventsByMultipleEventsType() throws ParseException, IOException {
        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(EVENT_TYPE, SubscriptionEventType.REFUNDED.toString() + ","
            + SubscriptionEventType.CANCEL.toString() + "," + SubscriptionEventType.EDIT.toString());
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;

        AssertCollector.assertThat("Total number Of events are not correct for subscription: " + subscriptionId,
            subscriptionEvents.getEventsData().size(), equalTo(3), assertionErrorList);
        for (final SubscriptionEventsData subscriptionEventData : subscriptionEvents.getEventsData()) {
            AssertCollector.assertThat("Event Type is not correct subscription: " + subscriptionId,
                subscriptionEventData.getEventType(), isOneOf(SubscriptionEventType.REFUNDED.toString(),
                    SubscriptionEventType.CANCEL.toString(), SubscriptionEventType.EDIT.toString()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the error for invalid subscription id.
     *
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void findSubscriptionEventsByInvalidSubscriptionId() throws ParseException, IOException {
        final String invalidSubscriptionId = "123wer4567";
        apiResponse = resource.getSubscriptionEventsClient().getSubscriptionEvents(invalidSubscriptionId, null);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        AssertCollector.assertThat("Invalid subscription id", subscriptionEvents.getErrors().get(0).getStatus(),
            equalTo(HttpStatus.BAD_REQUEST.value()), assertionErrorList);
        AssertCollector.assertThat("Invalid detail message", subscriptionEvents.getErrors().get(0).getDetail(),
            equalTo("Invalid Subscription Id in path."), assertionErrorList);
        AssertCollector.assertThat("Invalid code", subscriptionEvents.getErrors().get(0).getCode(), equalTo("990002"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the subscription events by non existing subscription id.
     *
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void findSubscriptionEventsByNonExistingSubscriptionId() throws ParseException, IOException {
        final String invalidSubscriptionId = "12345678909876655";
        apiResponse = resource.getSubscriptionEventsClient().getSubscriptionEvents(invalidSubscriptionId, null);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            Assert.fail("Bad request with status of " + httpError.getStatus());
        } else {
            subscriptionEvents = (JSubscriptionEvents) apiResponse;
        }
        AssertCollector.assertThat("Data is not null for non existing subscription id.",
            subscriptionEvents.getEventsData(), equalTo(null), assertionErrorList);
        AssertCollector.assertThat("Errors are not null for non existing subscription id." + subscriptionId,
            subscriptionEvents.getErrors(), equalTo(null), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests find subscription events by invalid event type.
     *
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void findSubscriptionEventsByInvalidEvent() throws ParseException, IOException {
        final String invalidEvent = "Pelican";
        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(EVENT_TYPE, invalidEvent);
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;

        AssertCollector.assertThat("Invalid status", subscriptionEvents.getErrors().get(0).getStatus(),
            equalTo(HttpStatus.BAD_REQUEST.value()), assertionErrorList);
        AssertCollector.assertThat("Invalid detail message", subscriptionEvents.getErrors().get(0).getDetail(),
            equalTo("Invalid event types."), assertionErrorList);
        AssertCollector.assertThat("Invalid code", subscriptionEvents.getErrors().get(0).getCode(), equalTo("990002"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the pagination with cursor.
     *
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void testGetSubscritionEventsPagination() throws ParseException, IOException {
        final int blockSize = 4;

        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(PelicanConstants.BLOCK_SIZE, String.valueOf(blockSize));
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        AssertCollector.assertThat("Number of events are not correct on first page for subscription: " + subscriptionId,
            subscriptionEvents.getEventsData().size(), equalTo(blockSize), assertionErrorList);
        final String nextCursorValueOnFirstPage = subscriptionEvents.getLinks().getNext();
        final String prevCursorValueOnFirstPage = subscriptionEvents.getLinks().getPrev();
        AssertCollector.assertThat("Next Cursor value is null on first page for subscription: " + subscriptionId,
            nextCursorValueOnFirstPage, notNullValue(), assertionErrorList);
        AssertCollector.assertThat(
            "Previous cursor value is not null on first page for subscription: " + subscriptionId,
            prevCursorValueOnFirstPage, nullValue(), assertionErrorList);

        // validation on second page
        requestParametersMap.clear();
        requestParametersMap.put(PelicanConstants.BLOCK_SIZE, String.valueOf(blockSize));
        requestParametersMap.put(PelicanConstants.NEXT_CURSOR, nextCursorValueOnFirstPage);
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        final String nextCursorValueOnSecondPage = subscriptionEvents.getLinks().getNext();
        final String prevCursorValueOnSecondPage = subscriptionEvents.getLinks().getPrev();
        AssertCollector.assertThat("Invalid error message", subscriptionEvents.getEventsData().size(),
            equalTo(blockSize), assertionErrorList);
        AssertCollector.assertThat("Next Cursor value is null on second page for subscription: " + subscriptionId,
            nextCursorValueOnSecondPage, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Previous cursor value is null on second page for subscription: " + subscriptionId,
            prevCursorValueOnSecondPage, notNullValue(), assertionErrorList);

        // validation on third page
        requestParametersMap.clear();
        requestParametersMap.put(PelicanConstants.BLOCK_SIZE, String.valueOf(blockSize));
        requestParametersMap.put(PelicanConstants.NEXT_CURSOR, nextCursorValueOnSecondPage);
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        final String nextCursorValueOnThirdPage = subscriptionEvents.getLinks().getNext();
        final String prevCursorValueOnThirdPage = subscriptionEvents.getLinks().getPrev();
        AssertCollector.assertThat("Invalid error message", subscriptionEvents.getEventsData().size(),
            equalTo(totalEventsOfASubscription - blockSize * 2), assertionErrorList);
        AssertCollector.assertThat("Next Cursor value is not null on third page for subscription: " + subscriptionId,
            nextCursorValueOnThirdPage, nullValue(), assertionErrorList);
        AssertCollector.assertThat("Previous cursor value is null on third page for subscription: " + subscriptionId,
            prevCursorValueOnThirdPage, notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies if an invalid cursor value is provided, the value is ignored and first page of the
     * results is returned.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testGetSubscritionEventsPaginationWithInvalidNextCursorValue() throws ParseException, IOException {

        final HashMap<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(PelicanConstants.NEXT_CURSOR, "Pelican");
        apiResponse =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, requestParametersMap);
        subscriptionEvents = (JSubscriptionEvents) apiResponse;
        AssertCollector.assertThat("All events of subscription are not returned for subscription:" + subscriptionId,
            subscriptionEvents.getEventsData().size(), equalTo(totalEventsOfASubscription), assertionErrorList);
        AssertCollector.assertThat(
            "Previous cursor value is not null on third page for subscription: " + subscriptionId,
            subscriptionEvents.getLinks().getPrev(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Next cursor value is not null on third page for subscription: " + subscriptionId,
            subscriptionEvents.getLinks().getNext(), nullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "subscriptionEventType")
    public Object[][] getTestDataForSubscriptionEventType() {
        return new Object[][] { { SubscriptionEventType.START_PHASE.toString(), startPhaseEventFields },
                { SubscriptionEventType.CHARGE.toString(), chargeEventFields },
                { SubscriptionEventType.ADD_SEATS.toString(), addSeatsEventFields },
                { SubscriptionEventType.REFUNDED.toString(), refundEventFields },
                { SubscriptionEventType.REDUCE_SEATS.toString(), reduceSeatsEventFields },
                { SubscriptionEventType.EC_CHANGE.toString(), ecChangeEventFields },
                { SubscriptionEventType.SUB_MIGRATED.toString(), subscriptionMigrationEventFields },
                { SubscriptionEventType.EDIT.toString(), editEventFields },
                { SubscriptionEventType.CANCEL.toString(), cancelEventFields },
                { SubscriptionEventType.EXPIRATION_REMINDER_SENT.toString(), expirationReminderEventFields },
                { SubscriptionEventType.RESTART.toString(), restartASubscriptionEventFields } };
    }

    private String createSubscriptionAndStartPhaseAndChargeEvents() {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        startPhaseEventFields = new SubscriptionActivity();
        startPhaseEventFields.setDate(purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptions().getSubscription().get(0).getSubscriptionPeriodStartDate());
        startPhaseEventFields.setActivity(SubscriptionEventType.START_PHASE.toString());
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        chargeEventFields = new SubscriptionActivity();
        chargeEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        chargeEventFields.setActivity(SubscriptionEventType.CHARGE.toString());
        chargeEventFields.setCharge(getBicSubscriptionPlan().getIncluded().getPrices().get(2).getAmount() + " "
            + getBicSubscriptionPlan().getIncluded().getPrices().get(2).getCurrency());
        chargeEventFields.setGrant(subscriptionDetailPage.getLastSubscriptionActivity().getGrant());
        chargeEventFields.setPurchaseOrder(purchaseOrder.getId());

        return purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
    }

    private SubscriptionActivity createEventForAddSeats(final String subscriptionId) {
        // get one date to change Subscriptions next billing date
        final int numberOfAddedDays = 15;
        final String addSeats = "3";
        final String changedNextBillingDate =
            DateTimeUtils.changeDateFormat(DateTimeUtils.getNowPlusDays(numberOfAddedDays),
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        final SubscriptionActivity addSeatsEventFields = new SubscriptionActivity();

        // Changing DB for subscription table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_FOR_UPDATE_NEXT_BILLING_DATE,
            changedNextBillingDate, subscriptionId), getEnvironmentVariables());

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), getBicMonthlyUsPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), addSeats);

        final PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", getBuyerUser());
        final String purchaseOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderIdForAddedSeats,
            ECStatus.ACCEPT);
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrderIdForAddedSeats,
            ECStatus.ACCEPT);

        subscriptionDetailPage.refreshPage();
        addSeatsEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        final String chargeForAddSeats =
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getCharge();
        addSeatsEventFields.setCharge(chargeForAddSeats);
        addSeatsEventFields.setGrant(Integer.toString(numberOfAddedDays) + " Days");
        addSeatsEventFields.setPurchaseOrder(purchaseOrderIdForAddedSeats);
        addSeatsEventFields.setMemo("Added " + addSeats + " seats.");

        return addSeatsEventFields;
    }

    private SubscriptionActivity createEventForRefund(final String purchaseOrderId) {
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity refundEventFields = new SubscriptionActivity();
        refundEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        refundEventFields.setActivity(SubscriptionEventType.REFUNDED.toString());
        refundEventFields.setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        refundEventFields.setMemo("Refunded PO #" + purchaseOrderId + ". Reduced 3 seats.");
        return refundEventFields;
    }

    private SubscriptionActivity createEventForReduceSeats(final String subscriptionId) {
        final String numberOfSeatsReduced = "1";
        resource.reduceSeatsBySubscriptionId().reduceSeats(subscriptionId, numberOfSeatsReduced);
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity reduceSeatsEventFields = new SubscriptionActivity();
        reduceSeatsEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        reduceSeatsEventFields.setActivity(SubscriptionEventType.REDUCE_SEATS.toString());
        reduceSeatsEventFields.setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        reduceSeatsEventFields.setGrant(numberOfSeatsReduced + " Seat");
        return reduceSeatsEventFields;
    }

    private SubscriptionActivity createEcStatusChangeEvent() {
        final ECStatus newEcStatus = ECStatus.REVIEW;
        final String note = "Ec change memo";
        subscriptionDetailPage.refreshPage();
        final ChangeExportControlStatusPage changeExportControlStatusPage =
            subscriptionDetailPage.clickOnChangeExportControlStatusLink();
        changeExportControlStatusPage.helperToChangeEcStatus(newEcStatus, note);
        changeExportControlStatusPage.submit();
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity ecChangeEventFields = new SubscriptionActivity();
        ecChangeEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        ecChangeEventFields.setActivity(SubscriptionEventType.EC_CHANGE.toString());
        ecChangeEventFields.setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        ecChangeEventFields.setMemo(newEcStatus.getName() + "." + note);
        return ecChangeEventFields;
    }

    private SubscriptionActivity createEventForSubscriptionMigration() {
        final SubscriptionMigrationPage subscriptionMigrationPage =
            subscriptionDetailPage.clickOnMigrateSubscriptionLink();
        subscriptionMigrationPage.setPriceId(getBicYearlyUsPriceId());
        subscriptionDetailPage = subscriptionMigrationPage.clickOnMigrateButton();
        final SubscriptionActivity subscriptionMigrationEventFields = new SubscriptionActivity();
        subscriptionMigrationEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        subscriptionMigrationEventFields.setActivity(SubscriptionEventType.SUB_MIGRATED.toString());
        subscriptionMigrationEventFields
            .setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        subscriptionMigrationEventFields.setMemo(
            "Old Price Id: " + getBicMonthlyUsPriceId() + ". " + "New Price Id: " + getBicYearlyUsPriceId() + ".");
        return subscriptionMigrationEventFields;
    }

    private SubscriptionActivity createEventForEditSubscription() {
        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        final String newNextBillingDate = DateTimeUtils.getNowPlusDays(31);
        subscriptionDetailPage = editSubscriptionPage.editASubscription(newNextBillingDate, null, null, null);

        final SubscriptionActivity editEventFields = new SubscriptionActivity();
        editEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        editEventFields.setActivity(SubscriptionEventType.EDIT.toString());
        editEventFields.setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        editEventFields
            .setMemo(subscriptionDetailPage.getLastSubscriptionActivity().getMemo().replaceAll("\n", ".") + ".");
        return editEventFields;
    }

    private SubscriptionActivity createEventForCancelASubscription(final String subscriptionId) {
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity cancelEventFields = new SubscriptionActivity();
        cancelEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        cancelEventFields.setActivity(SubscriptionEventType.CANCEL.toString());
        cancelEventFields.setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        return cancelEventFields;
    }

    private SubscriptionActivity createEventForExpireASubscription() {
        jobsResource.subscriptionExpirationReminder(getEnvironmentVariables());
        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity expirationReminderEventFields = new SubscriptionActivity();
        expirationReminderEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        expirationReminderEventFields.setActivity(SubscriptionEventType.EXPIRATION_REMINDER_SENT.toString());
        return expirationReminderEventFields;
    }

    private SubscriptionActivity createEventForRestartASubscription(final String subscriptionId) {
        resource.subscription().restartCancelledSubscription(subscriptionId);
        subscriptionDetailPage.refreshPage();
        final SubscriptionActivity restartASubscriptionEventFields = new SubscriptionActivity();
        restartASubscriptionEventFields.setDate(subscriptionDetailPage.getLastSubscriptionActivity().getDate());
        restartASubscriptionEventFields.setActivity(SubscriptionEventType.EXPIRATION_REMINDER_SENT.toString());
        restartASubscriptionEventFields
            .setRequestor(subscriptionDetailPage.getLastSubscriptionActivity().getRequestor());
        return restartASubscriptionEventFields;
    }

}
