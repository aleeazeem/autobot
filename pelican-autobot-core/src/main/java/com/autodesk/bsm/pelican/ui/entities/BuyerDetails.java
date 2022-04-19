package com.autodesk.bsm.pelican.ui.entities;

/**
 * Entity Pojo for Buyer details in Purchase Order details page
 *
 * @author kishor
 */
public class BuyerDetails {

    private String buyer;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String company;
    private String billingAddress;
    private String phoneNumber;
    private String lastFourDigits;

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(final String buyer) {
        this.buyer = buyer;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(final String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

}
