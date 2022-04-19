package com.autodesk.bsm.pelican.ui.downloads.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.FinanceReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FinanceReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to cover scenarios for Download Finance Report by Oxygen Id.
 */
public class DownloadFinanceReportByOxygenIdTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FinanceReportPage financeReportPage;
    private String file;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DownloadFinanceReportByOxygenIdTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        financeReportPage = adminToolPage.getPage(FinanceReportPage.class);
        file = XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.FINANCE_REPORT_FILE_NAME;
    }

    /**
     * Generate Finance Report by Oxygen Id for 15 oxygen Id.
     *
     * @throws IOException
     */
    @Test
    public void testSuccessMultipleOxygenIdsSupported() throws IOException {

        final int noOfOxygenId = 8;
        final List<String> oxygenIdList = new ArrayList<>();
        final List<String> purchaseOrderList = new ArrayList<>();
        final List<String> subscriptionList = new ArrayList<>();
        for (int i = 0; i < noOfOxygenId; i++) {

            final BuyerUser buyerUser = getBuyer();
            final String oxygenIdForBuyer = buyerUser.getExternalKey();
            // Create Purchase order with Authorized Status for Buyer
            final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
                Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);

            final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems()
                .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

            oxygenIdList.add(oxygenIdForBuyer);
            purchaseOrderList.add(purchaseOrderId);
            subscriptionList.add(subscriptionId);
        }

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.FINANCE_REPORT_FILE_NAME);

        financeReportPage.generateFinanceReportByOxygenId(oxygenIdList, PelicanConstants.DOWNLOAD);
        financeReportPage.clickOnGenerateReport(2);

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(file);
        commonAssertionsOnFinanceReport(fileData, purchaseOrderList, subscriptionList, oxygenIdList);
        AssertCollector.assertThat("Total Number Of Records are not matching", fileData.length - 1, is(noOfOxygenId),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Generate Finance Report by Oxygen Id for more than 100 oxygen Ids throw an error.
     *
     */
    @Test
    public void testErrorGenerateFinanceReportForMoreThan100OxygenIds() {

        final int noOfOxygenId = 105;
        final List<String> externalKeyList = new ArrayList<>();
        // Select query to get more than 100 user exteranl key.
        final List<Map<String, String>> oxygenIdList = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_SELECT_UNIQUE_USER_EXTERNAL_KEY, noOfOxygenId),
            getEnvironmentVariables());

        for (int i = 0; i < noOfOxygenId; i++) {
            externalKeyList.add(oxygenIdList.get(i).get("USER_EXTERNAL_KEY"));
        }

        financeReportPage.generateFinanceReportByOxygenId(externalKeyList, PelicanConstants.DOWNLOAD);
        financeReportPage.clickOnGenerateReportWithError(2);
        final String errorMessage = financeReportPage.getErrorText();
        AssertCollector.assertThat("Incorrect Error Message ", errorMessage,
            equalTo("Maximum of 100 user external keys are allowed."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify no time frame limit on generating Finance Report.
     *
     * @throws IOException
     */
    @Test
    public void testFinanceReportByOxygenIdForOrdersPlacedInPast() throws IOException {

        final BuyerUser buyerUser1 = getBuyer();
        final String oxygenIdForBuyer1 = buyerUser1.getExternalKey();
        // Submit a PO and update Finance Report for Order Date in past.
        final String purchaseOrderId1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser1, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId1);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId1);
        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId1).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final String getDateInPast = DateTimeUtils.getNowMinusDays(PelicanConstants.AUDIT_LOG_DATE_FORMAT, 120);

        // Updating DB for Finance Report table with above created date.
        DbUtils.updateQuery(
            String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_FINANCE_REPORT_TABLE, getDateInPast, purchaseOrderId1),
            getEnvironmentVariables());

        final BuyerUser buyerUser2 = getBuyer();
        final String oxygenIdForBuyer2 = buyerUser2.getExternalKey();
        // Create Purchase order with Authorized Status for Buyer
        final String purchaseOrderId2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser2, 1)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId2);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId2);
        LOGGER.info("Purchase Order Id : " + purchaseOrderId1 + "Oxygen Id :" + oxygenIdForBuyer2);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId2).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> oxygenIdList = Arrays.asList(oxygenIdForBuyer1, oxygenIdForBuyer2);
        final List<String> purchaseOrderList = Arrays.asList(purchaseOrderId1, purchaseOrderId2);
        final List<String> subscriptionList = Arrays.asList(subscriptionId1, subscriptionId2);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.FINANCE_REPORT_FILE_NAME);

        financeReportPage.generateFinanceReportByOxygenId(oxygenIdList, PelicanConstants.DOWNLOAD);
        financeReportPage.clickOnGenerateReport(2);

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(file);
        commonAssertionsOnFinanceReport(fileData, purchaseOrderList, subscriptionList, oxygenIdList);
        AssertCollector.assertThat("Total Number Of Records are not matching", fileData.length - 1, is(2),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Common assertion on Fiance Report Data.
     */
    private void commonAssertionsOnFinanceReport(final String[][] fileData, final List<String> purchaseOrderList,
        final List<String> subscriptionList, final List<String> oxygenIdList) {

        for (int i = 1; i < fileData.length; i++) {
            final int index = getPurchaseOrderIndex(fileData[i][0], purchaseOrderList);
            AssertCollector.assertThat("Incorrect Purchase Order Id ",
                fileData[i][FinanceReportHeaderConstants.PURCHASE_ORDER_ID], equalTo(purchaseOrderList.get(index)),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Id ",
                fileData[i][FinanceReportHeaderConstants.SUBSCRIPTION_ID], equalTo(subscriptionList.get(index)),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Oxygen Id ", fileData[i][FinanceReportHeaderConstants.O2_ID],
                equalTo(oxygenIdList.get(index)), assertionErrorList);
        }
    }

    /**
     * Method to return index number of Purchase Order.
     *
     * @param purchaseOrderId
     * @param purchaseOrderList
     * @return
     */
    private int getPurchaseOrderIndex(final String purchaseOrderId, final List<String> purchaseOrderList) {
        int index = 0;
        for (int i = 0; i < purchaseOrderList.size(); i++) {
            if (purchaseOrderId.equals(purchaseOrderList.get(i))) {
                index = i;
            }
        }
        return index;
    }

    /**
     * Method to add user and buyer user.
     *
     * @return BuyerUser
     */
    private static BuyerUser getBuyer() {
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(),
            "OxygenId_" + RandomStringUtils.randomAlphanumeric(8));

        // Create User with Params
        final UserUtils userUtils = new UserUtils();

        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // Setup Buyer User for User
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(user.getExternalKey());

        return buyerUser;
    }
}
