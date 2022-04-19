package com.autodesk.bsm.pelican.ui.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.FindBasicOfferingPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests Find Basic Offering Scenarios in Admin Tool -Find By Id -Find By External Key -Advanced Find
 *
 * @author Shweta Hegde
 */
public class FindBasicOfferingTest extends SeleniumWebdriver {

    private String basicOfferingId;
    private String basicOfferingExternalKey;
    private String cloudOfferingId;
    private String cloudOfferingExternalKey;
    private FindBasicOfferingPage findBasicOfferingPage;
    private BasicOfferingDetailPage basicOfferingDetailPage;
    private String productLineNameOfBasicOffering;
    private String productLineNameOfCloudOffering;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        // Create a Perpetual Basic Offering
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings basicOfferingsPerpetual = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOfferingId = basicOfferingsPerpetual.getOfferings().get(0).getId();
        basicOfferingExternalKey = basicOfferingsPerpetual.getOfferings().get(0).getExternalKey();
        productLineNameOfBasicOffering = basicOfferingsPerpetual.getOfferings().get(0).getProductLine();

        // Create a currency basic offering
        final Offerings cloudOfferings = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.CURRENCY, MediaType.USB, Status.ACTIVE, UsageType.COM, null);
        cloudOfferingId = cloudOfferings.getOfferings().get(0).getId();
        cloudOfferingExternalKey = cloudOfferings.getOfferings().get(0).getExternalKey();
        productLineNameOfCloudOffering = cloudOfferings.getOfferings().get(0).getProductLine();

        initializeDriver(getEnvironmentVariables());
        // actions = new PelicanActions(getDriver());

        // Initiating the environment and the appFamily set to AUTO
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
    }

    /**
     * This method tests Find Basic Offering By Id
     */
    @Test(dataProvider = "basicOfferingInfo")
    public void testFindBasicOfferingById(final String basicOfferingId, final String nameOrExternalKey,
        final OfferingType offeringType, final MediaType mediaType, final Status status, final UsageType usageType,
        final String productLineName) {

        // Find Basic Offering by id
        basicOfferingDetailPage = findBasicOfferingPage.findBasicOfferingById(basicOfferingId);

        // Assertions
        commonAssertions(basicOfferingDetailPage, basicOfferingId, nameOrExternalKey, offeringType, mediaType, status,
            usageType, productLineName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Find Basic Offering with external key
     */
    @Test(dataProvider = "basicOfferingInfo")
    public void testFindBasicOfferingByExternalKey(final String basicOfferingId, final String nameOrExternalKey,
        final OfferingType offeringType, final MediaType mediaType, final Status status, final UsageType usageType,
        final String productLineName) {

        // Find Basic Offering By external key
        basicOfferingDetailPage = findBasicOfferingPage.findBasicOfferingByExternalKey(nameOrExternalKey);

        commonAssertions(basicOfferingDetailPage, basicOfferingId, nameOrExternalKey, offeringType, mediaType, status,
            usageType, productLineName);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test(dataProvider = "basicOfferingInfo")
    public void testFindBasicOfferingByAdvancedFind(final String basicOfferingId, final String nameOrExternalKey,
        final OfferingType offeringType, final MediaType mediaType, final Status status, final UsageType usageType,
        final String productLineName) {

        // Advanced find
        final String productLineFormatted = productLineName + " (" + productLineName + ")";
        basicOfferingDetailPage = findBasicOfferingPage.selectResultRowWithAdvancedFind(1, productLineFormatted,
            offeringType.getDisplayName(), null, false, true, false);

        AssertCollector.assertThat("Incorrect offering type", basicOfferingDetailPage.getOfferingType(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", basicOfferingDetailPage.getProductLine(),
            equalTo(getProductLineExternalKeyRevit()), assertionErrorList);
        AssertCollector.assertThat("Incorrect name", basicOfferingDetailPage.getName(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key", basicOfferingDetailPage.getExternalKey(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", basicOfferingDetailPage.getProductLine(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect status", basicOfferingDetailPage.getStatus(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Id should not be null", basicOfferingDetailPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type", basicOfferingDetailPage.getUsageType(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Audit Trail links to incorrect entity", basicOfferingDetailPage.getAuditTrailLink(),
            notNullValue(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider to find testcases, All these data are used for assertions
     *
     * @return basic offering info
     */
    @DataProvider(name = "basicOfferingInfo")
    private Object[][] provideBasicOfferingInfo() {
        return new Object[][] {
                { basicOfferingId, basicOfferingExternalKey, OfferingType.PERPETUAL, MediaType.ELECTRONIC_DOWNLOAD,
                        Status.ACTIVE, UsageType.COM, productLineNameOfBasicOffering },
                { cloudOfferingId, cloudOfferingExternalKey, OfferingType.CURRENCY, null, Status.ACTIVE, UsageType.COM,
                        productLineNameOfCloudOffering } };
    }

    /**
     * Common Assertions for all finds
     */
    private void commonAssertions(final BasicOfferingDetailPage basicOfferingDetailPage, final String basicOfferingId,
        final String nameOrExternalKey, final OfferingType offeringType, final MediaType mediaType, final Status status,
        final UsageType usageType, final String productLineName) {

        AssertCollector.assertThat("Incorrect offering type", basicOfferingDetailPage.getOfferingType(),
            equalTo(offeringType.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", basicOfferingDetailPage.getProductLine(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect name", basicOfferingDetailPage.getName(), equalTo(nameOrExternalKey),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key", basicOfferingDetailPage.getExternalKey(),
            equalTo(nameOrExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", basicOfferingDetailPage.getProductLine(),
            equalTo(productLineName), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", basicOfferingDetailPage.getStatus(),
            equalTo(status.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Id should not be null", basicOfferingDetailPage.getId(), equalTo(basicOfferingId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type", basicOfferingDetailPage.getUsageType(),
            equalTo(usageType.toString()), assertionErrorList);
        AssertCollector.assertThat("Audit Trail links to incorrect entity", basicOfferingDetailPage.getAuditTrailLink(),
            equalTo(basicOfferingDetailPage.generateAuditTrailLink(basicOfferingId)), assertionErrorList);

        if (offeringType == OfferingType.PERPETUAL) {
            AssertCollector.assertThat("Incorrect media type", basicOfferingDetailPage.getMediaType(),
                equalTo(mediaType.getValue()), assertionErrorList);
        }
    }
}
