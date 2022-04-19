package com.autodesk.bsm.pelican.api.pojos.paymentprofile;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.CreditCard;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.DirectDebitPayment;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PaymentProfile extends PelicanPojo {

    private String userId;
    private String userExternalKey;
    private String appFamilyId;
    private String id;
    private String name;
    private String memo;
    private String paymentProcessor;
    private TokenPayment tokenPayment;
    private RecorderPayment recorderPayment;
    private DirectDebitPayment directDebitPayment;
    private CreditCard creditCard;
    private String total;
    private String ipCountry;

    public String getUserId() {
        return userId;
    }

    @XmlAttribute(name = "userId")
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    @XmlAttribute(name = "appFamilyId")
    public void setAppFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(final String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    @XmlAttribute(name = "memo")
    public void setMemo(final String memo) {
        this.memo = memo;
    }

    public String getpaymentProcessor() {
        return paymentProcessor;
    }

    @XmlAttribute(name = "paymentProcessor")
    public void setpaymentProcessor(final String paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public TokenPayment getTokenPayment() {
        return tokenPayment;
    }

    @XmlElement(name = "tokenPayment")
    public void setTokenPayment(final TokenPayment tokenPayment) {
        this.tokenPayment = tokenPayment;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    @XmlElement(name = "creditCard")
    public void setCreditCard(final CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getTotal() {
        return total;
    }

    @XmlAttribute(name = "total")
    public void setTotal(final String total) {
        this.total = total;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    @XmlElement(name = "ipCountry")
    public void setIpCountry(final String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public RecorderPayment getRecorderPayment() {
        return recorderPayment;
    }

    @XmlElement(name = "recorderPayment")
    public void setRecorderPayment(final RecorderPayment recorderPayment) {
        this.recorderPayment = recorderPayment;
    }

    public DirectDebitPayment getDirectDebitPayment() {
        return directDebitPayment;
    }

    @XmlElement(name = "directDebitPayment")
    public void setDirectDebitPayment(final DirectDebitPayment directDebitPayment) {
        this.directDebitPayment = directDebitPayment;
    }

    public String getUserExternalKey() {
        return userExternalKey;
    }

    @XmlAttribute(name = "userExternalKey")
    public void setUserExternalKey(final String userExternalKey) {
        this.userExternalKey = userExternalKey;
    }
}
