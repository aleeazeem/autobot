package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class AdditionalFees {

    private AdditionalFee additionalFee;

    public static class AdditionalFee {
        private int currencyId;
        private String type;
        private String feeCollectorId;
        private String feeCollectorExternalKey;
        private String taxIncludedInBasePrice;
        private String taxPayer;
        private String taxType;
        private String amount;
        private String currencyName;

        public int getCurrencyId() {
            return currencyId;
        }

        @XmlAttribute
        public void setCurrencyId(final int currencyId) {
            this.currencyId = currencyId;
        }

        public String getType() {
            return type;
        }

        @XmlAttribute
        public void setType(final String type) {
            this.type = type;
        }

        public String getFeeCollectorId() {
            return feeCollectorId;
        }

        @XmlAttribute
        public void setFeeCollectorId(final String feeCollectorId) {
            this.feeCollectorId = feeCollectorId;
        }

        public String getFeeCollectorExternalKey() {
            return feeCollectorExternalKey;
        }

        @XmlAttribute
        public void setFeeCollectorExternalKey(final String feeCollectorExternalKey) {
            this.feeCollectorExternalKey = feeCollectorExternalKey;
        }

        public String getTaxIncludedInBasePrice() {
            return taxIncludedInBasePrice;
        }

        @XmlAttribute
        public void setTaxIncludedInBasePrice(final String taxIncludedInBasePrice) {
            this.taxIncludedInBasePrice = taxIncludedInBasePrice;
        }

        public String getTaxPayer() {
            return taxPayer;
        }

        @XmlAttribute
        public void setTaxPayer(final String taxPayer) {
            this.taxPayer = taxPayer;
        }

        public String getTaxType() {
            return taxType;
        }

        @XmlAttribute
        public void setTaxType(final String taxType) {
            this.taxType = taxType;
        }

        public String getAmount() {
            return amount;
        }

        @XmlAttribute
        public void setAmount(final String amount) {
            this.amount = amount;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        @XmlAttribute
        public void setCurrencyName(final String currencyName) {
            this.currencyName = currencyName;
        }

    }

    public AdditionalFee getAdditionalFee() {
        return additionalFee;
    }

    @XmlElement
    public void setAdditionalFee(final AdditionalFee additionalFee) {
        this.additionalFee = additionalFee;
    }
}
