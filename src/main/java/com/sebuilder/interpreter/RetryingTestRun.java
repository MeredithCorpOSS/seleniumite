package com.sebuilder.interpreter;

import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A simplistic extension to the TestRun class that allows a given step to be retried if a predicate is satisfied.
 *
 * Created by cweiss1271 on 5/17/16.
 * @see TestRun
 */
public class RetryingTestRun extends TestRun {
    private static final Logger LOG = LoggerFactory.getLogger(RetryingTestRun.class);

    /**
     * See parent constructor
     * @param script Script object to run
     */
    public RetryingTestRun(Script script) {
        super(script);
    }


    /**
     * See parent constructor
     * @param script Script object to run
     * @param implicitlyWaitDriverTimeout int containing the number of seconds to wait before giving up trying to get a webdriver
     * @param pageLoadDriverTimeout int containing the number of seconds to wait for a page to load
     */
    public RetryingTestRun(Script script, int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout) {
        super(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param implicitlyWaitDriverTimeout int containing the number of seconds to wait before giving up trying to get a webdriver
     * @param pageLoadDriverTimeout int containing the number of seconds to wait for a page to load
     * @param initialVars Map containing any initial variables
     */
    public RetryingTestRun(Script script, int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout, Map<String, String> initialVars) {
        super(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     */
    public RetryingTestRun(Script script, Log log) {
        super(script, log);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     * @param webDriverFactory WebDriverFactory to create webdrivers
     * @param webDriverConfig Map containing configuration for the webdriver
     */
    public RetryingTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig) {
        super(script, log, webDriverFactory, webDriverConfig);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     * @param initialVars Map containing any initial variables
     * @param webDriverFactory WebDriverFactory to create webdrivers
     * @param webDriverConfig Map containing configuration for the webdriver
     */
    public RetryingTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Map<String, String> initialVars) {
        super(script, log, webDriverFactory, webDriverConfig, initialVars);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     * @param implicitlyWaitDriverTimeout int containing the number of seconds to wait before giving up trying to get a webdriver
     * @param pageLoadDriverTimeout int containing the number of seconds to wait for a page to load
     * @param webDriverFactory WebDriverFactory to create webdrivers
     * @param webDriverConfig Map containing configuration for the webdriver
     */
    public RetryingTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout) {
        super(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     * @param implicitlyWaitDriverTimeout int containing the number of seconds to wait before giving up trying to get a webdriver
     * @param pageLoadDriverTimeout int containing the number of seconds to wait for a page to load
     * @param initialVars Map containing any initial variables
     * @param webDriverFactory WebDriverFactory to create webdrivers
     * @param webDriverConfig Map containing configuration for the webdriver
     */
    public RetryingTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout, Map<String, String> initialVars) {
        super(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }

    /**
     * See parent constructor
     * @param script Script object to run
     * @param log Log object for logging (Commons logging)
     * @param previousRun TestRun object of the previous run of this test
     * @param implicitlyWaitDriverTimeout int containing the number of seconds to wait before giving up trying to get a webdriver
     * @param pageLoadDriverTimeout int containing the number of seconds to wait for a page to load
     * @param initialVars Map containing any initial variables
     */
    public RetryingTestRun(Script script, Log log, TestRun previousRun, int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout, Map<String, String> initialVars) {
        super(script, log, previousRun, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }

    /**
     * Executes the next step.
     *
     * @param maxRetries int containing the number of times to retry a step if the predicate is true
     * @param retryDelaySeconds int containing the number of seconds to wait before retry (0 for none)
     * @param retryPredicate Predicate to execute on the exception - return true to try again
     *
     * @return True on success.
     */
    public boolean nextWithRetry(int retryDelaySeconds, int maxRetries, Predicate<Exception> retryPredicate) {
        if (stepIndex == -1) {
            log.debug("Starting test run.");
        }

        initRemoteWebDriver();

        log.debug("Running step " + (stepIndex + 2) + ": " + script.steps.get(stepIndex + 1).toString());
        boolean result = false;

        int runIndex = ++stepIndex;
        Step step = script.steps.get(runIndex);

        int currentTry = 0;
        boolean stepFinished = false;

        while (currentTry<maxRetries && !stepFinished)
        {
            try {
                result = step.type.run(this);
                stepFinished = true;
            } catch (Exception e) {
                boolean retryPred = (retryPredicate!=null && retryPredicate.test(e));
                LOG.info("Exception hit while running step {} retryPred={} and try={} of {}", runIndex, retryPred, currentTry, maxRetries);
                if (retryPred && currentTry<maxRetries)
                {
                    LOG.debug("Retry predicate matched, retrying after {} seconds");
                    currentTry++;
                    try {
                        Thread.sleep(1000*retryDelaySeconds);
                    }
                    catch (InterruptedException ie)
                    {
                        // do nothing
                    }
                }
                else
                {
                    throw new RuntimeException(currentStep() + " failed.", e);
                }
            }
        }

        if (!result) {
            // If a verify failed, we just note this but continue.
            if (currentStep().type instanceof Verify) {
                log.error(currentStep() + " failed.");
                return false;
            }
            // In all other cases, we throw an exception to stop the run.
            RuntimeException e = new RuntimeException(currentStep() + " failed.");
            e.fillInStackTrace();
            log.fatal(e);
            throw e;
        } else {
            return true;
        }
    }


}
