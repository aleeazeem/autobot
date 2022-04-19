package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.MetaPagination;
import com.autodesk.bsm.pelican.api.pojos.json.Pagination;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLines;
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

/**
 * Endpoint for Product Lines. This class provides the methods which actually invoke the getProductLines API and returns
 * the parsed response.
 *
 * @author Muhammad
 */
public class ProductLinesClient {

    private static final String END_POINT = "productlines";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLinesClient.class.getSimpleName());

    public enum Parameter {
        COUNTRY_CODE("filter[country]"),
        STORE_ID("filter[store.id]"),
        STORE_EXTERNAL_KEY("filter[store.externalKey]"),
        START_INDEX("page[startIndex]"),
        BLOCK_SIZE("page[blockSize]"),
        SKIP_COUNT("page[skipCount]");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ProductLinesClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get productlines by Country Code and Store Id or externalKey
     *
     * @return ProductLines or HttpError
     */
    public <T extends PelicanPojo> T getProductLines(final Map<String, String> params) {
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        // Return the productLines.
        return parseResponse(response, params);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * ProductLines response has data and errors array.
     *
     * @return ProductLines or HttpError
     */
    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response,
        final Map<String, String> params) {
        T pojo = null;
        final int status = response.getStatusLine().getStatusCode();
        final ProductLines productLines = new ProductLines();

        try {
            final Gson gson = new GsonBuilder().create();
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();

            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                final HttpError httpError = new HttpError();
                httpError.setStatus(response.getStatusLine().getStatusCode());
                httpError.setErrorMessage(response.getStatusLine().getReasonPhrase());
                pojo = (T) httpError;
            } else {
                if (!(jsonObject.get("data") instanceof JsonNull)) {
                    final JsonArray dataArray = jsonObject.getAsJsonArray("data");
                    final List<ProductLineData> productLineDataList = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++) {
                        final ProductLineData productLineData = new ProductLineData();
                        final JsonObject dataObject = dataArray.get(i).getAsJsonObject();
                        productLineData.setType(dataObject.getAsJsonPrimitive("type").getAsString());
                        productLineData.setId(dataObject.getAsJsonPrimitive("id").getAsString());
                        productLineData.setExternalKey(dataObject.getAsJsonPrimitive("externalKey").getAsString());
                        productLineData.setName(dataObject.getAsJsonPrimitive("name").getAsString());
                        if (!(jsonObject.get("isActive") instanceof JsonNull)) {
                            productLineData
                                .setIsActive(Boolean.valueOf(dataObject.getAsJsonPrimitive("isActive").toString()));
                        }
                        productLineDataList.add(productLineData);
                    }
                    productLines.setProductLineData(productLineDataList);
                }
            }
            // Parse errors if present.
            if (!(jsonObject.get("errors") instanceof JsonNull)) {
                final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
                productLines.setErrors(Arrays.asList(errors));
            }
            // parse meta object
            if ((jsonObject.get("errors") instanceof JsonNull)) {
                final Pagination pagination = new Pagination();
                final MetaPagination metaPagination = new MetaPagination();

                final JsonObject metaPaginationObject = jsonObject.getAsJsonObject("meta");
                final JsonObject paginationObject = metaPaginationObject.getAsJsonObject("pagination");
                pagination.setblockSize(paginationObject.getAsJsonPrimitive("blockSize").getAsString());
                if (params.containsKey("page[skipCount]")) {
                    pagination.setCount(paginationObject.getAsJsonPrimitive("count").getAsString());
                }
                pagination.setSkipCount(paginationObject.getAsJsonPrimitive("skipCount").getAsString());
                pagination.setstartIndex(paginationObject.getAsJsonPrimitive("startIndex").getAsString());

                metaPagination.setPagination(pagination);
                productLines.setMetaPagination(metaPagination);
            }

            pojo = (T) productLines;
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
}
