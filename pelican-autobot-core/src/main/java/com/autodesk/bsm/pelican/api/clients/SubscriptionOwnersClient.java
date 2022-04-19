package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwnerData;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwners;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwners.Links;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
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
import java.util.Map;

/**
 * End point for Subscription Owners JSON API. This class provides the methods which invokes the get subscription owners
 * api and returns the parsed response.
 *
 * @author jains
 */
public class SubscriptionOwnersClient {
    private static final String END_POINT = "subscriptions/owners";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOwnersClient.class.getSimpleName());

    public SubscriptionOwnersClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Method to invoke SubscriptionOwners api get call.
     *
     * @param requestParameters
     * @return SubscriptionOwners
     */
    public SubscriptionOwners getSubscriptionOwners(final Map<String, String> requestParameters) {
        final CloseableHttpResponse response = client.doGet(getUrl(), requestParameters, authInfo,
            PelicanConstants.CONTENT_TYPE, PelicanConstants.CONTENT_TYPE);
        return parseResponse(response);
    }

    /**
     * Get SubscriptionOwners url.
     *
     * @return String
     */
    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * Parse SubscriptionOwners response.
     *
     * @param response
     * @return SubscriptionOwners
     */
    private SubscriptionOwners parseResponse(final CloseableHttpResponse response) {
        final SubscriptionOwners subscriptionOwners = new SubscriptionOwners();
        final int status = response.getStatusLine().getStatusCode();
        LOGGER.info("Response code " + status);

        try {
            final Gson gson = new GsonBuilder().create();
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                subscriptionOwners.setErrors(Arrays.asList(errors));
            } else {
                if (!jsonObject.get("data").isJsonNull()) {
                    final JsonArray dataArray = jsonObject.getAsJsonArray("data");
                    final SubscriptionOwnerData[] subscriptionOwnerDataList =
                        gson.fromJson(dataArray.toString(), SubscriptionOwnerData[].class);
                    subscriptionOwners.setData(Arrays.asList(subscriptionOwnerDataList));
                }
                final JsonObject linkObject = jsonObject.getAsJsonObject("links");
                final Links links = gson.fromJson(linkObject.toString(), Links.class);
                subscriptionOwners.setLinks(links);
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
        return subscriptionOwners;
    }
}
