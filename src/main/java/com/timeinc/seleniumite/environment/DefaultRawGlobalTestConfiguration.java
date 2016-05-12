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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton to allow convention-over-configuration for JsonGlobalTestConfiguration
 * Loads on first request if not loaded before - defaults to reading /test-configuration.json unless
 * a different target is supplied in environmental var or system property "test.configuration.location"
 * Created by cweiss1271 on 11/30/15.
 */
public class DefaultRawGlobalTestConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRawGlobalTestConfiguration.class);
    // TODO: Verify that this isnt overkill - maybe a straight synchronized is sufficient
    private static final Object LOCK = new Object();
    private static DefaultRawGlobalTestConfiguration INSTANCE;
    private RawGlobalTestConfiguration wrapped;

    private DefaultRawGlobalTestConfiguration(RawGlobalTestConfiguration wrapped) {
        super();
        if (wrapped == null) {
            throw new IllegalArgumentException("Can't create with a null configuration");
        }
        this.wrapped = wrapped;
    }

    public static RawGlobalTestConfiguration getDefault() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                loadConfiguration();
            }
        }
        return INSTANCE.wrapped;
    }

    private static final void loadConfiguration() {
        LOG.info("Creating configurator");
        String configurationLocation = EnvironmentUtils.findEnvOrProperty("TEST_CONFIGURATION_LOCATION", "/test-configuration.json");
        // TODO: Handle a URL here?
        // TODO: Classloader may be an issue here
        RawGlobalTestConfiguration config = EnvironmentUtils.parseGlobalTestConfigurationFile(DefaultRawGlobalTestConfiguration.class.getResourceAsStream(configurationLocation));
        INSTANCE = new DefaultRawGlobalTestConfiguration(config);
    }


}
