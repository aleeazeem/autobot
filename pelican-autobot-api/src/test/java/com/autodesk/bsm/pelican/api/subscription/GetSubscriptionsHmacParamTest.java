
package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.HttpClient;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RestClientUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Find Subscriptions API Test: Test methods to test scenarios of all the Subscription APIs
 *
 * @author yerragv
 */
public class GetSubscriptionsHmacParamTest extends BaseTestData {

    private static String subscriptionIdForBicCreditCard1;
    private static String subscriptionIdForBicCreditCard2;
    private String url;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetSubscriptionsHmacParamTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)

    public void setUp() {

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final String bicPrice1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();

        // Add Bic subscription plan
        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicPrice2 = bicOfferings.getIncluded().getPrices().get(0).getId();

        PurchaseOrder purchaseOrderForBicCreditCard1 =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicPrice1, getBuyerUser(), 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard1.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard1.getId());
        purchaseOrderForBicCreditCard1 = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard1.getId());
        subscriptionIdForBicCreditCard1 = purchaseOrderForBicCreditCard1.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        PurchaseOrder purchaseOrderForBicCreditCard2 =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicPrice2, getBuyerUser(), 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard2.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard2.getId());
        purchaseOrderForBicCreditCard2 = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard2.getId());
        subscriptionIdForBicCreditCard2 = purchaseOrderForBicCreditCard2.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        authInfo = new AuthenticationInfo(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());

        final String urlSuffix = "/subscriptions?auth.partnerId=" + getEnvironmentVariables().getPartnerId()
            + "&auth.appFamilyId=" + getEnvironmentVariables().getAppFamilyId() + "&auth.timestamp="
            + authInfo.getTimestamp() + "&auth.signature=" + RestClientUtils.getHMACSignatureValue(authInfo)
            + "&subscription[Ids]=" + subscriptionIdForBicCreditCard1 + "," + subscriptionIdForBicCreditCard2
            + "&userId=" + getUser().getId() + "&userExternalKey=" + getUserExternalKey() + "&statuses="
            + Status.ACTIVE.toString() + "&fr.blockSize=256&fr.skipCount=true";

        url = getEnvironmentVariables().getV2ApiUrl() + urlSuffix;

    }

    @Test
    public void testFindSubscriptionsByUserExternalKeyWithHmacParams() throws IOException {

        LOGGER.info("Request Url " + url);
        final CloseableHttpResponse httpResponse = HttpClient.doGet(httpClient, url);
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        final SubscriptionsClient subcriptionClient =
            new SubscriptionsClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final Object entity = subcriptionClient.getPojo(httpResponse, PelicanConstants.CONTENT_TYPE_XML);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(Subscriptions.class), assertionErrorList);
        } else {
            final Subscriptions subscriptions = (Subscriptions) entity;

            for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
                AssertCollector.assertThat("Incorrect owner external key",
                    subscriptions.getSubscriptions().get(i).getOwnerExternalKey(), is(getUserExternalKey()),
                    assertionErrorList);
            }
            helperToValidateAssertions(subscriptions);
        }

        httpResponse.close();
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which tests the api authentication using invalid login values
     *
     * In the url of the test case, I am appending the junk value "12" to the signature to make the signature invalid.
     *
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testFindSubscriptionsByUserExternalKeyWithInvalidLoginDetails() throws IOException {

        final String urlSuffix = "/subscriptions?auth.partnerId=" + getEnvironmentVariables().getPartnerId()
            + "&auth.appFamilyId=" + getEnvironmentVariables().getAppFamilyId() + "&auth.timestamp="
            + authInfo.getTimestamp() + "&auth.signature=" + RestClientUtils.getHMACSignatureValue(authInfo)
            + "12&subscription[Ids]=" + subscriptionIdForBicCreditCard1 + "," + subscriptionIdForBicCreditCard2
            + "&userId=" + getUser().getId() + "&userExternalKey=" + getUserExternalKey() + "&statuses="
            + Status.ACTIVE.toString() + "&fr.blockSize=256&fr.skipCount=true";

        url = getEnvironmentVariables().getV2ApiUrl() + urlSuffix;

        LOGGER.info("Request Url " + url);
        final CloseableHttpResponse httpResponse = HttpClient.doGet(httpClient, url);
        AssertCollector.assertThat("Response code is not correct", httpResponse.getStatusLine().getStatusCode(),
            equalTo(HttpStatus.SC_UNAUTHORIZED), assertionErrorList);

        httpResponse.close();
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method helps to assert different values for many test methods.
     */
    private void helperToValidateAssertions(final Subscriptions subscriptions) {

        AssertCollector.assertThat("Unable to get subscriptions by user external key. Are there any subscriptions?",
            subscriptions.getSubscriptions(), is(notNullValue()), assertionErrorList);

        for (int i = 0; i < subscriptions.getSubscriptions().size(); i++) {
            AssertCollector.assertThat("Incorrect owner external key",
                subscriptions.getSubscriptions().get(i).getOwnerExternalKey(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Application Family Id ",
                subscriptions.getSubscriptions().get(i).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing count",
                subscriptions.getSubscriptions().get(i).getBillingOption().getBillingPeriod().getCount(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing type",
                subscriptions.getSubscriptions().get(i).getBillingOption().getBillingPeriod().getType(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Plan usage type",
                subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getUsageType(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Plan support level",
                subscriptions.getSubscriptions().get(i).getSubscriptionPlan().getSupportLevel(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect application family id",
                subscriptions.getSubscriptions().get(i).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        }
    }
}
