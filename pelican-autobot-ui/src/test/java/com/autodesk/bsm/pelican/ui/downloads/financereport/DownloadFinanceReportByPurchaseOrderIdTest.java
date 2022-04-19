package com.autodesk.bsm.pelican.ui.downloads.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FinanceReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * This class is for downloading finance report by Id
 *
 * @author Shweta Hegde
 */
public class DownloadFinanceReportByPurchaseOrderIdTest extends SeleniumWebdriver {

    private FinanceReportPage financeReportPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        financeReportPage = adminToolPage.getPage(FinanceReportPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Find Finance Report By Purchase Order Id
     */
    @Test
    public void testSuccessDownloadFinanceReportByIdWithPurchaseOrderId() throws IOException {

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 3);

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.REFUND, purchaseOrderId);

        final String file = PelicanConstants.FINANCE_REPORT_FILE_NAME;

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), file);

        financeReportPage.findFinanceReportById(purchaseOrderId, PelicanConstants.DOWNLOAD, 1);
        financeReportPage.clickOnGenerateReport(1);

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + file;
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        for (int i = 1; i < fileData.length; i++) {

            AssertCollector.assertThat("Incorrect purchase order id", fileData[i][0], equalTo(purchaseOrderId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Subscription Id", fileData[i][9], equalTo(subscriptionId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect status", fileData[i][21],
                isOneOf(PurchaseOrder.OrderCommand.CHARGE.toString(), PurchaseOrder.OrderCommand.REFUND.toString()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect sale type", fileData[i][20],
                equalTo(PelicanConstants.NEW_ACQUISITION), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Error should be returned if non-numeric purchase order id is provided in finding finance report
     */
    @Test
    public void testErrorFinanceReportByIdInvalidPurchaseOrderId() {

        financeReportPage.findFinanceReportById("7645374bhj754", PelicanConstants.DOWNLOAD, 1);
        financeReportPage.clickOnGenerateReport(1);
        financeReportPage = financeReportPage.clickOnGenerateReportWithError(1);

        AssertCollector.assertThat("Incorrect error message", financeReportPage.getH3ErrorMessage(),
            equalTo(PelicanErrorConstants.NUMBER_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
