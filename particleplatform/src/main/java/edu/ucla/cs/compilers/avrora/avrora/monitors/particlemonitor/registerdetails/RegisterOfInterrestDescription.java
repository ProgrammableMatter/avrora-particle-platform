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
public class RegisterOfInterrestDescription {

    private Map<String, List<String>> enums;
    private Map<String, StructDefinition> structs;
    private Map<String, Integer> sizeofTypes;

    public Map<String, List<String>> getEnums() {
        return enums;
    }

    public void setEnums(Map<String, List<String>> enums) {
        this.enums = enums;
    }

    public Map<String, StructDefinition> getStructs() {
        return structs;
    }

    public void setStructs(Map<String, StructDefinition> structs) {
        this.structs = structs;
    }

    public Map<String, Integer> getSizeofTypes() {
        return sizeofTypes;
    }

    public void setSizeofTypes(Map<String, Integer> sizeofTypes) {
        this.sizeofTypes = sizeofTypes;
    }

    public static class StructDefinition {
        private List<String> propertyTypes;
        private List<String> properties;
        private List<Integer> propertyAddresses;

        public List<Integer> getPropertyAddresses() {
            return propertyAddresses;
        }

        public void setPropertyAddresses(List<Integer> propertyAddresses) {
            this.propertyAddresses = propertyAddresses;
        }

        public List<String> getPropertyTypes() {
            return propertyTypes;
        }

        public void setPropertyTypes(List<String> propertyTypes) {
            this.propertyTypes = propertyTypes;
        }

        public List<String> getProperties() {
            return properties;
        }

        public void setProperties(List<String> properties) {
            this.properties = properties;
        }
    }
}