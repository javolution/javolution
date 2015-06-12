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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javolution.text.TextBuilder;

import org.junit.Before;
import org.junit.Test;

public class UTF8ByteBufferReaderTest {

	private ByteBuffer _byteBuffer;
	private UTF8ByteBufferReader _utf8ByteBufferReader;
	
	@Before
	public void init(){
		_byteBuffer = ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8));
		_utf8ByteBufferReader = new UTF8ByteBufferReader(_byteBuffer);
	}
	
	@Test
	public void testRead() throws IOException {		
		assertEquals("Read Value is T", "T", String.valueOf((char)_utf8ByteBufferReader.read()));
	}
	
	@Test
	public void testReadToAppendable() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		_utf8ByteBufferReader.read(textBuilder);
		assertEquals("TextBuilder Should Contain Test", "Test", textBuilder.toString());
	}
	
	@Test
	public void testReadToArray() throws IOException{
		char[] charArray = new char[4];
		int charsRead = _utf8ByteBufferReader.read(charArray, 0, 4);
		assertEquals("4 Chars Read", charsRead, 4);
		assertEquals("Chars Read is Test", "Test", String.valueOf(charArray));
	}
	
	@Test(expected=IOException.class)
	public void testReadWithNoInputThrowsException() throws IOException {
		_utf8ByteBufferReader = new UTF8ByteBufferReader();
		_utf8ByteBufferReader.read();
	}
	
	@Test
	public void testReady() throws IOException {
		assertTrue("CharSequenceReader is Ready!", _utf8ByteBufferReader.ready());
		
		// Exhaust the Characters
		_utf8ByteBufferReader.read();
		_utf8ByteBufferReader.read();
		_utf8ByteBufferReader.read();
		_utf8ByteBufferReader.read();
		
		assertFalse("CharSequenceReader is Not Ready!", _utf8ByteBufferReader.ready());
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfClosed() throws IOException {
		_utf8ByteBufferReader.close();
		_utf8ByteBufferReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfNoInput() throws IOException {
		_utf8ByteBufferReader = new UTF8ByteBufferReader();
		_utf8ByteBufferReader.ready();
	}
	
	@Test(expected=IOException.class)
	public void testReadyThrowsExceptionIfReset() throws IOException {
		_utf8ByteBufferReader.reset();
		_utf8ByteBufferReader.ready();
	}

	@Test
	public void testSetByteBuffer() throws IOException{
		_utf8ByteBufferReader = new UTF8ByteBufferReader();
		_utf8ByteBufferReader.setByteBuffer(_byteBuffer);
		assertTrue("Reader Should Be Ready After Setting ByteBuffer", _utf8ByteBufferReader.ready());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetByteBufferThrowsExceptionIfInputAlreadyExists(){
		_utf8ByteBufferReader.setByteBuffer(_byteBuffer);
	}
	
	@Test
	public void testSetInput() throws IOException{
		_utf8ByteBufferReader = new UTF8ByteBufferReader();
		_utf8ByteBufferReader.setInput(_byteBuffer);
		assertTrue("Reader Should Be Ready After Setting ByteBuffer", _utf8ByteBufferReader.ready());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetInputThrowsExceptionIfInputAlreadyExists(){
		_utf8ByteBufferReader.setInput(_byteBuffer);
	}
	
}
