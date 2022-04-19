package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "itemInstances")
public class ItemInstances extends PelicanPojo {

    private List<ItemInstance> itemInstances;

    public List<ItemInstance> getItemInstances() {
        return itemInstances;
    }

    @XmlElement(name = "itemInstance")
    public void setItemInstances(final List<ItemInstance> itemInstances) {
        this.itemInstances = itemInstances;
    }
}
