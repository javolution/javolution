/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javolution.util.function.Consumer;
import javolution.util.function.Equalities;

import org.junit.Before;
import org.junit.Test;

public class FastSortedSetTest {

	private FastSortedSet<String> _fastSortedSet;
	
	@Before
	public void init(){
		_fastSortedSet = new FastSortedSet<String>();
	}
	
	@Test
	public void testAny(){
		_fastSortedSet.add("AA");
		_fastSortedSet.add("A");		
		_fastSortedSet.add("AAAA");
		_fastSortedSet.add("AAA");	
		
		String anyString = _fastSortedSet.any(String.class);
		assertNotNull("Any String Obtained", anyString);
		assertTrue("Set Contains String", _fastSortedSet.contains(anyString));
	}
	
	@Test
	public void testCaseInsensitiveSetWithLexicalCaseInsensitive(){
		_fastSortedSet = new FastSortedSet<String>(Equalities.LEXICAL_CASE_INSENSITIVE);
		_fastSortedSet.add("Test");
		
		assertTrue("Set Contains Test", _fastSortedSet.contains("Test"));
		assertTrue("Set Contains TEST", _fastSortedSet.contains("TEST"));
		assertTrue("Set Contains test", _fastSortedSet.contains("test"));
		assertTrue("Set Contains tESt", _fastSortedSet.contains("tESt"));
	}
	
	@Test
	public void testClear(){
		_fastSortedSet.add("Test");
		_fastSortedSet.clear();
		assertEquals("Set Size Is 0 After Clear", 0, _fastSortedSet.size());
		assertTrue("Set Is Empty After Clear", _fastSortedSet.isEmpty());	
	}
	
	@Test
	public void testContains(){
		_fastSortedSet.add("Test");
		assertTrue("Set Contains Test", _fastSortedSet.contains("Test"));
	}
	
	@Test
	public void testContainsAll(){
		Set<String> set = new FastSet<String>();
		set.add("Test1");
		set.add("Test2");
		set.add("Test3");
				
		_fastSortedSet.add("Test1");
		_fastSortedSet.add("Test2");
		_fastSortedSet.add("Test3");
		
		assertTrue("Set Contains All of the Elements", _fastSortedSet.containsAll(set));
	}
	
	@Test
	public void testIsEmpty(){
		assertTrue("Set Is Empty", _fastSortedSet.isEmpty());
		_fastSortedSet.add("Test");
		assertFalse("Set Is NOT Empty", _fastSortedSet.isEmpty());
	}
	
	@Test
	public void testIteratorAndSetRetainsInsertOrder(){
		_fastSortedSet.add("AAA");
		_fastSortedSet.add("A");
		_fastSortedSet.add("AAAA");
		_fastSortedSet.add("AA");
		
		Iterator<String> iterator = _fastSortedSet.iterator();
		
		assertEquals("Element 1 is A", "A", iterator.next());
		assertEquals("Element 2 is AA", "AA", iterator.next());
		assertEquals("Element 3 is AAA", "AAA", iterator.next());
		assertEquals("Element 4 is AAAA", "AAAA", iterator.next());
	}
	
	@Test
	public void testMax(){
		_fastSortedSet.add("AA");
		_fastSortedSet.add("A");		
		_fastSortedSet.add("AAAA");
		_fastSortedSet.add("AAA");	
		
		assertEquals("Set Max Is AAAA", "AAAA", _fastSortedSet.max());
	}
	
	@Test
	public void testMin(){
		_fastSortedSet.add("AA");
		_fastSortedSet.add("A");		
		_fastSortedSet.add("AAAA");
		_fastSortedSet.add("AAA");	
		
		assertEquals("Set Min Is A", "A", _fastSortedSet.min());
	}
	
	@Test
	public void testRetainAll(){
		Set<String> set = new FastSet<String>();
		set.add("A");
		
		_fastSortedSet.add("AA");
		_fastSortedSet.add("A");		
		_fastSortedSet.add("AAAA");
		_fastSortedSet.add("AAA");	
		
		_fastSortedSet.retainAll(set);
		
		assertTrue("Set Still Contains A", _fastSortedSet.contains("A"));
		assertEquals("Set Size is Now 1", 1, _fastSortedSet.size());
	}
	
	@Test
	public void testReversedView(){
		_fastSortedSet.add("A");
		_fastSortedSet.add("AA");
		_fastSortedSet.add("AAA");
		_fastSortedSet.add("AAAA");
		
		Collection<String> reversedCollection = _fastSortedSet.reversed();
		
		Iterator<String> iterator = reversedCollection.iterator();
		
		assertEquals("Element 1 is AAAA", "AAAA", iterator.next());
		assertEquals("Element 2 is AAA", "AAA", iterator.next());
		assertEquals("Element 3 is AA", "AA", iterator.next());
		assertEquals("Element 4 is A", "A", iterator.next());
	}
	
	@Test
	public void testSize(){
		_fastSortedSet.add("Test1");
		_fastSortedSet.add("Test2");
		_fastSortedSet.add("Test3");
		assertEquals("Set Size Is 0 After Clear", 3, _fastSortedSet.size());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testUnmodifiableView(){
		Set<String> unmodifiableSet = _fastSortedSet.unmodifiable();
		unmodifiableSet.add("Test");
	}
	
	@Test
	public void testUpdate(){
		_fastSortedSet.add("Test1");
		_fastSortedSet.add("Test2");
		_fastSortedSet.add("Test3");
		
		Consumer<Set<String>> removeAllUpdate = new Consumer<Set<String>>() {  
			public void accept(Set<String> view) {
				Iterator<String> it = view.iterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
		};
		
		_fastSortedSet.update(removeAllUpdate);
		
		assertEquals("Set Size Is 0 After Update", 0, _fastSortedSet.size());
		assertTrue("Set Is Empty After Update", _fastSortedSet.isEmpty());
	}
}
