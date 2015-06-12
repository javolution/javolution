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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.CharBuffer;

import org.junit.Before;
import org.junit.Test;

public class TextReaderTest {

	private Text _text;
	private TextReader _textReader;
	
	@Before
	public void init(){
		_text = Text.valueOf("Test");
		_textReader = new TextReader(_text);
	}
	
	@Test
	public void testMarkSupported(){
		assertTrue("Mark Is Supported", _textReader.markSupported());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMarkWithNegativeArgumentThrowsException() throws IOException{
		_textReader.mark(-1);
	}
	
	@Test
	public void testRead() throws IOException {		
		assertEquals("Read Value is T", "T", String.valueOf((char)_textReader.read()));
	}
	
	@Test
	public void testReadToAppendable() throws IOException{
		char[] array = new char[4];		
		_textReader.read(CharBuffer.wrap(array));
		assertEquals("TextBuilder Should Contain Test", "Test", String.valueOf(array));
	}
	
	@Test
	public void testReady() throws IOException {
		assertTrue("CharSequenceReader is Ready!", _textReader.ready());
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfClosed() throws IOException {
		_textReader.close();
		_textReader.ready();
	}
	
	@Test
	public void testResetReturnsToBeginningOfTextWithoutMark() throws IOException {
		_textReader.read();
		_textReader.reset();
		assertEquals("First Value After Reset Is T", 'T', (char)_textReader.read());
	}
	
	@Test
	public void testResetReturnsToMarkedPositionOfTextWithMark() throws IOException {
		_textReader.read();
		_textReader.mark(0);
		_textReader.reset();
		assertEquals("First Value After Reset Is e", 'e', (char)_textReader.read());
	}

}
