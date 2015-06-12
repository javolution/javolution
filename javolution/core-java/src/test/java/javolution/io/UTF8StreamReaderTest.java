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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javolution.text.TextBuilder;

import org.junit.Before;
import org.junit.Test;

public class UTF8StreamReaderTest {

	private InputStream _inputStream;
	private UTF8StreamReader _utf8StreamReader;
	
	@Before
	public void init(){
		_inputStream = new ByteArrayInputStream("Test".getBytes(StandardCharsets.UTF_8));
		_utf8StreamReader = new UTF8StreamReader(_inputStream);
	}
	
	@Test
	public void testRead() throws IOException {		
		assertEquals("Read Value is T", "T", String.valueOf((char)_utf8StreamReader.read()));
	}
	
	@Test
	public void testReadToAppendable() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		_utf8StreamReader.read(textBuilder);
		assertEquals("TextBuilder Should Contain Test", "Test", textBuilder.toString());
	}
	
	@Test
	public void testReadToArray() throws IOException{
		char[] charArray = new char[4];
		int charsRead = _utf8StreamReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", charsRead, 4);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test(expected=IOException.class)
	public void testReadWithNoInputThrowsException() throws IOException {
		_utf8StreamReader = new UTF8StreamReader();
		_utf8StreamReader.read();
	}
	
	@Test
	public void testReady() throws IOException {
		assertTrue("CharSequenceReader is Ready!", _utf8StreamReader.ready());
		
		// Exhaust the Characters
		_utf8StreamReader.read();
		_utf8StreamReader.read();
		_utf8StreamReader.read();
		_utf8StreamReader.read();
		
		assertFalse("CharSequenceReader is Not Ready!", _utf8StreamReader.ready());
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfClosed() throws IOException {
		_utf8StreamReader.close();
		_utf8StreamReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfNoInput() throws IOException {
		_utf8StreamReader = new UTF8StreamReader();
		_utf8StreamReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfReset() throws IOException {
		_utf8StreamReader.reset();
		_utf8StreamReader.ready();
	}

	@Test
	public void testSetInput() throws IOException{
		_utf8StreamReader = new UTF8StreamReader();
		_utf8StreamReader.setInput(_inputStream);
		assertTrue("Reader Should Be Ready After Setting ByteBuffer", _utf8StreamReader.ready());
	}
	
	@Test
	public void testSetInputStream() throws IOException{
		_utf8StreamReader = new UTF8StreamReader();
		_utf8StreamReader.setInputStream(_inputStream);
		assertTrue("Reader Should Be Ready After Setting ByteBuffer", _utf8StreamReader.ready());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetInputStreamThrowsExceptionIfInputAlreadyExists(){
		_utf8StreamReader.setInputStream(_inputStream);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetInputThrowsExceptionIfInputAlreadyExists(){
		_utf8StreamReader.setInput(_inputStream);
	}
	
}
