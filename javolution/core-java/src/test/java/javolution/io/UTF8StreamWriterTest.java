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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javolution.text.CharArray;

import org.junit.Before;
import org.junit.Test;

public class UTF8StreamWriterTest {

	private ByteArrayOutputStream _byteArrayOutputStream;
	private UTF8StreamWriter _utf8StreamWriter;
	
	@Before
	public void init(){
		_byteArrayOutputStream = new ByteArrayOutputStream();
		_utf8StreamWriter = new UTF8StreamWriter(_byteArrayOutputStream);
	}
	
	@Test
	public void testCloseEliminatesOutputReference() throws IOException{
		assertNotNull("AppendableWriter Has Output", _utf8StreamWriter.getOutput());
		_utf8StreamWriter.close();
		assertNull("AppendableWriter Has NO Output", _utf8StreamWriter.getOutput());
	}
	
	@Test
	public void testGetOutput(){
		assertEquals("Output Is Textbuilder", _byteArrayOutputStream, _utf8StreamWriter.getOutput());
	}
	
	@Test
	public void testResetEliminatesOutputReference(){
		assertNotNull("AppendableWriter Has Output", _utf8StreamWriter.getOutput());
		_utf8StreamWriter.reset();
		assertNull("AppendableWriter Has NO Output", _utf8StreamWriter.getOutput());
	}
	
	@Test
	public void testSetOutput(){
		_utf8StreamWriter = new UTF8StreamWriter();
		_utf8StreamWriter.setOutput(_byteArrayOutputStream);
		assertEquals("Output Is Textbuilder", _byteArrayOutputStream, _utf8StreamWriter.getOutput());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetOutputThrowsExceptionIfOutputIsAlreadySet(){
		_utf8StreamWriter.setOutput(_byteArrayOutputStream);
	}
	
	@Test
	public void testWriteArray() throws IOException{
		char[] charArray = {'T', 'e', 's', 't'};
		_utf8StreamWriter.write(charArray);
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is Test", "Test", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));		
	}
	
	@Test
	public void testWriteArrayWithOffset() throws IOException{
		char[] charArray = {'a', 'T', 'e', 's', 't', 'b' };
		_utf8StreamWriter.write(charArray, 1, 4);
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is Test", "Test", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));		
	}
	
	@Test
	public void testWriteChar() throws IOException{
		_utf8StreamWriter.write('T');
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is T", "T", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
	}
	
	@Test
	public void testWriteCharSequence() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_utf8StreamWriter.write(charSequence);
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is Test", "Test", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharSequenceWithNoOutputThrowsException() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_utf8StreamWriter = new UTF8StreamWriter();
		_utf8StreamWriter.write(charSequence);
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharWithNoOutputThrowsException() throws IOException{
		_utf8StreamWriter = new UTF8StreamWriter();
		_utf8StreamWriter.write('T');
	}
	
	@Test
	public void testWriteInt() throws IOException{
		_utf8StreamWriter.write((int)'T');
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is T", "T", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
	}
	
	@Test(expected=IOException.class)
	public void testWriteIntWithNoOutputThrowsException() throws IOException{
		_utf8StreamWriter = new UTF8StreamWriter();
		_utf8StreamWriter.write((int)'T');
	}
	
	@Test
	public void testWriteStringWithOffset() throws IOException{
		_utf8StreamWriter.write("aTesta", 1, 4);
		_utf8StreamWriter.flush();
		assertEquals("Text Written Is Test", "Test", _byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));		
	}
	
	@Test(expected=IOException.class)
	public void testWriteStringWithOffsetWitNoOutputThrowsException() throws IOException{
		_utf8StreamWriter = new UTF8StreamWriter();
		_utf8StreamWriter.write("aTesta", 1, 4);		
	}
}
