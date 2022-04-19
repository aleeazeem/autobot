package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonPendingPurchaseOrderStatus;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PendingPurchaseOrderJobTest extends SeleniumWebdriver {

    private JobsClient jobsResource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private int midasHiveValueForHour;
    private int midasHiveValueForDays;
    private BuyerUser buyerUser;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Get No of Hours Configured from Midas Hive.
        midasHiveValueForHour = Integer.parseInt(DbUtils.getMidasHiveValue(
            PelicanDbConstants.SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_IN_HOURS, getEnvironmentVariables()));

        // Get No of Days Configured from Midas Hive.
        midasHiveValueForDays = Integer.parseInt(DbUtils.getMidasHiveValue(
            PelicanDbConstants.SFDC_CASE_FOR_PENDING_PURCHASE_ORDERS_CREATED_IN_DAYS, getEnvironmentVariables()));

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG, true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG,
                false);
        }
    }

    /**
     * Method to test Pending Purchase Order for more than X hours create SFDC case.
     */
    @Test
    public void testPurchaseOrderStuckAtPendingCreatesSFDCCase() {

        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);

        final String purchaseOrderId = purchaseOrder.getId();

        // process the purchase order to 'Pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        // Database query to change last modified date in past by configured value + 1 hours.
        purchaseOrderUtils.updateTransactionDate(purchaseOrderId, DateTimeUtils
            .getCurrentTimeMinusSpecifiedHours(PelicanConstants.DB_DATE_FORMAT, midasHiveValueForHour + 1));

        // Run Pending PO SFDC job
        final Collection<JsonPendingPurchaseOrderStatus> pendingPurchaseOrderCollection =
            jobsResource.pendingPurchaseOrder();

        final List<String> purchaseOrderList = new ArrayList<>();

        for (final JsonPendingPurchaseOrderStatus jsonPendingPurchaseOrder : pendingPurchaseOrderCollection) {
            purchaseOrderList.add(jsonPendingPurchaseOrder.getPurchaseOrderId());
        }

        AssertCollector.assertTrue("Purchase Order is Missing", purchaseOrderList.contains(purchaseOrderId),
            assertionErrorList);

        final String sfdcCaseNumber =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_SFDC_CASE_FOR_PO, purchaseOrderId),
                getEnvironmentVariables()).get(0).get("SFDC_CASE_NUMBER");
        AssertCollector.assertThat("SFDC CASE should not be null", sfdcCaseNumber, notNullValue(), assertionErrorList);

        jobsResource.pendingPurchaseOrder();

        final int totalSfdcCases =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_SFDC_CASE_FOR_PO, purchaseOrderId),
                getEnvironmentVariables()).size();
        AssertCollector.assertThat("SFDC CASE should not be more than one", totalSfdcCases, is(1), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test Pending Purchase Order for more than X hours but created before Y days will not create SFDC case.
     */
    @Test
    public void testPurchaseOrderStuckAtPendingAndCreationDateIsInPastByYDaysNotCreateSFDCCase() {

        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);

        final String purchaseOrderId = purchaseOrder.getId();

        // process the purchase order to 'Pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        // Update DB for Create Date in past by Y+1 days.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_PURCHASE_ORDER_CREATE_DATE,
            DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_TIME_FORMAT, midasHiveValueForDays + 1),
            purchaseOrderId), getEnvironmentVariables());

        // Database query to change last modified date in past by configured value + 1 hours.
        purchaseOrderUtils.updateTransactionDate(purchaseOrderId, DateTimeUtils
            .getCurrentTimeMinusSpecifiedHours(PelicanConstants.DB_DATE_FORMAT, midasHiveValueForHour + 1));

        // Run Pending PO SFDC job
        final Collection<JsonPendingPurchaseOrderStatus> pendingPurchaseOrderCollection =
            jobsResource.pendingPurchaseOrder();

        final List<String> purchaseOrderList = new ArrayList<>();

        for (final JsonPendingPurchaseOrderStatus jsonPendingPurchaseOrder : pendingPurchaseOrderCollection) {
            purchaseOrderList.add(jsonPendingPurchaseOrder.getPurchaseOrderId());
        }

        AssertCollector.assertFalse("Purchase Order should not be present", purchaseOrderList.contains(purchaseOrderId),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Place an order with Billing info and properties with paypalPending echeck. update PO Pending time in past. Run
     * Pending Purchase Order Job for SFDC. It should not create SFDC case.
     */
    @Test
    public void testECheckPaypalPendingPurchaseOrderDoesNotCreateSFDCCase() {

        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(getBic2YearsUsPriceId(), 5, null, null);
        final List<LineItem> lineItems = Arrays.asList(lineitem1);
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitPaypalPurchaseOrderWithRecorderStateAndBillingInfo(
            lineItems, getEnvironmentVariables().getPaypalNamerPaymentGatewayId(), buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);

        // Database query to change last modified date in past by configured value + 1 hours.
        purchaseOrderUtils.updateTransactionDate(purchaseOrderId, DateTimeUtils
            .getCurrentTimeMinusSpecifiedHours(PelicanConstants.DB_DATE_FORMAT, midasHiveValueForHour + 1));

        updatePoProperties(purchaseOrderId);

        // Run Pending PO SFDC job
        final Collection<JsonPendingPurchaseOrderStatus> pendingPurchaseOrderCollection =
            jobsResource.pendingPurchaseOrder();

        final List<String> purchaseOrderList = new ArrayList<>();

        for (final JsonPendingPurchaseOrderStatus jsonPendingPurchaseOrder : pendingPurchaseOrderCollection) {
            purchaseOrderList.add(jsonPendingPurchaseOrder.getPurchaseOrderId());
        }

        AssertCollector.assertFalse("Purchase Order should not be present", purchaseOrderList.contains(purchaseOrderId),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test that Direct Debit Pending orders will not create SFDC orders if crosses the configured time.
     */
    @Test
    public void testDirectDebitPendingOrdersDoesNotCreateSFDCCase() {

        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Tom", "Wills", "ABC",
            "Main St", "", "94007", "CALIFORNIA", "San Francisco", Country.US, "(123)840-0007", "",
            PaymentType.DIRECT_DEBIT.getValue(), null, null, "6789", Payment.PaymentMethod.ACH.getValue());

        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitPurchaseOrderUsingDirectDebitPaymentProfile(
            Payment.PaymentMethod.ACH, Payment.PaymentProcessor.BLUESNAP_NAMER.getValue(), getBicMonthlyUsPriceId(),
            buyerUser, 1, billingInformation);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        // Database query to change last modified date in past by configured value + 1 hours.
        purchaseOrderUtils.updateTransactionDate(purchaseOrderId, DateTimeUtils
            .getCurrentTimeMinusSpecifiedHours(PelicanConstants.DB_DATE_FORMAT, midasHiveValueForHour + 1));

        // Run Pending PO SFDC job
        final Collection<JsonPendingPurchaseOrderStatus> pendingPurchaseOrderCollection =
            jobsResource.pendingPurchaseOrder();

        final List<String> purchaseOrderList = new ArrayList<>();

        for (final JsonPendingPurchaseOrderStatus jsonPendingPurchaseOrder : pendingPurchaseOrderCollection) {
            purchaseOrderList.add(jsonPendingPurchaseOrder.getPurchaseOrderId());
        }

        AssertCollector.assertFalse("Purchase Order is present in SFDC", purchaseOrderList.contains(purchaseOrderId),
            assertionErrorList);

        final List<Map<String, String>> sfdcQuery = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_SFDC_CASE_FOR_PO, purchaseOrderId), getEnvironmentVariables());
        AssertCollector.assertThat("SFDC CASE should not be created", sfdcQuery.size(), is(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Change Properties.
     * <p>
     * Note:This is to simulate Properties for PO.
     */
    private void updatePoProperties(final String purchaseOrderId) {

        // Get Payload from DB.
        final String payload = DbUtils.selectQuery("select PAYLOAD from purchase_order where id = " + purchaseOrderId,
            "PAYLOAD", getEnvironmentVariables()).get(0);

        // Set fulfillmentGroup status to Pending in payload
        final String updatedPayload = updatePayloadForProperties(payload);

        // Update payload to DB.
        DbUtils.updateTableInDb("purchase_order", "PAYLOAD", "'" + updatedPayload + "'", "id", purchaseOrderId,
            getEnvironmentVariables());
    }

    /**
     * Update Purchase Order XML for Properties.
     */
    private static String updatePayloadForProperties(final String payload) {
        try {
            final Document document = Util.loadXMLFromString(payload);
            final Element root = document.getDocumentElement();
            final Element properties = document.createElement("properties");
            final Element property = document.createElement("property");

            root.appendChild(properties);
            properties.appendChild(property);

            final Attr nameAttribute = document.createAttribute("name");
            nameAttribute.setValue("paypalPending");

            final Attr valueAttribute = document.createAttribute("value");
            valueAttribute.setValue("echeck");

            property.setAttributeNode(nameAttribute);
            property.setAttributeNode(valueAttribute);

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return payload;
    }

}
