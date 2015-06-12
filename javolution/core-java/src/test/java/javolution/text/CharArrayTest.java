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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class CharArrayTest {

	private CharArray _charArray;
	private char[] _primitiveCharArray;
	
	@Before
	public void init(){
		_primitiveCharArray = new char[]{'T','e','s','t'};
		_charArray = new CharArray("Test");
	}

	@Test
	public void testArray(){
		assertTrue("Array Equals Test", Arrays.equals(_primitiveCharArray, _charArray.array()));
	}
	
	@Test
	public void testCharAt(){
		assertEquals("Char At 0 Is T", 'T', _charArray.charAt(0));
		assertEquals("Char At 1 Is e", 'e', _charArray.charAt(1));
		assertEquals("Char At 2 Is s", 's', _charArray.charAt(2));
		assertEquals("Char At 3 Is t", 't', _charArray.charAt(3));
	}
	
	@Test
	public void testContentEquals(){
		assertTrue("contentEquals() Test", _charArray.contentEquals("Test"));		
	}
	
	@Test
	public void testEqualsHashCodeCharArray(){
		CharArray test = new CharArray("Test");
		assertTrue("equals() Test", _charArray.equals(test));
		
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		CharArray testView = new CharArray();
		testView.setArray(_primitiveCharArray, 4, 4);
		
		assertTrue("equals() Test", test.equals(testView));
		assertTrue("hashCode() Test == TestView", test.hashCode() == testView.hashCode());
	}
	
	@Test
	public void testEqualsCharSequence(){
		assertTrue("equals() Test", _charArray.equals((CharSequence)"Test"));
	}
	
	@Test
	public void testIndexOf(){
		assertEquals("Index Of T Is 0", 0, _charArray.indexOf('T'));
		assertEquals("Index Of e Is 1", 1, _charArray.indexOf('e'));
		assertEquals("Index Of s Is 2", 2, _charArray.indexOf('s'));
		assertEquals("Index Of t Is 3", 3, _charArray.indexOf('t'));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of T Is 0", 0, _charArray.indexOf('T'));
		assertEquals("Index Of e Is 1", 1, _charArray.indexOf('e'));
		assertEquals("Index Of s Is 2", 2, _charArray.indexOf('s'));
		assertEquals("Index Of t Is 3", 3, _charArray.indexOf('t'));
	}
	
	@Test
	public void testIndexOfWithCharSequence(){
		assertEquals("Index Of T Is 0", 0, _charArray.indexOf("Te"));
		assertEquals("Index Of e Is 1", 1, _charArray.indexOf("es"));
		assertEquals("Index Of s Is 2", 2, _charArray.indexOf("st"));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of Te Is 0", 0, _charArray.indexOf("Te"));
		assertEquals("Index Of es Is 1", 1, _charArray.indexOf("es"));
		assertEquals("Index Of st Is 2", 2, _charArray.indexOf("st"));
	}
	
	@Test
	public void testIndexOfWithFromIndex(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of T Is 4", 4, _charArray.indexOf('T',4));
		assertEquals("Index Of e Is 5", 5, _charArray.indexOf('e',4));
		assertEquals("Index Of s Is 6", 6, _charArray.indexOf('s',4));
		assertEquals("Index Of t Is 7", 7, _charArray.indexOf('t',4));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};		
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of T Is 4", 4, _charArray.indexOf('T',4));
		assertEquals("Index Of e Is 5", 5, _charArray.indexOf('e',4));
		assertEquals("Index Of s Is 6", 6, _charArray.indexOf('s',4));
		assertEquals("Index Of t Is 7", 7, _charArray.indexOf('t',4));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testIndexOfWithFromIndexThrowsExceptionIfOutOfBoundsOfView(){
		_charArray.indexOf('t',4);
	}
	
	@Test
	public void testIndexOfWithFromIndexWithCharSequence(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of Te Is 4", 4, _charArray.indexOf("Te",4));
		assertEquals("Index Of es Is 5", 5, _charArray.indexOf("es",4));
		assertEquals("Index Of st Is 6", 6, _charArray.indexOf("st",4));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of Te Is 4", 4, _charArray.indexOf("Te",4));
		assertEquals("Index Of es Is 5", 5, _charArray.indexOf("es",4));
		assertEquals("Index Of st Is 6", 6, _charArray.indexOf("st",4));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testIndexOfWithFromIndexWithCharSequenceThrowsExceptionIfOutOfBoundsOfView(){
		_charArray.indexOf("st",3);
	}
	
	@Test
	public void testLastIndexOf(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of T Is 4", 4, _charArray.lastIndexOf('T'));
		assertEquals("Index Of e Is 5", 5, _charArray.lastIndexOf('e'));
		assertEquals("Index Of s Is 6", 6, _charArray.lastIndexOf('s'));
		assertEquals("Index Of t Is 7", 7, _charArray.lastIndexOf('t'));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of T Is 4", 4, _charArray.lastIndexOf('T'));
		assertEquals("Index Of e Is 5", 5, _charArray.lastIndexOf('e'));
		assertEquals("Index Of s Is 6", 6, _charArray.lastIndexOf('s'));
		assertEquals("Index Of t Is 7", 7, _charArray.lastIndexOf('t'));
	}
	
	@Test
	public void testLastIndexOfWithCharSequence(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of Te Is 4", 4, _charArray.lastIndexOf("Te"));
		assertEquals("Index Of es Is 5", 5, _charArray.lastIndexOf("es"));
		assertEquals("Index Of st Is 6", 6, _charArray.lastIndexOf("st"));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);
		
		assertEquals("Index Of Te Is 4", 4, _charArray.lastIndexOf("Te"));
		assertEquals("Index Of es Is 5", 5, _charArray.lastIndexOf("es"));
		assertEquals("Index Of st Is 6", 6, _charArray.lastIndexOf("st"));
	}
	
	@Test
	public void testLastIndexOfWithFromIndex(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of T Is 0", 0, _charArray.lastIndexOf('T',3));
		assertEquals("Index Of e Is 1", 1, _charArray.lastIndexOf('e',3));
		assertEquals("Index Of s Is 2", 2, _charArray.lastIndexOf('s',3));
		assertEquals("Index Of t Is 3", 3, _charArray.lastIndexOf('t',3));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);

		assertEquals("Index Of T Is 0", 0, _charArray.lastIndexOf('T',3));
		assertEquals("Index Of e Is 1", 1, _charArray.lastIndexOf('e',3));
		assertEquals("Index Of s Is 2", 2, _charArray.lastIndexOf('s',3));
		assertEquals("Index Of t Is 3", 3, _charArray.lastIndexOf('t',3));		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testLastIndexOfWithFromIndexThrowsExceptionIfOutOfBoundsOfView(){
		_charArray.lastIndexOf('t',4);
	}
	
	@Test
	public void testLastIndexOfWithFromIndexWithCharSequence(){
		_charArray = new CharArray("TestTest");
		
		assertEquals("Index Of Te Is 0", 0, _charArray.lastIndexOf("Te",3));
		assertEquals("Index Of es Is 1", 1, _charArray.lastIndexOf("es",3));
		assertEquals("Index Of st Is 2", 2, _charArray.lastIndexOf("st",3));
		
		// Test On A View of An Array as Well
		_primitiveCharArray = new char[]{'x','x','x','x','T','e','s','t','T','e','s','t','x','x','x','x'};
		_charArray.setArray(_primitiveCharArray, 4, 8);

		assertEquals("Index Of Te Is 0", 0, _charArray.lastIndexOf("Te",3));
		assertEquals("Index Of es Is 1", 1, _charArray.lastIndexOf("es",3));
		assertEquals("Index Of st Is 2", 2, _charArray.lastIndexOf("st",3));		
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testLastIndexOfWithFromIndexWithCharSequenceThrowsExceptionIfOutOfBoundsOfView(){
		_charArray.lastIndexOf("st",3);
	}
	
	@Test
	public void testSetArrayWithViewPointSet(){
		_charArray.setArray(_primitiveCharArray, 1, 2);
		assertTrue("Array Equals Test", Arrays.equals(_primitiveCharArray, _charArray.array()));
		assertEquals("CharArray View Is es", "es", _charArray.toString());
	}
	
	@Test
	public void testSubsequence(){
		assertEquals("subSequence() es", "es", _charArray.subSequence(1, 3).toString());
	}
	
	@Test
	public void testToDouble(){
		_charArray = new CharArray("1.0");
		assertEquals("toDouble() Is 1.0", 1.0, _charArray.toDouble(), 0);
	}

	@Test(expected=NumberFormatException.class)
	public void testToDoubleThrowsExceptionIfCharArrayDoesNotRepresentADouble(){
		_charArray = new CharArray("!@#$");
		_charArray.toDouble();
	}
	
	@Test
	public void testToFloat(){
		_charArray = new CharArray("1.0");
		assertEquals("toFloat() Is 1.0", 1.0f, _charArray.toFloat(), 0);
	}
	
	@Test(expected=NumberFormatException.class)
	public void testToFloatThrowsExceptionIfCharArrayDoesNotRepresentAFloat(){
		_charArray = new CharArray("!@#$");
		_charArray.toFloat();
	}
	
	@Test
	public void testToInt(){
		_charArray = new CharArray("1");
		assertEquals("toInt() Is 1", 1, _charArray.toInt());
	}
	
	@Test(expected=NumberFormatException.class)
	public void testToIntThrowsExceptionIfCharArrayDoesNotRepresentAnInt(){
		_charArray = new CharArray("!@#$");
		_charArray.toInt();
	}
	
	@Test
	public void testToLong(){
		_charArray = new CharArray("1");
		assertEquals("toLong() Is 1", 1L, _charArray.toLong());
	}
	
	@Test(expected=NumberFormatException.class)
	public void testToLongThrowsExceptionIfCharArrayDoesNotRepresentALong(){
		_charArray = new CharArray("!@#$");
		_charArray.toLong();
	}
	
	@Test
	public void testToString(){
		assertEquals("toString() Is Test", "Test", _charArray.toString());
	}
}
