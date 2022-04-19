package com.autodesk.bsm.pelican.api.pojos.bicrelease;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo that has the eligible product line code and a list of versions
 *
 * @author yin
 */
public class EligibleProduct {

    private String eligibleProductLineCode;
    private List<BicVersion> versions = new ArrayList<>();

    public String getEligibleProductLineCode() {
        return eligibleProductLineCode;
    }

    public void setEligibleProductLineCode(final String eligibleProductLineCode) {
        this.eligibleProductLineCode = eligibleProductLineCode;
    }

    public List<BicVersion> getVersions() {
        return versions;
    }

    public void setVersions(final List<BicVersion> versions) {
        this.versions = versions;
    }

    public boolean addVersion(final BicVersion version) {
        return versions.add(version);
    }
}
