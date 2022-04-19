package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpClient;
import com.autodesk.bsm.pelican.util.HttpResponseListener;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Rest service: User endpoint
 *
 * @author Sumant Manda
 */
public class RoleClient {

    private static final String USER_URLPARAM = "users";
    private static final String ACTOR_URLPARAM = "actors";
    private static final String ROLE_URLPARAM = "roles";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleClient.class.getSimpleName());

    public RoleClient(final EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, environmentVariables.getAppFamily());
    }

    /**
     * Assign Role using UserParameters passed in HashMap
     *
     * @param userMap Map
     */
    public Map<String, String> assignRole(final Map<String, String> userMap) {

        LOGGER.info("Assign Role for " + userMap.get("requesttype") + " : " + userMap.get("id"));
        LOGGER.info("Header Info: PartnerID = " + authInfo.getPartnerId());

        if (userMap.containsKey("changeAppFamilyID")) {
            LOGGER.info("Modified the Authentication headers to :" + userMap.get("applicationFamily"));
            authInfo = new AuthenticationInfo(authInfo.getPartnerId(), userMap.get("applicationFamily"),
                authInfo.getSecretKey());
        }

        final CloseableHttpResponse response = client.doPost(getRoleURL(userMap.get("requesttype"), userMap.get("id")),
            userMap.get("body"), authInfo, PelicanConstants.CONTENT_TYPE, null);

        if (userMap.containsKey("changeAppFamilyID")) {
            LOGGER.info("Revert Authentication headers back to environment values");
            authInfo = new AuthenticationInfo(environmentVariables, environmentVariables.getAppFamily());
        }

        // sleep to reflect role assignment
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return parseResponse(response);
    }

    /**
     * Unassign Role using UserParameters passed in HashMap
     *
     * @param userMap (Map)
     */
    public Map<String, String> unassignRole(final Map<String, String> userMap) {

        final HttpResponseListener listener = null;
        final Map<String, String> roleResponse = new HashMap<>();

        LOGGER.info("Unassign Role for " + userMap.get("requesttype") + " : " + userMap.get("id"));
        LOGGER.info("Header Info: PartnerID = " + authInfo.getPartnerId());

        final AuthenticationInfo customAuthInfo =
            new AuthenticationInfo(environmentVariables, environmentVariables.getAppFamily());
        if (userMap.containsKey("changeAppFamilyID")) {
            customAuthInfo.setAppFamilyId(userMap.get("applicationFamily"));
        }

        int response = 0;
        try {
            response = HttpClient.doDelete(getRoleURL(userMap.get("requesttype"), userMap.get("id")),
                PelicanConstants.CONTENT_TYPE, customAuthInfo, userMap.get("body"), listener);
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        roleResponse.put("Responsecode", Integer.toString(response));
        return roleResponse;
    }

    private String getRoleURL(final String type, final String id) {

        if ("user".equalsIgnoreCase(type)) {
            return environmentVariables.getV2ApiUrl() + "/" + USER_URLPARAM + "/" + id + "/" + ROLE_URLPARAM;
        } else {
            return environmentVariables.getV2ApiUrl() + "/" + ACTOR_URLPARAM + "/" + id + "/" + ROLE_URLPARAM;
        }
    }

    private Map<String, String> parseResponse(final CloseableHttpResponse response) {

        final Map<String, String> roleResponse = new HashMap<>();
        roleResponse.put("Responsecode", Integer.toString(response.getStatusLine().getStatusCode()));

        if (response.getStatusLine().getStatusCode() >= 400) {
            final HttpEntity entity = response.getEntity();
            String responseString;
            try {
                responseString = EntityUtils.toString(entity, "UTF-8");
                LOGGER.info("Error message from Response Body:" + responseString);
                roleResponse.put("responsebody", responseString);
                // roleResponse.put("Responsecode", "400");
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        } else {
            roleResponse.put("responsebody", "");
        }

        return roleResponse;

    }

    private Map<String, String> parseResponse(final org.apache.http.HttpResponse response) {

        final Map<String, String> roleResponse = new HashMap<>();
        roleResponse.put("Responsecode", Integer.toString(response.getStatusLine().getStatusCode()));

        if (response.getStatusLine().getStatusCode() >= 400) {
            final HttpEntity entity = response.getEntity();
            String responseString;

            try {
                responseString = EntityUtils.toString(entity, "UTF-8");
                LOGGER.info("Error message from Response Body:" + responseString);
                roleResponse.put("responsebody", responseString);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        } else {
            roleResponse.put("responsebody", "");
        }

        return roleResponse;

    }
}
