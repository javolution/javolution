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

import javolution.text.CharArray;

import org.junit.Before;
import org.junit.Test;

public class AppendableWriterTest {

	private AppendableWriter _appendableWriter;
	private StringBuilder _stringBuilder;
	
	@Before
	public void init(){
		_stringBuilder = new StringBuilder();
		_appendableWriter = new AppendableWriter(_stringBuilder);
	}
	
	@Test
	public void testCloseEliminatesOutputReference(){
		assertNotNull("AppendableWriter Has Output", _appendableWriter.getOutput());
		_appendableWriter.close();
		assertNull("AppendableWriter Has NO Output", _appendableWriter.getOutput());
	}
	
	@Test
	public void testGetOutput(){
		assertEquals("Output Is Textbuilder", _stringBuilder, _appendableWriter.getOutput());
	}
	
	@Test
	public void testResetEliminatesOutputReference(){
		assertNotNull("AppendableWriter Has Output", _appendableWriter.getOutput());
		_appendableWriter.reset();
		assertNull("AppendableWriter Has NO Output", _appendableWriter.getOutput());
	}
	
	@Test
	public void testSetOutput(){
		_appendableWriter = new AppendableWriter();
		_appendableWriter.setOutput(_stringBuilder);
		assertEquals("Output Is Textbuilder", _stringBuilder, _appendableWriter.getOutput());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetOutputThrowsExceptionIfOutputIsAlreadySet(){
		_appendableWriter.setOutput(_stringBuilder);
	}
	
	@Test
	public void testWriteArray() throws IOException{
		char[] charArray = {'T', 'e', 's', 't'};
		_appendableWriter.write(charArray);
		assertEquals("Text Written Is Test", "Test", _stringBuilder.toString());		
	}
	
	@Test
	public void testWriteArrayWithOffset() throws IOException{
		char[] charArray = {'a', 'T', 'e', 's', 't', 'b' };
		_appendableWriter.write(charArray, 1, 4);
		assertEquals("Text Written Is Test", "Test", _stringBuilder.toString());		
	}
	
	@Test
	public void testWriteChar() throws IOException{
		_appendableWriter.write('T');
		assertEquals("Text Written Is T", "T", _stringBuilder.toString());
	}
	
	@Test
	public void testWriteCharSequence() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_appendableWriter.write(charSequence);
		assertEquals("Text Written Is Test", "Test", _stringBuilder.toString());
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharSequenceWithNoOutputThrowsException() throws IOException{
		CharSequence charSequence = new CharArray("Test");
		_appendableWriter = new AppendableWriter();
		_appendableWriter.write(charSequence);
	}
	
	@Test(expected=IOException.class)
	public void testWriteCharWithNoOutputThrowsException() throws IOException{
		_appendableWriter = new AppendableWriter();
		_appendableWriter.write('T');
	}
	
	@Test
	public void testWriteInt() throws IOException{
		_appendableWriter.write((int)'T');
		assertEquals("Text Written Is T", "T", _stringBuilder.toString());
	}
	
	@Test(expected=IOException.class)
	public void testWriteIntWithNoOutputThrowsException() throws IOException{
		_appendableWriter = new AppendableWriter();
		_appendableWriter.write((int)'T');
	}
	
	@Test
	public void testWriteStringWithOffset() throws IOException{
		_appendableWriter.write("aTesta", 1, 4);
		assertEquals("Text Written Is Test", "Test", _stringBuilder.toString());		
	}
	
	@Test(expected=IOException.class)
	public void testWriteStringWithOffsetWitNoOutputThrowsException() throws IOException{
		_appendableWriter = new AppendableWriter();
		_appendableWriter.write("aTesta", 1, 4);		
	}
}
