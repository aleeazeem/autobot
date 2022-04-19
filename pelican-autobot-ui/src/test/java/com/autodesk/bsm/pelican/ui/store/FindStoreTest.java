package com.autodesk.bsm.pelican.ui.store;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.stores.FindStoresPage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreSearchResultsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class FindStoreTest extends SeleniumWebdriver {
    private static FindStoresPage findStoresPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findStoresPage = adminToolPage.getPage(FindStoresPage.class);
    }

    /**
     * Validate correct headers in search result
     *
     * @Result: Correct headers including "Status"
     */
    @Test
    public void verifyStoreSearchPageColumnHeaders() {
        final StoreSearchResultsPage searchResultsPage = findStoresPage.findByIdDefaultSearch();
        final List<String> expHeaders = new ArrayList<>();

        expHeaders.add("ID");
        expHeaders.add("External Key");
        expHeaders.add("Type");
        expHeaders.add("Name");
        expHeaders.add("Status");
        expHeaders.add("Application Family");

        AssertCollector.assertThat("Incorrect headers", searchResultsPage.getGrid().getColumnHeaders(),
            equalTo(expHeaders), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
