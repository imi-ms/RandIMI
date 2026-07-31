package de.unimuenster.imi.randimi;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite that allows tho run all tests except for selenium tests.
 */
@Suite
@SelectPackages({"de.unimuenster.imi.randimi"})
@SuiteDisplayName("All tests")
public class AllTestSuite {
}
