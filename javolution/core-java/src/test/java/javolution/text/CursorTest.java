/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CursorTest {

	private Cursor _cursor;
	
	@Before
	public void init(){
		_cursor = new Cursor();
	}
	
	@Test
	public void testAtEnd(){
		assertFalse("String at Cursor is Test", _cursor.atEnd("Test"));
		_cursor.setIndex(4);
		assertTrue("String at Cursor is Test", _cursor.atEnd("Test"));
	}
	
	@Test
	public void testAtWithChar(){
		assertFalse("Char at Cursor is s", _cursor.at('s', "Test"));
		_cursor.setIndex(2);
		assertTrue("Char at Cursor is s", _cursor.at('s', "Test"));
	}
	
	@Test
	public void testAtWithCharSet(){
		assertTrue("At Cursor is Whitespace", _cursor.at(CharSet.WHITESPACES, " Test"));
		_cursor.setIndex(2);
		assertFalse("At Cursor is Whitespace", _cursor.at(CharSet.WHITESPACES, " Test"));
	}
	
	@Test
	public void testAtWithString(){
		assertFalse("String at Cursor is Test", _cursor.at("Test", " Test"));
		_cursor.setIndex(1);
		assertTrue("String at Cursor is Test", _cursor.at("Test", " Test"));
	}
	
	@Test
	public void testCurrentChar(){
		assertEquals("Current Char Is T", 'T', _cursor.currentChar("Test"));
		_cursor.setIndex(3);
		assertEquals("Current Char Is t", 't', _cursor.currentChar("Test"));
	}
	
	@Test
	public void testEquals(){
		assertFalse("Equals() Is False", _cursor.equals(null));
		assertFalse("Equals() Is False", _cursor.equals("Test"));
		assertTrue("Equals() Is True", _cursor.equals(_cursor));
		Cursor otherCursor = new Cursor(1);
		assertFalse("Equals() Is False", _cursor.equals(otherCursor));
		_cursor.setIndex(1);
		assertTrue("Equals() Is True", _cursor.equals(otherCursor));
		
	}
	
	@Test
	public void testGetAndSetIndex(){
		assertEquals("Cursor Index: 0", 0, _cursor.getIndex());
		_cursor.setIndex(9999);
		assertEquals("Cursor Index: 9999", 9999, _cursor.getIndex());
	}
	
	@Test
	public void testHead(){
		_cursor.setIndex(4);
		assertEquals("Head Is Test", "Test", _cursor.head("TesttseT"));
	}
	
	@Test
	public void testIncrement(){
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		_cursor.increment();
		assertEquals("Cursor Position: 0", 1, _cursor.getIndex());
		_cursor.increment(2);
		assertEquals("Cursor Position: 0", 3, _cursor.getIndex());
	}
	
	@Test
	public void testInitializeCursorWithIndex(){
		_cursor = new Cursor(5);
		assertEquals("Cursor Position: 5", 5, _cursor.getIndex());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInitializeCursorWithIndexWithNegativeStartingIndexThrowsException(){
		new Cursor(-5);
	}
	
	@Test
	public void testNextChar(){
		assertEquals("Current Char Is T", 'T', _cursor.nextChar("Test"));
		assertEquals("Current Char Is e", 'e', _cursor.nextChar("Test"));
		assertEquals("Current Char Is s", 's', _cursor.nextChar("Test"));
		assertEquals("Current Char Is t", 't', _cursor.nextChar("Test"));
	}
	
	@Test
	public void testNextToken(){
		assertEquals("Current Char Is T", "Test", _cursor.nextToken("Test tseT Test", ' '));
		assertEquals("Current Char Is T", "tseT", _cursor.nextToken("Test tseT Test", ' '));
		assertEquals("Current Char Is T", "Test", _cursor.nextToken("Test tseT Test", ' '));
	}
	
	@Test
	public void testNextTokenWithCharSet(){
		assertEquals("Current Char Is T", "Test", _cursor.nextToken("Test tseT Test", CharSet.WHITESPACES));
		assertEquals("Current Char Is T", "tseT", _cursor.nextToken("Test tseT Test", CharSet.WHITESPACES));
		assertEquals("Current Char Is T", "Test", _cursor.nextToken("Test tseT Test", CharSet.WHITESPACES));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetIndexWithNegativeValueForIndexThrowsException(){
		_cursor.setIndex(-5);
	}
	
	@Test
	public void testSkip(){
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertFalse("Skip() Is False", _cursor.skip(' ', "Test "));
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertTrue("Skip() Is True", _cursor.skip(' ', " Test"));
		assertEquals("Cursor Position: 1", 1, _cursor.getIndex());
	}
	
	@Test
	public void testSkipWithCharSequence(){
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertFalse("Skip() Is False", _cursor.skip("A", "TestA"));
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertTrue("Skip() Is True", _cursor.skip("Test", "TestA"));
		assertEquals("Cursor Position: 4", 4, _cursor.getIndex());
	}
	
	@Test
	public void testSkipWithCharSet(){
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertFalse("Skip() Is False", _cursor.skip(CharSet.WHITESPACES, "Test "));
		assertEquals("Cursor Position: 0", 0, _cursor.getIndex());
		assertTrue("Skip() Is True", _cursor.skip(CharSet.WHITESPACES, " Test"));
		assertEquals("Cursor Position: 1", 1, _cursor.getIndex());
	}
	
	@Test
	public void testTail(){
		_cursor.setIndex(4);
		assertEquals("Tail Is tseT", "tseT", _cursor.tail("TesttseT"));
	}
}
