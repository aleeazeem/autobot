package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.List;

/**
 * This class represents the JSON object of subscriptions data
 *
 * @author t_mohag
 */
public class JSubscriptionsData {
    private EntityType type;
    private List<Subscription> subscriptions;
    private int startIndex;
    private int blockSize;
    private boolean skipCount;
    private Integer total;

    public EntityType getType() {
        return type;
    }

    public void setType(final EntityType type) {
        this.type = type;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(final List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(final int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isSkipCount() {
        return skipCount;
    }

    public void setSkipCount(final boolean skipCount) {
        this.skipCount = skipCount;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(final Integer total) {
        this.total = total;
    }
}
