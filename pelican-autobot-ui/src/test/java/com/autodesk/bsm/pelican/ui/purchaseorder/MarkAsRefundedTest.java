package com.autodesk.bsm.pelican.ui.purchaseorder;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.MARK_AS_REFUND_NOTES;
import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Transaction.TransactionType;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This test class verifies the purchase Order Page in Admin Tool to check if EBSO user is able to perform Mark As
 * Refunded and non-EBSO user does is not able to perform this action.
 *
 * @author jains
 */
public class MarkAsRefundedTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private AdminToolPage adminToolPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private UserUtils userUtils;
    private HashMap<String, String> userParams;
    private RolesHelper rolesHelper;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private User ebsoUser;
    private boolean isEbsoUserLoggedIn;
    private boolean isNonEbsoUserLoggedIn;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        rolesHelper = new RolesHelper(getEnvironmentVariables());
        isEbsoUserLoggedIn = false;
        isNonEbsoUserLoggedIn = false;

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Test to verify the PO page that EBSO user is able to manually refund a order
     */
    @Test
    public void markAsRefundedPurchaseOrderEBSOTest() {
        if (!isEbsoUserLoggedIn) {
            // creating and logging as ebso user
            adminToolPage.login();
            userParams = new HashMap<>();
            userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.EBSO_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
            ebsoUser = userUtils.createAssignRoleAndLoginUser(
                userParams, Lists.newArrayList(Role.EBSO.getValue(), Role.BANKING_ADMIN.getValue(),
                    Role.ADMIN.getValue(), Role.APPLICATION_MANAGER.getValue()),
                adminToolPage, getEnvironmentVariables());
            isEbsoUserLoggedIn = true;
        }
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);

        final String chargedPurchaseOrderId = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser).getId();

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(chargedPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // clicking on mark as refunded and checking if notes can be left blank
        final boolean isNotesEmpty = purchaseOrderDetailPage.checkEmptyNotesOnMarkAsRefundedOrder();
        AssertCollector.assertTrue("Notes should not be left blank", isNotesEmpty, assertionErrorList);

        // clicking on mark as refunded if order is not already marked as refunded
        if (isNotesEmpty) {
            purchaseOrderDetailPage.clickMarkAsRefunded();
            purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(MARK_AS_REFUND_NOTES);
        }
        purchaseOrderDetailPage.refreshPage();
        // getting credit note number from po call
        final PurchaseOrder purchaseOrderFromApi = resource.purchaseOrder().getById(chargedPurchaseOrderId);
        final String expectedCreditNoteNumber = purchaseOrderFromApi.getCreditNoteNumber();
        final String expectedOrderStateFromApi = purchaseOrderFromApi.getOrderState();

        AssertCollector.assertThat("Incorrect order state in api call", expectedOrderStateFromApi.toUpperCase(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state in order details", purchaseOrderDetailPage.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect transaction type", purchaseOrderDetailPage.getTransactionType(5),
            equalTo(TransactionType.REFUND.toString()), assertionErrorList);
        AssertCollector.assertThat("Credit note number not matching with po call",
            purchaseOrderDetailPage.getCreditNoteNumber(), equalTo(expectedCreditNoteNumber), assertionErrorList);
        AssertCollector.assertThat("Incorrect requested by user name",
            purchaseOrderDetailPage.getTransactionRequestedBy(5).split(" ")[0], equalTo(ebsoUser.getName()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect notes returned", purchaseOrderDetailPage.getProperties(1, 2),
            equalTo(MARK_AS_REFUND_NOTES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the PO page that EBSO user is able to manually refund a order
     */
    @Test
    public void markAsRefundedPurchaseOrderNonEBSOTest() {
        if (!isNonEbsoUserLoggedIn) {
            adminToolPage.login();
            userParams = new HashMap<>();
            userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_EBSO_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

            // Log in as a non-ebso user
            final List nonEbsoRoleList = rolesHelper.getNonEbsoRoleList();
            userUtils.createAssignRoleAndLoginUser(userParams, nonEbsoRoleList, adminToolPage,
                getEnvironmentVariables());
            isNonEbsoUserLoggedIn = true;
        }
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);

        final String chargedPurchaseOrderId = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, true, buyerUser).getId();

        findPurchaseOrdersPage.findPurchaseOrderById(chargedPurchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        AssertCollector.assertThat(
            String.format("Mark As Refunded should not be enabled for Non EBSO user for PO %s", chargedPurchaseOrderId),
            purchaseOrderDetailPage.isMarkAsRefundedEnabled(), equalTo(PelicanConstants.FALSE_VALUE),
            assertionErrorList);

        AssertCollector.assertThat(String.format("Order State is incorrect for PO %s", chargedPurchaseOrderId),
            purchaseOrderDetailPage.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);

        AssertCollector.assertThat(String.format("Last Transaction is incorrect for PO %s", chargedPurchaseOrderId),
            purchaseOrderDetailPage.getTransactionType(3), equalTo(TransactionType.SALE.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
