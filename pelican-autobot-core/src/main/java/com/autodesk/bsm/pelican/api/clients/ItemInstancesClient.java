package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstances;
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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class ItemInstancesClient {

    private static final String END_POINT = "itemInstances";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemInstancesClient.class.getSimpleName());

    public enum Parameter {
        USER_EXT_KEY("userExternalKey"),
        SUBSCRIPTION_ID("subscriptionId"),
        PARENT_EXT_KEY("parentItemExternalKeys"),
        ITEM_EXT_KEY("itemExternalKeys"),
        INCLUDE_SIBLINGS("includeSiblings"),
        BLOCK_SIZE("fr.blockSize"),
        INCLUDE_EXPIRED("includeExpired");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ItemInstancesClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get item instances with parameters
     *
     * @return ItemInstances or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemInstances(final Map<String, String> params) {

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

    private ItemInstances parseResponse(final CloseableHttpResponse response) {

        ItemInstances itemInstances = null;
        try {
            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Http Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(ItemInstances.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            itemInstances =
                (ItemInstances) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return itemInstances;

    }
}
