package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions.Included;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionsData;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Rest api for Subscriptions
 *
 * @author yin
 */
public class SubscriptionsClient {

    private static final String END_POINT = "subscriptions";
    private final EnvironmentVariables environmentVariables;
    private final AuthenticationInfo authInfo;
    private final HttpRestClient client = new HttpRestClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionsClient.class.getSimpleName());

    public SubscriptionsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    public enum FieldName {
        SUBSCRIPTION_IDS("subscription[Ids]"),
        USER_ID("userId"),
        USER_EXTERNAL_KEY("userExternalKey"),
        PLAN_ID("planId"),
        PLAN_EXTERNAL_KEY("planExternalKey"),
        APP_ID("appId"),
        STATUSES("statuses"),
        DAYS_PAST_EXPIRED("daysPastExpired"),
        LAST_MODIFIED_AFTER("lastModifiedAfter"),
        INCLUDE("include"),
        START_INDEX("fr.startIndex"),
        BLOCK_SIZE("fr.blockSize"),
        SKIP_COUNT("fr.skipCount");

        private String name;

        FieldName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Find Subscriptions with single/multiple/zero filters
     *
     * @return Subscriptions or HttpError
     */
    public <T extends PelicanPojo> T getSubscriptions(final Map<String, String> requestParameters,
        final String contentType) {

        final CloseableHttpResponse response =
            client.doGet(getUrl(), requestParameters, authInfo, contentType, contentType);
        return getPojo(response, contentType);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    public <T extends PelicanPojo> T getPojo(final CloseableHttpResponse response, final String contentType) {
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            if (contentType.equalsIgnoreCase("application/xml")) {
                return (T) RestClientUtils.parseErrorResponse(response);
            }
        }
        return (T) parseResponse(response, contentType);
    }

    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response, final String contentType) {
        Subscriptions subscriptions = null;

        if (contentType.equalsIgnoreCase("application/xml")) {
            try {
                final HttpEntity entity = response.getEntity();
                final InputStream inputStream = entity.getContent();
                final JAXBContext jaxbContext = JAXBContext.newInstance(Subscriptions.class);
                final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                subscriptions = (Subscriptions) jaxbUnmarshaller.unmarshal(inputStream);
            } catch (IllegalStateException | IOException | JAXBException e) {
                e.printStackTrace();
            }
            return (T) subscriptions;
        }

        return (T) parseJsonResponse(response);
    }

    /**
     * This method parses the JSON response of get subscriptions api
     */
    /**
     * This method parses the JSON response of get subscriptions api
     */
    private JSubscriptions parseJsonResponse(final CloseableHttpResponse response) {
        final JSubscriptions subscriptions = new JSubscriptions();
        JSubscriptionsData subscriptionsData = new JSubscriptionsData();

        final Included included = new Included();
        Price price;
        SubscriptionOffering offering;
        BillingPlan billingPlan;

        final int status = response.getStatusLine().getStatusCode();
        LOGGER.info("Response code " + status);
        try {
            final Gson gson = new GsonBuilder().create();
            final String responseString = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + responseString);

            final JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                parseErrorsInJsonResponse(jsonObject, gson, subscriptions);
            } else {
                if (!jsonObject.get("data").isJsonNull()) {
                    subscriptionsData =
                        gson.fromJson(jsonObject.getAsJsonObject("data").toString(), JSubscriptionsData.class);
                }

                subscriptions.setData(subscriptionsData);

                // parse included JSONArray
                final JsonArray includedArray = jsonObject.getAsJsonArray("included");
                final int count = includedArray.size();
                for (int i = 0; i < count; i++) {
                    final String includedResponse = includedArray.get(i).toString();
                    final JsonApi jsonApi = gson.fromJson(includedResponse, JsonApi.class);
                    if (jsonApi.getType().equals(EntityType.PRICE)) {
                        price = gson.fromJson(includedResponse, Price.class);
                        LOGGER.info("price id " + price.getId());
                        included.getPrices().add(price);
                    } else if (jsonApi.getType().equals(EntityType.OFFERING)) {
                        offering = gson.fromJson(includedResponse, SubscriptionOffering.class);
                        included.getOfferings().add(offering);
                    } else if (jsonApi.getType().equals(EntityType.BILLINGPLAN)) {
                        billingPlan = gson.fromJson(includedResponse, BillingPlan.class);
                        included.getBillingPlans().add(billingPlan);
                    } else {
                        throw new RuntimeException("Parse Error: Unknown entity type:" + jsonApi.getType().toString());
                    }
                }
                subscriptions.setIncluded(included);
                // Parse errors if present.
                if (!(jsonObject.get("errors") instanceof JsonNull)) {
                    parseErrorsInJsonResponse(jsonObject, gson, subscriptions);
                }
            }
        } catch (ParseException |

            IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return subscriptions;
    }

    /**
     * This method parses the errors array in the JSON response of get subscriptions api
     */
    private void parseErrorsInJsonResponse(final JsonObject jsonObject, final Gson gson,
        final JSubscriptions subscriptions) {
        final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
        final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
        subscriptions.setErrors(Arrays.asList(errors));
    }
}
