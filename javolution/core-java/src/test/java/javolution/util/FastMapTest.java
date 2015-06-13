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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javolution.util.function.Consumer;
import javolution.util.function.Equalities;

import org.junit.Before;
import org.junit.Test;

public class FastMapTest {

	private FastMap<String,String> _fastMap;
	
	@Before
	public void init(){
		_fastMap = new FastMap<String,String>();
	}
	
	@Test
	public void testCaseInsensitiveMapWithLexicalCaseInsensitive(){
		_fastMap = new FastMap<String,String>(Equalities.LEXICAL_CASE_INSENSITIVE);
		_fastMap.put("TestKey1", "TestValue1");
		String result = _fastMap.get("TestKey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);
		result = _fastMap.get("TESTKEY1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);
		result = _fastMap.get("Testkey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);		
	}
	
	@Test
	public void testClear(){
		_fastMap.put("TestKey1", "TestValue1");
		_fastMap.put("TestKey2", "TestValue2");
		_fastMap.put("TestKey3", "TestValue3");		
		_fastMap.clear();
		assertEquals("Size Is 0 After Clear", _fastMap.size(), 0);
		assertTrue("Map Is Empty After Clear", _fastMap.isEmpty());
	}
	
	@Test
	public void testClearWithAtomicView(){
		_fastMap = _fastMap.atomic();
		_fastMap.put("TestKey1", "TestValue1");
		_fastMap.put("TestKey2", "TestValue2");
		_fastMap.put("TestKey3", "TestValue3");		
		_fastMap.clear();
		assertEquals("Size Is 0 After Clear", 0, _fastMap.size());
		assertTrue("Map Is Empty After Clear", _fastMap.isEmpty());
	}
	
	@Test
	public void testContainsKey(){
		_fastMap.put("TestKey", "TestValue");
		assertTrue("FastMap Contains Key TestKey", _fastMap.containsKey("TestKey"));		
	}
	
	@Test
	public void testContainsValue(){
		_fastMap.put("TestKey", "TestValue");
		assertTrue("FastMap Contains Value TestValue", _fastMap.containsValue("TestValue"));		
	}
	
	@Test
	public void testEntrySetAndMapRetainsInsertOrder(){
		_fastMap.put("TestKey1", "TestValue1");
		_fastMap.put("TestKey2", "TestValue2");
		_fastMap.put("TestKey3", "TestValue3");
		
		Set<Entry<String,String>> entrySet = _fastMap.entrySet();
		Iterator<Entry<String,String>> entrySetIterator = entrySet.iterator();
		
		Entry<String,String> entry = entrySetIterator.next();
		assertEquals("Key 1 Is TestKey1", "TestKey1", entry.getKey());
		assertEquals("Value 1 Is TestValue1", "TestValue1", entry.getValue());
		
		entry = entrySetIterator.next();
		assertEquals("Key 2 Is TestKey2", "TestKey2", entry.getKey());
		assertEquals("Value 2 Is TestValue2", "TestValue2", entry.getValue());
		
		entry = entrySetIterator.next();
		assertEquals("Key 3 Is TestKey3", "TestKey3", entry.getKey());
		assertEquals("Value 3 Is TestValue3", "TestValue3", entry.getValue());
	}
	
	@Test
	public void testGetPutRemoveOperations(){
		String result = _fastMap.get("TestKey1");
		assertNull("Reset of Get TestKey1 Should Be Null", result);
		_fastMap.put("TestKey1", "TestValue1");
		result = _fastMap.get("TestKey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);
		_fastMap.remove("TestKey1","TestValue2");
		result = _fastMap.get("TestKey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);		
		_fastMap.remove("TestKey1");
		result = _fastMap.get("TestKey1");
		assertNull("Reset of Get TestKey1 Should Be Null", result);
	}
	
	@Test
	public void testIsEmpty(){
		assertTrue("FastMap Should Be Empty", _fastMap.isEmpty());
		_fastMap.put("TestKey1", "TestValue1");
		assertFalse("FastMap Should NOT Be Empty", _fastMap.isEmpty());		
	}
	
	@Test
	public void testKeySet(){
		_fastMap.put("TestKey1", "TestValue1");		
		Set<String> keySet = _fastMap.keySet();
		assertEquals("Key Set Size Should Be 1", keySet.size(), 1);
		assertEquals("Key Set Value Is TestKey1", keySet.toArray()[0], "TestKey1");
	}
	
	@Test
	public void testPutAll(){
		Map<String,String> map = new FastMap<String,String>();
		map.put("TestKey1", "TestValue1");
		map.put("TestKey2", "TestValue2");
		map.put("TestKey3", "TestValue3");
		
		_fastMap.putAll(map);
		
		String result = _fastMap.get("TestKey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);
		result = _fastMap.get("TestKey2");
		assertEquals("Result Should Equal TestValue2", "TestValue2", result);
		result = _fastMap.get("TestKey3");
		assertEquals("Result Should Equal TestValue3", "TestValue3", result);
	}
	
	@Test
	public void testPutIfAbsent(){
		_fastMap.put("TestKey1", "TestValue1");
		_fastMap.putIfAbsent("TestKey1", "TestValue2");
		String result = _fastMap.get("TestKey1");
		assertEquals("Result Should Equal TestValue1", "TestValue1", result);		
	}
	
	@Test
	public void testSize(){
		assertEquals("Size Equals 0", _fastMap.size(), 0);
		_fastMap.put("TestKey1", "TestValue1");
		assertEquals("Size Equals 1", _fastMap.size(), 1);
		_fastMap.put("TestKey2", "TestValue2");
		assertEquals("Size Equals 2", _fastMap.size(), 2);
		_fastMap.put("TestKey3", "TestValue3");
		assertEquals("Size Equals 3", _fastMap.size(), 3);
	}
	
	@Test
	public void testUpdate(){
		_fastMap.put("TestKey1", "TestValue1");
		_fastMap.put("TestKey2", "TestValue1");
		_fastMap.put("TestKey3", "TestValue1");
		
		Consumer<Map<String,String>> value2Update = new Consumer<Map<String,String>>() {  
			public void accept(Map<String,String> view) {
				Iterator<Entry<String,String>> it = view.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String,String> entry = it.next();
					entry.setValue("TestValue2");
				}
			}
		};
		
		_fastMap.update(value2Update);
		
		String result = _fastMap.get("TestKey1");		
		assertEquals("Result for TestKey1 Is TestValue2", "TestValue2", result);
		result = _fastMap.get("TestKey2");
		assertEquals("Result for TestKey2 Is TestValue2", "TestValue2", result);
		result = _fastMap.get("TestKey3");
		assertEquals("Result for TestKey3 Is TestValue2", "TestValue2", result);		
	}
	
}
