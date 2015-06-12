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

public class FastSortedTableTest {

	private FastSortedTable<String> _fastSortedTable;

	@Before
	public void init(){
		_fastSortedTable = new FastSortedTable<String>();
		_fastSortedTable.add("Test1");
		_fastSortedTable.add("Test2");
		_fastSortedTable.add("Test3");
	}

	@Test
	public void testAdd(){
		_fastSortedTable.add("Test");
		assertTrue("Table Contains Test", _fastSortedTable.contains("Test"));
	}

	@Test
	public void testAddLast(){
		_fastSortedTable.addLast("Test4");
		assertEquals("Index Of Test4 is 3", 3, _fastSortedTable.indexOf("Test4"));
	}

	@Test
	public void testClear(){
		_fastSortedTable.clear();
		assertEquals("Table Size Is 0", 0 , _fastSortedTable.size());
		assertTrue("Table Is Empty", _fastSortedTable.isEmpty());
	}

	@Test
	public void testGetFirst(){
		assertEquals("First Is Test1", "Test1", _fastSortedTable.getFirst());
	}

	@Test
	public void testGetLast(){
		assertEquals("Last Is Test3", "Test3", _fastSortedTable.getLast());
	}

	@Test
	public void testIndexOf(){
		assertEquals("Index Of Test2 is 1", 1, _fastSortedTable.indexOf("Test2"));
	}

	@Test
	public void testIsEmpty(){
		_fastSortedTable = new FastSortedTable<String>();
		assertTrue("Table Is Empty", _fastSortedTable.isEmpty());
		_fastSortedTable.add("Test");
		assertFalse("Table Is NOT Empty", _fastSortedTable.isEmpty());
	}

	@Test
	public void testLastIndexOf(){
		_fastSortedTable.add("Test2");
		_fastSortedTable.add("Test1");
		assertEquals("LastIndexOf Test1 is 1", 1, _fastSortedTable.lastIndexOf("Test1"));
	}

	@Test
	public void testOffer(){
		assertTrue("Offer Accepted", _fastSortedTable.offer("Test4"));
		assertEquals("Index Of Test4 is 3", 3, _fastSortedTable.indexOf("Test4"));
	}

	@Test
	public void testOfferLast(){
		assertTrue("Offer Accepted", _fastSortedTable.offerLast("Test4"));
		assertEquals("Index Of Test4 is 3", 3, _fastSortedTable.indexOf("Test4"));
	}

	@Test
	public void testPeek(){
		assertEquals("Peek Is Test1", "Test1", _fastSortedTable.peek());
		assertTrue("Still Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testPeekFirst(){
		assertEquals("PeekFirst Is Test1", "Test1", _fastSortedTable.peekFirst());
		assertTrue("Still Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testPeekLast(){
		assertEquals("PeekLast Is Test3", "Test3", _fastSortedTable.peekLast());
		assertTrue("Still Contains Test3", _fastSortedTable.contains("Test3"));
	}

	@Test
	public void testPoll(){
		assertEquals("Poll Is Test1", "Test1", _fastSortedTable.poll());
		assertFalse("No Longer Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testPollFirst(){
		assertEquals("PollFirst Is Test1", "Test1", _fastSortedTable.pollFirst());
		assertFalse("No Longer Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testPollLast(){
		assertEquals("PollLast Is Test3", "Test3", _fastSortedTable.pollLast());
		assertFalse("No Longer Contains Test3", _fastSortedTable.contains("Test3"));
	}

	@Test
	public void testPushPop(){
		_fastSortedTable = new FastSortedTable<String>();
		_fastSortedTable.push("Test1");
		_fastSortedTable.push("Test2");
		_fastSortedTable.push("Test3");
		assertEquals("Pop Is Test3", "Test3", _fastSortedTable.pop());
		assertFalse("No Longer Contains Test3", _fastSortedTable.contains("Test3"));
	}

	@Test
	public void testRemove(){
		_fastSortedTable.remove();
		assertFalse("No Longer Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testRemoveFirst(){
		_fastSortedTable.removeFirst();
		assertFalse("No Longer Contains Test1", _fastSortedTable.contains("Test1"));
	}

	@Test
	public void testRemoveFirstOccurance(){
		_fastSortedTable.add("Test2");
		_fastSortedTable.add("Test1");
		_fastSortedTable.removeFirstOccurrence("Test1");
		assertEquals("Index Of Test1 is 0", 0, _fastSortedTable.indexOf("Test1"));
	}

	@Test
	public void testRemoveLast(){
		_fastSortedTable.removeLast();
		assertFalse("No Longer Contains Test3", _fastSortedTable.contains("Test3"));
	}

	@Test
	public void testRemoveLastOccurance(){
		_fastSortedTable.add("Test2");
		_fastSortedTable.add("Test1");
		_fastSortedTable.removeLastOccurrence("Test1");
		assertEquals("Index Of Test1 is 0", 0, _fastSortedTable.indexOf("Test1"));
	}

	@Test
	public void testRemoveWithIndex(){
		_fastSortedTable.remove(1);
		assertFalse("No Longer Contains Test2", _fastSortedTable.contains("Test2"));
	}
}
