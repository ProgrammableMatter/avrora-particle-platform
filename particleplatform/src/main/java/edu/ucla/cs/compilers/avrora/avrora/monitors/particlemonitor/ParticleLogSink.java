/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that accepts logs that are written to {@link #logFile} for external analysis of monitoring results or
 * simulation events. It is not meant to be used for logging in general.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleLogSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleLogSink.class.getName());
    private static ParticleLogSink Instance;
    private final Object mutex = new Object();
    private boolean isLoggingEnabled = false;
    private File logFile;
    private FileWriter writer;
    private String absoluteFileName = "";

    private ParticleLogSink() {
        try {
            absoluteFileName = System.getProperty("java.io.tmpdir") + "/particle-states.log";
            logFile = new File(absoluteFileName);
            if (!logFile.createNewFile()) {
                LOGGER.error("failed to create new log file");
            }

            writer = new FileWriter(logFile, true);
        } catch (IOException ioe) {
            try {
                writer.close();
            } catch (Exception e) {
                LOGGER.error("failed to release resource", e);
            }
            LOGGER.error("failed to create log file", ioe);
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
    public static ParticleLogSink getInstance(boolean isParticleLogFileEnabled) {

        if (Instance == null) {
            synchronized (LOGGER) {
                if (Instance == null) {
                    Instance = new ParticleLogSink();
                }
            }
        }
        Instance.isLoggingEnabled = isParticleLogFileEnabled;
        return Instance;
    }

    /**
     * convenience method for {@link #getInstance(boolean)}
     *
     * @return see {@link #getInstance(boolean)}
     */
    public static ParticleLogSink getInstance() {
        if (Instance != null) {
            return getInstance(Instance.isLoggingEnabled);
        } else {
            return getInstance(true);
        }
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
                        LOGGER.error("failed to release resource", e);
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
            synchronized (mutex) {
                try {
                    writer.write(line + '\n');
                    writer.flush(); // flush for inotify
                } catch (IOException e) {
                    LOGGER.error("failed appending line to log", e);
                }
            }
        }
    }

    /**
     * @return the file name including the absolute path
     */
    public String getAbsoluteFileName() {
        return absoluteFileName;
    }
}