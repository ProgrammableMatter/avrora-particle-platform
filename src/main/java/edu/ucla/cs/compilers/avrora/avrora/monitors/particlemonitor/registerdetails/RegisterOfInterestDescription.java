/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails;

import java.util.List;
import java.util.Map;

/**
 * @author Raoul Rubien on 22.11.2015.
 */
public class RegisterOfInterestDescription {

    private Map<String, List<String>> enums;
    private Map<String, List<StructProperties>> structs;
    private Map<String, Integer> sizeofTypes;
    private Map<String, Integer> labels;

    public Map<String, Integer> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Integer> labels) {
        this.labels = labels;
    }

    public Map<String, List<String>> getEnums() {
        return enums;
    }

    public void setEnums(Map<String, List<String>> enums) {
        this.enums = enums;
    }

    public Map<String, List<StructProperties>> getStructs() {
        return structs;
    }

    public void setStructs(Map<String, List<StructProperties>> structs) {
        this.structs = structs;
    }

    public Map<String, Integer> getSizeofTypes() {
        return sizeofTypes;
    }

    public void setSizeofTypes(Map<String, Integer> sizeofTypes) {
        this.sizeofTypes = sizeofTypes;
    }

    public static class StructProperties {
        String type;
        String property;
        String address;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}