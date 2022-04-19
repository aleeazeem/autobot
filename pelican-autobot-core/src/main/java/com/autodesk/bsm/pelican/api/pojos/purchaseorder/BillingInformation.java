package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.enums.Country;

import javax.xml.bind.annotation.XmlElement;

/**
 * The pojo for billingInformation entity for Submit PO request XML
 *
 * @author kishor
 */
public class BillingInformation {
    private String firstName;
    private String surname;
    private String companyName;
    private String street;
    private String street2;
    private String postalCode;
    private String city;
    private String stateProvince;
    private String country;
    private String phone;
    private String vatRegistrationId;
    private String paymentMethod;
    private String debitType;
    private String cardType;
    private String expDate;
    private String last4Digits;
    private Country countryCode;

    public String getFirstName() {
        return firstName;
    }

    @XmlElement(name = "firstName")
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    @XmlElement(name = "surname")
    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getCompanyName() {
        return companyName;
    }

    @XmlElement(name = "companyName")
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }

    public String getStreet() {
        return street;
    }

    @XmlElement(name = "street")
    public void setStreet(final String street) {
        this.street = street;
    }

    public String getStreet2() {
        return street2;
    }

    @XmlElement(name = "street2")
    public void setStreet2(final String street2) {
        this.street2 = street2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @XmlElement(name = "postalCode")
    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    @XmlElement(name = "city")
    public void setCity(final String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    @XmlElement(name = "stateProvince")
    public void setStateProvince(final String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    @XmlElement(name = "countryCode")
    public void setCountry(final String country) {
        this.country = country;
    }

    public Country getCountryCode() {
        return countryCode;
    }

    @XmlElement(name = "countryCode")
    public void setCountryCode(final Country countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    @XmlElement(name = "phone")
    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getVatRegistrationId() {
        return vatRegistrationId;
    }

    @XmlElement(name = "vatRegistrationId")
    public void setVatRegistrationId(final String vatRegistrationId) {
        this.vatRegistrationId = vatRegistrationId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @XmlElement(name = "paymentMethod")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardType() {
        return cardType;
    }

    @XmlElement(name = "cardType")
    public void setCardType(final String cardType) {
        this.cardType = cardType;
    }

    public String getExpDate() {
        return expDate;
    }

    @XmlElement(name = "expDate")
    public void setExpDate(final String expDate) {
        this.expDate = expDate;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    @XmlElement(name = "last4Digits")
    public void setLast4Digits(final String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public String getDebitType() {
        return debitType;
    }

    @XmlElement(name = "debitType")
    public void setDebitType(final String debitType) {
        this.debitType = debitType;
    }

}
