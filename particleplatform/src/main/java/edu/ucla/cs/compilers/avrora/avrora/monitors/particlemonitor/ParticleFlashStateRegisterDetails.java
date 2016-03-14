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
        readDescriptoin();
        mapAddressToTypeName();
    }

    private void mapAddressToTypeName() {
        for (Map.Entry<String, RegisterOfInterrestDescription.StructDefinition> entry : registerDescription
                .getStructs().entrySet()) {

            RegisterOfInterrestDescription.StructDefinition struct = entry.getValue();
            String structName = entry.getKey();

            int propertyPos = 0;
            for (String property : struct.getProperties()) {
                // construct the address to variable name translation map of all defined structures
                String readableProperty = (property.length() > 0) ? structName + "." + property : structName;
                addressToRegisterName.put(struct.getPropertyAddresses().get(propertyPos), readableProperty);
                // construct the sram address to type translation map
                addressToTypeName.put(struct.getPropertyAddresses().get(propertyPos), struct
                        .getPropertyTypes().get(propertyPos));
                propertyPos++;
            }
        }
    }

    private void readDescriptoin() {
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
            return "0x" + Integer.toHexString(value & 0xFF);
        }

        // if type is an enum resolve its name
        List<String> enumValueList = registerDescription.getEnums().get(type);
        if (enumValueList != null) {
            try {
                return "(" + enumValueList.get(value) + ")"; // + " 0x" + Integer.toHexString(value);
            } catch (IndexOutOfBoundsException ioobe) {
                return "(" + Integer.toHexString(value) + ")";
            }
        }

        String detailedType;
        switch (type) {
            case "bit":
                detailedType = "0b" + String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace('
                ', '0');
                break;

            case "unsignd char":
                detailedType = "" + Byte.toUnsignedInt(value);
                break;

            case "char":
                String asCharRepresentation = "";
                if ((char) value == '\n') {
                    asCharRepresentation = "\\n";
                } else if (!isPrintableChar((char) value)) {
                    asCharRepresentation = "<unprintable>";
                } else {
                    asCharRepresentation = Character.toString((char) value);
                }
                detailedType = "'" + asCharRepresentation + "'";
                break;
            case "int":
                detailedType = "" + value;
                break;

            default:
                detailedType = "0x" + Integer.toHexString(value & 0xFF);
                break;
        }
        return "(" + detailedType + ")";
    }

    /**
     * @param c the character under inspection
     * @return true if c is a printable character
     */
    private boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }
}