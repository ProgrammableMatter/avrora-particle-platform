/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails
        .RegisterOfInterrestDescription;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class describes the position of the {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform
 * .ParticlePlatform} internal state.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleFlashStateRegisterDetails {

    private static final Logger LOGGER = Logger.getLogger(ParticleFlashStateRegisterDetails.class.getName());
    private static final String descriptionFileName = "ParticleStateDescription.json";
    private Map<Integer, String> addressToRegisterName = new HashMap<>();
    private Map<Integer, String> addressToTypeName = new HashMap<>();
    private RegisterOfInterrestDescription registerDescription = null;

    /**
     * Reads description from {@link #descriptionFileName} file.
     *
     * @throws Exception if file cannot be read or interpreted
     */
    public ParticleFlashStateRegisterDetails() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        try {
            // read file from resource if project is not packed to jar
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(descriptionFileName).getFile());
            registerDescription = mapper.readValue(file, RegisterOfInterrestDescription.class);
        } catch (Exception e) {
            // read file from resource if project is packed to jar
            InputStream in = getClass().getResourceAsStream("/" + descriptionFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
                registerDescription = mapper.readValue(reader, RegisterOfInterrestDescription.class);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, "failed parse [" + descriptionFileName + "] from .jar correctly",
                        e1);
            }
        }

        for (Map.Entry<String, RegisterOfInterrestDescription.StructDefinition> entry : registerDescription
                .getStructs().entrySet()) {

            RegisterOfInterrestDescription.StructDefinition struct = entry.getValue();
            String structName = entry.getKey();

            int propertyPos = 0;
            for (String property : struct.getProperties()) {
                // construct the address to variable name translation map of all defined structures
                if (property.length() > 0) {
                    addressToRegisterName.put(struct.getPropertyAddresses().get(propertyPos), structName +
                            "." +
                            property);
                } else {
                    addressToRegisterName.put(struct.getPropertyAddresses().get(propertyPos), structName);
                }
                // construct the sram address to type translation map
                addressToTypeName.put(struct.getPropertyAddresses().get(propertyPos), struct
                        .getPropertyTypes().get(propertyPos));
                propertyPos++;
            }
        }
    }

    /**
     * @return a predefined mapping of microcontroller addresses to their variable names in source.
     */
    public Map<Integer, String> getAddressToRegisterNameMapping() {
        return addressToRegisterName;
    }

    /**
     * Translates the int value to an enum field's name according to the sram address where it is to be
     * stored. Note I: compile with "-fshort-enums" for 8bit enums. Note II: values of byte are in (-128, ...,
     * 127) but enums in (0, ..., 255). Note III: current implementation evaluates only 1-byte fields.
     *
     * @param sramAddress the sram address on the microcontroller
     * @param value       The enum field's value.
     */
    public String toDetailedType(int sramAddress, byte value) {

        // TODO: also use the RegisterOfInterrestDescription.getSizeofTypes() to determine the field size

        String type = addressToTypeName.get(sramAddress);
        if (type == null) {
            return Integer.toHexString(value);
        }

        // if type is an enum resolve its name
        List<String> enumValueList = registerDescription.getEnums().get(type);
        if (enumValueList != null) {
            try {
                return enumValueList.get(value) + " 0x" + Integer.toHexString(value) + "";
            } catch (IndexOutOfBoundsException ioobe) {
                return Integer.toHexString(value);
            }
        }

        if (type.compareTo("bit") == 0) {
            return "(0b" + String.format("%8s", Integer.toBinaryString(value & 0xff)).replace(' ', '0') + ")";
        } else if (type.compareTo("unsigned char") == 0) {
            return "(" + Byte.toUnsignedInt(value) + ")";
        } else if (type.compareTo("int") == 0) {
            return "(" + value + ")";
        } else if (type.compareTo("char") == 0) {
            String asCharRepresentation = "";
            if ((char) value == '\n') {
                asCharRepresentation = "\\n";
            } else {
                asCharRepresentation = Character.toString((char) value);
            }
            return "('" + asCharRepresentation + "')";
        }
        return type + " (0x" + Integer.toHexString(value) + ")";
    }
}