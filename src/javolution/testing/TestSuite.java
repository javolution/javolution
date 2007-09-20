/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.testing;

import javolution.util.FastTable;
import j2me.lang.CharSequence;
import j2me.util.List;

/**
 * <p> This class represents a collection of {@link TestCase test cases} and 
 *     detailed information about the test being performed. [code]
 *      class TextBuilderSuite extends TestSuite {
 *           public void run() {
 *                TestContext.info("Test Suite for TextBuilder");
 *                TestContext.test(appendInt);
 *                ...
 *           }
 *           TestCase appendInt = new TestCase() {
 *                TextBuilder tmp = new TextBuilder();
 *                int i;
 *                public void prepare() {
 *                   tmp.reset();
 *                   i = MathLib.randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
 *                }
 *                public void execute() {
 *                    tmp.append(i);
 *                }
 *                public void validate() {
 *                    TextContext.assertEquals(String.valueOf(i), tmp.toString());
 *                    ... // We may also validate min, max, zero boundary cases here.
 *                }
 *                public CharSequence getDescription() {
 *                    return "TextBuilder.append(int)";
 *                }
 *           };
 *           ...
 *      }[/code]</p>
 *  <p> Test suites can be run in the current logging context or within 
 *      specialized {@link TestContext test contexts}:[code] 
 *         
 *      // Runs test suite directly (validation with results being logged).
 *      new TextBuilderSuite().run();
 *      
 *      // Performs regression (no logging but exception if test fails).
 *      TestContext.enter(TestContext.REGRESSION);
 *      try {
 *          new TextBuilderSuite().run();
 *      } finally {
 *          TestContext.exit();
 *      }
 *      
 *      // Performance measurements.
 *      TimeContext.enter(); 
 *      try {
 *          new TextBuilderSuite().run(); 
 *      } finally {
 *          TimeContext.exit();
 *      }[/code]<p>
 *           
 * <p> Nothing prevent a test suite to run other test suites. It is also 
 *     possible {@link #getTestCases() to retrieve} all the test cases which 
 *     are to be executed by a test suite (for integration with an IDE for
 *     example).</p>  
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 19, 2007
 * @see <a href="http://en.wikipedia.org/wiki/Test_suite">
 *      Wikipedia: Test Suite</a>
 */
public abstract class TestSuite implements Runnable {
    
    /**
     * Default constructor.
     */
    protected TestSuite() {
    }
    
    /**
     * Runs this test suite.
     */
    public abstract void run();
        
    /**
     * Retrieves the list of test cases to be executed by this test suite.
     * 
     * @return the ordered list of test cases.
     */
    public List getTestCases() {
    	GetTestCases getTestCases = new GetTestCases();
    	TestContext.enter(getTestCases);
    	try {
    		this.run();
    	} finally {
    		TestContext.exit();
    	}
    	return getTestCases._testCases;
    }
    
    private static final class GetTestCases extends TestContext {
        FastTable _testCases = new FastTable();
        
		public boolean doAssertEquals(String message, Object expected, Object actual) {
			return true;
		}

		public void doTest(TestCase testCase) {
			_testCases.add(testCase);			
		}

		public boolean isErrorLogged() {
			return false;
		}

		public boolean isInfoLogged() {
			return false;
		}

		public boolean isWarningLogged() {
			return false;
		}

		public void logError(Throwable error, CharSequence message) {
		}

		public void logInfo(CharSequence message) {
		}

		public void logWarning(CharSequence message) {
		}    	
    } 
    
    
    /**
     * Returns the description of this test suite or <code>null</code> if none.
     * 
     * @return the description or <code>null</code>
     */
    public CharSequence getDescription() {
        return null;
    }
        
    /**
     * Returns the <code>String</code> representation of this test suite 
     * (the description or the class name by default).
     * 
     * @return the string representation of this test suite. 
     */
    public String toString() {
    	CharSequence description = getDescription();
        return description == null ? this.getClass().getName() :
        	description.toString();
    }    
}