package com.timeinc.seleniumite;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.function.Predicate;

/**
 * This is a step retry predicate that causes a step to be retried if the target element wasn't clickable/visible the
 * first time.
 * Created by cweiss1271 on 5/17/16.
 */
public class NotClickablePredicate implements Predicate<Exception> {
    @Override
    public boolean test(Exception e) {
        String fullStack = StringUtils.trimToEmpty(ExceptionUtils.getFullStackTrace(e)).toUpperCase();
        return (fullStack.contains("ELEMENT IS NOT CLICKABLE") || fullStack.contains("ELEMENT IS NOT CURRENTLY VISIBLE"));
                //"fullStack.contains("<div class=\"blockUI blockOverlay\""));
    }
}
