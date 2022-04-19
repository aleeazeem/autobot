package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

public class CreditCard {

    private String firstName;
    private String middleName;
    private String surname;
    private String billToStreetAddress;
    private String city;
    private String stateProvince;
    private String billToZipCode;
    private String countryCode;

    private String creditCardNumber;
    private String creditCardType; // enum?
    private String expDate;
    private String securityCode;
    private String phoneNumber;

    public String getFirstName() {
        return firstName;
    }

    @XmlElement
    public void setFirstName(final String name) {
        this.firstName = name;
    }

    public String getSurname() {
        return surname;
    }

    public String getMiddleName() {
        return middleName;
    }

    @XmlElement(name = "middleName")
    public void setMiddleName(final String name) {
        this.middleName = name;
    }

    @XmlElement
    public void setSurname(final String name) {
        this.surname = name;
    }

    public String getStreetAddress() {
        return billToStreetAddress;
    }

    @XmlElement(name = "billToStreetAddress")
    public void setStreetAddress(final String value) {
        this.billToStreetAddress = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String value) {
        this.city = value;
    }

    public String getState() {
        return stateProvince;

    }

    @XmlElement(name = "stateProvince")
    public void setState(final String value) {
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

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    @XmlElement
    public void setCreditCardNumber(final String value) {
        this.creditCardNumber = value;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    @XmlElement
    public void setCreditCardType(final String type) {
        this.creditCardType = type;
    }

    public String getExpirationDate() {
        return expDate;
    }

    @XmlElement(name = "expDate")
    public void setExpirationDate(final String value) {
        this.expDate = value;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(final String value) {
        this.securityCode = value;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @XmlElement(name = "phoneNumber")
    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
