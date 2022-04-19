package com.autodesk.bsm.pelican.api.pojos.subscription;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription.Included;

import java.util.List;

/**
 * Json object for get subscription owners api.
 *
 * @author jains
 *
 */
public class SubscriptionOwners extends PelicanPojo {

    private List<SubscriptionOwnerData> data;
    private Included included;
    private List<Errors> errors;
    private Links links;

    public static class Links {
        private String prev;
        private String next;

        public String getPrev() {
            return prev;
        }

        public void setPrev(final String prev) {
            this.prev = prev;
        }

        public String getNext() {
            return next;
        }

        public void setNext(final String next) {
            this.next = next;
        }
    }

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(final Included included) {
        this.included = included;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public List<SubscriptionOwnerData> getData() {
        return data;
    }

    public void setData(final List<SubscriptionOwnerData> data) {
        this.data = data;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }
}
