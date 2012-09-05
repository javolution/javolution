/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.testing;

import java.util.List;

import org.javolution.util.FastTable;

/**
 * <p> This class represents a grouping of potentially {@link #isParallelizable() 
 *     parallelizable}{@link TestCase test cases}. [code]
 *      class TypeFormatTestSuite extends TestSuite {
 *           public TypeFormatTestSuite() {
 *                addTest(new ParseBoolean());
 *                addTest(new ParseInt().ignore(true)); // Adds this test case but it is ignored for now.
 *                ...
 *           }
 *           class ParseBoolean extends TestCase { ... };
 *           class ParseInt extends TestCase { ... };
 *           ...
 *      }[/code]</p>
 *
 *  <p> How the test suite is executed, how the test results are logged and how 
 *      the test report is created depends upon the {@link TestContext} in which 
 *      the test suite is {@link TestContext#run(TestSuite) run}. Specialized
 *      test contexts may address specific concerns such as performance
 *      ({@link TimeContext}), memory usage, code coverage, etc. The test
 *      context determinates also how test results are reported (e.g. html
 *      formatted, IDE integrated, etc.)</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 8, 2009
 * @see <a href="http://en.wikipedia.org/wiki/Test_suite">
 *      Wikipedia: Test Suite</a>
 */
public abstract class TestSuite {

    /**
     * Holds the test cases.
     */
    FastTable _tests = new FastTable();

    /**
     * Default constructor.
     */
    protected TestSuite() {
    }
    
    /**
     * Returns the name of this test case. The default implementation
     * returns the class name.
     *
     * @return the test suite name.
     */
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * Adds the specified test case to this test suite.
     *
     * @param testCase the test case being added.
     * @return the specified test case.
     */
    protected TestCase addTest(TestCase testCase) {
        _tests.add(testCase);
        return testCase;
    }

    /**
     * Prepares the test suite execution (the default implementation does
     * nothing).
     */
    public void setUp() {
        // Does nothing.
    }

    /**
     * Cleanup once test suite execution is complete (the default implementation
     * does nothing).
     */
    public void tearDown() {
        // Does nothing.
    }

    /**
     * Returns the collection of test cases belonging to this test suite.
     *
     * @return the test cases
     */
    public List <TestCase>  tests() {
        return _tests;
    }

    /**
     * Indicates if the test cases of this test suite can be run concurrently
     * (default <code>true</code>). If the test suite is not parallelizable then
     * the test cases are executed in sequence, first added runs first.
     *
     * @return <code>true</code> if parallelizable;
     *         <code>false</code> otherwise.
     */
    public boolean isParallelizable() {
    	return true;
    }

    /**
     * Returns the <code>String</code> representation of this test suite.
     *
     * @return <code>this.getName()</code>
     */
    public String toString() {
    	return getName();
    }
}