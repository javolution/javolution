/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.ArrayList;
import java.util.Random;
import javolution.context.LogContext;
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

    public void testArrayListCreation() {
        long t = executionTimeOf(new Runnable() {
           public void run() {               
               new ArrayList();
           } 
        });
        LogContext.info("ArrayList creation: ",  t, " ns");
    }
        
    public void testFastTableCreation() {
        long t = executionTimeOf(new Runnable() {
           public void run() {               
               new FastTable();
           } 
        });
        LogContext.info("FastTable creation: ",  t, " ns");
    }
        
    public void testArrayListCreateAndAdd() {
        long t = executionTimeOf(new Runnable() {
           public void run() {               
               ArrayList al = new ArrayList();
               for (int i=0; i < 16; i++) {
                   al.add(this);
               }
           }    
        });
        LogContext.info("ArrayList create and add: ",  t, " ns");
    }
        
    public void testFastTableCreateAndAdd() {
        long t = executionTimeOf(new Runnable() {
           public void run() {               
               FastTable ft = new FastTable();
               for (int i=0; i < 16; i++) {
                   ft.add(this);
               }
           } 
        });
        LogContext.info("FastTable creation and add: ",  t, " ns");
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

    static volatile double x;
    private static long executionTimeOf(final Runnable logic) {
        final Random r = new Random();
        Runnable logicA = new Runnable() {
            public void run() {
                 x = r.nextGaussian();
            }
        };
        Runnable logicB = new Runnable() {
            public void run() {
                 x = r.nextGaussian();
                 logic.run();
            }
        };
        int n = 10000000;
        long time = System.nanoTime();
        for (int i=0; i < n; i++) {
             logicA.run();
        }
        long timeA = System.nanoTime();
        for (int i=0; i < n; i++) {
             logicB.run();
        }
        long timeB = System.nanoTime();
        return (timeB - timeA - (timeA - time)) / n;
    }
}
