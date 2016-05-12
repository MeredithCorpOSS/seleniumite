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

import com.sebuilder.interpreter.Script;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by cweiss1271 on 12/2/15.
 */
public class TestEnvironmentUtils {

    @Test
    public void testLocalConfigurationParse()
            throws Exception
    {
        RawGlobalTestConfiguration conf = EnvironmentUtils.parseGlobalTestConfigurationFile(getClass().getResourceAsStream("/test-configuration.json"));

        assertThat(conf.getRemoteEnvironments().size(), is(0));
        assertThat(conf.isLocalRunIncluded(), is(true));
        assertThat(conf.getOverrides().size(),is(1));
        assertThat(conf.getRawSourceRoots().size(),is(1));
        assertThat(conf.getGlobalDriverData().size(),is(0));
        List<RawSourceTestingEnvironment> l = conf.createRawSourcedEnvironments();
        assertThat(l.size(),is(2));

        for (RawSourceTestingEnvironment e:l)
        {
            List<Script> scripts = e.createScripts();
            // All of the script files have 1 script in them (no suites)
            assertThat(scripts.size(), is(1));
        }

    }

    @Test
    public void testRemoteConfigurationParse()
    {
        RawGlobalTestConfiguration conf = EnvironmentUtils.parseGlobalTestConfigurationFile(getClass().getResourceAsStream("/remote-test.json"));

        assertThat(conf.getRemoteEnvironments().size(),is(5));
        assertThat(conf.isLocalRunIncluded(),is(false));
        assertThat(conf.getOverrides().size(),is(1));
        assertThat(conf.getRawSourceRoots().size(),is(1));
        assertThat(conf.getGlobalDriverData().size(),is(5));

        List<RawSourceTestingEnvironment> l = conf.createRawSourcedEnvironments();
        assertThat(l.size(),is(10));

        for (RawSourceTestingEnvironment e:l)
        {
            List<Script> scripts = e.createScripts();
            // All of the script files have 1 script in them (no suites)
            assertThat(scripts.size(),is(1));
        }
    }
}
