package com.autodesk.bsm.pelican.util;

import java.io.InputStream;

public interface HttpResponseListener {
    void setStatusCode(int code);

    int getStatusCode();

    String getStatusText();

    void setStatusText(String s);

    void header(String name, String value);

    void content(InputStream is);

    String getResponse();

    String getHeader(String name);
}
