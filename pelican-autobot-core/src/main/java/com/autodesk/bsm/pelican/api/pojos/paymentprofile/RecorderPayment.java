package com.autodesk.bsm.pelican.api.pojos.paymentprofile;

import javax.xml.bind.annotation.XmlElement;

public class RecorderPayment {
    private String state;

    public String getState() {
        return state;
    }

    @XmlElement(name = "state")
    public void setState(final String state) {
        this.state = state;
    }

}
