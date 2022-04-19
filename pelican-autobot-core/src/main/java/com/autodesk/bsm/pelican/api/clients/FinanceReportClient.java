package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class FinanceReportClient {

    private final EnvironmentVariables environmentVariables;
    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceReportClient.class.getSimpleName());
    private final String applicationFamily;

    public FinanceReportClient(final EnvironmentVariables environmentVariables, final String applicationFamily) {
        this.environmentVariables = environmentVariables;
        this.applicationFamily = applicationFamily;
    }

    /*
     * This method returns only report header
     *
     * @param - startDate
     *
     * @param - endDate
     *
     * @param - fulfillment start date
     *
     * @param - fulfillment end date
     *
     * @param - last modified start date
     *
     * @param - last modified end date
     *
     * @return - header as string
     */
    public String getReportHeader(final String startDate, final String endDate, final String fulfillmentStartDate,
        final String fulfillmentEndDate, final String lastModifiedStartDate, final String lastModifiedEndDate) {
        final String data = getReport(startDate, endDate, fulfillmentStartDate, fulfillmentEndDate,
            lastModifiedStartDate, lastModifiedEndDate);

        String header = null;
        if (data == null) {
            return header;
        }

        final BufferedReader rdr = new BufferedReader(new StringReader(data));
        try {
            header = rdr.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return header;
    }

    /**
     * Get Finance Report Header for the purchase order id
     *
     * @param purchaseOrderId
     * @return header
     */
    public String getReportHeader(final String purchaseOrderId) {

        final String data = getReport(purchaseOrderId);

        String header = null;
        if (data == null) {
            return header;
        }

        final BufferedReader rdr = new BufferedReader(new StringReader(data));
        try {
            header = rdr.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return header;

    }

    public List<String> getReportData(final String startDate, final String endDate, final String fulfillmentStartDate,
        final String fulfillmentEndDate, final String lastModifiedStartDate, final String lastModifiedEndDate) {

        final String data = getReport(startDate, endDate, fulfillmentStartDate, fulfillmentEndDate,
            lastModifiedStartDate, lastModifiedEndDate);

        return readFinanceReport(data);
    }

    /**
     * Get Finance Report data for the purchase order id
     *
     * @param purchaseOrderId
     * @return data
     */
    public List<String> getReportData(final String purchaseOrderId) {

        final String data = getReport(purchaseOrderId);

        return readFinanceReport(data);
    }

    private List<String> readFinanceReport(final String data) {

        final List<String> lines = new ArrayList<>();

        if (data == null) {
            return lines;
        } else if (data.startsWith("<?xml version")) {

            String errorMessage = null;
            try {
                final JAXBContext jaxbContext = JAXBContext.newInstance(HttpError.class);
                final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
                final HttpError httpError =
                    (HttpError) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(data)));
                errorMessage = httpError.getErrorMessage();
            } catch (IllegalStateException | JAXBException e) {
                e.printStackTrace();
            }
            lines.add(errorMessage);
            return lines;
        } else {
            final BufferedReader rdr = new BufferedReader(new StringReader(data));

            // skip the header
            String line;
            try {
                line = rdr.readLine();
                boolean done = false;

                while (!done) {
                    line = rdr.readLine();
                    if (line == null) {
                        done = true;
                    } else {
                        lines.add(line);
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    /**
     * Find the data from the Order Date column
     */
    public List<String> getColumnValues(final String startDate, final String endDate, final String columnName,
        final String fulfillmentStartDate, final String fulfillmentEndDate, final String lastModifiedStartDate,
        final String lastModifiedEndDate) {
        final List<String> values = new ArrayList<>();

        // get column index for order date
        int columnIndex = -1;

        final String header = getReportHeader(startDate, endDate, fulfillmentStartDate, fulfillmentEndDate,
            lastModifiedStartDate, lastModifiedEndDate);
        final String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase("Order Date")) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex < 0) {
            throw new RuntimeException("Unable to find header '" + columnName + "' in Finance Report\n" + header);
        }

        // get values
        final List<String> lines = getReportData(startDate, endDate, fulfillmentStartDate, fulfillmentEndDate,
            lastModifiedStartDate, lastModifiedEndDate);
        for (final String line : lines) {
            final String[] rowData = line.split(",");
            values.add(rowData[columnIndex]);
        }
        return values;
    }

    private String getReport(final String orderStartDate, final String orderEndDate, final String fulfillmentStartDate,
        final String fulfillmentEndDate, final String lastModifiedStartDate, final String lastModifiedEndDate) {

        final HttpRestClient client = new HttpRestClient();
        final AuthenticationInfo authInfo = new AuthenticationInfo(environmentVariables, applicationFamily);

        final Map<String, String> bodyParams = new HashMap<>();

        if (orderStartDate != null || orderEndDate != null) {
            bodyParams.put("orderStartDate", orderStartDate);
            bodyParams.put("orderEndDate", orderEndDate);
        }

        if (fulfillmentStartDate != null || fulfillmentEndDate != null) {
            bodyParams.put("fulfillmentStartDate", fulfillmentStartDate);
            bodyParams.put("fulfillmentStartDate", fulfillmentEndDate);
        }

        if (lastModifiedStartDate != null || lastModifiedEndDate != null) {
            bodyParams.put("lastModifiedStartDate", lastModifiedStartDate);
            bodyParams.put("lastModifiedEndDate", lastModifiedEndDate);
        }

        LOGGER.info("Post body before encoding: " + bodyParams);
        final String body = RestClientUtils.urlEncode(bodyParams);

        final CloseableHttpResponse response =
            client.doPost(getUrl(), body, authInfo, MediaType.APPLICATION_FORM_URLENCODED_VALUE, null);
        if (response == null) {
            return null;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code: " + response.getStatusLine().getStatusCode());
            String errorMessage = "";
            try {
                errorMessage = EntityUtils.toString(response.getEntity());
                LOGGER.info("Error Message" + errorMessage);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }

            return errorMessage;
        }

        String reportData = null;
        try {
            reportData = EntityUtils.toString(response.getEntity());

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return reportData;
    }

    private String getReport(final String purchaseOrderId) {

        final HttpRestClient client = new HttpRestClient();
        final AuthenticationInfo authInfo = new AuthenticationInfo(environmentVariables, applicationFamily);

        final CloseableHttpResponse response =
            client.doGet(getUrl() + "/" + purchaseOrderId, null, authInfo, null, null);
        if (response == null) {
            return null;
        }

        String errorMessage = null;
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code: " + response.getStatusLine().getStatusCode());
            try {
                errorMessage = EntityUtils.toString(response.getEntity());
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            return errorMessage;
        }

        String reportData = null;
        try {
            reportData = EntityUtils.toString(response.getEntity());

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return reportData;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/reports/finance";
    }
}
