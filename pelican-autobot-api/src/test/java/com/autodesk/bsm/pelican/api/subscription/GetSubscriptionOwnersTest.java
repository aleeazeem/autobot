package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwnerData;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwners;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GetSubscriptionOwnersTest extends BaseTestData {

    private final String OFFERING_ID = "offeringId";
    private final String OFFERING_EXTERNAL_KEY = "offeringExternalKey";
    private final String SUBSCRIPTION_STATUSES = "subscriptionStatuses";
    private final String NEXT_CURSOR = "page[next-cursor]";
    private PelicanPlatform resource;
    private HashMap<String, String> requestParametersMap;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private String offeringId;
    private String offeringExternalKey;

    private final String userExternalKeyWithActiveSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithActiveSubscription;
    private final String userExternalKeyWithCancelledSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithCancelledSubscription;
    private final String userExternalKeyWithExpiredSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithExpiredSubscription;
    private final String userExternalKeyWithDelinquentSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithDelinquentSubscription;
    private final String userExternalKeyWithPendingSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithPendingSubscription;
    private final String userExternalKeyWithPendingMigrationSubscription =
        "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private String userIdWithPendingMigrationSubscription;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetSubscriptionOwnersTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // creating BiC Offering
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        offeringId = bicOffering.getOfferings().get(0).getId();
        offeringExternalKey = bicOffering.getOfferings().get(0).getExternalKey();
        final String priceId = bicOffering.getIncluded().getPrices().get(0).getId();
        userIdWithActiveSubscription =
            createSubscription(userExternalKeyWithActiveSubscription, priceId, SubscriptionStatus.ACTIVE);
        userIdWithCancelledSubscription =
            createSubscription(userExternalKeyWithCancelledSubscription, priceId, SubscriptionStatus.CANCELLED);
        userIdWithDelinquentSubscription =
            createSubscription(userExternalKeyWithDelinquentSubscription, priceId, SubscriptionStatus.DELINQUENT);
        userIdWithExpiredSubscription =
            createSubscription(userExternalKeyWithExpiredSubscription, priceId, SubscriptionStatus.EXPIRED);
        userIdWithPendingSubscription =
            createSubscription(userExternalKeyWithPendingSubscription, priceId, SubscriptionStatus.PENDING);
        userIdWithPendingMigrationSubscription = createSubscription(userExternalKeyWithPendingMigrationSubscription,
            priceId, SubscriptionStatus.PENDING_MIGRATION);

        LOGGER.info("userIdWithActiveSubscription: " + userIdWithActiveSubscription);
        LOGGER.info("userIdWithCancelledSubscription: " + userIdWithCancelledSubscription);
        LOGGER.info("userIdWithExpiredSubscription: " + userIdWithExpiredSubscription);
        LOGGER.info("userIdWithPendingSubscription: " + userIdWithPendingSubscription);
        LOGGER.info("userIdWithPendingMigrationSubscription: " + userIdWithPendingMigrationSubscription);
        LOGGER.info("userIdWithDelinquentSubscription: " + userIdWithDelinquentSubscription);
    }

    /**
     * This test verifies that user data is returned based on given subscription status and default status.
     *
     * @param subscriptionStatus
     * @param userIdList
     * @param userExternalKeyList
     */
    @Test(dataProvider = "getSubscriptionOwnerData")
    public void testSubscriptionOwnersWithDifferentStatus(final String subscriptionStatus,
        final List<String> userIdList, final List<String> userExternalKeyList) {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, offeringId);
        requestParametersMap.put(OFFERING_EXTERNAL_KEY, offeringExternalKey);
        requestParametersMap.put(SUBSCRIPTION_STATUSES, subscriptionStatus);
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response should contain only " + userIdList.size() + " user objects.",
            subscriptionOwners.getData().size(), equalTo(userIdList.size()), assertionErrorList);
        for (final SubscriptionOwnerData subscriptionOwnersData : subscriptionOwners.getData()) {
            AssertCollector.assertThat("Type should be user", subscriptionOwnersData.getType(), equalTo("user"),
                assertionErrorList);
            if (userIdList.contains(subscriptionOwnersData.getId())) {
                AssertCollector.assertThat(
                    "user external key is not correct for user id " + subscriptionOwnersData.getId(),
                    subscriptionOwnersData.getExternalKey(),
                    equalTo(userExternalKeyList.get(userIdList.indexOf(subscriptionOwnersData.getId()))),
                    assertionErrorList);
            } else {
                AssertCollector.assertFalse(
                    "This user id should not be returned in the reposnse: " + subscriptionOwnersData.getId(), true,
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that either offering id or offering external key is mandatory parameter.
     */
    @Test
    public void testSubscriptionOwnersWithoutAnyParameter() {
        requestParametersMap = new HashMap<>();
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response data should be null", subscriptionOwners.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Response status is not correct", subscriptionOwners.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertThat("Error message is not correct.", subscriptionOwners.getErrors().get(0).getDetail(),
            equalTo("Offering ID and Offer External Key cannot be empty!"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that no data is returned if a non-existing offering external key is provided.
     */
    @Test
    public void testSubscriptionOwnersWithNonExistingOfferingExternalKey() {
        // null response
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_EXTERNAL_KEY, "abcxyz");
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response data should be null", subscriptionOwners.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Response error should be null", subscriptionOwners.getErrors(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that no data is returned if a non-existing offering id is provided.
     */
    @Test
    public void testSubscriptionOwnersWithNonExistingOfferingId() {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, "999999999999");
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response error should be null", subscriptionOwners.getErrors(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Response data should be null", subscriptionOwners.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test verifies if offering id and offering external key both are provided then offering external key is
     * ignored.
     */
    @Test
    public void testSubscriptionOwnersWithNonMatchingOfferingIdAndExternalKey() {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, offeringId);
        requestParametersMap.put(OFFERING_EXTERNAL_KEY,
            getBicSubscriptionPlan().getOfferings().get(0).getExternalKey());
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        // only 3 user objects since user with default status should be returned. Subscriptions belonging to offering
        // external key are not returned.
        AssertCollector.assertThat("Response should contain only 3 user objects.", subscriptionOwners.getData().size(),
            equalTo(3), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that no user is returned if there is no subscription associated with the offering.
     */
    @Test
    public void testSubscriptionOwnersWithNoSubscriptions() {
        final Offerings offering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_EXTERNAL_KEY, offering.getOfferings().get(0).getExternalKey());
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response error should be null", subscriptionOwners.getErrors(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Response data should be null", subscriptionOwners.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the error message when an invalid status is passed.
     */
    @Test
    public void testSubscriptionOwnersWithInvalidStatus() {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, offeringId);
        requestParametersMap.put(SUBSCRIPTION_STATUSES, "abc");
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        AssertCollector.assertThat("Response data should be null", subscriptionOwners.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Response status is not correct", subscriptionOwners.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertThat("Error message is not correct.", subscriptionOwners.getErrors().get(0).getDetail(),
            equalTo("Invalid Subscription Status: \"abc\""), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies the pagination.
     */
    @Test
    public void testSubscriptionOwnersPagination() {
        // api doc does not support this parameter. This is added for testing purpose only.
        final String blockSize = "4";
        final String allSubscriptionStatus = SubscriptionStatus.ACTIVE.getDisplayName() + ","
            + SubscriptionStatus.CANCELLED.getDisplayName() + "," + SubscriptionStatus.DELINQUENT.getDisplayName() + ","
            + SubscriptionStatus.EXPIRED.getDisplayName() + "," + SubscriptionStatus.PENDING.getDisplayName() + ","
            + SubscriptionStatus.PENDING_MIGRATION.getDisplayName();
        final List<String> userIdList = Arrays.asList(userIdWithActiveSubscription, userIdWithCancelledSubscription,
            userIdWithDelinquentSubscription, userIdWithExpiredSubscription, userIdWithPendingSubscription,
            userIdWithPendingMigrationSubscription);
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, offeringId);
        requestParametersMap.put(SUBSCRIPTION_STATUSES, allSubscriptionStatus);
        requestParametersMap.put(PelicanConstants.BLOCK_SIZE, blockSize);

        // make first call to api, previous cursor should be null here and data should contain 4 users.
        SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        String nextCursorValue = subscriptionOwners.getLinks().getNext();
        String prevCursorValue = subscriptionOwners.getLinks().getPrev();
        AssertCollector.assertThat("Previous cursor value should be null on first page", prevCursorValue, nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Next cursor value should not be null on first page", nextCursorValue,
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("There should be 4 users on first page", subscriptionOwners.getData().size(),
            equalTo(4), assertionErrorList);
        // validating user id in the response
        for (int i = 0; i < 4; i++) {
            final SubscriptionOwnerData subscriptionOwnersData = subscriptionOwners.getData().get(i);
            AssertCollector.assertThat("User id " + i + " is not correct with first api call",
                subscriptionOwnersData.getId(), equalTo(userIdList.get(i)), assertionErrorList);
        }

        // make second call to api with next cursor value, next cursor should be null here since its the last page.
        requestParametersMap.put(NEXT_CURSOR, nextCursorValue);
        subscriptionOwners = resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        nextCursorValue = subscriptionOwners.getLinks().getNext();
        prevCursorValue = subscriptionOwners.getLinks().getPrev();
        AssertCollector.assertThat("Previous cursor value should not be null on last page",
            subscriptionOwners.getLinks().getPrev(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Next cursor value should be null on last page", nextCursorValue, nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("There should be 2 users on last page", subscriptionOwners.getData().size(),
            equalTo(2), assertionErrorList);
        // validating user id in the response
        for (int i = 0; i < 2; i++) {
            final SubscriptionOwnerData subscriptionOwnersData = subscriptionOwners.getData().get(i);
            AssertCollector.assertThat("User id " + i + " is not correct with second api call on last page",
                subscriptionOwnersData.getId(), equalTo(userIdList.get(i + 4)), assertionErrorList);
        }

        // make another call with previous cursor value so the result returned will be first page and previous cursor
        // value will be null
        // setting next cursor value to null otherwise next cursor takes precedence
        final String PREV_CURSOR = "page[prev-cursor]";
        requestParametersMap.put(NEXT_CURSOR, null);
        requestParametersMap.put(PREV_CURSOR, prevCursorValue);
        subscriptionOwners = resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        nextCursorValue = subscriptionOwners.getLinks().getNext();
        prevCursorValue = subscriptionOwners.getLinks().getPrev();
        AssertCollector.assertThat("Previous cursor value should be null on first page while using previous cursor",
            prevCursorValue, nullValue(), assertionErrorList);
        AssertCollector.assertThat("Next cursor value should not be null on first page while using previous cursor",
            nextCursorValue, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("There should be 4 users on first page while using previous cursor",
            subscriptionOwners.getData().size(), equalTo(4), assertionErrorList);
        for (int i = 0; i < 4; i++) {
            final SubscriptionOwnerData subscriptionOwnersData = subscriptionOwners.getData().get(i);
            AssertCollector.assertThat("User id " + i + " is not correct on first page while using previous cursor",
                subscriptionOwnersData.getId(), equalTo(userIdList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies if an invalid cursor value is provided, the value is ignored and first page of the
     * results is returned.
     */
    @Test
    public void testSubscriptionOwnersPaginationWithInvalidNextCursorValue() {
        // default subscription status is active, cancelled and expired
        final List<String> userIdList = Arrays.asList(userIdWithActiveSubscription, userIdWithCancelledSubscription,
            userIdWithDelinquentSubscription);
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(OFFERING_ID, offeringId);
        requestParametersMap.put(NEXT_CURSOR, "abcdef");

        // First page should be returned. 6 records will be returned and previous and next cursor should be null.
        final SubscriptionOwners subscriptionOwners =
            resource.getSubscriptionOwnersClient().getSubscriptionOwners(requestParametersMap);
        final String nextCursorValue = subscriptionOwners.getLinks().getNext();
        final String prevCursorValue = subscriptionOwners.getLinks().getPrev();
        AssertCollector.assertThat("Previous cursor value should be null on first page", prevCursorValue, nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Next cursor value should be null on first page", nextCursorValue, nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("There should be 6 users on first page", subscriptionOwners.getData().size(),
            equalTo(3), assertionErrorList);
        for (int i = 0; i < 3; i++) {
            final SubscriptionOwnerData subscriptionOwnersData = subscriptionOwners.getData().get(i);
            AssertCollector.assertThat("User id " + i + " is not correct on first page", subscriptionOwnersData.getId(),
                equalTo(userIdList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to create subscription with different status.
     *
     * @param userExternalKey
     * @param priceId
     * @param subscriptionStatus
     * @return userId
     */
    private String createSubscription(final String userExternalKey, final String priceId,
        final SubscriptionStatus subscriptionStatus) {
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(userExternalKey);

        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 1).getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.UPDATE_SUBSCRIPTION_STATUS, subscriptionStatus.ordinal(), subscriptionId),
            getEnvironmentVariables());
        return user.getId();
    }

    /**
     * Data provider for testSubscriptionOwnersWithDifferentStatus test method.
     *
     * @return Object[][]
     */
    @DataProvider(name = "getSubscriptionOwnerData")
    public Object[][] getSubscriptionOwnerData() {
        return new Object[][] {
                // data for default subscription status
                { "", Arrays.asList(userIdWithActiveSubscription, userIdWithCancelledSubscription,
                    userIdWithDelinquentSubscription),
                        Arrays.asList(userExternalKeyWithActiveSubscription, userExternalKeyWithCancelledSubscription,
                            userExternalKeyWithDelinquentSubscription) },

                { SubscriptionStatus.ACTIVE.getDisplayName(), Arrays.asList(userIdWithActiveSubscription),
                        Arrays.asList(userExternalKeyWithActiveSubscription) },

                { SubscriptionStatus.CANCELLED.getDisplayName(), Arrays.asList(userIdWithCancelledSubscription),
                        Arrays.asList(userExternalKeyWithCancelledSubscription) },

                { SubscriptionStatus.DELINQUENT.getDisplayName(), Arrays.asList(userIdWithDelinquentSubscription),
                        Arrays.asList(userExternalKeyWithDelinquentSubscription) },

                { SubscriptionStatus.EXPIRED.getDisplayName(), Arrays.asList(userIdWithExpiredSubscription),
                        Arrays.asList(userExternalKeyWithExpiredSubscription) },

                { SubscriptionStatus.PENDING_MIGRATION.getDisplayName(),
                        Arrays.asList(userIdWithPendingMigrationSubscription),
                        Arrays.asList(userExternalKeyWithPendingMigrationSubscription) },

                { SubscriptionStatus.PENDING.getDisplayName(), Arrays.asList(userIdWithPendingSubscription),
                        Arrays.asList(userExternalKeyWithPendingSubscription) },

                { SubscriptionStatus.ACTIVE.getDisplayName() + "," + SubscriptionStatus.CANCELLED.getDisplayName(),
                        Arrays.asList(userIdWithActiveSubscription, userIdWithCancelledSubscription),
                        Arrays.asList(userExternalKeyWithActiveSubscription,
                            userExternalKeyWithCancelledSubscription) },

                { SubscriptionStatus.EXPIRED.getDisplayName() + "," + SubscriptionStatus.CANCELLED.getDisplayName()
                    + "," + SubscriptionStatus.PENDING.getDisplayName(),
                        Arrays.asList(userIdWithExpiredSubscription, userIdWithCancelledSubscription,
                            userIdWithPendingSubscription),
                        Arrays.asList(userExternalKeyWithExpiredSubscription, userExternalKeyWithCancelledSubscription,
                            userExternalKeyWithPendingSubscription) },

                { SubscriptionStatus.ACTIVE.getDisplayName() + "," + SubscriptionStatus.CANCELLED.getDisplayName() + ","
                    + SubscriptionStatus.PENDING.getDisplayName() + ","
                    + SubscriptionStatus.PENDING_MIGRATION.getDisplayName() + ","
                    + SubscriptionStatus.EXPIRED.getDisplayName(),
                        Arrays.asList(userIdWithActiveSubscription, userIdWithCancelledSubscription,
                            userIdWithPendingSubscription, userIdWithPendingMigrationSubscription,
                            userIdWithExpiredSubscription),
                        Arrays.asList(userExternalKeyWithActiveSubscription, userExternalKeyWithCancelledSubscription,
                            userExternalKeyWithPendingSubscription, userExternalKeyWithPendingMigrationSubscription,
                            userExternalKeyWithExpiredSubscription) } };
    }
}
