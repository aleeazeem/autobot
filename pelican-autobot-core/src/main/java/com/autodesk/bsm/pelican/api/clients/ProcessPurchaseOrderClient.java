package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

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
 * This class defines the REST endpoints and API calls related to the ProcessPurchaseOrders and parses the response back
 * from XML to PO objects
 *
 * @author kishor
 */
public class ProcessPurchaseOrderClient {

    private static final String END_POINT = "purchaseOrder";
    private static final String COMMAND_ENDPOINT = "command";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessPurchaseOrderClient.class.getSimpleName());

    public ProcessPurchaseOrderClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Process purchase order
     *
     * @param PurchaseOrderCommand - PENDING,CHARGE,REFUND etc
     * @return PurchaseOrder or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T process(final PurchaseOrderCommand poCommand, final String poId) {

        T pojo;

        LOGGER.info("Process Purchase Order");
        final String body = toXml(poCommand);
        final CloseableHttpResponse response = client.doPost(getUrl(poId), body, authInfo,
            PelicanConstants.CONTENT_TYPE_XML_UTF8, PelicanConstants.CONTENT_TYPE_XML);

        final int status = response.getStatusLine().getStatusCode();
        if (status != 201 && status != 200) {
            LOGGER.error("Unable to Process purchase order " + poId + ". Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    private String getUrl(final String id) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + id + "/" + COMMAND_ENDPOINT;
    }

    private String toXml(final PurchaseOrderCommand purchaseOrderCommand) {

        final StringWriter body = new StringWriter();

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(PurchaseOrderCommand.class);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(purchaseOrderCommand, body);
        } catch (final JAXBException e) {
            e.printStackTrace();
        }

        return body.toString();
    }

    private PurchaseOrder parseResponse(final CloseableHttpResponse response) {

        PurchaseOrder purchaseOrder = null;

        try {

            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(PurchaseOrder.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            purchaseOrder = (PurchaseOrder) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return purchaseOrder;
    }
}
