package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionClient;
import com.autodesk.bsm.pelican.api.pojos.json.UpdateSubscription;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

/**
 * This is a Util class for subscription, this can be used to create payload etc before calling SubscriptionClient.
 *
 * @author Shweta Hegde
 */
public class SubscriptionUtils {

    /**
     * This method is calling Update Patch API to update Subscription NBD. As of now it is not a generic method where it
     * can be used to update any element of Subscription. If requirement arises in future, this method needs to be
     * updated.
     *
     * @param subscriptionId
     * @param resource
     * @param appFamily
     * @param environmentVariables
     * @param date
     */
    public static void updateSubscriptionNBD(final String subscriptionId, final PelicanPlatform resource,
        final String appFamily, final EnvironmentVariables environmentVariables, final String date) {

        final Subscription subscription = resource.subscription().getById(subscriptionId);

        final UpdateSubscription.Data data = new UpdateSubscription.Data();
        data.setNextBillingDate(date);
        data.setPriceId(subscription.getPriceId());
        data.setQuantity(subscription.getQuantity());
        data.setStatus(subscription.getStatus());
        data.setDaysCredited(subscription.getCreditDays());

        final SubscriptionClient subscriptionClient = new SubscriptionClient(environmentVariables, appFamily);
        subscriptionClient.updatePatchSubscription(subscriptionId, data);
    }
}
