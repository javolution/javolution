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

import java.io.IOException;

import org.junit.Test;

public class TypeFormatTest {
	
	@Test
	public void parseBoolean(){
		assertTrue("True Parsed", TypeFormat.parseBoolean("true"));
		assertFalse("False Parsed", TypeFormat.parseBoolean("false"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseBooleanThrowsExceptionIfStringDoesNotRepresentABoolean(){
		TypeFormat.parseBoolean("aaaa");
	}
	
	@Test
	public void parseBooleanWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertTrue("True Parsed", TypeFormat.parseBoolean("aaaatrue", cursor));
		cursor.setIndex(4);
		assertFalse("False Parsed", TypeFormat.parseBoolean("aaaafalse", cursor));
	}
	
	@Test
	public void parseDouble(){
		assertEquals("1.0 Parsed", 1.0, TypeFormat.parseDouble("1.0"), 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseDoubleThrowsExceptionIfStringDoesNotRepresentADouble(){
		TypeFormat.parseDouble("aaaa");
	}
	
	@Test
	public void parseDoubleWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertEquals("1.0 Parsed", 1.0, TypeFormat.parseDouble("aaaa1.0", cursor), 0);
	}
	
	@Test
	public void parseDoubleWithExplicitPositiveNumber(){
		assertEquals("+15 Parsed", 15.0, TypeFormat.parseDouble("+15.0"), 0);
	}
	
	@Test
	public void parseDoubleWithNegativeNumber(){
		assertEquals("-15 Parsed", -15.0, TypeFormat.parseDouble("-15.0"), 0);
	}
	
	@Test
	public void parseFloat(){
		assertEquals("1.0 Parsed", 1.0f, TypeFormat.parseFloat("1.0"), 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseFloatThrowsExceptionIfStringDoesNotRepresentAFloat(){
		TypeFormat.parseFloat("aaaa");
	}
	
	@Test
	public void parseFloatWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertEquals("1.0 Parsed", 1.0f, TypeFormat.parseFloat("aaaa1.0", cursor), 0);
	}
	
	@Test
	public void parseFloatWithExplicitPositiveNumber(){
		assertEquals("+15 Parsed", 15.0f, TypeFormat.parseFloat("+15.0"), 0);
	}
	
	@Test
	public void parseFloatWithNegativeNumber(){
		assertEquals("-15 Parsed", -15.0f, TypeFormat.parseFloat("-15.0"), 0);
	}
	
	@Test
	public void parseInt(){
		assertEquals("1 Parsed", 1, TypeFormat.parseInt("1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseIntThrowsExceptionIfStringDoesNotRepresentAnInt(){
		TypeFormat.parseInt("aaaa");
	}
	
	@Test
	public void parseIntWithBinaryRadix(){
		assertEquals("0b1001 Parsed", 9, TypeFormat.parseInt("1001",2));
	}
	
	@Test
	public void parseIntWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertEquals("1 Parsed", 1, TypeFormat.parseInt("aaaa1", cursor));
	}
	
	@Test
	public void parseIntWithExplicitPositiveNumber(){
		assertEquals("+15 Parsed", 15, TypeFormat.parseInt("+15"));
	}
	
	@Test
	public void parseIntWithHexRadix(){
		assertEquals("0xAAAA Parsed", 0xAAAA, TypeFormat.parseInt("aaaa",16));
	}
	
	@Test
	public void parseIntWithNegativeNumber(){
		assertEquals("-15 Parsed", -15, TypeFormat.parseInt("-15"));
	}
	
	@Test
	public void parseLong(){
		assertEquals("1 Parsed", 1L, TypeFormat.parseLong("1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseLongThrowsExceptionIfStringDoesNotRepresentALong(){
		TypeFormat.parseLong("aaaa");
	}
	
	@Test
	public void parseLongWithBinaryRadix(){
		assertEquals("0b1001 Parsed", 9L, TypeFormat.parseLong("1001",2));
	}
	
	@Test
	public void parseLongWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertEquals("1 Parsed", 1L, TypeFormat.parseLong("aaaa1", cursor));
	}
	
	@Test
	public void parseLongWithExplicitPositiveNumber(){
		assertEquals("+15 Parsed", 15L, TypeFormat.parseLong("+15"));
	}
	
	@Test
	public void parseLongWithHexRadix(){
		assertEquals("0xAAAA Parsed", 0xAAAAL, TypeFormat.parseLong("aaaa",16));
	}
	
	@Test
	public void parseLongWithNegativeNumber(){
		assertEquals("-15 Parsed", -15L, TypeFormat.parseLong("-15"));
	}
	
	@Test
	public void parseShort(){
		assertEquals("1 Parsed", (short)1, TypeFormat.parseShort("1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void parseShortThrowsExceptionIfStringDoesNotRepresentAnShort(){
		TypeFormat.parseShort("aaaa");
	}
	
	@Test
	public void parseShortWithBinaryRadix(){
		assertEquals("0b1001 Parsed", 9, TypeFormat.parseShort("1001",2));
	}
	
	@Test
	public void parseShortWithCursor(){
		Cursor cursor = new Cursor();
		cursor.setIndex(4);
		assertEquals("1 Parsed", (short)1, TypeFormat.parseShort("aaaa1", cursor));
	}
	
	@Test
	public void parseShortWithExplicitPositiveNumber(){
		assertEquals("+15 Parsed", (short)15, TypeFormat.parseShort("+15"));
	}
	
	@Test
	public void parseShortWithHexRadix(){
		assertEquals("0xAA Parsed", 0xAA, TypeFormat.parseShort("aa",16));
	}
	
	@Test
	public void parseShortWithNegativeNumber(){
		assertEquals("-15 Parsed", (short)-15, TypeFormat.parseShort("-15"));
	}
	
	@Test(expected=NumberFormatException.class)
	public void parseTestShortMaxOverflow(){
		TypeFormat.parseShort(String.valueOf(Integer.MAX_VALUE));
	}
	
	@Test(expected=NumberFormatException.class)
	public void parseTestShortMinOverflow(){
		TypeFormat.parseShort(String.valueOf(Integer.MIN_VALUE));
	}
	
	@Test
	public void testFormatBoolean() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		TypeFormat.format(true, textBuilder);
		assertEquals("Format: true", "true", textBuilder.toString());
		
		textBuilder = new TextBuilder();
		TypeFormat.format(false, textBuilder);
		assertEquals("Format: false", "false", textBuilder.toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormatBooleanWithNullAppendableThrowsException() throws IOException{
		TypeFormat.format(false, null);
	}
	
	@Test
	public void testFormatDouble() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		TypeFormat.format(1.0, textBuilder);
		assertEquals("Format: 1.0", "1.0", textBuilder.toString());
		
		textBuilder = new TextBuilder();
		TypeFormat.format(-1.0, textBuilder);
		assertEquals("Format: -1.0", "-1.0", textBuilder.toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormatDoubleWithNullAppendableThrowsException() throws IOException{
		TypeFormat.format(1.0, null);
	}
	
	@Test
	public void testFormatFloat() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		TypeFormat.format(1.0f, textBuilder);
		assertEquals("Format: 1.0", "1.0", textBuilder.toString());
		
		textBuilder = new TextBuilder();
		TypeFormat.format(-1.0f, textBuilder);
		assertEquals("Format: -1.0", "-1.0", textBuilder.toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormatFloatWithNullAppendableThrowsException() throws IOException{
		TypeFormat.format(1.0f, null);
	}
	
	@Test
	public void testFormatInt() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		TypeFormat.format(1, textBuilder);
		assertEquals("Format: 1", "1", textBuilder.toString());
		
		textBuilder = new TextBuilder();
		TypeFormat.format(-1, textBuilder);
		assertEquals("Format: -1", "-1", textBuilder.toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormatIntWithNullAppendableThrowsException() throws IOException{
		TypeFormat.format(1, null);
	}
	
	@Test
	public void testFormatLong() throws IOException{
		TextBuilder textBuilder = new TextBuilder();
		TypeFormat.format(1L, textBuilder);
		assertEquals("Format: 1", "1", textBuilder.toString());
		
		textBuilder = new TextBuilder();
		TypeFormat.format(-1L, textBuilder);
		assertEquals("Format: -1", "-1", textBuilder.toString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormatLongWithNullAppendableThrowsException() throws IOException{
		TypeFormat.format(1L, null);
	}
	
	@Test
	public void testMatch(){
		assertTrue("Matching String", TypeFormat.match("Test", "Test", 0, 4));
		assertTrue("Matching Partial String", TypeFormat.match("Test", "XTestX", 1, 4));
		assertFalse("Non Matching String", TypeFormat.match("Test", "xxxx", 0, 4));
	}
	
	@Test
	public void testMatchWithCharSequence(){
		assertTrue("Matching String", TypeFormat.match("Test", Text.valueOf("Test"), 0, 4));
		assertTrue("Matching Partial String", TypeFormat.match("Test", Text.valueOf("XTestX"), 1, 4));
		assertFalse("Non Matching String", TypeFormat.match("Test", Text.valueOf("xxxx"), 0, 4));
	}
}
