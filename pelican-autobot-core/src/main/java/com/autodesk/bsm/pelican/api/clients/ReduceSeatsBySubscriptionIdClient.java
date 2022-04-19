package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.ReduceSeatsBySubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.json.ReduceSeatsBySubscriptionId.Data;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
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
 * This class is used to reduce seats for a subscription and also to parse the response returned
 *
 * @author Muhammad
 */
public class ReduceSeatsBySubscriptionIdClient {

    private static final String END_POINT = "subscription";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLinesClient.class.getSimpleName());

    /**
     * Constructor for reduce seats
     *
     * @param environmentVariables
     * @param appFamily TODO
     */
    public ReduceSeatsBySubscriptionIdClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Method to reduce seats for a subscription.
     *
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T reduceSeats(final String subscriptionId, final String qtyToReduce) {
        T pojo;
        final String body = "subscriptionId=" + subscriptionId + "&qtyToReduce=" + qtyToReduce;

        LOGGER.info("Request body: " + body);
        final CloseableHttpResponse response = client.doPut(getUrl(subscriptionId),
            PelicanConstants.CONTENT_TYPE_URL_ENCODED, PelicanConstants.CONTENT_TYPE, body, authInfo);
        final int status = response.getStatusLine().getStatusCode();
        pojo = parseResponse(response);
        return pojo;
    }

    /**
     * Parsing a response after the put call for reduce seats by subscription id api.
     *
     * @return seats reduced or HttpError
     */
    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response) {
        T pojo = null;
        final int status = response.getStatusLine().getStatusCode();
        final ReduceSeatsBySubscriptionId getReduceSeats = new ReduceSeatsBySubscriptionId();

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
                getReduceSeats.setErrors(Arrays.asList(errors));
            } else {
                if (!(jsonObject.get(PelicanConstants.DATA) instanceof JsonNull)) {
                    final Data reduceSeats = new Data();
                    final JsonObject reduceSeatsObject = jsonObject.getAsJsonObject(PelicanConstants.DATA);
                    reduceSeats.setType(reduceSeatsObject.getAsJsonPrimitive(PelicanConstants.TYPE).getAsString());
                    reduceSeats.setId(reduceSeatsObject.getAsJsonPrimitive(PelicanConstants.ID).getAsString());
                    reduceSeats
                        .setQuantity(reduceSeatsObject.getAsJsonPrimitive(PelicanConstants.QUANTITY).getAsString());
                    reduceSeats.setQtyToReduce(
                        reduceSeatsObject.getAsJsonPrimitive(PelicanConstants.QTY_TO_REDUCE).getAsString());
                    reduceSeats.setRenewalQuantity(
                        reduceSeatsObject.getAsJsonPrimitive(PelicanConstants.RENEWAL_QTY).getAsString());
                    getReduceSeats.setData(reduceSeats);
                }
            }
            pojo = (T) getReduceSeats;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return pojo;
    }

    private String getUrl(final String subscriptionId) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + subscriptionId + "/" + "reduceSeats";
    }
}
