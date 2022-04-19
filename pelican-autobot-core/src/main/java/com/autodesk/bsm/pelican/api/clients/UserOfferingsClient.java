package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi;
import com.autodesk.bsm.pelican.api.pojos.json.Meta;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.OfferingDetail;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings.Included;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.enums.EntityType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserOfferingsClient {

    private static final String END_POINT = "user/offerings";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserOfferingsClient.class.getSimpleName());

    public enum Parameter {
        USER_EXT_KEY("filter[user.externalKey]"),
        SUBSCRIPTION_ID("filter[subscription]"),
        OFFER_EXT_KEY("filter[offer.externalKey]"),
        STORE_EXT_KEY("filter[store.externalKey]"),
        STORE_ID("filter[store.id]"),
        STORE_TYPE_EXT_KEY("filter[storeType]"),
        COUNTRY("filter[country]"),
        INCLUDE("include");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public UserOfferingsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get User Offerings by query parameters
     *
     * @param hashmap, which takes multiple parameters which needed for GetUserOfferings
     * @return Offerings
     */
    public Offerings getUserOfferings(final Map<String, String> params) {

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        return parseResponse(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * Offerings response has data, included and errors array Data contains list of offerings Included has the
     * supporting info: offering detail, billing plans, and prices.
     *
     * @return Offerings
     */
    private Offerings parseResponse(final CloseableHttpResponse response) {

        final Offerings offerings = new Offerings();
        final Included included = new Included();
        Meta meta = new Meta();
        final List<BillingPlan> billingPlans = new ArrayList<>();
        final List<Price> prices = new ArrayList<>();
        final List<JPromotionData> promotions = new ArrayList<>();
        Offering[] offeringArray;

        final int status = response.getStatusLine().getStatusCode();

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
                offerings.setErrors(Arrays.asList(errors));
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    final JsonArray dataArray = jsonObject.getAsJsonArray("data");
                    offeringArray = gson.fromJson(dataArray.toString(), Offering[].class);
                    offerings.setOfferings(Arrays.asList(offeringArray));
                }

                // Parse included
                final JsonArray includedArray = jsonObject.getAsJsonArray("included");
                final int count = includedArray.size();
                for (int i = 0; i < count; i++) {
                    final String includedResponse = includedArray.get(i).toString();
                    final JsonApi jsonApi = gson.fromJson(includedResponse, JsonApi.class);
                    if (jsonApi.getType().equals(EntityType.OFFERING_DETAIL)) {
                        included.setOfferingDetail(gson.fromJson(includedResponse, OfferingDetail.class));
                    } else if (jsonApi.getType().equals(EntityType.BILLINGPLAN)) {
                        final BillingPlan billingPlan = gson.fromJson(includedResponse, BillingPlan.class);
                        billingPlans.add(billingPlan);
                    } else if (jsonApi.getType().equals(EntityType.PRICE)) {
                        final Price price = gson.fromJson(includedResponse, Price.class);
                        prices.add(price);
                    } else if (jsonApi.getType().equals(EntityType.PROMOTION)) {
                        final JPromotionData promotion = gson.fromJson(includedResponse, JPromotionData.class);
                        promotions.add(promotion);
                    } else {
                        throw new RuntimeException(
                            "Parse Offerings: Unknown entity type:" + jsonApi.getType().toString());
                    }
                }

                // Parse errors if present.
                if (!(jsonObject.get("errors") instanceof JsonNull)) {
                    final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
                    final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
                    offerings.setErrors(Arrays.asList(errors));
                }

                included.setBillingPlans(billingPlans);
                included.setPrices(prices);
                included.setPromotions(promotions);
                offerings.setIncluded(included);

                // Parse the meta object
                final JsonObject metaObject = jsonObject.getAsJsonObject("meta");
                meta = gson.fromJson(metaObject.toString(), Meta.class);
                offerings.setMeta(meta);
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
        return offerings;
    }
}
