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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Just a DTO that wraps up a name for a script and its json
 * Mainly here so that I can give descriptive error messages regardless of the source of the
 * script (I didn't want to assume a "File" type)
 * <p>
 * Created by cweiss1271 on 12/11/15.
 */
public class RawTestScript {
    private String name;
    private String rawTestContents;
    private File optionalSourceFile;
    private ScriptFormat format = ScriptFormat.JSON;

    /**
     * A helper function to make it easy to filter down lists of scripts to a given pattern on either
     * the name or source file field - Note, if the file is set, the pattern is matched against the
     * entire file path
     * @param pattern Pattern to match the name or file against
     * @return true if pattern is null, or if either the name or the file name matches
     */
    public boolean matchesFilter(Pattern pattern)
    {
        boolean rval = true;
        if (pattern!=null)
        {
            boolean nameMatches = (name!=null && pattern.matcher(name).matches());
            boolean fileMatches = (optionalSourceFile!=null && pattern.matcher(optionalSourceFile.getAbsolutePath()).matches());
            rval = nameMatches || fileMatches;
        }
        return rval;
    }


    public static RawTestScript fromFile(File f) {
        if (f.exists() && f.isFile()) {
            try {
                return new RawTestScript().withName(f.getName()).withRawTestContents(IOUtils.toString(new FileInputStream(f))).withOptionalSourceFile(f);
            } catch (IOException ioe) {
                throw new RuntimeException("Shouldnt happen", ioe);
            }
        } else {
            throw new IllegalArgumentException("Cant process - doesnt exist or isnt a file : " + f);
        }
    }

    public RawTestScript withName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getRawTestContents() {
        return rawTestContents;
    }

    public void setRawTestContents(String rawTestContents) {
        this.rawTestContents = rawTestContents;
    }

    public ScriptFormat getFormat() {
        return format;
    }

    public void setFormat(ScriptFormat format) {
        this.format = format;
    }

    public RawTestScript withRawTestContents(final String rawTestContents) {
        this.rawTestContents = rawTestContents;
        return this;
    }

    public RawTestScript withFormat(final ScriptFormat format) {
        this.format = format;
        return this;
    }

    public File getOptionalSourceFile() {
        return optionalSourceFile;
    }

    public void setOptionalSourceFile(File optionalSourceFile) {
        this.optionalSourceFile = optionalSourceFile;
    }

    public RawTestScript withOptionalSourceFile(final File optionalSourceFile) {
        this.optionalSourceFile = optionalSourceFile;
        return this;
    }


}
