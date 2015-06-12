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

public class MultiVariableTest {

	private MultiVariable<String,String> _multiVariable;
	
	@Before
	public void init(){
		_multiVariable = new MultiVariable<String,String>("Test","tseT");
	}
	
	@Test
	public void testGetLeft(){
		assertEquals("Left Is Test", "Test", _multiVariable.getLeft());
	}
	
	@Test
	public void testGetRight(){
		assertEquals("Right Is tseT", "tseT", _multiVariable.getRight());
	}
}
