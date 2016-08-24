/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.javolution.lang.Index;
import org.junit.Test;

public class IndexTest {

	private Index _index;
	
	@Test
	public void testCompareToWithIndex(){		
		_index = Index.of(3);
		Index two = Index.of(2);
		assertTrue("Index is 3", _index.compareTo(two) > 0);
		assertTrue("Index is 3", _index.compareTo(two) == 1);
	}
	
	@Test
	public void testCompareToWithInt(){		
		_index = Index.of(3);
		assertTrue("Index is 3", _index.compareTo(2) > 0);
		assertTrue("Index is 3", _index.compareTo(2) == 1);
	}
	
	@Test
	public void testDoubleValue(){
		_index = Index.of(3);
		assertEquals("Index is 3", 3.0, _index.doubleValue(), 0.0);
	}
	
	@Test
	public void testFloatValue(){
		_index = Index.of(3);
		assertEquals("Index is 3", 3.0f, _index.floatValue(), 0.0f);
	}
	

	@Test
	public void testIntValue(){
		_index = Index.of(3);
		assertEquals("Index is 3", 3, _index.intValue());
	}
		
	@Test
	public void testLongValue(){
		_index = Index.of(3);
		assertEquals("Index is 3", 3L, _index.longValue());
	}
	
	@Test
	public void testNext(){
		Index zero = Index.of(0);
		Index one = Index.of(1);
		_index = zero.next();
		assertEquals("Next Index is 1", one, _index);
	}
	
	@Test
	public void testOfWithPreAllocatedValue(){
		_index = Index.of(1);
		assertNotNull("Index Returned", _index);
		Index index = Index.of(1);
		assertNotNull("Index Returned", index);
		assertEquals("Index Is Same", _index, index);
	}
	
	@Test
	public void testOfWithUnllocatedValue(){
		_index = Index.of(2048);
		assertNotNull("Index Returned", _index);
		Index index = Index.of(2048);
		assertNotNull("Index Returned", index);
		assertFalse("Index Is NOT The Same", _index==index);
	}
	
	@Test
	public void testPrevious(){
		Index zero = Index.of(0);
		Index one = Index.of(1);
		_index = one.previous();
		assertEquals("Previous Index is 0", zero, _index);
	}
	
	@Test
	public void testToString(){
		_index = Index.of(3);
		assertEquals("Index Is 3", "3", _index.toString());
	}
}
