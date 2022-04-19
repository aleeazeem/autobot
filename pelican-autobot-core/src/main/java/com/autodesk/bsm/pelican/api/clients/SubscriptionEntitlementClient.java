package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementData;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementRequest;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementResponse;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Endpoint for Add Entitlement. This class provides the method which adds entitlement to a subscription plan and
 * returns the parsed response.
 *
 * @author jains
 */
public class SubscriptionEntitlementClient {

    private static final String END_POINT_ENTITLEMENTS = "entitlements";
    private static final String END_POINT_OFFERINGS = "offerings";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final String ACCEPT = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionEntitlementClient.class.getSimpleName());

    public SubscriptionEntitlementClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * This method does post call to add entitlement
     */
    public <T extends PelicanPojo> T addEntitlement(final JSubscriptionEntitlementRequest subscriptionEntitlement,
        final String offeringId) {
        final Gson gson = new GsonBuilder().create();
        final CloseableHttpResponse response =
            client.doPost(getUrl(offeringId), gson.toJson(subscriptionEntitlement), authInfo, CONTENT_TYPE, ACCEPT);
        return getPojo(gson, response);
    }

    private String getUrl(final String offeringId) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT_OFFERINGS + "/" + offeringId + "/"
            + END_POINT_ENTITLEMENTS;
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T getPojo(final Gson gson, final CloseableHttpResponse response) {
        final JSubscriptionEntitlementResponse subscriptionEntitlementResponse = new JSubscriptionEntitlementResponse();
        JSubscriptionEntitlementData subscriptionEntitlementData;
        final int status = response.getStatusLine().getStatusCode();
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            if (status == HttpStatus.SC_CREATED) {
                // Parse data
                if (!jsonObject.get("data").isJsonNull()) {
                    final JsonObject dataObj = jsonObject.getAsJsonObject("data");
                    subscriptionEntitlementData = gson.fromJson(dataObj.toString(), JSubscriptionEntitlementData.class);
                    subscriptionEntitlementResponse.setData(subscriptionEntitlementData);
                }
            } else {
                // Parse errors
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                subscriptionEntitlementResponse.setErrors(Arrays.asList(errors));
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return (T) subscriptionEntitlementResponse;
    }
}
