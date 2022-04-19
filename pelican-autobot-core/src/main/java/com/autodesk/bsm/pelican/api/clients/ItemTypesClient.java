package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.ItemTypes;
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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Rest api for ItemTypes
 *
 * @author t_mohag
 */
public class ItemTypesClient {

    private static final String END_POINT = "itemTypes";
    private final EnvironmentVariables environmentVariables;
    private final AuthenticationInfo authInfo;
    private final HttpRestClient client = new HttpRestClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemTypesClient.class.getSimpleName());

    public ItemTypesClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Find item types by appId
     *
     * @return ItemTypes or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypesByAppId(final String appId) {
        return getItemTypes(appId, null, null, null);
    }

    /**
     * Find item types by appId and startIndex
     *
     * @param appId and startIndex
     * @return itemTypes or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypesWithStartIndex(final String appId, final Integer startIndex) {
        return getItemTypes(appId, startIndex, null, null);
    }

    /**
     * Find item types by appId and blockSize
     *
     * @param appId and blockSize
     * @return itemTypes or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypesWithBlockSize(final String appId, final Integer blockSize) {
        return getItemTypes(appId, null, blockSize, null);
    }

    /**
     * Find item types by appId and skipCount
     *
     * @param appId and skipCount
     * @return itemTypes or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypesWithSkipCount(final String appId, final Boolean skipCount) {
        return getItemTypes(appId, null, null, skipCount);
    }

    /**
     * Find item types
     *
     * @param appId, startIndex, blockSize, skipCount
     * @return itemTypes or HttpErrorInfo
     */
    private <T extends PelicanPojo> T getItemTypes(final String appId, final Integer startIndex,
        final Integer blockSize, final Boolean skipCount) {
        final Map<String, String> params = new HashMap<>();

        if (appId != null) {
            params.put("appId", appId);
        }
        if (startIndex != null) {
            params.put("fr.startIndex", String.valueOf(startIndex));
        }
        if (blockSize != null) {
            params.put("fr.blockSize", String.valueOf(blockSize));
        }
        if (skipCount != null) {
            params.put("fr.skipCount", String.valueOf(skipCount));
        }
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

    private ItemTypes parseResponse(final CloseableHttpResponse response) {
        ItemTypes itemTypes = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(ItemTypes.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            itemTypes = (ItemTypes) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return itemTypes;
    }
}
