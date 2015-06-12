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

public class TextTest {

	private Text _text;
	
	@Before
	public void init(){
		_text = Text.valueOf("Test");
	}
	
	@Test
	public void testCharAt(){
		assertEquals("CharAt 0 Is T", 'T', _text.charAt(0));
		assertEquals("CharAt 1 Is e", 'e', _text.charAt(1));
		assertEquals("CharAt 2 Is s", 's', _text.charAt(2));
		assertEquals("CharAt 3 Is t", 't', _text.charAt(3));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testCharAtGreaterThanContentLengthThrowsException(){
		_text.charAt(9999);
	}
	
	@Test
	public void testConcat(){
		_text = _text.concat(Text.valueOf("Test"));
		assertEquals("Concat is TestTest", Text.valueOf("TestTest"), _text);
	}
	
	@Test
	public void testContentEquals(){
		assertTrue("Content Is Test", _text.contentEquals("Test"));
	}
	
	@Test
	public void testContentEqualsIgnoreCase(){
		assertTrue("Content Is TEST (Ignore Case)", _text.contentEqualsIgnoreCase("TEST"));
	}
	
	@Test
	public void testCopy(){
		Text text = _text.copy();
		assertFalse("Copied Text Instance Is DIfferent", text==_text);
		assertTrue("Copied Text Content Is The Same", text.contentEquals(_text));
	}
	
	@Test
	public void testDelete(){
		_text = _text.delete(1, 3);
		assertEquals("delete() Text Is Tt", Text.valueOf("Tt"), _text);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testDeleteThrowsExceptionWithEndGreatherThanContentLength(){
		_text.delete(1, 9999);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testDeleteThrowsExceptionWithStartGreaterThanEnd(){
		_text.delete(3, 2);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testDeleteThrowsExceptionWithStartLessThanZero(){
		_text.delete(-1, 2);
	}
	
	@Test
	public void testEndsWith(){
		assertTrue("Text Ends With t", _text.endsWith("t"));
		assertTrue("Text Ends With st", _text.endsWith("st"));
		assertTrue("Text Ends With est", _text.endsWith("est"));
		assertTrue("Text Ends With Test", _text.endsWith("Test"));
	}
	
	@Test
	public void testGetChars(){
		char[] chars = new char[4];
		_text.getChars(0, 4, chars, 0);
		assertEquals("getChars() is Test", Text.valueOf("Test"), Text.valueOf(chars));		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetCharsWithEndGreaterThanContentLengthThrowsException(){
		char[] chars = new char[4];
		_text.getChars(0, 9999, chars, 0);		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetCharsWithStartGreaterThanEndThrowsException(){
		char[] chars = new char[4];
		_text.getChars(4, 3, chars, 0);		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetCharsWithStartLessThanZeroThrowsException(){
		char[] chars = new char[4];
		_text.getChars(-1, 4, chars, 0);		
	}
	
	@Test
	public void testIndexOfWithChar(){
		assertEquals("Index of T is 0", 0, _text.indexOf('T'));
		assertEquals("Index of e is 1", 1, _text.indexOf('e'));
		assertEquals("Index of s is 2", 2, _text.indexOf('s'));
		assertEquals("Index of t is 3", 3, _text.indexOf('t'));
	}
	
	@Test
	public void testIndexOfWithCharSequence(){
		assertEquals("Index of Te is 0", 0, _text.indexOf("Te"));
		assertEquals("Index of es is 1", 1, _text.indexOf("es"));
		assertEquals("Index of st is 2", 2, _text.indexOf("st"));
	}
	
	@Test
	public void testIndexOfWithCharSequenceWithIndex(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Index of Te is 4", 4, _text.indexOf("Te",4));
		assertEquals("Index of es is 5", 5, _text.indexOf("es",4));
		assertEquals("Index of st is 6", 6, _text.indexOf("st",4));
	}
	
	@Test
	public void testIndexOfWithCharWithIndex(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Index of T is 4", 4, _text.indexOf('T',4));
		assertEquals("Index of e is 5", 5, _text.indexOf('e',4));
		assertEquals("Index of s is 6", 6, _text.indexOf('s',4));
		assertEquals("Index of t is 7", 7, _text.indexOf('t',4));
	}
	
	@Test
	public void testInsert(){
		_text = _text.insert(2, Text.valueOf("Test"));
		assertEquals("insert() Text Is TeTestst", Text.valueOf("TeTestst"), _text);
	}
	
	@Test
	public void testIntern(){
		_text = Text.intern("Test");
		Text text = Text.intern("Test");
		assertTrue("Text Instances Are The Same", _text==text);
	}
	
	@Test
	public void testIsBlank(){
		assertFalse("IsBlank() = False", _text.isBlank());
		_text = Text.valueOf("    ");
		assertTrue("IsBlank() = True", _text.isBlank());
	}
	
	@Test
	public void testIsBlankWithIndexAndLength(){
		_text = Text.valueOf("Test    Test");
		assertFalse("IsBlank() = False", _text.isBlank(0, 4));
		assertTrue("IsBlank() = True", _text.isBlank(4, 4));
		assertFalse("IsBlank() = False", _text.isBlank(8, 4));
	}
	
	@Test
	public void testLastIndexOfWithChar(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Last Index of T is 4", 4, _text.lastIndexOf('T'));
		assertEquals("Last Index of e is 5", 5, _text.lastIndexOf('e'));
		assertEquals("Last Index of s is 6", 6, _text.lastIndexOf('s'));
		assertEquals("Last Index of t is 7", 7, _text.lastIndexOf('t'));
	}
	
	@Test
	public void testLastIndexOfWithCharSequence(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Last Index of Te is 4", 4, _text.lastIndexOf("Te"));
		assertEquals("Last Index of es is 5", 5, _text.lastIndexOf("es"));
		assertEquals("Last Index of st is 6", 6, _text.lastIndexOf("st"));
	}
	
	@Test
	public void testLastIndexOfWithCharSequenceWithIndex(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Last Index of Te is 0", 0, _text.lastIndexOf("Te",3));
		assertEquals("Last Index of es is 1", 1, _text.lastIndexOf("es",3));
		assertEquals("Last Index of st is 2", 2, _text.lastIndexOf("st",3));
	}
	
	@Test
	public void testLastIndexOfWithCharWithIndex(){
		_text = Text.valueOf("TestTest");
		
		assertEquals("Index of T is 0", 0, _text.lastIndexOf('T',3));
		assertEquals("Index of e is 1", 1, _text.lastIndexOf('e',3));
		assertEquals("Index of s is 2", 2, _text.lastIndexOf('s',3));
		assertEquals("Index of t is 3", 3, _text.lastIndexOf('t',3));
	}
	
	@Test
	public void testLength(){
		assertEquals("Length Is 4", 4, _text.length());
	}
	
	@Test
	public void testPadLeft(){
		_text = _text.padLeft(10);
		assertEquals("padLeft() To 10", Text.valueOf("      Test"), _text);
	}
	
	@Test
	public void testPadLeftWithChar(){
		_text = _text.padLeft(10,'*');
		assertEquals("padLeft() To 10", Text.valueOf("******Test"), _text);
	}
	
	@Test
	public void testPadRight(){
		_text = _text.padRight(10);
		assertEquals("padRight() To 10", Text.valueOf("Test      "), _text);
	}
	
	@Test
	public void testPadRightWithChar(){
		_text = _text.padRight(10,'*');
		assertEquals("padRight() To 10", Text.valueOf("Test******"), _text);
	}
	
	@Test
	public void testPlus(){
		_text = _text.plus("Test");
		assertEquals("plus() Text is TestTest", Text.valueOf("TestTest"), _text);
	}
	
	@Test
	public void testReplace(){
		_text = _text.replace("es", "tt");
		assertEquals("replace() Text is Tttt", Text.valueOf("Tttt"), _text);
	}
	
	@Test
	public void testStartsWith(){
		assertTrue("Text Starts With T", _text.startsWith("T"));
		assertTrue("Text Starts With Te", _text.startsWith("Te"));
		assertTrue("Text Starts With Tes", _text.startsWith("Tes"));
		assertTrue("Text Starts With Test", _text.startsWith("Test"));
	}
	
	@Test
	public void testSubSequence(){
		CharSequence charSequence = _text.subSequence(1, 3);
		assertEquals("testSubSequence() es", Text.valueOf("es"), charSequence);		
	}
	
	@Test
	public void testSubText(){
		_text = _text.subtext(1);
		assertEquals("testSubText() est", Text.valueOf("est"), _text);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testSubTextWithEndGreatherThanContentLengthThrowsException(){
		_text.subtext(3, 9999);		
	}
	
	@Test
	public void testSubTextWithStartAndEnd(){
		_text = _text.subtext(1,3);
		assertEquals("testSubText() es", Text.valueOf("es"), _text);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testSubTextWithStartGreatherThanEndThrowsException(){
		_text.subtext(3, 2);		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testSubTextWithStartLessThanZeroThrowsException(){
		_text.subtext(-1);		
	}
	
	@Test
	public void testToLowerCase(){
		assertEquals("toLowerCase() test", Text.valueOf("test"), _text.toLowerCase());
	}
	
	@Test
	public void testToUpperCase(){
		assertEquals("toUpperCase() test", Text.valueOf("TEST"), _text.toUpperCase());
	}
	
	@Test
	public void testTrim(){
		_text = Text.valueOf("    Test    ").trim();
		assertEquals("trim() is Test", Text.valueOf("Test"), _text);
	}
	
	@Test
	public void testTrimEnd(){
		_text = Text.valueOf("    Test    ").trimEnd();
		assertEquals("trim() is     Test", Text.valueOf("    Test"), _text);
	}
	
	@Test
	public void testTrimStart(){
		_text = Text.valueOf("    Test    ").trimStart();
		assertEquals("trim() is Test    ", Text.valueOf("Test    "), _text);
	}
	
	@Test
	public void testValueOfBoolean(){
		assertEquals("valueOf(false) is false", Text.valueOf("false"), Text.valueOf(false));
		assertEquals("valueOf(true) is true", Text.valueOf("true"), Text.valueOf(true));		
	}
	
	@Test
	public void testValueOfChar(){
		assertEquals("valueOf('C') is C", Text.valueOf("C"), Text.valueOf('C'));		
	}
	
	@Test
	public void testValueOfCharSequence(){
		assertEquals("valueOf(\"Test\") is 1", Text.valueOf("Test"), Text.valueOf((CharSequence)"Test"));
	}
	
	@Test
	public void testValueOfCharWithLength(){
		assertEquals("valueOf('C',5) is CCCCC", Text.valueOf("CCCCC"), Text.valueOf('C',5));		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testValueOfCharWithLengthLessThanZeroThrowsException(){
		Text.valueOf('C',-1);		
	}
	
	@Test
	public void testValueOfDouble(){
		assertEquals("valueOf(1.0) is 1.0", Text.valueOf("1.0"), Text.valueOf(1.0));
	}

	@Test
	public void testValueOfFloat(){
		assertEquals("valueOf(1.0f) is 1.0", Text.valueOf("1.0"), Text.valueOf(1.0f));
	}
	
	@Test
	public void testValueOfInt(){
		assertEquals("valueOf(1) is 1", Text.valueOf("1"), Text.valueOf(1));
	}
	
	@Test
	public void testValueOfLong(){
		assertEquals("valueOf(1L) is 1", Text.valueOf("1"), Text.valueOf(1L));
	}
	
	@Test
	public void testValueOfPrimitiveCharArray(){
		assertEquals("valueOf(char[]) is Test", Text.valueOf("Test"), Text.valueOf(new char[]{'T','e','s','t'}));
	}
	
}
