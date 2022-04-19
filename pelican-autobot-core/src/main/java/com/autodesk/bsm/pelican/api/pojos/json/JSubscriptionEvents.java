package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionOwners.Links;

import java.util.List;

/**
 * This class represents the JSON object of the request to get subscription events.
 *
 * @author Muhammad
 *
 */
public class JSubscriptionEvents extends PelicanPojo {
    private List<Errors> errors;
    private List<SubscriptionEventsData> eventsData;
    private Links links;

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public List<SubscriptionEventsData> getEventsData() {
        return eventsData;
    }

    public void setEventsData(final List<SubscriptionEventsData> eventsData) {
        this.eventsData = eventsData;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

}
