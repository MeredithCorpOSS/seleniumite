package com.timeinc.seleniumite.junit;

/**
 MIT License

 Copyright (c) 2016 Time, Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/**
 * The canonical implementation of this looks like the code below - it loads the configuration in /test-configuration.json
 * which tells it where to find the JSON source files and what browser/platform combos to try.  It then uses ParallelParameterized
 * to execute them in parallel.
 * <p>
 * You could either run a copy of this class, or just import it in a Suite class like so:
 *
 * @RunWith(Suite.class)
 * @Suite.SuiteClasses({ SimpleSeleniumIdeTest.class })
 * <p>
 * Created by cweiss1271 on 10/19/15.
 */

import com.googlecode.junittoolbox.ParallelParameterized;
import com.sebuilder.interpreter.RetryingTestRun;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.timeinc.seleniumite.environment.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RunWith(ParallelParameterized.class)
public class SimpleSeleniumBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSeleniumBuilderTest.class);
    private static Map<String, SessionId> sessionRegistry = new ConcurrentHashMap<>();
    private static Object GLOBAL_TEST_LOCK = new Object();
    private RawSourceTestingEnvironment testingEnvironment;
    private boolean includeStackTracesInLogs = "true".equalsIgnoreCase(EnvironmentUtils.findEnvOrProperty("includeStackTraces"));
    private boolean runSingleThreaded = "true".equalsIgnoreCase(EnvironmentUtils.findEnvOrProperty("runSingleThreaded"));

    public SimpleSeleniumBuilderTest(RawSourceTestingEnvironment testingEnvironment) {
        this.testingEnvironment = testingEnvironment;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getEnvironments() throws Exception {
        List<RawSourceTestingEnvironment> rval = DefaultRawGlobalTestConfiguration.getDefault().createRawSourcedEnvironments();

        String testFilter = EnvironmentUtils.findEnvOrProperty("testFilter");
        if (testFilter!=null)
        {
            int preCount = rval.size();
            Pattern pattern = Pattern.compile(testFilter);
            if (LOG.isTraceEnabled())
            {
                rval.stream().forEach((p)->LOG.trace(" Source : {} , {}",p.getSource().getName(), p.getSource().getOptionalSourceFile()));
            }

            rval = rval.stream().filter((p)->p.getSource().matchesFilter(pattern)).collect(Collectors.toList());
            LOG.info("After applying filter {} to {} source files, {} remain", testFilter, preCount, rval.size());
        }

        // Since Parameters REALY wants an array of objects
        List<Object[]> castToExpectedValue = rval.stream().map(p->new Object[]{p}).collect(Collectors.toList());
        return castToExpectedValue;
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void performTesting() throws Exception
    {
        if (runSingleThreaded)
        {
            synchronized (GLOBAL_TEST_LOCK)
            {
                testSeleniumIdeFile();
            }
        }
        else
        {
            testSeleniumIdeFile();
        }

    }

    public void testSeleniumIdeFile() throws Exception {
        String threaded = (runSingleThreaded)?"Single-Thread":"Multi-Threaded";
        LOG.info("{} processing file : {}", threaded,testingEnvironment);

        List<String> failures = new LinkedList<>();
        RetryingTestRun lastRun = null;
        Log log = LogFactory.getFactory().getInstance(SimpleSeleniumBuilderTest.class);
        HashMap<String, String> driverConfig = testingEnvironment.createDriverConfig(DefaultRawGlobalTestConfiguration.getDefault());
        Predicate<Exception> retryPredicate = createRetryPredicate();

        RetryingTestRunFactory testRunFactory = new RetryingTestRunFactory();

        for (Script script : testingEnvironment.createScripts()) {
            LoggingRemoteWebDriverFactory wdf = new LoggingRemoteWebDriverFactory(testingEnvironment.webDriverFactory(), script.name);

            LOG.info("Executing script {}", script.name);
            for (Map<String, String> data : script.dataRows) {
                Step currentStep = null;
                try {
                    lastRun = testRunFactory.createTestRun(script, log, wdf, driverConfig, data, lastRun);

                    Boolean lastStepResult = null;

                    if (!lastRun.hasNext())
                    {
                        LOG.warn("Has next is false and havent started yet");
                    }

                    // Actually run the script
                    while (lastRun.hasNext())
                    {
                        lastStepResult = lastRun.nextWithRetry(3,3,retryPredicate ); // 3 tries with 3 second waits
                        currentStep = lastRun.currentStep();
                        LOG.debug("{} for step : {}",lastStepResult, currentStep.toJSON() );
                    }
                    String sessionId = "SID:" + SessionRegistry.INSTANCE.lookup(script.name, driverConfig);
                    String message = createMessage(lastStepResult, testingEnvironment, sessionId, currentStep);
                    LOG.info(message);

                    if (!Boolean.TRUE.equals(lastStepResult)) {
                        failures.add(message);
                    }
                } catch (Exception e) {
                    Throwable throwableToLog = unwrapWebDriverException(e);
                    String sessionId = "SID:" + SessionRegistry.INSTANCE.lookup(script.name, driverConfig);
                    String message = createMessage(false, testingEnvironment, sessionId, currentStep, throwableToLog);
                    LOG.info(message);
                    failures.add(message);
                }

                // Run "finish" so that it'll shut down the driver if necessary
                try {
                    if (lastRun!=null)
                    {
                        lastRun.finish();
                    }
                }
                catch (Exception e)
                {
                    LOG.debug("Error while trying to shut down",e);
                }
            }
        }

        if (failures.size() > 0) {
            LOG.info("Failing test with : {}", failures);
            Assert.fail("Test Failure : " + failures.toString());
        }
    }

    private Throwable unwrapWebDriverException(Throwable e)
    {
        Throwable rval = e;
        // The runtime exception wrapper doesn't help in this case, toss it
        if (RuntimeException.class.isAssignableFrom(e.getClass()) && e.getCause()!=null &&
                WebDriverException.class.isAssignableFrom(e.getCause().getClass()))
        {
            // Strip the wrapping RuntimeException in this case
            rval = e.getCause();
        }
        return rval;
    }

    private String createMessage(Boolean success, TestingEnvironment environment, String sessionId, Step step) {
        StringBuilder sb = new StringBuilder();
        sb.append((Boolean.TRUE.equals(success))?"Success: ":"Failure: ");
        sb.append("Session : ").append(sessionId).append(" : environment : ").append(environment.shortSummary()).append(" Step : ").append(step);
        return sb.toString();
    }

    private String createMessage(Boolean success, TestingEnvironment environment, String sessionId, Step step, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(createMessage(success, environment, sessionId, step));
        if (t!=null)
        {
            sb.append(" Exception : ");
            if (includeStackTracesInLogs)
            {
                sb.append(ExceptionUtils.getFullStackTrace(t));
            }
            else
            {
                sb.append(t.getMessage()).append(" (Exception class is ").append(t.getClass()).append(")");
            }
        }
        return sb.toString();
    }

    private Predicate<Exception> createRetryPredicate()
    {
        Predicate<Exception> rval = null;
        String predicateClass = EnvironmentUtils.findEnvOrProperty("retryPredicateClass");
        if (predicateClass!=null)
        {
            LOG.info("Attempting to create retry predicate for class : {}",predicateClass);
            try
            {
                rval = (Predicate<Exception>)Class.forName(predicateClass).newInstance();
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                throw new RuntimeException("Couldnt create retry predicate",e);
            }
        }
        else
        {
            LOG.info("No retry predicate defined");
        }
        return rval;
    }


    @After
    public void tearDown() throws Exception {
    }

}