package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.RoleClient;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RolesHelper {

    private RoleClient roleResource;
    private EnvironmentVariables environmentVariables;
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesHelper.class.getSimpleName());

    public RolesHelper(final EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        roleResource = new RoleClient(environmentVariables);
    }

    /**
     * This is a temp method which returns test Actors from DB, Actual implementation is to have a API to create Actor.
     *
     * @return the testactor API
     */
    public String getActor() {

        final String sqlQuery =
            "select ID from named_party where type = '1' and appf_id='2001' and NAME like '$TestActor%'";
        final String tableColumn = "ID";
        final List<String> resultList = DbUtils.selectQuery(sqlQuery, tableColumn, environmentVariables);
        if (resultList.size() == 0) {
            return null;
        } else {
            return resultList.get(0);
        }

    }

    /**
     * This method assigns Roles to User
     *
     * @param userParams Map
     * @return user
     */
    public Map<String, String> assignUserRole(final Map<String, String> userParams) {

        userParams.put("requesttype", "user");
        userParams.put("id", userParams.get("userID"));
        return roleResource.assignRole(userParams);
    }

    /**
     * This method assigns Roles to User
     *
     * @param userParams Map
     * @return user
     */
    public Map<String, String> unassignUserRole(final Map<String, String> userParams) {

        userParams.put("requesttype", "user");
        userParams.put("id", userParams.get("userID"));
        return roleResource.unassignRole(userParams);
    }

    /**
     * This method assigns Roles to User
     *
     * @param userParams Map
     * @return user
     */
    public Map<String, String> assignActorRole(final Map<String, String> userParams) {

        userParams.put("requesttype", "actor");
        userParams.put("id", userParams.get("actorID"));
        return roleResource.assignRole(userParams);
    }

    /**
     * This method assigns Roles to User
     *
     * @param userParams (Map)
     * @return user
     */
    public Map<String, String> unassignActorRole(final Map<String, String> userParams) {

        userParams.put("requesttype", "actor");
        userParams.put("id", userParams.get("actorID"));
        return roleResource.unassignRole(userParams);
    }

    /**
     * Method to return list of all roles except EBSO user
     *
     * @return List<String>
     */
    public List<String> getNonEbsoRoleList() {
        final List<String> nonEbsoRoleList = getAllRolesList();
        nonEbsoRoleList.remove(Role.EBSO.getValue());
        return nonEbsoRoleList;
    }

    /**
     * Method to return list of all roles except GCSO user
     *
     * @return List<String> list og all roles except gcso
     */
    public List<String> getNonGcsoRoleList() {
        final List<String> nonGcsoRoleList = new ArrayList<>();
        nonGcsoRoleList.addAll(getNonEbsoRoleList());
        nonGcsoRoleList.add(Role.EBSO.getValue());
        nonGcsoRoleList.remove(Role.ATC_ADMIN.getValue());
        nonGcsoRoleList.remove(Role.STORE_MANAGER.getValue());
        return nonGcsoRoleList;
    }

    /**
     * Method to return list of all roles except offering manager user
     *
     * @return List<String> list og all roles except offering manager
     */
    public List<String> getNonOfferingManagerRoleList() {
        final List<String> nonOfferingManagerRoleList = new ArrayList<>();
        nonOfferingManagerRoleList.addAll(getNonEbsoRoleList());
        nonOfferingManagerRoleList.add(Role.EBSO.getValue());
        nonOfferingManagerRoleList.remove(Role.OFFERING_MANAGER.getValue());

        return nonOfferingManagerRoleList;
    }

    /**
     * Method to return list of read only
     *
     * @return List<String> list of read only role
     */
    public List<String> getReadOnlyRoleList() {
        final List<String> readOnlyRoleList = new ArrayList<>();
        readOnlyRoleList.add(Role.READ_ONLY.getValue());

        return readOnlyRoleList;
    }

    /**
     * Method to return list of atc admin only
     *
     * @return List<String> list of atc admin only role
     */
    public List<String> getATCAdminOnlyRoleList() {
        final List<String> atcAdminOnlyRoleList = new ArrayList<>();
        atcAdminOnlyRoleList.add(Role.ATC_ADMIN.getValue());

        return atcAdminOnlyRoleList;
    }

    /**
     * Method to return list of qa only
     *
     * @return List<String> list of qa only role
     */
    public List<String> getQAOnlyRoleList() {
        final List<String> qaAdminOnlyRoleList = new ArrayList<>();
        qaAdminOnlyRoleList.add(Role.QA_ONLY.getValue());

        return qaAdminOnlyRoleList;
    }

    /**
     * Method to return list of GCSO role
     *
     * @return List<String> list of gcso role
     */
    public List<String> getGCSOOnlyRoleList() {
        final List<String> gcsoOnlyRoleList = new ArrayList<>();
        gcsoOnlyRoleList.add(Role.GCSO.getValue());

        return gcsoOnlyRoleList;
    }

    /**
     * Method to return list of GDPR role
     *
     * @return List<String> list of GDPR role
     */
    public List<String> getGDPROnlyRoleList() {
        final List<String> gdprOnlyRoleList = new ArrayList<>();
        gdprOnlyRoleList.add(Role.GDPR_ROLE.getValue());

        return gdprOnlyRoleList;
    }

    /**
     * Method to return list of ebso only
     *
     * @return List<String> list of ebso only role
     */
    public List<String> getEBSOOnlyRoleList() {
        final List<String> ebsoOnlyRoleList = new ArrayList<>();
        ebsoOnlyRoleList.add(Role.EBSO.getValue());

        return ebsoOnlyRoleList;
    }

    /**
     * Method to return list of offering manager role only
     *
     * @return List<String> list of offering manager only role
     */
    public List<String> getOfferingManagerOnlyRoleList() {
        final List<String> offeringManagerOnlyRoleList = new ArrayList<>();
        offeringManagerOnlyRoleList.add(Role.OFFERING_MANAGER.getValue());

        return offeringManagerOnlyRoleList;
    }

    /**
     * Method to unassign all roles from the user id and app family passed in userparams
     */
    public void unassignAllUserRole(final HashMap<String, String> userParams) {
        final HashMap<String, String> roleListFromDb = DbUtils.getAllRoles(environmentVariables);
        final JSONObject request = new JSONObject();
        final JSONArray dataArray = new JSONArray();
        final Map<String, String> requestParams = new HashMap<>();

        requestParams.put(UserParameter.APPLICATION_FAMILY.getName(),
            userParams.get(UserParameter.APPLICATION_FAMILY.getName()));
        requestParams.put(UserParameter.USER_ID.getName(), userParams.get(UserParameter.USER_ID.getName()));

        // unassigning all existing roles
        for (final String roleId : roleListFromDb.keySet()) {
            final JSONObject dataObject = new JSONObject();
            dataObject.put("type", "roles");
            dataObject.put("id", roleListFromDb.get(roleId));
            dataArray.add(dataObject);
        }
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());
        unassignUserRole(requestParams);
        LOGGER.info("Unassigned all roles");
    }

    public List<String> getAllRolesList() {
        final List<String> allRolesList = new ArrayList<>();
        // Add all roles to allRolesList
        allRolesList.add(Role.ADMIN.getValue());
        allRolesList.add(Role.APPLICATION_MANAGER.getValue());
        allRolesList.add(Role.ATC_ADMIN.getValue());
        allRolesList.add(Role.AUTHENTICATOR.getValue());
        allRolesList.add(Role.BANKING_ADMIN.getValue());
        allRolesList.add(Role.BIC_RELEASES_ADMIN.getValue());
        allRolesList.add(Role.COMMUNITY_ADMIN.getValue());
        allRolesList.add(Role.CSAT_PROMOTION_REPRESENTATIVE.getValue());
        allRolesList.add(Role.OFFERING_MANAGER.getValue());
        allRolesList.add(Role.PAYMENT_GATEWAY_AGENT.getValue());
        allRolesList.add(Role.PAYMENT_GATEWAY_MANAGER.getValue());
        allRolesList.add(Role.PAYMENT_SETTLEMENT_AGENT.getValue());
        allRolesList.add(Role.PAYMENT_SETTLEMENT_AGENT.getValue());
        allRolesList.add(Role.PROMOTION_MANAGER.getValue());
        allRolesList.add(Role.QA_ONLY.getValue());
        allRolesList.add(Role.READ_ONLY.getValue());
        allRolesList.add(Role.ROLE_GRANTER.getValue());
        allRolesList.add(Role.SECURITY_MANAGER.getValue());
        allRolesList.add(Role.STORE_MANAGER.getValue());
        allRolesList.add(Role.SUB_RENEWAL_ROLE.getValue());
        allRolesList.add(Role.SUBSCRIPTIONMANAGER.getValue());
        allRolesList.add(Role.USER_ADMIN.getValue());
        allRolesList.add(Role.EBSO.getValue());
        allRolesList.add(Role.GCSO_EDIT_SUBSCRIPTION.getValue());

        return allRolesList;
    }

}
