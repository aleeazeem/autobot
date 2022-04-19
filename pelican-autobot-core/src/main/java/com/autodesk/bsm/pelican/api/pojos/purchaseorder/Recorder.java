package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class represents the payment recorder entity in processPurchaseOrder API request
 *
 * @author kishor
 */
public class Recorder {

    private String state;

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    @XmlElement
    public void setState(final String state) {
        this.state = state;
    }
}
