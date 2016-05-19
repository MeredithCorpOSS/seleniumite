package com.timeinc.seleniumite.environment;

import com.sebuilder.interpreter.RetryingTestRun;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cweiss1271 on 5/19/16.
 */
public class RetryingTestRunFactory extends TestRunFactory {
    private int implicitlyWaitDriverTimeout = -1;
    private int pageLoadDriverTimeout = -1;
    private int retryDelaySeconds = 3;

    public int getImplicitlyWaitDriverTimeout() { return implicitlyWaitDriverTimeout; }
    public void setImplicitlyWaitDriverTimeout(int implicitlyWaitDriverTimeout) { this.implicitlyWaitDriverTimeout = implicitlyWaitDriverTimeout; }

    public int getPageLoadDriverTimeout() { return pageLoadDriverTimeout; }
    public void setPageLoadDriverTimeout(int pageLoadDriverTimeout) { this.pageLoadDriverTimeout = pageLoadDriverTimeout; }

    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    /**
     * @param script
     * @return A TestRun for the script
     */
    public RetryingTestRun createTestRun(Script script) {
        return new RetryingTestRun(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
    }

    /**
     * @param script
     * @param initialVars
     * @return A TestRun for the script
     */
    public RetryingTestRun createTestRun(Script script, Map<String, String> initialVars) {
        return new RetryingTestRun(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }

    /**
     * @param script
     * @param log
     * @param webDriverFactory
     * @param webDriverConfig
     * @return A new instance of TestRun
     */
    public RetryingTestRun createTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig) {
        return new RetryingTestRun(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
    }

    /**
     * @param script
     * @param log
     * @param webDriverFactory
     * @param webDriverConfig
     * @param initialVars
     * @return A new instance of TestRun
     */
    public RetryingTestRun createTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Map<String, String> initialVars) {
        return new RetryingTestRun(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }

    /**
     * @param script
     * @param log
     * @param webDriverFactory
     * @param webDriverConfig
     * @param initialVars
     * @param previousRun
     * @return A new instance of TestRun, using the previous run's driver and vars if available.
     */
    public RetryingTestRun createTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Map<String, String> initialVars, TestRun previousRun) {
        if (script.usePreviousDriverAndVars && previousRun != null && previousRun.driver() != null) {
            return new RetryingTestRun(script, log, previousRun, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
        }
        return new RetryingTestRun(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
    }
}
