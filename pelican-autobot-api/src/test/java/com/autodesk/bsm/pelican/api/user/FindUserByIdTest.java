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
 * Test : Get User By Id
 *
 * @author Shweta Hegde
 */
public class FindUserByIdTest extends BaseTestData {

    private PelicanPlatform resource;
    private HttpError httpError;
    private Object apiResponse;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * This test method tests "Get user by Id" API for existing user. Validation is done for id,name, external key
     */
    @Test
    public void testGetUserById() {

        // get user by id
        apiResponse = resource.user().getUserById(getUser().getId());
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
     * This test method tests "Get user by Id" API for invalid id,
     */
    @Test
    public void testGetNonExistingUserById() {
        // get user by id
        apiResponse = resource.user().getUserById("9876543210");
        // check whether apiResponse is an instance of httperror
        httpError = (HttpError) apiResponse;
        AssertCollector.assertThat("Incorrect entity type", httpError, instanceOf(HttpError.class), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", httpError.getStatus(), is(404), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("User not found (id: 9876543210)"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests "Get user by Id" API, for new user Validation is done for id, name, external key and email
     * address
     */
    @Test
    public void testGetUserByIdCreatedByNoExternalKey() {
        // create a new user without external key
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final User user = new UserUtils().createPelicanUser(userParams, getEnvironmentVariables());
        // get user by id
        apiResponse = resource.user().getUserById(user.getId());
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
