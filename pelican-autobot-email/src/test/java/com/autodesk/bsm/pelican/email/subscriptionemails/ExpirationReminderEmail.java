package com.autodesk.bsm.pelican.email.subscriptionemails;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanAndOfferDescriptorsPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DescriptorUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Test class to verify Expiration Reminder Email.
 *
 * @author jains
 *
 */
public class ExpirationReminderEmail extends SeleniumWebdriver {

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
     * This test case verifies expiration reminder email is send with out Descriptor
     *
     */
    @Test
    public void testExpirationReminderEmailWithoutDescriptor() {

        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);
        final String purchaseOrderId =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null).getId();
        // get purchase order api response
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        // get subscription id from purchase order
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String buyerUserEmail = purchaseOrder.getBuyerUser().getEmail();

        final FindSubscriptionsPage findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        // Change the next billing date of the subscription in Admin Tool
        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        final String nextBillingOrExpirationDate = DateTimeUtils.getNowPlusDays(31);
        subscriptionDetailPage = editSubscriptionPage.editASubscription(nextBillingOrExpirationDate, null, null, null);

        // Cancel the subscription
        final boolean isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionId,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            Assert.fail("Error! Subscription is not cancelled " + subscriptionId);
        }

        // Run expiration reminder job
        jobsResource.subscriptionExpirationReminder(getEnvironmentVariables());
        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
        subscriptionDetailPage.refreshPage();
        subscriptionDetailPage = subscriptionDetailPage.getPage(SubscriptionDetailPage.class);
        if (!subscriptionDetailPage.getLastSubscriptionActivity().getActivity()
            .equals(PelicanConstants.EXPIRATION_REMINDER)) {
            Assert.fail("Expiration reminder activity is not captured for subscription id: " + subscriptionId);
        }
        PelicanDefaultEmailValidations.expirationReminder(nextBillingOrExpirationDate, getEnvironmentVariables(),
            buyerUserEmail, "AUTO_PRODUCT_LINE_MAYA");
    }

    /**
     * This test case verifies expiration reminder email is send with Descriptor
     *
     */
    @Test
    public void testExpirationReminderEmailWithDescriptor() {

        final SubscriptionPlanDetailPage subscriptionPlan = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        final EditSubscriptionPlanAndOfferDescriptorsPage editSubscriptionPlanAndOfferDescriptorsPage =
            adminToolPage.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
        final AddDescriptorPage addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        final String descriptorProductValue = "Test product line";
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings offerings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionPlanId = offerings.getOfferings().get(0).getId();
        final String subscriptionPriceId = offerings.getIncluded().getPrices().get(0).getId();
        final Descriptor descriptors = DescriptorUtils.getDescriptorData(
            DescriptorEntityTypes.getEntityType(PelicanConstants.SUBSCRIPTION_PLAN_FIELD), PelicanConstants.ESTORE,
            "productName1", "productName1", "No", "");
        final DescriptorDefinitionDetailPage detailPage =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());

        Descriptor descriptorsPage;

        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        subscriptionPlan.navigateToSubscriptionPlanPage(subscriptionPlanId);

        // wait for Edit Subscription plan page to load .Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = addSubscriptionPlanPage.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);

        subscriptionPlan.clickOnEditNonLocalizedDescriptors();
        editSubscriptionPlanAndOfferDescriptorsPage.editNonLocalizedEstoreDescriptorValue(descriptorProductValue);
        editSubscriptionPlanAndOfferDescriptorsPage.submit(TimeConstants.TWO_SEC);

        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(subscriptionPriceId, 1);

        final String purchaseOrderId =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null).getId();
        // get purchase order api response
        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        // get subscription id from purchase order
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String buyerUserEmail = purchaseOrder.getBuyerUser().getEmail();

        final FindSubscriptionsPage findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        // Change the next billing date of the subscription in Admin Tool
        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        final String nextBillingOrExpirationDate = DateTimeUtils.getNowPlusDays(31);
        subscriptionDetailPage = editSubscriptionPage.editASubscription(nextBillingOrExpirationDate, null, null, null);

        // Cancel the subscription
        final boolean isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionId,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            Assert.fail("Error! Subscription is not cancelled " + subscriptionId);
        }

        // Run expiration reminder job
        jobsResource.subscriptionExpirationReminder(getEnvironmentVariables());
        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
        subscriptionDetailPage.refreshPage();
        subscriptionDetailPage = subscriptionDetailPage.getPage(SubscriptionDetailPage.class);
        if (!subscriptionDetailPage.getLastSubscriptionActivity().getActivity()
            .equals(PelicanConstants.EXPIRATION_REMINDER)) {
            Assert.fail("Expiration reminder activity is not captured for subscription id: " + subscriptionId);
        }
        PelicanDefaultEmailValidations.expirationReminder(nextBillingOrExpirationDate, getEnvironmentVariables(),
            buyerUserEmail, descriptorProductValue);
    }

}
