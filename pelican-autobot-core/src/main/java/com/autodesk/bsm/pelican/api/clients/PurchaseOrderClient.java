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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class PurchaseOrderClient {

    private static final String END_POINT = "purchaseOrder";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderClient.class.getSimpleName());

    public PurchaseOrderClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add purchase order
     *
     * @return PurchaseOrder or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T add(final PurchaseOrder po) {

        T pojo;

        LOGGER.info("Add Purchase Order");
        final String body = toXml(po);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo,
            PelicanConstants.CONTENT_TYPE_XML_UTF8, PelicanConstants.CONTENT_TYPE_XML);

        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to submit purchase order. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;

    }

    /**
     * This method returns response from the Get Purchase Order API Purchase order Id is accepted as an argument to
     * retrieve payload
     *
     * @return PurchaseOrder
     */
    public PurchaseOrder getById(final String purchaseOrderId) {
        final CloseableHttpResponse response =
            client.doGet(getUrl() + "/" + purchaseOrderId, null, authInfo, null, null);
        return parseResponse(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String toXml(final PurchaseOrder purchaseOrder) {

        final StringWriter body = new StringWriter();

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(PurchaseOrder.class);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(purchaseOrder, body);

        } catch (final JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return body.toString();
    }

    private PurchaseOrder parseResponse(final CloseableHttpResponse response) {

        PurchaseOrder purchaseOrder = null;

        try {

            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(PurchaseOrder.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            purchaseOrder =
                (PurchaseOrder) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return purchaseOrder;

    }

}
