package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class is a pojo for ListOfMessage entity in FulfillmentCallBack API request
 *
 * @author kishor
 */
public class ListOfMessage {

    private List<Message> listOfMessage;

    /**
     * @return the listOfMessage
     */
    public List<Message> getListOfMessage() {
        return listOfMessage;
    }

    /**
     * @param listOfMessage the listOfMessage to set
     */
    @XmlElement(name = "Message", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setListOfMessage(final List<Message> listOfMessage) {
        this.listOfMessage = listOfMessage;
    }
}
