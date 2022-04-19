package com.autodesk.bsm.pelican.util;

import org.apache.commons.httpclient.methods.PostMethod;

public class HttpDeleteWithBody extends PostMethod {

    public HttpDeleteWithBody(final String url) {
        super(url);
    }

    @Override
    public String getName() {
        return "DELETE";
    }

}
