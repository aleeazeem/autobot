package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionSearchResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;

/**
 * This test class verifies find subscription or subscriptions
 *
 * @author Muhammad Azeem
 */
public class FindSubscriptionTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static FindSubscriptionsPage findSubscriptionPage;
    private String purchaseOrderIdBic;
    private String purchaseOrderIdMeta;
    private String bicSubscriptionId;
    private String metaSubscriptionId;
    private SubscriptionDetailPage subscriptionDetailPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private SubscriptionSearchResultPage subscriptionSearchResultPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(FindSubscriptionTest.class.getSimpleName());
    private BuyerUser buyerUser;

    /**
     * Data setup - open a admin tool page and login into it
     *
     */
    @BeforeClass(alwaysRun = true)
    public void findSubscriptionTestSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();

        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        subscriptionSearchResultPage = adminToolPage.getPage(SubscriptionSearchResultPage.class);
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        // Create Po for bic subscription
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrderBic =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrderIdBic = purchaseOrderBic.getId();
        bicSubscriptionId = purchaseOrderBic.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();

        // Create Po for meta subscription
        priceQuantityMap.clear();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrderMeta =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        purchaseOrderIdMeta = purchaseOrderMeta.getId();
        // fulfill meta PO
        purchaseOrderUtils.fulfillRequest(purchaseOrderMeta, FulfillmentCallbackStatus.Created);
        purchaseOrderMeta = resource.purchaseOrder().getById(purchaseOrderIdMeta);
        metaSubscriptionId = purchaseOrderMeta.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
    }

    /**
     * Verify that no result will be returned if non existing subscription external key is entered.
     *
     * @result grid without any result
     */
    @Test
    public void findSubsWithNonExistingExternalKey() {
        final String subExternalKey = RandomStringUtils.randomAlphanumeric(10);
        findSubscriptionPage.getSubscriptionByExternalKey(subExternalKey);
        subscriptionSearchResultPage = findSubscriptionPage.clickOnFindSubscriptionsButtonToGrid();
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionSearchResultPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE), assertionErrorList);
        AssertCollector.assertThat("Subscription is found", subscriptionSearchResultPage.getTotalItems(), equalTo(0),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find bic subscription by id
     *
     * @result bic subscription detail page
     */
    @Test
    public void findBicSubscriptionById() {
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(bicSubscriptionId);
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionDetailPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("Subscription Id is not correct for bic", subscriptionDetailPage.getId(),
            equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find meta subscription by id
     *
     * @result meta subscription detail page
     */
    @Test
    public void findMetaSubscriptionById() {
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(metaSubscriptionId);
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionDetailPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("Subscription Id is not correct for meta", subscriptionDetailPage.getId(),
            equalTo(metaSubscriptionId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find bic subscription by Po id
     *
     * @result subscription detail page
     */
    @Test
    public void findBicSubscriptionByPurchaseOrder() {
        findSubscriptionPage.findSubscriptionByPoId(purchaseOrderIdBic);
        subscriptionDetailPage = findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionDetailPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("User of Subscription is not correct", subscriptionDetailPage.getId(),
            equalTo(bicSubscriptionId), assertionErrorList);
        AssertCollector.assertTrue(
            "Purchase order is not correct on subscription detail page under subscription activity",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder()
                .equals(purchaseOrderIdBic)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find meta subscription by Po id
     *
     * @result subscription detail page
     */
    @Test
    public void findMetaSubscriptionByPurchaseOrder() {
        findSubscriptionPage.findSubscriptionByPoId(purchaseOrderIdMeta);
        subscriptionDetailPage = findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionDetailPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("Subscription for " + purchaseOrderIdMeta + " is not correct",
            subscriptionDetailPage.getId(), equalTo(metaSubscriptionId), assertionErrorList);
        AssertCollector.assertTrue(
            "Purchase order is not correct on subscription detail page under subscription activity",
            (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getPurchaseOrder()
                .equals(purchaseOrderIdMeta)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find subscription by user name
     *
     * @result subscription detail page
     */
    @Test
    public void findSubscriptionsByUserName() {
        findSubscriptionPage.findByUserId(getUser().getName());
        findSubscriptionPage.clickOnFindSubscriptionsButtonToGrid();
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionSearchResultPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE), assertionErrorList);
        AssertCollector.assertThat("User of Subscription is not correct",
            subscriptionSearchResultPage.getColumnValuesOfUser(), everyItem(equalTo(getUser().getName())),
            assertionErrorList);
        subscriptionSearchResultPage.selectResultRow(0);
        AssertCollector.assertThat("User of Subscription is not correct on subscription detail page",
            subscriptionDetailPage.getUser(), equalTo(getUser().getName() + " (" + getUser().getId() + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify find subscription by user external key
     *
     * @result subscription detail page
     */
    @Test
    public void findSubscriptionByUserExternalKey() {
        findSubscriptionPage.findSubscriptionByUserExternalKey(getUser().getExternalKey());
        subscriptionSearchResultPage = findSubscriptionPage.clickOnFindSubscriptionsButtonToGrid();
        AssertCollector.assertThat("Title of the Page is not correct", subscriptionSearchResultPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE), assertionErrorList);
        AssertCollector.assertThat("User of Subscription is not correct",
            subscriptionSearchResultPage.getColumnValuesOfUser(), everyItem(equalTo(getUser().getName())),
            assertionErrorList);
        subscriptionSearchResultPage.selectResultRow(0);
        AssertCollector.assertThat("User of Subscription is not correct on subscription detail page",
            subscriptionDetailPage.getUser(), equalTo(getUser().getName() + " (" + getUser().getId() + ")"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify find subscription by advanced find In this advanced find test a subscription or subscriptions can be find
     * with combination of the following . subscription plan . user name or id . user external key . status .
     * subscriptionECStatus . creation date and next billing date
     *
     * @result
     */
    @Test(dataProvider = "advancedFindForSubscriptions")
    public void testFindSubscriptionByAdvancedFind(final Offerings subscriptionPlan, final String userNameOrID,
        final String userExternalKey, final Status status, final ECStatus ecStatus, final String creationDateFrom,
        final String creationDateTo, final String nextBillingDateFrom, final String nextBillingDateTo) {
        if (ecStatus != null) {
            final PurchaseOrder purchaseOrder =
                (PurchaseOrder) purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
                    getBicMonthlyUsPriceId(), buyerUser, 1, OrderCommand.AUTHORIZE);
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrderId, ECStatus.REVIEW);
            purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrderId, ECStatus.REVIEW);
        }

        findSubscriptionPage.getSubscriptionByAdvancedFind(subscriptionPlan, userNameOrID, userExternalKey, status,
            ecStatus, creationDateFrom, creationDateTo, nextBillingDateFrom, nextBillingDateTo);
        findSubscriptionPage.clickOnAdvFindSubscriptionsButton();

        // Assertions required on search result page
        if (subscriptionSearchResultPage.getTitle().equals(PelicanConstants.SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE)) {
            if (subscriptionSearchResultPage.getTotalItems() > 0) {
                validationsOnSubscriptionsSearchResultPage(subscriptionPlan, userNameOrID, userExternalKey, status,
                    ecStatus);
            }
        } else {
            LOGGER.info("One record is found with search criteria that's why page directly navigated to "
                + "subscription details page");
        }
        validationsOnSubscriptionDetailPage(subscriptionPlan, userNameOrID, userExternalKey, status, ecStatus);
        validationOnDatesFromSubscriptionDetailsPage(creationDateFrom, creationDateTo, nextBillingDateFrom,
            nextBillingDateTo);
        AssertCollector.assertAll(assertionErrorList);

    }

    private void validationsOnSubscriptionsSearchResultPage(final Offerings subscriptionPlan, final String userNameOrID,
        final String userExternalKey, final Status status, final ECStatus ecStatus) {
        AssertCollector.assertThat("Page title is not correct", subscriptionSearchResultPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_SEARCH_RESULT_PAGE_TITLE), assertionErrorList);
        if (userNameOrID != null || userExternalKey != null) {
            AssertCollector.assertThat("Column values of User are not correct on subscription search result page.",
                subscriptionSearchResultPage.getColumnValuesOfUser(), everyItem(equalTo(buyerUser.getName())),
                assertionErrorList);
        }
        if (status != null) {
            AssertCollector.assertThat("Column values of Status are not correct on subscription search result page.",
                subscriptionSearchResultPage.getColumnValuesOfStatus(), everyItem(equalTo(status.toString())),
                assertionErrorList);
        }
        if (subscriptionPlan != null) {
            AssertCollector.assertThat(
                "Column values of Subscription Plan are not correct on subscription search result page.",
                subscriptionSearchResultPage.getColumnValuesOfSubscriptionPlan(),
                everyItem(equalTo(subscriptionPlan.getOfferings().get(0).getExternalKey())), assertionErrorList);
        }
        if (ecStatus != null) {
            AssertCollector.assertThat("Column values of EC Status are not correct on subscription search result page.",
                subscriptionSearchResultPage.getColumnValuesOfECStatus(), everyItem(equalTo(ecStatus.toString())),
                assertionErrorList);
        }
        final boolean isSubscriptionFound =
            findSubscriptionPage.selectSubscriptionFromGridIfExists(findSubscriptionPage, adminToolPage);
        AssertCollector.assertTrue("No subscription found with the selected criteria", isSubscriptionFound,
            assertionErrorList);
    }

    private void validationsOnSubscriptionDetailPage(final Offerings subscriptionPlan, final String userNameOrID,
        final String userExternalKey, final Status status, final ECStatus ecStatus) {
        // Assertions required on subscription details page.
        AssertCollector.assertThat("Page title is not correct", subscriptionDetailPage.getTitle(),
            equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE), assertionErrorList);
        if (subscriptionPlan != null) {
            final String subscriptionPlanOnDetailsPage = subscriptionDetailPage.getSubscriptionPlan();
            final String subscriptionPlanIdOnDetailsPage = subscriptionPlanOnDetailsPage
                .substring(subscriptionPlanOnDetailsPage.indexOf("(") + 1, subscriptionPlanOnDetailsPage.indexOf(")"));
            AssertCollector.assertThat("User of Subscription is not correct", subscriptionPlanIdOnDetailsPage,
                equalTo(subscriptionPlan.getOfferings().get(0).getId()), assertionErrorList);
        }
        if (userNameOrID != null) {
            AssertCollector.assertThat("User name in Subscription is not correct", subscriptionDetailPage.getUser(),
                equalTo(userNameOrID + " (" + buyerUser.getId() + ")"), assertionErrorList);
        }
        if (userExternalKey != null) {
            AssertCollector.assertThat("User externalKey in Subscription is not correct", buyerUser.getExternalKey(),
                equalTo(userExternalKey), assertionErrorList);
        }
        if (status != null) {
            AssertCollector.assertThat("Status of subscription is not correct", subscriptionDetailPage.getStatus(),
                equalTo((status).toString()), assertionErrorList);
        }
        if (ecStatus != null) {
            AssertCollector.assertThat("Export control status of subscription is not correct",
                subscriptionDetailPage.getExportControlStatus(), equalTo((ecStatus.getDisplayName())),
                assertionErrorList);
        }
    }

    private void validationOnDatesFromSubscriptionDetailsPage(final String creationDateFrom,
        final String creationDateTo, final String nextBillingDateFrom, final String nextBillingDateTo) {
        if (creationDateFrom != null && creationDateTo != null) {
            final Date createdDateFrom =
                DateTimeUtils.convertStringToDate(creationDateFrom, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final Date createdDateTo =
                DateTimeUtils.convertStringToDate(creationDateTo, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final String createdDateFromActivity =
                subscriptionDetailPage.getSubscriptionActivity().get(0).getDate().split(" ")[0];
            final Date createdDateOnDetailsPage =
                DateTimeUtils.convertStringToDate(createdDateFromActivity, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            AssertCollector.assertThat("Creation date for subscription is not correct", createdDateFrom,
                lessThanOrEqualTo((createdDateOnDetailsPage)), assertionErrorList);
            AssertCollector.assertThat("Creation date for subscription is not correct", createdDateTo,
                greaterThanOrEqualTo((createdDateOnDetailsPage)), assertionErrorList);
        }

        if (nextBillingDateFrom != null && nextBillingDateTo != null) {
            final Date nextBillingDateFromFormatted =
                DateTimeUtils.convertStringToDate(nextBillingDateFrom, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final Date nextBillingDateToFormatted =
                DateTimeUtils.convertStringToDate(nextBillingDateTo, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final Date nextBillingDateOnDetailPage = DateTimeUtils.convertStringToDate(
                subscriptionDetailPage.getNextBillingDate().split(" ")[0], PelicanConstants.DATE_FORMAT_WITH_SLASH);
            AssertCollector.assertThat("Creation date for subscription is not correct", nextBillingDateFromFormatted,
                lessThanOrEqualTo((nextBillingDateOnDetailPage)), assertionErrorList);
            AssertCollector.assertThat("Creation date for subscription is not correct", nextBillingDateToFormatted,
                greaterThanOrEqualTo((nextBillingDateOnDetailPage)), assertionErrorList);
        }
    }

    /**
     * combination of filters use to find subscriptions by advanced find
     */
    @DataProvider(name = "advancedFindForSubscriptions")
    public Object[][] getTestDataForAdvancedFind() {
        final String fromDate = DateTimeUtils.getNowMinusDays(2);
        final String toDate = DateTimeUtils.getNowPlusDays(1);
        return new Object[][] {

                { getBicSubscriptionPlan(), buyerUser.getName(), buyerUser.getExternalKey(), Status.ACTIVE,
                        ECStatus.ACCEPT, null, null, null, null },
                { null, null, buyerUser.getExternalKey(), Status.ACTIVE, null, null, null, null, null },
                { null, buyerUser.getName(), null, Status.ACTIVE, null, null, null, null, null },
                { null, null, null, null, ECStatus.REVIEW, null, null, null, null },
                { null, null, null, null, null, fromDate, toDate, null, null },
                { null, null, null, Status.ACTIVE, null, null, null, fromDate, toDate } };

    }
}
