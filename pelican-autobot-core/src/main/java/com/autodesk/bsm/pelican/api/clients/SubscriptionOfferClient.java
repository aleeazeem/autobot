package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferData;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/*
 * This class is used to add a subscription offer to a subscription plan and also to parse the response returned
 */
public class SubscriptionOfferClient {
    private static final String END_POINT_OFFERINGS = "offerings";
    private static final String END_POINT_OFFERS = "offers";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOfferClient.class.getSimpleName());

    public SubscriptionOfferClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add a Subscription Offer
     *
     * @return Pelican Pojo
     */
    public <T extends PelicanPojo> T addSubscriptionOffer(final SubscriptionOffer subscriptionOffer,
        final String offeringId) {

        T pojo;
        LOGGER.info("Add Subscription Offer");
        final String body = toJSON(subscriptionOffer);
        final CloseableHttpResponse response =
            client.doPost(getUrlWithOfferingId(offeringId), body, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to create a subscription offer. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            LOGGER.info("Parsing the correct response");
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    private String getUrlWithOfferingId(final String offeringId) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT_OFFERINGS + "/" + offeringId + "/"
            + END_POINT_OFFERS;
    }

    private String toJSON(final SubscriptionOffer subscriptionOffer) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(subscriptionOffer);
    }

    private SubscriptionOffer parseResponse(final CloseableHttpResponse response) {

        final SubscriptionOffer subscriptionOffer = new SubscriptionOffer();
        final int status = response.getStatusLine().getStatusCode();

        try {
            final Gson gson = new GsonBuilder().create();

            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            if (status != HttpStatus.SC_CREATED) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                subscriptionOffer.setErrors(errors[0]);
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final SubscriptionOffer storeObj = gson.fromJson(jsonObj, SubscriptionOffer.class);
                        }
                    } else {
                        final SubscriptionOfferData subscriptionOfferData =
                            gson.fromJson(jsonObject.getAsJsonObject("data").toString(), SubscriptionOfferData.class);
                        subscriptionOffer.setData(subscriptionOfferData);
                    }
                }
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
        return subscriptionOffer;
    }
}
