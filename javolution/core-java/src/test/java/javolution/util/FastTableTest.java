/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javolution.util;

import junit.framework.TestCase;

/**
 *
 * @author T0100257
 */
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
        
        System.out.println("Test Add");
        FastTable ft = new FastTable();
        for (int i=0; i < 10; i++) {
            ft.add("INT " + i);
        }
        System.out.println(ft);
    }

}
