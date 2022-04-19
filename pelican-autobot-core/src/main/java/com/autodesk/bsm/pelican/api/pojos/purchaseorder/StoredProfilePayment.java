package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

public class StoredProfilePayment {

    private String storedPaymentProfileId;

    public String getStoredPaymentProfileId() {
        return storedPaymentProfileId;
    }

    @XmlElement
    public void setStoredPaymentProfileId(final String value) {
        this.storedPaymentProfileId = value;
    }
}
