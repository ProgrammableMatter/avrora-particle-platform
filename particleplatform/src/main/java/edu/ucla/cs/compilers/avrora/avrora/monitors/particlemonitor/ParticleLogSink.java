/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.cck.util.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that accepts logs that are written to {@link #logFile} for external analysis.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleLogSink {

    private static final Logger LOGGER = Logger.getLogger(ParticleLogSink.class.getName());
    private static ParticleLogSink Instance;

    private boolean isLoggingEnabled = false;
    private File logFile;
    private FileWriter writer;

    private ParticleLogSink() {
        try {
            logFile = new File(System.getProperty("java.io.tmpdir") + "/particle-states.log");
            logFile.createNewFile();
            writer = new FileWriter(logFile);
        } catch (IOException ioe) {
            try {
                writer.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "failed to release resource", e);
            }
            LOGGER.log(Level.SEVERE, "failed to create log file", ioe);
        }
    }

    /**
     * @return true if there is an instance alive, else false
     */
    public static boolean isInstanceAlive() {
        return Instance != null;
    }

    /**
     * @param isParticleLogFileEnabled whether further logging of this instance should be suppressed or not
     * @return the current instance or a newly created one
     */
    public static ParticleLogSink getInstance(Option.Bool isParticleLogFileEnabled) {

        if (Instance == null) {
            synchronized (LOGGER) {
                if (Instance == null) {
                    Instance = new ParticleLogSink();
                }
            }
        }
        Instance.isLoggingEnabled = isParticleLogFileEnabled.get();
        return Instance;
    }

    /**
     * Closes resources and removes the instance reference.
     */
    public static void deleteInstance() {

        if (Instance != null) {
            synchronized (LOGGER) {
                if (Instance != null) {
                    try {
                        if (Instance.writer != null) {
                            Instance.writer.flush();
                            Instance.writer.close();
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "failed to release resource", e);
                    }
                    Instance.writer = null;
                    if (Instance.logFile != null) {
                        Instance.logFile = null;
                    }
                    Instance = null;
                }
            }
        }
    }

    public void log(StringBuffer line) {
        log(line.toString());
    }

    public void log(String line) {

        if (!isLoggingEnabled) {
            return;
        }

        if (logFile != null) {
            synchronized (logFile) {
                try {
                    writer.write(line.toString() + '\n');
                    writer.flush(); // flush needed for inotify
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "failed to add line to log", e);
                }
            }
        }
    }

    public String getAbsoluteFileName() {
        return logFile.getAbsoluteFile().toString();
    }
}