package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails.RegisterOfInterrestDescription;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class describes the position of the {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform .ParticlePlatform}
 * internal state.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleFlashStateRegisterDetails {

    private static final Logger LOGGER = Logger.getLogger(ParticleFlashStateRegisterDetails.class.getName());
    private static final String descriptionFileName = "ParticleStateDescription.json";
    private Map<Integer, String> addressToRegisterName = new HashMap<Integer, String>();
    private Map<Integer, String> addressToTypeName = new HashMap<Integer, String>();
    private RegisterOfInterrestDescription registerDescription = null;

    public ParticleFlashStateRegisterDetails() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        try {
            // read file from resource if project is not packed to jar
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(descriptionFileName).getFile());
            registerDescription = mapper.readValue(file, RegisterOfInterrestDescription.class);
        } catch (Exception e) {
            // read file from resource if project is packt to jar
            InputStream in = getClass().getResourceAsStream("/" + descriptionFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
                registerDescription = mapper.readValue(reader, RegisterOfInterrestDescription.class);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, "failed parse [" + descriptionFileName + "] from .jar correctly", e1);
            }
        }

        for (Map.Entry<String, RegisterOfInterrestDescription.StructDefinition> entry : registerDescription
                .getStructs().entrySet()) {

            RegisterOfInterrestDescription.StructDefinition struct = entry.getValue();
            String structName = entry.getKey();

            int propertyPos = 0;
            for (String property : struct.getProperties()) {
                // construct the address to variable name translation map of all defined structs
                addressToRegisterName.put(struct.getPropertyAddresses().get(propertyPos), structName + "." +
                        property);
                // construct the sram address to type translation map
                addressToTypeName.put(struct.getPropertyAddresses().get(propertyPos), struct.getPropertyTypes().get
                        (propertyPos));
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
     * Translates the int value to an enum field's name according to the sram address where it is to be stored.
     *
     * @param sramAddress the sram address on the microcontroller
     * @param value       the enum field's value
     * @return the enum field's name
     */
    public String toDetailedType(int sramAddress, int value) {

        String type = addressToTypeName.get(sramAddress);
        if (type == null) {
            return Integer.toHexString(value);
        }

        // if type is an enum resolve its name
        List<String> enumValueList = registerDescription.getEnums().get(type);
        if (enumValueList != null) {
            try {
                return enumValueList.get(value) + " (" + Integer.toHexString(value) + ")";
            } catch (IndexOutOfBoundsException ioobe) {
                return Integer.toHexString(value);
            }
        }

        if (type.compareTo("bit") == 0) {
            return "(0b" + String.format("%8s", Integer.toBinaryString(value & 0xff)).replace(' ', '0') + ")";
        } else if (type.compareTo("unsigned char") == 0 || type.compareTo("char") == 0 || type.compareTo("int") == 0) {
            return "(" + value + ")";
        }

        return type + " (0x" + Integer.toHexString(value) + ")";
    }
}