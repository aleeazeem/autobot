package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.Root;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.OrderResponse;
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
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class defines REST endpoints and methods for FulfillmentCallBack API Parses the response from XML to
 * corresponding pojos
 *
 * @author kishor
 */
public class FulfillmentCallBackClient {

    private static final String END_POINT = "callback/fulfillment";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentCallBackClient.class.getSimpleName());

    /**
     * Constructor to set the same environment across tests
     *
     * @param appFamily TODO
     */
    public FulfillmentCallBackClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * FulfillPurchaseOrder
     *
     * @return PurchaseOrder or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T fulfillPurchaseOrder(final Root root) {

        T pojo;

        final String body = toXml(root);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo,
            PelicanConstants.CONTENT_TYPE_XML_UTF8, PelicanConstants.CONTENT_TYPE_XML);

        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
            LOGGER.error("Unable to submit purchase order. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String toXml(final Root root) {

        final StringWriter body = new StringWriter();

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Root.class);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(root, body);
        } catch (final JAXBException e) {
            e.printStackTrace();
        }

        return body.toString();
    }

    private OrderResponse parseResponse(final CloseableHttpResponse response) {

        OrderResponse orderResponse = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();

            final JAXBContext jaxbContext = JAXBContext.newInstance(OrderResponse.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            orderResponse = (OrderResponse) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return orderResponse;
    }
}
