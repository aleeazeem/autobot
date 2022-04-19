package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription.Included;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.enums.EntityType;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint for Subscription JSON API. This class provides the methods which invokes the get subscription api and
 * returns the parsed response.
 *
 * @author jains
 */
public class SubscriptionJsonClient {
    private static final String END_POINT = "subscription";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionJsonClient.class.getSimpleName());

    public SubscriptionJsonClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    private enum Parameter {
        INCLUDE("include");
        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    /**
     * This method does get call to get a subscription
     *
     * @return JSubscription
     */
    public JSubscription getSubscription(final String subscriptionId, final String contentType) {
        return getSubscription(subscriptionId, null, contentType);
    }

    /**
     * This method does get call to get a subscription
     *
     * @return JSubscription
     */
    public JSubscription getSubscription(final String subscriptionId, final String include, final String contentType) {
        final Map<String, String> params = new HashMap<>();
        params.put(Parameter.INCLUDE.getName(), include);

        final CloseableHttpResponse response =
            client.doGet(getUrl(subscriptionId), params, authInfo, contentType, contentType);
        return parseResponse(response);
    }

    private String getUrl(final String id) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + id;
    }

    /*
     * This method parse the JSON response of get subscription by id
     */
    private JSubscription parseResponse(final CloseableHttpResponse response) {
        final JSubscription subscription = new JSubscription();
        Subscription subscriptionData = new Subscription();
        final Included included = new Included();
        Price price = new Price();
        SubscriptionOffering offering = new SubscriptionOffering();
        BillingPlan billingPlan = new BillingPlan();

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
                subscription.setErrors(Arrays.asList(errors));
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    subscriptionData = gson.fromJson(jsonObject.getAsJsonObject("data").toString(), Subscription.class);
                }

                // parse included JSONArray
                final JsonArray includedArray = jsonObject.getAsJsonArray("included");
                final int count = includedArray.size();
                for (int i = 0; i < count; i++) {
                    final String includedResponse = includedArray.get(i).toString();
                    final JsonApi jsonApi = gson.fromJson(includedResponse, JsonApi.class);
                    if (jsonApi.getType().equals(EntityType.PRICE)) {
                        price = gson.fromJson(includedResponse, Price.class);
                        LOGGER.info("price id " + price.getId());
                        included.setPrice(price);
                    } else if (jsonApi.getType().equals(EntityType.OFFERING)) {
                        offering = gson.fromJson(includedResponse, SubscriptionOffering.class);
                        included.setOffering(offering);
                    } else if (jsonApi.getType().equals(EntityType.BILLINGPLAN)) {
                        billingPlan = gson.fromJson(includedResponse, BillingPlan.class);
                        included.setBillingPlan(billingPlan);
                    } else {
                        throw new RuntimeException("Parse Error: Unknown entity type:" + jsonApi.getType().toString());
                    }
                }
                subscription.setData(subscriptionData);
                subscription.setIncluded(included);
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
        return subscription;
    }
}
