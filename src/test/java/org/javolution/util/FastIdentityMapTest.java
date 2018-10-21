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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.javolution.util.function.Order;
import org.junit.Before;
import org.junit.Test;

public class FastIdentityMapTest {

	private AbstractMap<Class<?>,Class<?>> _fastIdentityMap;
	
	@Before
	public void init(){
		_fastIdentityMap = new FastMap<Class<?>,Class<?>>(Order.identity());
	}

	@Test
	public void testClear(){
		_fastIdentityMap.put(Integer.class, int.class);
		_fastIdentityMap.put(Boolean.class, boolean.class);
		_fastIdentityMap.put(Long.class, long.class);
		_fastIdentityMap.clear();
		assertEquals("Size Is 0 After Clear", _fastIdentityMap.size(), 0);
		assertTrue("Map Is Empty After Clear", _fastIdentityMap.isEmpty());
	}
	
	@Test
	public void testClearWithAtomicView(){
		_fastIdentityMap = _fastIdentityMap.atomic();
		_fastIdentityMap.put(Integer.class, int.class);
		_fastIdentityMap.put(Boolean.class, boolean.class);
		_fastIdentityMap.put(Long.class, long.class);
		_fastIdentityMap.clear();
		assertEquals("Size Is 0 After Clear", 0, _fastIdentityMap.size());
		assertTrue("Map Is Empty After Clear", _fastIdentityMap.isEmpty());
	}
	
	@Test
	public void testContainsKey(){
		_fastIdentityMap.put(Integer.class, int.class);
		assertTrue("FastMap Contains Key TestKey", _fastIdentityMap.containsKey(Integer.class));
	}
	
	@Test
	public void testContainsValue(){
		_fastIdentityMap.put(Integer.class, int.class);
		assertTrue("FastMap Contains Value TestValue", _fastIdentityMap.containsValue(int.class));
	}
	
	@Test
	public void testEntrySetAndMapRetainsInsertOrder(){
        _fastIdentityMap = _fastIdentityMap.linked();

        _fastIdentityMap.put(Integer.class, int.class);
		_fastIdentityMap.put(Boolean.class, boolean.class);
		_fastIdentityMap.put(Long.class, long.class);
		
		Set<Entry<Class<?>,Class<?>>> entrySet = _fastIdentityMap.entrySet();
		Iterator<Entry<Class<?>,Class<?>>> entrySetIterator = entrySet.iterator();
		
		Entry<Class<?>,Class<?>> entry = entrySetIterator.next();
		assertEquals("Key 1 Is Integer.class", Integer.class, entry.getKey());
		assertEquals("Value 1 Is int.class", int.class, entry.getValue());
		
		entry = entrySetIterator.next();
		assertEquals("Key 2 Is Boolean.class", Boolean.class, entry.getKey());
		assertEquals("Value 2 Is boolean.class", boolean.class, entry.getValue());
		
		entry = entrySetIterator.next();
		assertEquals("Key 3 Is Long.class", Long.class, entry.getKey());
		assertEquals("Value 3 Is long.class", long.class, entry.getValue());
	}
	
	@Test
	public void testGetPutRemoveOperations(){
		Class<?> result = _fastIdentityMap.get(Integer.class);
		assertNull("Reset of Get TestKey1 Should Be Null", result);
		_fastIdentityMap.put(Integer.class, int.class);
		result = _fastIdentityMap.get(Integer.class);
		assertEquals("Result Should Equal int.class", int.class, result);
		_fastIdentityMap.remove(Integer.class, long.class);
		result = _fastIdentityMap.get(Integer.class);
		assertEquals("Result Should Equal int.class", int.class, result);
		_fastIdentityMap.remove(Integer.class);
		result = _fastIdentityMap.get(Integer.class);
		assertNull("Reset of Get Integer.class Should Be Null", result);
	}
	
	@Test
	public void testIsEmpty(){
		assertTrue("FastMap Should Be Empty", _fastIdentityMap.isEmpty());
		_fastIdentityMap.put(Integer.class, int.class);
		assertFalse("FastMap Should NOT Be Empty", _fastIdentityMap.isEmpty());
	}
	
	@Test
	public void testKeySet(){
		_fastIdentityMap.put(Integer.class, int.class);
		Set<Class<?>> keySet = _fastIdentityMap.keySet();
		assertEquals("Key Set Size Should Be 1", keySet.size(), 1);
		assertEquals("Key Set Value Is Integer.class", keySet.toArray()[0], Integer.class);
	}
	
	@Test
	public void testPutAll(){
        _fastIdentityMap = _fastIdentityMap.linked().with(Integer.class, int.class)
                .with(Boolean.class, boolean.class).with(Long.class, long.class);
		
		Class<?> result = _fastIdentityMap.get(Integer.class);
		assertEquals("Result Should Equal int.class", int.class, result);
		result = _fastIdentityMap.get(Boolean.class);
		assertEquals("Result Should Equal boolean.class", boolean.class, result);
		result = _fastIdentityMap.get(Long.class);
		assertEquals("Result Should Equal TestValue3", long.class, result);
	}
	
	@Test
	public void testPutIfAbsent(){
		_fastIdentityMap.put(Integer.class, int.class);
		_fastIdentityMap.putIfAbsent(Integer.class, long.class);
		Class<?> result = _fastIdentityMap.get(Integer.class);
		assertEquals("Result Should Equal int.class", int.class, result);
	}
	
	@Test
	public void testSize(){
		assertEquals("Size Equals 0", _fastIdentityMap.size(), 0);
		_fastIdentityMap.put(Integer.class, int.class);
		assertEquals("Size Equals 1", _fastIdentityMap.size(), 1);
		_fastIdentityMap.put(Boolean.class, boolean.class);
		assertEquals("Size Equals 2", _fastIdentityMap.size(), 2);
		_fastIdentityMap.put(Long.class, long.class);
		assertEquals("Size Equals 3", _fastIdentityMap.size(), 3);
	}
	
	
}
