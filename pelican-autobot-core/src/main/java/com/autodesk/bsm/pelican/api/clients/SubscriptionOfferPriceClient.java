package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPriceData;
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
 * This class is used to add a price to the subscription offer and also to parse the response returned
 */
public class SubscriptionOfferPriceClient {

    private static final String END_POINT_OFFERINGS = "offerings";
    private static final String END_POINT_OFFERS = "offers";
    private static final String END_POINT_PRICES = "prices";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOfferPriceClient.class.getSimpleName());

    public SubscriptionOfferPriceClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add a subscription offer price
     *
     * @return Pelican Pojo
     */
    public <T extends PelicanPojo> T addSubscriptionOfferPrice(final SubscriptionOfferPrice subscriptionOfferPrice,
        final String offeringId, final String offerId) {

        T pojo;
        LOGGER.info("Add Subscription Offer Price");
        final String body = toJSON(subscriptionOfferPrice);
        final CloseableHttpResponse response = client.doPost(getUrlWithOfferingIdAndOfferId(offeringId, offerId), body,
            authInfo, CONTENT_TYPE, CONTENT_TYPE);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to create a subscription offer price. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            LOGGER.info("Parsing the correct response");
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    private String getUrlWithOfferingIdAndOfferId(final String offeringId, final String offerId) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT_OFFERINGS + "/" + offeringId + "/"
            + END_POINT_OFFERS + "/" + offerId + "/" + END_POINT_PRICES;
    }

    private String toJSON(final SubscriptionOfferPrice subscriptionOfferPrice) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(subscriptionOfferPrice);
    }

    private SubscriptionOfferPrice parseResponse(final CloseableHttpResponse response) {

        final SubscriptionOfferPrice subscriptionOfferPrice = new SubscriptionOfferPrice();
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
                subscriptionOfferPrice.setErrors(errors[0]);
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final SubscriptionOfferPrice subscriptionOfferPriceObj =
                                gson.fromJson(jsonObj, SubscriptionOfferPrice.class);
                        }
                    } else {
                        final SubscriptionOfferPriceData data = gson
                            .fromJson(jsonObject.getAsJsonObject("data").toString(), SubscriptionOfferPriceData.class);
                        subscriptionOfferPrice.setData(data);
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
        return subscriptionOfferPrice;
    }
}
