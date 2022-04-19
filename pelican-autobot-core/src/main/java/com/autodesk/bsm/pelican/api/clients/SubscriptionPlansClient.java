package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlans;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class SubscriptionPlansClient {

    private static final String END_POINT = "subscriptionPlans";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlansClient.class.getSimpleName());

    public enum Parameter {
        PLAN_NAME("name"),
        PLAN_EXT_KEYS("planExternalKeys"),
        STATUS("status"),
        USAGE_TYPE("usageType"),
        PRODUCT_LINE("productLine"),
        OFFER_EXT_KEYs("offerExternalKeys"),
        ITEM_EXT_KEY("itemExternalKey"),
        START_INDEX("fr.startIndex"),
        BLOCK_SIZE("fr.blockSize"),
        SKIP_COUNT("fr.skipCount");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public SubscriptionPlansClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get SubscriptionPlans by external keys
     *
     * @return SubscriptionPlans or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getSubscriptionPlansByExternalKeys(final List<String> keys) {

        final String queryParm = listToString(keys);
        final Map<String, String> params = new HashMap<>();
        params.put(Parameter.PLAN_EXT_KEYS.getName(), queryParm);

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Get SubscriptionPlans by offer external keys
     *
     * @return SubscriptionPlans or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getSubscriptionPlansByOfferExternalKeys(final List<String> keys) {
        final String queryParm = listToString(keys);
        final Map<String, String> params = new HashMap<>();
        params.put(Parameter.OFFER_EXT_KEYs.getName(), queryParm);

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Get SubscriptionPlans by Parameter
     *
     * @return SubscriptionPlans or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getSubscriptionPlans(final Map<String, String> params) {
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        return getPojo(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T getPojo(final CloseableHttpResponse response) {
        T pojo;
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private SubscriptionPlans parseResponse(final CloseableHttpResponse response) {

        SubscriptionPlans subscriptionPlans = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(SubscriptionPlans.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            subscriptionPlans = (SubscriptionPlans) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | JAXBException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return subscriptionPlans;
    }

    /**
     * Convert a list of keys into comma separated string
     *
     * @return comma separated keys
     */
    private String listToString(final List<String> keys) {
        String value = null;
        for (final String key : keys) {
            if (value == null) {
                value = key;
            } else {
                value = value + "," + key;
            }
        }
        return value;
    }
}
