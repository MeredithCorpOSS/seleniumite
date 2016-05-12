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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper functions for searching directories for raw test files, parsing JSON format files, etc.
 * <p>
 * Created by cweiss1271 on 11/30/15.
 */
public class EnvironmentUtils {
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentUtils.class);

    public static String mask(String input) {
        String rval = input;
        if (rval != null && rval.length() > 5) {
            rval = input.substring(0, 2) + "*******" + input.substring(input.length() - 2);
        }
        return rval;
    }

    public static String applyEnvVars(String input) {
        String rval = input;
        if (rval != null) {
            int sIdx = rval.indexOf("${");
            while (sIdx != -1) {
                int eIdx = rval.indexOf("}", sIdx + 2);
                if (eIdx == -1) {
                    throw new IllegalArgumentException("Environment variable started but not closed in : " + input);
                }

                String varName = rval.substring(sIdx + 2, eIdx);
                String varVal = StringUtils.trimToEmpty(EnvironmentUtils.findEnvOrProperty(varName));
                LOG.debug("Converted env variable {} to {}", varName, mask(varVal));
                rval = rval.substring(0, sIdx) + varVal + rval.substring(eIdx + 1);
                sIdx = rval.indexOf("${");
            }
        }
        return rval;
    }

    public static RawGlobalTestConfiguration parseGlobalTestConfigurationFile(InputStream ios) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(ios, RawGlobalTestConfiguration.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't process global test configuration", ioe);
        }
    }


    public static List<RawSourceTestingEnvironment> createRawSourceEnvironments(List<RawTestScript> sources, List<TestingEnvironment> environments) {
        List<RawSourceTestingEnvironment> rval = new LinkedList<>();

        for (TestingEnvironment te : environments) {
            for (RawTestScript s : sources) {
                rval.add(new RawSourceTestingEnvironment(te, s));
            }
        }

        LOG.info("For {} environments and {} raw sources created {} sourced environments", environments.size(), sources.size(), rval.size());

        return rval;
    }

    public static List<RawSourceTestingEnvironment> createRawSourceEnvironmentsFromRoots(List<String> roots, List<TestingEnvironment> environments) {
        return createRawSourceEnvironments(findRawTestScripts(roots), environments);
    }

    public static List<RawTestScript> findRawTestScripts(List<String> roots) {
        List<RawTestScript> scripts = new LinkedList<>();

        for (String s : roots) {
            if (s.startsWith("classpath:")) {
                File root = new File(EnvironmentUtils.class.getResource(s.substring("classpath:".length())).getFile());

                Collection<File> matchFiles = FileUtils.listFiles(
                        root,
                        new WildcardFileFilter("*.json"),
                        DirectoryFileFilter.DIRECTORY
                );

                for (File f : matchFiles) {
                    if (f.exists() && f.isFile()) {
                        scripts.add(RawTestScript.fromFile(f));
                    } else {
                        LOG.info("Skipping {} - doesn't exist or isnt a file");
                    }
                }
            } else {
                LOG.warn("Skipping unknown protocol:{}", s);
            }
        }

        return scripts;
    }

    public static String findEnvOrProperty(String propName, String defValue) {
        String rval = System.getProperty(propName);
        rval = (rval == null) ? System.getenv(propName) : rval;
        rval = (rval == null) ? defValue : rval;
        return rval;
    }

    public static String findEnvOrProperty(String propName) {
        return findEnvOrProperty(propName, null);
    }


}
