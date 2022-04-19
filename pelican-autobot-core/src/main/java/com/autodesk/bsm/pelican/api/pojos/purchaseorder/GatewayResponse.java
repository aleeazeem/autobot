package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;

public class GatewayResponse {

    private String txnDate;
    private String billingCountry; // enum?
    private String billingPostalCode;
    private String billingStateProvince;
    private String billingCity;
    private String state; // enum?
    private String amountCharged;
    private String amountChargedInUsd;
    private String exchangeRateToUsd;
    private String amountChargedCurrencyId;
    private String amountChargedCurrencyISOCode;
    private String configId;
    private String paymentType; // enum?
    private String paymentMethod; // enum?
    private String detailName;
    private String gatewayPaymentInstrumentId;

    public String getTxnDate() {
        return txnDate;
    }

    @XmlAttribute
    public void setTxnDate(final String value) {
        this.txnDate = value;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    @XmlAttribute
    public void setBillingCountry(final String value) {
        this.billingCountry = value;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    @XmlAttribute
    public void setBillingPostalCode(final String value) {
        this.billingPostalCode = value;
    }

    public String getBillingStateProvince() {
        return billingStateProvince;
    }

    @XmlAttribute
    public void setBillingStateProvince(final String value) {
        this.billingStateProvince = value;
    }

    public String getBillingCity() {
        return billingCity;
    }

    @XmlAttribute
    public void setBillingCity(final String value) {
        this.billingCity = value;
    }

    public String getState() {
        return state;
    }

    @XmlAttribute
    public void setState(final String value) {
        this.state = value;
    }

    public String getAmountCharged() {
        return amountCharged;
    }

    @XmlAttribute
    public void setAmountCharged(final String value) {
        this.amountCharged = value;
    }

    public String getAmountChargedInUsd() {
        return amountChargedInUsd;
    }

    @XmlAttribute
    public void setAmountChargedInUsd(final String value) {
        this.amountChargedInUsd = value;
    }

    public String getExchangeRateToUsd() {
        return exchangeRateToUsd;
    }

    @XmlAttribute
    public void setExchangeRateToUsd(final String value) {
        this.exchangeRateToUsd = value;
    }

    public String getAmountChargedCurrencyId() {
        return amountChargedCurrencyId;
    }

    @XmlAttribute
    public void setAmountChargedCurrencyid(final String value) {
        this.amountChargedCurrencyId = value;
    }

    public String getAmountChargedCurrencyISOCode() {
        return amountChargedCurrencyISOCode;
    }

    @XmlAttribute
    public void setAmountChargedCurrencyISOCode(final String value) {
        this.amountChargedCurrencyISOCode = value;
    }

    public String getConfigId() {
        return configId;
    }

    @XmlAttribute
    public void setConfigId(final String value) {
        this.configId = value;
    }

    public String getPaymentType() {
        return paymentType;
    }

    @XmlAttribute
    public void setPaymentType(final String value) {
        this.paymentType = value;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @XmlAttribute
    public void setPaymentMethod(final String value) {
        this.paymentMethod = value;
    }

    public String getDetailName() {
        return detailName;
    }

    @XmlAttribute
    public void setDetailName(final String value) {
        this.detailName = value;
    }

    public String getGatewayPaymentInstrumentId() {
        return gatewayPaymentInstrumentId;
    }

    @XmlAttribute
    public void setGatewayPaymentInstrumentId(final String value) {
        this.gatewayPaymentInstrumentId = value;
    }
}
