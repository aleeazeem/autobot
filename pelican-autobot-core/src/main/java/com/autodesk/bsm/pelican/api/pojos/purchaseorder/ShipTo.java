package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.StateProvince;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ShipTo {
    private String firstName;
    private String surname;
    private String companyName;
    private String street;
    private String street2;
    private String city;
    private StateProvince state;
    private String postalCode;
    private Country country;

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

    public String getCity() {
        return city;
    }

    @XmlElement(name = "city")
    public void setCity(final String city) {
        this.city = city;
    }

    public StateProvince getState() {
        return state;
    }

    @XmlElement(name = "state")
    public void setState(final StateProvince state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @XmlElement(name = "postalCode")
    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public Country getCountry() {
        return country;
    }

    @XmlElement(name = "country")
    public void setCountry(final Country country) {
        this.country = country;
    }
}
