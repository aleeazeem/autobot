package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import javax.xml.bind.annotation.XmlElement;

/**
 * /**This class is a pojo for Message entity in FulfillmentCallBack API request
 *
 * @author kishor
 */
public class Message {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    @XmlElement(name = "Code", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setCode(final String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    @XmlElement(name = "Message", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setMessage(final String message) {
        this.message = message;
    }
}
