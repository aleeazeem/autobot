package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.OfferingDetail;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings.Included;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.amazonaws.util.StringUtils;
import com.amazonaws.util.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for offerings
 *
 * @author yin
 */
public class OfferingsClient {

    private static final String END_POINT = "offerings";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingsClient.class.getSimpleName());

    public enum Parameter {
        ENTITY_TYPE("type"),
        STATUS("status"),
        EXT_KEY("externalKey"),
        OFFERING_TYPE("offeringType"),
        NAME("name"),
        SUPPORT_LEVEL("supportLevel"),
        PRICES("prices"),
        MEDIA_TYPE("mediaType"),
        COUNTRY("filter[country]"),
        STORE_TYPE("filter[storeType]"),
        PRODUCT_LINE("filter[productLine]"),
        STORE_ID("filter[store.id]"),
        STORE_EXTKEY("filter[store.externalKey]"),
        PRICE_ID("filter[priceIds]"),
        FEATURE_EXTERNAL_KEY("filter[feature]"),
        INCLUDE("include"),
        OFFERING_TYPE_FILTER("filter[offeringType]"),
        USAGE_TYPE_FILTER("filter[usageType]"),
        SUPPORT_LEVEL_FILTER("filter[supportLevel]"),
        BILLING_CYCLE_COUNT_FILTER("filter[offer.billingCycleCount]"),
        BILLING_PERIOD_COUNT_FILTER("filter[offer.billingPeriodCount]"),
        BILLING_PERIOD_FILTER("filter[offer.billingPeriod]");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public OfferingsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get offerings by query parameters
     *
     * @param hashmap, which takes multiple parameters which needed for GetOfferings
     * @return Offerings
     */
    public Offerings getOfferings(final Map<String, String> params) {

        final CloseableHttpResponse response = client.doGet(getV2Url(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        return parseResponse(response);
    }

    /**
     * This method gets Offering By Id, including all status of the offering
     *
     * @return Offerings
     */
    public Offerings getOfferingById(final String offeringId, final String includeParams) {

        String url;
        // include parameter is optional, if the value is null, only offering id is sent as Path
        // Parameter
        if (includeParams == null) {
            url = getV2Url() + "/" + offeringId;
        } else {
            // else include parameters are added along with offering id
            url = getV2Url() + "/" + offeringId + "?include=" + includeParams;
        }
        final CloseableHttpResponse response = client.doGet(url, null, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        return parseResponse(response);
    }

    /**
     * Add a basic offering/subscription plan
     *
     * @return pelican pojo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T addOffering(final Offerings offerings) {

        T pojo;
        LOGGER.info("Add Offering");
        final String body = toJSON(offerings);
        final CloseableHttpResponse response = client.doPost(getV2Url(), body, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to create a basic offering. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            LOGGER.info("Parsing the correct response");
            pojo = (T) parseResponseOfPost(response);
        }

        return pojo;
    }

    private String toJSON(final Offerings offerings) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(offerings);
    }

    private String getV2Url() {
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
        final List<BillingPlan> billingPlans = new ArrayList<>();
        final List<Price> prices = new ArrayList<>();
        final List<JPromotionData> promotions = new ArrayList<>();
        final Offering[] offeringArray = null;

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
                    offerings.setOfferings(Arrays.asList(parseOfferingData(dataArray)));
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
                        billingPlans
                            .add((BillingPlan) parseBillingPlanAndPromotion(includedResponse, EntityType.BILLINGPLAN));
                    } else if (jsonApi.getType().equals(EntityType.PRICE)) {
                        final Price price = gson.fromJson(includedResponse, Price.class);
                        prices.add(price);
                    } else if (jsonApi.getType().equals(EntityType.PROMOTION)) {
                        promotions
                            .add((JPromotionData) parseBillingPlanAndPromotion(includedResponse, EntityType.PROMOTION));
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

    /**
     * Method to part Offering Json along with descriptors
     *
     * @param dataArray
     * @return Offering[]
     */
    private Offering[] parseOfferingData(final JsonArray dataArray) {
        final Gson gson = new GsonBuilder().create();
        final Offering[] offeringArray = gson.fromJson(dataArray.toString(), Offering[].class);
        for (int i = 0; i < dataArray.size(); i++) {
            try {
                final Map<String, String> ippProperties = new HashMap<>();
                final Map<String, String> estoreProperties = new HashMap<>();
                final JsonElement element = dataArray.get(i);
                final JSONObject jsonObj = new JSONObject(element.getAsJsonObject().toString());

                if (!(jsonObj.isNull("descriptors"))) {
                    if (jsonObj.getJSONObject("descriptors").has("ipp")) {
                        for (int j = 0; j < jsonObj.getJSONObject("descriptors").getJSONObject("ipp").names()
                            .length(); j++) {
                            final String key =
                                (String) jsonObj.getJSONObject("descriptors").getJSONObject("ipp").names().get(j);
                            final String value =
                                jsonObj.getJSONObject("descriptors").getJSONObject("ipp").getString(key);
                            if (!(StringUtils.isNullOrEmpty(key))) {
                                ippProperties.put(key, value);
                            }
                        }
                    }

                    if (jsonObj.getJSONObject("descriptors").has("estore")) {
                        for (int j = 0; j < jsonObj.getJSONObject("descriptors").getJSONObject("estore").names()
                            .length(); j++) {
                            final String key =
                                (String) jsonObj.getJSONObject("descriptors").getJSONObject("estore").names().get(j);
                            final String value =
                                jsonObj.getJSONObject("descriptors").getJSONObject("estore").getString(key);
                            if (!(StringUtils.isNullOrEmpty(key))) {
                                estoreProperties.put(key, value);
                            }
                        }
                    }
                }
                if (offeringArray[i].descriptors != null) {
                    if (offeringArray[i].descriptors.getIpp() != null) {
                        offeringArray[i].descriptors.getIpp().setProperties(ippProperties);
                    }
                    if (offeringArray[i].descriptors.getEstore() != null) {
                        offeringArray[i].descriptors.getEstore().setProperties(estoreProperties);
                    }
                }
            } catch (final Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return offeringArray;
    }

    /**
     * Method to parse Promotion Data along with Descriptors
     *
     * @param includedResponse
     * @param entityType
     * @return Object
     */
    private Object parseBillingPlanAndPromotion(final String includedResponse, final EntityType entityType) {
        JSONObject jsonObj;
        final Gson gson = new GsonBuilder().create();
        try {
            final Map<String, String> ippProperties = new HashMap<>();
            final Map<String, String> estoreProperties = new HashMap<>();
            jsonObj = new JSONObject(includedResponse);

            if (!(jsonObj.isNull("descriptors"))) {
                if (jsonObj.getJSONObject("descriptors").has("ipp")) {
                    for (int j = 0; j < jsonObj.getJSONObject("descriptors").getJSONObject("ipp").names()
                        .length(); j++) {
                        final String key =
                            (String) jsonObj.getJSONObject("descriptors").getJSONObject("ipp").names().get(j);
                        final String value = jsonObj.getJSONObject("descriptors").getJSONObject("ipp").getString(key);
                        if (!(StringUtils.isNullOrEmpty(key))) {
                            ippProperties.put(key, value);
                        }
                    }
                }

                if (jsonObj.getJSONObject("descriptors").has("estore")) {
                    for (int j = 0; j < jsonObj.getJSONObject("descriptors").getJSONObject("estore").names()
                        .length(); j++) {
                        final String key =
                            (String) jsonObj.getJSONObject("descriptors").getJSONObject("estore").names().get(j);
                        final String value =
                            jsonObj.getJSONObject("descriptors").getJSONObject("estore").getString(key);
                        if (!(StringUtils.isNullOrEmpty(key))) {
                            estoreProperties.put(key, value);
                        }
                    }
                }
            }

            if (entityType.equals(EntityType.BILLINGPLAN)) {
                BillingPlan billingPlan = new BillingPlan();
                billingPlan = gson.fromJson(includedResponse, BillingPlan.class);
                if (billingPlan.descriptors != null) {
                    if (billingPlan.descriptors.getIpp() != null) {
                        billingPlan.descriptors.getIpp().setProperties(ippProperties);
                    }
                    if (billingPlan.descriptors.getEstore() != null) {
                        billingPlan.descriptors.getEstore().setProperties(estoreProperties);
                    }
                }
                return billingPlan;
            } else if (entityType.equals(EntityType.PROMOTION)) {
                JPromotionData promotion = new JPromotionData();
                promotion = gson.fromJson(includedResponse, JPromotionData.class);
                if (promotion.descriptors != null) {
                    if (promotion.descriptors.getIpp() != null) {
                        promotion.descriptors.getIpp().setProperties(ippProperties);
                    }
                    if (promotion.descriptors.getEstore() != null) {
                        promotion.descriptors.getEstore().setProperties(estoreProperties);
                    }
                }
                return promotion;
            } else {
                LOGGER.error("Descriptor doesnt exist for this entity Type");
            }

        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Offerings parseResponseOfPost(final CloseableHttpResponse response) {

        final Offerings offerings = new Offerings();
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
                offerings.setError(errors[0]);
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final Offering basicOfferingObj = gson.fromJson(jsonObj, Offering.class);
                        }
                    } else {
                        final Offering data =
                            gson.fromJson(jsonObject.getAsJsonObject("data").toString(), Offering.class);
                        offerings.setOffering(data);
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
        return offerings;
    }

}
