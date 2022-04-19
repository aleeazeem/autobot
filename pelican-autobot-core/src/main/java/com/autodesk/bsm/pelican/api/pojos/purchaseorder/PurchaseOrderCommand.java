package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the pojo for marshalling processPurchaseOrder API request
 *
 * @author kishor
 */
@XmlRootElement
public class PurchaseOrderCommand extends PelicanPojo {

    private String commandType;
    private Payment payment;
    private String finalExportControlStatus;
    private String declineReason;

    /**
     * @return the commandType
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * @param commandType the commandType to set
     */
    @XmlElement
    public void setCommandType(final String commandType) {
        this.commandType = commandType;
    }

    /**
     * @return the payment
     */
    public Payment getPayment() {
        return payment;
    }

    /**
     * @param payment the payment to set
     */
    @XmlElement
    public void setPayment(final Payment payment) {
        this.payment = payment;
    }

    /**
     * @return the finalExportControlStatus
     */
    public String getFinalExportControlStatus() {
        return finalExportControlStatus;
    }

    /**
     * @param payment the finalExportControlStatus to set
     */
    @XmlElement(name = "finalExportControlStatus")
    public void setFinalExportControlStatus(final String finalExportControlStatus) {
        this.finalExportControlStatus = finalExportControlStatus;
    }

    /**
     * @return the declineReason
     */
    public String getDeclineReason() {
        return declineReason;
    }

    /**
     * @param declineReason
     */
    @XmlElement(name = "declineReason")
    public void setDeclineReason(final String declineReason) {
        this.declineReason = declineReason;
    }
}
