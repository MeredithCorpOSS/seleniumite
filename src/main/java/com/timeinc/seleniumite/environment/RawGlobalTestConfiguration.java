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


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wraps up a global test configuration including all the environments, what webdrivercreator to use,
 * any overrides to pass to the json files, and what sources to probe for json files
 * <p>
 * Thin wrapper around the de-facto configuration file format plus a couple of helper methods
 * <p>
 * Created by cweiss1271 on 11/30/15.
 */
public class RawGlobalTestConfiguration {
    private String url;
    private boolean localRunIncluded;
    private List<RemoteTestingEnvironment> remoteEnvironments;
    private Map<String, String> overrides = new TreeMap<>();
    private Map<String, String> globalDriverData = new TreeMap<>();
    private List<String> rawSourceRoots;

    public boolean isLocalRunIncluded() {
        return localRunIncluded;
    }

    public void setLocalRunIncluded(boolean localRunIncluded) {
        this.localRunIncluded = localRunIncluded;
    }

    public List<RemoteTestingEnvironment> getRemoteEnvironments() {
        return remoteEnvironments;
    }

    public void setRemoteEnvironments(List<RemoteTestingEnvironment> remoteEnvironments) {
        this.remoteEnvironments = remoteEnvironments;
    }

    public Map<String, String> getOverrides() {
        return overrides;
    }

    public void setOverrides(Map<String, String> overrides) {
        this.overrides = overrides;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getGlobalDriverData() {
        return globalDriverData;
    }

    public void setGlobalDriverData(Map<String, String> globalDriverData) {
        this.globalDriverData = globalDriverData;
    }

    public List<String> getRawSourceRoots() {
        return rawSourceRoots;
    }

    public void setRawSourceRoots(List<String> rawSourceRoots) {
        this.rawSourceRoots = rawSourceRoots;
    }

    public List<TestingEnvironment> allEnvironments() {
        List<TestingEnvironment> allEnvironments = new LinkedList<>();
        if (remoteEnvironments != null) {
            allEnvironments.addAll(remoteEnvironments);
        }
        if (localRunIncluded) {
            allEnvironments.add(new LocalTestingEnvironment());
        }

        return allEnvironments;
    }

    public List<RawSourceTestingEnvironment> createRawSourcedEnvironments() {
        List<RawTestScript> rawData = EnvironmentUtils.findRawTestScripts(rawSourceRoots);

        List<TestingEnvironment> allEnvironments = allEnvironments();

        if (allEnvironments.size() == 0) {
            throw new IllegalStateException("No remote environments specified and also no local run specified");
        }
        List<RawSourceTestingEnvironment> rval = EnvironmentUtils.createRawSourceEnvironments(rawData, allEnvironments);
        return rval;
    }

}
