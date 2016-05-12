package com.timeinc.seleniumite.environment;

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

import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import com.timeinc.seleniumite.junit.SessionRegistry;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by cweiss1271 on 12/11/15.
 */
public class LoggingRemoteWebDriverFactory implements WebDriverFactory {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingRemoteWebDriverFactory.class);
    private WebDriverFactory wrapped;
    private String scriptName;

    public LoggingRemoteWebDriverFactory(WebDriverFactory wrapped, String scriptName) {
        this.wrapped = wrapped;
        this.scriptName = scriptName;
    }

    @Override
    public RemoteWebDriver make(HashMap<String, String> hashMap) throws Exception {
        RemoteWebDriver rval = wrapped.make(hashMap);
        SessionId sessionId = rval.getSessionId();
        SessionRegistry.INSTANCE.store(scriptName, hashMap, sessionId);

        LOG.info("Create driver : {} : {} : {}", scriptName, hashMap, sessionId);
        return rval;
    }


}
