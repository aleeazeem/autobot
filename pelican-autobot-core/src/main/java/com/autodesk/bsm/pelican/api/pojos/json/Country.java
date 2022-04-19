package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.Linkage;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.LinkageArray;

public class Country {
    private String country;
    private Links links;

    public static class Links {
        private LinkageArray shippingMethods;
        private Linkage priceList;

        public LinkageArray getShippingMethods() {
            return shippingMethods;
        }

        public void setShippingMethods(final LinkageArray shippingMethods) {
            this.shippingMethods = shippingMethods;
        }

        public Linkage getPriceLists() {
            return priceList;
        }

        public void setPriceLists(final Linkage priceLists) {
            this.priceList = priceLists;
        }
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

}
