package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ShippingMethod {

    private String externalKey;
    private String amount;
    private String currencyName;
    private int currencyId;
    private int forwardingAgentId;

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute(name = "externalKey")
    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getAmount() {
        return amount;
    }

    @XmlAttribute(name = "amount")
    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    @XmlAttribute(name = "currencyName")
    public void setCurrencyName(final String currencyName) {
        this.currencyName = currencyName;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    @XmlAttribute(name = "currencyId")
    public void setCurrencyId(final int currencyId) {
        this.currencyId = currencyId;
    }

    public int getForwardingAgentId() {
        return forwardingAgentId;
    }

    @XmlAttribute(name = "forwardingAgentId")
    public void setForwardingAgentId(final int forwardingAgentId) {
        this.forwardingAgentId = forwardingAgentId;
    }
}
