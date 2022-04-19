package com.autodesk.bsm.pelican.api.pojos.json;

import java.util.Map;

/**
 * This class represents the JSON object of Descriptors.
 *
 * @author t_mohag
 */
public class Descriptors {

    private IPP ipp;
    private EStore estore;
    private Map<String, String> other;

    public static class IPP {
        private Map<String, String> properties;

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(final Map<String, String> properties) {
            this.properties = properties;
        }
    }

    public static class EStore {
        private Map<String, String> properties;

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(final Map<String, String> properties) {
            this.properties = properties;
        }
    }

    public IPP getIpp() {
        return ipp;
    }

    public void setIpp(final IPP ipp) {
        this.ipp = ipp;
    }

    public EStore getEstore() {
        return estore;
    }

    public void setEstore(final EStore estore) {
        this.estore = estore;
    }

    public Map<String, String> getOther() {
        return other;
    }

    public void setOther(final Map<String, String> other) {
        this.other = other;
    }
}
