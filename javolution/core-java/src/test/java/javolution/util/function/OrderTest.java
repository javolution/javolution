/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class OrderTest {

	@Before
	public void init(){
	}
	
	@Test
	public void testLexical(){
		boolean b = Order.LEXICAL_CASE_INSENSITIVE.areEqual("Toto", "ToTo");
		assertEquals("Lexical Case Insensitive", true, b);
	}
	
}
