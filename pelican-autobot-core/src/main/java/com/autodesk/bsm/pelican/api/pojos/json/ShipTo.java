package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.StateProvince;

public class ShipTo {
    private String streetAddressLine1;
    private String streetAddressLine2;
    private String city;
    private StateProvince state;
    private Country country;

    public String getStreetAddressLine1() {
        return streetAddressLine1;
    }

    public void setStreetAddressLine1(final String streetAddressLine1) {
        this.streetAddressLine1 = streetAddressLine1;
    }

    public String getStreetAddressLine2() {
        return streetAddressLine2;
    }

    public void setStreetAddressLine2(final String streetAddressLine2) {
        this.streetAddressLine2 = streetAddressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public StateProvince getState() {
        return state;
    }

    public void setState(final StateProvince state) {
        this.state = state;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(final Country country) {
        this.country = country;
    }
}
