/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2009 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.testing.JUnitContext;
import javolution.testing.TestCase;
import junit.framework.Test;

/**
 * <p> This class represents a JUnit TestSuite running Javolution test cases.<p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 21, 2009
 */
public class JavolutionTest extends junit.framework.TestSuite {

    public static Test suite() {
        JavolutionTest suite = new JavolutionTest();
        for (TestCase test : new TypeFormatTestSuite().tests()) {
            suite.addTest(new JUnitTestCase(test));
        }
        for (TestCase test : new ContextTestSuite().tests()) {
            suite.addTest(new JUnitTestCase(test));
        }
        for (TestCase test : new StructTestSuite().tests()) {
            suite.addTest(new JUnitTestCase(test));
        }
        // ...
        return suite;
    }

    public static class JUnitTestCase extends junit.framework.TestCase {

        private final TestCase _test;

        public JUnitTestCase(TestCase test) {
            _test = test;
        }

        public String getName() {
            return _test.getName();
        }

        protected void runTest() throws Exception {
            JUnitContext.enter();
            try {
                JUnitContext.run(_test);
            } finally {
                JUnitContext.exit();
            }
        }
    }
}
