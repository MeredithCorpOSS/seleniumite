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


