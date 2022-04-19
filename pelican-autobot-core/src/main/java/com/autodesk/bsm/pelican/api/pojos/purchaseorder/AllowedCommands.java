package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class AllowedCommands {

    private List<String> commands;

    public List<String> getAllowedCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    @XmlElement(name = "command")
    public void setAllowedCommands(final List<String> commands) {
        this.commands = commands;
    }
}
