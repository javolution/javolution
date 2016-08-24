/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_LANG_STRING_TEST_HPP
#define _JAVOLUTION_LANG_STRING_TEST_HPP

#include "javolution/lang/String.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace lang {
        class StringTest_API;
    }
}

class javolution::lang::StringTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::lang::StringTest_API)
    ADD_TEST(testValueOf_Object)
    ADD_TEST(testValueOf_wchars)
    ADD_TEST(testValueOf_wstring)
    ADD_TEST(testValueOf_chars)
    ADD_TEST(testValueOf_string)
    ADD_TEST(testValueOf_wchar)
    ADD_TEST(testValueOf_char)
    ADD_TEST(testValueOf_int8)
    ADD_TEST(testValueOf_int16)
    ADD_TEST(testValueOf_int32)
    ADD_TEST(testValueOf_int64)
    ADD_TEST(testValueOf_float32)
    ADD_TEST(testValueOf_float64)
    ADD_TEST(testValueOf_boolean)
    ADD_TEST(testLength)
    ADD_TEST(testSubstring_int32)
    ADD_TEST(testSubstring_int32_int32)
    ADD_TEST(testCharAt_int32)
    ADD_TEST(testConcat_String)
    ADD_TEST(testStartsWith_String)
    ADD_TEST(testStartsWith_String_int32)
    ADD_TEST(testEquals_Object)
    ADD_TEST(testEquals_String)
    ADD_TEST(testHashCode)
    ADD_TEST(testToUTF8)
    ADD_TEST(testIntern)
    ADD_TEST(testToWString)
    ADD_TEST(testToString)
    END_SUITE()

    void testValueOf_Object();
    void testValueOf_wchars();
    void testValueOf_wstring();
    void testValueOf_chars();
    void testValueOf_string();
    void testValueOf_wchar();
    void testValueOf_char();
    void testValueOf_int8();
    void testValueOf_int16();
    void testValueOf_int32();
    void testValueOf_int64();
    void testValueOf_float32();
    void testValueOf_float64();
    void testValueOf_boolean();
    void testLength();
    void testSubstring_int32();
    void testSubstring_int32_int32();
    void testCharAt_int32();
    void testConcat_String();
    void testStartsWith_String();
    void testStartsWith_String_int32();
    void testEquals_Object();
    void testEquals_String();
    void testHashCode();
    void testToUTF8();
    void testIntern();
    void testToWString();
    void testToString();

};

#endif
