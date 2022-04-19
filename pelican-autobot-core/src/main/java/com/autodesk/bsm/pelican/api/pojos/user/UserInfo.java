package com.autodesk.bsm.pelican.api.pojos.user;

/**
 * User Info POJO to support GDPR Request.
 *
 * @author t_joshv
 *
 */
public class UserInfo {

    private String id;
    private String email;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
