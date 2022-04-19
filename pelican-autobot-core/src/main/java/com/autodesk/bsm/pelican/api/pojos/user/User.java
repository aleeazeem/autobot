package com.autodesk.bsm.pelican.api.pojos.user;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User pojo
 *
 * @author Shweta Hegde
 */
@XmlRootElement
public class User extends PelicanPojo {

    private String id;
    private String name;
    private String externalKey;
    private String applicationFamily;

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute(name = "externalKey")
    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    public String getApplicationFamily() {
        return applicationFamily;
    }

    @XmlAttribute(name = "applicationFamily")
    public void setApplicationFamily(final String value) {
        this.applicationFamily = value;
    }
}
