package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.user.Users;
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

public class UsersClient {

    private static final String END_POINT = "users";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersClient.class.getSimpleName());

    public UsersClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get Users with no parameters
     *
     * @return Users or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getUsers() {

        final CloseableHttpResponse response = client.doGet(getUrl(), null, authInfo, null, null);
        return getPojo(response);
    }

    public <T extends PelicanPojo> T getUsersByName(final String name) {

        final Map<String, String> params = new HashMap<>();
        params.put("name", name);

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

    private Users parseResponse(final CloseableHttpResponse response) {

        Users users = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            users = (Users) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JAXBException e) {
            LOGGER.info("Got JAXBException: " + e.getMessage());
            // ignore as user may be missing
            e.printStackTrace();

        }

        return users;

    }
}
