/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.registerdetails;

import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by rubienr on 24.03.16.
 */
public class RegisterOfInterestDescriptionTest {

    @Test
    public void test_readJson_expect_mapAbleWithoutErrors() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        RegisterOfInterestDescription registerDescription = null;
        String descriptionFileName = ParticlePlatformTestUtils.getFilePath("ParticleRegisterDescription" +
                ".json");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(descriptionFileName);
            registerDescription = mapper.readValue(file, RegisterOfInterestDescription.class);
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
