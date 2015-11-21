package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

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
 * @author Raoul Rubien on 20.11.15.
 */
public class ParticleFlashStateRegisterDetails {

    private static final Logger LOGGER = Logger.getLogger(ParticleFlashStateRegisterDetails.class.getName());
    private static final String descriptionFileName = "ParticleStateDescription.json";
    private Map<Integer, String> addressToRegisterName = new HashMap<Integer, String>();

    public ParticleFlashStateRegisterDetails() {

        RegisterOfInterrestDetails registerDetails = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        try {
            // read file from resource if project is not packed to jar
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(descriptionFileName).getFile());
            registerDetails = mapper.readValue(file, RegisterOfInterrestDetails.class);
        } catch (Exception e) {
            // read file from resource if project is packt to jar
            InputStream in = getClass().getResourceAsStream("/" + descriptionFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
                registerDetails = mapper.readValue(reader, RegisterOfInterrestDetails.class);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, "failed parse [" + descriptionFileName + "] from .jar correctly", e1);
            }
        }

        int startAddress = registerDetails.addresses.get("globalState");
        for (String structFieldName : registerDetails.getGlobalState().keySet()) {
            addressToRegisterName.put(startAddress, "GlobalState." + structFieldName);
            String fieldType = registerDetails.globalState.get(structFieldName);
            startAddress += registerDetails.sizeofTypes.get(fieldType);
        }
    }

    public Map<Integer, String> getAddressToRegisterMapping() {
        return addressToRegisterName;
    }

    private static class RegisterOfInterrestDetails {

        private List<String> stateType;
        private List<String> nodeType;
        private Map<String, String> globalState;
        private Map<String, Integer> addresses;
        private Map<String, Integer> sizeofTypes;

        public Map<String, Integer> getSizeofTypes() {
            return sizeofTypes;
        }

        public void setSizeofTypes(Map<String, Integer> sizeofTypes) {
            this.sizeofTypes = sizeofTypes;
        }

        public Map<String, Integer> getAddresses() {
            return addresses;
        }

        public void setAddresses(Map<String, Integer> addresses) {
            this.addresses = addresses;
        }

        public Map<String, String> getGlobalState() {
            return globalState;
        }

        public void setGlobalState(Map<String, String> globalState) {
            this.globalState = globalState;
        }

        public List<String> getStateType() {
            return stateType;
        }

        public void setStateType(List<String> stateType) {
            this.stateType = stateType;
        }

        public List<String> getNodeType() {
            return nodeType;
        }

        public void setNodeType(List<String> nodeType) {
            this.nodeType = nodeType;
        }
    }
}