package com.autodesk.bsm.pelican.api.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test : Get User By External Key
 *
 * @author Shweta Hegde
 */
public class FindUserByExternalKeyTest extends BaseTestData {

    private PelicanPlatform resource;
    private HttpError httpError;
    private Object apiResponse;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * This test method tests "Get user by External Key" API. Validation is done for id, name, external key and email
     * address
     */
    @Test
    public void testGetUserByValidExternalKey() {
        // get user by external key
        apiResponse = resource.user().getUserByExternalKey(getUserExternalKey());
        // check whether apiResponse is an instance of httperror
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(User.class), assertionErrorList);
        } else {
            // type casting apiResponse to User
            final User existingUser = (User) apiResponse;
            AssertCollector.assertThat("Incorrect User Id", existingUser.getId(), equalTo(getUser().getId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect User Name", existingUser.getName(), equalTo(getUser().getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect User external key", existingUser.getExternalKey(),
                equalTo(getUserExternalKey()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests "Get user by External key" API for invalid external key,
     */
    @Test
    public void testGetUserByInvalidExternalKey() {
        final String invalidExternalKey = "abcdefghijklmnopqrst";
        // get user by external key
        apiResponse = resource.user().getUserByExternalKey(invalidExternalKey);
        // check whether apiResponse is an instance of httperror
        httpError = (HttpError) apiResponse;
        AssertCollector.assertThat("Incorrect entity type", httpError, instanceOf(HttpError.class), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(404), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("User not found (externalKey: " + invalidExternalKey + ")"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests "Get user by External key" API, Validation is done for id, name, external key and email
     * address
     */
    @Test
    public void testGetUserByExternalKeyCreatedByNoExternalKey() {
        // create a new user without external key
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final User user = new UserUtils().createPelicanUser(userParams, getEnvironmentVariables());
        // get user by external key
        apiResponse = resource.user().getUserByExternalKey(user.getExternalKey());
        // check whether apiResponse is an instance of httperror
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(User.class), assertionErrorList);
        } else {
            // type casting apiResponse to User
            final User newUser = (User) apiResponse;
            AssertCollector.assertThat("Incorrect User Id", newUser.getId(), equalTo(user.getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect User Name", newUser.getName(), equalTo(user.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect User external key", newUser.getExternalKey(),
                equalTo(user.getExternalKey()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
