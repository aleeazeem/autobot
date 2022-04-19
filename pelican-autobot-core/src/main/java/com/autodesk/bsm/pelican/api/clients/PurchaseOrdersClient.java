package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrders;
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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * This class is the end point of all PurchaseOrders.
 *
 * @author Shweta Hegde
 */
public class PurchaseOrdersClient {

    private static final String END_POINT = "purchaseOrders";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrdersClient.class.getSimpleName());

    /**
     * Below enums are filters for Get Purchase Orders
     */
    public enum PurchaseOrderParameter {
        BUYER_USER_ID("buyerUserId"),
        BUYER_USER_EXTERNAL_KEY("buyerUserExternalKey"),
        EXTERNAL_KEY("externalKey"),
        ORDER_STATE("orderState"),
        CREATED_AFTER("createdAfter"),
        CREATED_BEFORE("createdBefore"),
        MODIFIED_AFTER("modifiedAfter"),
        MODIFIED_BEFORE("modifiedBefore"),
        FULFILLMENT_STATUS("fulfillmentStatus"),
        PURCHASE_TYPE("purchaseType"),
        PAYMENT_TYPE("paymentType"),
        GATEWAY_CONFIG_ID("gatewayConfigId"),
        SUBSCRIPTION_ID("subscriptionId"),
        TAGS("tags"),
        PROPERTIES("properties"),
        START_INDEX("fr.startIndex"),
        BLOCK_SIZE("fr.blockSize"),
        SKIP_COUNT("fr.skipCount");

        private String name;

        PurchaseOrderParameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public PurchaseOrdersClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get PurchaseOrders by Parameter, it accepts MAP of filters
     *
     * @return PurchaseOrders or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getPurchaseOrders(final Map<String, String> params) {
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

    private PurchaseOrders parseResponse(final CloseableHttpResponse response) {

        PurchaseOrders purchaseOrders = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(PurchaseOrders.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            purchaseOrders = (PurchaseOrders) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return purchaseOrders;
    }

}
