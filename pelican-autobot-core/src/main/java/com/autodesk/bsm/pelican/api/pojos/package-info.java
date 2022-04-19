/*
 * This is a special class which is used for namespace mapping in the XMLs with elements with namespaces
 */
@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = { @XmlNs(prefix = "pfx2", namespaceURI = "http://www.autodesk.com/schemas/Business/AssetV1.0"),
            @XmlNs(prefix = "pfx3", namespaceURI = "http://www.autodesk.com/schemas/Business/OrderV1.0") })
package com.autodesk.bsm.pelican.api.pojos;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
