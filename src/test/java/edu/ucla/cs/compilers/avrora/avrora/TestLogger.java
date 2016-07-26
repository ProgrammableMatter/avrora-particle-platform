/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;

/**
 * Logger that logs start end and test outcome to log file.
 */
public class TestLogger extends TestWatcher {

    private final Logger logger;

    public TestLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        logger.error("FAILED TEST: {} {}", description, e);
    }

    @Override
    protected void succeeded(Description description) {
        logger.info("PASSED TEST: {}", description);
    }

    @Override
    protected void starting(Description description) {
        logger.info("STARTING TEST: {}", description);
    }

    @Override
    protected void finished(Description description) {
        logger.info("FINISHED TEST: {}", description);
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        logger.error("SKIPPED TEST: {} {}", description, e);
    }
}
