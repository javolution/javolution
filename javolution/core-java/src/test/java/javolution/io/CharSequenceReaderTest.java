/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javolution.text.CharArray;
import javolution.text.Text;
import javolution.text.TextBuilder;

import org.junit.Before;
import org.junit.Test;

public class CharSequenceReaderTest {

	private CharSequenceReader _charSequenceReader;
	
	@Before
	public void init(){
		_charSequenceReader = new CharSequenceReader("Test");
	}
	
	@Test
	public void testCloseEliminatesReferenceToInput(){
		_charSequenceReader.close();
		assertNull("Input Is Null After Close", _charSequenceReader.getInput());
	}
	
	@Test
	public void testGetInput(){
		assertEquals("Input Should Be Test", "Test", _charSequenceReader.getInput());
	}
	
	@Test
	public void testRead() throws IOException {		
		assertEquals("Read Value is T", "T", String.valueOf((char)_charSequenceReader.read()));
	}
	
	@Test
	public void testReadToAppendable() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		_charSequenceReader.read(textBuilder);
		assertEquals("TextBuilder Should Contain Test", "Test", textBuilder.toString());
	}
	
	@Test
	public void testReadToArrayFromCharArray() throws IOException{
		_charSequenceReader = new CharSequenceReader(new CharArray("Test"));
		char[] charArray = new char[4];
		int charsRead = _charSequenceReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", charsRead, 4);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test
	public void testReadToArrayFromString() throws IOException{
		char[] charArray = new char[4];
		int charsRead = _charSequenceReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", charsRead, 4);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test
	public void testReadToArrayFromText() throws IOException{
		_charSequenceReader = new CharSequenceReader(Text.valueOf("Test"));
		char[] charArray = new char[4];
		int charsRead = _charSequenceReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", 4, charsRead);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test
	public void testReadToArrayFromTextBuilder() throws IOException{
		_charSequenceReader = new CharSequenceReader(new TextBuilder("Test"));
		char[] charArray = new char[4];
		int charsRead = _charSequenceReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", 4, charsRead);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test(expected=IOException.class)
	public void testReadWithNoInputThrowsException() throws IOException {
		_charSequenceReader = new CharSequenceReader();
		_charSequenceReader.read();
	}
	
	@Test
	public void testReady() throws IOException {
		assertTrue("CharSequenceReader is Ready!", _charSequenceReader.ready());
		
		// Exhaust the Characters
		_charSequenceReader.read();
		_charSequenceReader.read();
		_charSequenceReader.read();
		_charSequenceReader.read();
		
		assertFalse("CharSequenceReader is Not Ready!", _charSequenceReader.ready());
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfClosed() throws IOException {
		_charSequenceReader.close();
		_charSequenceReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfNoInput() throws IOException {
		_charSequenceReader = new CharSequenceReader();
		_charSequenceReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfReset() throws IOException {
		_charSequenceReader.reset();
		_charSequenceReader.ready();
	}
	
	@Test
	public void testSetInput(){
		_charSequenceReader = new CharSequenceReader();
		_charSequenceReader.setInput("Test2");
		assertNotNull("Input Should Not Be Null After Set Input", _charSequenceReader.getInput());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetInputThrowsExceptionIfInputAlreadyExists(){
		_charSequenceReader.setInput("Test2");
	}
	
}
