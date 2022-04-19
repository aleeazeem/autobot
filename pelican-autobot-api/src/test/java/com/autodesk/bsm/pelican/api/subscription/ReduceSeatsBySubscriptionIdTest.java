package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.ReduceSeatsBySubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
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

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class tests the reduce seats by subscription id api.
 *
 * @author Muhammad
 *
 */
public class ReduceSeatsBySubscriptionIdTest extends BaseTestData {

    private PelicanPlatform resource;
    private Object apiResponse;
    private HttpError httpError;
    private PurchaseOrderUtils purchaseOrderUtils;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static String bicSubscriptionId;
    private static final int QUANTITY = 10;
    private static final String ERRORS_DETAIL_META_SUBSCRIPTION = "Can't reduce seats for a Meta Subscription";
    private static final String ERRORS_DETAIL_INVALID_REDUCE_SEATS = "Invalid reduce seats quantity : ";
    private static final String ERRORS_DETAIL_INACTIVE_SUBSCRIPTION =
        "Can't reduce seats for a subscription " + "that is not active";
    private static final String VALIDATION_EXCEPTION_CODE = "10005";
    private ReduceSeatsBySubscriptionId reduceSeatsBySubId;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final String bicPrice = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice, QUANTITY);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        bicSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
    }

    /**
     * This method tests the seats are reduced for a subscription sucessfuly.
     */
    @Test
    public void testReduceSeatsForSubscriptionSuccessScenario() {
        final String quantityToReduce = "4";
        apiResponse = resource.reduceSeatsBySubscriptionId().reduceSeats(bicSubscriptionId, quantityToReduce);
        if (apiResponse instanceof HttpError) { // if get items api returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ReduceSeatsBySubscriptionId.class), assertionErrorList);
        } else {
            reduceSeatsBySubId = (ReduceSeatsBySubscriptionId) apiResponse;
            AssertCollector.assertThat("Errors found for reduce seats in a subscription",
                reduceSeatsBySubId.getErrors(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Subscription id is not correct in response",
                reduceSeatsBySubId.getData().getId(), equalTo(bicSubscriptionId), assertionErrorList);
            AssertCollector.assertThat("Quantity to reduce is not correct in response",
                reduceSeatsBySubId.getData().getQtyToReduce(), equalTo(quantityToReduce), assertionErrorList);
            AssertCollector.assertThat("Quantity is not correct in response",
                reduceSeatsBySubId.getData().getQuantity(), equalTo(String.valueOf(QUANTITY)), assertionErrorList);
            AssertCollector.assertThat("Renewal quantity is not correct in response",
                reduceSeatsBySubId.getData().getRenewalQuantity(),
                equalTo(String.valueOf(QUANTITY - Integer.valueOf(quantityToReduce))), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests quantity to reduce can be canceled in reduce seats by subscription id api.
     */
    @Test(dependsOnMethods = "testReduceSeatsForSubscriptionSuccessScenario")
    public void testRevertReduceSeatsForSubscriptionSuccessScenario() {
        final String quantityToReduce = "-4";
        apiResponse = resource.reduceSeatsBySubscriptionId().reduceSeats(bicSubscriptionId, quantityToReduce);
        if (apiResponse instanceof HttpError) { // if get items api returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(ReduceSeatsBySubscriptionId.class), assertionErrorList);
        } else {
            reduceSeatsBySubId = (ReduceSeatsBySubscriptionId) apiResponse;
            AssertCollector.assertThat("Errors found for reduce seats in a subscription",
                reduceSeatsBySubId.getErrors(), equalTo(null), assertionErrorList);
            AssertCollector.assertThat("Subscription id is not correct in response",
                reduceSeatsBySubId.getData().getId(), equalTo(bicSubscriptionId), assertionErrorList);
            AssertCollector.assertThat("Quantity to reduce is not correct in response",
                reduceSeatsBySubId.getData().getQtyToReduce(), equalTo("0"), assertionErrorList);
            AssertCollector.assertThat("Quantity is not correct in response",
                reduceSeatsBySubId.getData().getQuantity(), equalTo(String.valueOf(QUANTITY)), assertionErrorList);
            AssertCollector.assertThat("Renewal quantity is not correct in response",
                reduceSeatsBySubId.getData().getRenewalQuantity(), equalTo(String.valueOf(QUANTITY)),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This methods tests seats can't be reduced for meta subscription.
     */
    @Test
    public void testErrorScenarioReduceSeatsForMetaSubscription() {
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final String metaPrice =
            subscriptionPlanApiUtils
                .addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                    BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
                .getIncluded().getPrices().get(0).getId();
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(metaPrice, QUANTITY);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String metaSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        apiResponse = resource.reduceSeatsBySubscriptionId().reduceSeats(metaSubscriptionId, "3");

        reduceSeatsBySubId = (ReduceSeatsBySubscriptionId) apiResponse;
        AssertCollector.assertThat("Errors found for reduce seats in a subscription",
            reduceSeatsBySubId.getErrors().get(0).getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()),
            assertionErrorList);
        AssertCollector.assertThat("Details are not correct in response",
            reduceSeatsBySubId.getErrors().get(0).getDetail(), equalTo(ERRORS_DETAIL_META_SUBSCRIPTION),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error code in response", reduceSeatsBySubId.getErrors().get(0).getCode(),
            equalTo(VALIDATION_EXCEPTION_CODE), assertionErrorList);
        AssertCollector.assertThat("Details are not correct in response",
            reduceSeatsBySubId.getErrors().get(0).getDetail(), equalTo(ERRORS_DETAIL_META_SUBSCRIPTION),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the following error scenarios. - if reduce seats value is equal to the value of quantity. - if
     * reduce seats value 0. - if reduce seats value is greater than the value of quantity.
     *
     * @param quantityToReduce
     */
    @Test(dataProvider = "invalidValuesForQtyToReduce",
        dependsOnMethods = "testRevertReduceSeatsForSubscriptionSuccessScenario")
    public void testErrorScenarioForSameQuantityToReduceAsQuantity(final String quantityToReduce) {
        apiResponse = resource.reduceSeatsBySubscriptionId().reduceSeats(bicSubscriptionId, quantityToReduce);
        reduceSeatsBySubId = (ReduceSeatsBySubscriptionId) apiResponse;
        AssertCollector.assertThat("Invalid status in response", reduceSeatsBySubId.getErrors().get(0).getDetail(),
            equalTo(ERRORS_DETAIL_INVALID_REDUCE_SEATS + quantityToReduce), assertionErrorList);
        AssertCollector.assertThat("Invalid error code in response", reduceSeatsBySubId.getErrors().get(0).getCode(),
            equalTo(VALIDATION_EXCEPTION_CODE), assertionErrorList);
        AssertCollector.assertThat("Invalid status in response", reduceSeatsBySubId.getErrors().get(0).getStatus(),
            equalTo(HttpStatus.BAD_REQUEST.value()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the errors scenarios for inactive subscriptions.
     *
     * @param status
     */
    @Test(dataProvider = "invalidStatuses")
    public void testErrorScenarioOfReduceSeatsForInactiveSubscription(final int status) {
        final String sqlQuery =
            "select ID from subscription where status = " + status + " and quantity > 1 and APP_FAMILY_ID = "
                + getEnvironmentVariables().getAppFamilyId() + " order by id desc";
        final List<Map<String, String>> resultList = DbUtils.selectQuery(
            String.format(sqlQuery, getEnvironmentVariables().getAppFamilyId()), getEnvironmentVariables());
        final String subscriptionId = resultList.get(0).get("ID");
        final String quantityToReduce = "1";

        apiResponse = resource.reduceSeatsBySubscriptionId().reduceSeats(subscriptionId, quantityToReduce);

        reduceSeatsBySubId = (ReduceSeatsBySubscriptionId) apiResponse;
        AssertCollector.assertThat("Invalid Details in response", reduceSeatsBySubId.getErrors().get(0).getDetail(),
            equalTo(ERRORS_DETAIL_INACTIVE_SUBSCRIPTION), assertionErrorList);
        AssertCollector.assertThat("Invalid error code in response", reduceSeatsBySubId.getErrors().get(0).getCode(),
            equalTo(VALIDATION_EXCEPTION_CODE), assertionErrorList);
        AssertCollector.assertThat("Invalid status in response", reduceSeatsBySubId.getErrors().get(0).getStatus(),
            equalTo(HttpStatus.BAD_REQUEST.value()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "invalidValuesForQtyToReduce")
    private Object[][] getInvalidValuesForQtyToReduce() {
        return new Object[][] { { String.valueOf(QUANTITY) }, { "0" }, { "-1" }, { String.valueOf(QUANTITY + 1) } };
    }

    @DataProvider(name = "invalidStatuses")
    private Object[][] getInvalidStatuses() {
        return new Object[][] { { SubscriptionStatus.EXPIRED.ordinal() }, { SubscriptionStatus.CANCELLED.ordinal() },
                { SubscriptionStatus.DELINQUENT.ordinal() } };
    }
}
