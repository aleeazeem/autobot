package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FindUserTest extends SeleniumWebdriver {

    private FindUserPage findUserPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findUserPage = adminToolPage.getPage(FindUserPage.class);
    }

    /**
     * Verify user can be find by id in AdminTool
     *
     * @result Detail Page of searched User.
     */
    @Test
    public void findUserById() {

        final UserDetailsPage userDetails = findUserPage.getById(getUser().getId());
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        AssertCollector.assertThat("User is not found by ID", userDetails.getId(), equalTo(getUser().getId()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify user can be find by username in AdminTool
     *
     * @result Detail Page of searched User.
     */
    @Test
    public void findUserByUserName() {

        final UserDetailsPage userDetails = findUserPage.getByName(getUser().getName());

        AssertCollector.assertThat("User is not found by user name", userDetails.getUserName(),
            equalTo(getUser().getName()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify user can be find by externalKey in AdminTool
     *
     * @result Detail Page of searched User.
     */
    @Test
    public void findUserByExternalKey() {

        final UserDetailsPage userDetails =
            findUserPage.getByExternalKey(getEnvironmentVariables().getUserExternalKey());
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        AssertCollector.assertTrue("User Doesn't Exist", userDetails.isTitlePagePresent(), assertionErrorList);
        AssertCollector.assertThat("User is not found by External Key", userDetails.getValueByField("External Key"),
            equalTo(getEnvironmentVariables().getUserExternalKey()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
