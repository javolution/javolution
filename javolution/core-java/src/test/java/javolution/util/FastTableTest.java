/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import junit.framework.TestCase;

public class FastTableTest extends TestCase {
    
    public FastTableTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of forEach method, of class FastTable.
     */
    public void testAdd() {
       if (true) return; 
        System.out.println("Test Add");
        FastTable ft = new FastTable();
        for (int i=0; i < 10; i++) {
            ft.add("INT " + i);
        }
        System.out.println(ft);
    }

}
