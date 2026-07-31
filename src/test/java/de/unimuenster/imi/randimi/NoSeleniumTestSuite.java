package de.unimuenster.imi.randimi;

import org.junit.platform.suite.api.*;

/**
 * Test suite that allows tho run all tests except for selenium tests.
 */
@Suite
@SelectPackages({"de.unimuenster.imi.randimi"})
@ExcludePackages({"de.unimuenster.imi.randimi.selenium"})
@SuiteDisplayName("No selenium")
public class NoSeleniumTestSuite {
}
