package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

/**
 * POJO for DirectDebitPayment entity for Add Payment Profile request.
 */
import javax.xml.bind.annotation.XmlElement;

public class DirectDebitPayment {

    private String paymentMethod;

    // ACH Fields
    private String accountType;
    private String accountNumber;
    private String accountNumberLastFour;
    private String routingNumber;
    private String routingNumberLastFour;

    // SEPA Fields
    private String iban;
    private String ibanFirstFour;
    private String ibanLastFour;

    // Command Fields between ACH & SEPA
    private String firstName;
    private String surname;
    private String billToStreetAddress;
    private String city;
    private String stateProvince;
    private String billToZipCode;
    private String countryCode;
    private String accountNickname;
    private String companyName;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @XmlElement(name = "paymentMethod")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAccountType() {
        return accountType;
    }

    @XmlElement(name = "accountType")
    public void setAccountType(final String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @XmlElement(name = "accountNumber")
    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumberLastFour() {
        return accountNumberLastFour;
    }

    @XmlElement(name = "accountNumber-last-four")
    public void setAccountNumberLastFour(final String accountNumberLastFour) {
        this.accountNumberLastFour = accountNumberLastFour;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    @XmlElement(name = "routingNumber")
    public void setRoutingNumber(final String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getRoutingNumberLastFour() {
        return routingNumberLastFour;
    }

    @XmlElement(name = "routingNumber-last-four")
    public void setRoutingNumberLastFour(final String routingNumberLastFour) {
        this.routingNumberLastFour = routingNumberLastFour;
    }

    public String getFirstName() {
        return firstName;
    }

    @XmlElement(name = "firstName")
    public void setFirstName(final String name) {
        this.firstName = name;
    }

    public String getSurname() {
        return surname;
    }

    @XmlElement(name = "surname")
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

    @XmlElement(name = "city")
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

    @XmlElement(name = "countryCode")
    public void setCountryCode(final String value) {
        this.countryCode = value;
    }

    public String getAccountNickname() {
        return accountNickname;
    }

    @XmlElement(name = "accountNickname")
    public void setAccountNickname(final String accountNickname) {
        this.accountNickname = accountNickname;
    }

    public String getIban() {
        return iban;
    }

    @XmlElement(name = "iban")
    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getIbanFirstFour() {
        return ibanFirstFour;
    }

    @XmlElement(name = "iban-first-four")
    public void setIbanFirstFour(final String ibanFirstFour) {
        this.ibanFirstFour = ibanFirstFour;
    }

    public String getIbanLastFour() {
        return ibanLastFour;
    }

    @XmlElement(name = "iban-last-four")
    public void setIbanLastFour(final String ibanLastFour) {
        this.ibanLastFour = ibanLastFour;
    }

    public String getCompanyName() {
        return companyName;
    }

    @XmlElement(name = "companyName")
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }

}
