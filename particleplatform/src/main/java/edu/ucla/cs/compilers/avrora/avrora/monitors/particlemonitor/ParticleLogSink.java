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
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that accepts logs that are written to {@link #logFile} for external analysis of monitoring results or
 * simulation events. It is not meant to be used for logging in general.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticleLogSink {

    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleLogSink.class.getName());
    private static ParticleLogSink Instance;
    private static String absoluteFileName = "";

    static {
        absoluteFileName = System.getProperty("java.io.tmpdir") + "/particle-state.log";
    }

    private final Object mutex = new Object();
    private FileLock lock = null;
    private boolean isLoggingEnabled = false;
    private File logFile;
    private FileWriter writer;

    private ParticleLogSink() {
        try {

            LOGGER.info("constructing particle log sink");
            logFile = new File(absoluteFileName);
            FileChannel channel = new RandomAccessFile(logFile, "rw").getChannel();

            try {
                lock = channel.tryLock();
            } catch (ClosedChannelException cce) {
                LOGGER.error("failed to lock log file due to channel exception", cce);
            } catch (OverlappingFileLockException ofe) {
                LOGGER.error("failed to lock log file due to overlapping locks", ofe);
            } catch (IOException ioe) {
                LOGGER.error("failed to lock log file", ioe);
            }

            if (!logFile.delete()) {
                LOGGER.error("failed to delete log file");
            }

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
        LOGGER.debug("created {} instance [{}]", ParticleLogSink.class.getSimpleName(), instanceCounter
                .incrementAndGet());
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

        if (!isInstanceAlive()) {
            synchronized (LOGGER) {
                if (!isInstanceAlive()) {
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
        if (isInstanceAlive()) {
            return getInstance(Instance.isLoggingEnabled);
        } else {
            return getInstance(true);
        }
    }

    /**
     * Closes resources and removes the instance reference.
     */
    public static void deleteInstance() {

        if (isInstanceAlive()) {
            synchronized (LOGGER) {
                if (isInstanceAlive()) {

                    if (null != Instance.lock) {
                        try {
                            Instance.lock.release();
                        } catch (ClosedChannelException cce) {
                            LOGGER.error("failed to release file lock due to closed channel exception", cce);
                        } catch (IOException ioe) {
                            LOGGER.error("failed to release file lock", ioe);
                        }
                        Instance.lock = null;
                    }
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
            LOGGER.debug("deleted {} instance [{}] ", ParticleLogSink.class.getSimpleName(),
                    instanceCounter.getAndDecrement());
        }
    }

    /**
     * @return the file name including the absolute path
     */

    public static String getAbsoluteFileName() {
        return absoluteFileName;
    }

    public void log(StringBuffer line) {
        log(line.toString());
    }

    public void log(StringBuilder line) {
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
}