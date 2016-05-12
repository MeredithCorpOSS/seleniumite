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

import com.sebuilder.interpreter.webdriverfactory.Remote;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Thin wrapper for the fields needed to create a specific environment
 * <p>
 * Created by cweiss1271 on 10/19/15.
 */
public class RemoteTestingEnvironment implements TestingEnvironment {
    private static final WebDriverFactory DEFAULT = new Remote();
    private String platform;
    private String browserName;
    private String browserVersion;
    private Map<String, String> otherData = new TreeMap<>();

    public RemoteTestingEnvironment() {
        super();
    }

    @Override
    public String shortSummary() {
        String rval = StringUtils.trimToEmpty(platform) + "/" + StringUtils.trimToEmpty(browserName) + "/" +
                StringUtils.trimToEmpty(browserVersion);
        if (otherData != null && otherData.size() > 0) {
            rval += "/" + otherData.toString();
        }

        return rval;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public TestingEnvironment withPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public TestingEnvironment withBrowserName(String browserName) {
        this.browserName = browserName;
        return this;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public TestingEnvironment withBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    public HashMap<String, String> createDriverConfig(RawGlobalTestConfiguration globalTestConfiguration) {
        HashMap<String, String> rval = new HashMap<>();

        // put in any driver strings from global
        if (globalTestConfiguration.getGlobalDriverData() != null) {
            globalTestConfiguration.getGlobalDriverData().forEach((k, v) -> rval.put(k, EnvironmentUtils.applyEnvVars(v)));
        }

        // then for this particular one
        if (otherData != null) {
            otherData.forEach((k, v) -> rval.put(k, EnvironmentUtils.applyEnvVars(v)));
        }


        // Finally, the forced fields
        rval.put("url", EnvironmentUtils.applyEnvVars(globalTestConfiguration.getUrl()));
        rval.put("platform", EnvironmentUtils.applyEnvVars(platform));
        rval.put("browser", EnvironmentUtils.applyEnvVars(browserName));
        rval.put("browserVersion", EnvironmentUtils.applyEnvVars(browserVersion));
        return rval;
    }

    @Override
    public WebDriverFactory webDriverFactory() {
        return DEFAULT;
    }

    public Map<String, String> getOtherData() {
        return otherData;
    }

    public void setOtherData(Map<String, String> otherData) {
        this.otherData = otherData;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
