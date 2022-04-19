package com.autodesk.bsm.pelican.email.productline;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.BicRelease;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.bicrelease.BicReleasePage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScfEmails extends SeleniumWebdriver {

    private JobsClient jobsResource;
    private String bicReleaseId1;
    private String bicReleaseId2;
    private String bicReleaseId3;
    private String bicReleaseId4;
    private String releaseName1;
    private String releaseName2;
    private String releaseName3;
    private String releaseName4;
    private String releaseName5;
    private String releaseName6;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private Item item7;
    private Item item8;
    private final List<String> offerAndFeatureNamesList = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        final String productLineExternalKey1 = RandomStringUtils.randomAlphanumeric(6);
        final String productLineExternalKey2 = RandomStringUtils.randomAlphanumeric(6);
        final String productLineExternalKey3 = RandomStringUtils.randomAlphanumeric(6);

        final String productLineExternalKey4 = RandomStringUtils.randomAlphanumeric(6);
        final String productLineExternalKey5 = RandomStringUtils.randomAlphanumeric(6);
        final String productLineExternalKey6 = RandomStringUtils.randomAlphanumeric(6);

        subscriptionPlanApiUtils.addProductLine(productLineExternalKey4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey5);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey6);

        final Applications applications = resource.application().getApplications();
        final String appId = applications.getApplications().get(0).getId();
        final String name = "Auto";
        final String itemTypeName = name.concat("ItemType_").concat(RandomStringUtils.randomAlphanumeric(5));
        final ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);

        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey1);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey1);
        item1 = resource.item().addItem(paramMap);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey2);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey2);
        item2 = resource.item().addItem(paramMap);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey3);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey3);
        item3 = resource.item().addItem(paramMap);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey4);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey4);
        item4 = resource.item().addItem(paramMap);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey5);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey5);
        final Item item5 = resource.item().addItem(paramMap);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey6);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey6);
        final Item item6 = resource.item().addItem(paramMap);

        final String productLineExternalKey7 = RandomStringUtils.randomAlphanumeric(6);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey7);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey7);
        item7 = resource.item().addItem(paramMap);

        final String productLineExternalKey8 = RandomStringUtils.randomAlphanumeric(6);

        paramMap.put(ItemParameter.NAME.getName(), productLineExternalKey8);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), productLineExternalKey8);
        item8 = resource.item().addItem(paramMap);

        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey3);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey7);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey8);

        // Creating 4 offerings for BIC, Meta
        final Offerings bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceIdForBic1 = bicOfferings1.getIncluded().getPrices().get(0).getId();

        Offerings bicOfferings2 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey4,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource, null, null);
        bicOfferings2 = subscriptionPlanApiUtils.addOfferAndPrices(bicOfferings2.getOffering().getId(),
            getPricelistExternalKeyUs());
        final String priceIdForBic2 = bicOfferings2.getIncluded().getPrices().get(0).getId();

        Offerings bicOfferings3 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey5,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource, null, null);
        bicOfferings3 = subscriptionPlanApiUtils.addOfferAndPrices(bicOfferings3.getOffering().getId(),
            getPricelistExternalKeyUs());
        final String priceIdForBic3 = bicOfferings3.getIncluded().getPrices().get(0).getId();

        Offerings bicOfferings4 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey6,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource, null, null);
        bicOfferings4 = subscriptionPlanApiUtils.addOfferAndPrices(bicOfferings4.getOffering().getId(),
            getPricelistExternalKeyUs());
        final String priceIdForBic4 = bicOfferings4.getIncluded().getPrices().get(0).getId();

        Offerings bicOfferings5 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOfferings5 = subscriptionPlanApiUtils.addOfferAndPrices(bicOfferings5.getOfferings().get(0).getId(),
            getPricelistExternalKeyUs());
        final String priceIdForBic5 = bicOfferings5.getIncluded().getPrices().get(0).getId();

        SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferings1.getOfferings().get(0).getId());
        EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editPackagingType(PackagingType.INDUSTRY_COLLECTION);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        AssertCollector.assertThat("Incorrect packaging type for the subscription plan",
            subscriptionPlanDetailPage.getPackagingType(), equalTo(PackagingType.INDUSTRY_COLLECTION.getDisplayName()),
            assertionErrorList);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferings5.getOfferings().get(0).getId());
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editPackagingType(PackagingType.VERTICAL_GROUPING);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        AssertCollector.assertThat("Incorrect packaging type for the subscription plan",
            subscriptionPlanDetailPage.getPackagingType(), equalTo(PackagingType.VERTICAL_GROUPING.getDisplayName()),
            assertionErrorList);

        // create Entitlements for BIC Commercial Subscription Plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            item1.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            item3.getId(), null, true);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings2.getOfferings().get(0).getId(),
            item4.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings3.getOfferings().get(0).getId(),
            item5.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings4.getOfferings().get(0).getId(),
            item6.getId(), null, true);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings5.getOfferings().get(0).getId(),
            item7.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings5.getOfferings().get(0).getId(),
            item8.getId(), null, true);

        final String userExternalKey = getEnvironmentVariables().getUserExternalKey();
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(userExternalKey);
        // Creating price id for BIC
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final int quantity = 1;

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic1, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic1, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder2 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard3 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic1, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard3);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard3);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder3 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard3);
        final String subscriptionIdBicCreditCard3 =
            purchaseOrder3.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard4 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic2, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard4);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard4);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder4 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard4);
        final String subscriptionIdBicCreditCard4 =
            purchaseOrder4.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard5 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic3, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard5);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard5);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder5 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard5);
        final String subscriptionIdBicCreditCard5 =
            purchaseOrder5.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard6 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic4, getBuyerUser(), quantity)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard6);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard6);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder6 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard6);
        final String subscriptionIdBicCreditCard6 =
            purchaseOrder6.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard7 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic5, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard7);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard7);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder7 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard7);
        final String subscriptionIdBicCreditCard7 =
            purchaseOrder7.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        String updateQuery =
            PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
                + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                    PelicanConstants.DATE_TIME_FORMAT, -2)
                + "'" + PelicanDbConstants.UPDATE_CONDITION + subscriptionIdBicCreditCard1;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE
            + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
            + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                PelicanConstants.DATE_TIME_FORMAT, -2)
            + "'" + "," + PelicanDbConstants.UPDATE_STATUS_IN_SUBSCRIPTION + "2" + PelicanDbConstants.UPDATE_CONDITION
            + subscriptionIdBicCreditCard2;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE
            + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
            + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                PelicanConstants.DATE_TIME_FORMAT, -2)
            + "'" + "," + PelicanDbConstants.UPDATE_STATUS_IN_SUBSCRIPTION + "3" + PelicanDbConstants.UPDATE_CONDITION
            + subscriptionIdBicCreditCard3;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery =
            PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
                + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                    PelicanConstants.DATE_TIME_FORMAT, -2)
                + "'" + PelicanDbConstants.UPDATE_CONDITION + subscriptionIdBicCreditCard4;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE
            + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
            + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                PelicanConstants.DATE_TIME_FORMAT, -2)
            + "'" + "," + PelicanDbConstants.UPDATE_STATUS_IN_SUBSCRIPTION + "2" + PelicanDbConstants.UPDATE_CONDITION
            + subscriptionIdBicCreditCard5;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE
            + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
            + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                PelicanConstants.DATE_TIME_FORMAT, -2)
            + "'" + "," + PelicanDbConstants.UPDATE_STATUS_IN_SUBSCRIPTION + "3" + PelicanDbConstants.UPDATE_CONDITION
            + subscriptionIdBicCreditCard6;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_SUBSCRIPTION_TABLE
            + PelicanDbConstants.UPDATE_CREATED_DATE_IN_SUBSCRIPTION + "'"
            + DateTimeUtils.addDaysToDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_FORMAT),
                PelicanConstants.DATE_TIME_FORMAT, -2)
            + "'" + "," + PelicanDbConstants.UPDATE_STATUS_IN_SUBSCRIPTION + "3" + PelicanDbConstants.UPDATE_CONDITION
            + subscriptionIdBicCreditCard7;
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final BicReleasePage bicReleaseAddPage = adminToolPage.getPage(BicReleasePage.class);

        BicRelease bicRelease1 = new BicRelease();
        final String release = RandomStringUtils.randomAlphanumeric(4);
        bicRelease1.setDownloadRelease(release);
        bicRelease1.setSubsPlanProductLine(productLineExternalKey1);
        bicRelease1.setDownloadProductLine(productLineExternalKey1);
        bicRelease1.setStatus(Status.ACTIVE);
        bicRelease1.setClic(true);
        bicRelease1.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease1.setIgnoredEmailNotification(false);
        bicRelease1 = bicReleaseAddPage.add(bicRelease1);
        bicReleaseId1 = bicRelease1.getId();
        releaseName1 = bicRelease1.getDownloadRelease();

        BicRelease bicRelease2 = new BicRelease();
        bicRelease2.setDownloadRelease(release);
        bicRelease2.setSubsPlanProductLine(productLineExternalKey2);
        bicRelease2.setDownloadProductLine(productLineExternalKey2);
        bicRelease2.setStatus(Status.ACTIVE);
        bicRelease2.setClic(true);
        bicRelease2.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease2.setIgnoredEmailNotification(false);
        bicRelease2 = bicReleaseAddPage.add(bicRelease2);
        bicReleaseId2 = bicRelease2.getId();
        releaseName2 = bicRelease2.getDownloadRelease();

        BicRelease bicRelease3 = new BicRelease();
        bicRelease3.setDownloadRelease(release);
        bicRelease3.setSubsPlanProductLine(productLineExternalKey3);
        bicRelease3.setDownloadProductLine(productLineExternalKey3);
        bicRelease3.setStatus(Status.ACTIVE);
        bicRelease3.setClic(true);
        bicRelease3.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease3.setIgnoredEmailNotification(false);
        bicRelease3 = bicReleaseAddPage.add(bicRelease3);
        bicReleaseId3 = bicRelease3.getId();
        releaseName3 = bicRelease3.getDownloadRelease();

        BicRelease bicRelease4 = new BicRelease();
        bicRelease4.setDownloadRelease(release);
        bicRelease4.setSubsPlanProductLine(productLineExternalKey4);
        bicRelease4.setDownloadProductLine(productLineExternalKey4);
        bicRelease4.setStatus(Status.ACTIVE);
        bicRelease4.setClic(true);
        bicRelease4.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease4.setIgnoredEmailNotification(false);
        bicRelease4 = bicReleaseAddPage.add(bicRelease4);
        bicReleaseId4 = bicRelease4.getId();
        releaseName4 = bicRelease4.getDownloadRelease();

        BicRelease bicRelease5 = new BicRelease();
        bicRelease5.setDownloadRelease(release);
        bicRelease5.setSubsPlanProductLine(productLineExternalKey7);
        bicRelease5.setDownloadProductLine(productLineExternalKey7);
        bicRelease5.setStatus(Status.ACTIVE);
        bicRelease5.setClic(true);
        bicRelease5.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease5.setIgnoredEmailNotification(false);
        bicRelease5 = bicReleaseAddPage.add(bicRelease5);
        releaseName5 = bicRelease5.getDownloadRelease();

        BicRelease bicRelease6 = new BicRelease();
        bicRelease6.setDownloadRelease(release);
        bicRelease6.setSubsPlanProductLine(productLineExternalKey8);
        bicRelease6.setDownloadProductLine(productLineExternalKey8);
        bicRelease6.setStatus(Status.ACTIVE);
        bicRelease6.setClic(true);
        bicRelease6.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease6.setIgnoredEmailNotification(false);
        bicRelease6 = bicReleaseAddPage.add(bicRelease6);
        releaseName6 = bicRelease6.getDownloadRelease();
    }

    @AfterClass
    public void tearDown() {
        final String updateQuery = PelicanDbConstants.UPDATE_BIC_RELEASE_TABLE + bicReleaseId1 + "," + bicReleaseId2
            + "," + bicReleaseId3 + "," + bicReleaseId4 + ")";
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

    }

    @Test
    public void testScfEmailsForDiffPackagingTypeSubscriptions() {

        // running the scf job for industry collection and Vertical grouping
        jobsResource.scfForWeeklyJob();

        offerAndFeatureNamesList.clear();
        String productName1 = item1.getName() + " " + releaseName1;
        String productName2 = item2.getName() + " " + releaseName2;
        final String productName3 = item3.getName() + " " + releaseName3;

        offerAndFeatureNamesList.add(productName1);
        offerAndFeatureNamesList.add(productName2);
        offerAndFeatureNamesList.add(productName3);

        PelicanDefaultEmailValidations.checkScfEmail(getEnvironmentVariables(), offerAndFeatureNamesList, true);

        offerAndFeatureNamesList.clear();
        productName1 = item7.getName() + " " + releaseName5;
        productName2 = item8.getName() + " " + releaseName6;

        offerAndFeatureNamesList.add(productName1);
        offerAndFeatureNamesList.add(productName2);
        PelicanDefaultEmailValidations.checkScfEmail(getEnvironmentVariables(), offerAndFeatureNamesList, false);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testScfEmailsForRegularSubscriptions() {

        // running the scf job for regular subscriptions
        jobsResource.scfEmails();
        offerAndFeatureNamesList.clear();
        final String productName4 = item4.getName() + " " + releaseName4;

        offerAndFeatureNamesList.add(productName4);
        PelicanDefaultEmailValidations.checkScfEmail(getEnvironmentVariables(), offerAndFeatureNamesList, true);

        AssertCollector.assertAll(assertionErrorList);
    }
}
