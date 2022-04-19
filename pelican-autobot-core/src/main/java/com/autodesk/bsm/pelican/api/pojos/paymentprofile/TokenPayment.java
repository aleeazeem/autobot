package com.autodesk.bsm.pelican.api.pojos.paymentprofile;

import javax.xml.bind.annotation.XmlElement;

public class TokenPayment {

    private String paymentMethod;
    private String firstName;
    private String middleName;
    private String surname;
    private String billToStreetAddress;
    private String city;
    private String stateProvince;
    private String billToZipCode;
    private String countryCode;
    private String phoneNumber;
    private String token;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @XmlElement
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getFirstName() {
        return firstName;
    }

    @XmlElement
    public void setFirstName(final String name) {
        this.firstName = name;
    }

    public String getMiddleName() {
        return middleName;
    }

    @XmlElement
    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getSurname() {
        return surname;
    }

    @XmlElement
    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getBillToStreetAddress() {
        return billToStreetAddress;
    }

    @XmlElement(name = "billToStreetAddress")
    public void setBillToStreetAddress(final String value) {
        this.billToStreetAddress = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String value) {
        this.city = value;
    }

    public String getStateProvince() {
        return stateProvince;

    }

    @XmlElement(name = "stateProvince")
    public void setStateProvince(final String value) {
        this.stateProvince = value;
    }

    public String getZipCode() {
        return billToZipCode;
    }

    @XmlElement(name = "billToZipCode")
    public void setZipCode(final String value) {
        this.billToZipCode = value;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @XmlElement
    public void setCountryCode(final String value) {
        this.countryCode = value;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @XmlElement
    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    @XmlElement
    public void setToken(final String token) {
        this.token = token;
    }

}
