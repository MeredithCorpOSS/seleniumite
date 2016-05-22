# Seleniumite

Important Note : As of 12/2/2015 I realized that a combination of Selenium Builder (http://seleniumbuilder.github.io/se-builder/)
and Selenium Interpreter (https://github.com/SeleniumBuilder/SeInterpreter-Java) handles MOST of what I needed here.  What
they don't handle (although the NodeJS version comes closer than the Java one) is the ability to parameterize across
multiple files and multiple browsers.

So instead of this being an interpreter (which Selenium Interpreter already handles pretty well) I'm making this an
addition to interpreter to add those capabilities.  At the same time I'm abandoning Selenium IDE for the newer and
closer to compliant Selenium Builder.

---

## Environments

An environment is basically just a configuration object for a WebDriver to abstract away the difference between running
tests locally (against a local firefox, for instance) and remotely (against browserstack, for instance).  Typically
these are used in conjunction with @Parameterized.Parameters (for @RunWith(Parameterized.class) or 
@RunWith(ParallelParameterized.class)) so that the same test can be executed on each stack.

In the case of Selenium Builder Json files, there is only a single JUnit test, but we will execute it once, 
per environment, per Selenium Json file; we here leverage @Parameterized above to pull that off.

---

## Sample configuration files

These can be found at src/test/resources (remote-test.json is a remote test, test-configuration.json is a local one)


---

## Configurable options

*testFilter*
If a testFilter param is set via system property or environmental variable (e.g. : -DtestFilter=.*0021.* ) then the
SimpleSeleniumBuilderTest class will filter the set of all matching test cases down to only those that match the 
regular expression that was passed (either the name or the full file path much match the passed expression)

*includeStackTraces*
If set to true (e.g. -DincludeStackTraces=true) the output on any test that fails due to an exception will include
the full stack trace - by default, only the message is included.

*retryPredicateClass*
If set (e.g., -DretryPredicateClass=com.timeinc.seleniumite.NotClickablePredicate) then whenever an exception is thrown
for a given step, it will be checked with this  pdicate and if the predicate returns true, the step will be retried 
(up to 3 times, with 3 second waits between.  I plan to parameterize this in the future, but not right now).

Seleniumite includes a built in class, NotClickablePredicate, which handles the common usage of a library like 
JQuery's BlockUI - if BlockUI happens to be up when the click is attempted, Selenium will just wait and then
try again if this retry predicate is specified.

*runSingleThreaded*
If set to true (e.g. -DrunSingleThreaded=true) then the tests will run globally single-threaded - useful for if
you have test setup that cannot run multithreaded.