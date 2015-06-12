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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class FastTableTest {

	private FastTable<String> _fastTable;
	
	@Before
	public void init(){
		_fastTable = new FastTable<String>();
		_fastTable.add("Test1");
		_fastTable.add("Test2");
		_fastTable.add("Test3");
	}
	
	@Test
	public void testAdd(){
		_fastTable.add("Test");
		assertTrue("Table Contains Test", _fastTable.contains("Test"));
	}
	
	@Test
	public void testAddFirst(){
		_fastTable.addFirst("Test4");
		assertEquals("Index Of Test4 is 0", 0, _fastTable.indexOf("Test4"));
	}
	
	@Test
	public void testAddLast(){
		_fastTable.addLast("Test4");
		assertEquals("Index Of Test4 is 3", 3, _fastTable.indexOf("Test4"));
	}
	
	@Test
	public void testClear(){
		_fastTable.clear();
		assertEquals("Table Size Is 0", 0 , _fastTable.size());
		assertTrue("Table Is Empty", _fastTable.isEmpty());
	}
	
	@Test
	public void testGetFirst(){
		assertEquals("First Is Test1", "Test1", _fastTable.getFirst());
	}
	
	@Test
	public void testGetLast(){
		assertEquals("Last Is Test3", "Test3", _fastTable.getLast());
	}
	
	@Test
	public void testIndexOf(){
		assertEquals("Index Of Test2 is 1", 1, _fastTable.indexOf("Test2"));
	}
	
	@Test
	public void testIsEmpty(){
		_fastTable = new FastTable<String>();
		assertTrue("Table Is Empty", _fastTable.isEmpty());
		_fastTable.add("Test");
		assertFalse("Table Is NOT Empty", _fastTable.isEmpty());
	}
	
	@Test
	public void testLastIndexOf(){
		_fastTable.add("Test2");
		_fastTable.add("Test1");
		assertEquals("LastIndexOf Test1 is 4", 4, _fastTable.lastIndexOf("Test1"));
	}
	
	@Test
	public void testOffer(){
		assertTrue("Offer Accepted", _fastTable.offer("Test4"));
		assertEquals("Index Of Test4 is 3", 3, _fastTable.indexOf("Test4"));
	}
	
	@Test
	public void testOfferFirst(){
		assertTrue("Offer Accepted", _fastTable.offerFirst("Test4"));
		assertTrue("Offer Accepted", _fastTable.offerFirst("Test5"));
		assertEquals("Index Of Test4 is 1", 1, _fastTable.indexOf("Test4"));
		assertEquals("Index Of Test5 is 0", 0, _fastTable.indexOf("Test5"));
	}
	
	@Test
	public void testOfferLast(){
		assertTrue("Offer Accepted", _fastTable.offerLast("Test4"));
		assertEquals("Index Of Test4 is 3", 3, _fastTable.indexOf("Test4"));
	}
	
	@Test
	public void testPeek(){
		assertEquals("Peek Is Test1", "Test1", _fastTable.peek());
		assertTrue("Still Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testPeekFirst(){
		assertEquals("PeekFirst Is Test1", "Test1", _fastTable.peekFirst());
		assertTrue("Still Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testPeekLast(){
		assertEquals("PeekLast Is Test3", "Test3", _fastTable.peekLast());
		assertTrue("Still Contains Test3", _fastTable.contains("Test3"));
	}
	
	@Test
	public void testPoll(){
		assertEquals("Poll Is Test1", "Test1", _fastTable.poll());
		assertFalse("No Longer Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testPollFirst(){
		assertEquals("PollFirst Is Test1", "Test1", _fastTable.pollFirst());
		assertFalse("No Longer Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testPollLast(){
		assertEquals("PollLast Is Test3", "Test3", _fastTable.pollLast());
		assertFalse("No Longer Contains Test3", _fastTable.contains("Test3"));
	}
	
	@Test
	public void testPushPop(){
		_fastTable = new FastTable<String>();
		_fastTable.push("Test1");
		_fastTable.push("Test2");
		_fastTable.push("Test3");
		assertEquals("Pop Is Test3", "Test3", _fastTable.pop());
		assertFalse("No Longer Contains Test3", _fastTable.contains("Test3"));
	}
	
	@Test
	public void testRemove(){
		_fastTable.remove();
		assertFalse("No Longer Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testRemoveFirst(){
		_fastTable.removeFirst();
		assertFalse("No Longer Contains Test1", _fastTable.contains("Test1"));
	}
	
	@Test
	public void testRemoveFirstOccurance(){
		_fastTable.add("Test2");
		_fastTable.add("Test1");
		_fastTable.removeFirstOccurrence("Test1");
		assertEquals("Index Of Test1 is 3", 3, _fastTable.indexOf("Test1"));
	}
	
	@Test
	public void testRemoveLast(){
		_fastTable.removeLast();
		assertFalse("No Longer Contains Test3", _fastTable.contains("Test3"));
	}
	
	@Test
	public void testRemoveLastOccurance(){
		_fastTable.add("Test2");
		_fastTable.add("Test1");
		_fastTable.removeLastOccurrence("Test1");
		assertEquals("Index Of Test1 is 0", 0, _fastTable.indexOf("Test1"));
	}
	
	@Test
	public void testRemoveWithIndex(){
		_fastTable.remove(1);
		assertFalse("No Longer Contains Test2", _fastTable.contains("Test2"));
	}
}
