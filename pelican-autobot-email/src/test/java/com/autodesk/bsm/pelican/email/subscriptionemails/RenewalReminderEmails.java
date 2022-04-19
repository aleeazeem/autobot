package com.autodesk.bsm.pelican.email.subscriptionemails;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.RENEWAL_REMINDER_EMAIL_DAYS_MONTHLY;
import static com.autodesk.bsm.pelican.constants.PelicanConstants.RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class tests the billing reminder emails= for monthly and yearly subscriptions. For monthly subscription email is
 * triggered 7 days before renewal and for yearly subscriptions email is triggered 15 days before renewal of a
 * subscription.
 *
 * @author Muhammad
 *
 */
public class RenewalReminderEmails extends SeleniumWebdriver {
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private JobsClient jobsResource;
    private AdminToolPage adminToolPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
    }

    /**
     * This method tests the renewal reminder email for the yearly and monthly subscriptions with credit card and paypal
     * purchases.
     *
     * @param billingFrequency
     * @param priceId
     * @param paymentType
     */
    @Test(dataProvider = "getDataToValidateEmails")
    public void testRenewalReminderEmail(final BillingFrequency billingFrequency, final String priceId,
        final PaymentType paymentType) {

        int plusDaysFromToday;
        if (BillingFrequency.MONTH == billingFrequency) {
            plusDaysFromToday = RENEWAL_REMINDER_EMAIL_DAYS_MONTHLY;
        } else {
            plusDaysFromToday = RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY;
        }

        String purchaseOrderId;
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(priceId, 1);
        if (PaymentType.CREDIT_CARD == paymentType) {
            purchaseOrderId = purchaseOrderUtils
                .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null).getId();
        } else {
            purchaseOrderId = purchaseOrderUtils
                .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, null).getId();
        }
        // get purchase order api response
        PurchaseOrder fulfillmentCheckForPO = resource.purchaseOrder().getById(purchaseOrderId);
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(fulfillmentCheckForPO, FulfillmentCallbackStatus.Created);

        // get purchase order api response
        fulfillmentCheckForPO = resource.purchaseOrder().getById(purchaseOrderId);
        // get subscription id from purchase order
        final String subscriptionId = fulfillmentCheckForPO.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        final String buyerUserEmail = fulfillmentCheckForPO.getBuyerUser().getEmail();

        // Change the expiration billing date of the subscription in Admin Tool
        final FindSubscriptionsPage subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final SubscriptionDetailPage subscriptionDetailPage = subscriptionPage.findBySubscriptionId(subscriptionId);

        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowPlusDays(plusDaysFromToday), null, null, null);

        jobsResource.renewalReminder();
        final Object apiResponse = resource.subscriptionJson().getSubscription(subscriptionId, "offering.entitlements",
            PelicanConstants.CONTENT_TYPE);
        final JSubscription subscription = (JSubscription) apiResponse;

        PelicanDefaultEmailValidations.renewalReminder(subscription, getEnvironmentVariables(), billingFrequency,
            buyerUserEmail, "$");
    }

    @DataProvider(name = "getDataToValidateEmails")
    private Object[][] getDataToValidateEmails() {
        return new Object[][] { { BillingFrequency.MONTH, getBicMonthlyUsPriceId(), PaymentType.CREDIT_CARD },
                { BillingFrequency.YEAR, getMetaYearlyUsPriceId(), PaymentType.PAYPAL }, };
    }

}
