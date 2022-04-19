package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Endpoint for offerings
 *
 * @author mandas
 */
public class OfferingsV3Client {

    private static final String END_POINT = "offerings";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;

    public enum Parameter {
        ENTITY_TYPE("type"),
        STATUS("status"),
        EXT_KEY("externalKey"),
        OFFERING_TYPE("offeringType"),
        NAME("name"),
        SUPPORT_LEVEL("supportLevel"),
        PRICES("prices"),
        MEDIA_TYPE("mediaType"),
        COUNTRY("country"),
        STORE_TYPE("storeType"),
        PRODUCT_LINE("productLine"),
        STORE_ID("store.id"),
        STORE_EXTKEY("store.externalKey"),
        PRICE_ID("priceIds"),
        FEATURE_EXTERNAL_KEY("feature"),
        INCLUDE("include"),
        OFFERING_TYPE_FILTER("offeringType"),
        USAGE_TYPE_FILTER("usageType"),
        SUPPORT_LEVEL_FILTER("supportLevel"),
        BILLING_CYCLE_COUNT_FILTER("offer.billingCycleCount"),
        BILLING_PERIOD_COUNT_FILTER("offer.billingPeriodCount"),
        BILLING_PERIOD_FILTER("offer.billingPeriod");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public OfferingsV3Client(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get offerings V3 by query parameters
     *
     * @param hashmap, which takes multiple parameters which needed for GetOfferings
     * @return Map
     */
    public Map<String, Object> getOfferings(final Map<String, String> params) {

        final CloseableHttpResponse response = client.doGet(getV3Url(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);

        try {
            return new ObjectMapper().readValue(EntityUtils.toString(response.getEntity()),
                new TypeReference<Map<String, Object>>() {});
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private String getV3Url() {
        return environmentVariables.getV3ApiUrl() + "/" + END_POINT;
    }

}
