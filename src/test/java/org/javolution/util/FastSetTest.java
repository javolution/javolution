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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class FastSetTest {

	private AbstractSet<String> _fastSet;
	
	@Before
	public void init(){
		_fastSet = new FastSet<String>();
	}
	
	@Test
	public void testAny(){
		_fastSet.add("AA");
		_fastSet.add("A");		
		_fastSet.add("AAAA");
		_fastSet.add("AAA");	
		
		String anyString = _fastSet.findAny();
		assertNotNull("Any String Obtained", anyString);
		assertTrue("Set Contains String", _fastSet.contains(anyString));
	}
		
	@Test
	public void testClear(){
		_fastSet.add("Test");
		_fastSet.clear();
		assertEquals("Set Size Is 0 After Clear", 0, _fastSet.size());
		assertTrue("Set Is Empty After Clear", _fastSet.isEmpty());	
	}
	
	@Test
	public void testContains(){
		_fastSet.add("Test");
		assertTrue("Set Contains Test", _fastSet.contains("Test"));
	}
	
	@Test
	public void testContainsAll(){
		Set<String> set = new FastSet<String>();
		set.add("Test1");
		set.add("Test2");
		set.add("Test3");
				
		_fastSet.add("Test1");
		_fastSet.add("Test2");
		_fastSet.add("Test3");
		
		assertTrue("Set Contains All of the Elements", _fastSet.containsAll(set));
	}
	
	@Test
	public void testIsEmpty(){
		assertTrue("Set Is Empty", _fastSet.isEmpty());
		_fastSet.add("Test");
		assertFalse("Set Is NOT Empty", _fastSet.isEmpty());
	}
	
	@Test
	public void testIteratorAndSetRetainsInsertOrder(){
		_fastSet.add("A");
		_fastSet.add("AA");
		_fastSet.add("AAA");
		_fastSet.add("AAAA");
		
		Iterator<String> iterator = _fastSet.iterator();
		
		assertEquals("Element 1 is A", "A", iterator.next());
		assertEquals("Element 2 is AA", "AA", iterator.next());
		assertEquals("Element 3 is AAA", "AAA", iterator.next());
		assertEquals("Element 4 is AAAA", "AAAA", iterator.next());
	}
	
	@Test
	public void testRetainAll(){
		Set<String> set = new FastSet<String>();
		set.add("A");
		
		_fastSet.add("AA");
		_fastSet.add("A");		
		_fastSet.add("AAAA");
		_fastSet.add("AAA");	
		
		_fastSet.retainAll(set);
		
		assertTrue("Set Still Contains A", _fastSet.contains("A"));
		assertEquals("Set Size is Now 1", 1, _fastSet.size());
	}

	@Test
	public void testRemove(){
		_fastSet.add("AA");
		_fastSet.add("A");

		_fastSet.remove("AA");

		assertTrue("Set doesn't contains AA", !_fastSet.contains("AA"));
		assertTrue("Set Still Contains A", _fastSet.contains("A"));
		assertEquals("Set Size is Now 1", 1, _fastSet.size());
	}

	@Test
	public void testGetAny(){
		_fastSet.add("A");
		_fastSet.add("AA");
		_fastSet.remove("AA");

		String any = _fastSet.getAny("A");

		assertEquals("Set doesn't contains A", "A", any);
	}

	@Test
	public void testReversedView(){
		_fastSet.add("A");
		_fastSet.add("AA");
		_fastSet.add("AAA");
		_fastSet.add("AAAA");
		
		Collection<String> reversedCollection = _fastSet.reversed();
		
		Iterator<String> iterator = reversedCollection.iterator();
		
		assertEquals("Element 1 is AAAA", "AAAA", iterator.next());
		assertEquals("Element 2 is AAA", "AAA", iterator.next());
		assertEquals("Element 3 is AA", "AA", iterator.next());
		assertEquals("Element 4 is A", "A", iterator.next());
	}
	
	@Test
	public void testSize(){
		_fastSet.add("Test1");
		_fastSet.add("Test2");
		_fastSet.add("Test3");
		assertEquals("Set Size Is 0 After Clear", 3, _fastSet.size());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testUnmodifiableView(){
		Set<String> unmodifiableSet = _fastSet.unmodifiable();
		unmodifiableSet.add("Test");
	}
}
