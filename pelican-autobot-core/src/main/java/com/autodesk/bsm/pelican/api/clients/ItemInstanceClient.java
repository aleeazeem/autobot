package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstance;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * This is client class for "itemInstance" endpoint. It has client methods for add, update, update expiration date.
 *
 * @author Shweta Hegde
 */
public class ItemInstanceClient {

    private static final String END_POINT = "itemInstance";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemInstanceClient.class.getSimpleName());

    /**
     * Constructor for ItemInstanceClient
     *
     * @param appFamily
     */
    public ItemInstanceClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Enum for Item Parameter
     */
    public enum ItemInstanceParameter {
        ITEM_ID("itemId"),
        OWNER_ID("ownerId"),
        IP_ADDRESS("ipAddress"),
        ITEM_INSTANCE_ID("itemInstanceId"),
        EXPIRATION_DATE("expirationDate");

        private String name;

        ItemInstanceParameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Add Item Instance to the owner id
     *
     * @param requestParam
     * @return
     */
    public <T extends PelicanPojo> T add(final Map<String, String> requestParam) {

        LOGGER.info("Add item instance for owner id: " + requestParam.get(ItemInstanceParameter.OWNER_ID.getName()));

        LOGGER.info("Post body before encoding: " + requestParam);
        final String body = RestClientUtils.urlEncode(requestParam);

        final CloseableHttpResponse response =
            client.doPost(getUrl(), body, authInfo, MediaType.APPLICATION_FORM_URLENCODED_VALUE, null);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            return (T) RestClientUtils.parseErrorResponse(response);
        } else {
            return (T) parseResponse(response);
        }
    }

    /**
     * Update Item instance, mainly owner id
     *
     * @param requestParam
     * @return
     */
    public <T extends PelicanPojo> T update(final Map<String, String> requestParam) {

        final String itemInstanceId = requestParam.get(ItemInstanceParameter.ITEM_INSTANCE_ID.getName());
        LOGGER.info("Update an iteminstance with id: " + itemInstanceId);

        LOGGER.info("Put body before encoding: " + requestParam);
        final String body = RestClientUtils.urlEncode(requestParam);

        final CloseableHttpResponse response = client.doPut(getUrl() + "/" + itemInstanceId,
            PelicanConstants.CONTENT_TYPE_XML, PelicanConstants.CONTENT_TYPE_URL_ENCODED, body, authInfo);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            return (T) RestClientUtils.parseErrorResponse(response);
        }
        return null;
    }

    /**
     * Update expiration date of the item instance
     *
     * @param requestParam
     * @return
     */
    public <T extends PelicanPojo> T updateExpirationDate(final Map<String, String> requestParam) {

        final String itemInstanceId = requestParam.get(ItemInstanceParameter.ITEM_INSTANCE_ID.getName());
        LOGGER.info("Update an iteminstance with id: " + itemInstanceId);

        LOGGER.info("Put body before encoding: " + requestParam);
        final String body = RestClientUtils.urlEncode(requestParam);

        final CloseableHttpResponse response = client.doPut(getUrl() + "/" + itemInstanceId + "/expirationDate",
            PelicanConstants.CONTENT_TYPE_XML, PelicanConstants.CONTENT_TYPE_URL_ENCODED, body, authInfo);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            return (T) RestClientUtils.parseErrorResponse(response);
        } else {
            return (T) parseResponse(response);
        }
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private ItemInstance parseResponse(final CloseableHttpResponse response) {

        ItemInstance itemInstance = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(ItemInstance.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            itemInstance = (ItemInstance) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return itemInstance;
    }
}
