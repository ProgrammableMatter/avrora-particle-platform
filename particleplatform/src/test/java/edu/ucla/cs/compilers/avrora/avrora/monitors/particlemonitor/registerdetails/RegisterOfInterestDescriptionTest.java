/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails;

import at.tugraz.iti.SimulationTestUtils;
import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTest;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rubienr on 24.03.16.
 */
public class RegisterOfInterestDescriptionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformTest.class);
    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();
    }

    @Test
    public void test_readJson_expect_mapAbleWithoutErrors() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        RegisterOfInterestDescription registerDescription = null;
        String descriptionFileName = SimulationTestUtils.getFilePath("ParticleRegisterDescription" +
                ".json");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(descriptionFileName);
            registerDescription = mapper.readValue(file, RegisterOfInterestDescription.class);
            int foo = 1;
        } catch (Exception e) {
            assertTrue(false);
        }

        assertNotNull(registerDescription);
        assertTrue(registerDescription.getEnums().containsKey("StateType"));
        assertTrue(registerDescription.getEnums().containsKey("NodeType"));
        assertTrue(registerDescription.getStructs().containsKey("A.in"));
        assertTrue(registerDescription.getStructs().containsKey("A.dir"));
        assertTrue(registerDescription.getStructs().containsKey("A.out"));
        assertTrue(registerDescription.getStructs().containsKey("B.in"));
        assertTrue(registerDescription.getStructs().containsKey("B.dir"));
        assertTrue(registerDescription.getStructs().containsKey("B.out"));
        assertTrue(registerDescription.getStructs().containsKey("C.in"));
        assertTrue(registerDescription.getStructs().containsKey("C.dir"));
        assertTrue(registerDescription.getStructs().containsKey("C.out"));
        assertTrue(registerDescription.getStructs().containsKey("D.in"));
        assertTrue(registerDescription.getStructs().containsKey("D.dir"));
        assertTrue(registerDescription.getStructs().containsKey("D.out"));
//        assertTrue(registerDescription.getStructs().containsKey("UDR"));
        assertTrue(registerDescription.getStructs().containsKey("char-out"));
        assertTrue(registerDescription.getStructs().containsKey("GICR"));
        assertTrue(registerDescription.getStructs().containsKey("TCCR1A"));
        assertTrue(registerDescription.getStructs().containsKey("TCCR1B"));
        assertTrue(registerDescription.getStructs().containsKey("OCR1AH"));
        assertTrue(registerDescription.getStructs().containsKey("OCR1AL"));
        assertTrue(registerDescription.getStructs().containsKey("MCUCR"));
        assertTrue(registerDescription.getStructs().containsKey("MCUCSR"));
        assertTrue(registerDescription.getStructs().containsKey("TCNT0"));
        assertTrue(registerDescription.getStructs().containsKey("TCNT1H"));
        assertTrue(registerDescription.getStructs().containsKey("TCNT1L"));
        assertTrue(registerDescription.getStructs().containsKey("TIMSK"));
        assertTrue(registerDescription.getStructs().containsKey("GIFR"));
        assertTrue(registerDescription.getStructs().containsKey("SREG"));
        assertNotNull(registerDescription.getLabels());
        assertNotNull(registerDescription.getSizeofTypes());
    }
}
