package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.UpdateSubscription;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * Rest service: Subscription end point
 *
 * @author yin
 */
public class SubscriptionClient {

    private static final String END_POINT = "subscription";
    private static final String CONTENT_TYPE = "application/xml";
    private static final String SUBSCRIPTION_SYNC_END_POINT = "/sync";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private boolean success;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionClient.class.getSimpleName());

    private enum Field {
        SUBSCRIPTION_ID("subscriptionId"),
        PAYMENT_PROFILE_ID("paymentProfileId"),
        CANCELLATION_POLICY("cancellationPolicy"),
        INCLUDE("include");

        private String fieldName;

        Field(final String name) {
            this.fieldName = name;
        }

        String getName() {
            return fieldName;
        }
    }

    public SubscriptionClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add subscription
     *
     * @return Subscription
     */
    public Subscription add(final String userExtKey, final String subsOfferExtKey, final Currency currency) {

        final Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("userExternalKey", userExtKey);
        bodyParams.put("subscriptionOfferExternalKey", subsOfferExtKey);
        bodyParams.put("currencyName", currency.toString());

        LOGGER.info("Add subscription for " + userExtKey + " with offer external key #" + subsOfferExtKey);

        LOGGER.info("Post body before encoding: " + bodyParams);
        final String body = RestClientUtils.urlEncode(bodyParams);

        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE, PelicanConstants.CONTENT_TYPE_XML);
        return parseResponse(response);
    }

    /**
     * Update subscription by id
     *
     * @return true if success or false if failed
     */
    public boolean updateSubscription(final String subscriptionId, final String newPaymentProfileId) {
        success = false;
        LOGGER.info("Updating subscription #" + subscriptionId + " with payment profile #" + newPaymentProfileId);
        final String url = getUrl() + "/" + subscriptionId;
        final Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(Field.SUBSCRIPTION_ID.getName(), subscriptionId);
        paramsMap.put(Field.PAYMENT_PROFILE_ID.getName(), newPaymentProfileId);

        LOGGER.info("Put body before encoding: " + paramsMap);
        final String body = RestClientUtils.urlEncode(paramsMap);

        final CloseableHttpResponse response =
            client.doPut(url, PelicanConstants.CONTENT_TYPE_URL_ENCODED, null, body, authInfo);
        if (response.getStatusLine().getStatusCode() == 200) {
            success = true;
        }
        return success;
    }

    /**
     * Get subscription by id
     *
     * @return Subscription or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getById(final String subscriptionId) {
        return getById(subscriptionId, null);
    }

    /**
     * This method does get call to get a subscription
     *
     * @return Subscription or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getById(final String subscriptionId, final String include) {
        final Map<String, String> params = new HashMap<>();
        params.put(Field.INCLUDE.getName(), include);
        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + subscriptionId, params, authInfo,
            CONTENT_TYPE, PelicanConstants.CONTENT_TYPE_XML);

        return getPojo(response);
    }

    /**
     * Cancel subscription by id and cancellation policy
     *
     * @return true if success or false if failed
     */
    public boolean cancelSubscription(final String subscriptionId, final CancellationPolicy policy) {

        LOGGER.info("Cancelling subscription #" + subscriptionId + " with " + policy.toString());
        final String url = getUrl() + "/" + subscriptionId + "?" + Field.SUBSCRIPTION_ID.getName() + "="
            + subscriptionId + "&" + Field.CANCELLATION_POLICY.getName() + "=" + policy.toString();
        final CloseableHttpResponse response = client.doDelete(url, authInfo);
        return (response.getStatusLine().getStatusCode() == 200);
    }

    /**
     * Restart a cancelled subscription.
     *
     * @return PelicanPojo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T restartCancelledSubscription(final String subscriptionId) {
        T pojo = null;
        success = false;
        final String body = "subscriptionId=" + subscriptionId;
        LOGGER.info("Restarting a cancelled subscription #" + subscriptionId);
        final String url = getUrl() + "/" + subscriptionId + "/restart";
        final CloseableHttpResponse response =
            client.doPut(url, PelicanConstants.CONTENT_TYPE_URL_ENCODED, null, body, authInfo);
        if (!(response.getStatusLine().getStatusCode() == 200)) {
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        }
        return pojo;

    }

    /**
     * This is a method which will call sync subscription api in the subscription service
     *
     * @param environmentVariables - environment variables
     * @param requestBody - Request body to update
     */
    public void syncSubscription(final EnvironmentVariables environmentVariables, final String requestBody) {
        final CloseableHttpResponse httpResponse =
            client.doPut(environmentVariables.getSubscriptionServiceUrl() + "v2" + SUBSCRIPTION_SYNC_END_POINT,
                MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE, requestBody, authInfo);
    }

    /**
     * This method updates required fields of subscription by calling Subscription Service end point.
     *
     * @param subscriptionId
     * @param data
     */
    public void updatePatchSubscription(final String subscriptionId, final UpdateSubscription.Data data) {

        final UpdateSubscription updateSubscription = new UpdateSubscription();
        final UpdateSubscription.Meta meta = new UpdateSubscription.Meta();
        meta.setContext("PO_UPDATE");
        updateSubscription.setMeta(meta);
        updateSubscription.setData(data);

        final Gson gson = new Gson();
        final String body = gson.toJson(updateSubscription);

        client.doPatch(environmentVariables.getSubscriptionServiceUrl() + "v3/" + subscriptionId,
            MediaType.APPLICATION_JSON_VALUE, PelicanConstants.CONTENT_TYPE, body, authInfo);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T getPojo(final CloseableHttpResponse response) {
        T pojo;
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private Subscription parseResponse(final CloseableHttpResponse response) {

        Subscription subscription = null;
        try {
            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(Subscription.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            subscription = (Subscription) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return subscription;
    }
}
