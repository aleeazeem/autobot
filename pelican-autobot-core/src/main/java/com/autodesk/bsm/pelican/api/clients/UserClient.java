package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.user.GDPRRequest;
import com.autodesk.bsm.pelican.api.pojos.user.GDPRResponse;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Rest service: User endpoint
 *
 * @author Shweta Hegde
 */
public class UserClient {

    private static final String END_POINT = "user";
    private static final String GDPR_END_POINT = "/user/gdpr?appFamilyId=%s";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserClient.class.getSimpleName());

    public UserClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * User parameter enum is used to set create user with different user parameters
     */
    public enum UserParameter {
        NAME("name"),
        EXTERNAL_KEY("externalKey"),
        APPLICATION_FAMILY("applicationFamily"),
        USER_ID("userID"),
        PASSWORD("Password0");

        private String name;

        UserParameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Get user by id
     *
     * @return User or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getUserById(final String id) {

        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + id, null, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Get user by external key
     *
     * @return User or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getUserByExternalKey(final String externalKey) {
        final Map<String, String> params = new HashMap<>();
        params.put("externalKey", externalKey);
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Add user using UserParameters passed in HashMap
     *
     * @param userMap ( Map)
     * @return User
     */
    public User addUser(final Map<String, String> userMap) {

        LOGGER.info("Add user: " + userMap.get(UserParameter.NAME.getName()));

        LOGGER.info("Post body before encoding: " + userMap);
        final String body = RestClientUtils.urlEncode(userMap);

        final CloseableHttpResponse response =
            client.doPost(getUrl(), body, authInfo, MediaType.APPLICATION_FORM_URLENCODED_VALUE, null);
        return parseResponse(response);
    }

    /**
     * Process GDPR User Request.
     *
     * @param request
     * @param environmentVariables
     * @param <T>
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public <T extends PelicanPojo> T gdprUser(final GDPRRequest request,
        final EnvironmentVariables environmentVariables) throws ParseException, IOException {
        final T pojo;

        final String key =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_VALUE_FROM_MIDAS_HIVE, "GDPR_WEBHOOK_SECRET"),
                environmentVariables).get(0).get(PelicanDbConstants.VALUE);
        LOGGER.info("Process GDPR User Request");
        final Gson gson = new GsonBuilder().create();
        final CloseableHttpResponse response = client.doGDPRPost(
            String.format(environmentVariables.getGdprUrl() + GDPR_END_POINT, environmentVariables.getAppFamilyId()),
            gson.toJson(request), key);
        final int status = response.getStatusLine().getStatusCode();
        final String content = EntityUtils.toString(response.getEntity());
        LOGGER.info("Content : {}", content);
        LOGGER.info("Status while processing GDPR request {}", status);
        final boolean isContentEmpty = content.isEmpty();

        if ((status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST) && (!isContentEmpty)) {
            pojo = (T) gson.fromJson(content, GDPRResponse.class);
        } else {
            pojo = (T) parseError(response);
        }
        return pojo;
    }

    private HttpError parseError(final CloseableHttpResponse response) throws IOException {
        HttpError errorResponse = null;
        try {
            errorResponse = new HttpError();
            final int status = response.getStatusLine().getStatusCode();
            final String reason = response.getStatusLine().getReasonPhrase();
            errorResponse.setStatus(status);
            errorResponse.setReason(reason);

            response.close();
        } catch (final IllegalStateException e) {
            LOGGER.error("Error occured while parsing error {}", e);
        }
        return errorResponse;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T getPojo(final CloseableHttpResponse response) {
        T pojo;
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = {}", status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private User parseResponse(final CloseableHttpResponse response) {

        User user = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(User.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            user = (User) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | JAXBException | IOException e) {
            e.printStackTrace();
        }

        return user;

    }
}
