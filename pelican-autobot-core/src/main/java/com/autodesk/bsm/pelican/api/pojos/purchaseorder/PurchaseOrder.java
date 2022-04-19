package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "purchaseOrder")
public class PurchaseOrder extends PelicanPojo {

    private String version;
    private String appFamilyId;
    private String creationTime;
    private String lastModified;
    private String id;
    private String subscriptionId;
    private String externalKey;
    private String storeExternalKey;
    private FulFillmentStatus fulfillmentStatus;
    private FulfillmentGroups fulfillmentGroups;
    private String invoiceNumber;
    private Transactions transactions;
    private BuyerUser buyerUser;
    private LineItems lineItems;
    private String orderCommand;
    private String orderState;
    private String orderType;
    private Payment payment;
    private Shipping shipping;
    private BillingInformation billingInformation;
    private String creditNoteNumber;
    private PurchaseOrderProperty purchaseOrderProperties;
    private SentEmails sentEmails;
    private AllowedCommands allowedCommands;
    private String storeId;
    private String declineReason;
    private String language;

    public enum OrderCommand {

        CHARGE("Charge"),
        AUTHORIZE("Authorize"),
        FUNDS_AUTHORIZATION("Funds Authorization"),
        CANCEL("Cancel"),
        REFUND("Refund"),
        CHARGEBACK("Chargeback"),
        PENDING("Pending"),
        DECLINE("Decline"),
        START_REFUND("Start Refund"),
        CAPTURE_FUNDS("Capture Funds"),
        CANCEL_FUNDS_AUTHORIZATION("Cancel Funds Authorization");

        private String command;

        OrderCommand(final String command) {
            this.command = command;
        }

        public String getValue() {
            return command;
        }
    }

    public enum OrderState {
        CHARGED("Charged"),
        AUTHORIZED("Authorized"),
        FUNDS_AUTHORIZED("Fund Authorized"),
        SUBMITTED("Submitted"),
        APPROVED("Approved"),
        DECLINED("Declined"),
        REFUNDED("Refunded"),
        RECORDED("Recorded"),
        ERROR("Error"),
        PENDING("Pending"),
        CHARGED_BACK("Charged Back"),
        CANCELLED("Cancelled");

        private String status;

        OrderState(final String status) {
            this.status = status;
        }

        public String getValue() {
            return status;
        }

        public static OrderState getByValue(final String value) {
            if (value != null) {
                for (final OrderState item : OrderState.values()) {
                    if (item.status.equalsIgnoreCase(value)) {
                        return item;
                    }
                }
            }
            throw new IllegalArgumentException("No Order Status with value '" + value + "' found.");
        }
    }

    public String getVersion() {
        return version;
    }

    @XmlAttribute(name = "version")
    public void setVersion(final String value) {
        this.version = value;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    @XmlAttribute(name = "appFamilyId")
    public void setAppFamilyId(final String id) {
        this.appFamilyId = id;
    }

    public String getCreationTime() {
        return creationTime;
    }

    @XmlAttribute(name = "creationTime")
    public void setCreationTime(final String time) {
        this.creationTime = time;
    }

    public String getLastModified() {
        return lastModified;
    }

    @XmlAttribute(name = "lastModified")
    public void setLastModified(final String time) {
        this.lastModified = time;
    }

    public FulFillmentStatus getFulFillmentStatus() {
        return fulfillmentStatus;
    }

    @XmlAttribute(name = "fulfillmentStatus")
    public void setFulFillmentStatus(final FulFillmentStatus status) {
        this.fulfillmentStatus = status;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    @XmlAttribute(name = "invoiceNumber")
    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlElement(name = "externalKey")
    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public BuyerUser getBuyerUser() {
        return buyerUser;
    }

    @XmlElement(name = "buyerUser")
    public void setBuyerUser(final BuyerUser buyerUser) {
        this.buyerUser = buyerUser;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    @XmlElement(name = "declineReason")
    public void setDeclineReason(final String declineReason) {
        this.declineReason = declineReason;
    }

    public String getOrderCommand() {
        return orderCommand;
    }

    @XmlElement(name = "orderCommand")
    public void setOrderCommand(final String orderCommand) {
        this.orderCommand = orderCommand;
    }

    public String getOrderState() {
        return orderState;
    }

    @XmlElement(name = "orderState")
    public void setOrderState(final String orderState) {
        this.orderState = orderState;
    }

    /**
     * @return the orderType
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * @param orderType the orderType to set
     */
    @XmlElement(name = "orderType")
    public void setOrderType(final String orderType) {
        this.orderType = orderType;
    }

    public LineItems getLineItems() {
        return lineItems;
    }

    @XmlElement(name = "lineItems")
    public void setLineItems(final LineItems lineItems) {
        this.lineItems = lineItems;
    }

    public FulfillmentGroups getFulFillmentGroups() {
        return fulfillmentGroups;
    }

    @XmlElement(name = "fulfillmentGroups")
    public void setFulFillmentGroups(final FulfillmentGroups fulfillmentGroups) {
        this.fulfillmentGroups = fulfillmentGroups;
    }

    public Transactions getTransactions() {
        return transactions;
    }

    @XmlElement(name = "transactions")
    public void setTransactions(final Transactions transactions) {
        this.transactions = transactions;
    }

    public Shipping getShipping() {
        return shipping;
    }

    @XmlElement(name = "shipping")
    public void setShipping(final Shipping shipping) {
        this.shipping = shipping;
    }

    public Payment getPayment() {
        return payment;
    }

    @XmlElement(name = "payment")
    public void setPayment(final Payment payment) {
        this.payment = payment;
    }

    public AllowedCommands getAllowedCommands() {
        return allowedCommands;
    }

    @XmlElement(name = "allowedCommands")
    public void setAllowedCommands(final AllowedCommands commands) {
        this.allowedCommands = commands;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getStoreExternalKey() {
        return storeExternalKey;
    }

    @XmlAttribute(name = "storeExternalKey")
    public void setStoreExternalKey(final String storeExternalKey) {
        this.storeExternalKey = storeExternalKey;
    }

    /**
     * @return the billingInformation
     */
    public BillingInformation getBillingInformation() {
        return billingInformation;
    }

    /**
     * @param billingInformation the billingInformation to set
     */
    @XmlElement(name = "billingInformation")
    public void setBillingInformation(final BillingInformation billingInformation) {
        this.billingInformation = billingInformation;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    @XmlElement(name = "creditNote")
    public void setCreditNoteNumber(final String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }

    public PurchaseOrderProperty getProperties() {
        return purchaseOrderProperties;
    }

    @XmlElement(name = "properties")
    public void setProperties(final PurchaseOrderProperty purchaseOrderProperties) {
        this.purchaseOrderProperties = purchaseOrderProperties;
    }

    public SentEmails getSentEmails() {
        return sentEmails;
    }

    @XmlElement(name = "sentEmails")
    public void setSentEmails(final SentEmails sentEmails) {
        this.sentEmails = sentEmails;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(final String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStoreId() {
        return storeId;
    }

    @XmlAttribute(name = "storeId")
    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    @XmlElement(name = "language")
    public void setLanguage(final String language) {
        this.language = language;
    }
}
