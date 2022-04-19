package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.EligibleVersions;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BicReleaseClient {

    private static final String PRODUCT_LINE_CODES = "productLineCodes";
    private final EnvironmentVariables environmentVariables;
    private static final Logger LOGGER = LoggerFactory.getLogger(BicReleaseClient.class.getSimpleName());

    public BicReleaseClient(final EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Get eligibleVersion with no parameter
     *
     * @return EligibleVersions
     */
    public EligibleVersions getEligibleVersions() {
        return getEligibleVersions(new ArrayList<>(), null, null);
    }

    /**
     * Get eligibleVersions with productLineCodes
     *
     * @return EligibleVersions or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getEligibleVersions(final List<String> productLines) {

        return getEligibleVersions(productLines, null, null);
    }

    /**
     * Get eligibleVersions by start and end date
     *
     * @param startDate and endDate
     * @return EligibleVersions or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getEligibleVersions(final String startDate, final String endDate) {
        return getEligibleVersions(new ArrayList<>(), startDate, endDate);
    }

    /**
     * Get eligibleVersions by start and end date
     *
     * @param startDate and endDate
     * @return EligibleVersions or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T getEligibleVersions(final List<String> productLines, final String startDate,
        final String endDate) {

        T pojo;

        final HttpRestClient client = new HttpRestClient();

        final AuthenticationInfo authInfo =
            new AuthenticationInfo(environmentVariables, environmentVariables.getApplicationFamily());

        final String queryValue = listToString(productLines);

        final Map<String, String> params = new HashMap<>();

        if (queryValue != null) {
            params.put(PRODUCT_LINE_CODES, queryValue);
        }

        if (startDate != null && !startDate.equals("")) {
            params.put("fcsDateAfter", startDate);
        }

        if (endDate != null && !endDate.equals("")) {
            params.put("fcsDateBefore", endDate);
        }

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/productLine/eligibleVersions";
    }

    private EligibleVersions parseResponse(final CloseableHttpResponse response) {

        EligibleVersions eligibleVersion = null;
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy").create();
            eligibleVersion = gson.fromJson(content, EligibleVersions.class);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return eligibleVersion;
    }

    private String listToString(final List<String> values) {
        String queryValue = null;
        for (final String value : values) {
            if (queryValue == null) {
                queryValue = value;
            } else {
                queryValue = queryValue + "," + value;
            }
        }
        return queryValue;
    }
}
