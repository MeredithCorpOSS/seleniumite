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
import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * An extension of TestingEnvironment wrapping up a combination of what environment to test against and
 * what SeleniumBuilder file to execute
 * <p>
 * NOTE - this copies the file (typically json) into memory - this tradeoff is on purpose for 2 reasons:
 * 1) Most of the files tend to be pretty small and not a huge risk of blowing memory
 * 2) The files will often be re-read multiple times (on multiple-environment builds) meaning that in the case
 * of non-local-file sources this functions a bit like a cache
 * <p>
 * Created by cweiss1271 on 11/9/15.
 */
public class RawSourceTestingEnvironment implements TestingEnvironment {
    private static final Logger LOG = LoggerFactory.getLogger(RawSourceTestingEnvironment.class);

    private RawTestScript source;
    private TestingEnvironment wrapped;

    public RawSourceTestingEnvironment(TestingEnvironment src, RawTestScript source) {
        this.wrapped = src;
        this.source = source;
    }

    @Override
    public String shortSummary() {
        return "Script : " + source.getName() + " Environment :" + wrapped.shortSummary();
    }

    @Override
    public HashMap<String, String> createDriverConfig(RawGlobalTestConfiguration globalTestConfiguration) {
        return wrapped.createDriverConfig(globalTestConfiguration);
    }

    @Override
    public WebDriverFactory webDriverFactory() {
        return wrapped.webDriverFactory();
    }

    public TestingEnvironment getWrapped() {
        return wrapped;
    }

    public void setWrapped(TestingEnvironment wrapped) {
        this.wrapped = wrapped;
    }

    public RawTestScript getSource() {
        return source;
    }

    public void setSource(RawTestScript source) {
        this.source = source;
    }

    public List<Script> createScripts() {
        try {
            List<Script> rval = null;
            ScriptFactory sf = new ScriptFactory();
            sf.setStepTypeFactory(createStepTypeFactory());
            if (source.getOptionalSourceFile() != null) {
                rval = sf.parse(source.getOptionalSourceFile());
            } else {
                File f = new File(source.getName());
                rval = sf.parse(source.getRawTestContents(), f);
            }

            return rval;

        } catch (IOException | JSONException e) {
            throw new RuntimeException("Converting to runtime since cant do anything with it", e);
        }
    }

    protected StepTypeFactory createStepTypeFactory()
    {
        StepTypeFactory rval = new StepTypeFactory();
        String secondary = EnvironmentUtils.findEnvOrProperty("stepTypePackage","com.timeinc.seleniumite.extension.steptype");
        rval.setSecondaryPackage(secondary);

        LOG.trace("Using secondary step types from {}", secondary);
        return rval;
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();
        temp.append("JsonSourceTestingEnvironment [sourceData ").append(source.getRawTestContents().length())
                .append(" chars, wrapped=").append(wrapped).append(",name=").append(source.getName())
                .append("]");
        return temp.toString();
    }

}
