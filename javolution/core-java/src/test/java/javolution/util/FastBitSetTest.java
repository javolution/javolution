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

public class FastBitSetTest {

	private FastBitSet _fastBitSet1248;
	private FastBitSet _fastBitSet3567;
	private FastBitSet _fastBitSetAll;
	private FastBitSet _fastBitSetNone;
	
	@Before
	public void init(){
		_fastBitSet1248 = FastBitSet.of(Index.of(1), Index.of(2), Index.of(4), Index.of(8));
		_fastBitSet3567 = FastBitSet.of(Index.of(3), Index.of(5), Index.of(6), Index.of(7));
		_fastBitSetAll = FastBitSet.of(Index.of(1), Index.of(2), Index.of(3), Index.of(4), 
				Index.of(5), Index.of(6), Index.of(7), Index.of(8));
		_fastBitSetNone = new FastBitSet();
	}
	
	@Test
	public void testAnd(){
		FastBitSet resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.and(_fastBitSet3567);
		
		for(int i = 1; i <= 8; i++){
			assertFalse(String.format("Bit %d = False", i), resultBitSet.get(i));
		}
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.and(_fastBitSetNone);
		
		for(int i = 1; i <= 8; i++){
			assertFalse(String.format("Bit %d = False", i), resultBitSet.get(i));
		}
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.and(_fastBitSetAll);
		
		assertTrue("Bit 1 = True", resultBitSet.get(1));
		assertTrue("Bit 2 = True", resultBitSet.get(2));
		assertTrue("Bit 4 = True", resultBitSet.get(4));
		assertTrue("Bit 8 = True", resultBitSet.get(8));
	}
	
	@Test
	public void testCardinality(){
		assertEquals("Cardinality: FastBitSet 1,2,4,8 = 4", 4, _fastBitSet1248.cardinality());
		assertEquals("Cardinality: FastBitSet 3,5,6,7 = 4", 4, _fastBitSet3567.cardinality());
		assertEquals("Cardinality: FastBitSet 1,2,3,4,5,6,7,8 = 8", 8, _fastBitSetAll.cardinality());
		assertEquals("Cardinality: FastBitSet <Empty> = 0", 0, _fastBitSetNone.cardinality());
	}
	
	@Test
	public void testFlip(){
		for(int i = 0; i < 8; i++){
			assertFalse(String.format("Bit %d = False", i), _fastBitSetNone.get(i));
		}
		
		_fastBitSetNone.flip(0, 8);

		for(int i = 0; i < 8; i++){
			assertTrue(String.format("Bit %d = True", i), _fastBitSetNone.get(i));
		}
	}
	
	@Test
	public void testIsEmpty(){
		assertTrue("IsEmpty: FastBitSet <Empty> = 0", _fastBitSetNone.isEmpty());
		assertFalse("IsEmpty: FastBitSet 1,2,3,4,5,6,7,8 = 8", _fastBitSetAll.isEmpty());		
	}
	
	@Test
	public void testNextClearBit(){
		assertEquals("Next Clear Bit Is 0", 0, _fastBitSetNone.nextClearBit(0));
		assertEquals("Next Clear Bit = 9, From 1", 9, _fastBitSetAll.nextClearBit(1));
		assertEquals("Next Clear Bit = 4, From 3", 4, _fastBitSet3567.nextClearBit(3));
	}
	
	@Test
	public void testNextSetBit(){
		assertEquals("No Next Set Bit", -1, _fastBitSetNone.nextSetBit(0));
		assertEquals("Next Set Bit = 1", 1, _fastBitSetAll.nextSetBit(0));
		assertEquals("Next Set Bit = 6, From 4", 5, _fastBitSet3567.nextSetBit(4));
	}
	
	@Test
	public void testOf(){
		assertTrue("Bit 1 = True", _fastBitSet1248.get(1));
		assertTrue("Bit 2 = True", _fastBitSet1248.get(2));
		assertFalse("Bit 3 = False", _fastBitSet1248.get(3));
		assertTrue("Bit 4 = True", _fastBitSet1248.get(4));
		assertFalse("Bit 5 = False", _fastBitSet1248.get(5));
		assertFalse("Bit 6 = False", _fastBitSet1248.get(6));
		assertFalse("Bit 7 = False", _fastBitSet1248.get(7));
		assertTrue("Bit 8 = True", _fastBitSet1248.get(8));		
	}
	
	@Test
	public void testOr(){
		FastBitSet resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.or(_fastBitSet3567);
		
		for(int i = 1; i <= 8; i++){
			assertTrue(String.format("Bit %d = True", i), resultBitSet.get(i));
		}
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.or(_fastBitSetAll);
		
		for(int i = 1; i <= 8; i++){
			assertTrue(String.format("Bit %d = True", i), resultBitSet.get(i));
		}
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.or(_fastBitSetNone);
		
		assertTrue("Bit 1 = True", resultBitSet.get(1));
		assertTrue("Bit 2 = True", resultBitSet.get(2));
		assertTrue("Bit 4 = True", resultBitSet.get(4));
		assertTrue("Bit 8 = True", resultBitSet.get(8));
	}
	
	@Test
	public void testXOr(){
		FastBitSet resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.xor(_fastBitSet3567);
		
		for(int i = 1; i <= 8; i++){
			assertTrue(String.format("Bit %d = True", i), resultBitSet.get(i));
		}
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.xor(_fastBitSetAll);
		
		assertTrue("Bit 3 = True", resultBitSet.get(3));
		assertTrue("Bit 5 = True", resultBitSet.get(5));
		assertTrue("Bit 6 = True", resultBitSet.get(6));
		assertTrue("Bit 7 = True", resultBitSet.get(7));
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.xor(_fastBitSetNone);
		
		assertTrue("Bit 1 = True", resultBitSet.get(1));
		assertTrue("Bit 2 = True", resultBitSet.get(2));
		assertTrue("Bit 4 = True", resultBitSet.get(4));
		assertTrue("Bit 8 = True", resultBitSet.get(8));
		
		resultBitSet = new FastBitSet();
		resultBitSet.addAll(_fastBitSet1248);
		resultBitSet.xor(_fastBitSet1248);

		assertFalse("Bit 1 = False", resultBitSet.get(1));
		assertFalse("Bit 2 = False", resultBitSet.get(2));
		assertFalse("Bit 4 = False", resultBitSet.get(4));
		assertFalse("Bit 8 = False", resultBitSet.get(8));
	}
	
	@Test
	public void testClear(){
		_fastBitSetAll.clear();
		assertTrue("FastBitSet Is Empty", _fastBitSetAll.isEmpty());
		assertEquals("FastBitSet Size == 0 ", 0,  _fastBitSetAll.size());
	}
}
