package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Rest service: Subscription Plan endpoint
 *
 * @author yin
 */
public class SubscriptionPlanClient {

    private static final String END_POINT = "subscriptionPlan";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanClient.class.getSimpleName());

    public SubscriptionPlanClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    public SubscriptionPlan getById(final String subscriptionPlanId, final String offerId) {
        final Map<String, String> params = new ConcurrentHashMap<>();
        if (offerId != null) {
            params.put("offerId", offerId);
        }
        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + subscriptionPlanId, params, authInfo,
            PelicanConstants.CONTENT_TYPE_XML, null);
        return parseResponse(response);
    }

    /**
     * update Subscription Plan
     *
     * @param HashMap RequestBody - RequestBody -contains Subscription Plan Id, status and Properties
     * @return HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T update(final HashMap<String, String> requestBody) {

        T pojo = null;

        LOGGER.info("Update Subscription Plan");

        LOGGER.info("Put body before encoding: " + requestBody);
        final String body = RestClientUtils.urlEncode(requestBody);

        final CloseableHttpResponse response = client.doPut(getUpdateUrl(requestBody.values().iterator().next()),
            PelicanConstants.CONTENT_TYPE_URL_ENCODED, null, body, authInfo);
        final int status = response.getStatusLine().getStatusCode();
        /*
         * @ Return HttpErrorResponse if status is not ok
         *
         * @ Return 200 ok if it the update is success Request Body is null if the update is success
         */
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Unable to update a subscription plan. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        }

        return pojo;

    }

    /*
     * @ Param - subscriptionPlanId
     *
     * @ Return- Subscription Plan Update api url
     */
    private String getUpdateUrl(final String subscriptionPlanId) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + subscriptionPlanId;
    }

    /*
     * @ Return - subscription plan api url
     */
    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private SubscriptionPlan parseResponse(final CloseableHttpResponse response) {

        SubscriptionPlan subscriptionPlan = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(SubscriptionPlan.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            subscriptionPlan = (SubscriptionPlan) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | JAXBException | IOException e) {
            e.printStackTrace();
        }

        return subscriptionPlan;
    }
}
