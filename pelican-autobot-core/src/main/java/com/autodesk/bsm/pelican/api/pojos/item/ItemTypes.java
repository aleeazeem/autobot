package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "itemTypes")
public class ItemTypes extends PelicanPojo {

    private List<ItemType> itemTypes;
    private int startIndex;
    private int blockSize;
    private Integer total;

    public List<ItemType> getItemTypes() {
        if (itemTypes == null) {
            itemTypes = new ArrayList<>();
        }
        return itemTypes;
    }

    @XmlElement(name = "itemType")
    public void setItemTypes(final List<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @XmlAttribute
    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }

    public int getBlockSize() {
        return blockSize;
    }

    @XmlAttribute
    public void setBlockSize(final int blockSize) {
        this.blockSize = blockSize;
    }

    public Integer getTotal() {
        return total;
    }

    @XmlAttribute
    public void setTotal(final Integer total) {
        this.total = total;
    }
}
