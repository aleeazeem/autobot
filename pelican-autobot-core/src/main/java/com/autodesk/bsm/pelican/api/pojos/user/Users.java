package com.autodesk.bsm.pelican.api.pojos.user;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "users")
public class Users extends PelicanPojo {

    private List<User> users;

    public List<User> getUsers() {
        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    @XmlElement(name = "user")
    public void setUsers(final List<User> users) {
        this.users = users;
    }
}
