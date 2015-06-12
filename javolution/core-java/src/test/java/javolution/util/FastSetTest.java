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

public class FastSetTest {

	private FastSet<String> _fastSet;
	
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
		
		String anyString = _fastSet.any(String.class);
		assertNotNull("Any String Obtained", anyString);
		assertTrue("Set Contains String", _fastSet.contains(anyString));
	}
	
	@Test
	public void testCaseInsensitiveSetWithLexicalCaseInsensitive(){
		_fastSet = new FastSet<String>(Equalities.LEXICAL_CASE_INSENSITIVE);
		_fastSet.add("Test");
		
		assertTrue("Set Contains Test", _fastSet.contains("Test"));
		assertTrue("Set Contains TEST", _fastSet.contains("TEST"));
		assertTrue("Set Contains test", _fastSet.contains("test"));
		assertTrue("Set Contains tESt", _fastSet.contains("tESt"));
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
	public void testMax(){
		_fastSet.add("AA");
		_fastSet.add("A");		
		_fastSet.add("AAAA");
		_fastSet.add("AAA");	
		
		assertEquals("Set Max Is AAAA", "AAAA", _fastSet.max());
	}
	
	@Test
	public void testMin(){
		_fastSet.add("AA");
		_fastSet.add("A");		
		_fastSet.add("AAAA");
		_fastSet.add("AAA");	
		
		assertEquals("Set Min Is A", "A", _fastSet.min());
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
	
	@Test
	public void testUpdate(){
		_fastSet.add("Test1");
		_fastSet.add("Test2");
		_fastSet.add("Test3");
		
		Consumer<Set<String>> removeAllUpdate = new Consumer<Set<String>>() {  
			public void accept(Set<String> view) {
				Iterator<String> it = view.iterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
		};
		
		_fastSet.update(removeAllUpdate);
		
		assertEquals("Set Size Is 0 After Update", 0, _fastSet.size());
		assertTrue("Set Is Empty After Update", _fastSet.isEmpty());
	}
}
