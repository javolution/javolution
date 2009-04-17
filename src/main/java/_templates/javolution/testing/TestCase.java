/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.testing;

/**
 * <p> This class represents a test case which can be used for validation, 
 *     performance and regression tests.</p>
 *     
 * <p> The structure of a test case is as follow:[code]
 *     class MyTestCase extends TestCase {
 *
 *         // Prepares data/state in which to run the test.
 *         public void setUp() { ... } // Optional
 *         
 *         // Executes the test possibly exercising the function tested multiple times.
 *         public void execute() throws Throwable { ... } // Mandatory.
 *         
 *         // Returns the number of times the function tested has been exercised (default 1).
 *         public int count() { ... } // Optional
 *     
 *         // Validates the test results and possibly check for limit cases or exceptions.
 *         public void validate() throws Throwable { ... } // Mandatory.
 *         
 *         // Cleanups after execution (e.g. to release resources).
 *         public void tearDown() { ... } // Optional

 *     }[/code]
 *     It should be noted that some testing contexts (e.g. {@link TimeContext})
 *     may run the sequence: setUp, execute and tearDown multiple
 *     times to calculate for example the average execution time,
 *     {@link #validate validation} in that case is performed only once
 *     after the last execution.[code]
 *   public class TypeFormatParseInt extends TestCase {
 *
 *       static final int N = 1000; // Number of random samples.
 *       int[] _expected = new int[N];
 *       int[] _actual = new int[N];
 *       String[] _strings = new String[N];
 *
 *       public void setUp() {
 *           for (int i = 0; i < _expected.length; i++) {
 *               _expected[i] = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
 *               _strings[i] = String.valueOf(_expected[i]);
 *           }
 *       }
 *
 *       public void execute() {
 *           for (int i = 0; i < N; i++) {
 *               _actual[i] = TypeFormat.parseInt(_strings[i]);
 *           }
 *       }
 *
 *       public int count() {
 *           return N;
 *       }
 *
 *       public void validate() {
 *
 *           // Compares expected versus actual (for random values).
 *           TestContext.assertArrayEquals(_expected, _actual);
 *
 *           // Supplementary tests to check for limit cases.
 *           TestContext.assertEquals(Integer.MIN_VALUE, TypeFormat.parseInt("-2147483648"));
 *           TestContext.assertEquals(0, TypeFormat.parseInt("0"));
 *           TestContext.assertEquals(Integer.MAX_VALUE, TypeFormat.parseInt("2147483647"));
 *
 *           // Checks exceptions raised.
 *           TestContext.assertException(NumberFormatException.class, new Runnable() {
 *               public void run() {
 *                   TypeFormat.parseInt("2147483648"); // Overflow
 *               }
 *           });
 *           TestContext.assertException(NumberFormatException.class, new Runnable() {
 *               public void run() {
 *                   TypeFormat.parseInt("123E4"); // Invalid Character
 *               }
 *           });
 *       }
 *   }[/code]</p>
 *
 * <p> Test cases may be {@link TestContext#run(javolution.testing.TestCase)
 *     run} individually or as part of a {@link TestSuite}. If an error occurs
 *     the location of the assert failing is usually available (a hyperlink
 *     in Netbeans and Eclipse).
 *     <pre>
 *     ...
 *    > [test] TypeFormat.parseDouble(CharSequence)
 *    > <font color="#FF0055">[error] Array element at 840, expected 2.078139623637765E-308 but found 2.0781396236377647E-308
	at javolution.TypeFormatTest$ParseDouble.validate(TypeFormatTest.java:419)</font>
 *     </pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, February 27, 2009
 * @see     TestContext
 */
public abstract class TestCase {
    /**
     * Indicates if this test case has to be ignored.
     */
   boolean _isIgnored;

    /**
     * Returns the name of this test case. The default implementation
     * returns the class name.
     *
     * @return the test case name.
     */
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * Default constructor.
     */
    protected TestCase() {
    }

    /**
     * Selects whether or not this test case should be ignored. If the
     * test case is ignored it is not executed, but the test context
     * will usually indicate that the test is being ignored.
     *
     * @param isIgnored <code>true</code> if test case is ignored;
     *        <code>false</code> otherwise.
     * @return this test case.
     */
    public TestCase ignore(boolean isIgnored) {
        _isIgnored = isIgnored;
        return this;
    }

    /**
     * Indicates whether or not this test case should be ignored.
     *
     * @return <code>true</code> if this test case is ignored;
     *        <code>false</code> otherwise.
     */
    public boolean isIgnored() {
        return _isIgnored;
    }

    /**
     * Prepares the test case execution (the default implementation does 
     * nothing).
     */
    public void setUp() {
        // Does nothing.
    }

    /**
     * Executes this test case (possibly multiple times in which case 
     * the {@link #count()} method should be overriden).
     */
    public abstract void execute() throws Exception;

    /**
     * The number of times the test case is exercised (default <code>1</code>).
     * 
     * @return the number of test case occurences in {@link #execute}.
     */
    public int count() {
        return 1;
    }

    /**
     * Validates the test results and possibly checks for limit cases
     * or exceptions.
     */
    public abstract void validate() throws Exception;

    /**
     * Cleanup once test is complete (the default implementation does 
     * nothing).
     */
    public void tearDown() {
        // Does nothing.
    }

    /**
     * Returns the <code>String</code> representation of this test case.
     * 
     * @return <code>this.getName()</code>
     */
    public String toString() {
        return getName();
    }

}