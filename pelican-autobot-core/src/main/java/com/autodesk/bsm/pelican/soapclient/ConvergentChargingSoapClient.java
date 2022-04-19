package com.autodesk.bsm.pelican.soapclient;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.CONVERGENT_CHARGING_NAMESPACE;

import com.autodesk.bsm.pelican.soap.convergentcharging.QuerySubscriptionBalanceResponse;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

/**
 * ConvergentChargingSoapClient Created by t_mohav on 2/4/17.
 */
public class ConvergentChargingSoapClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvergentChargingSoapClient.class.getSimpleName());
    private static ConvergentChargingSoapClient soapClient = new ConvergentChargingSoapClient();
    private static EnvironmentVariables environmentVariables =
        new PelicanEnvironment().initializeEnvironmentVariables();

    public static ConvergentChargingSoapClient getInstance() {
        return soapClient;
    }

    private ConvergentChargingSoapClient() {}

    /**
     * @param contractNumber required
     * @return QuerySubscriptionBalanceResponse object
     */
    public QuerySubscriptionBalanceResponse getQuerySubscriptionBalanceResponseObject(final String contractNumber) {
        String soapResponseXml = null;
        try {
            // Create SOAP Connection
            final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            final SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            final String convergentChargingWsdl = environmentVariables.getConvergentChargingWsdl();
            final SOAPMessage soapResponse =
                soapConnection.call(createSOAPRequest(contractNumber), convergentChargingWsdl);

            // Print the response xml to console
            soapResponseXml = getSoapMessageXml(soapResponse);
            LOGGER.info("Response SOAP Message = " + soapResponseXml);
            LOGGER.info("\n");
            soapConnection.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return getResponseObject(soapResponseXml);
    }

    /**
     * Method to construct the SOAP request body
     *
     * @return SOAPMessage
     * @throws Exception
     */
    private SOAPMessage createSOAPRequest(final String contractNumber) throws Exception {
        final MessageFactory messageFactory = MessageFactory.newInstance();
        final SOAPMessage soapMessage = messageFactory.createMessage();
        final SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        final SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("con", CONVERGENT_CHARGING_NAMESPACE);
        // SOAP Body
        final SOAPBody soapBody = envelope.getBody();
        final SOAPElement querySubscriptionBalanceRequestElement =
            soapBody.addChildElement("QuerySubscriptionBalanceRequest", "con");
        final SOAPElement listOfContractsElement =
            querySubscriptionBalanceRequestElement.addChildElement("ListOfContracts");
        final SOAPElement contractElement = listOfContractsElement.addChildElement("Contract");
        final SOAPElement contractNumberElement = contractElement.addChildElement("ContractNumber");
        contractNumberElement.addTextNode(contractNumber);
        soapMessage.saveChanges();

        /* Print the request message */
        LOGGER.info("Request SOAP Message = " + getSoapMessageXml(soapMessage));
        return soapMessage;
    }

    /**
     * Method to get the Soap response XML
     *
     * @param soapMessage required
     * @return soap body xml
     * @throws IOException
     * @throws SOAPException
     */
    private String getSoapMessageXml(final SOAPMessage soapMessage) throws IOException, SOAPException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessage.writeTo(out);
        return new String(out.toByteArray());
    }

    /**
     * Method to parse the response XML and convert to java object
     *
     * @param responseXml required
     * @return required
     */
    private QuerySubscriptionBalanceResponse getResponseObject(final String responseXml) {
        try {
            final SOAPMessage message =
                MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(responseXml.getBytes()));
            final Unmarshaller unmarshaller =
                JAXBContext.newInstance(QuerySubscriptionBalanceResponse.class).createUnmarshaller();
            return (QuerySubscriptionBalanceResponse) unmarshaller
                .unmarshal(message.getSOAPBody().extractContentAsDocument());
        } catch (JAXBException | SOAPException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * main method - Used only for testing purpose.
     *
     * @param args required
     */
    public static void main(final String args[]) {
        final String contractNumber = "201612041751674";
        // Get the response Object
        final QuerySubscriptionBalanceResponse response =
            getInstance().getQuerySubscriptionBalanceResponseObject(contractNumber);
        if (response != null) {
            // Getting the first contract from the list
            final QuerySubscriptionBalanceResponse.ListOfContracts.Contract contract =
                response.getListOfContracts().getContract().get(0);
            LOGGER.info("Contract Number: " + contract.getContractNumber());
            LOGGER.info("Contract Status: " + contract.getStatus());
            LOGGER.info("Contract Type: " + contract.getContractType());
            LOGGER.info(
                "Number of ServicePrivileges: " + contract.getListOfServicePrivileges().getServicePrivilege().size());
        } else {
            LOGGER.info("Error occurred while sending SOAP Request to Server");
        }
    }
}
