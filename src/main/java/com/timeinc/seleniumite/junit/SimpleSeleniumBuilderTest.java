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
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.TestRun;
import com.timeinc.seleniumite.environment.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RunWith(ParallelParameterized.class)
public class SimpleSeleniumBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSeleniumBuilderTest.class);
    private static Map<String, SessionId> sessionRegistry = new ConcurrentHashMap<>();
    private RawSourceTestingEnvironment testingEnvironment;

    public SimpleSeleniumBuilderTest(RawSourceTestingEnvironment testingEnvironment) {
        this.testingEnvironment = testingEnvironment;
    }

    @Parameterized.Parameters
    public static List<RawSourceTestingEnvironment> getEnvironments() throws Exception {
        List<RawSourceTestingEnvironment> rval = DefaultRawGlobalTestConfiguration.getDefault().createRawSourcedEnvironments();

        String testFilter = EnvironmentUtils.findEnvOrProperty("testFilter");
        if (testFilter!=null)
        {
            int preCount = rval.size();
            Pattern pattern = Pattern.compile(testFilter);
            rval.stream().forEach((p)->LOG.info(" Source : {} , {}",p.getSource().getName(), p.getSource().getOptionalSourceFile()));

            rval = rval.stream().filter((p)->p.getSource().matchesFilter(pattern)).collect(Collectors.toList());
            LOG.info("After applying filter {} to {} source files, {} remain", testFilter, preCount, rval.size());
        }

        return rval;
    }

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testingSeleniumIdeFile() throws Exception {
        LOG.info("Processing file : {}", testingEnvironment);

        List<String> failures = new LinkedList<>();
        TestRun lastRun = null;
        Log log = LogFactory.getFactory().getInstance(SimpleSeleniumBuilderTest.class);
        HashMap<String, String> driverConfig = testingEnvironment.createDriverConfig(DefaultRawGlobalTestConfiguration.getDefault());

        for (Script script : testingEnvironment.createScripts()) {
            LoggingRemoteWebDriverFactory wdf = new LoggingRemoteWebDriverFactory(testingEnvironment.webDriverFactory(), script.name);

            LOG.info("Executing script {}", script.name);
            for (Map<String, String> data : script.dataRows) {
                try {
                    lastRun = script.testRunFactory.createTestRun(script, log, wdf, driverConfig, data, lastRun);

                    // Have to call finish first so it will run
                    boolean finished = lastRun.finish();
                    String sessionId = "SID:" + SessionRegistry.INSTANCE.lookup(script.name, driverConfig);


                    if (finished) {
                        LOG.info("Success : {} ", createMessage(testingEnvironment, sessionId));
                    } else {
                        String message = "Failure : " + createMessage(testingEnvironment, sessionId);
                        LOG.info(message);
                        failures.add(message);
                    }


                } catch (Exception e) {
                    // Runtime exception containing a org.openqa.selenium exception of some type
                    String sessionId = "SID:" + SessionRegistry.INSTANCE.lookup(script.name, driverConfig);
                    String message = "Failure : " + createMessage(testingEnvironment, sessionId)
                            + " : Exception : " + e.getMessage();

                    LOG.info(message);
                    failures.add(message);
                }
            }
        }

        if (failures.size() > 0) {
            LOG.info("Failing test with : {}", failures);
            Assert.fail("Test Failure : " + failures.toString());
        }
    }

    private String createMessage(TestingEnvironment environment, String sessionId) {
        return "Session : " + sessionId + " : environment : " + environment.shortSummary();
    }

    @After
    public void tearDown() throws Exception {
    }

}