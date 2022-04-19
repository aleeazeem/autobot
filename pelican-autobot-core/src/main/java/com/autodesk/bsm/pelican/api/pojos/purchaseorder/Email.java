package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class contains setter and getter methods for 'email' element
 *
 * @author Shweta Hegde
 */
public class Email {

    private String template;
    private String referenceId;

    public String getTemplate() {
        return template;
    }

    @XmlAttribute(name = "template")
    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getReferenceId() {
        return referenceId;
    }

    @XmlAttribute(name = "referenceId")
    public void setReferenceId(final String referenceId) {
        this.referenceId = referenceId;
    }

}
