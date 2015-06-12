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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javolution.text.CharArray;

import org.junit.Before;
import org.junit.Test;

public class UTF8ByteBufferWriterTest {

	private byte[] _byteArray;
	private ByteBuffer _byteBuffer;
	private UTF8ByteBufferWriter _utf8ByteBufferWriter;
	
	@Before
	public void init(){
		_byteArray = new byte[4];
		_byteBuffer = ByteBuffer.wrap(_byteArray);
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter(_byteBuffer);
	}
	
	@Test
	public void testCloseEliminatesOutputReference() throws IOException{
		assertNotNull("AppendableWriter Has Output", _utf8ByteBufferWriter.getOutput());
		_utf8ByteBufferWriter.close();
		assertNull("AppendableWriter Has NO Output", _utf8ByteBufferWriter.getOutput());
	}
	
	@Test
	public void testGetOutput(){
		assertEquals("Output Is Textbuilder", _byteBuffer, _utf8ByteBufferWriter.getOutput());
	}
	
	@Test
	public void testResetEliminatesOutputReference(){
		assertNotNull("AppendableWriter Has Output", _utf8ByteBufferWriter.getOutput());
		_utf8ByteBufferWriter.reset();
		assertNull("AppendableWriter Has NO Output", _utf8ByteBufferWriter.getOutput());
	}
	
	@Test
	public void testSetOutput(){
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter();
		_utf8ByteBufferWriter.setOutput(_byteBuffer);
		assertEquals("Output Is Textbuilder", _byteBuffer, _utf8ByteBufferWriter.getOutput());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetOutputThrowsExceptionIfOutputIsAlreadySet(){
		_utf8ByteBufferWriter.setOutput(_byteBuffer);
	}
	
	@Test
	public void testWriteArray() throws IOException{
		char[] charArray = {'T', 'e', 's', 't'};
		_utf8ByteBufferWriter.write(charArray);
		assertEquals("Text Written Is Test", "Test", new String(_byteArray, StandardCharsets.UTF_8));		
	}
	
	@Test
	public void testWriteArrayWithOffset() throws IOException{
		char[] charArray = {'a', 'T', 'e', 's', 't', 'b' };
		_utf8ByteBufferWriter.write(charArray, 1, 4);
		assertEquals("Text Written Is Test", "Test", new String(_byteArray, StandardCharsets.UTF_8));		
	}
	
	@Test
	public void testWriteChar() throws IOException{
		_utf8ByteBufferWriter.write('T');
		assertEquals("Text Written Is T", "T", new String(_byteArray, StandardCharsets.UTF_8).trim());
	}
	
	@Test
	public void testWriteCharSequence() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_utf8ByteBufferWriter.write(charSequence);
		assertEquals("Text Written Is Test", "Test", new String(_byteArray, StandardCharsets.UTF_8));
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharSequenceWithNoOutputThrowsException() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter();
		_utf8ByteBufferWriter.write(charSequence);
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharWithNoOutputThrowsException() throws IOException{
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter();
		_utf8ByteBufferWriter.write('T');
	}
	
	@Test
	public void testWriteInt() throws IOException{
		_utf8ByteBufferWriter.write((int)'T');
		assertEquals("Text Written Is T", "T", new String(_byteArray, StandardCharsets.UTF_8).trim());
	}
	
	@Test(expected=IOException.class)
	public void testWriteIntWithNoOutputThrowsException() throws IOException{
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter();
		_utf8ByteBufferWriter.write((int)'T');
	}
	
	@Test
	public void testWriteStringWithOffset() throws IOException{
		_utf8ByteBufferWriter.write("aTesta", 1, 4);
		assertEquals("Text Written Is Test", "Test", new String(_byteArray, StandardCharsets.UTF_8));		
	}
	
	@Test(expected=IOException.class)
	public void testWriteStringWithOffsetWitNoOutputThrowsException() throws IOException{
		_utf8ByteBufferWriter = new UTF8ByteBufferWriter();
		_utf8ByteBufferWriter.write("aTesta", 1, 4);		
	}
}
