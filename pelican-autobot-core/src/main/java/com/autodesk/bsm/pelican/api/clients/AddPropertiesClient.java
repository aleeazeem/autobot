package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
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
 * This is a resource class for adding properties to a purchase order
 *
 * @author vineel
 */
public class AddPropertiesClient {

    private static final String END_POINT2 = "properties";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddPropertiesClient.class.getSimpleName());

    public AddPropertiesClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add properties to purchase order
     *
     * @param poId
     * @param propertiesMap
     * @param encode
     * @return PurchaseOrder or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T addProperties(final String poId, final Map<String, String> propertiesMap,
        final boolean encode) {

        T pojo;

        LOGGER.info("Add Properties to Purchase Order");
        final CloseableHttpResponse response;
        String body;
        if (encode) {

            body = RestClientUtils.urlEncode(propertiesMap);
            response = client.doPost(getUrl(poId), body, authInfo, PelicanConstants.CONTENT_TYPE_URL_ENCODED,
                PelicanConstants.CONTENT_TYPE_XML);
        } else {

            // This else part is specifically for defect BIC-7876.
            final StringBuilder stringBuilder = new StringBuilder();
            for (final String key : propertiesMap.keySet()) {
                stringBuilder.append(key).append("=").append(propertiesMap.get(key).replace(" ", "%20")).append("&");
            }

            body = stringBuilder.toString();
            response = client.doPost(getUrl(poId), body.substring(0, body.length() - 1), authInfo,
                PelicanConstants.CONTENT_TYPE_URL_ENCODED, PelicanConstants.CONTENT_TYPE_XML);
        }

        final int status = response.getStatusLine().getStatusCode();
        LOGGER.info("Response Status: " + status);
        if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
            LOGGER.error("Unable to add properties to purchase order. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;

    }

    /**
     * Method to return the end point url
     */
    private String getUrl(final String purchaseOrderId) {
        return environmentVariables.getV2ApiUrl() + "/" + PelicanConstants.PURCHASEORDER + "/" + purchaseOrderId + "/"
            + END_POINT2;
    }

    /**
     * @return PurchaseOrder
     */
    private PurchaseOrder parseResponse(final CloseableHttpResponse response) {

        PurchaseOrder purchaseOrder = null;

        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(PurchaseOrder.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            purchaseOrder = (PurchaseOrder) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | JAXBException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return purchaseOrder;

    }

}
