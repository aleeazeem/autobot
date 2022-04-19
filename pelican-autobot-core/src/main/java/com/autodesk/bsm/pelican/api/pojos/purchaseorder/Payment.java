package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Payment {

    private String configId;
    private String currencyId;
    private String paymentProcessor;
    private CreditCard creditCard;
    private StoredProfilePayment storedProfilePayment;
    private Recorder recorder;
    private String mandateId;
    private String mandateDate;

    public enum PaymentProcessor {

        BLUESNAP_NAMER("BLUESNAP-NAMER"),
        BLUESNAP_EMEA("BLUESNAP-EMEA"),
        PAYPAL_NAMER("PAYPAL-NAMER"),
        PAYPAL_EMEA("PAYPAL-EMEA");

        private String paymentProcessor;

        PaymentProcessor(final String paymentProcessor) {
            this.paymentProcessor = paymentProcessor;
        }

        public String getValue() {
            return paymentProcessor;
        }
    }

    public enum PaymentType {

        PAYPAL("PAYPAL"),
        CREDIT_CARD("CREDIT-CARD"),
        DIRECT_DEBIT("Direct Debit");

        private String paymentType;

        PaymentType(final String paymentType) {
            this.paymentType = paymentType;
        }

        public String getValue() {
            return paymentType;
        }
    }

    public enum PaymentMethod {
        ACH("ACH"),
        SEPA("SEPA");

        private String paymentMethod;

        PaymentMethod(final String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getValue() {
            return paymentMethod;
        }
    }

    public enum PaymentOption {
        VISA("VISA"),
        MASTERCARD("MASTERCARD"),
        AMEX("AMEX"),
        PAYPAL("PAYPAL");

        private String paymentOption;

        PaymentOption(final String paymentOption) {
            this.paymentOption = paymentOption;
        }

        public String getValue() {
            return paymentOption;
        }
    }

    public String getConfigId() {
        return configId;
    }

    @XmlAttribute(name = "configId")
    public void setConfigId(final String value) {
        this.configId = value;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    @XmlAttribute(name = "currencyId")
    public void setCurrencyId(final String value) {
        this.currencyId = value;
    }

    public String getPaymentProcessor() {
        return paymentProcessor;
    }

    @XmlAttribute(name = "paymentProcessor")
    public void setPaymentProcessor(final String paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public StoredProfilePayment getStoredProfilePayment() {
        return storedProfilePayment;
    }

    @XmlElement(name = "storedProfilePayment")
    public void setStoredProfilePayment(final StoredProfilePayment storedProfilePayment) {
        this.storedProfilePayment = storedProfilePayment;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    @XmlElement(name = "creditCard")
    public void setCreditCard(final CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    /**
     * @return the recorder
     */
    public Recorder getRecorder() {
        return recorder;
    }

    /**
     * @param recorder the recorder to set
     */
    @XmlElement(name = "recorder")
    public void setRecorder(final Recorder recorder) {
        this.recorder = recorder;
    }

    public String getMandateId() {
        return mandateId;
    }

    /**
     * Mandate Id Field for SEPA
     *
     * @param mandateId
     */
    @XmlElement(name = "mandateId")
    public void setMandateId(final String mandateId) {
        this.mandateId = mandateId;
    }

    public String getMandateDate() {
        return mandateDate;
    }

    /**
     * Set Mandate Date for SEPA
     *
     * @param mandateDate
     */
    @XmlElement(name = "mandateDate")
    public void setMandateDate(final String mandateDate) {
        this.mandateDate = mandateDate;
    }
}
