/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/lang/StringTest.hpp"
#include "javolution/lang/Boolean.hpp"

using namespace javolution::lang;

// For portability reasons (issues with Visual Studio),
// non-ascii literals characters are represented using their Unicode value.

void StringTest_API::testValueOf_Object() {
	Object obj = Boolean_API::valueOf(true);
    assertEquals(JAVOLUTION_LINE_INFO, (String) "true", String_API::valueOf(obj));
}
void StringTest_API::testValueOf_wchars() {
	Type::wchar wchars[] = L"1234567890\x00A9";
    assertEquals(JAVOLUTION_LINE_INFO, 11, String_API::valueOf(wchars)->length());
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf(wchars)->toWString() == std::wstring(wchars));
}
void StringTest_API::testValueOf_wstring() {
	std::wstring wstr = L"1234567890\x00A9";
    assertEquals(JAVOLUTION_LINE_INFO, 11, String_API::valueOf(wstr)->length());
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf(wstr)->toWString() == wstr);
}
void StringTest_API::testValueOf_chars() {
	char chars[] = "123456789012345678901234567890";
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf(chars)->toUTF8() == std::string(chars));
}
void StringTest_API::testValueOf_string() {
	std::string str = "123456789012345678901234567890";
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf(str)->toUTF8() == str);
}
void StringTest_API::testValueOf_wchar() {
	Type::wchar c = L'\x00A9'; // Copyright character '©'
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"\x00A9", String_API::valueOf(c));
}
void StringTest_API::testValueOf_char() {
	char c = 'a';
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"a", String_API::valueOf(c));
}
void StringTest_API::testValueOf_int8() {
	Type::int8 i = -128;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-128", String_API::valueOf(i));
}
void StringTest_API::testValueOf_int16() {
	Type::int32 i = -32768;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-32768", String_API::valueOf(i));
}
void StringTest_API::testValueOf_int32() {
    Type::int64 i = -128;
    i *= 256 * 256 * 256;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-2147483648", String_API::valueOf(i));
}
void StringTest_API::testValueOf_int64() {
    Type::int64 i = -128 * 256;
    i *= 256 * 256 * 256;
    i *= 256 * 256 * 256;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-9223372036854775808", String_API::valueOf(i));
}
void StringTest_API::testValueOf_float32() {
    Type::float32 f = -1.2345f;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-1.2345", String_API::valueOf(f));
}
void StringTest_API::testValueOf_float64() {
    Type::float64 f = -1.2345;
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"-1.2345", String_API::valueOf(f));
}
void StringTest_API::testValueOf_boolean() {
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"true", String_API::valueOf(true));
    assertEquals(JAVOLUTION_LINE_INFO, (String) L"false", String_API::valueOf(false));
}
void StringTest_API::testLength() {
    assertEquals(JAVOLUTION_LINE_INFO, 0, String_API::valueOf("")->length());
    assertEquals(JAVOLUTION_LINE_INFO, 1, String_API::valueOf(L"\x00A9")->length());
}
void StringTest_API::testSubstring_int32() {
    assertEquals(JAVOLUTION_LINE_INFO, (String) "bc", String_API::valueOf("abc")->substring(1));
}
void StringTest_API::testSubstring_int32_int32() {
    assertEquals(JAVOLUTION_LINE_INFO, (String) "b", String_API::valueOf("abc")->substring(1,2));
}
void StringTest_API::testCharAt_int32() {
    assertEquals(JAVOLUTION_LINE_INFO, L'\x00A9', String_API::valueOf(L" \x00A9 ")->charAt(1));
}
void StringTest_API::testConcat_String() {
	String str1 = "This is my first string.";
	String str2 = "This is my second string.";
	String expected = "This is my first string.This is my second string.";
    assertEquals(JAVOLUTION_LINE_INFO, expected, str1->concat(str2));
}
void StringTest_API::testStartsWith_String() {
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf("abc")->startsWith("abc"));
    assertFalse(JAVOLUTION_LINE_INFO, String_API::valueOf("abc")->startsWith("abcd"));
}
void StringTest_API::testStartsWith_String_int32() {
    assertTrue(JAVOLUTION_LINE_INFO, String_API::valueOf(" abc")->startsWith("abc", 1));
}
void StringTest_API::testEquals_Object() {
	Object obj1 = (String) "xyz";
	Object obj2 = (String) "xyz";
    assertTrue(JAVOLUTION_LINE_INFO, obj1->equals(obj2));
}
void StringTest_API::testEquals_String() {
	String str1 = "xyz";
	String str2 = "xyz";
	String str3 = "xyv";
    assertTrue(JAVOLUTION_LINE_INFO, str1->equals(str2));
    assertFalse(JAVOLUTION_LINE_INFO, str1->equals(str3));
}
void StringTest_API::testHashCode() {
    assertEquals(JAVOLUTION_LINE_INFO, 0, String_API::valueOf("")->hashCode());
}
void StringTest_API::testToUTF8() {
	String copyright = L"Copyright \x00A9 2011";
	std::string expected = "Copyright \x00C2\x00A9 2011";
    assertTrue(JAVOLUTION_LINE_INFO, expected == copyright->toUTF8());
}
void StringTest_API::testIntern() {
	String copyright1 = L"Copyright \x00A9 2011";
	String copyright2 = L"Copyright \x00A9 2011";
	String copyright3 = copyright2->intern();
    assertEquals(JAVOLUTION_LINE_INFO, copyright1, copyright2);
    assertNotSame(JAVOLUTION_LINE_INFO, copyright1, copyright2);
    assertSame(JAVOLUTION_LINE_INFO, copyright2, copyright3);
}
void StringTest_API::testToWString() {
	String copyright = L"Copyright \x00A9 2011";
	std::wstring expected = L"Copyright \x00A9 2011";
    assertTrue(JAVOLUTION_LINE_INFO, expected == copyright->toWString());
}
void StringTest_API::testToString() {
	String copyright = L"Copyright \x00A9 2011";
    assertSame(JAVOLUTION_LINE_INFO, copyright, copyright->toString());
}
