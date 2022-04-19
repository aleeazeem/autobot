package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwners.Links;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Endpoint for Subscription JSON API. This class provides the methods which invokes the get subscription events api and
 * returns the parsed response.
 *
 * @author Muhammad
 *
 */
public class SubscriptionEventsClient {
    private final AuthenticationInfo authInfo;
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private static final String END_POINT = "subscription";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionEventsClient.class.getSimpleName());

    public SubscriptionEventsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    public <T extends PelicanPojo> T getSubscriptionEvents(final String subscriptionId,
        final Map<String, String> params) throws ParseException, IOException {
        final CloseableHttpResponse response =
            client.doGet(getUrl(subscriptionId), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        return paraseResponse(response);
    }

    private String getUrl(final String id) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + id + "/events";
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T paraseResponse(final CloseableHttpResponse response)
        throws ParseException, IOException {
        final JSubscriptionEvents subscriptionEvents = new JSubscriptionEvents();
        final String httpResponse = EntityUtils.toString(response.getEntity());
        LOGGER.info("Response : " + httpResponse);
        final JsonObject jsonObject = new JsonParser().parse(httpResponse).getAsJsonObject();
        final Gson gson = new GsonBuilder().create();
        T pojo;
        if (!(jsonObject.get("data") instanceof JsonNull)) {
            final JsonArray dataArray = jsonObject.getAsJsonArray("data");
            final SubscriptionEventsData[] subscriptionEventsDataArray =
                gson.fromJson(dataArray.toString(), SubscriptionEventsData[].class);
            subscriptionEvents.setEventsData(Arrays.asList(subscriptionEventsDataArray));
            // data is null then there is no links in response.
            if (!(jsonObject.get("links") instanceof JsonNull)) {
                final JsonObject linkObject = jsonObject.getAsJsonObject("links");
                final Links links = gson.fromJson(linkObject.toString(), Links.class);
                subscriptionEvents.setLinks(links);
            }
        }
        // Parse errors if present.
        if (!(jsonObject.get("errors") instanceof JsonNull)) {
            final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
            final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
            subscriptionEvents.setErrors(Arrays.asList(errors));
        }
        pojo = (T) subscriptionEvents;
        return pojo;
    }
}
