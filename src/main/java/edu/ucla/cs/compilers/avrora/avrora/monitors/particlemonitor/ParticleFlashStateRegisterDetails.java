/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails
        .RegisterOfInterestDescription;
import edu.ucla.cs.compilers.avrora.avrora.sim.AtmelInterpreter;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class describes the position of the {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform
 * .ParticlePlatform} internal state.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleFlashStateRegisterDetails {

    private static final String descriptionFileName = "ParticleRegisterDescription.json";
    private final Logger logger = LoggerFactory.getLogger(ParticleFlashStateRegisterDetails.class.getName());
    private Map<Integer, String> addressToRegisterName = new HashMap<>();
    private Map<Integer, String> addressToTypeName = new HashMap<>();
    private RegisterOfInterestDescription registerDescription = null;

    /**
     * Reads description from {@link #descriptionFileName} file.
     */
    public ParticleFlashStateRegisterDetails() {
        readDescription();
        mapAddressToTypeName();
    }

    /**
     * @return a predefined mapping of microcontroller addresses to their variable names in source.
     */
    public Map<Integer, String> getAddressToRegisterNameMapping() {
        return addressToRegisterName;
    }

    private void mapAddressToTypeName() {
        for (Map.Entry<String, List<RegisterOfInterestDescription.StructProperties>> entry :
                registerDescription.getStructs().entrySet()) {

            List<RegisterOfInterestDescription.StructProperties> properties = entry.getValue();
            String structName = entry.getKey();

            for (RegisterOfInterestDescription.StructProperties property : properties) {

                // construct the address to variable name translation map of all defined structures
                String readableProperty = (property.getProperty().length() > 0) ? structName + "." +
                        property.getProperty() : structName;
                addressToRegisterName.put(toIntegerAddress(property.getAddress(), registerDescription
                        .getLabels()), readableProperty);

                // construct the SRAM address to type translation map
                addressToTypeName.put(toIntegerAddress(property.getAddress(), registerDescription.getLabels
                        ()), property.getType());
            }
        }
    }

    /**
     * Translates from address in string form to integer address according to the specified address labels. An
     * arbitrary address may occur as <b>"^([a-zA-Z]*)([+]*)([0-9]*)$"</b>:<br/> "60" address at byte 60<br/>
     * "label+60" - address at byte 60 after "label"<br/> "label" - address "label"<br/>
     *
     * @param address       the address as specified above
     * @param addressLabels label name to address mapping
     * @return the resolved address value as int
     * @throws IllegalArgumentException if the address cannot be parsed or the label cannot be found
     */
    private int toIntegerAddress(String address, Map<String, Integer> addressLabels) throws
            IllegalArgumentException {
        Pattern labelPattern = Pattern.compile("^([a-zA-Z]*)([+]*)([0-9]*)$");
        Matcher m = labelPattern.matcher(address);

        if (m.matches()) {
            String label = m.group(1);
            Integer labelValue = null;
            if (label != null) {
                labelValue = addressLabels.get(label);
            }

            String offset = m.group(3);
            Integer offsetValue = null;
            if (offset != null) {
                offsetValue = Integer.parseInt(offset);
            }

            String operator = m.group(2);
            if (labelValue != null && operator != null && offsetValue != null) {
                return labelValue + offsetValue;
            } else if (labelValue != null) {
                return labelValue;
            } else if (offsetValue != null) {
                return offsetValue;
            }
        }

        throw new IllegalArgumentException("cannot resolve address [" + address + "]");
    }

    private void readDescription() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        try {
            // read file from resource if project is not packed to jar
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL url = classLoader.getResource(descriptionFileName);
            if (url != null) {
                File file = new File(url.getFile());
                registerDescription = mapper.readValue(file, RegisterOfInterestDescription.class);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            // read file from resource if project is packed to jar
            InputStream in = getClass().getResourceAsStream("/" + descriptionFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
                registerDescription = mapper.readValue(reader, RegisterOfInterestDescription.class);
            } catch (IOException e1) {
                logger.error("failed parse [" + descriptionFileName + "] from .jar correctly", e1);
            }
        }
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

    /**
     * Translates the int value to an enum field's name according to the sram address where it is to be
     * stored. Note I: compile with "-fshort-enums" for 8bit enums. Note II: values of byte are in (-128, ...,
     * 127) but enums in (0, ..., 255). Note III: current implementation evaluates only 1-byte fields.
     *
     * @param sramAddress the sram address on the microcontroller
     * @param value       The enum field's value.
     */
    String toDetailedType(int sramAddress, byte value, AtmelInterpreter.StateImpl state) {
        // TODO: also use the RegisterOfInterestDescription.getSizeofTypes() to determine the field size
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
        int intValue;
        switch (type) {
            // int8_t
            case "bit":
                detailedType = "0b" + String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace
                        (/**/' ', '0');
                break;

            case "dbit":
                detailedType = "0b" + String.format("%8s", Integer.toBinaryString(state.getDataByte
                        (sramAddress + 1) & 0xff)).replace(' ', '0') + String.format("%8s", Integer
                        .toBinaryString(value & 0xFF)).replace(' ', '0');
                break;

            // uint8_t
            case "unsigned":
                detailedType = "" + Byte.toUnsignedInt(value);
                break;

            case "char":
                String asCharRepresentation;
                if ((char) value == '\n') {
                    asCharRepresentation = "\\n";
                } else if (!isPrintableChar((char) value)) {
                    asCharRepresentation = "<unprintable char code [" + (int) value + "]>";
                } else {
                    asCharRepresentation = Character.toString((char) value);
                }
                detailedType = "'" + asCharRepresentation + "'";
                break;

            // int8_t
            case "signed":
                detailedType = "" + value;
                break;

            // int16_t
            case "int":
            case "signed int":
                intValue = ((state.getDataByte(sramAddress + 1) & 0xff) << 8) | (value & 0xff);
                if ((intValue & (1 << 15)) != 0) {
                    detailedType = "";
                } else {
                    detailedType = "-";
                }
                detailedType += (intValue & 0x7FFF);
                break;

            // uint16_t
            case "uint":
            case "unsigned int":
                intValue = ((state.getDataByte(sramAddress + 1) & 0xff) << 8) | (value & 0xff);
                detailedType = "" + intValue;
                break;

            // int16_t
            case "dhex":
            case "hex16":
                detailedType = "0x" + Integer.toHexString(state.getDataByte(sramAddress + 1) & 0xff) + " "
                        + Integer.toHexString(value & 0xff);
                break;

            // int8_t
            default:
                logger.warn("unrecognized type specified [{}]", type);
            case "hex":
                detailedType = "0x" + Integer.toHexString(value & 0xFF);
                break;
        }
        return "(" + detailedType + ")";
    }
}