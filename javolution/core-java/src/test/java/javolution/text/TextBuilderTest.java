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

public class TextBuilderTest {

	private TextBuilder _textBuilder;
	
	@Before
	public void init(){
		_textBuilder = new TextBuilder();
	}
	
	@Test
	public void testAppendBoolean(){
		_textBuilder.append(true);
		assertEquals("TextBuilder String Is true", _textBuilder.toString(), "true");
	}
	
	@Test
	public void testAppendChar(){
		_textBuilder.append('1');
		assertEquals("TextBuilder String Is 1", _textBuilder.toString(), "1");
	}
	
	@Test
	public void testAppendChars(){
		char[] chars = {'T','e','s','t'};
		_textBuilder.append(chars);
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testAppendCharSequence(){
		CharSequence charSequence = new CharArray("Test");
		_textBuilder.append(charSequence);
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testAppendDouble(){
		_textBuilder.append(1.0);
		assertEquals("TextBuilder String Is 1.0", _textBuilder.toString(), "1.0");
	}
	
	@Test
	public void testAppendFloat(){
		_textBuilder.append(1.0f);
		assertEquals("TextBuilder String Is 1.0", _textBuilder.toString(), "1.0");
	}
	
	@Test
	public void testAppendInt(){
		_textBuilder.append(1);
		assertEquals("TextBuilder String Is 1", _textBuilder.toString(), "1");
	}
	
	@Test
	public void testAppendLong(){
		_textBuilder.append(1L);
		assertEquals("TextBuilder String Is 1", _textBuilder.toString(), "1");
	}
	
	@Test
	public void testAppendString(){
		_textBuilder.append("Test");
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testAppendText(){
		_textBuilder.append(Text.valueOf("Test"));
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testCharAt(){
		_textBuilder.append("Test");
		assertEquals("TextBuilder CharAt 2 is s", _textBuilder.charAt(2), 's');		
	}
	
	@Test
	public void testClear(){
		_textBuilder.append("Test");
		_textBuilder.clear();
		assertEquals("TextBuilder Length Is 0", _textBuilder.length(), 0);
	}
	
	@Test
	public void testContentEquals(){
		_textBuilder.append("Test");
		assertTrue("Content Is Test", _textBuilder.contentEquals("Test"));
	}
	
	@Test
	public void testDelete(){
		_textBuilder.append("Teblahst");
		_textBuilder.delete(2, 6);
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testEquals(){
		assertTrue("TextBuilder Is Equal To Itself", _textBuilder.equals(_textBuilder));
		_textBuilder.append("Test");
		TextBuilder anotherTextBuilder = new TextBuilder("Test");
		assertTrue("TextBuilder Is Equal To Another TextBuilder With Same Text", _textBuilder.equals(anotherTextBuilder));
		anotherTextBuilder = new TextBuilder("Another");
		assertFalse("TextBuilder Is NOT Equal To Another TextBuilder", _textBuilder.equals(anotherTextBuilder));
	}
	
	@Test
	public void testGetChars(){
		char[] chars = new char[4];
		_textBuilder.append("testTesttest");
		_textBuilder.getChars(4, 8, chars, 0);
		assertEquals("Chars Value Is Test", String.valueOf(chars), "Test");
	}
	
	@Test
	public void testInsert(){
		_textBuilder.append("Tt");
		_textBuilder.insert(1, "es");
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testLength(){
		assertEquals("TextBuilder Length Is 0", _textBuilder.length(), 0);
		_textBuilder.append("T");
		assertEquals("TextBuilder Length Is 1", _textBuilder.length(), 1);		
	}
	
	@Test
	public void testReverse(){
		_textBuilder.append("tseT");
		TextBuilder reverseBuilder = _textBuilder.reverse();
		assertEquals("Reversed TextBuilder String Is Test", reverseBuilder.toString(), "Test");
	}
	
	@Test
	public void testSetCharAt(){
		_textBuilder.append("Tsst");
		_textBuilder.setCharAt(1, 'e');
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testSetLength(){
		_textBuilder.append("Test");
		_textBuilder.setLength(1);
		assertEquals("TextBuilder String Is T", _textBuilder.toString(), "T");
		assertEquals("TextBuilder Length is 1", _textBuilder.length(), 1);
	}
	
	@Test
	public void testSubsequence(){
		_textBuilder.append("testTesttest");
		CharSequence charSequence = _textBuilder.subSequence(4, 8);
		assertEquals("Charsequence Value Is Test", String.valueOf(charSequence), "Test");
	}
	
	@Test
	public void testToCharArray(){
		_textBuilder.append("Test");
		assertEquals("TextBuilder CharArray Is Test", _textBuilder.toCharArray(), new CharArray("Test"));
	}
	
	@Test
	public void testToString(){
		_textBuilder.append("Test");
		assertEquals("TextBuilder String Is Test", _textBuilder.toString(), "Test");
	}
	
	@Test
	public void testToText(){
		_textBuilder.append("Test");
		assertEquals("TextBuilder Text Is Test", _textBuilder.toText(), Text.valueOf("Test"));
	}
}
