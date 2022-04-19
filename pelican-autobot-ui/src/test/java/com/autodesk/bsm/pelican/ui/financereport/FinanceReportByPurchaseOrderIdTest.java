package com.autodesk.bsm.pelican.ui.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.FinanceReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FinanceReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

/**
 * This test class if for View Of Finance Report By Purchase Order Id
 *
 * @author Shweta Hegde
 */
public class FinanceReportByPurchaseOrderIdTest extends SeleniumWebdriver {

    private FinanceReportPage financeReportPage;
    private PurchaseOrderUtils purchaseOrderUtils;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        financeReportPage = adminToolPage.getPage(FinanceReportPage.class);

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * Error should be returned if no purchase order id is provided in finding finance report
     */
    @Test
    public void testErrorFinanceReportByIdWithoutPurchaseOrderId() {

        financeReportPage.findFinanceReportById("", PelicanConstants.VIEW, 1);
        financeReportPage.clickOnGenerateReport(1);
        financeReportPage = financeReportPage.clickOnGenerateReportWithError(1);

        AssertCollector.assertThat("Incorrect error message", financeReportPage.getErrorText(),
            equalTo("Purchase Order Id must be entered."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find Finance Report By Purchase Order Id VIEW
     */
    @Test
    public void testSuccessViewFinanceReportByIdWithPurchaseOrderId() {

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 3);

        final UserUtils userUtils = new UserUtils();
        final BuyerUser buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.REFUND, purchaseOrderId);

        financeReportPage.findFinanceReportById(purchaseOrderId, PelicanConstants.VIEW, 1);
        financeReportPage.clickOnGenerateReport(1);

        final List<String> reportData = financeReportPage.getReportData();
        for (final String data : reportData) {
            final String[] dataArray = data.split(",");

            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrderId),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Subscription Id",
                dataArray[FinanceReportHeaderConstants.SUBSCRIPTION_ID], equalTo(subscriptionId), assertionErrorList);

            AssertCollector.assertThat("Incorrect status", dataArray[FinanceReportHeaderConstants.STATUS],
                isOneOf(PurchaseOrder.OrderCommand.CHARGE.toString(), PurchaseOrder.OrderCommand.REFUND.toString()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect sale type", dataArray[FinanceReportHeaderConstants.SALE_TYPE],
                equalTo(PelicanConstants.NEW_ACQUISITION), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

}
