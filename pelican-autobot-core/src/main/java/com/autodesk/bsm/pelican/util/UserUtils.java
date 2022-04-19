package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.user.GDPRRequest;
import com.autodesk.bsm.pelican.api.pojos.user.GDPRRequestPayload;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.api.pojos.user.UserInfo;
import com.autodesk.bsm.pelican.api.pojos.user.WebHookInfo;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddPasswordForUserPage;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserUtils {

    private JSONObject request = new JSONObject();
    private PelicanPlatform resource;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUtils.class.getSimpleName());

    /**
     * This method checks whether user is existing, if exists sends the existing user else creates a new user and sends
     * new user for AUTO Family
     *
     * @param userParams Map
     * @return user
     */
    public User createPelicanUser(final Map<String, String> userParams,
        final EnvironmentVariables environmentVariables) {

        if (userParams.get(UserParameter.APPLICATION_FAMILY.getName()).equals(ApplicationFamily.AUTO.toString())) {
            resource = new PelicanClient(environmentVariables, environmentVariables.getAppFamily()).platform();
        } else {
            resource = new PelicanClient(environmentVariables, environmentVariables.getOtherAppFamily()).platform();
        }

        if (resource.user()
            .getUserByExternalKey(userParams.get(UserParameter.EXTERNAL_KEY.getName())) instanceof HttpError) {
            if (!userParams.containsKey(UserParameter.NAME.getName())) {
                userParams.put(UserParameter.NAME.getName(), userParams.get(UserParameter.EXTERNAL_KEY.getName()));
            }
            return resource.user().addUser(userParams);
        } else {
            return resource.user().getUserByExternalKey(userParams.get(UserParameter.EXTERNAL_KEY.getName()));
        }
    }

    /**
     * Method to create a new user with given user and log in to admin tool with new user. Also assign Roles
     *
     * @param userParams - provide applicationFamily, externalKey and password
     * @param roleList - provide a list of roles to be added to user
     * @return User
     */
    public User createAssignRoleAndLoginUser(final HashMap<String, String> userParams, final List<String> roleList,
        final AdminToolPage adminToolPage, final EnvironmentVariables environmentVariables) {
        final User user = createAssignRole(userParams, roleList, adminToolPage, environmentVariables);

        // log out existing user
        adminToolPage.logout();

        // login in admin tool with new created user
        LOGGER.info("Logging in with new user");
        adminToolPage.login(user.getName(), userParams.get(UserParameter.PASSWORD.getName()));
        return user;
    }

    /**
     * Method to create a new Pelican user and assign role
     *
     * @param userParams
     * @param roleList
     * @param adminToolPage
     * @param environmentVariables
     */
    public User createAssignRole(final HashMap<String, String> userParams, final List<String> roleList,
        final AdminToolPage adminToolPage, final EnvironmentVariables environmentVariables) {
        resource = new PelicanClient(environmentVariables).platform();
        final String pelicanUserPassword = environmentVariables.getPassword();
        final String newUserPassword = userParams.get(UserParameter.PASSWORD.getName());
        final RolesHelper rolesHelper = new RolesHelper(environmentVariables);
        final JSONArray dataArray = new JSONArray();
        final HashMap<String, String> requestParams = new HashMap<>();
        final HashMap<String, String> roleListFromDb = DbUtils.getAllRoles(environmentVariables);

        final User user = createPelicanUser(userParams, environmentVariables);
        requestParams.put(UserParameter.APPLICATION_FAMILY.getName(),
            userParams.get(UserParameter.APPLICATION_FAMILY.getName()));
        requestParams.put(UserParameter.EXTERNAL_KEY.getName(), userParams.get(UserParameter.EXTERNAL_KEY.getName()));
        requestParams.put(UserParameter.USER_ID.getName(), user.getId());

        // assign new Password to created user
        final AddPasswordForUserPage userPasswordPage = adminToolPage.getPage(AddPasswordForUserPage.class);

        userPasswordPage.getCredentialDetail(user.getId(), pelicanUserPassword, newUserPassword);

        // unassign all existing roles
        LOGGER.info("Unassigning all existing roles from user");
        rolesHelper.unassignAllUserRole(requestParams);

        // creating data array for role assignment
        for (final String role : roleList) {
            final JSONObject dataObject = new JSONObject();
            dataObject.clear();
            if (roleListFromDb.get(role) != null) {
                dataObject.put("type", "roles");
                dataObject.put("id", roleListFromDb.get(role));
                dataArray.add(dataObject);
            } else {
                LOGGER.error("Role not found in database: " + role);
            }
        }
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        // assigning roles
        rolesHelper.assignUserRole(requestParams);
        LOGGER.info("Role assignment done");

        return user;
    }

    /**
     * Helper Method to handle GDPR User Request for event type and oxygen id.
     *
     * @param oxygenId
     * @param eventType
     * @param taskId
     * @param environmentVariables
     * @throws IOException
     * @throws ParseException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public <T extends PelicanPojo> T gdprUserRequest(final String oxygenId, final String eventType, final String taskId,
        final EnvironmentVariables environmentVariables) throws ParseException, IOException {
        resource = new PelicanClient(environmentVariables).platform();
        final GDPRRequest gdprRequest = new GDPRRequest();
        final WebHookInfo hookInfo = new WebHookInfo();
        final GDPRRequestPayload requestPayload = new GDPRRequestPayload();
        if (eventType != null) {
            hookInfo.setEventType(eventType);
        }
        final UserInfo userInfo = new UserInfo();
        if (oxygenId != null) {
            userInfo.setId(oxygenId);
        }
        requestPayload.setUserInfo(userInfo);
        requestPayload.setVersion("1");
        if (taskId != null) {
            requestPayload.setTaskId(taskId);
        }
        gdprRequest.setHook(hookInfo);
        gdprRequest.setPayload(requestPayload);
        return resource.user().gdprUser(gdprRequest, environmentVariables);
    }

    /**
     * Method to create buyerUser to submit purchase order. If userExternalKey is null, user will be created with
     * randomly generated external key.
     *
     * @param environmentVariables
     * @param userExternalKey
     * @param appFamily
     * @return BuyerUser
     */
    public BuyerUser createBuyerUser(final EnvironmentVariables environmentVariables, final String userExternalKey,
        final String appFamily) {
        resource = new PelicanClient(environmentVariables).platform();
        String externalKey;
        if (userExternalKey == null) {
            externalKey = "User" + RandomStringUtils.randomNumeric(4) + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        } else {
            externalKey = userExternalKey;
        }

        // Set the request
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), externalKey);
        userRequestParam.put(UserParameter.APPLICATION_FAMILY.getName(), appFamily);

        // Add user
        final User user = createPelicanUser(userRequestParam, environmentVariables);

        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User external key: " + user.getExternalKey());

        // build buyerUser object
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(BaseTestData.getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(externalKey);
        buyerUser.setName(user.getName());

        return buyerUser;
    }
}
