package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * @author Muhammad
 */
public class Pagination {
    private String startIndex;
    private String blockSize;
    private String count;
    private String skipCount;

    public String getstartIndex() {
        return startIndex;
    }

    public void setstartIndex(final String startIndex) {
        this.startIndex = startIndex;
    }

    public String getblockSize() {
        return blockSize;
    }

    public void setblockSize(final String blockSize) {
        this.blockSize = blockSize;
    }

    public String getCount() {
        return count;
    }

    public void setCount(final String count) {
        this.count = count;
    }

    public String getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(final String skipCount) {
        this.skipCount = skipCount;
    }

}
