package com.autodesk.bsm.pelican.cse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BootstrapEntityType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapEventStatusPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;

import java.util.List;

/**
 * Helper class for bootstrap related tests.
 */
public class HelperForAssertions {

    public static void assertionsForBootstrap(final BootstrapEventStatusPage bootstrapEventStatusPage,
        final String category, final int eventsSize, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect id value of the bootstrap job", bootstrapEventStatusPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect category value of the bootstrap job",
            bootstrapEventStatusPage.getCategory(), equalTo(category), assertionErrorList);
        AssertCollector.assertThat("Incorrect status value of the bootstrap job", bootstrapEventStatusPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Empty events list",
            Integer.parseInt(bootstrapEventStatusPage.getProcessedRecordsCount()), is(eventsSize), assertionErrorList);
    }

    /**
     * This is a helper method which will do the assertions on the CSE message, when the number of expected events is
     * unknown
     *
     * @param bootstrapEventStatusPage
     * @param category
     * @param eventsSize
     * @param status
     * @param assertionErrorList
     */
    public static void assertionsForBootstrapWhenEventsAreUnKnown(
        final BootstrapEventStatusPage bootstrapEventStatusPage, final String category, final int eventsSize,
        final Status status, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect id value of the bootstrap job", bootstrapEventStatusPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect category value of the bootstrap job",
            bootstrapEventStatusPage.getCategory(), equalTo(category), assertionErrorList);
        AssertCollector.assertThat("Incorrect status value of the bootstrap job", bootstrapEventStatusPage.getStatus(),
            equalTo(status.toString()), assertionErrorList);
        AssertCollector.assertThat("Empty events list",
            Integer.parseInt(bootstrapEventStatusPage.getProcessedRecordsCount()), greaterThanOrEqualTo(eventsSize),
            assertionErrorList);
    }

    /**
     * This is a method for common Assertions on generated change notifications
     *
     * @param eventsList
     * @param entityType
     * @param assertionErrorList
     */
    public static void commonAssertionsOnChangeNotifications(final List<ChangeNotificationMessage> eventsList,
        final String entityType, final List<AssertionError> assertionErrorList) {

        for (final ChangeNotificationMessage message : eventsList) {

            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(message.getData());

            AssertCollector.assertThat("Incorrect type of notifications", cseEvent.getData().getType(),
                equalTo(PelicanConstants.CHANGE_NOTIFICATIONS), assertionErrorList);
            AssertCollector.assertThat("Incorrect change type of change notifications",
                cseEvent.getData().getAttributes().getChangeType(), equalTo(PelicanConstants.UPDATED),
                assertionErrorList);

            if (entityType.equalsIgnoreCase(BootstrapEntityType.SUBSCRIPTION.getDisplayName())) {
                AssertCollector.assertThat("Incorrect category of change notifications",
                    cseEvent.getData().getAttributes().getSubject(), equalTo(PelicanConstants.SUBSCRIPTION),
                    assertionErrorList);
            }
            if (entityType.equalsIgnoreCase(BootstrapEntityType.PURCHASE_ORDER.getDisplayName())) {
                AssertCollector.assertThat("Incorrect category of change notifications",
                    cseEvent.getData().getAttributes().getSubject(), equalTo(PelicanConstants.PURCHASEORDER),
                    assertionErrorList);
            }
            if (entityType.equalsIgnoreCase(BootstrapEntityType.ENTITLEMENT.getDisplayName())) {
                AssertCollector.assertThat("Incorrect category of change notifications",
                    cseEvent.getData().getAttributes().getSubject(), equalTo(PelicanConstants.ENTITLEMENT),
                    assertionErrorList);
            }
            if (entityType.equalsIgnoreCase(BootstrapEntityType.SUBSCRIPTION_PLAN.getDisplayName())) {
                AssertCollector.assertThat("Incorrect category of change notifications",
                    cseEvent.getData().getAttributes().getSubject(), equalTo(PelicanConstants.SUBSCRIPTION_OFFERING),
                    assertionErrorList);
            }
            if (entityType.equalsIgnoreCase(BootstrapEntityType.BASIC_OFFERING.getDisplayName())) {
                AssertCollector.assertThat("Incorrect category of change notifications",
                    cseEvent.getData().getAttributes().getSubject(), equalTo(PelicanConstants.BASIC_OFFERING),
                    assertionErrorList);
            }
        }

    }
}
