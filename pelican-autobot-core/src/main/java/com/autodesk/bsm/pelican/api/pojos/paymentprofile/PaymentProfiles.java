package com.autodesk.bsm.pelican.api.pojos.paymentprofile;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "paymentProfiles")
public class PaymentProfiles extends PelicanPojo {

    private List<PaymentProfile> paymentProfiles;
    private String total;
    private String startIndex;

    public List<PaymentProfile> getPaymentProfile() {
        if (paymentProfiles == null) {
            paymentProfiles = new ArrayList<>();
        }
        return paymentProfiles;
    }

    @XmlElement(name = "paymentProfile")
    public void setPaymentProfiles(final List<PaymentProfile> paymentProfiles) {
        this.paymentProfiles = paymentProfiles;
    }

    public List<PaymentProfile> getPaymentProfiles() {
        return paymentProfiles;
    }

    public String getTotal() {
        return total;
    }

    @XmlAttribute(name = "total")
    public void setTotal(final String total) {
        this.total = total;
    }

    public String getStartIndex() {
        return startIndex;
    }

    @XmlAttribute(name = "startIndex")
    public void setStartIndex(final String startIndex) {
        this.startIndex = startIndex;
    }

}
