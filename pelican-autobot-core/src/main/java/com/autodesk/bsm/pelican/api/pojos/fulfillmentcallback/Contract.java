package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class is a pojo for Contract entity in FulfillmentCallBack API request
 *
 * @author kishor
 */
public class Contract {

    private String contractNumber;

    /**
     * @return the contractNumber
     */
    public String getContractNumber() {
        return contractNumber;
    }

    /**
     * @param contractNumber the contractNumber to set
     */
    @XmlElement(name = "ContractNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setContractNumber(final String contractNumber) {
        this.contractNumber = contractNumber;
    }
}
