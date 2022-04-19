package com.autodesk.bsm.pelican.ui.financereport;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.FinanceReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FinanceReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test class to cover scenario for view finance report by oxygen id.
 */
public class FinanceReportByOxygenIdTest extends SeleniumWebdriver {
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FinanceReportPage financeReportPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        financeReportPage = adminToolPage.getPage(FinanceReportPage.class);
    }

    /**
     * Test Method to verify Finance Report by Oxygen Id.
     *
     */
    @Test
    public void testGenerateFinanceReportByOxygenIdView() {

        final String userExternalKey = "OxygenId_" + RandomStringUtils.randomAlphanumeric(8);

        // Create User with Params.
        final UserUtils userUtils = new UserUtils();
        final BuyerUser buyerUser = userUtils.createBuyerUser(getEnvironmentVariables(), userExternalKey,
            getEnvironmentVariables().getAppFamily());

        // Create Purchase order with Authorized Status for Buyer
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String oxygenId = buyerUser.getExternalKey();
        financeReportPage.generateFinanceReportByOxygenId(Arrays.asList(oxygenId), PelicanConstants.VIEW);
        financeReportPage.clickOnGenerateReport(2);

        final String reportData = financeReportPage.getReportData().get(0);
        final String[] dataArray = reportData.split(",");
        AssertCollector.assertThat("Incorrect Purchase Order ",
            dataArray[FinanceReportHeaderConstants.PURCHASE_ORDER_ID], equalTo(purchaseOrderId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Id ",
            dataArray[FinanceReportHeaderConstants.SUBSCRIPTION_ID], equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Oxygen Id ", dataArray[FinanceReportHeaderConstants.O2_ID],
            equalTo(oxygenId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order State", dataArray[FinanceReportHeaderConstants.STATUS],
            equalTo(PurchaseOrder.OrderCommand.CHARGE.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test to validate Error Message on not providing oxygen Id.
     */
    @Test
    public void testErrorToGenerateFinanceReportWithoutOxygenId() {

        financeReportPage.generateFinanceReportByOxygenId(Arrays.asList(""), PelicanConstants.VIEW);
        financeReportPage.clickOnGenerateReportWithError(2);
        final String errorMessage = financeReportPage.getErrorText();
        AssertCollector.assertThat("Incorrect Error Message", errorMessage,
            equalTo("At least one user external key must be entered."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
