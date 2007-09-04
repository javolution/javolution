/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.testing;

import j2me.lang.CharSequence;

/**
 * <p> This class represents a test case which can be used for validation, 
 *     performance and regression tests.</p>
 *     
 * <p> The structure of a test case is as follow:[code]
 *     class MyTestCase extends TestCase {
 *     
 *         // Prepares data/state in which to run the test.
 *         public void prepare() { ... } // Optional 
 *         
 *         // Executes the test (it may exercise the test case several times).
 *         public void execute() { ... } // Mandatory.
 *         
 *         // Returns the number of times the test case has been exercised (default 1).  
 *         public int count() { ... } // Optional
 *     
 *         // Validates the results. 
 *         public void validate() { ... } // Optional.
 *         
 *         // Cleanups after execution (e.g. release resources).  
 *         public void cleanup() { ... } // Optional

 *     }[/code]
 *     It should be noted that some testing contexts (e.g. {@link TimeContext})
 *     may run the sequence (prepare, execute, validate, cleanup) multiple 
 *     times to calculate for example the average execution time (
 *     {@link #validate validation} in that case is performed only once
 *     after the last run). Here is an example of test case 
 *     implementation for the <code>HashMap.put(key, value)</code> method:[code]
 *     class HashMap_put extends TestCase {
 *         private HashMap _map;
 *         private int _size;
 *                   
 *         public HashMap_put(int n) { 
 *             _size = 0; 
 *             Index.setMinimumRange(0, n); // Preallocates.
 *         }               
 *         
 *         public void prepare() {
 *             _map = new HashMap();
 *         }
 *         
 *         public void execute() {
 *             for (int i=0; i < _size;) {
 *                 _map.put(Index.valueOf(i), Index.valueOf(i++));
 *             }
 *         }
 *         
 *         public int count() { // Number of put operations performed. 
 *             return _size;
 *         }
 *         
 *         public void validate() {
 *             TestContext.assertTrue("Wrong size", _size == _map.size());
 *             for (int i=0; i < _size;) {
 *                 if (!TestContext.assertEquals(_map.get(Index.valueOf(i)), Index.valueOf(i++)))
 *                      break; // Error, no need to continue.
 *             }
 *              
 *             // Asserts performance.
 *             long avgTime = TimeContext.getAverageTime("ns");
 *             TestContext.assertTrue(avgTime + "ns too slow!", avgTime < 100);
 *         }
 *         
 *         public CharSequence getDescription() {
 *            return "java.util.HashMap.put(key, value) - " + n + " entries added";
 *         }
 *    };[/code]
 *    Test cases are typically grouped into a {@link TestSuite}:[code]
 *    public HashMapTests extends TestSuite {
 *        public void run() {
 *            TestContext.info("Test put(key, value) for various size");
 *            TestContext.test(new HashMap_put(10));
 *            TestContext.test(new HashMap_put(100));
 *            TestContext.test(new HashMap_put(1000));
 *            ...
 *        }
 *    }
 *    TimeContext.enter(); // To measure execution time.
 *    try {
 *        new HashMapTests().run();
 *    } finally {
 *        TimeContext.exit();
 *    }[/code] </p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 5, 2007
 * @see     TestContext
 */
public abstract class TestCase {
    
    /**
     * Default constructor.
     */
    protected TestCase() {
    }
    
    /**
     * Prepares the test case execution (the default implementation does 
     * nothing).
     */
    public void prepare() {
        // Does nothing.
    }
    
    /**
     * Executes this test case (possibly multiple times in which case 
     * the {@link #count()} method should be overriden).
     */
    public abstract void execute();
    
    /**
     * The number of times the test case is exercised (default <code>1</code>).
     * 
     * @return the number of test case occurences in {@link #execute}.
     */
    public int count() {
        return 1;
    }

    /**
     * Validates the test results (the default implementation does 
     * nothing).
     */
    public void validate() {
        // Does nothing.
    }
      
    /**
     * Cleanup once test is complete (the default implementation does 
     * nothing).
     */
    public void cleanup() {
        // Does nothing.
    }    
    
    /**
     * Returns the description of this test case or <code>null</code> if none.
     * 
     * @return the description or <code>null</code>
     */
    public CharSequence getDescription() {
        return null;
    }
        
    /**
     * Returns the <code>String</code> representation of this test case 
     * (the description or the class name by default).
     * 
     * @return the string representation of this test case. 
     */
    public String toString() {
    	CharSequence description = getDescription();
        return description == null ? this.getClass().getName() :
        	description.toString();
    }
}