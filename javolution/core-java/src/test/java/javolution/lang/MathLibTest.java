/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathLibTest {

	@Test
	public void testAbsWithDouble(){
		assertEquals("Absolute Value of Negative Input Returns Positive", 1.0, MathLib.abs(-1.0), 0);
		assertEquals("Absolute Value of Positive Input Returns Positive", 1.0, MathLib.abs(-1.0), 0);
	}
	
	@Test
	public void testAbsWithFloat(){
		assertEquals("Absolute Value of Negative Input Returns Positive", 1.0f, MathLib.abs(-1.0f), 0);
		assertEquals("Absolute Value of Positive Input Returns Positive", 1.0f, MathLib.abs(-1.0f), 0);
	}
	
	@Test
	public void testAbsWithInt(){
		assertEquals("Absolute Value of Negative Input Returns Positive", 1, MathLib.abs(-1));
		assertEquals("Absolute Value of Positive Input Returns Positive", 1, MathLib.abs(1));
	}
	
	@Test
	public void testAbsWithLong(){
		assertEquals("Absolute Value of Negative Input Returns Positive", 1L, MathLib.abs(-1L));
		assertEquals("Absolute Value of Positive Input Returns Positive", 1L, MathLib.abs(1L));
	}
	
	@Test
	public void testBitCount(){
		assertEquals("BitCount of 1 is 1", 1, MathLib.bitCount(1L));
		assertEquals("BitCount of 2 is 1", 1, MathLib.bitCount(2L));
		assertEquals("BitCount of 3 is 2", 2, MathLib.bitCount(3L));
		assertEquals("BitCount of 4 is 1", 1, MathLib.bitCount(4L));
		assertEquals("BitCount of 5 is 2", 2, MathLib.bitCount(5L));
		assertEquals("BitCount of 6 is 2", 2, MathLib.bitCount(6L));
		assertEquals("BitCount of 7 is 3", 3, MathLib.bitCount(7L));
		assertEquals("BitCount of 8 is 1", 1, MathLib.bitCount(8L));
	}
	
	@Test
	public void testBitLengthWithInt(){
		assertEquals("BitLength of 1 is 1", 1, MathLib.bitLength(1));
		assertEquals("BitLength of 2 is 2", 2, MathLib.bitLength(2));
		assertEquals("BitLength of 3 is 2", 2, MathLib.bitLength(3));
		assertEquals("BitLength of 4 is 3", 3, MathLib.bitLength(4));
		assertEquals("BitLength of 5 is 3", 3, MathLib.bitLength(5));
		assertEquals("BitLength of 6 is 3", 3, MathLib.bitLength(6));
		assertEquals("BitLength of 7 is 3", 3, MathLib.bitLength(7));
		assertEquals("BitLength of 8 is 4", 4, MathLib.bitLength(8));
	}
	
	@Test
	public void testBitLengthWithLong(){
		assertEquals("BitLength of 1 is 1", 1, MathLib.bitLength(1L));
		assertEquals("BitLength of 2 is 2", 2, MathLib.bitLength(2L));
		assertEquals("BitLength of 3 is 2", 2, MathLib.bitLength(3L));
		assertEquals("BitLength of 4 is 3", 3, MathLib.bitLength(4L));
		assertEquals("BitLength of 5 is 3", 3, MathLib.bitLength(5L));
		assertEquals("BitLength of 6 is 3", 3, MathLib.bitLength(6L));
		assertEquals("BitLength of 7 is 3", 3, MathLib.bitLength(7L));
		assertEquals("BitLength of 8 is 4", 4, MathLib.bitLength(8L));
	}
	
	@Test
	public void testCeil(){
		assertEquals("Ceil(1.0) is 1.0", 1.0, MathLib.ceil(1.0), 1.0);
		assertEquals("Ceil(1.1) is 2.0", 2.0, MathLib.ceil(1.1), 1.0);
		assertEquals("Ceil(1.2) is 2.0", 2.0, MathLib.ceil(1.2), 1.0);
		assertEquals("Ceil(1.3) is 2.0", 2.0, MathLib.ceil(1.3), 1.0);
		assertEquals("Ceil(1.4) is 2.0", 2.0, MathLib.ceil(1.4), 1.0);
		assertEquals("Ceil(1.5) is 2.0", 2.0, MathLib.ceil(1.5), 1.0);
		assertEquals("Ceil(1.6) is 2.0", 2.0, MathLib.ceil(1.6), 1.0);
		assertEquals("Ceil(1.7) is 2.0", 2.0, MathLib.ceil(1.7), 1.0);
		assertEquals("Ceil(1.8) is 2.0", 2.0, MathLib.ceil(1.8), 1.0);
		assertEquals("Ceil(1.9) is 2.0", 2.0, MathLib.ceil(1.9), 1.0);
	}
	
	@Test
	public void testDigitLengthWithInt(){
		assertEquals("Digit Length of 1 is 1", 1, MathLib.digitLength(1));
		assertEquals("Digit Length of 12 is 2", 2, MathLib.digitLength(12));
		assertEquals("Digit Length of 123 is 3", 3, MathLib.digitLength(123));
		assertEquals("Digit Length of 1234 is 4", 4, MathLib.digitLength(1234));
		assertEquals("Digit Length of -1 is 1", 1, MathLib.digitLength(-1));
		assertEquals("Digit Length of -12 is 2", 2, MathLib.digitLength(-12));
		assertEquals("Digit Length of -123 is 3", 3, MathLib.digitLength(-123));
		assertEquals("Digit Length of -1234 is 4", 4, MathLib.digitLength(-1234));
		assertEquals("Digit Length of Integer.MIN_VALUE is 10", 10, MathLib.digitLength(Integer.MIN_VALUE));
		assertEquals("Digit Length of Integer.MAX_VALUE is 10", 10, MathLib.digitLength(Integer.MAX_VALUE));
	}
	
	@Test
	public void testDigitLengthWithLong(){
		assertEquals("Digit Length of 1 is 1", 1, MathLib.digitLength(1L));
		assertEquals("Digit Length of 12 is 2", 2, MathLib.digitLength(12L));
		assertEquals("Digit Length of 123 is 3", 3, MathLib.digitLength(123L));
		assertEquals("Digit Length of 1234 is 4", 4, MathLib.digitLength(1234L));
		assertEquals("Digit Length of -1 is 1", 1, MathLib.digitLength(-1L));
		assertEquals("Digit Length of -12 is 2", 2, MathLib.digitLength(-12L));
		assertEquals("Digit Length of -123 is 3", 3, MathLib.digitLength(-123L));
		assertEquals("Digit Length of -1234 is 4", 4, MathLib.digitLength(-1234L));
		assertEquals("Digit Length of Long.MIN_VALUE is 19", 19, MathLib.digitLength(Long.MIN_VALUE));
		assertEquals("Digit Length of Long.MAX_VALUE is 19", 19, MathLib.digitLength(Long.MAX_VALUE));
	}

	@Test
	public void testFloor(){		
		assertEquals("Floor(1.1) is 1.0", 1.0, MathLib.floor(1.1), 1.0);
		assertEquals("Floor(1.2) is 1.0", 1.0, MathLib.floor(1.2), 1.0);
		assertEquals("Floor(1.3) is 1.0", 1.0, MathLib.floor(1.3), 1.0);
		assertEquals("Floor(1.4) is 1.0", 1.0, MathLib.floor(1.4), 1.0);
		assertEquals("Floor(1.5) is 1.0", 1.0, MathLib.floor(1.5), 1.0);
		assertEquals("Floor(1.6) is 1.0", 1.0, MathLib.floor(1.6), 1.0);
		assertEquals("Floor(1.7) is 1.0", 1.0, MathLib.floor(1.7), 1.0);
		assertEquals("Floor(1.8) is 1.0", 1.0, MathLib.floor(1.8), 1.0);
		assertEquals("Floor(1.9) is 1.0", 1.0, MathLib.floor(1.9), 1.0);
		assertEquals("Floor(2.0) is 2.0", 2.0, MathLib.floor(2.0), 1.0);
	}
	
	@Test
	public void testMaxWithDouble(){
		assertEquals("Max(5.0,7.0) Is 7.0", 7.0, MathLib.max(5.0,7.0), 0.0);
		assertEquals("Max(-7.0,5.0) Is 5.0", 5.0, MathLib.max(-7.0,5.0), 0.0);
	}
	
	@Test
	public void testMaxWithFloat(){
		assertEquals("Max(5.0,7.0) Is 7.0", 7.0f, MathLib.max(5.0f,7.0f), 0.0);
		assertEquals("Max(-7.0,5.0) Is 5.0", 5.0f, MathLib.max(-7.0f,5.0f), 0.0);
	}
	
	@Test
	public void testMaxWithInt(){
		assertEquals("Max(5,7) Is 7", 7, MathLib.max(5,7));
		assertEquals("Max(-7,5) Is 5", 5, MathLib.max(-7,5));
	}
	
	@Test
	public void testMaxWithLong(){
		assertEquals("Max(5,7) Is 7", 7L, MathLib.max(5L,7L));
		assertEquals("Max(-7,5) Is 5", 5L, MathLib.max(-7L,5L));
	}
	
	@Test
	public void testMinWithDouble(){
		assertEquals("Min(5.0,7.0) Is 5.0", 5.0, MathLib.min(5.0,7.0), 0.0);
		assertEquals("Min(-7.0,5.0) Is -7.0", -7.0, MathLib.min(-7.0,5.0), 0.0);
	}
	
	@Test
	public void testMinWithFloat(){
		assertEquals("Min(5.0,7.0) Is 5.0", 5.0f, MathLib.min(5.0f,7.0f), 0.0);
		assertEquals("Min(-7.0,5.0) Is -7.0", -7.0f, MathLib.min(-7.0f,5.0f), 0.0);
	}
	
	@Test
	public void testMinWithInt(){
		assertEquals("Min(5,7) Is 5", 5, MathLib.min(5,7));
		assertEquals("Min(-7,5) Is -7", -7, MathLib.min(-7,5));
	}
	
	@Test
	public void testMinWithLong(){
		assertEquals("Min(5,7) Is 5", 5L, MathLib.min(5L,7L));
		assertEquals("Min(-7,5) Is -7", -7L, MathLib.min(-7L,5L));
	}
	
	@Test
	public void testNumberOfLeadingZeros(){
		assertEquals("Leading Zeroes of 0 is 64", 64, MathLib.numberOfLeadingZeros(0L));
		assertEquals("Leading Zeroes of 1 is 63", 63, MathLib.numberOfLeadingZeros(1L));
		assertEquals("Leading Zeroes of 2 is 62", 62, MathLib.numberOfLeadingZeros(2L));
		assertEquals("Leading Zeroes of 3 is 62", 62, MathLib.numberOfLeadingZeros(3L));
		assertEquals("Leading Zeroes of 4 is 61", 61, MathLib.numberOfLeadingZeros(4L));
	}

	@Test
	public void testNumberOfTrailingZeros(){
		// When all bits are 0, you can't classify leading vs. trailing so its 64
		assertEquals("Leading Zeroes of 0 is 64", 64, MathLib.numberOfTrailingZeros(0L));
		assertEquals("Leading Zeroes of 1 is 0", 0, MathLib.numberOfTrailingZeros(1L));
		assertEquals("Leading Zeroes of 2 is 1", 1, MathLib.numberOfTrailingZeros(2L));
		assertEquals("Leading Zeroes of 3 is 0", 0, MathLib.numberOfTrailingZeros(3L));
		assertEquals("Leading Zeroes of 4 is 2", 2, MathLib.numberOfTrailingZeros(4L));
	}
	
	@Test
	public void testRoundWithDouble(){
		assertEquals("Round(1.0) is 1.0", 1.0, MathLib.round(1.0), 1.0);
		assertEquals("Round(1.1) is 1.0", 1.0, MathLib.round(1.1), 1.0);
		assertEquals("Round(1.2) is 1.0", 1.0, MathLib.round(1.2), 1.0);
		assertEquals("Round(1.3) is 1.0", 1.0, MathLib.round(1.3), 1.0);
		assertEquals("Round(1.4) is 1.0", 1.0, MathLib.round(1.4), 1.0);
		assertEquals("Round(1.5) is 2.0", 2.0, MathLib.round(1.5), 1.0);
		assertEquals("Round(1.6) is 2.0", 2.0, MathLib.round(1.6), 1.0);
		assertEquals("Round(1.7) is 2.0", 2.0, MathLib.round(1.7), 1.0);
		assertEquals("Round(1.8) is 2.0", 2.0, MathLib.round(1.8), 1.0);
		assertEquals("Round(1.9) is 2.0", 2.0, MathLib.round(1.9), 1.0);
	}
	
	@Test
	public void testRoundWithFloat(){
		assertEquals("Round(1.0) is 1.0", 1.0f, MathLib.round(1.0f), 1.0);
		assertEquals("Round(1.1) is 1.0", 1.0f, MathLib.round(1.1f), 1.0);
		assertEquals("Round(1.2) is 1.0", 1.0f, MathLib.round(1.2f), 1.0);
		assertEquals("Round(1.3) is 1.0", 1.0f, MathLib.round(1.3f), 1.0);
		assertEquals("Round(1.4) is 1.0", 1.0f, MathLib.round(1.4f), 1.0);
		assertEquals("Round(1.5) is 2.0", 2.0f, MathLib.round(1.5f), 1.0);
		assertEquals("Round(1.6) is 2.0", 2.0f, MathLib.round(1.6f), 1.0);
		assertEquals("Round(1.7) is 2.0", 2.0f, MathLib.round(1.7f), 1.0);
		assertEquals("Round(1.8) is 2.0", 2.0f, MathLib.round(1.8f), 1.0);
		assertEquals("Round(1.9) is 2.0", 2.0f, MathLib.round(1.9f), 1.0);
	}
	
	@Test
	public void testSqrt(){
		assertEquals("Sqrt(9) Is 3", 3.0, MathLib.sqrt(9.0), 0.0);
	}
}
