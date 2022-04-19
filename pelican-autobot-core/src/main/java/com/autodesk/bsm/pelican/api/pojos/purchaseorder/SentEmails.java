package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class contains setter and getter methods of 'SentEmails' element
 *
 * @author Shweta Hegde
 */
public class SentEmails {

    private List<Email> email;

    public List<Email> getEmail() {
        return email;
    }

    @XmlElement(name = "email")
    public void setEmail(final List<Email> email) {
        this.email = email;
    }
}
